package com.traq.manipulator.dataparser;

import com.lambdaworks.redis.GeoWithin;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class FM1120Parser extends BaseInitializer {
    public org.json.JSONArray process(String binaryData, String deviceDtl){
        DecimalFormat sixDecimalPlaces = new DecimalFormat("0.000000");
        info("FM1120Parser............");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        org.json.JSONArray histarr = new org.json.JSONArray();

        try {
            String data = TagValues.getNodeValue(binaryData, Constants.NODEDATA);
           // System.out.println("data "+data);
            String header = data.substring(0,20);
            debug("FM1120Parser............header = "+header);

            String actPkt = data.substring(20);

            int dataLength = Integer.parseInt(header.substring(8,16),16);
            int numOfPackets = Integer.parseInt(header.substring(18,20),16);
            int dataLength1 = dataLength*2-6;
            int n = dataLength1/numOfPackets;

            if(numOfPackets == 1){
                return histarr;
            }

            List<String> histPkts = new ArrayList<>();

            int index=0;
            while (index<dataLength1){
                histPkts.add(actPkt.substring(index,Math.min(index+n,dataLength1)));
                index+=n;
            }

            // First Packet already parsed as live packet, so starting from 2nd packet
            for(int j=1;j<histPkts.size();j++){
                org.json.JSONObject jsonObject = new org.json.JSONObject();

                jsonObject.put(Constants.NODEACCOUNTID, TagValues.getNodeValue(deviceDtl, Constants.NODEACCOUNTID));
                jsonObject.put(Constants.NODEDEVICEID, TagValues.getNodeValue(deviceDtl, Constants.NODEDEVICEID));
                jsonObject.put(Constants.NODECLIENT, TagValues.getNodeValue(deviceDtl, Constants.NODECLIENT));
                jsonObject.put(Constants.NODEVEHICLENUMBER, TagValues.getNodeValue(deviceDtl, Constants.NODEVEHICLENUMBER));
                jsonObject.put(Constants.NODEIMEI, TagValues.getNodeValue(deviceDtl, Constants.NODEIMEI));
                jsonObject.put(Constants.NODEDEVICETYPEID, TagValues.getNodeValue(deviceDtl, Constants.NODEDEVICETYPEID));
                jsonObject.put(Constants.NODECREATEDON, TagValues.getNodeValue(deviceDtl, Constants.NODECREATEDON));
                jsonObject.put(Constants.NODEPACKETTYPE, "H");
                String newHistData = histPkts.get(j);

                try{
                    Long time=Long.parseLong(newHistData.substring(0,16),16);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(time);
                    jsonObject.put(Constants.NODEORIGINTS, sdf.format(calendar.getTime()));

                    String priority=String.valueOf(Integer.parseInt(newHistData.substring(16,18),16));
                    jsonObject.put(Constants.NODEPRIORITY, priority);

                    String longitude = sixDecimalPlaces.format(Double.valueOf(Integer.parseInt(newHistData.substring(18, 26),16)) / 10000000);
                    jsonObject.put(Constants.NODELONGITUDE, longitude);
                    String latitude = sixDecimalPlaces.format(Double.valueOf(Integer.parseInt(newHistData.substring(26, 34),16)) / 10000000);
                    jsonObject.put(Constants.NODELATITUDE, latitude);

                    try {
                        List<GeoWithin<String>> srcGeo = geoRadius(Double.parseDouble(longitude), Double.parseDouble(latitude));
                        jsonObject.put(Constants.NODEADDRESS, srcGeo.get(0).getMember());
                        jsonObject.put(Constants.NODEDISTANCE, srcGeo.get(0).getDistance());
                    }catch (Exception e){

                        jsonObject.put(Constants.NODEADDRESS, "NA");
                        jsonObject.put(Constants.NODEDISTANCE, 0.0D);
                    }

                    String altitude = String.valueOf(Integer.parseInt(newHistData.substring(34,38), 16));
                    jsonObject.put(Constants.NODEALTITUDE, altitude);

                    String direction = String.valueOf(Integer.parseInt(newHistData.substring(38,42), 16));
                    jsonObject.put(Constants.NODEDISHA, direction);

                    int satVal = Integer.parseInt(newHistData.substring(42, 44), 16);
                    String satelite = String.valueOf(satVal);
                    jsonObject.put(Constants.NODESATELLITES, satelite);

                    if(satVal >3){
                        jsonObject.put(Constants.NODEGPS, "A");
                    }else{
                        jsonObject.put(Constants.NODEGPS, "V");
                    }

                    double speed = Integer.parseInt(newHistData.substring(44,48), 16);
                    jsonObject.put(Constants.NODESPEED, speed);

                    String eventId=String.valueOf(Integer.parseInt(newHistData.substring(48,50), 16));

                    String numOfIoElements=String.valueOf(Integer.parseInt(newHistData.substring(50,52), 16));

                    int numOfOneByteElement=Integer.parseInt( newHistData.substring(52,54), 16);
                    debug("FM1120Parser............"+jsonObject.getString(Constants.NODEDEVICEID) +"  numOfIoElements ==  "+numOfIoElements);
                    if(numOfOneByteElement!=0){

                        String IoElementOneByteHeader = newHistData.substring(54,54+numOfOneByteElement*4);

                        List<String> oneByteList = new ArrayList<>();
                        int dataLength2=IoElementOneByteHeader.length();
                        int n1=4;
                        int indexOneByte=0;
                        while (indexOneByte<dataLength2){
                            oneByteList.add(IoElementOneByteHeader.substring(indexOneByte,Math.min(indexOneByte+n1,dataLength2)));
                            indexOneByte+=n1;
                        }

                        for(String oneButeIoData : oneByteList) {
                            int Id=Integer.parseInt(oneButeIoData.substring(0,2),16);
                            int value=Integer.parseInt(oneButeIoData.substring(2,4),16);

                            switch(Id){
                                case 1:
                                    jsonObject.put(Constants.NODEIGNITION, onOff(value));
                                    break;
                                case 2:
                                    jsonObject.put(Constants.NODEAC, onOff(value));
                                    break;
                                case 3:
                                    jsonObject.put(Constants.NODEDOOR, onOff(value));
                                    break;
                                case 179:
                                    jsonObject.put(Constants.NODEIMMOBILISER, onOff(value));
                                    break;
                            }
                        }
                    }
                    int numOfTwoByteElement=Integer.parseInt( newHistData.substring(54+numOfOneByteElement*4,54+numOfOneByteElement*4+2), 16);
                    if(numOfTwoByteElement > 0) {
                        String IoElementTwoByteHeader = newHistData.substring(54+numOfOneByteElement*4+2,54+numOfOneByteElement*4+2+numOfTwoByteElement*6);
                        List<String> twoByteList = new ArrayList<>();
                        int dataLength3=IoElementTwoByteHeader.length();
                        int n2=6;
                        int indexTwoByte=0;
                        while (indexTwoByte<dataLength3){
                            twoByteList.add(IoElementTwoByteHeader.substring(indexTwoByte,Math.min(indexTwoByte+n2,dataLength3)));
                            indexTwoByte+=n2;
                        }

                        for(String twoByteIoData : twoByteList) {
                            int id=Integer.parseInt(twoByteIoData.substring(0,2),16);
                            int value=Integer.parseInt(twoByteIoData.substring(2,6),16);
                            info("ID="+id +" VALUE="+value);
                            double fuel = 0.0;
                            switch(id){
                                case 66:
                                    double powerStatus=value/1000.0;
                                    if(powerStatus<0.0){
                                        jsonObject.put(Constants.NODEPOWSTATUS, "N");
                                    }else{
                                        jsonObject.put(Constants.NODEPOWSTATUS, "Y");
                                    }
                                    break;
                                case 9:
                                    fuel=value/1000.0;
                                    break;
                            }
                            jsonObject.put(Constants.NODEFUEL, fuel);
                            jsonObject.put(Constants.NODELOADS, fuel);
                        }
                    }
                    int numOfFourByteElement=Integer.parseInt( newHistData.substring(54+numOfOneByteElement*4+2+numOfTwoByteElement*6,54+numOfOneByteElement*4+2+numOfTwoByteElement*6+2), 16);

                    if(numOfFourByteElement!=0) {
                        String IoElementFourByteHeader = newHistData.substring(54+numOfOneByteElement*4+2+numOfTwoByteElement*6+2,54+numOfOneByteElement*4+2+numOfTwoByteElement*6+2+numOfFourByteElement*10);
                    }
                    int numOfEightByteElement=Integer.parseInt( newHistData.substring(54+numOfOneByteElement*4+2+numOfTwoByteElement*6+2+numOfFourByteElement*10,54+numOfOneByteElement*4+2+numOfTwoByteElement*6+2+numOfFourByteElement*10+2), 16);
                    if(numOfEightByteElement!=0) {
                        String IoElementEightByteHeader = newHistData.substring(54 + numOfOneByteElement * 4 + 2 + numOfTwoByteElement * 6 + 2 + numOfFourByteElement * 10 + 2, 54 + numOfOneByteElement * 4 + 2 + numOfTwoByteElement * 6 + 2 + numOfFourByteElement * 10 + 2 + numOfEightByteElement * 18);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                histarr.put(jsonObject);
            }
        } catch (org.json.JSONException je) {
            je.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        debug("FM1120Parser............"+TagValues.getNodeValue(deviceDtl, Constants.NODEDEVICEID) +" RETURN  histarr ==  "+histarr);
        return histarr;
    }

    private String onOff(int value){
        if(value == 1)
            return "Y";
        else
            return "N";
    }
}

