package com.rep.beanobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traq.common.base.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Created by Amit on 27/9/18.
 */
public class QuickViewObj {
    private Long total=0L;
    private Long running=0L;
    private Long hr5=0L;
    private Long hr1=0L;
    private Long idle=0L;
    private Long stop=0L;
    private Long online=0L;
    private Long offline=0L;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getRunning() {
        return running;
    }

    public void setRunning(Long running) {
        this.running = running;
    }

    public Long getHr5() {
        return hr5;
    }

    public void setHr5(Long hr5) {
        this.hr5 = hr5;
    }

    public Long getHr1() {
        return hr1;
    }

    public void setHr1(Long hr1) {
        this.hr1 = hr1;
    }

    public Long getIdle() {
        return idle;
    }

    public void setIdle(Long idle) {
        this.idle = idle;
    }

    public Long getStop() {
        return stop;
    }

    public void setStop(Long stop) {
        this.stop = stop;
    }

    public Long getOnline() {
        return online;
    }

    public void setOnline(Long online) {
        this.online = online;
    }

    public Long getOffline() {
        return offline;
    }

    public void setOffline(Long offline) {
        this.offline = offline;
    }

    public String toJson() throws JsonProcessingException{
        ObjectMapper mapperObj = new ObjectMapper();

        String jsonStr = mapperObj.writeValueAsString(this);

        return jsonStr;
    }

    public static JSONObject toJson(QuickViewObj quickView) {
        JSONObject object = new JSONObject();
        try{
            object.put(Constants.NODETOTAL, quickView.getTotal());
            object.put(Constants.NODEOFFLINE, quickView.getOffline());
            object.put(Constants.NODEONLINE, quickView.getOnline());
            object.put(Constants.NODESTOP, quickView.getStop());
            object.put(Constants.NODEIDLE, quickView.getIdle());
            object.put(Constants.NODEHR1, quickView.getHr1());
            object.put(Constants.NODEHR5, quickView.getHr5());
            object.put(Constants.NODERUNNING, quickView.getRunning());

        }catch (JSONException je){
            je.printStackTrace();
            return null;

        }catch (NullPointerException nfe){
            return null;
        }
        return object;
    }

    public String toJsonArray(List<QuickViewObj> dbSummary) throws JsonProcessingException {
        String jsonObj = null;
        JSONArray jsonArray = new JSONArray();
        for(QuickViewObj summary : dbSummary){
            JSONObject obj = QuickViewObj.toJson(summary);
            jsonArray.put(obj);
        }
        jsonObj = jsonArray.toString();

        return jsonObj;
    }

    @Override
    public String toString() {
        return "QuickViewObj{" +
                "total=" + total +
                ", running=" + running +
                ", hr5=" + hr5 +
                ", hr1=" + hr1 +
                ", idle=" + idle +
                ", stop=" + stop +
                ", online=" + online +
                ", offline=" + offline +
                '}';
    }
}
