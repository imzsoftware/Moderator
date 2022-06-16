package com.imz.praj.data.obj;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountTarget {
    private Long accId;
    private Long blockId;
    private Long districtId;
    private Integer totalDevices = 0;
    private String cts;

    public Long getAccId() {
        return accId;
    }

    public void setAccId(Long accId) {
        this.accId = accId;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public Integer getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(Integer totalDevices) {
        this.totalDevices = totalDevices;
    }

    public String getCts() {
        return cts;
    }

    public void setCts(String cts) {
        this.cts = cts;
    }


    @Override
    public String toString() {
        return "AccountTarget{" +
                "accId=" + accId +
                ", blockId=" + blockId +
                ", districtId=" + districtId +
                ", totalDevices=" + totalDevices+
                '}';
    }
}
