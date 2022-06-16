package com.imz.ecom.processor;

import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.Device;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.DeviceDao;
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


public class ECOMDeviceInfoProcessor extends BaseInitializer implements RequestProcessorInterface {
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


    public ECOMDeviceInfoProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public ECOMDeviceInfoProcessor() {
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
            requestMessage.setName(TagValues.getNodeValue(reqObj, Constants.NODENAME));

            responseMessage.setMessagetype(requestMessage.getMessagetype());
            responseMessage.setResponsects(sdf.format(new Date()));
            responseMessage.setTransid(rb.getTransid());
            DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
            List<Device> deviceList = deviceDao.getDeviceByName(requestMessage.getName());
            boolean isValid = false;

            if(deviceList == null || deviceList.size() == 0){
                throw  new NoRecordFoundException();
            }else{
                for(Device device : deviceList) {
                    if(requestMessage.getVendorcode().equalsIgnoreCase(device.getAccount().getClient())){
                        isValid = true;
                        String data = hget(device.getAccId()+"",device.getId()+"");
                        liveData.put(Constants.NODEBATTERY,TagValues.getNodeValue(data, Constants.NODEBATTERY));
                        liveData.put(Constants.NODELATITUDE,TagValues.getNodeValue(data, Constants.NODELATITUDE));
                        liveData.put(Constants.NODELONGITUDE,TagValues.getNodeValue(data, Constants.NODELONGITUDE));
                        liveData.put(Constants.NODEACCOUNTID,TagValues.getNodeValue(data, Constants.NODEACCOUNTID));
                        liveData.put(Constants.NODEDEVICEID,TagValues.getNodeValue(data, Constants.NODEDEVICEID));
                        liveData.put(Constants.NODELOCK,TagValues.getNodeValue(data, Constants.NODELOCK));
                        liveData.put(Constants.NODEGPS,TagValues.getNodeValue(data, Constants.NODEGPS));
                        liveData.put(Constants.NODESATELLITES,TagValues.getNodeValue(data, Constants.NODESATELLITES));
                        liveData.put(Constants.NODEORIGINTSMILLI,TagValues.getNodeValue(data, Constants.NODEORIGINTSMILLI));
                        liveData.put(Constants.NODEORIGINTS,TagValues.getNodeValue(data, Constants.NODEORIGINTS));
                        liveData.put(Constants.NODEADDRESS,TagValues.getNodeValue(data, Constants.NODEADDRESS));
                        break;
                    }
                }
            }
            if(isValid) {
                Long curTime = System.currentTimeMillis() + (19800*1000);   // Convert to IST
                Long orgTs = Long.parseLong(liveData.getString(Constants.NODEORIGINTSMILLI));
                info("ECOMDeviceInfooProcessor ..... NODEORIGINTSMILLI "+liveData.get(Constants.NODEORIGINTSMILLI)+", CurrentTime "+System.currentTimeMillis());
                info("ECOMDeviceInfooProcessor ..... curTime - orgTs "+(curTime - orgTs));


                //if device data more than 15 min old
                if((curTime - orgTs) > (15*60*1000)){
                    throw new StalePacketException(liveData.getString(Constants.NODEORIGINTS), 0);
                }
                //if battery less than 50%
                if(Integer.parseInt(liveData.getString(Constants.NODEBATTERY)) < 50){
                    throw new BatteryException("50%",0);
                }
                responseMessage.setResultcode(0);
                responseMessage.setResultDescription("Success");
            }else{
                throw new NoRecordFoundException();
            }
        } catch (EntityException ee) {
            this.responseMessage.setResultcode(ee.getResultCode(0));
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
            jsonObject.put(Constants.NODEDATA,liveData);
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
