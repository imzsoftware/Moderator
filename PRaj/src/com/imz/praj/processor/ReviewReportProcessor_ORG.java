package com.imz.praj.processor;

import com.imz.praj.data.AccountTargetDao;
import com.imz.praj.data.impl.PrajReportDaoImpl;
import com.imz.praj.data.obj.AccountTarget;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReviewReportProcessor_ORG extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    //private List<LiveTrack> liveTrackListprimary = null;
    private RequestMessage requestMessage;
    private String request;
    private Map<String, String> liveTrackMapprimary = null;

    private Map<Long, Device> deviceMap = new HashMap<>();
    private Map<Long, Map<Long,Device>> deviceMapByDist = new HashMap<>();
    private Map<Long, Map<String,String>> deviceDataByDist = new HashMap<>();
    private Map<String, String> dataList = new HashMap<>();
    JSONArray accArray = null;

    SimpleDateFormat outputPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    RequestBean rb;
    long curTime = (new Date()).getTime();

    long timeDiff = 19800*1000;  // Value in milliseconds 5Hr 30min from GMT
    long offLineTime = 1000*60*60*24;
    List<AccountTarget> targetList = null;
    Map<String, String> reportMap = new HashMap<>();

    private String stdData = "<traq><lat>0.0</lat><lng>0.0</lng><disha>0</disha><speed>0</speed><ign>N</ign><distance></distance><fuel></fuel><temp></temp><ac></ac><door></door><reserved></reserved><gps></gps><satellites>0</satellites><orgts></orgts><cts></cts><address></address><deviceid></deviceid><accid></accid><imb>N</imb><powsts></powsts><battery>0</battery><devip></devip><devport></devport><servip></servip><servport></servport><devicetypeid></devicetypeid></traq>";

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public ReviewReportProcessor_ORG(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public ReviewReportProcessor_ORG() {}

    public String executeXML(RequestBean _rb) {
        return this.request;
    }

    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();
        responseMessage = new ResponseMessage();

        RequestHandler rh = new RequestHandler();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        String response = "";
        //Map<Long, Account> accountMap = null;
        try {
            JSONObject object = new JSONObject(this.request);
            JSONObject reqObj = object.getJSONObject("request");
            requestMessage = rh.getRequest(reqObj, TagValues.getNodeValue(object, "requesttype"));
            accArray =  TagValues.getArrayValue(reqObj, Constants.NODEACCOUNT);

            if (requestMessage.getIpaddress() == null) {
                throw new InvalidRequest("Ipaddess should not be null");
            }

            responseMessage.setMessagetype(requestMessage.getMessagetype());
            responseMessage.setResponsects(sdf.format(new Date()));
            responseMessage.setTransid(rb.getTransid());

            UserDao userDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");
            User user = userDao.login(requestMessage.getUsername());

            boolean isValid = validateUser(user, requestMessage.getPin());

            if (isValid) {
                DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
                AccountDao accountDao = (AccountDao)ApplicationBeanContext.getInstance().getBean("accountDao");
                AccountTargetDao accountTargetDao = (AccountTargetDao)ApplicationBeanContext.getInstance().getBean("accountTargetDao");

                List<Account> accountList = null;
                List<Long> accIds = new ArrayList<>();

                if (accArray != null) {
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
                            accIds.add(accArray.getLong(i));
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
                        dataList = new HashMap<>();
                        deviceMapByDist.put(accArray.getLong(i),deviceMap);
                        dataList.putAll(hgetAll(accountList));
                        deviceDataByDist.put(accArray.getLong(i),dataList);
                    }
                    targetList = accountTargetDao.findByDistrict(accIds);
                }else{
                    throw new NoRecordFoundException();
                }

/*                if (dataList != null && dataList.size() > 0) {
                    liveTrackListprimary = createLiveTrackList(dataList);
                } else {
                    throw new NoRecordFoundException();
                }*/
            } else {
                if (!user.getPin().equals(this.requestMessage.getPin()))
                    throw new InvalidPassword();
                if (!user.getStatus().getCode().equals("A")) {
                    throw new InActiveUser();
                }
                throw new InvalidUser();
            }

            responseMessage.setResultcode(0);
            responseMessage.setResultDescription("Success");
        } catch (EntityException ee) {
            responseMessage.setResultcode(ee.getResultCode(Integer.valueOf(0)));
            responseMessage.setResultDescription(ee.getDescription());
            responseMessage.setTimestamp((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(new Date()));
        }
        catch (Exception ex) {
            responseMessage.setResultcode(Integer.valueOf(206));
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            ex.printStackTrace();
        } finally {
            info(" responseMessage.getResultcode()       "+responseMessage.getResultcode());
            if (responseMessage.getResultcode() == 0) {
                response = generateReviewResponse();
            } else {
                response = generateFailureResponse(this.responseMessage);
            }
        }
        return response;
    }

    public String generateReviewResponse() {
        JSONObject mainObj = new JSONObject();
        JSONArray distArr = new JSONArray();
        try {
            JSONObject jsonObject = responseHeader(requestMessage, this.responseMessage);
            jsonObject.put("transid", responseMessage.getTransid());
            jsonObject.put(Constants.NODESTARTDATE, requestMessage.getStartDate());
            jsonObject.put(Constants.NODEENDDATE, requestMessage.getEndDate());

            Long nonFuncIot = 0L;
            Long nonFuncSch = 0L;
            Long target = 0L;
            if(targetList != null && targetList.size()>0){
                for(AccountTarget accountTarget : targetList){
                    target = target + accountTarget.getTotalDevices();
                }
            }

            if (deviceDataByDist != null) {
                PrajReportDaoImpl prp = new PrajReportDaoImpl();
                List <Long> nonFuncDevList = new ArrayList<>();
                List <Long> nonFuncSchList = new ArrayList<>();

                SimpleDateFormat redisKeyFormat = new SimpleDateFormat("yyyy-MM-dd");
                String reportDate = redisKeyFormat.format(new Date());
                String repKey ="PRAJ_REP_"+reportDate;
                reportMap = hgetAll(repKey);

                for (Map.Entry<Long, Map<String, String>> entry : deviceDataByDist.entrySet()) {
                    Long dist = entry.getKey();
                    Map <String, String> deviceData = entry.getValue();
                    JSONObject distObj = new JSONObject();
                    for (Map.Entry<String, String> dataEntry : deviceData.entrySet()) {
                        Long devId = Long.parseLong(dataEntry.getKey());
                        TagValues.getNodeValue(dataEntry.getValue(), Constants.NODEORIGINTSMILLI);

                        Long lastUpdated = Long.parseLong(TagValues.getNodeValue(dataEntry.getValue(), Constants.NODEORIGINTSMILLI));
                        Long lastOperated = 0L;
                        try {
                            lastOperated = Long.parseLong(TagValues.getNodeValue(dataEntry.getValue(), Constants.NODEIGNTIME));
                        } catch (Exception e) {
                            lastOperated = 0L;
                        }

                        if ((curTime - timeDiff - lastUpdated) > offLineTime) {
                            nonFuncDevList.add(requestMessage.getAssetId());
                        }
                        if ((curTime - timeDiff - lastOperated) < offLineTime && (lastUpdated + offLineTime + timeDiff) > curTime) {
                            nonFuncSchList.add(devId);
                        }
                    }
                    nonFuncIot = prp.reviewReport(requestMessage,nonFuncDevList);
                    nonFuncSch = prp.reviewReport(requestMessage,nonFuncSchList);
                    distObj.put("total", target);
                    distObj.put(Constants.NODEQUANTITY, deviceMap.size());
                    distObj.put("funciot", deviceMap.size()-nonFuncIot);
                    distObj.put("nonfunciot", nonFuncIot);
                    distObj.put("nonfuncsch", nonFuncSch);
                    distObj.put("funcsch", deviceMap.size()-nonFuncSch);
                    String name = BaseInitializer.getAccountMap().get(dist).getName();
                    distObj.put(Constants.NODEDISTRICT,name);
                    distObj.put(Constants.NODEACCOUNTID,dist);
                    distArr.put(distObj);
                }
                jsonObject.put(Constants.NODERECORDS,distArr);
            }
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
