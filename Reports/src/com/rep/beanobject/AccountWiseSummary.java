package com.rep.beanobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;

import java.util.List;

/**
 * Created by Amit on 27/9/18.
 */
public class AccountWiseSummary {
    private String name="";
    private Long total=0L;
    private Long online=0L;
    private Long offLine=0L;
    private Long hr1=0L;
    private Long hr5=0L;

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

    public Long getOffLine() {
        return offLine;
    }

    public void setOffLine(Long offLine) {
        this.offLine = offLine;
    }

    public Long getHr1() {
        return hr1;
    }

    public void setHr1(Long hr1) {
        this.hr1 = hr1;
    }

    public Long getHr5() {
        return hr5;
    }

    public void setHr5(Long hr5) {
        this.hr5 = hr5;
    }


    // Convert to JSON Format
    public String toJson(AccountWiseSummary acSummary) throws JsonProcessingException {
        ObjectMapper mapperObj = new ObjectMapper();
        String jsonStr = mapperObj.writeValueAsString(acSummary);

        return jsonStr;
    }

    public String toJsonArray(List<AccountWiseSummary> acSummary) throws JsonProcessingException {
        String jsonObj = null;
        JSONArray jsonArray = new JSONArray();
        for(AccountWiseSummary summary : acSummary){
            String jsonStr = toJson(summary);
            jsonArray.put(jsonStr);
        }
        jsonObj = jsonArray.toString();

        return jsonObj;
    }

}
