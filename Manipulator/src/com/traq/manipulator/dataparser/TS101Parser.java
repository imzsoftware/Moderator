package com.traq.manipulator.dataparser;

import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class TS101Parser extends BaseInitializer {
    public JSONArray process(String rawdata) {

        debug("History Data....." + rawdata);
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
                String imei = parts[1];
                jsonObject.put(Constants.NODEIMEI, imei);
                String msg_eventCode = parts[2];
                jsonObject.put(Constants.NODEMESSAGE, msg_eventCode);
                String lat = parts[3];
                String lng = parts[4];
                jsonObject.put(Constants.NODELATITUDE, lat);
                jsonObject.put(Constants.NODELONGITUDE, lng);
                String time = parts[5];
                String year = String.valueOf(Integer.parseInt(time.substring(0, 2)) + 2000);
                String month = time.substring(2, 4);
                String day = time.substring(4, 6);
                String hour = time.substring(6, 8);
                String minute = time.substring(8, 10);
                String seconds = time.substring(10, 12);
                String date = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + seconds;
                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(sdf.parse(date));

                } catch (Exception e) {

                }
                sdf.setTimeZone(TimeZone.getTimeZone("IST"));
                jsonObject.put(Constants.NODEORIGINTS, sdf.format(calendar.getTime()));

                String gnss = parts[6];
                if (gnss.equals("A")) {
                    gnss = "A";
                } else {
                    gnss = "V";
                }
                jsonObject.put(Constants.NODEGPS, gnss);
                String speed = parts[8];
                jsonObject.put(Constants.NODESPEED, speed);
                String distance = parts[9];
                jsonObject.put(Constants.NODEDISTANCE, distance);
                String direction = parts[10];
                jsonObject.put(Constants.NODEDISHA, direction);
                String satellite = parts[11];
                jsonObject.put(Constants.NODESATELLITES, satellite);
                Integer voltage = Integer.parseInt(parts[15]);
                jsonObject.put(Constants.NODEFUEL, voltage/1000.0);
                String tamper = parts[17];
                jsonObject.put(Constants.NODETAMPER, tamper);
                String powerStatus = parts[23];
                jsonObject.put(Constants.NODEPOWSTATUS, powerStatus);
                String ignitionStatus = parts[27];
                if (ignitionStatus.equals("1")) {
                    ignitionStatus = "Y";
                } else {
                    ignitionStatus = "N";
                }
                jsonObject.put(Constants.NODEIGNITION, ignitionStatus);

/*                String hA = parts[42];
                jsonObject.put(Constants.NODEHARSHBREAK, hA);
                String hB = parts[43];
                jsonObject.put(Constants.NODEHARSHACC, hB);*/
                histarr.put(jsonObject);

            }

        }

        catch (JSONException je) {

        }
        return histarr;
    }

}

