package com.rep.beanobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traq.common.base.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TopOfflineView {
    private Long total=0L;
    private Long hr1=0L;
    private Long hr6=0L;
    private Long hr12=0L;
    private Long hr18=0L;
    private Long hr24=0L;
    private Long offline=0L;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long gethr1() {
        return hr1;
    }

    public void sethr1(Long hr1) {
        this.hr1 = hr1;
    }

    public Long gethr6() {
        return hr6;
    }

    public void sethr6(Long hr6) {
        this.hr6 = hr6;
    }

    public Long gethr12() {
        return hr12;
    }

    public void sethr12(Long hr12) {
        this.hr12 = hr12;
    }

    public Long gethr18() {
        return hr18;
    }

    public void sethr18(Long hr18) {
        this.hr18 = hr18;
    }

    public Long gethr24() {
        return hr24;
    }

    public void sethr24(Long hr24) {
        this.hr24 = hr24;
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

    public static JSONObject toJson(TopOfflineView quickView) {
        JSONObject object = new JSONObject();
        try{
            object.put(Constants.NODETOTAL, quickView.getTotal());
            object.put(Constants.NODEOFFLINE, quickView.getOffline());
            object.put(Constants.NODEHR1, quickView.gethr1());
            object.put(Constants.NODEHR6, quickView.gethr6());
            object.put(Constants.NODEHR12, quickView.gethr12());
            object.put(Constants.NODEHR18, quickView.gethr18());
            object.put(Constants.NODEHR24, quickView.gethr24());


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
                ", hr6=" + hr6 +
                ", hr1=" + hr1 +
                ", hr12=" + hr12 +
                ", hr18=" + hr18 +
                ", hr24=" + hr24 +
                ", offline=" + offline +
                '}';
    }
}
