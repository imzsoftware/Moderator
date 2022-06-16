package com.imz.praj.processor;

import com.imz.praj.data.impl.PrajReportDaoImpl;
import com.imz.praj.data.impl.RedisToMongoDaoImpl;
import com.imz.praj.entity.PrajReport;
import com.imz.praj.entity.ViewDetailRep;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.User;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.exceptions.*;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.RequestHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.common.xmlutils.XMLProcessor;
import com.traq.util.RequestBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class ResetOperationTypeProcessor extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    private RequestMessage requestMessage;
    private String request;

    SimpleDateFormat outputPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    RequestBean rb;
    long curTime = (new Date()).getTime();
    long timeDiff = 19800*1000;  // Value in milliseconds 5Hr 30min from GMT
    long oneDay = (24*60*60*1000);

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public ResetOperationTypeProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public ResetOperationTypeProcessor() {}

    public String executeXML(RequestBean _rb) {
        return this.request;
    }

    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();

        RequestHandler rh = new RequestHandler();
        Map<String, String> dataMap = new HashMap<>();
        Map<Long,PrajReport> prajReports = new HashMap<>();

        String response = "";
        //Map<Long, Account> accountMap = null;
        try {
            responseMessage = new ResponseMessage();
            JSONObject object = new JSONObject(this.request);
            JSONObject reqObj = object.getJSONObject("request");
            requestMessage = rh.getRequest(reqObj, TagValues.getNodeValue(object, "requesttype"));
            JSONArray accArray =  TagValues.getArrayValue(reqObj, Constants.NODEACCOUNT);

            responseMessage.setMessagetype(requestMessage.getMessagetype());
            responseMessage.setResponsects(sdf.format(new Date()));
            responseMessage.setTransid(rb.getTransid());

            UserDao userDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");
            User user = userDao.login(requestMessage.getUsername());

            boolean isValid = true; //validateUser(user, requestMessage.getPin());

            if (isValid) {
                DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
                AccountDao accountDao = (AccountDao)ApplicationBeanContext.getInstance().getBean("accountDao");

                List<Account> accountList = null;
                    Map<Long, Device> deviceMap = new HashMap<Long, Device>();
                    if (accArray != null) {
                        accountList = findLevel5Account(accountDao, accArray);
                        for (Account acc : accountList) {
                            List<Device> devices = deviceDao.getDeviceByAccount(acc.getId());
                            for (Device dev : devices) {
                                deviceMap.put(dev.getId(), dev);
                            }
                        }
                        RedisToMongoDaoImpl redisToMongoDao = new RedisToMongoDaoImpl();
                        prajReports = redisToMongoDao.wardReport(deviceMap,requestMessage.getStartDate(),requestMessage.getEndDate());
                        dataMap.putAll(hgetAll(accountList));

                        if(dataMap != null && dataMap.size() > 0){
                            Long currentTime = System.currentTimeMillis();
                            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                                String orgTime = TagValues.getNodeValue(entry.getValue(), Constants.NODEORIGINTSMILLI);
                                if(checkNullAndEmpty(orgTime)){
                                    if((Long.parseLong(orgTime)+(10*60*1000) < currentTime) && (TagValues.getNodeValue(entry.getValue(), Constants.NODEIGN).equals("Y"))){
                                        String value = XMLProcessor.replaceInsertXML(entry.getValue(),Constants.NODEIGN,"N");
                                        Map <String,String> map = new HashMap <String, String>();
                                        map.put(TagValues.getNodeValue(entry.getValue(), Constants.NODEDEVICEID),value);
                                        hmset(TagValues.getNodeValue(entry.getValue(), Constants.NODEACCOUNT),map);
                                    }
                                }
                            }
                        }
                    }

            } else {
                if (!user.getPin().equals(this.requestMessage.getPin()))
                    throw new InvalidPassword();
                if (!user.getStatus().getCode().equals("A")) {
                    throw new InActiveUser();
                }
                throw new InvalidUser();
            }

            this.responseMessage.setResultcode(0);
            this.responseMessage.setResultDescription("Success");
        } catch (EntityException ee) {
            this.responseMessage.setResultcode(ee.getResultCode(Integer.valueOf(0)));
            this.responseMessage.setResultDescription(ee.getDescription());
            this.responseMessage.setTimestamp((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(new Date()));
        }
        catch (Exception ex) {
            this.responseMessage.setResultcode(206);
            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(206));
            ex.printStackTrace();
        } finally {
            if (this.responseMessage.getResultcode() == 0) {
                response = generateJSONResponse(dataMap,prajReports);
            } else {
                response = generateFailureResponse(this.responseMessage);
            }
        }
        return response;
    }

    public String generateJSONResponse(Map<String,String> dataMap, Map<Long, PrajReport> prajReports) {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = responseHeader(requestMessage, this.responseMessage);
            jsonObject.put("transid", responseMessage.getTransid());

            mainObj.put(Constants.NODERESPONSE, jsonObject);
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

    private List<Device> findWardsList(AccountDao accountDao, JSONArray accArray) throws JSONException{
        List<Device> deviceList = new ArrayList<>();
        DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
        //info("GenReportProcessor .... accArray = "+accArray);
        for (int i = 0; i < accArray.length(); i++) {
            Account account = BaseInitializer.getAccountMap().get(accArray.getLong(i));
            if ("L5".equalsIgnoreCase(account.getType())) {
                List<Device> devices = deviceDao.getDeviceByAccount(accArray.getLong(i));
                deviceList.addAll(devices);
            } else {
                List<Account> accounts = accountDao.findAllChildren(accArray.getLong(i));
                for (Account acc : accounts) {
                    if ("L5".equalsIgnoreCase(acc.getType())) {
                        List<Device> devices = deviceDao.getDeviceByAccount(acc.getId());
                        deviceList.addAll(devices);
                    }
                }
            }
        }
        return deviceList;
    }

    private List<Account> findLevel5Account(AccountDao accountDao, JSONArray accArray) throws JSONException{
        List<Account> accList = new ArrayList<>();
        for (int i = 0; i < accArray.length(); i++) {
            Account account = BaseInitializer.getAccountMap().get(accArray.getLong(i));
            if ("L5".equalsIgnoreCase(account.getType())) {
                accList.add(account);
            } else {
                List<Account> accounts = accountDao.findAllChildren(accArray.getLong(i));
                for (Account acc : accounts) {
                    if ("L5".equalsIgnoreCase(acc.getType())) {
                        accList.add(acc);
                    }
                }
            }
        }
        return accList;
    }
}
