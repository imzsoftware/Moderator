package com.imz.praj.data.impl;

import com.imz.praj.data.obj.PrajReportData;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Accumulators;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.data.entity.Account;
import com.traq.db.MongoConnection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.SimpleDateFormat;
import java.util.*;

public class PrajReportDaoImpl extends BaseInitializer {
    private SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static final String entityName = "BOREWELLDATA";
    private SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int asc = 1;
    private static final int desc = -1;

    public void saveReport(Document data, String client) {
        MongoConnection conn = MongoConnection.getInstance();
        try {
            conn.init();
            String entity = "REPORT";

            client = nullAndEmpty(client, "TM");
            if (checkNullAndEmpty(client)){
                if (client.endsWith("_")) {
                    entity = client + entity;
                } else {
                    entity = client + "_" + entity;
                }
            } else {
                entity = entity.toUpperCase();
            }
            MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);
            mongoCollection.insertOne(data);
        } catch (Exception exception) {
        } finally {
        }
    }

    public PrajReportData findGenReport(RequestMessage rm) {
        PrajReportData reportData = null;
        try {
            BasicDBObject alertData = new BasicDBObject();
            if (checkNullAndEmpty(rm.getAssetId()) && rm.getAssetId().longValue() > 0L) {
                BasicDBObject assetObj = new BasicDBObject();
                assetObj.put("$eq", rm.getAssetId());
                alertData.put("deviceid", assetObj);
            }

            if (checkNullAndEmpty(rm.getStartDate()) && checkNullAndEmpty(rm.getEndDate())) {
                BasicDBObject dates = new BasicDBObject();
                dates.put("$gte", rm.getStartDate().substring(0,  rm.getStartDate().indexOf(" ")));
                dates.put("$lte", rm.getEndDate().substring(0,  rm.getEndDate().indexOf(" ")));

                alertData.put(Constants.NODEDATE, dates);
                MongoConnection conn = MongoConnection.getInstance();
                try {
                    String entity = "PRAJ_REPORT";

                    conn.init();
                    MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);
                    int sort = 1;

                    FindIterable<Document> trackIterable = null;
                    //info("PrajReportDaoImpl findAlerts 2.................... " + entity + ", " + alertData + ", " + sort);
                    trackIterable = mongoCollection.find((Bson)alertData).sort((Bson)new BasicDBObject("date", Integer.valueOf(sort)));
                    //info("PrajReportData.wardGenReport.................... " );
                    reportData = PrajReportData.wardGenReport(trackIterable);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reportData;
    }

    public PrajReportData findNonFuncSchemeReport(RequestMessage rm) {
        PrajReportData reportData = null;
        try {
            BasicDBObject alertData = new BasicDBObject();
            if (checkNullAndEmpty(rm.getAssetId()) && rm.getAssetId().longValue() > 0L) {
                BasicDBObject assetObj = new BasicDBObject();
                assetObj.put("$eq", rm.getAssetId());
                alertData.put("deviceid", assetObj);
            }

            if (checkNullAndEmpty(rm.getStartDate()) && checkNullAndEmpty(rm.getEndDate())) {
                BasicDBObject dates = new BasicDBObject();
                dates.put("$gte", rm.getStartDate().substring(0,  rm.getStartDate().indexOf(" ")));
                dates.put("$lte", rm.getEndDate().substring(0,  rm.getEndDate().indexOf(" ")));

                alertData.put(Constants.NODEDATE, dates);
                BasicDBObject func = new BasicDBObject();
                func.put("$eq", 0L);
                alertData.put(Constants.NODETOTAL,func);
                MongoConnection conn = MongoConnection.getInstance();
                try {
                    String entity = "PRAJ_REPORT";

                    conn.init();
                    MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);
                    int sort = 1;

                    FindIterable<Document> trackIterable = null;
                    //info("PrajReportDaoImpl findAlerts 2.................... " + entity + ", " + alertData + ", " + sort);
                    trackIterable = mongoCollection.find(alertData).sort(new BasicDBObject("date", sort));
                    //info("PrajReportData.wardGenReport.................... " );
                    reportData = PrajReportData.wardGenReport(trackIterable);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reportData;
    }

    public PrajReportData reviewReport(RequestMessage rm) {
        PrajReportData reportData = null;
        try {
            BasicDBObject alertData = new BasicDBObject();
            if (checkNullAndEmpty(rm.getAssetId()) && rm.getAssetId().longValue() > 0L) {
                BasicDBObject assetObj = new BasicDBObject();
                assetObj.put("$eq", rm.getAssetId());
                alertData.put("deviceid", assetObj);
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1);

            BasicDBObject dates = new BasicDBObject();
            dates.put("$eq", new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()));

            alertData.put(Constants.NODEDATE, dates);

            BasicDBObject func = new BasicDBObject();
            func.put("$gt", 0L);

            alertData.put(Constants.NODETOTAL,func);
            MongoConnection conn = MongoConnection.getInstance();
            try {
                String entity = "PRAJ_REPORT";

                conn.init();
                MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);

                info("PrajReportDaoImpl findAlerts 2.................... " + entity + ", " + alertData );
                Long funcCount = mongoCollection.countDocuments((Bson)alertData);
                info("reviewReport........... isFunc  ......... funcCount = "+funcCount );

                func = new BasicDBObject();
                func.put("$eq", 0L);
                alertData.put(Constants.NODETOTAL,func);

                info("PrajReportDaoImpl findAlerts 2.................... " + entity + ", " + alertData );
                Long nonFuncCount = mongoCollection.countDocuments((Bson)alertData);
                info("reviewReport........... isFunc  ......... funcCount = "+nonFuncCount );

                reportData.setFuncCount(funcCount);
                reportData.setNonFuncCount(nonFuncCount);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {}
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reportData;
    }

    public Long reviewReport(RequestMessage rm, List<Long> devList) {
        Long count = 0L;
        try {
            BasicDBObject alertData = new BasicDBObject();
            if(devList != null && devList.size() > 0){
                BasicDBObject assetObj = new BasicDBObject();
                assetObj.put("$in", devList);
                alertData.put("deviceid", assetObj);
            }
/*            if (checkNullAndEmpty(rm.getAssetId()) && rm.getAssetId().longValue() > 0L) {
                BasicDBObject assetObj = new BasicDBObject();
                assetObj.put("$eq", rm.getAssetId());
                alertData.put("deviceid", assetObj);
            }*/

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1);

            BasicDBObject dates = new BasicDBObject();
            dates.put("$eq", new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()));

            alertData.put(Constants.NODEDATE, dates);

            BasicDBObject func = new BasicDBObject();
            //func.put("$gt", 0L);

            alertData.put(Constants.NODETOTAL,func);
            MongoConnection conn = MongoConnection.getInstance();
            try {
                String entity = "PRAJ_REPORT";

                conn.init();
                MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);

/*                info("PrajReportDaoImpl findAlerts 2.................... " + entity + ", " + alertData );
                Long funcCount = mongoCollection.countDocuments((Bson)alertData);
                info("reviewReport........... isFunc  ......... funcCount = "+funcCount );*/

                func = new BasicDBObject();
                func.put("$eq", 0L);
                alertData.put(Constants.NODETOTAL,func);

                //info("PrajReportDaoImpl findAlerts 2.................... " + entity + ", " + alertData );
                count = mongoCollection.countDocuments((Bson)alertData);
                info("reviewReport........... isFunc  ......... count = "+count );
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {}
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    public Long reviewReport(RequestMessage rm, List<Long> devList, Long district) {
        Long count = 0L;
        try {
            BasicDBObject alertData = new BasicDBObject();
            if(devList != null && devList.size() > 0){
                BasicDBObject assetObj = new BasicDBObject();
                assetObj.put("$in", devList);
                alertData.put("deviceid", assetObj);
            }
            if(district != null && district > 0){
                BasicDBObject assetObj = new BasicDBObject();
                assetObj.put("$eq", district);
                alertData.put("district", district );
            }
/*            if (checkNullAndEmpty(rm.getAssetId()) && rm.getAssetId().longValue() > 0L) {
                BasicDBObject assetObj = new BasicDBObject();
                assetObj.put("$eq", rm.getAssetId());
                alertData.put("deviceid", assetObj);
            }*/

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1);

            BasicDBObject dates = new BasicDBObject();
            dates.put("$eq", new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()));

            alertData.put(Constants.NODEDATE, dates);

            BasicDBObject func = new BasicDBObject();
            //func.put("$gt", 0L);

            alertData.put(Constants.NODETOTAL,func);
            MongoConnection conn = MongoConnection.getInstance();
            try {
                String entity = "PRAJ_REPORT";

                conn.init();
                MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);

/*                info("PrajReportDaoImpl findAlerts 2.................... " + entity + ", " + alertData );
                Long funcCount = mongoCollection.countDocuments((Bson)alertData);
                info("reviewReport........... isFunc  ......... funcCount = "+funcCount );*/

                func = new BasicDBObject();
                func.put("$eq", 0L);
                alertData.put(Constants.NODETOTAL,func);

                info("PrajReportDaoImpl findAlerts 2.................... " + entity + ", " + alertData );
                count = mongoCollection.countDocuments((Bson)alertData);
                info("reviewReport........... isFunc  ......... count = "+count );
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {}
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

}
