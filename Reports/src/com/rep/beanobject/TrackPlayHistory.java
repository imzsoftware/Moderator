package com.rep.beanobject;

import com.traq.common.base.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TrackPlayHistory {

    static SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    static SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static DecimalFormat twoDecimalPlaces = new DecimalFormat("0.00");

    private String Name;
    private String vehicleNumber;
    private String deviceType;
    private String Latitude;
    private String Longitude;
    private String Workdatetime;
    private String Location;
    private String Speed;
    private String Status;
    private String direction;
    private String satellites;
    private Double distance;
    private String locksts;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getWorkdatetime() {
        return Workdatetime;
    }

    public void setWorkdatetime(String workdatetime) {
        Workdatetime = workdatetime;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getSpeed() {
        return Speed;
    }

    public void setSpeed(String speed) {
        Speed = speed;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getSatellites() {
        return satellites;
    }

    public void setSatellites(String satellites) {
        this.satellites = satellites;
    }

    public Double getDistance() {return distance; }

    public void setDistance(Double distance) { this.distance = distance; }

    public String getDeviceType() { return deviceType; }

    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getLocksts() { return locksts; }

    public void setLocksts(String locksts) { this.locksts = locksts; }

    public static JSONObject toJson(TrackPlayHistory history)throws JSONException, ParseException {
        JSONObject object = new JSONObject();
        object.accumulate(Constants.NODENAME, history.getName());
        object.accumulate(Constants.NODEVEHICLENUMBER, history.getVehicleNumber());
        object.accumulate(Constants.NODELATITUDE, history.getLatitude());
        object.accumulate(Constants.NODELONGITUDE, history.getLongitude());
        object.accumulate(Constants.NODELOCK,history.getLocksts());
        Date datetime = dbPattern.parse(history.getWorkdatetime());
        object.accumulate(Constants.NODETS, newPattern.format(datetime));
        object.accumulate(Constants.NODEDISTANCE, twoDecimalPlaces.format(history.getDistance()));
        object.accumulate(Constants.NODEADDRESS, history.getLocation());
        object.accumulate(Constants.NODESPEED, history.getSpeed());
        object.accumulate(Constants.NODESTATUS, history.getStatus());
        object.accumulate(Constants.NODESATELLITES, history.getSatellites());
        object.accumulate(Constants.NODEDISHA, history.getDirection());
        object.accumulate(Constants.NODEDEVICETYPE,history.getDeviceType());
        return object;
    }

    public static JSONArray toJsoArray(List<TrackPlayHistory> reportList) throws JSONException, Exception
    {
        JSONArray inner = new JSONArray();

        for(TrackPlayHistory history : reportList) {
            inner.put(toJson(history));
        }
        //array.put(devObj);
        return inner;
    }



}
