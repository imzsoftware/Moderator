package com.imz.praj.processor;

import com.imz.praj.data.AccountTargetDao;
import com.imz.praj.data.impl.PrajReportDaoImpl;
import com.imz.praj.data.obj.AccountTarget;
import com.traq.beanobject.QuickViewObj;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.LiveTrack;
import com.traq.common.data.entity.User;
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

public class DBSummaryProcessor_ORG extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    private List<LiveTrack> liveTrackListprimary = null;
    private RequestMessage requestMessage;
    private String request;
    private List<LiveTrack> liveTrackListsecondary = null;

    private Map<String, String> liveTrackMapprimary = null;
    private Map<String, String> liveTrackMapsecondary = null;

    private Map<Long, Device> deviceMap = new HashMap<>();
    private Map<String, String> dataList = new HashMap<>();
    private QuickViewObj quickView = new QuickViewObj();
    JSONArray accArray = null;
    JSONArray devArray = null;

    SimpleDateFormat outputPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    RequestBean rb;

    Long totalDevice = Long.valueOf(0L);
    long curTime = (new Date()).getTime();
    long timeDiff = 19800*1000;  // Value in milliseconds 5Hr 30min from GMT
    long offLineTime = 1000*60*60*24;
    List<AccountTarget> targetList = null;

    boolean isDetailed = false;

    private String stdData = "<traq><lat>0.0</lat><lng>0.0</lng><disha>0</disha><speed>0</speed><ign>N</ign><distance></distance><fuel></fuel><temp></temp><ac></ac><door></door><reserved></reserved><gps></gps><satellites>0</satellites><orgts></orgts><cts></cts><address></address><deviceid></deviceid><accid></accid><imb>N</imb><powsts></powsts><battery>0</battery><devip></devip><devport></devport><servip></servip><servport></servport><devicetypeid></devicetypeid></traq>";

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {

        this.responseMessage = responseMessage;
    }

    public DBSummaryProcessor_ORG(ResponseMessage responseMessage) {

        this.responseMessage = responseMessage;
    }

    public DBSummaryProcessor_ORG() {
    }

    public String executeXML(RequestBean _rb) {
        return request;
    }

    public String executeJSON(RequestBean _rb) {
        rb = _rb;
        this.request = this.rb.getRequest();

        RequestHandler rh = new RequestHandler();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        String response = "";

        Map<Long, Account> accountMap = null;
        try {
            JSONObject object = new JSONObject(request);
            JSONObject reqObj = object.getJSONObject("request");
            requestMessage = rh.getRequest(reqObj, TagValues.getNodeValue(object, "requesttype"));

            if (requestMessage.getIpaddress() == null) {
                throw new InvalidRequest("Ipaddess should not be null");
            }

            responseMessage = new ResponseMessage();
            responseMessage.setMessagetype(requestMessage.getMessagetype());
            responseMessage.setResponsects(sdf.format(new Date()));
            responseMessage.setTransid(rb.getTransid());
            responseMessage.setVendorcode(rb.getVendorCode());

            UserDao userDao = (UserDao) ApplicationBeanContext.getInstance().getBean("userDao");
            User user = userDao.login(requestMessage.getUsername());
            boolean isValid = validateUser(user, requestMessage.getPin());
            if (isValid) {
                AccountDao accountDao = (AccountDao) ApplicationBeanContext.getInstance().getBean("accountDao");
                DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
                AccountTargetDao accountTargetDao = (AccountTargetDao)ApplicationBeanContext.getInstance().getBean("accountTargetDao");

                boolean isHo = false;
                List<Account> accountList = null;
                List<Long> accIds = new ArrayList<>();
                accArray =  TagValues.getArrayValue(reqObj, Constants.NODEACCOUNT);
                devArray =  TagValues.getArrayValue(reqObj, Constants.NODEASSET);
                if (devArray != null && devArray.length()>0) {
                    for (int i=0;i<devArray.length();i++){
                        Device dev = deviceDao.getDeviceById(devArray.getLong(i));
                        deviceMap.put(devArray.getLong(i), dev);
                        String data = hget(dev.getAccId()+"", dev.getId()+"");
                        dataList.put(dev.getId()+"", data);
                    }
                    targetList = accountTargetDao.findByPanchayat(accIds);
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
                            accIds.add(accArray.getLong(i));
                        }else{
                            //accIds.add(accArray.getLong(i));
                            List<Account> accounts = accountDao.findAllChildren(accArray.getLong(i));
                            for(Account acc : accounts){
                                if("L5".equalsIgnoreCase(acc.getType())){
                                    accountList.add(BaseInitializer.getAccountMap().get(acc.getId()));
                                    accIds.add(acc.getId());
                                    List<Device> devices = deviceDao.getDeviceByAccount(acc.getId());
                                    for(Device dev : devices){
                                        deviceMap.put(dev.getId(), dev);
                                    }
                                }
                            }
                        }
                    }
                    targetList = accountTargetDao.findByPanchayat(accIds);
                    dataList.putAll(hgetAll(accountList));
                    deviceMap = deviceDao.getModifiedDeviceMapByAcc(accountList);
                }else{
                    throw new NoRecordFoundException();
                }

                liveTrackMapprimary = new HashMap<>();
                if (dataList != null && dataList.size() > 0) {
                    redisData();
                }

                if (this.liveTrackMapprimary != null && this.liveTrackMapprimary.size() > 0) {
                    this.liveTrackListprimary = createLiveTrackList(this.liveTrackMapprimary);
                    if (this.liveTrackListprimary.size() > 1)
                        Collections.sort(this.liveTrackListprimary, LiveTrack.timeComparatorDesc);

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
        }catch (EntityException ee) {
            this.responseMessage.setResultcode(ee.getResultCode(Integer.valueOf(0)));
            this.responseMessage.setResultDescription(ee.getDescription());
            this.responseMessage.setTimestamp((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(new Date()));

        }catch (Exception ex) {
            this.responseMessage.setResultcode(Integer.valueOf(206));
            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            ex.printStackTrace();
        } finally {
            if (this.responseMessage.getResultcode().intValue() == 0) {
                response = generateJSONResponse(accountMap);
            } else {
                response = generateFailureResponse(this.responseMessage);
            }
        }
        return response;
    }

        public String generateJSONResponse(Map<Long, Account> accountMap) {
            JSONObject mainObj = new JSONObject();
            Long offline = Long.valueOf(0L), online = Long.valueOf(0L);
            try {
                JSONObject jsonObject = responseHeader(this.requestMessage, this.responseMessage);
                jsonObject.put("transid", this.responseMessage.getTransid());

                Long nonFuncIot = 0L;

                Long target = 0L;
                if(targetList != null && targetList.size()>0){
                    for(AccountTarget accountTarget : targetList){
                        target = target + accountTarget.getTotalDevices();
                    }
                }else{
                    target = devArray.length() +0L;
                }

                if (liveTrackListprimary != null) {
                    PrajReportDaoImpl prp = new PrajReportDaoImpl();
                    List <Long> nonFuncDevList = new ArrayList<>();

                    for (LiveTrack liveTrack : liveTrackListprimary) {
                        requestMessage.setAssetId(Long.parseLong(liveTrack.getDeviceId()));
                        Long lastUpdated = liveTrack.getOrgtsMillis();
                        if(lastUpdated == 0){
                            info("orgts= "+liveTrack.getOrgTs() +" DeviceID= "+liveTrack.getDeviceId());
                            Date time =  getDataPattern().parse(liveTrack.getOrgTs());
                            lastUpdated = time.getTime();
                        }
                        //info("curTime= "+curTime + " lastUpdated= " +lastUpdated +" DeviceID= "+liveTrack.getDeviceId());
                        //if ((curTime - timeDiff - lastUpdated) > offLineTime) {
                        if ((curTime - lastUpdated) > offLineTime) {
                            nonFuncDevList.add(requestMessage.getAssetId());
                            nonFuncIot++;
                        }
                    }
                    //nonFuncIot = prp.reviewReport(requestMessage,nonFuncDevList);

                }

                this.quickView.setStop(offline);
                this.quickView.setOnline(deviceMap.size()-nonFuncIot);
                this.quickView.setOffline(nonFuncIot);
                JSONObject summary = new JSONObject();
                summary.put(Constants.NODETOTAL, target);
                summary.put(Constants.NODERUNNING, deviceMap.size());
                //summary.put(Constants.NODERUNNING, quickView.getTotal());
                summary.put(Constants.NODEOFFLINE, quickView.getOffline());
                summary.put(Constants.NODEONLINE, quickView.getOnline());

                summary.put("outofreach", nonFuncIot);
                jsonObject.put("summary",summary);
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

    private List<LiveTrack> createLiveTrackList(Map<String, String> map) throws Exception {
                List<LiveTrack> liveTrackList = new ArrayList();
                try {
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        String record = entry.getValue();
                        LiveTrack track = new LiveTrack();
                        track.setAccId(Long.getLong(TagValues.getNodeValue(record, "accid")));
                        track.setDeviceId(entry.getKey());

                        track.setIgnition(TagValues.getNodeValue(record, "ign"));
                        if (checkNullAndEmpty(TagValues.getNodeValue(record, "orgts"))){
                            Date date = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(TagValues.getNodeValue(record, "orgts"));
                            track.setOrgTs(this.outputPattern.format(date));

                            try {
                                Long orgMillis = Long.parseLong(TagValues.getNodeValue(record, Constants.NODEORIGINTSMILLI));
                                track.setOrgtsMillis(orgMillis);
                            } catch (Exception e) {
                                track.setOrgtsMillis(0L);
                            }
                            try {
                                Long ignTime = Long.parseLong(TagValues.getNodeValue(record, Constants.NODEIGNTIME));
                                if(ignTime >0) {
                                    track.setIgnTime(Utility.convertSecToDurationHH((curTime - (ignTime - timeDiff)) / 1000));
                                }else{
                                    track.setIgnTime("Not Available");
                                }
                            } catch (Exception e) {
                                track.setIgnTime("0 Sec");
                            }

                            try {
                                Long powTime = Long.parseLong(TagValues.getNodeValue(record, Constants.NODEPOWTIME));
                                if(powTime >0) {
                                    track.setPowTime(Utility.convertSecToDurationHH((curTime-(powTime-timeDiff))/1000));
                                }else{
                                    track.setIgnTime("Not Available");
                                }
                            } catch (Exception e) {
                                track.setPowTime("0 Sec");
                            }

                            //track = considerGpsNotFixedData(track, record);

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
                    jsonObject.put(Constants.NODEVENDORCODE, rm.getVendorcode());
                    mainObj.put("response", jsonObject);
                } catch (JSONException jSONException) {
                }
                return mainObj.toString();
            }


    private void redisData() {
        try {
            for (Map.Entry<Long, Device> device : deviceMap.entrySet()) {
                totalDevice++;
                String data = null;
                if (dataList.containsKey(device.getKey().toString())) {
                    data = dataList.get(device.getKey().toString());
                } else {
                    data = hget(device.getValue().getAccId().toString(), device.getKey().toString());
                }
                //info("DBSummaryProcessor .... Data = "+data);
                if (data != null) {
                    liveTrackMapprimary.put(device.getKey().toString(), data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
