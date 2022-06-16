package com.rep.beanobject;

import com.traq.common.base.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DistrictReport {

    private String taluka;
    private int villages=0;
    private int trips=0;
    private int allocatedtrips=0;
    private String reportdate;


    public String getTaluka() {
        return taluka;
    }

    public void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    public int getVillages() {
        return villages;
    }

    public void setVillages(int villages) {
        this.villages = villages;
    }

    public int getTrips() {
        return trips;
    }

    public void setTrips(int trips) {
        this.trips = trips;
    }

    public String getReportdate() {
        return reportdate;
    }

    public void setReportdate(String reportdate) {
        this.reportdate = reportdate;
    }

    public int getAllocatedtrips() {
        return allocatedtrips;
    }

    public void setAllocatedtrips(int allocatedtrips) {
        this.allocatedtrips = allocatedtrips;
    }

    public static JSONObject toJson(DistrictReport districtReport)  {
        JSONObject object = new JSONObject();
        try{
           // object.put(Constants.NODEACCOUNTNAME, districtReport.getTaluka());
            object.put(Constants.NODETALUKA, districtReport.getTaluka());
            object.put(Constants.NODEVILLAGE,  districtReport.getVillages());
            object.put(Constants.NODEDONETRIP,districtReport.getTrips());
            object.put(Constants.NODEALLOCATEDTRIP,districtReport.getAllocatedtrips());
            object.put(Constants.NODECREATEDON,districtReport.getReportdate());
           }catch (JSONException je){

        }
        return object;
    }

    public static JSONArray toJsonArr(List<DistrictReport> districtReportList) throws JSONException{

        JSONArray array = new JSONArray();
        if(districtReportList!=null) {
            for (DistrictReport districtReport : districtReportList) {

                array.put(toJson(districtReport));
            }
        }
        return array;
    }

}
