package com.traq.manipulator.dataparser;

import com.traq.common.apihandler.TagValues;
import com.traq.common.base.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class BHARAT101Parser {
    public JSONArray process(String rawdata) {

        DecimalFormat sixDecimalPlaces = new DecimalFormat("0.000000");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        JSONArray histarr = new JSONArray();

        try {

            String data = TagValues.getNodeValue(rawdata, Constants.NODEDATA);
            String[] splitData = data.split("\r\n");

            for (int i = 0; i < splitData.length; i++) {
                JSONObject jsonObject = new JSONObject();
                String histPacket = "";
                String histData = splitData[i];
                jsonObject.put(Constants.NODEACCOUNTID, TagValues.getNodeValue(rawdata, Constants.NODEACCOUNTID));
                jsonObject.put(Constants.NODEDEVICEID, TagValues.getNodeValue(rawdata, Constants.NODEDEVICEID));
                jsonObject.put(Constants.NODECLIENT, TagValues.getNodeValue(rawdata, Constants.NODECLIENT));
                jsonObject.put(Constants.NODEVEHICLENUMBER, TagValues.getNodeValue(rawdata, Constants.NODEVEHICLENUMBER));
                jsonObject.put(Constants.NODEIMEI, TagValues.getNodeValue(rawdata, Constants.NODEIMEI));
                jsonObject.put(Constants.NODEDEVICETYPEID, TagValues.getNodeValue(rawdata, Constants.NODEDEVICETYPEID));
                jsonObject.put(Constants.NODECREATEDON, TagValues.getNodeValue(rawdata, Constants.NODECREATEDON));
                jsonObject.put(Constants.NODEPACKETTYPE, "H");

                String[] parts = data.split(",");            //Split whole string with comma's
//                System.out.println("string "+data);
                String imei = parts[6];
//                System.out.println("Imei "+imei);
                String veh_reg = parts[7];
//                System.out.println("Vehicle reg no. "+veh_reg);

                String lat = parts[11].replaceAll("\n", "");
                String lng = parts[13];
                jsonObject.put(Constants.NODELATITUDE, lat);
                jsonObject.put(Constants.NODELONGITUDE, lng);
                String year1 = parts[9];
//                System.out.println("Year value "+year1.substring(4,8));
                String year = year1.substring(4,8) ;
//                System.out.println("year"+year);
                String month = year1.substring(2, 4);
//                System.out.println("month"+month);
                String day = year1.substring(0, 2);
//                System.out.println("day"+day);
                String time = parts[10];
                String hour = time.substring(0,2);
                String minute = time.substring(2, 4);
                String seconds = time.substring(4, 6);
                String date = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + seconds;
//                System.out.println("date "+date);
                Calendar calendar = Calendar.getInstance();

                try{
                    calendar.setTime(sdf.parse(date));

                }
                catch (Exception e){

                }
                sdf.setTimeZone(TimeZone.getTimeZone("IST"));
                jsonObject.put(Constants.NODEORIGINTS, sdf.format(calendar.getTime()));

                String gpsfix = parts[8];
                jsonObject.put(Constants.NODEGPS, gpsfix);

                String speed = parts[15];
                jsonObject.put(Constants.NODESPEED, speed);
                String direction = parts[18];
                jsonObject.put(Constants.NODEDISHA, direction);
                String satellite = parts[17];
                jsonObject.put(Constants.NODESATELLITES, satellite);
                String tamper = parts[27];
                jsonObject.put(Constants.NODETAMPER, tamper);
                if (tamper.equals("C")){
                    tamper="Cover Closed";
                }
                else if(tamper.equals("O")){
                    tamper="Cover Opened";
                }
                String powerStatus = parts[23];
                jsonObject.put(Constants.NODEPOWSTATUS, powerStatus);
                String ignitionStatus = parts[22];
                if(ignitionStatus.equals("1"))
                {
                    ignitionStatus="Y";
                }
                else {
                    ignitionStatus="N";
                }
                jsonObject.put(Constants.NODEIGNITION, ignitionStatus);
                String inBattery = parts[25];
                jsonObject.put(Constants.NODEBATTERY, inBattery);

                String distance = String.valueOf(Double.parseDouble(parts[50])/1000);
                jsonObject.put(Constants.NODEDISTANCE, distance);
                histarr.put(jsonObject);

            }

        }

        catch (JSONException je) {

        }
        return histarr;
    }

}
