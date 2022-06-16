package com.traq.beanobjects;

import com.traq.common.apihandler.RequestMessage;
import com.traq.common.data.entity.*;


/**
 * *********************************************************
 * Copyright:     DBTN Online Pvt. Ltd.
 * distancesbetween.com project
 * Creator:       Amit Kamboj
 * Date:          3/7/17 5:32 PM
 * *********************************************************
 */
public class ProcessorObject {

    private String message;
    private Long searchId;
    private Vendor vendor;
    private Status status;          // default active status
    private Country country;

    private Boolean success;
    private String resvalue;
    private String language = "";
    private String mpinexpiry;
    private String ipinexpiry;
    private String comments;
    private String transRefNo;
    private String udv1;
    private RequestMessage requestMessage;
    private String  pgauthid;
    private String  servicetype;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getSearchId() {
        return searchId;
    }

    public void setSearchId(Long searchId) {
        this.searchId = searchId;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }


    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getResvalue() {
        return resvalue;
    }

    public void setResvalue(String resvalue) {
        this.resvalue = resvalue;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMpinexpiry() {
        return mpinexpiry;
    }

    public void setMpinexpiry(String mpinexpiry) {
        this.mpinexpiry = mpinexpiry;
    }

    public String getIpinexpiry() {
        return ipinexpiry;
    }

    public void setIpinexpiry(String ipinexpiry) {
        this.ipinexpiry = ipinexpiry;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTransRefNo() {
        return transRefNo;
    }

    public void setTransRefNo(String transRefNo) {
        this.transRefNo = transRefNo;
    }

    public String getUdv1() {
        return udv1;
    }

    public void setUdv1(String udv1) {
        this.udv1 = udv1;
    }

    public RequestMessage getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(RequestMessage requestMessage) {
        this.requestMessage = requestMessage;
    }

    public String getPgauthid() {
        return pgauthid;
    }

    public void setPgauthid(String pgauthid) {
        this.pgauthid = pgauthid;
    }

    public String getServicetype() {
        return servicetype;
    }

    public void setServicetype(String servicetype) {
        this.servicetype = servicetype;
    }
}