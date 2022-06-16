package com.traq.core;

import com.traq.beanobjects.ProcessorObject;
import com.traq.common.base.BaseInitializer;
import com.traq.common.exceptions.EntityException;
import com.traq.common.exceptions.NotFound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * *********************************************************
 * Copyright:     DBTN Online Pvt. Ltd.
 * distancesbetween.com Route Planner
 * Creator:       Amit Kamboj
 * Date:          3/7/17 5:32 PM
 * *********************************************************
 */
public abstract class GenerateProcessorObject extends BaseInitializer{

    public abstract ProcessorObject processObject()
            throws EntityException;

    public abstract Boolean saveObject()
            throws EntityException;

    private Map<String, Integer> transactionTimeout = null;



    private void getApplicableBlackoutTransaction() throws EntityException {

        String blackoutTransactions = (getAppConfig().getBlackoutTransactionWithTime() != null) ? getAppConfig().getBlackoutTransactionWithTime().trim() : null;
        Map blackoutTransactionsMap = (getAppConfig().getBlackoutTransactionWithTimeMap() != null) ? getAppConfig().getBlackoutTransactionWithTimeMap() : null;

        if (blackoutTransactionsMap == null || blackoutTransactionsMap.size() == 0) {
            if (blackoutTransactions != null && !blackoutTransactions.trim().isEmpty()) {
                if (getAppConfig().getBlackoutTransactionWithTimeMap() == null ||
                        getAppConfig().getBlackoutTransactionWithTimeMap().size() == 0) {


                    String[] transactionTime = blackoutTransactions.split("[|]");
                    transactionTimeout = new HashMap<String, Integer>(transactionTime.length);

                    for (int i = 0; i < transactionTime.length; i++) {
                        String[] s = transactionTime[i].split("[:]");
                        Integer timeoutPeriod = 0;
                        try {
                            timeoutPeriod = Integer.parseInt(s[1].trim());
                        } catch (NumberFormatException e) {
                            info("BlackOut Time Period Is invalid format Please check for it for Integer value for transaction: " + s[0]);
                            throw new NotFound("Configure Blackout Period Not Has Integer Value, Transaction Name :" + s[0]);
                        }


                        transactionTimeout.put(s[0], timeoutPeriod);


                    }

                }
            } else {
                info("No Blackout Feature Implemented, Please make entry [blackoutTransactionWithTime] if You want to Apply");
            }

        }
    }


    public Boolean checkNotNullAndEmpty(String tagValue) {
        Boolean result = false;
        if (tagValue != null && !tagValue.trim().isEmpty()) {
            result = true;

        }
        return result;

    }
}

