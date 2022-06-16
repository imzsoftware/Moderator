package com.traq.manipulator.dataparser;

import com.lambdaworks.redis.GeoWithin;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.traq.util.Utility.sdf;

public class TraqmateGT06Parser extends BaseInitializer{

    public JSONArray process(String rawdata){
        DecimalFormat sixDecimalPlaces = new DecimalFormat("0.000000");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfIST = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String disha="";
        String gps="";
        JSONArray histDataArray = new JSONArray();
//         rawdata = "<cts>2019-10-12 12:31:13</cts><deviceid>10934</deviceid><accid>465</accid><devicetypeid>33</devicetypeid><client>TST</client><imei>868003030637818</imei><vehnum>868003030637818</vehnum>" +
//                 "<data>78781F12130A0C063B08C9030CEA3C08435DC3001479019400043700A2C8000FCEE20D0A78781F12130A0C063B26C9030CEA3C08435DC3001560019400043700A2C80012EA050D0A78781F12130A0C070008C9030CEA3C08435DC3001482019400043700A2C80015706A0D0A78781F12130A0C070026C9030CE93D08435E4700148A019400043700A2C80017F54E0D0A" +
//                 "78781F12130A0C070108C9030CE93D08435E47001467019400043700A2C80019BF260D0A</data>";
        try {
            String data = TagValues.getNodeValue(rawdata, Constants.NODEDATA);
            String [] splitData = data.split("0D0A");
            for (int i =0;i<=splitData.length;i++){
                JSONObject object = new JSONObject();
                String histPacket= "";
                String histData = splitData[i]+"0D0A";
                object.put(Constants.NODEACCOUNTID,TagValues.getNodeValue(rawdata, Constants.NODEACCOUNTID));
                object.put(Constants.NODEDEVICEID,TagValues.getNodeValue(rawdata, Constants.NODEDEVICEID));
                object.put(Constants.NODEVEHICLENUMBER,TagValues.getNodeValue(rawdata, Constants.NODEVEHICLENUMBER));
                object.put(Constants.NODEIMEI,TagValues.getNodeValue(rawdata, Constants.NODEIMEI));
                object.put(Constants.NODEDEVICETYPEID,TagValues.getNodeValue(rawdata, Constants.NODEDEVICETYPEID));
                object.put(Constants.NODECREATEDON,TagValues.getNodeValue(rawdata, Constants.NODECREATEDON));

                String year = String.valueOf(Integer.parseInt(histData.substring(8,10),16)+2000);
                String month =  String.valueOf(Integer.parseInt(histData.substring(10,12),16));
                String day =  String.valueOf(Integer.parseInt(histData.substring(12,14),16));

                String hour = String.valueOf( Integer.parseInt(histData.substring(14,16),16));
                String minute =  String.valueOf(Integer.parseInt(histData.substring(16,18),16));
                String seconds =  String.valueOf(Integer.parseInt(histData.substring(18,20),16));

                String date = year+"-"+month+"-"+day+" "+hour+":"+minute+":"+seconds;
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                sdfIST.setTimeZone(TimeZone.getTimeZone("IST"));

                Date timestamp = sdf.parse(date);
                String dateIST = sdfIST.format(timestamp);

                object.put(Constants.NODEORIGINTS,dateIST);

                String latitude = sixDecimalPlaces.format(Double.valueOf(Integer.parseInt(histData.substring(22,30),16))/1800000);
                String longitude = sixDecimalPlaces.format(Double.valueOf(Integer.parseInt(histData.substring(30,38),16))/1800000);

                object.put(Constants.NODELATITUDE,latitude);
                object.put(Constants.NODELONGITUDE,longitude);

                List<GeoWithin<String>> srcGeo = geoRadius(Double.parseDouble(longitude),Double.parseDouble(latitude));

                object.put(Constants.NODEADDRESS,srcGeo.get(0).getMember());
                object.put(Constants.NODEDISTANCE,srcGeo.get(0).getDistance());

                // String addtess = srcGeo.get(0).getMember();
                //String distance = srcGeo.get(0).getDistance().toString();

                int speed = Integer.parseInt(histData.substring(38,40),16);
                if (speed<=255) {
                    object.put(Constants.NODESPEED, speed);
                }
                if (speed<5){
                    object.put(Constants.NODEIGNITION,"N");
                }else {
                    object.put(Constants.NODEIGNITION,"Y");
                }
                String course = Integer.toBinaryString(Integer.parseInt(histData.substring(40,44),16));
                if (course.length()!=16){
                    String paddedString = StringUtils.leftPad(course, 16, "0");
                    if (Integer.parseInt(paddedString.substring(6,16))<=360) {
                        object.put(Constants.NODEDISHA, paddedString.substring(6, 16));
                    }
                    object.put(Constants.NODEGPS, paddedString.substring(3,4));
//                    disha = paddedString.substring(6,16);
//                    gps = paddedString.substring(3,4);
                }else{
                    object.put(Constants.NODEDISHA, course.substring(6, 16));
                    object.put(Constants.NODEGPS, course.substring(3,4));
                }
                String satetlite = String.valueOf(Integer.parseInt(histData.substring(21,22),16));
                object.put(Constants.NODESATELLITES, satetlite);

                histDataArray.put(object);
//                System.out.println("year .. "+year);
//                System.out.println("latt .. "+latitude);
//                System.out.println("lang .. "+speed);
               // System.out.println("paddi..."+ dir+"   "+gps);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return histDataArray;
    }
}
