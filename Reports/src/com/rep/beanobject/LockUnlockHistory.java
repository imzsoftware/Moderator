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

public class LockUnlockHistory extends BaseInitializer {

    private static SimpleDateFormat inputPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static Comparator<LockUnlockHistory> timeComparator = new Comparator<LockUnlockHistory>() {

        public int compare(LockUnlockHistory t1, LockUnlockHistory t2) {
            Date time1 = null;
            Date time2 = null;
            try {
                time1 = inputPattern.parse(t1.getStartDate());
                time2 = inputPattern.parse(t2.getStartDate());
            }
            /*catch (ParseException pe){}
            catch (NumberFormatException nfe){}*/ catch (Exception e) {
                e.printStackTrace();
            }

            //ascending order
            return time1.compareTo(time2);

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }
    };
    private static SimpleDateFormat outputPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private String vehicleno;
    private String lockStatus;
    private String startLocation;
    private String startlatlng;
    private String endLocation;
    private String endlatlng;
    private String startDate;
    private String endDate;
    private String speed;
    private String status;
    private String rfid;
    private String eventtype;
    private String passwordstatus;
    private String gps;

    public static JSONArray toJsonArr(List<LockUnlockHistory> trackList) {

        JSONArray array = new JSONArray();
        if (trackList != null) {
            for (LockUnlockHistory dev : trackList) {
                array.put(toJson(dev));
            }
        }
        return array;
    }

    public static JSONObject toJson(LockUnlockHistory th) {
        JSONObject object = new JSONObject();
        try {
            object.put(Constants.NODEVEHICLENUMBER, nullAndEmpty(th.getVehicleno()));
            object.put(Constants.NODESTARTLOCATION, nullAndEmpty(th.getStartLocation()));
            object.put(Constants.NODESOURCELATLNG, nullAndEmpty(th.getStartlatlng()));
            object.put(Constants.NODEDESTINATIONLATLNG, nullAndEmpty(th.getEndlatlng()));
            object.put(Constants.NODEENDLOCATION, nullAndEmpty(th.getEndLocation()));
            object.put(Constants.NODESPEED, nullAndEmpty(th.getSpeed(), "0"));
            object.put(Constants.NODELOCK, nullAndEmpty(th.getLockStatus()));
            object.put(Constants.NODEGPS, nullAndEmpty(th.getGps()));
            object.put(Constants.NODERFID, nullAndEmpty(th.getRfid()));
            object.put(Constants.NODEEVENTSOURCETYPE, nullAndEmpty(th.getEventtype()));
            object.put(Constants.NODEPASSWORDSTATUS, nullAndEmpty(th.getPasswordstatus()));
            Date startDate = null;
            Date endDate = null;
            try {
                startDate = inputPattern.parse(th.getStartDate());
                try {
                    object.put(Constants.NODESTARTDATE, nullAndEmpty(outputPattern.format(startDate)));
                    endDate = inputPattern.parse(th.getEndDate());
                    object.put(Constants.NODEENDDATE, nullAndEmpty(outputPattern.format(endDate)));
                    long millisec = endDate.getTime() - startDate.getTime();
                    object.put(Constants.NODEDURATION, Utility.convertDuration(millisec));
                } catch (Exception e) {
                    object.put(Constants.NODEENDDATE, "NA");
                    object.put(Constants.NODEDURATION, Utility.convertDuration(0));
                }
            } catch (Exception e) {
                object.put(Constants.NODESTARTDATE, "NA");
                object.put(Constants.NODEENDDATE, "NA");
                object.put(Constants.NODEDURATION, Utility.convertDuration(0));
                e.printStackTrace();
            }
        } catch (Exception je) {
            je.printStackTrace();
        }
        return object;
    }

    public String getStartlatlng() {
        return startlatlng;
    }

    public void setStartlatlng(String startlatlng) {
        this.startlatlng = startlatlng;
    }

    public String getEndlatlng() {
        return endlatlng;
    }

    public void setEndlatlng(String endlatlng) {
        this.endlatlng = endlatlng;
    }

    public String getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(String lockStatus) {
        this.lockStatus = lockStatus;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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


}
