package com.imz.praj.entity;

import com.traq.common.apihandler.RequestMessage;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.AlertType;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.Driver;
import com.traq.common.data.model.dao.AlertTypeDao;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ViewDetailRep {
    private Long quantity = 0L;
    private String date;
    private String onTime = "NA";
    private String offTime = "NA";
    private Long runTime = 0L;

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOnTime() {
        return onTime;
    }

    public void setOnTime(String onTime) {
        this.onTime = onTime;
    }

    public String getOffTime() {
        return offTime;
    }

    public void setOffTime(String offTime) {
        this.offTime = offTime;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }

    public static JSONObject toJson(ViewDetailRep detailRep){
        JSONObject object = new JSONObject();
        try {
            object.put(Constants.NODEQUANTITY, detailRep.getQuantity());
            object.put(Constants.NODESTARTTIME, detailRep.getOnTime());
            object.put(Constants.NODEENDTIME, detailRep.getOffTime());
            object.put(Constants.NODERUNNING, detailRep.getRunTime());
            object.put(Constants.NODEDATE, detailRep.getDate());

        } catch (JSONException je) {
            je.printStackTrace();
            return null;
        }
        catch (NullPointerException nfe) {
            nfe.printStackTrace();
            return null;
        }
        return object;
    }
    public static JSONArray toJsonArr(List<ViewDetailRep> detailReps){
        JSONArray jsonArray = new JSONArray();
        if (detailReps != null && detailReps.size() > 0)
            for (ViewDetailRep detailRep : detailReps) {
                if (null != detailRep) {
                    jsonArray.put(toJson(detailRep));
                }
            }
        return jsonArray;
    }

    @Override
    public String toString() {
        return "ViewDetailRep{" +
                "quantity=" + quantity +
                ", date='" + date + '\'' +
                ", onTime='" + onTime + '\'' +
                ", offTime='" + offTime + '\'' +
                ", runTime=" + runTime +
                '}';
    }
}
