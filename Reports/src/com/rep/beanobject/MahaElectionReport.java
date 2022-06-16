package com.rep.beanobject;

import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MahaElectionReport extends BaseInitializer {
    private Long accountId;
    private String accountName;
    private String vehicleNumber;
    private String driverName;
    private String driverNumber;
    private String distance;
    private String stoppageCount;
    private String runningDuration;
    private String stoppageDuration;

    public static JSONArray toJsonArr(List<MahaElectionReport> list) {

        JSONArray array = new JSONArray();
        if (list != null) {
            for (MahaElectionReport dev : list) {
                array.put(toJson(dev));
            }
        }
        return array;
    }

    public static JSONObject toJson(MahaElectionReport th) {
        JSONObject object = new JSONObject();
        try {
            object.put(Constants.NODEVEHICLENUMBER, nullAndEmpty(th.getVehicleNumber()));
            object.put(Constants.NODEACCOUNTID, nullAndEmpty(th.getAccountId()));
            object.put(Constants.NODEACCOUNTNAME, nullAndEmpty(th.getAccountName()));
            object.put(Constants.NODEDRIVERNAME, nullAndEmpty(th.getDriverName()));
            object.put(Constants.NODEDRIVERPHONE, nullAndEmpty(th.getDriverNumber()));
            object.put(Constants.NODEDISTANCE, nullAndEmpty(th.getDistance()));
            object.put(Constants.NODEDURATION, nullAndEmpty(th.getRunningDuration()));
            object.put(Constants.NODECOUNT, nullAndEmpty(th.getStoppageCount()));
            object.put(Constants.NODESTOPDURATION, nullAndEmpty(th.getStoppageDuration()));
        } catch (Exception je) {
            je.printStackTrace();
        }
        return object;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverNumber() {
        return driverNumber;
    }

    public void setDriverNumber(String driverNumber) {
        this.driverNumber = driverNumber;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getStoppageCount() {
        return stoppageCount;
    }

    public void setStoppageCount(String stoppageCount) {
        this.stoppageCount = stoppageCount;
    }

    public String getRunningDuration() {
        return runningDuration;
    }

    public void setRunningDuration(String runningDuration) {
        this.runningDuration = runningDuration;
    }

    public String getStoppageDuration() {
        return stoppageDuration;
    }

    public void setStoppageDuration(String stoppageDuration) {
        this.stoppageDuration = stoppageDuration;
    }
}
