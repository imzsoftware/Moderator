package com.traq.manipulator.dataparser;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lambdaworks.redis.GeoWithin;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class JV200Parser extends BaseInitializer {

    public JSONArray process(String rawdata){


        DecimalFormat sixDecimalPlaces = new DecimalFormat("0.000000");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String disha="";
        String gps="";

        JSONArray histarr = new JSONArray();
        try {

            String data = TagValues.getNodeValue(rawdata, Constants.NODEDATA);
            String[] splitData = data.split("0D0A");
            for (int i = 0; i <= splitData.length; i++) {
                JSONObject jsonObject = new JSONObject();
                String histPacket = "";
                String histData = splitData[i] + "0D0A";
                jsonObject.put(Constants.NODEACCOUNTID, TagValues.getNodeValue(rawdata, Constants.NODEACCOUNTID));
                jsonObject.put(Constants.NODEDEVICEID, TagValues.getNodeValue(rawdata, Constants.NODEDEVICEID));
                jsonObject.put(Constants.NODECLIENT, TagValues.getNodeValue(rawdata, Constants.NODECLIENT));
                jsonObject.put(Constants.NODEVEHICLENUMBER, TagValues.getNodeValue(rawdata, Constants.NODEVEHICLENUMBER));
                jsonObject.put(Constants.NODEIMEI, TagValues.getNodeValue(rawdata, Constants.NODEIMEI));
                jsonObject.put(Constants.NODEDEVICETYPEID, TagValues.getNodeValue(rawdata, Constants.NODEDEVICETYPEID));
                jsonObject.put(Constants.NODECREATEDON, TagValues.getNodeValue(rawdata, Constants.NODECREATEDON));
                jsonObject.put(Constants.NODEPACKETTYPE, "H");
                //78782222130A0F073B1BCF02BF09080921E6E000148A0195340644003972000001037ED0070D0A

                String year = String.valueOf(Integer.parseInt(histData.substring(8,10),16)+2000);
                String month =  String.valueOf(Integer.parseInt(histData.substring(10,12),16));
                String day =  String.valueOf(Integer.parseInt(histData.substring(12,14),16));

                String hour = String.valueOf( Integer.parseInt(histData.substring(14,16),16));
                String minute =  String.valueOf(Integer.parseInt(histData.substring(16,18),16));
                String seconds =  String.valueOf(Integer.parseInt(histData.substring(18,20),16));
                String date = year+"-"+month+"-"+day+" "+hour+":"+minute+":"+seconds;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
                jsonObject.put(Constants.NODEORIGINTS, sdf.format(date));
                String latitude = sixDecimalPlaces.format(Double.valueOf(Integer.parseInt(histData.substring(22,30),16))/1800000);
                String longitude = sixDecimalPlaces.format(Double.valueOf(Integer.parseInt(histData.substring(30,38),16))/1800000);

                jsonObject.put(Constants.NODELATITUDE,latitude);
                jsonObject.put(Constants.NODELONGITUDE,longitude);

                List<GeoWithin<String>> srcGeo = geoRadius(Double.parseDouble(longitude),Double.parseDouble(latitude));

                jsonObject.put(Constants.NODEADDRESS,srcGeo.get(0).getMember());
                jsonObject.put(Constants.NODEDISTANCE,srcGeo.get(0).getDistance());

                int speed = Integer.parseInt(histData.substring(38,40),16);
                if (speed<=255) {
                    jsonObject.put(Constants.NODESPEED, speed);
                }

                String course = Integer.toBinaryString(Integer.parseInt(histData.substring(40,44),16));
                if (course.length()!=16){
                    String paddedString = StringUtils.leftPad(course, 16, "0");
                    paddedString=StringUtils.reverse(paddedString);
                    if (Integer.parseInt(paddedString.substring(6,16))<=360) {
                        jsonObject.put(Constants.NODEDISHA, paddedString.substring(6, 16));
                    }
                    jsonObject.put(Constants.NODEGPS, paddedString.substring(3,4));
                }else{
                    jsonObject.put(Constants.NODEDISHA, course.substring(6, 16));
                    jsonObject.put(Constants.NODEGPS, course.substring(3,4));
                }
                String satetlite = String.valueOf(Integer.parseInt(histData.substring(21,22),16));
                jsonObject.put(Constants.NODESATELLITES, satetlite);

                String ign = String.valueOf(Integer.parseInt(histData.substring(60,62),16));
                jsonObject.put(Constants.NODEIGNITION, ign);

                histarr.put(jsonObject);

            }

        }catch (JSONException je) {

        }
        return histarr;

}}
