package com.imz.ecom.processor;

import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Device;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.exceptions.EntityException;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class TripCompleteProcessor extends BaseInitializer implements RequestProcessorInterface {
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


    public TripCompleteProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public TripCompleteProcessor() {
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
            requestMessage.setIMEI(TagValues.getNodeValue(reqObj, Constants.NODEIMEI));

            responseMessage.setMessagetype(requestMessage.getMessagetype());
            responseMessage.setResponsects(sdf.format(new Date()));
            responseMessage.setTransid(rb.getTransid());
            DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
            Device device = deviceDao.getDeviceByIMEI(requestMessage.getIMEI());
            if(device != null) {
                if (requestMessage.getVendorcode().equalsIgnoreCase(device.getAccount().getClient())) {
                    StringBuilder GEOIN = new StringBuilder().append("GEO_IN_").append(device.getAccId());
                    hdel(GEOIN.toString(),device.getId().toString());
                    responseMessage.setResultcode(Integer.valueOf(0));
                    responseMessage.setResultDescription("Success");
                }
            }else{
                throw new NoRecordFoundException();
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
