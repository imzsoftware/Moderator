package com.rep.beanobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;

import java.util.List;

public class OnOffStatus {

    private String name;
    private Long total=0L;
    private Long online=0L;
    private Long offline=0L;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
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

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapperObj = new ObjectMapper();

        String jsonStr = mapperObj.writeValueAsString(this);

        return jsonStr;
    }

    public String toJsonArray(List<QuickViewObj> dbSummary) throws JsonProcessingException {
        String jsonObj = null;
        JSONArray jsonArray = new JSONArray();
        for(QuickViewObj summary : dbSummary){
            String jsonStr = summary.toJson();
            jsonArray.put(jsonStr);
        }
        jsonObj = jsonArray.toString();

        return jsonObj;
    }


}
