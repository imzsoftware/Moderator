package com.rep.beanobject;

import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.util.Utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class InOutHistory extends BaseInitializer {

    private static SimpleDateFormat inputPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat outputPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private String vehicleno;
    private String lockStatus;
    private String location;
    private String latlng;
    private String oprName;
    private String startDate;
    private String endDate;
    private String rfid;
    private String eventtype;
    private String passwordstatus;
    private String gps;
    private String reason;


    public String getLockStatus() { return lockStatus; }

    public void setLockStatus(String lockStatus) { this.lockStatus = lockStatus; }


    public String getStartDate() { return startDate; }

    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }

    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getEventtype() {
        return eventtype;
    }

    public void setEventtype(String eventtype) {
        this.eventtype = eventtype;
    }

    public String getPasswordstatus() {
        return passwordstatus;
    }

    public void setPasswordstatus(String passwordstatus) {
        this.passwordstatus = passwordstatus;
    }

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }

    public String getVehicleno() {
        return vehicleno;
    }

    public void setVehicleno(String vehicleno) {
        this.vehicleno = vehicleno;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLatlng() {
        return latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public String getOprName() {
        return oprName;
    }

    public void setOprName(String oprName) {
        this.oprName = oprName;
    }

    public String getReason() {return reason; }

    public void setReason(String reason) { this.reason = reason; }

    public static JSONArray toJsonArr(List<InOutHistory> trackList){

        JSONArray array = new JSONArray();
        if(trackList!=null) {
            for (InOutHistory dev : trackList) {
                array.put(toJson(dev));
            }
        }
        return array;
    }


    public static JSONObject toJson(InOutHistory th) {
        JSONObject object = new JSONObject();
        try{
            object.put(Constants.NODEVEHICLENUMBER,nullAndEmpty(th.getVehicleno()));
            object.put(Constants.NODEADDRESS,nullAndEmpty(th.getLocation()));
            object.put(Constants.NODEGEOCODE,nullAndEmpty(th.getLatlng()));
            object.put(Constants.NODELOCK,nullAndEmpty(th.getLockStatus()));
            object.put(Constants.NODEGPS,nullAndEmpty(th.getGps()));
            object.put(Constants.NODERFID,nullAndEmpty(th.getRfid()));
            object.put(Constants.NODEEVENTSOURCETYPE,nullAndEmpty(th.getEventtype()));
            object.put(Constants.NODEPASSWORDSTATUS,nullAndEmpty(th.getPasswordstatus()));
            object.put(Constants.NODEREASON,nullAndEmpty(th.getReason()));
            if(checkNullAndEmpty(th.getOprName())) {
                object.put(Constants.NODENAME, th.getOprName());
            }else{
                object.put(Constants.NODENAME,nullAndEmpty(th.getRfid()));
            }
            Date startDate = null;
            Date endDate = null;
            try {
                startDate = inputPattern.parse(th.getStartDate());
                object.put(Constants.NODESTARTDATE,nullAndEmpty(outputPattern.format(startDate)));
                try {
                    endDate = inputPattern.parse(th.getEndDate());
                    object.put(Constants.NODEENDDATE,nullAndEmpty(outputPattern.format(endDate)));
                    long millisec = endDate.getTime() - startDate.getTime();
                    object.put(Constants.NODEDURATION,Utility.convertDuration(millisec));
                }catch (Exception e){
                    object.put(Constants.NODEENDDATE,"NA");
                    object.put(Constants.NODEDURATION,Utility.convertDuration(0));
                }
            }catch (Exception e){
                object.put(Constants.NODESTARTDATE,"NA");
                object.put(Constants.NODEENDDATE,"NA");
                object.put(Constants.NODEDURATION,Utility.convertDuration(0));
                e.printStackTrace();
            }
        }catch (Exception je){
            je.printStackTrace();
        }
        return object;
    }


    public static Comparator<InOutHistory> timeComparator = new Comparator<InOutHistory>() {

        public int compare(InOutHistory t1, InOutHistory t2) {
            Date time1 = null;
            Date time2 = null;
            try {
                time1 = inputPattern.parse(t1.getStartDate());
                time2 = inputPattern.parse(t2.getStartDate());
            }
            /*catch (ParseException pe){}
            catch (NumberFormatException nfe){}*/
            catch (Exception e){e.printStackTrace();}

            //ascending order
            return time1.compareTo(time2);

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }};


}
