package com.imz.praj.processor;

import com.imz.praj.data.impl.PrajReportDaoImpl;
import com.imz.praj.data.obj.PrajReportData;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.*;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.exceptions.*;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.RequestHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import com.traq.util.Utility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class GeneralReportProcessor extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    private List<LiveTrack> liveTrackListprimary = null;
    private RequestMessage requestMessage;
    private String request;
    private Map<String, String> liveTrackMapprimary = null;

    private Map<Long, Device> deviceMap = new HashMap<>();
    private Map<String, String> dataList = new HashMap<>();

    SimpleDateFormat outputPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    RequestBean rb;
    long curTime = (new Date()).getTime();

    long timeDiff = 19800*1000;  // Value in milliseconds 5Hr 30min from GMT
    long offLineTime = 1000*60*60*24;
    long nonFuncSch = 1000*60*60*24;
    Long totalDevice = Long.valueOf(0L);

    private String stdData = "<traq><lat>0.0</lat><lng>0.0</lng><disha>0</disha><speed>0</speed><ign>N</ign><distance></distance><fuel></fuel><temp></temp><ac></ac><door></door><reserved></reserved><gps></gps><satellites>0</satellites><orgts></orgts><cts></cts><address></address><deviceid></deviceid><accid></accid><imb>N</imb><powsts></powsts><battery>0</battery><devip></devip><devport></devport><servip></servip><servport></servport><devicetypeid></devicetypeid></traq>";

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public GeneralReportProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public GeneralReportProcessor() {}

    public String executeXML(RequestBean _rb) {
        return this.request;
    }

    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();

        RequestHandler rh = new RequestHandler();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        String response = "";
        //Map<Long, Account> accountMap = null;
        try {
            JSONObject object = new JSONObject(this.request);
            JSONObject reqObj = object.getJSONObject("request");
            requestMessage = rh.getRequest(reqObj, TagValues.getNodeValue(object, "requesttype"));
            requestMessage.setStartDate(TagValues.getNodeValue(reqObj, Constants.NODESTARTDATE));
            requestMessage.setEndDate(TagValues.getNodeValue(reqObj, Constants.NODEENDDATE));
            JSONArray accArray =  TagValues.getArrayValue(reqObj, Constants.NODEACCOUNT);
            JSONArray devArray =  TagValues.getArrayValue(reqObj, Constants.NODEASSET);

            Date date = getDataPattern().parse(requestMessage.getStartDate());
            this.requestMessage.setStartDate(getDbPattern().format(date));
            date = getDataPattern().parse(requestMessage.getEndDate());
            requestMessage.setEndDate(getDbPattern().format(date));

            if (requestMessage.getIpaddress() == null) {
                throw new InvalidRequest("Ipaddess should not be null");
            }
            responseMessage = new ResponseMessage();
            responseMessage.setMessagetype(requestMessage.getMessagetype());
            responseMessage.setResponsects(sdf.format(new Date()));
            responseMessage.setTransid(rb.getTransid());

            UserDao userDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");
            User user = userDao.login(requestMessage.getUsername());

            boolean isValid = validateUser(user, requestMessage.getPin());

            if (isValid) {
                DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
                AccountDao accountDao = (AccountDao)ApplicationBeanContext.getInstance().getBean("accountDao");

                List<Account> accountList = null;

                if (devArray != null && devArray.length()>0) {
                    for (int i=0;i<devArray.length();i++){
                        Device dev = deviceDao.getDeviceById(devArray.getLong(i));
                        deviceMap.put(devArray.getLong(i), dev);
                        String data = hget(dev.getAccId()+"", dev.getId()+"");
                        dataList.put(dev.getId()+"", data);
                    }
                }else if (accArray != null) {
                    accountList = new ArrayList<>();
                    for (int i=0;i<accArray.length();i++){
                        Account account = BaseInitializer.getAccountMap().get(accArray.getLong(i));
                        if("L5".equalsIgnoreCase(account.getType())){
                            accountList.add(BaseInitializer.getAccountMap().get(accArray.getLong(i)));
                            List<Device> devices = deviceDao.getDeviceByAccount(accArray.getLong(i));
                            for(Device dev : devices){
                                deviceMap.put(dev.getId(), dev);
                            }
                        }else{
                            List<Account> accounts = accountDao.findAllChildren(accArray.getLong(i));
                            for(Account acc : accounts){
                                if("L5".equalsIgnoreCase(acc.getType())){
                                    accountList.add(BaseInitializer.getAccountMap().get(acc.getId()));
                                    List<Device> devices = deviceDao.getDeviceByAccount(acc.getId());
                                    for(Device dev : devices){
                                        deviceMap.put(dev.getId(), dev);
                                    }
                                }
                            }
                        }
                    }
                    dataList.putAll(hgetAll(accountList));
                }

                this.liveTrackMapprimary = new HashMap<>();

                if (dataList != null && dataList.size() > 0) {
                    redisData();
                }

                if (this.liveTrackMapprimary != null && this.liveTrackMapprimary.size() > 0) {
                    liveTrackListprimary = createLiveTrackList(this.liveTrackMapprimary);

                    if (this.liveTrackListprimary.size() > 1)
                        Collections.sort(this.liveTrackListprimary, LiveTrack.timeComparatorDesc);
                } else {
                    throw new NoRecordFoundException();
                }
            } else {
                if (!user.getPin().equals(this.requestMessage.getPin()))
                    throw new InvalidPassword();
                if (!user.getStatus().getCode().equals("A")) {
                    throw new InActiveUser();
                }
                throw new InvalidUser();
            }

            this.responseMessage.setResultcode(Integer.valueOf(0));
            this.responseMessage.setResultDescription("Success");
        } catch (EntityException ee) {
            this.responseMessage.setResultcode(ee.getResultCode(Integer.valueOf(0)));
            this.responseMessage.setResultDescription(ee.getDescription());
            this.responseMessage.setTimestamp((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(new Date()));
        }
        catch (Exception ex) {
            this.responseMessage.setResultcode(Integer.valueOf(206));
            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            ex.printStackTrace();
        } finally {
            if (this.responseMessage.getResultcode().intValue() == 0) {
                if(requestMessage.getMessagetype().equals("REVIEWREPORT")){
                    response = generateReviewResponse();
                }else {
                    response = generateJSONResponse();
                }
            } else {
                response = generateFailureResponse(this.responseMessage);
            }
        }
        return response;
    }

    private void redisData() {
        try {
            for (Map.Entry<String, String> device : dataList.entrySet()) {
                totalDevice++;
                String data =  dataList.get(device.getKey().toString());

                if (data != null) {
                    liveTrackMapprimary.put(device.getKey().toString(), data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String generateJSONResponse() {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = responseHeader(requestMessage, this.responseMessage);
            jsonObject.put("transid", responseMessage.getTransid());
            jsonObject.put(Constants.NODESTARTDATE, requestMessage.getStartDate());
            jsonObject.put(Constants.NODEENDDATE, requestMessage.getEndDate());

            JSONArray arrayprimary = new JSONArray();
            if (this.liveTrackListprimary != null) {
                PrajReportDaoImpl prp = new PrajReportDaoImpl();

                for (LiveTrack liveTrack : this.liveTrackListprimary) {
                    requestMessage.setAssetId(Long.parseLong(liveTrack.getDeviceId()));
                    Long lastUpdated = liveTrack.getOrgtsMillis();
                    Long lastOperated = 0L;
                    try {
                        lastOperated = Long.parseLong(liveTrack.getIgnTime());
                    }catch (Exception e){
                        liveTrack.setIgnTime("0");
                        //Date ts = (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).parse(liveTrack.getOrgTs());
                        //lastOperated = ts.getTime();
                    }
                    JSONObject refineData = new JSONObject();
                    if(requestMessage.getMessagetype().equals("REVIEWREPORT")){
                        PrajReportData reportData = prp.reviewReport(requestMessage);
                    }else {
                        if ("NONFUNC".equalsIgnoreCase(requestMessage.getTranstype())
                                && (curTime - timeDiff - lastUpdated) > offLineTime) {
                            info("lastUpdated ... " + lastUpdated + "  ....... curTime ...... " + curTime);
                            PrajReportData reportData = prp.findNonFuncSchemeReport(requestMessage);
                            refineData = mergeData(reportData, liveTrack);
                            //refineData = nonFuncRep(liveTrack);
                            refineData.accumulate("status", "OFF");
                            arrayprimary.put(refineData);
                        } else if ("NONFUNCSCHEME".equalsIgnoreCase(requestMessage.getTranstype())) {
                            // if ((curTime - timeDiff - lastOperated) < offLineTime && (lastUpdated + nonFuncSch - timeDiff) > curTime) {
                            if ((lastUpdated + nonFuncSch - timeDiff) < curTime) {
                                info("lastOperated ... " + lastOperated + " lastUpdated ... " + lastUpdated + " ... curTime ...... " + curTime);
                                PrajReportData reportData = prp.findNonFuncSchemeReport(requestMessage);
                                refineData = mergeData(reportData, liveTrack);
                                refineData.accumulate("status", "OFF");
                                arrayprimary.put(refineData);
                            }
                        } else if ("".equalsIgnoreCase(requestMessage.getTranstype())) {
                            PrajReportData reportData = prp.findGenReport(requestMessage);
                            refineData = mergeData(reportData, liveTrack);

                            //info("lastUpdated ... "+lastUpdated +"  ....... curTime ...... "+curTime);
                            //if ((ts.getTime() +oneDay) >= curTime) {
                            //if ((lastUpdated + oneDay) >= curTime) {
                            if (refineData.getString("ign").equals("Y")) {
                                refineData.accumulate("status", "ON");
                            } else {
                                refineData.accumulate("status", "STOP");
                            }
                            arrayprimary.put(refineData);
                        }
                    }
                }
            }
            jsonObject.put("records", arrayprimary);

            mainObj.put("response", jsonObject);
        }
        catch (JSONException je) {
            je.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return mainObj.toString();
    }

    public String generateReviewResponse() {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = responseHeader(requestMessage, this.responseMessage);
            jsonObject.put("transid", responseMessage.getTransid());
            jsonObject.put(Constants.NODESTARTDATE, requestMessage.getStartDate());
            jsonObject.put(Constants.NODEENDDATE, requestMessage.getEndDate());

            JSONArray arrayprimary = new JSONArray();
            Long nonFuncIot = 0L;
            Long nonFuncSch = 0L;

            if (this.liveTrackListprimary != null) {
                PrajReportDaoImpl prp = new PrajReportDaoImpl();
                List <Long> nonFuncDevList = new ArrayList<>();
                List <Long> nonFuncSchList = new ArrayList<>();

                for (LiveTrack liveTrack : this.liveTrackListprimary) {
                    requestMessage.setAssetId(Long.parseLong(liveTrack.getDeviceId()));
                    Long lastUpdated = liveTrack.getOrgtsMillis();
                    Long lastOperated = 0L;
                    try {
                        lastOperated = Long.parseLong(liveTrack.getIgnTime());
                    }catch (Exception e){
                        liveTrack.setIgnTime("0");
                        //Date ts = (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).parse(liveTrack.getOrgTs());
                        //lastOperated = ts.getTime();
                    }

                    if ((curTime - timeDiff - lastUpdated) > offLineTime) {
                        nonFuncDevList.add(requestMessage.getAssetId());
                    }
                    if ((curTime - timeDiff - lastOperated) < offLineTime && (lastUpdated + nonFuncSch + timeDiff) > curTime) {
                        nonFuncSchList.add(requestMessage.getAssetId());
                    }
                }
                nonFuncIot = prp.reviewReport(requestMessage,nonFuncDevList);
                nonFuncSch = prp.reviewReport(requestMessage,nonFuncSchList);
            }
            jsonObject.put("funciot", deviceMap.size()-nonFuncIot);
            jsonObject.put("nonfunciot", nonFuncIot);
            jsonObject.put("nonfuncsch", nonFuncSch);
            jsonObject.put("funcsch", deviceMap.size()-nonFuncSch);

            mainObj.put("response", jsonObject);
        }
        catch (JSONException je) {
            je.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return mainObj.toString();
    }

    private JSONObject mergeData(PrajReportData reportData, LiveTrack liveTrack) throws JSONException, NullPointerException {
        JSONObject object = new JSONObject();
        try {
            Device device = this.deviceMap.get(Long.parseLong(liveTrack.getDeviceId()));
            //object = liveTrack.toJson();
            object.accumulate(Constants.NODEORIGINTS,liveTrack.getOrgTs());
            object.accumulate(Constants.NODEORIGINTSMILLI,liveTrack.getOrgtsMillis());
            object.accumulate(Constants.NODEIGNITION,liveTrack.getIgnition());
            object.accumulate(Constants.NODESPEED,liveTrack.getSpeed());
            if (device != null) {
                object.accumulate("name", device.getName());
                object.accumulate("vehnum", device.getLicenseno());

                if (null != device.getDeviceType()) {
                    object.accumulate("devicetype", device.getDeviceType().getName());
                }
                object.put("accid", device.getAccId());

                try {
                    Account acc = BaseInitializer.getAccountMap().get(reportData.getAccId());
                    object.accumulate("accname", acc.getName());
                    Account parent = BaseInitializer.getAccountMap().get(reportData.getBlockId());
                    object.accumulate(Constants.NODEBLOCK, parent.getName());
                    object.accumulate(Constants.NODEDISTRICT, BaseInitializer.getAccountMap().get(reportData.getDistId()).getName());

                    //Long quantity = prp.totalQuantity(device.getAccId(),device.getId(),date1,date2,acc.getClient());
                    object.accumulate(Constants.NODEQUANTITY, reportData.getTotalQty());
                } catch (Exception exception) {
                    error("GeneralReportProcessor .. NOT FOUND "+exception.getMessage());
                }

                object.accumulate("deviceid", device.getId());

                object.put("address", liveTrack.getLocation());
                Long lastUpdated = Long.parseLong(liveTrack.getIgnTime());
                if(lastUpdated > 0){
                    Date dt = new Date(lastUpdated - timeDiff);
                    String opr = (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).format(dt);
                    object.put(Constants.NODEIGNTIME, opr);
                }else{
                    object.put(Constants.NODEIGNTIME, "NA");
                }

                object.put(Constants.NODEPOWTIME, liveTrack.getPowTime());
                object.put(Constants.NODELATITUDE, liveTrack.getLatitude());
                object.put(Constants.NODELONGITUDE, liveTrack.getLongitude());
                try {
                    object.put("driver", Driver.toJson(device.getDriver()));
                } catch (Exception exception) {}
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    private JSONObject nonFuncRep(LiveTrack liveTrack) throws NullPointerException {
        JSONObject object = new JSONObject();
        try {
            Device device = this.deviceMap.get(Long.valueOf(Long.parseLong(liveTrack.getDeviceId())));
            //object = liveTrack.toJson();
            object.accumulate(Constants.NODEORIGINTS,liveTrack.getOrgTs());
            object.accumulate(Constants.NODEORIGINTSMILLI,liveTrack.getOrgtsMillis());
            object.accumulate(Constants.NODEIGNITION,liveTrack.getIgnition());
            object.accumulate(Constants.NODESPEED,liveTrack.getSpeed());
            if (device != null) {
                object.accumulate("name", device.getName());
                object.accumulate("vehnum", device.getLicenseno());

                if (null != device.getDeviceType()) {
                    object.accumulate("devicetype", device.getDeviceType().getName());
                }
                object.put("accid", device.getAccId());

                try {
                    Account acc = BaseInitializer.getAccountMap().get(liveTrack.getAccId());
                    object.accumulate("accname", acc.getName());
                    Account parent = BaseInitializer.getAccountMap().get(acc.getParentAccountId());
                    object.accumulate(Constants.NODEBLOCK, parent.getName());
                    object.accumulate(Constants.NODEDISTRICT, BaseInitializer.getAccountMap().get(parent.getParentAccountId()).getName());
                    object.accumulate(Constants.NODEQUANTITY, 0L);
                } catch (Exception exception) {}

                object.accumulate("deviceid", device.getId());

                object.put("address", liveTrack.getLocation());
                object.put(Constants.NODEIGNTIME, liveTrack.getIgnTime());
                object.put(Constants.NODEPOWTIME, liveTrack.getPowTime());
                object.put(Constants.NODELATITUDE, liveTrack.getLatitude());
                object.put(Constants.NODELONGITUDE, liveTrack.getLongitude());
                try {
                    object.put("driver", Driver.toJson(device.getDriver()));
                } catch (Exception exception) {}
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    private List<LiveTrack> createLiveTrackList(Map<String, String> map) throws Exception {
        List<LiveTrack> liveTrackList = new ArrayList();
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String record = entry.getValue();
                LiveTrack track = new LiveTrack();
                track.setAccId(Long.getLong(TagValues.getNodeValue(record, "accid")));
                track.setDeviceId(entry.getKey());

                try {
                    Long powTime = Long.parseLong(TagValues.getNodeValue(record, Constants.NODEPOWTIME));
                    track.setPowTime(Utility.convertSecToDurationHH((curTime-(powTime-timeDiff))/1000));
                } catch (Exception e) {
                    track.setPowTime("0 Sec");
                }
                track.setIgnition(TagValues.getNodeValue(record, "ign"));
                track.setIgnTime(TagValues.getNodeValue(record, Constants.NODEIGNTIME));
                try {
                    track.setOrgtsMillis(Long.parseLong(TagValues.getNodeValue(record, Constants.NODEORIGINTSMILLI)));
                }catch (NumberFormatException nfe){
                    track.setOrgtsMillis(0L);
                }
                if (checkNullAndEmpty(TagValues.getNodeValue(record, "orgts"))) {
                    Date date = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(TagValues.getNodeValue(record, "orgts"));
                    track.setOrgTs(this.outputPattern.format(date));
                    track.setTamper(TagValues.getNodeValue(record, "tamp"));
                    track.setAc(TagValues.getNodeValue(record, "ac"));
                    track.setPowStatus(TagValues.getNodeValue(record, "powsts"));
                    track = considerGpsNotFixedData(track, record);
                    liveTrackList.add(track);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return liveTrackList;
    }

    public String generateFailureResponse(ResponseMessage rm) {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("responsetype", rm.getMessagetype());
            jsonObject.put("resultcode", rm.getResultcode());
            jsonObject.put("resultdescription", rm.getResultDescription());
            jsonObject.put("transid", rm.getTransid());

            mainObj.put("response", jsonObject);
        } catch (JSONException jSONException) {}
        return mainObj.toString();
    }

    private LiveTrack considerGpsNotFixedData(LiveTrack track, String record) {
        try {
            try {
                track.setLatitude(Double.valueOf(Double.parseDouble(TagValues.getNodeValue(record, "lat"))));
            } catch (Exception e) {
                track.setLatitude(Double.valueOf(0.0D));
            }
            try {
                track.setLongitude(Double.valueOf(Double.parseDouble(TagValues.getNodeValue(record, "lng"))));
            } catch (Exception e) {
                track.setLongitude(Double.valueOf(0.0D));
            }
            //track.setLocation(TagValues.getNodeValue(record, "address").replaceAll(":", ","));
            //track.setGps(TagValues.getNodeValue(record, "gps"));

/*            if (TagValues.getNodeValue(record, "gps").equals("A")) {
                Long long_1 = this.gps, long_2 = this.gps = Long.valueOf(this.gps.longValue() + 1L);
            } else {
                Long long_1 = this.noGps, long_2 = this.noGps = Long.valueOf(this.noGps.longValue() + 1L);
            }
            if (!checkNullAndEmpty(track.getDistance())) {
                track.setDistance("0.0");
            }
            if ("NA".equals(track.getLocation())) {
                track.setLocation("Unnamed Road (0 m)");
            } else {
                long dist = Math.round(Double.parseDouble(track.getDistance()));
                track.setLocation(track.getLocation().replaceAll("Unnamed Road,", "") + " (" + dist + " m)");
            }
            if (getAppConfig().getGpsNotFixedData().equals("0") &&
                    TagValues.getNodeValue(record, "gps").equals("V")) {
                track.setLatitude(Double.valueOf(0.0D));
                track.setLongitude(Double.valueOf(0.0D));
                track.setLocation("GPS Not Set..");
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
        return track;
    }
}
