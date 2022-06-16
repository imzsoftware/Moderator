package com.traq.manipulator.tp.pushdata;

import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.exceptions.CommonException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by Amit on 17/12/18.
 */
public class Delhivery extends BaseInitializer implements Callable<String> {


    private JSONObject jsonRequest;
    private String request;
    private String transid;
    private String url;

    public Delhivery(){

    }

    public Delhivery(String request, String url, String transid) {
        this.request = request;
        this.url  = url;
        this.transid  = transid;
    }

    public Delhivery(JSONObject jsonRequest, String url, String transid) {
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

    public synchronized String call() throws CommonException {

        String searchFlights = null;
        Thread.currentThread().setName(transid);
        info("AMIT Delhivery.............."+request);
        searchFlights = pushData(request);

        return searchFlights;

    }

    private synchronized String pushData(String data) throws CommonException {
        StringBuilder response = new StringBuilder();
        JSONObject reqObj = new JSONObject();
        info("AMIT Delhivery.............."+data);
        try {
            reqObj.put("vendor_name", "traqmatrix");
            reqObj.put("count", 1);
            JSONArray arr = new JSONArray();
            JSONObject innerObj = new JSONObject();
            innerObj.put("epoch", Long.parseLong(transid));
            innerObj.put("lat", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELATITUDE)));
            innerObj.put("lon", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELONGITUDE)));
            innerObj.put("spd", Double.parseDouble(TagValues.getNodeValue(data, Constants.NODESPEED)));
            innerObj.put("server_timestamp", System.currentTimeMillis());
            innerObj.put("vendor_device_id", TagValues.getNodeValue(data, Constants.NODEIMEI));
            innerObj.put("ignition", false);
            innerObj.put("orientation", TagValues.getNodeValue(data,Constants.NODEDISHA));
            innerObj.put("address", TagValues.getNodeValue(data,Constants.NODEADDRESS));

            arr.put(innerObj);

            reqObj.put("coordinates",arr);
        }catch (JSONException je){

        }

        BufferedReader reader = null;
        info("Delhivery.............."+reqObj);

        try {
            String url = "https://mts-staging.delhivery.com/v2/location-data";
            URL endPoint = new URL(url);
            OutputStreamWriter writer = null;
            OutputStream output = null;

            HttpURLConnection connection = (HttpURLConnection) endPoint.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6Im1pZG1pbGUtdHJhcW1hdHJpeCIsInRva2VuX25hbWUiOiJtaWRtaWxlLXRyYXFtYXRyaXgiLCJjZW50ZXIiOlsiSU5EMTIyMDAzQUFCIl0sInVzZXJfdHlwZSI6Ik5GIiwiYXBwX2lkIjo3OCwiYXVkIjoiLmRlbGhpdmVyeS5jb20iLCJmaXJzdF9uYW1lIjoibWlkbWlsZS10cmFxbWF0cml4Iiwic3ViIjoidW1zOjp1c2VyOjpmMWI5YmM3YS05Y2E5LTExZTktYWQ1ZS0wMmZiY2RiZDVjNzYiLCJleHAiOjE3MTk3Mzg5MzMsImFwcF9uYW1lIjoiTVRTIiwiYXBpX3ZlcnNpb24iOiJ2MiJ9.5fmPDODkSkpVhn6-hvMVaYnuHctkMBDz48PvNCo1VB0");
            connection.setRequestProperty("Postman-Token", "ac20fbd9-9216-4336-a113-6fb9d80a0a88");
            connection.setRequestProperty("cache-control", "no-cache");

            writer = new OutputStreamWriter(connection.getOutputStream());
            output = connection.getOutputStream();

            String request = reqObj.toString();

            System.out.println(request);
            output.write(request.getBytes("UTF-8"));
            writer.flush();

            String line;
            InputStreamReader is = null;

            if (connection.getResponseCode() >= 400) {
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
        info("Response .. "+response);
        return response.toString();
    }

}
