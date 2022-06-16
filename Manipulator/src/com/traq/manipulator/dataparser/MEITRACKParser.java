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

public class MEITRACKParser extends BaseInitializer {
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
                String imei = parts[6];
                jsonObject.put(Constants.NODEIMEI, imei);
                String gps = parts[8];
                if(gps.equals("1")){
                    gps="GPS fix";
                    jsonObject.put(Constants.NODEGPS, gps);
                }
                else if(gps.equals("0")){
                    gps="GPS invalid";
                    jsonObject.put(Constants.NODEGPS, gps);
                }
                String time = parts[9];
                String year = String.valueOf(Integer.parseInt(time.substring(4,6))+2000) ;
                String month = time.substring(2, 4);
                String day = time.substring(0, 2);
                String time1 = parts[10];
                String hour = time1.substring(0, 2);
                String minute = time1.substring(2, 4);
                String seconds = time1.substring(4, 6);
                String date = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + seconds;
                Calendar calendar = Calendar.getInstance();

                try{
                    calendar.setTime(sdf.parse(date));

                }
                catch (Exception e){

                }
                sdf.setTimeZone(TimeZone.getTimeZone("IST"));
                jsonObject.put(Constants.NODEORIGINTS, sdf.format(calendar.getTime()));
                String lat = parts[11];
                jsonObject.put(Constants.NODELATITUDE, lat);
                String lng = parts[13];
                jsonObject.put(Constants.NODELONGITUDE, lng);
                String speed = parts[15];
                jsonObject.put(Constants.NODESPEED, speed);
                String dir = parts[16];
                jsonObject.put(Constants.NODEDISHA, dir);
                String satellite = parts[17];
                jsonObject.put(Constants.NODESATELLITES, satellite);
                String alt = parts[18];
                jsonObject.put(Constants.NODEALTITUDE, alt);
                String ign = parts[22];
                if(ign.equals("1")){
                    ign="Ignition On";
                    jsonObject.put(Constants.NODEIGNITION, ign);
                }
                else if(ign.equals("0")){
                    ign="Ignition Off";
                    jsonObject.put(Constants.NODEIGNITION, ign);
                }
                String powerStatus = parts[23];
                if(powerStatus.equals("0")){
                    powerStatus="Vehicle Battery Disconnected";
                    jsonObject.put(Constants.NODEPOWSTATUS, powerStatus);
                }
                else if(powerStatus.equals("1")){
                    powerStatus="Vehicle Battery reConnected";
                    jsonObject.put(Constants.NODEPOWSTATUS, powerStatus);
                }

                String batt = parts[25];
                jsonObject.put(Constants.NODEBATTERY, batt);
                String tamp = parts[27];
                if(tamp.equals("O")){
                    tamp="Cover Open";
                    jsonObject.put(Constants.NODETAMPER, tamp);
                }
                else if(tamp.equals("C")){
                    tamp="Cover Closed";
                    jsonObject.put(Constants.NODETAMPER, tamp);
                }
                String digInp = parts[45];
                String digOut = parts[46];

                histarr.put(jsonObject);

            }
        }
        catch (JSONException je) {

        }
        return histarr;
    }

}