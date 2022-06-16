package com.imz.ecom.processor;

import com.imz.ecom.processor.utility.InventoryCheck;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.GeoFence;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.GeoFenceDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.exceptions.EntityException;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.common.data.entity.User;
import com.traq.util.RequestBean;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;


public class InventoryDashProcessor extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage = new ResponseMessage();
    private RequestMessage requestMessage = new RequestMessage();
    private String request;
    SimpleDateFormat outputPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    RequestBean rb;
    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }


    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public InventoryDashProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public InventoryDashProcessor() {
    }

    public String executeXML(RequestBean _rb) {
        return this.request;
    }

    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        String response = "";
        JSONObject liveData = new JSONObject();
        try {
            JSONObject object = new JSONObject(this.request);
            JSONObject reqObj = object.getJSONObject("request");
            String reqType = TagValues.getNodeValue(object, "requesttype");

            requestMessage.setMessagetype(reqType);
            requestMessage.setVendorcode(TagValues.getNodeValue(object, Constants.NODEVENDORCODE));
            requestMessage.setIpaddress(TagValues.getNodeValue(object, Constants.NODEIPADDRESS));
            requestMessage.setPin(TagValues.getNodeValue(reqObj, Constants.NODEPIN));
            requestMessage.setUsername(TagValues.getNodeValue(reqObj, Constants.NODEUSER));
            String accId = TagValues.getNodeValue(reqObj, Constants.NODEACCOUNTID);
            if(checkNullAndEmpty(accId)) {
                requestMessage.setAccId(Long.parseLong(accId));
            }else{
                requestMessage.setAccId(0L);
            }

            responseMessage.setMessagetype(requestMessage.getMessagetype());
            responseMessage.setResponsects(sdf.format(new Date()));
            responseMessage.setTransid(rb.getTransid());

            UserDao customerDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");
            User user = customerDao.login(requestMessage.getUsername());
            boolean isValid = validateUser(user, requestMessage.getPin());

            if (isValid) {
                AccountDao accountDao = (AccountDao) ApplicationBeanContext.getInstance().getBean("accountDao");
                DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");

                List<Account> accountList = new ArrayList<Account>();
                if(requestMessage.getAccId() == 0){
                    accountList = accountDao.AllChildren(user.getAccId());
                }else{
                    accountList = accountDao.AllChildren(requestMessage.getAccId());
                }
                List<Device> deviceList = deviceDao.getDevicesByAccount(accountList);
                Map <Long, Integer> totalStock = new HashMap<Long, Integer>();
                Map <Long, Integer> devAtLoc = new HashMap<Long, Integer>();
                Map <Long, Integer> misRoute = new HashMap<Long, Integer>();
                Map <Long, Integer> inBoundToHub = new HashMap<Long, Integer>();
                Map <Long, Integer> outBoundFromHub = new HashMap<Long, Integer>();
                Map <Long, Integer> offLine = new HashMap<Long, Integer>();
                Map <Long, Integer> maintainance = new HashMap<Long, Integer>();

                if(deviceList != null && deviceList.size() > 0) {
                    // Convert GMT to IST + 30 min for Offline calculation
                    Long curTime = System.currentTimeMillis() + (6*60*60*1000);
                    for(Device device : deviceList) {
                        if(totalStock.containsKey(device.getAccId())) {
                            totalStock.put(device.getAccId(), totalStock.get(device.getAccId())+1);
                        }else{
                            totalStock.put(device.getAccId(), 1);
                        }
                        StringBuilder GEOIN = new StringBuilder().append("GEO_IN_").append(device.getAccId());
                        String geoIn = hget(GEOIN.toString(), device.getId().toString());
                        //String data = hget(device.getAccId() + "", device.getId() + "");

                        if (checkNullAndEmpty(geoIn)){
                            if(devAtLoc.containsKey(device.getAccId())){
                                devAtLoc.put(device.getAccId(),devAtLoc.get(device.getAccId())+1);
                            }else{
                                devAtLoc.put(device.getAccId(),1);
                            }
                        }else {
                            if(outBoundFromHub.containsKey(device.getAccId())){
                                outBoundFromHub.put(device.getAccId(),outBoundFromHub.get(device.getAccId())+1);
                            }else{
                                outBoundFromHub.put(device.getAccId(),1);
                            }
                        }

                        responseMessage.setResultcode(Integer.valueOf(0));
                        responseMessage.setResultDescription("Success");
                    }
                }else{
                    throw new NoRecordFoundException();
                }

                responseMessage.setResultcode(Integer.valueOf(0));
                responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(0)));
            } else {
                responseMessage.setResultcode(Integer.valueOf(11));
                responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(11)));
            }
        } catch (EntityException ee) {
            this.responseMessage.setResultcode(ee.getResultCode(Integer.valueOf(0)));
            this.responseMessage.setResultDescription(ee.getDescription());
            this.responseMessage.setTimestamp((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(new Date()));
        } catch (Exception ex) {
            this.responseMessage.setResultcode(Integer.valueOf(206));
            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            ex.printStackTrace();
        } finally {
            if (responseMessage.getResultcode() == 0) {
                response = generateJSONResponse(liveData);
            } else {
                response = generateFailureResponse(responseMessage);
            }
        } 
        return response;
    }


    public String generateJSONResponse(JSONObject liveData) {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = responseHeader(this.requestMessage, this.responseMessage);
            jsonObject.put(Constants.NODETRANSID, this.responseMessage.getTransid());
            mainObj.put(Constants.NODERESPONSE, jsonObject);
        }catch (JSONException je) {
            je.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mainObj.toString();

    }

    public String generateFailureResponse(ResponseMessage rm) {
/* 437 */
        JSONObject mainObj = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("responsetype", rm.getMessagetype());
            jsonObject.put("resultcode", rm.getResultcode());
            jsonObject.put("resultdescription", rm.getResultDescription());
            jsonObject.put("transid", rm.getTransid());
            mainObj.put("response", jsonObject);
        } catch (JSONException jSONException) {
        }
         return mainObj.toString();

    }

}
