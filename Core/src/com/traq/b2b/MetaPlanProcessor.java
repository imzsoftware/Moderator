package com.traq.b2b;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Amit Kamboj on 2/8/16.
 */
public class MetaPlanProcessor {

    public String metaPlan(JSONObject jsonObject){
        String result = "";
        try{

            URL url = new URL("http://db1.umotional.net/india-intermodal-planner/metaplanning/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            OutputStream output = connection.getOutputStream();

            writer.write(jsonObject.toString());
            //output.write(data.toString().getBytes("UTF-8"));
            writer.flush();

            String line;
            InputStreamReader is = null;
            BufferedReader reader = null;
            if (connection.getResponseCode() >= 400) {
                is = new InputStreamReader(connection.getErrorStream());
            } else {
                is = new InputStreamReader(connection.getInputStream());
            }
            reader = new BufferedReader(is);
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            writer.close();
            reader.close();


        }catch(Exception ex){
            System.out.println("Exception.... "+ex);
        }
        return result;
    }

    public static void main(String[] args){
        MetaPlanProcessor tt = new MetaPlanProcessor();
        JSONObject data = tt.buildJsonRequest();
        String response = tt.metaPlan(data);
        System.out.println(response);

    }

    private JSONObject buildJsonRequest(){
        JSONObject request = null;
        try{
            String originLat="27.2222";
            String originLong="75.2244";
            String destLat="26.98777";
            String destLong="76.76754";

            JSONArray jsonArray = new JSONArray();
                /*jsonArray.put("AUTO");
                jsonArray.put("BUS");
                jsonArray.put("TAXI");*/
            jsonArray.put("TRAIN");
            jsonArray.put("PLANE");


            JSONObject origin = new JSONObject();
            origin.put("lon", originLong);
            origin.put("lat", originLat);

            JSONObject dest = new JSONObject();
            dest.put("lat", destLat);
            dest.put("lon", destLong);

            request = new JSONObject();
            request.accumulate("origin",origin);
            request.accumulate("destination", dest);
            request.accumulate("modes", jsonArray);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            String date = "2016-08-10 10:12";
            Date dt = sdf.parse(date);
            System.out.println("sdf.parse(date)..."+sdf.format(dt));
            request.accumulate("departure", sdf.format(dt));
            request.accumulate("client", "WEB");
        }catch(Exception ex){

        }

        System.out.println("JSON OBJECT .  "+request);

        return request;

    }

}


