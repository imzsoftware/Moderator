package com.traq.manipulator.dataparser;

import com.lambdaworks.redis.GeoWithin;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class JT701Parser extends BaseInitializer {

    public JSONArray process(String rawdata) {

        debug("History Data....." + rawdata);
        DecimalFormat sixDecimalPlaces = new DecimalFormat("0.000000");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONArray histarr = new JSONArray();

        String gps = "";
        try {

            String data = TagValues.getNodeValue(rawdata, Constants.NODEDATA);
            String header = data.substring(0,20);
            String [] newData = data.split(header);

            int length = newData.length;
            for (int i = 1; i < length; i++) {

                JSONObject jsonObject = new JSONObject();
                String histData = newData[i];
                jsonObject.put(Constants.NODEACCOUNTID, TagValues.getNodeValue(rawdata, Constants.NODEACCOUNTID));
                jsonObject.put(Constants.NODEDEVICEID, TagValues.getNodeValue(rawdata, Constants.NODEDEVICEID));
                jsonObject.put(Constants.NODECLIENT, TagValues.getNodeValue(rawdata, Constants.NODECLIENT));
                jsonObject.put(Constants.NODEVEHICLENUMBER, TagValues.getNodeValue(rawdata, Constants.NODEVEHICLENUMBER));
                jsonObject.put(Constants.NODEIMEI, TagValues.getNodeValue(rawdata, Constants.NODEIMEI));
                jsonObject.put(Constants.NODEDEVICETYPEID, TagValues.getNodeValue(rawdata, Constants.NODEDEVICETYPEID));
                jsonObject.put(Constants.NODECREATEDON, TagValues.getNodeValue(rawdata, Constants.NODECREATEDON));
                jsonObject.put(Constants.NODEPACKETTYPE, "H");

                String day = String.valueOf(Integer.parseInt(histData.substring(0, 2)));
                String month = String.valueOf(Integer.parseInt(histData.substring(2, 4)));
                String year = String.valueOf(Integer.parseInt(histData.substring(4, 6)) + 2000);

                String hour = String.valueOf(Integer.parseInt(histData.substring(6, 8)));
                String minute = String.valueOf(Integer.parseInt(histData.substring(8, 10)));
                String seconds = String.valueOf(Integer.parseInt(histData.substring(10, 12)));
                String date = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + seconds;
                Calendar calendar = Calendar.getInstance();
                try {
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                    calendar.setTime(sdf.parse(date));
                } catch (Exception e) {

                }

                sdf.setTimeZone(TimeZone.getTimeZone("IST"));
                jsonObject.put(Constants.NODEORIGINTS, sdf.format(calendar.getTime()));

                double latitude1 = 0;

                //String latitude = sixDecimalPlaces.format(Double.valueOf(Integer.parseInt(histData.substring(12, 20))) / 1000000);
                latitude1 = Double.valueOf(Integer.parseInt(histData.substring(12, 20))) / 1000000.0;

                try {
                    //latitude1 = Double.parseDouble(latitude);
                    if (latitude1 < 0) {
                        latitude1 = (0 - latitude1) / 60;
                        jsonObject.put(Constants.NODELATITUDE, latitude1);
                    } else {
                        jsonObject.put(Constants.NODELATITUDE, latitude1);
                        //jsonObject.put(Constants.NODELATITUDE, latitude);
                    }


                    double longitude1 = 0;
                    //String longitude = sixDecimalPlaces.format(Double.valueOf(Integer.parseInt(histData.substring(20, 29))) / 1000000);
                    longitude1 = Double.valueOf(Integer.parseInt(histData.substring(20, 29))) / 1000000.0;

                    //longitude1 = Double.parseDouble(longitude);
                    if (longitude1 < 0) {
                        longitude1 = (0 - longitude1) / 60;
                        jsonObject.put(Constants.NODELONGITUDE, longitude1);
                    } else {
                        //jsonObject.put(Constants.NODELONGITUDE, longitude);
                        jsonObject.put(Constants.NODELONGITUDE, longitude1);
                    }
                    jsonObject.put(Constants.NODELONGITUDE, longitude1);
                    try {
                        List<GeoWithin<String>> srcGeo = geoRadius(longitude1, latitude1);
                        //List<GeoWithin<String>> srcGeo = geoRadius(Double.parseDouble(longitude), Double.parseDouble(latitude));
                        jsonObject.put(Constants.NODEADDRESS, srcGeo.get(0).getMember());
                        jsonObject.put(Constants.NODEDISTANCE, srcGeo.get(0).getDistance());
                    }catch (Exception e){

                        jsonObject.put(Constants.NODEADDRESS, "NA");
                        jsonObject.put(Constants.NODEDISTANCE, 0.0D);
                    }
                } catch (Exception e) {

                }

                String loc_ind = Integer.toBinaryString(Integer.parseInt(histData.substring(29, 30), 16));
                if (loc_ind.length()!=4) {
                    loc_ind = StringUtils.leftPad(loc_ind, 4, "0");
                    loc_ind = StringUtils.reverse(loc_ind);

                }
                gps = loc_ind.substring(3, 4);
                if("1".equals(gps)) {
                    jsonObject.put(Constants.NODEGPS, "A");
                }else{
                    jsonObject.put(Constants.NODEGPS, "V");
                }


                double speed = Integer.parseInt(histData.substring(30, 32), 16);
                if (speed <= 255) {
                    jsonObject.put(Constants.NODESPEED, speed);
                }
                String direction = String.valueOf(Integer.parseInt(histData.substring(32, 34), 16) * 2);
                jsonObject.put(Constants.NODEDISHA, direction);

                String satelite = String.valueOf(Integer.parseInt(histData.substring(42, 44), 16));
                jsonObject.put(Constants.NODESATELLITES, satelite);

                try {
                    String dev_status = Integer.toBinaryString(Integer.parseInt(histData.substring(52, 56), 16));
                    if (dev_status.length()!=16) {
                        dev_status = StringUtils.rightPad(dev_status, 16, "0");
                        //dev_status = StringUtils.reverse(dev_status);

                    }
                    String reservd = dev_status.substring(0, 1);
                    jsonObject.put(Constants.NODERESERVED, reservd);
                    String motor = dev_status.substring(1, 2);
                    jsonObject.put(Constants.NODEMOTORFAULT, motor);
                    String back_cap = dev_status.substring(2, 3);
                    String open_cap = dev_status.substring(3, 4);
                    String low_battery = dev_status.substring(4, 5);
                    jsonObject.put(Constants.NODELOW, low_battery);
                    String rfid = dev_status.substring(5, 6);
                    jsonObject.put(Constants.NODERFID, rfid);
                    String wrong_pwd = dev_status.substring(6, 7);
                    jsonObject.put(Constants.NODEPASSWORDSTATUS, wrong_pwd);
                    String unlock_alarm = dev_status.substring(7, 8);
                    jsonObject.put(Constants.NODEUNLOCKSTATUS, unlock_alarm);
                    String motor_lock = dev_status.substring(8, 9);
                    jsonObject.put(Constants.NODELOCK, motor_lock);
                    String steel_string = dev_status.substring(9, 10);
                    jsonObject.put(Constants.NODESTRINGCUT, steel_string);
                    String confirmed = dev_status.substring(10, 11);
                    jsonObject.put(Constants.NODECONFIRMPIN, confirmed);
                    /*String vibration_alarm = dev_status.substring(11, 12);
                    String cut_alarm = dev_status.substring(12, 13);
                    String exit_geofence = dev_status.substring(13, 14);
                    String enter_geofence = dev_status.substring(14, 15);
                    String lbs = dev_status.substring(15, 16);*/

                }

                catch (Exception e) {

                }
                Integer battery = Integer.parseInt(histData.substring(56, 58), 16);
                jsonObject.put(Constants.NODEBATTERY,battery);
                if(battery == 255){
                    jsonObject.put(Constants.NODEBATCHARGE,1);
                }else{
                    jsonObject.put(Constants.NODEBATCHARGE,0);
                }

                histarr.put(jsonObject);

            }

        } catch (JSONException je) {

        }
        debug("histarr..........."+histarr);
        return histarr;

    }
}
