package com.imz.praj.processor;

import com.imz.praj.data.impl.AlertDataDaoImpl;
import com.imz.praj.data.impl.RedisToMongoDaoImpl;
import com.imz.praj.data.obj.PrajReportData;
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
import com.traq.common.exceptions.EntityException;
import com.traq.common.exceptions.MandatoryValuesNull;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.ReportHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GetDeviceDataProcessor extends BaseInitializer  implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    private RequestMessage requestMessage;
    private String request;
    RequestBean rb;
    User user = null;

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public GetDeviceDataProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public GetDeviceDataProcessor() {}

    public String executeXML(RequestBean _rb) {
        return this.request;
    }


    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();

        String response = "";

        DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
        PrajReport report = null;
        try {
            responseMessage = new ResponseMessage();
            JSONObject object = new JSONObject(this.request);
            requestMessage = new RequestMessage();
            requestMessage.setName(object.getString(Constants.NODEDEVICEID));
            requestMessage.setStartDate(object.getString(Constants.NODEDATE));

            requestMessage.setVendorcode(TagValues.getNodeValue(object, "vendorcode"));
            if (!checkNullAndEmpty(requestMessage.getStartDate())) {
                throw new MandatoryValuesNull();
            }

            if (!checkNullAndEmpty(requestMessage.getName())) {
                throw new MandatoryValuesNull();
            }

            boolean isValid = true;
            if (isValid) {
                //responseMessage.setMessagetype(requestMessage.getMessagetype());
                //responseMessage.setResponsects(newPattern.format(new Date()));
                List<Device> deviceList = deviceDao.getDeviceByName(requestMessage.getName());
                report = getDeviceData(deviceList.get(0).getId(), deviceList.get(0).getId(), requestMessage.getStartDate());

                responseMessage.setResultcode(Integer.valueOf(0));
                responseMessage.setResultDescription("Success");

            } else {
                responseMessage.setResultcode(Integer.valueOf(11));
                responseMessage.setResultDescription("User Not Found");
            }

        } catch (EntityException ee) {
            responseMessage.setResultcode(Integer.valueOf(24));
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(24)));
        }
        catch (Exception ex) {
            error("PRAJReportProcess ......... ex "+ex.getMessage());
            responseMessage.setResultcode(Integer.valueOf(206));
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            ex.printStackTrace();
        } finally {
            if (responseMessage.getResultcode().intValue() == 0) {
                response = generateJSONResponse(report);
            } else {
                response = generateFailureResponse(responseMessage);
            }
        }
        return response;
    }

    private PrajReport getDeviceData(Long deviceId, Long accId, String date){
        RedisToMongoDaoImpl redisToMongoDao = new RedisToMongoDaoImpl();
        return redisToMongoDao.deviceReport(deviceId,date, date);
    }

    public String generateJSONResponse(PrajReport report) {
        JSONObject jsonObject = new JSONObject();
        try {
            //JSONObject jsonObject = responseHeader(this.requestMessage, this.responseMessage);
            if (report != null) {
                jsonObject.put("data", createObject(report));
                jsonObject.put("Result",1);
                jsonObject.put("Message", "OK.");
            }else{
                jsonObject.put("data", new JSONArray());
                jsonObject.put("Result",0);
                jsonObject.put("Message", "OK.");
            }
        }
        catch (JSONException je) {
            je.printStackTrace();
        } catch (Exception exception) {}
        return jsonObject.toString();
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

    private JSONArray createObject(PrajReport report) throws JSONException{
        JSONArray array = new JSONArray();
        List<ViewDetailRep> detailReps = report.getDetailReports();
        for(ViewDetailRep rep : detailReps){
            JSONObject obj = new JSONObject();
            obj.put("Vend_ID","BH07");
            obj.put("NIC_DeviceId",requestMessage.getName());
            obj.put("Date_Of_Operation",requestMessage.getStartDate());
            obj.put("Motor_RunningTotalTime",(rep.getRunTime()*60)+"");
            obj.put("Water_Volume",rep.getQuantity().toString());
            obj.put("Start_Time",rep.getOnTime());
            obj.put("Start_TimeId","1");
            obj.put("End_Time",rep.getOffTime());
            obj.put("End_TimeId","1");
            array.put(obj);
        }
        return array;
    }

}
