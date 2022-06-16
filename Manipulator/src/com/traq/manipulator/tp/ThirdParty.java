package com.traq.manipulator.tp;

import com.google.gson.JsonArray;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.exceptions.CommonException;
import com.traq.manipulator.tp.pushdata.Delhivery;
import com.traq.util.DateTime;
import com.traq.util.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Created by Amit on 17/12/18.
 */
public class ThirdParty extends BaseInitializer{


    private JSONObject jsonRequest;
    private String request;
    private String transid;
    private String url;
    private String event = "";
    private boolean isEventChanged = false;

    public ThirdParty(){

    }

    public ThirdParty(String request, String url, String transid) {
        this.request = request;
        this.url  = url;
        this.transid  = transid;
    }

    public ThirdParty(JSONObject jsonRequest, String url, String transid) {
        this.jsonRequest = jsonRequest;
        this.url  = url;
        this.transid  = transid;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTransid() {
        return transid;
    }

    public void setTransid(String transid) {
        this.transid = transid;
    }

    public void pushDataAccountWise(String data){

    }

    public void pushData(String data, String client){
        //System.out.println("client = "+client +", data="+data);
        //if(client.equals("DLVR")){
            System.out.println("client = "+client  );
            try {
                //pushData(data);
                //if(isEventChanged){
                //if(TagValues.getNodeValue(data, Constants.NODEDEVICETYPEID).equals("56"))
                    //pushData(data);
                    eventData(data,event);

                //}
            }catch (Exception ee){
                System.out.println("Delhivery Error message ... "+ee.getMessage());
                ee.printStackTrace();
            }
        //}else
    }

    public void pushData(JSONArray dataArr, String client){
        try {
            info("Event API client .. "+client);
            info("Event API arr .. "+dataArr);
            JSONArray arr = eventDataArr(dataArr, event);

            JSONObject reqObj = new JSONObject();
            reqObj.put("vendor", "traqmatix");
            reqObj.put("app", "gls");
            reqObj.put("data",arr);
            info("Event API reqObj .. "+reqObj);
            String response = apiResponse(reqObj);
            info("Event API Response .. "+response);
        }catch (Exception ee){
            info("ThirdParty Error message ... " + ee.getMessage());
            ee.printStackTrace();
        }
    }

    private String eventData(String data, String event) throws CommonException {

        JSONObject reqObj = new JSONObject();
        info("AMIT Event ..............");
        Map<Integer,String> healthMap=new HashMap<Integer,String>();
        healthMap.put(100,"11520");
        healthMap.put(90,"10320");
        healthMap.put(80,"9120");
        healthMap.put(70,"7920");
        healthMap.put(60,"6720");
        healthMap.put(50,"5400");
        healthMap.put(40,"4200");
        healthMap.put(30,"3000");
        healthMap.put(20,"1800");
        healthMap.put(15,"900");
        healthMap.put(10,"300");

        try {
            reqObj.put("vendor", "traqmatix");
            reqObj.put("app", "gls");

            JSONArray arr = new JSONArray();
            JSONObject innerObj = new JSONObject();
            try {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                cal.setTime(sdf.parse(TagValues.getNodeValue(data, Constants.NODEORIGINTS)));
                innerObj.put("tis", cal.getTimeInMillis() - 8400 );
            }catch (Exception e) {
                System.out.println("Exception in Time Conversion");
                innerObj.put("tis", System.currentTimeMillis());
            }
            innerObj.put("lat", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELATITUDE)));
            innerObj.put("lon", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELONGITUDE)));
            innerObj.put("server_tis", System.currentTimeMillis());
            String imei = TagValues.getNodeValue(data, Constants.NODEIMEI);
            innerObj.put("vendor_device_id", imei);
            innerObj.put("device_id", TagValues.getNodeValue(data, Constants.NODENAME));
            innerObj.put("speed_kmph", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODESPEED)));
            try {
                innerObj.put("accuracy_meters", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODEACCURACY)));
            }catch (Exception ee){
                innerObj.put("accuracy_meters", 0.0);
            }

            String btCharge = TagValues.getNodeValue(data, Constants.NODEBATCHARGE);
            if("0".equals(btCharge)) {
                innerObj.put("charging", false);
            }else{
                innerObj.put("charging", true);
            }
            //innerObj.put("ignition", false);
            if("A".equals(TagValues.getNodeValue(data, Constants.NODEGPS))) {
                innerObj.put("gps_validity", "valid");
            }else{
                innerObj.put("gps_validity", "invalid");
            }

            String evnt = "locked";
            String lock = TagValues.getNodeValue(data, Constants.NODELOCK);
            String lockSts = TagValues.getNodeValue(data, Constants.NODEUNLOCKSTATUS);

            if("1".equals(lockSts) && "0".equals(lock)){
                evnt = "lock-disengaged";
                innerObj.put("lock_status", "unlocked");
            }else if("0".equals(lockSts) && "0".equals(lock)){
                evnt = "unlocked";
                innerObj.put("lock_status", "unlocked");
            }else{
                innerObj.put("lock_status", "locked");
            }

            try {
                innerObj.put("device_battery_percentage", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODEBATTERY)));
            }catch (Exception e){
                innerObj.put("device_battery_percentage", 0.0);
            }

            Integer battery = Integer.parseInt(TagValues.getNodeValue(data, Constants.NODEBATTERY));
            if (battery>0){
                innerObj.put("device_health_status", healthMap.get(battery));
            }else{
                innerObj.put("device_health_status", "0");
            }
            //String isEvent = TagValues.getNodeValue(data, Constants.NODEEVENT);

            String reason = TagValues.getNodeValue(data, Constants.NODEREASON);
            //info("lock_status="+evnt +", isEvent="+isEvent +", reason="+reason);
            //if("Y".equalsIgnoreCase(isEvent)) {
                if ("SMS".equalsIgnoreCase(reason) || "RFID".equalsIgnoreCase(reason) || "BT".equalsIgnoreCase(reason) || "API".equalsIgnoreCase(reason)) {
                    innerObj.put("event_source", reason.toLowerCase());
                    innerObj.put("event", evnt);
                }else if("Locked".equalsIgnoreCase(reason)){
                    innerObj.put("event", evnt);
                    innerObj.put("event_source", reason.toLowerCase());
                }else if("LSC".equalsIgnoreCase(reason)){
                    innerObj.put("event", evnt);
                    innerObj.put("event_source", reason.toLowerCase());
                }
/*                else if("TSC".equalsIgnoreCase(reason)){
                    innerObj.put("event", evnt);
                    innerObj.put("event_source", reason.toLowerCase());
                }*/
            //}
            arr.put(innerObj);

            reqObj.put("data",arr);
        }catch (JSONException je){

        }

        info("Event API Delhivery.............."+reqObj);
        String response = apiResponse(reqObj);
        //String response = "";
        info("Event API Response .. "+response);
        return response.toString();
    }

    private JSONArray eventDataArr(JSONArray dataArr, String event) throws JSONException, CommonException {

        JSONArray arr = new JSONArray();
        for(int i=0; i<dataArr.length(); i++) {
            JSONObject data = dataArr.getJSONObject(i);
            JSONObject innerObj = new JSONObject();
            try {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                cal.setTime(sdf.parse(TagValues.getNodeValue(data, Constants.NODEORIGINTS)));
                innerObj.put("tis", cal.getTimeInMillis() - 8400);
            } catch (Exception e) {
                System.out.println("Exception in Time Conversion");
                innerObj.put("tis", System.currentTimeMillis());
            }
            innerObj.put("lat", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELATITUDE)));
            innerObj.put("lon", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELONGITUDE)));
            innerObj.put("server_tis", System.currentTimeMillis());
            String imei = TagValues.getNodeValue(data, Constants.NODEIMEI);
            innerObj.put("vendor_device_id", imei);
            innerObj.put("device_id", TagValues.getNodeValue(data, Constants.NODENAME));
            innerObj.put("speed_kmph", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODESPEED)));
            innerObj.put("accuracy_meters", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODEACCURACY)));

            String btCharge = TagValues.getNodeValue(data, Constants.NODEBATCHARGE);
            if ("0".equals(btCharge)) {
                innerObj.put("charging", false);
            } else {
                innerObj.put("charging", true);
            }
            //innerObj.put("ignition", false);
            if ("A".equals(TagValues.getNodeValue(data, Constants.NODEGPS))) {
                innerObj.put("gps_validity", "valid");
            } else {
                innerObj.put("gps_validity", "invalid");
            }

            String evnt = "locked";
            String lock = TagValues.getNodeValue(data, Constants.NODELOCK);
            String lockSts = TagValues.getNodeValue(data, Constants.NODEUNLOCKSTATUS);

            if ("1".equals(lockSts) && "0".equals(lock)) {
                evnt = "lock-disengaged";
                innerObj.put("lock_status", "unlocked");
            } else if ("0".equals(lockSts) && "0".equals(lock)) {
                evnt = "unlocked";
                innerObj.put("lock_status", "unlocked");
            } else {
                innerObj.put("lock_status", "locked");
            }


            try {
                innerObj.put("device_battery_percentage", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODEBATTERY)));
            }catch (Exception e){
                innerObj.put("device_battery_percentage", 0.0);
            }

            //String isEvent = TagValues.getNodeValue(data, Constants.NODEEVENT);

            String reason = TagValues.getNodeValue(data, Constants.NODEREASON);
            //info("lock_status="+evnt +", isEvent="+isEvent +", reason="+reason);
            //if("Y".equalsIgnoreCase(isEvent)) {
            if ("SMS".equalsIgnoreCase(reason) || "RFID".equalsIgnoreCase(reason) || "BT".equalsIgnoreCase(reason) || "API".equalsIgnoreCase(reason)) {
                innerObj.put("event_source", reason.toLowerCase());
                innerObj.put("event", evnt);
            } else if ("Locked".equalsIgnoreCase(reason)) {
                innerObj.put("event", evnt);
                innerObj.put("event_source", reason.toLowerCase());
            } else if ("LSC".equalsIgnoreCase(reason)) {
                innerObj.put("event", evnt);
                innerObj.put("event_source", reason.toLowerCase());
            }
/*                else if("TSC".equalsIgnoreCase(reason)){
                innerObj.put("event", evnt);
                innerObj.put("event_source", reason.toLowerCase());
            }*/
            //}
            arr.put(innerObj);
        }
        return arr;
    }


    private String apiResponse(JSONObject reqObj){
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            //String url = "https://gls-stagingd.delhivery.com/data";
            String url = getAppConfig().getApiURL().trim();
            URL endPoint = new URL(url);
            OutputStreamWriter writer = null;
            OutputStream output = null;

            HttpURLConnection connection = (HttpURLConnection) endPoint.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer "+getAppConfig().getAuthToken());
            connection.setRequestProperty("Postman-Token", getAppConfig().getPostmanToken());
            connection.setRequestProperty("cache-control", "no-cache");

            writer = new OutputStreamWriter(connection.getOutputStream());
            output = connection.getOutputStream();

            String request = reqObj.toString();

            info(request);
            output.write(request.getBytes("UTF-8"));
            writer.flush();

            String line;
            InputStreamReader is = null;

            if (connection.getResponseCode() >= 400) {
                info("Event API Error Code .. "+connection.getResponseCode());
                is = new InputStreamReader(connection.getErrorStream());
            } else {
                is = new InputStreamReader(connection.getInputStream());
            }
            reader = new BufferedReader(is);
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return response.toString();
    }

    // Specially developed for Intuguine

}
