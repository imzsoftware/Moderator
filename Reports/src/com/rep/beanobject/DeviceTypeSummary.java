package com.rep.beanobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Amit on 27/9/18.
 */
public class DeviceTypeSummary {
    private String name="";
    private Long online=0L;
    private Long offline=0L;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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


    // Convert Object in JSON format
    public String toJson(DeviceTypeSummary dtSummary) throws JsonProcessingException {
        ObjectMapper mapperObj = new ObjectMapper();
        String jsonStr = mapperObj.writeValueAsString(dtSummary);

        return jsonStr;
    }
}
