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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class G300Parser extends BaseInitializer {

    public JSONArray process(String rawdata) {

        debug("History Data....."+rawdata);
        DecimalFormat sixDecimalPlaces = new DecimalFormat("0.000000");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        JSONArray histarr = new JSONArray();

        try {
            String data = TagValues.getNodeValue(rawdata, Constants.NODEDATA);
            Integer pktLength = Integer.parseInt(data.substring(0, 2), 16);
            List<String> dataList = new ArrayList<String>();
            while(pktLength > 0 && data.length()>pktLength*2){
                dataList.add(data.substring(2,(pktLength+1)*2));
                data = data.substring((pktLength+1)*2);
                try {
                    pktLength = Integer.parseInt(data.substring(0, 2), 16);
                }catch (Exception ee){
                    pktLength = 0;
                }
            }

            JSONObject addMsgObj = null;

            for (int i = 0; i < dataList.size(); i++) {
                JSONObject jsonObject = new JSONObject();

                String histData = dataList.get(i);
                debug("History Data....."+histData);
                jsonObject.put(Constants.NODEACCOUNTID, TagValues.getNodeValue(rawdata, Constants.NODEACCOUNTID));
                jsonObject.put(Constants.NODENAME, TagValues.getNodeValue(rawdata, Constants.NODENAME));
                jsonObject.put(Constants.NODEDEVICEID, TagValues.getNodeValue(rawdata, Constants.NODEDEVICEID));
                jsonObject.put(Constants.NODECLIENT, TagValues.getNodeValue(rawdata, Constants.NODECLIENT));
                jsonObject.put(Constants.NODEVEHICLENUMBER, TagValues.getNodeValue(rawdata, Constants.NODEVEHICLENUMBER));
                jsonObject.put(Constants.NODEIMEI, TagValues.getNodeValue(rawdata, Constants.NODEIMEI));
                jsonObject.put(Constants.NODEDEVICETYPEID, TagValues.getNodeValue(rawdata, Constants.NODEDEVICETYPEID));
                jsonObject.put(Constants.NODECREATEDON, TagValues.getNodeValue(rawdata, Constants.NODECREATEDON));
                jsonObject.put(Constants.NODEPACKETTYPE, "H");

                // Accuracy in meters
                Integer accuracy = Integer.parseInt(histData.substring(0, 4), 16);
                jsonObject.put(Constants.NODEACCURACY, accuracy);


                String year = String.valueOf(Integer.parseInt(histData.substring(44, 46)) + 2000);
                String month = histData.substring(46, 48);
                String day = histData.substring(48, 50);
                String hour = histData.substring(50, 52);
                String minute = histData.substring(52, 54);
                String seconds = histData.substring(54, 56);
                String date = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + seconds;
                Calendar calendar = Calendar.getInstance();
                try {
                    //sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                    calendar.setTime(sdf.parse(date));
                    //System.out.println("GMT TIME " + (sdf.parse(date)));
                    calendar.add(calendar.HOUR, -2);
                    calendar.add(calendar.MINUTE, -30);

                } catch (Exception e) {
                    error("G300 Exception  " + e.getMessage());
                }

                sdf.setTimeZone(TimeZone.getTimeZone("IST"));
                // System.out.println("IST Format.." + sdf.format(calendar.getTime()));
                jsonObject.put(Constants.NODEORIGINTS, sdf.format(calendar.getTime()));

                String latitude = sixDecimalPlaces.format(Double.valueOf(Integer.parseInt(histData.substring(16, 24), 16)) / 1000000);
                String longitude = sixDecimalPlaces.format(Double.valueOf(Integer.parseInt(histData.substring(24, 32), 16)) / 1000000);

                jsonObject.put(Constants.NODELATITUDE, latitude);
                jsonObject.put(Constants.NODELONGITUDE, longitude);
                try {
                    List<GeoWithin<String>> srcGeo = geoRadius(Double.parseDouble(longitude), Double.parseDouble(latitude));
                    jsonObject.put(Constants.NODEADDRESS, srcGeo.get(0).getMember());
                    jsonObject.put(Constants.NODEDISTANCE, srcGeo.get(0).getDistance());
                }catch (Exception e){

                    jsonObject.put(Constants.NODEADDRESS, "NA");
                    jsonObject.put(Constants.NODEDISTANCE, 0.0D);
                }
                String alt = String.valueOf(Integer.parseInt(histData.substring(32, 36), 16));
                jsonObject.put(Constants.NODEALTITUDE, alt);

                int speed = Integer.parseInt(histData.substring(36, 40), 16);

                jsonObject.put(Constants.NODESPEED, speed/10);

                String status_bit = Integer.toBinaryString(Integer.parseInt(histData.substring(8, 12), 16));

                if (status_bit.length()!=16) {
                    status_bit = StringUtils.leftPad(status_bit, 16, "0");
                }
                if(!histData.substring(12, 16).isEmpty()) {
                    String status_bit1 = Integer.toBinaryString(Integer.parseInt(histData.substring(12, 16), 16));
                    if (status_bit1.length() != 16) {
                        status_bit1 = StringUtils.leftPad(status_bit1, 16, "0");
                    }
                    status_bit = status_bit + status_bit1;
                }

                status_bit = StringUtils.reverse(status_bit);
                String gpsPos = "";
                gpsPos = status_bit.substring(1, 2);
                if (gpsPos.equals("0")) {
                    gpsPos = "V";
                } else {
                    gpsPos = "A";
                }

                jsonObject.put(Constants.NODEGPS, gpsPos);
                jsonObject.put(Constants.NODEBATCHARGE, status_bit.substring(4, 6));

                String direction = String.valueOf(Integer.parseInt(histData.substring(40, 44), 16));
                jsonObject.put(Constants.NODEDISHA, direction);

                if (addMsgObj == null){
                    addMsgObj = addMsg(histData);
                }
                jsonObject.put(Constants.NODELOCK, TagValues.getNodeValue(addMsgObj,Constants.NODELOCK));
                jsonObject.put(Constants.NODEUNLOCKSTATUS, TagValues.getNodeValue(addMsgObj,Constants.NODEUNLOCKSTATUS));
                jsonObject.put(Constants.NODEBATTERY, TagValues.getNodeValue(addMsgObj,Constants.NODEBATTERY));
                jsonObject.put(Constants.NODETAMPER, TagValues.getNodeValue(addMsgObj,Constants.NODETAMPER));
                jsonObject.put(Constants.NODEREASON, TagValues.getNodeValue(addMsgObj,Constants.NODEREASON));
                jsonObject.put(Constants.NODESATELLITES, TagValues.getNodeValue(addMsgObj,Constants.NODESATELLITES));
                jsonObject.put(Constants.NODERFID, TagValues.getNodeValue(addMsgObj,Constants.NODERFID));

                histarr.put(jsonObject);
            }

        } catch (JSONException je) {
            error("G300 JSONException  "+je.getMessage());
        }
        //info("histarr..........."+histarr);
        return histarr;
    }

    private JSONObject addMsg(String histData) throws JSONException{
        String addMsgId = "0";
        String addMsg = "";
        String operator_id="NA";
        JSONObject jsonObject = new JSONObject();
        addMsg = histData.substring(60).replaceAll("\n", "");
        addMsgId = String.valueOf(Integer.parseInt(histData.substring(56,58), 16));

        if (addMsgId.equals("51")) {
            StringBuilder out = new StringBuilder();
            for (int j = 0; j < addMsg.length(); j += 2) {
                String str = addMsg.substring(j, j + 2);
                out.append((char) Integer.parseInt(str, 16));

            }
            String str = out.toString();

            String[] parts = str.split(",");            //Split whole string with comma's
            String part1 = parts[0];
            String part2 = parts[1];
            String part3 = parts[2];

            String[] p = part3.split("&");              //At index 2 further splitting
            String pa1 = p[0];                                  //Use it only
            //String pa2 = p[1];
            //String pa3 = p[2];

            info("Battery................."+pa1);
            String lock_status = "";
            lock_status = pa1.substring(0, 1);
            jsonObject.put(Constants.NODEUNLOCKSTATUS, lock_status);

            String seal_status = "";
            seal_status = pa1.substring(1, 2);
            jsonObject.put(Constants.NODELOCK, seal_status);
            //jsonObject.put(Constants.NODESTATUS, seal_status);

            int battery;
            battery = Integer.parseInt(pa1.substring(2, 5));
            debug("Battery................."+battery);
            String battery_per="0";

            if( battery<=250)
            {
                battery_per="0";
            }
            else if(battery>250 && battery<=350)
            {
                battery_per="5";
            }
            else if(battery>350 && battery<=368)
            {
                battery_per="10";
            }
            else if(battery>368 && battery<=370)
            {
                battery_per="15";
            }
            else if(battery>370 && battery<=373)
            {
                battery_per="20";
            }
            else if(battery>373 && battery<=377)
            {
                battery_per="30";
            }
            else if(battery>377 && battery<=379)
            {
                battery_per="40";
            }
            else if(battery>379 && battery<=382)
            {
                battery_per="50";
            }
            else if(battery>382 && battery<=387)
            {
                battery_per="60";
            }
            else if(battery>387 && battery<=393)
            {
                battery_per="70";
            }
            else if(battery>393 && battery<=400)
            {
                battery_per="80";
            }
            else if(battery>400 && battery<=408)
            {
                battery_per="90";
            }
            else if(battery>408 && battery<=419)
            {
                battery_per="95";
            }
            else if (battery>=420)
            {
                battery_per="100";
            }

            jsonObject.put(Constants.NODEBATTERY, battery_per);

            String tamper_status = "";
            tamper_status = pa1.substring(5, 6);

            jsonObject.put(Constants.NODETAMPER, tamper_status);

            String report_reason = "";
            report_reason = pa1.substring(6, 7);

            switch(report_reason)
            {
                case "0": report_reason = "S";
                    break;

                case "1": report_reason = "LSC";
                    operator_id="000000";
                    break;

                case "2":  report_reason = "TSC";
                    operator_id="000000";
                    break;

                case "3":  report_reason = "Locked";
                    operator_id="000000";
                    break;

                case "4": report_reason = "RFID";
                    operator_id=pa1.substring(7,13);
                    break;

                case "5": report_reason = "RFID";
                    operator_id=pa1.substring(7,13);
                    break;

                case "6": report_reason = "SMS";
                    operator_id=pa1.substring(7,13);
                    break;

                case "7": report_reason = "SMS";
                    operator_id=pa1.substring(7,13);
                    break;

                case "8": report_reason = "Low battery sleep";
                    break;

                case "9": report_reason = "Invalid RFID";
                    break;

                case "A":  report_reason = "Invalid RFID";
                    break;

                case "B": report_reason = "RFID";
                    break;

                case "C": report_reason = "RFID";
                    break;

                case "D": report_reason = "BT";
                    operator_id=pa1.substring(7,13);
                    break;

                case "E": report_reason = "BT";
                    operator_id=pa1.substring(7,13);
                    break;

                case "F": report_reason = "API";
                    operator_id="000000";
                    break;

                case "G": report_reason = "API";
                    operator_id="000000";
                    break;

                case "H":  report_reason = "Invalid RFID";
                    break;

                case "I": report_reason = "Invalid RFID";
                    break;
/*
                        case "L":  report_reason = "sublock is unlocked under seal status";
                            break;

                        case "M": report_reason = "sublock tampered";
                            break;

                        case "N": report_reason = "sublock communication time out";
                            break;

                        case "O":  report_reason = "sublock status changed";
                            break;

                        case "P": report_reason = "outside area swipe seal card";
                            break;

                        case "Q": report_reason = "outside area swipe unseal card";
                            break;

                        case "R": report_reason = "auto unseal in desgnated area";
                            break;

                        case "S": report_reason = "Button request unlock";
                            break;*/

                case "T": report_reason = "Emergency";
                    break;

                default: report_reason = "S";

            }

            jsonObject.put(Constants.NODEREASON, report_reason);
            jsonObject.put(Constants.NODESATELLITES, "111");
            jsonObject.put(Constants.NODERFID,operator_id);

        }
        return jsonObject;
    }
}
