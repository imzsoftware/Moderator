package com.imz.ecom.processor;

import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.User;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.exceptions.EntityException;
import com.traq.common.exceptions.InvalidPassword;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


public class AlertFromAppProcessor extends BaseInitializer implements RequestProcessorInterface {
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


    public AlertFromAppProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public AlertFromAppProcessor() {
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
            //APPEVENT
            String reqType = TagValues.getNodeValue(object, "requesttype");

            UserDao userDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");

            //{"request":{"geofence":"","lat":28.4956817,"lng":77.0793551,"address":""}}


            requestMessage.setMessagetype(reqType);
            requestMessage.setVendorcode(TagValues.getNodeValue(object, Constants.NODEVENDORCODE));
            requestMessage.setIpaddress(TagValues.getNodeValue(object, Constants.NODEIPADDRESS));
            requestMessage.setUsername(TagValues.getNodeValue(reqObj, Constants.NODEUSER));
            requestMessage.setPin(TagValues.getNodeValue(reqObj, Constants.NODEPIN));
            User user = userDao.login(this.requestMessage.getUsername());
            boolean isValid = validateUser(user, this.requestMessage.getPin());
            boolean isSuccess = false;
            if (isValid) {
                requestMessage.setIMEI(TagValues.getNodeValue(reqObj, Constants.NODEIMEI));
                requestMessage.setClienttype(TagValues.getNodeValue(reqObj, Constants.NODECLIENTTYPE));
                requestMessage.setCreatedOn(TagValues.getNodeValue(reqObj, Constants.NODEORIGINTS));
                requestMessage.setType(TagValues.getNodeValue(reqObj, Constants.NODETYPE));
                requestMessage.setUdv1(TagValues.getNodeValue(reqObj, Constants.NODEEVENTSOURCETYPE));
                requestMessage.setUdv2(TagValues.getNodeValue(reqObj, Constants.NODETRIPID));
                requestMessage.setAddress(TagValues.getNodeValue(reqObj, Constants.NODEGEOFENCE));
                if(requestMessage.getAddress().isEmpty()){
                    requestMessage.setAddress(TagValues.getNodeValue(reqObj, Constants.NODEADDRESS));
                }
                requestMessage.setAccId(Long.parseLong(TagValues.getNodeValue(reqObj, Constants.NODEACCOUNTID)));
                requestMessage.setAssetId(Long.parseLong(TagValues.getNodeValue(reqObj, Constants.NODEDEVICEID)));
                requestMessage.setLatitude(Double.parseDouble(TagValues.getNodeValue(reqObj, Constants.NODELATITUDE)));
                requestMessage.setLongitude(Double.parseDouble(TagValues.getNodeValue(reqObj, Constants.NODELONGITUDE)));

            }else{
                throw new InvalidPassword();
            }
            responseMessage.setMessagetype(requestMessage.getMessagetype());
            responseMessage.setResponsects(sdf.format(new Date()));
            responseMessage.setTransid(rb.getTransid());
            DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
            Device device = deviceDao.getDeviceByIMEI(requestMessage.getIMEI());
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
