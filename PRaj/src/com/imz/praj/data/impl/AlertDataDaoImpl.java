package com.imz.praj.data.impl;

import com.imz.praj.data.AlertDataDao;
import com.imz.praj.data.obj.PrajReportData;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.base.BaseInitializer;
import com.traq.common.data.entity.Account;
import com.traq.db.MongoConnection;
import org.bson.Document;
import org.bson.conversions.Bson;


import java.text.SimpleDateFormat;
import java.util.*;

public class AlertDataDaoImpl extends BaseInitializer implements AlertDataDao {
    private SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); private static final String entityName = "ALERTDATA";
    private static final int asc = 1;
    private static final int desc = -1;

    public List<PrajReportData> findAlerts(RequestMessage rm, List<Account> accountList, boolean ascsort) {
        List<PrajReportData> logList = null;
        try {
            BasicDBObject alertData = new BasicDBObject();
            if (checkNullAndEmpty(rm.getAssetId()) && rm.getAssetId().longValue() > 0L) {
                BasicDBObject assetObj = new BasicDBObject();
                assetObj.put("$eq", rm.getAssetId());
                alertData.put("deviceid", assetObj);
            } else if (rm.getAssetids() != null && rm.getAssetids().size() == 1) {
                BasicDBObject assetObj = new BasicDBObject();
                assetObj.put("$eq", rm.getAssetids().get(0));
                alertData.put("deviceid", assetObj);
            } else if (rm.getAssetids() != null && rm.getAssetids().size() > 0) {
                BasicDBObject assetObj = new BasicDBObject();
                assetObj.put("$in", rm.getAssetids());
                alertData.put("deviceid", assetObj);
            }
            if (accountList != null && accountList.size() > 0) {
                BasicDBObject accObj = new BasicDBObject();
                List<Long> accIds = new ArrayList<>();
                accountList.stream().forEach(account -> accIds.add(account.getId()));
                accObj.put("$in", accIds);
                alertData.put("accid", accObj);
            } else if (checkNullAndEmpty(rm.getAccId()) && rm.getAccId().longValue() > 0L) {
                BasicDBObject accObj = new BasicDBObject();
                accObj.put("$eq", rm.getAccId());
                alertData.put("accid", accObj);
            }

            if (rm.getCodes() != null && rm.getCodes().size() > 0) {
                BasicDBObject basicDBObject = new BasicDBObject();
                basicDBObject.put("$in", rm.getCodes());
                alertData.put("type", basicDBObject);
            }else{
                BasicDBObject basicDBObject = new BasicDBObject();
                basicDBObject.put("$eq", "IGN");
                alertData.put("type", basicDBObject);
            }

            BasicDBObject codeObj = null;

            if (checkNullAndEmpty(rm.getStartDate()) && checkNullAndEmpty(rm.getEndDate())) {
                BasicDBObject dates = new BasicDBObject();
                dates.put("$gte", rm.getStartDate());
                dates.put("$lte", rm.getEndDate());

                alertData.put("cts", dates);
                MongoConnection conn = MongoConnection.getInstance();
                try {
                    String entity = "";
                    String client = rm.getVendorcode();
                    if (checkNullAndEmpty(client)) {
                        if (client.endsWith("_")) {
                            entity = client + "ALERTDATA";
                        } else {
                            entity = client + "_" + "ALERTDATA";
                        }
                    }
                    else {
                        entity = "ALERTDATA".toLowerCase();
                    }
                    conn.init();
                    MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);
                    int sort = 1;
                    if (!ascsort) {
                        sort = -1;
                    }

                    FindIterable<Document> trackIterable = null;
                   // info("AlertDataDaoImpl findAlerts 2.................... " + entity + ", " + alertData + ", " + sort);
                    trackIterable = mongoCollection.find((Bson)alertData).sort((Bson)new BasicDBObject("cts", Integer.valueOf(sort)));

                    logList = PrajReportData.createList(trackIterable);
                    info("AlertDataDaoImpl  logList = "+logList);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return logList;
    }


}

