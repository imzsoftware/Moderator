package com.traq.manipulator.dataparser;

import com.traq.common.base.BaseInitializer;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ParserMain extends BaseInitializer {

    private String devicetypeid = "0";
    private String data;
    private String deviceDetail;

    public ParserMain(){}

    public ParserMain(String devicetypeid,String data){
        this.devicetypeid = devicetypeid ;
        this.data = data;
    }

    public ParserMain(String devicetypeid,String data, String deviceDetail){
        this.devicetypeid = devicetypeid ;
        this.data = data;
        this.deviceDetail = deviceDetail;
    }

    public String getDevicetypeid() {
        return devicetypeid;
    }

    public void setDevicetypeid(String devicetypeid) {
        this.devicetypeid = devicetypeid;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDeviceDetail() {
        return deviceDetail;
    }

    public void setDeviceDetail(String deviceDetail) {
        this.deviceDetail = deviceDetail;
    }

    public JSONArray parse(){
        List<String> parsedData = new ArrayList<>();
        JSONArray jsonArray =new JSONArray();
        debug("ParserMain... devicetypeid........."+devicetypeid);
        switch(devicetypeid){
            case "14":
            case "15":
                FM1120Parser fm1120Parser = new FM1120Parser();
                jsonArray = fm1120Parser.process(data, deviceDetail);
                break;
            case "19":
                JV200Parser parser2 = new JV200Parser();
                jsonArray = parser2.process(data);
                break;
            case "41":
                TraqmateGT06Parser parser1 = new TraqmateGT06Parser();
                jsonArray = parser1.process(data);
                break;
            case "55":
            case "56":
                G300Parser parser3 = new G300Parser();
                jsonArray = parser3.process(data);
                break;
            case "50":
            case "57":
                TS101Parser parser4 = new TS101Parser();
                jsonArray = parser4.process(data);
                break;
            case "58":
                BHARAT101Parser parser8 = new BHARAT101Parser();
                jsonArray = parser8.process(data);
                break;
            case "62":
                JT701Parser parser6 = new JT701Parser();
                jsonArray = parser6.process(data);
                break;
            case " ":
                MEITRACKParser parser9 = new MEITRACKParser();
                jsonArray = parser9.process(data);
                break;

//            case "":
//                JV03HistoryParser parser7 = new JV03HistoryParser();
//                jsonArray = parser7.process(data);
//                break;
//
//
//            case "   ":
//                FM1202Parser parser9 = new FM1202Parser();
//                jsonArray = parser9.process(data);
//                break;
        }
        return jsonArray;
    }
}
