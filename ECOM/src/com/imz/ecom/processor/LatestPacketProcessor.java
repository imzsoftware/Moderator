package com.imz.ecom.processor;


import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.*;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.SummaryHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.common.xmlutils.XMLProcessor;
import com.traq.common.xmlutils.XMLtoJsonConverter;
import com.traq.util.RequestBean;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


public class LatestPacketProcessor
        extends BaseInitializer
        implements RequestProcessorInterface
{
    private ResponseMessage responseMessage;
    private RequestMessage requestMessage;
    private String request;
    RequestBean rb;
    SimpleDateFormat outputPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public LatestPacketProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public LatestPacketProcessor() {}

    public String executeXML(RequestBean _rb) {
        return this.request;
    }

    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();

        SummaryHandler rh = new SummaryHandler();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        String response = "";
        try {
            JSONObject object = new JSONObject(this.request);

            JSONObject message = object.getJSONObject("request");
            this.requestMessage = new RequestMessage();
            requestMessage.setIMEI(TagValues.getNodeValue(message, Constants.NODEIMEI));
            requestMessage.setIpaddress(TagValues.getNodeValue(message, Constants.NODEIPADDRESS));
            requestMessage.setVendorcode(TagValues.getNodeValue(object, Constants.NODEVENDORCODE));

            this.responseMessage = new ResponseMessage();
            this.responseMessage.setMessagetype(this.requestMessage.getMessagetype());
            this.responseMessage.setResponsects(sdf.format(new Date()));

/*            UserDao customerDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");
            User user = customerDao.login(this.requestMessage.getUsername());*/
            boolean isValid = true;  //validateUser(user, this.requestMessage.getPin());

            if (isValid) {
                this.responseMessage.setResultcode(Integer.valueOf(0));
                this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(0)));
            } else {
                this.responseMessage.setResultcode(Integer.valueOf(11));
                this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(11)));
            }
        }
        catch (Exception ex) {
            this.responseMessage.setResultcode(Integer.valueOf(206));
            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            ex.printStackTrace();
        } finally {
            if (this.responseMessage.getResultcode().intValue() == 0) {
                response = generateJSONResponse();
            } else {
                response = generateFailureResponse(this.responseMessage);
            }
        }

        return response;
    }

    public JSONObject responseHeader() {
        JSONObject header = new JSONObject();
        try {
            header.put("responsetype", this.responseMessage.getMessagetype());
            header.put("resultcode", this.responseMessage.getResultcode());
            header.put("resultdescription", this.responseMessage.getResultDescription());
            header.put("responsects", this.responseMessage.getResponsects());
        } catch (Exception exception) {}


        return header;
    }


    public String generateJSONResponse() {
        JSONObject mainObj = new JSONObject();
        try {
            DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
            Device device = deviceDao.getDeviceByIMEI(requestMessage.getIMEI());
            JSONObject jsonObject = responseHeader(this.requestMessage, this.responseMessage);
            mainObj = responseHeader();

            JSONObject obj = liveTrackJSON(this.requestMessage, device);
            if (obj != null) {
                jsonObject.put("data", obj);
            } else {
                jsonObject.put("data", "");
            }

            mainObj.put("response", jsonObject);
        }
        catch (JSONException jSONException) {

        } catch (Exception exception) {}



        return mainObj.toString();
    }

    public String generateFailureResponse(ResponseMessage rm) {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("responsetype", rm.getMessagetype());
            jsonObject.put("resultcode", rm.getResultcode());
            jsonObject.put("resultdescription", rm.getResultDescription());

            mainObj.put("response", jsonObject);
        } catch (JSONException jSONException) {}


        return mainObj.toString();
    }

    private LiveTrack getLiveTrack(RequestMessage rm) {
        LiveTrack liveTrack = new LiveTrack();
        try {
            String data = hget(rm.getAccId().toString(), rm.getAssetId().toString());

            liveTrack.setDoor(TagValues.getNodeValue(data, "door"));
            liveTrack.setTemp(TagValues.getNodeValue(data, "temp"));
            liveTrack.setAc(TagValues.getNodeValue(data, "ac"));
            liveTrack.setFuel(TagValues.getNodeValue(data, "fuel"));
            liveTrack.setLatitude(Double.valueOf(Double.parseDouble(TagValues.getNodeValue(data, "lat"))));
            liveTrack.setLongitude(Double.valueOf(Double.parseDouble(TagValues.getNodeValue(data, "lng"))));
            liveTrack.setLoads(TagValues.getNodeValue(data, "load"));
            liveTrack.setIgnition(TagValues.getNodeValue(data, "ign"));
            liveTrack.setLastStop(TagValues.getNodeValue(data, "laststop"));
            liveTrack.setDevPort(TagValues.getNodeValue(data, "devport"));
            if (liveTrack.getIgnition().equals("N")) {
                liveTrack.setSpeed(Integer.valueOf(Integer.parseInt(TagValues.getNodeValue(data, "speed"))));
            } else {
                liveTrack.setSpeed(Integer.valueOf(0));
            }
            liveTrack.setGeoId(TagValues.getNodeValue(data, "geoid"));
            liveTrack.setStatus((Status)BaseInitializer.getStatusMap().get(TagValues.getNodeValue(data, "status")));
            if (checkNullAndEmpty(TagValues.getNodeValue(data, "orgts"))) {
                Date date = (new SimpleDateFormat(getDateFormat())).parse(TagValues.getNodeValue(data, "orgts"));
                liveTrack.setOrgTs(this.outputPattern.format(date));
            } else {
                liveTrack.setOrgTs(TagValues.getNodeValue(data, "orgts"));
            }

            liveTrack.setDevIp(TagValues.getNodeValue(data, "devip"));
            liveTrack.setPowStatus(TagValues.getNodeValue(data, "powsts"));
            if (checkNullAndEmpty(TagValues.getNodeValue(data, "idlets"))) {
                Date date = (new SimpleDateFormat(getDateFormat())).parse(TagValues.getNodeValue(data, "idlets"));
                liveTrack.setIdleTs(this.outputPattern.format(date));
            } else {
                liveTrack.setIdleTs(TagValues.getNodeValue(data, "idlets"));
            }
            liveTrack.setIdleFlag(TagValues.getNodeValue(data, "idleflag"));
            liveTrack.setServIp(TagValues.getNodeValue(data, "servip"));
            liveTrack.setServPort(TagValues.getNodeValue(data, "servport"));
            liveTrack.setCourse(Double.valueOf(Double.parseDouble(TagValues.getNodeValue(data, "disha"))));
            liveTrack.setLocation(TagValues.getNodeValue(data, "address"));

            liveTrack.setImb(TagValues.getNodeValue(data, "imb"));
            liveTrack.setDistance(TagValues.getNodeValue(data, "distance"));

        }
        catch (Exception exception) {
            exception.printStackTrace();
        } finally {}



        return liveTrack;
    }

    private JSONObject liveTrackJSON(RequestMessage rm, Device device) {
        JSONObject refineData = new JSONObject();
        try {
            String data = hget(device.getAccId().toString(), device.getId().toString());
            data = XMLProcessor.removeJunkCharacters(data);
            if (checkNullAndEmpty(data)) {
                //refineData =  XMLtoJsonConverter.xmlToJson(data).getJSONObject("live");
                refineData.put(Constants.NODEBATTERY,TagValues.getNodeValue(data,Constants.NODEBATTERY));
                refineData.put(Constants.NODESPEED,TagValues.getNodeValue(data,Constants.NODESPEED));
                refineData.put(Constants.NODEDEVICEID,TagValues.getNodeValue(data,Constants.NODEDEVICEID));
                refineData.put(Constants.NODEACCOUNTID,TagValues.getNodeValue(data,Constants.NODEACCOUNTID));
                refineData.put(Constants.NODELOCK,TagValues.getNodeValue(data,Constants.NODELOCK));
                refineData.put(Constants.NODECLIENT,TagValues.getNodeValue(data,Constants.NODECLIENT));
                refineData.put(Constants.NODELATITUDE,Double.parseDouble(TagValues.getNodeValue(data,Constants.NODELATITUDE)));
                refineData.put(Constants.NODELONGITUDE,Double.parseDouble(TagValues.getNodeValue(data,Constants.NODELONGITUDE)));
                refineData.put(Constants.NODECREATEDON,TagValues.getNodeValue(data,Constants.NODECREATEDON));
                refineData.put(Constants.NODEORIGINTSMILLI,Long.parseLong(TagValues.getNodeValue(data,Constants.NODEORIGINTSMILLI)));
                refineData.put(Constants.NODEORIGINTS,Long.parseLong(TagValues.getNodeValue(data,Constants.NODEORIGINTS)));
                refineData.put(Constants.NODEBATCHARGE,Long.parseLong(TagValues.getNodeValue(data,Constants.NODEBATCHARGE)));
                refineData.put(Constants.NODETAMPER,Long.parseLong(TagValues.getNodeValue(data,Constants.NODETAMPER)));
                refineData.put(Constants.NODEIMEI,device.getIMEI());
                refineData.put(Constants.NODEGPS,TagValues.getNodeValue(data,Constants.NODEGPS));

                if (device != null) {
                    if (this.requestMessage.getClienttype().equalsIgnoreCase("web")) {
                        refineData.accumulate("vehnum", device.getLicenseno());
                    } else {
                        refineData.put("vehnum", device.getLicenseno());
                    }
                } else {
                    refineData.accumulate("vehnum", "0000");
                }

                Date ts = (new SimpleDateFormat(getDateFormat())).parse(TagValues.getNodeValue(refineData, "orgts"));
                if (!"FIXED".equalsIgnoreCase(this.requestMessage.getClient())) {
                    long curTime = (new Date()).getTime();
                    if (ts.getTime() + 3600000L >= curTime) {
                        if (refineData.getString("ign").equals("Y") && refineData.getInt("speed") >= 5) {
                            refineData.accumulate("status", "MOTION");
                        } else {
                            refineData.accumulate("status", "STOP");
                        }
                    } else {
                        refineData.accumulate("status", "OFF");
                    }

                } else if (refineData.getString("ign").equals("Y")) {
                    refineData.accumulate("status", "ON");
                } else {
                    refineData.accumulate("status", "OFF");
                }
                refineData.put("orgts", (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).format(ts));
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {}

        return refineData;
    }
}