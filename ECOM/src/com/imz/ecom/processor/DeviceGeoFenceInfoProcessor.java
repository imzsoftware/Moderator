package com.imz.ecom.processor;

import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.GeoFence;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.GeoFenceDao;
import com.traq.common.exceptions.EntityException;
import com.traq.common.exceptions.InvalidVendorCode;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class DeviceGeoFenceInfoProcessor extends BaseInitializer implements RequestProcessorInterface {
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


    public DeviceGeoFenceInfoProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public DeviceGeoFenceInfoProcessor() {
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
                    if(requestMessage.getVendorcode().equalsIgnoreCase(device.getAccount().getClient())) {
                        isValid = true;
                        String data = hget(device.getAccId() + "", device.getId() + "");
                        Double lat = null;
                        Double lng = null;
                        if(reqType.equalsIgnoreCase("MOBILEGEO")) {
                            lat = Double.parseDouble(TagValues.getNodeValue(reqObj, Constants.NODELATITUDE));
                            lng = Double.parseDouble(TagValues.getNodeValue(reqObj, Constants.NODELONGITUDE));
                        }else{
                            lat = Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELATITUDE));
                            lng = Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELONGITUDE));
                        }
                        liveData.put(Constants.NODELATITUDE, TagValues.getNodeValue(reqObj, Constants.NODELATITUDE));
                        liveData.put(Constants.NODELONGITUDE, TagValues.getNodeValue(reqObj, Constants.NODELONGITUDE));
                        //liveData.put(Constants.NODELOCK, TagValues.getNodeValue(data, Constants.NODELOCK));
                        liveData.put(Constants.NODEIMEI, TagValues.getNodeValue(data, Constants.NODEIMEI));
                        GeoFenceDao geoFenceDao = (GeoFenceDao) ApplicationBeanContext.getInstance().getBean("geoFenceDao");
                        String geoName = null;
                        List<GeoFence> geoFenceList = geoFenceDao.getGeofenceByAccount(Long.parseLong(TagValues.getNodeValue(data, Constants.NODEACCOUNTID)));
                        int count = 0;
                        for (GeoFence geoFence : geoFenceList) {
                            String type = geoFence.getType();
                            if (checkNullAndEmpty(type)) {
                                if (type.equals("POLYGON")) {
                                    count = geoFenceDao.validateGeoFence(geoFence.getId(), lat, lng);
                                } else if (type.equals("CIRCLE")) {
                                    count = geoFenceDao.validateInCircle(lat, lng, 200, geoFence.getId());
                                } else if (type.equals("POI")) {
                                    count = geoFenceDao.validateInCircle(lat, lng, 500, geoFence.getId());
                                } else {
                                    count = geoFenceDao.validateGeoFence(geoFence.getId(), lat, lng);
                                }

                                if (count > 0) {
                                    geoName = geoFence.getDescription();
                                    if (geoName.equals("NA") && geoName.length() > 5) {
                                        geoName = geoFence.getName().substring(0, 5).toUpperCase();
                                    } else if (geoName.equals("NA")) {
                                        geoName = geoFence.getName();
                                    }
                                    break;
                                }
                            }
                        }
                        if (count > 0){
                            liveData.put(Constants.NODESTATUS, "in");
                            liveData.put(Constants.NODECODE, geoName);
                        }else {
                            liveData.put(Constants.NODESTATUS, "out");
                        }
                        break;
                    }
                }
            }
            if(isValid) {
                responseMessage.setResultcode(Integer.valueOf(0));
                responseMessage.setResultDescription("Success");
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
