package com.traq.beanobjects;

import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.base.BaseInitializer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * *********************************************************
 * Copyright:     DBTN Online Pvt. Ltd.
 * distancesbetween.com project
 * Creator:       Amit Kamboj
 * Date:          3/7/17 5:32 PM
 * *********************************************************
 */
public class SaveProcessorObject extends BaseInitializer {

    private ResponseMessage responseMessage;
    private ProcessorObject processorObject;
    private Boolean saveToWalletHistory = false;
    private Boolean saveToSubTransaction = false;

    public Boolean getSaveToSubTransaction() {
        return saveToSubTransaction;
    }

    public void setSaveToSubTransaction(Boolean saveToSubTransaction) {
        this.saveToSubTransaction = saveToSubTransaction;
    }

    public Boolean getSaveToWalletHistory() {
        return saveToWalletHistory;
    }

    public void setSaveToWalletHistory(Boolean saveToWalletHistory) {
        this.saveToWalletHistory = saveToWalletHistory;
    }


    public SaveProcessorObject() {

    }

    public ResponseMessage getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public ProcessorObject getProcessorObject() {
        return processorObject;
    }

    public void setProcessorObject(ProcessorObject processorObject) {
        this.processorObject = processorObject;
    }

}
