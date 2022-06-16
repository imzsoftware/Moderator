package com.imz.praj.data.impl;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.imz.praj.data.PrajAlertLogDao;
import com.imz.praj.entity.PrajAlertLog;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.Device;
import com.traq.db.MongoConnection;
import org.bson.BsonNull;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.*;


public class PrajAlertLogDaoImpl extends BaseInitializer implements PrajAlertLogDao {
    private SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); private static final String entityName = "ALERTDATA";
    private SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int asc = 1;
    private static final int desc = -1;
    private static final Long timeDiff = 19800000L;     // GMT and IST Difference in milliseconds

    public void saveAlertLog(PrajAlertLog log) {
        Document document = createDocument(log);
        MongoConnection conn = MongoConnection.getInstance();
        try {
            conn.init();
            String entity = "ALERTDATA";
            String client = nullAndEmpty(log.getClient(), "TM");
            if (checkNullAndEmpty(client))
            { if (client.endsWith("_")) {
                entity = client.toUpperCase() + "ALERTDATA";
            } else {
                entity = client.toUpperCase() + "_" + "ALERTDATA";
            }  }
            else { entity = "ALERTDATA"; }


            MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);

            mongoCollection.insertOne(document);
        } catch (Exception exception) {

        } finally {}
    }

    public void saveNonFuncAlert(Device device, String devRedisData, Calendar cal) {
        Document document = createNonFunc(device, devRedisData,cal);
        MongoConnection conn = MongoConnection.getInstance();
        try {
            conn.init();
            String collectionName = "PRAJ_ALERTDATA";
            MongoCollection mongoCollection = conn.getDatastore().getCollection(collectionName);

            BasicDBObject alertObj = new BasicDBObject();
            alertObj.put(Constants.NODEACCOUNTID,document.getLong(Constants.NODEACCOUNTID));
            alertObj.put(Constants.NODEDEVICEID,document.getLong(Constants.NODEDEVICEID));
            alertObj.put(Constants.NODETYPE,document.getLong(Constants.NODETYPE));
            alertObj.put(Constants.NODEORIGINTSMILLI,document.getLong(Constants.NODEORIGINTSMILLI));
            FindIterable<Document> trackIterable = mongoCollection.find(alertObj);
            if(!document.isEmpty()) {
                if (trackIterable != null && !trackIterable.iterator().hasNext()) {
                    info("saveNonFuncAlert ... " + document);
                    mongoCollection.insertOne(document);
                }
            }

        } catch (Exception exception) {

        } finally {}
    }

    public void saveAlert(String log) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject)jsonParser.parse(log);
        Document doc = Document.parse(log);

        MongoConnection conn = MongoConnection.getInstance();
        try {
            conn.init();
            String entity = "ALERTDATA";
            String client = nullAndEmpty(jsonObject.get("client"), "TM");
            if (checkNullAndEmpty(client))
            { if (client.endsWith("_")) {
                entity = client.toUpperCase() + "ALERTDATA";
            } else {
                entity = client.toUpperCase() + "_" + "ALERTDATA";
            }  }
            else { entity = "ALERTDATA"; }

            MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);
            mongoCollection.insertOne(doc);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {}
    }


    public int updateOne(String id, String field, String value) {
        int success = 0;
        MongoConnection conn = MongoConnection.getInstance();
        try {
            conn.init();
            String entity = "ALERTDATA";

            String client = "TM";
            if (checkNullAndEmpty(client))
            { if (client.endsWith("_")) {
                entity = client + "ALERTDATA";
            } else {
                entity = client + "_" + "ALERTDATA";
            }  }
            else { entity = "ALERTDATA".toUpperCase(); }


            MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);


            UpdateResult ur = mongoCollection.updateOne((Bson)new BasicDBObject("_id", new ObjectId(id)), (Bson)new BasicDBObject("$set", new BasicDBObject(field, value)));
            if (ur.wasAcknowledged()) {
                info("updated");
            }
            success = 1;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.close();
        }

        return success;
    }

    public int updateMultipleFields(String id, Map<String, String> fieldvaluemap) {
        int success = 0;
        if (fieldvaluemap != null && fieldvaluemap.size() > 0) {
            MongoConnection conn = MongoConnection.getInstance();
            try {
                conn.init();
                MongoCollection mongoCollection = conn.getDatastore().getCollection("ALERTDATA");


                BasicDBObject dbl = new BasicDBObject();
                for (Map.Entry<String, String> mapp : fieldvaluemap.entrySet()) {
                    dbl.append(((String)mapp.getKey()).trim(), ((String)mapp.getValue()).trim());
                }

                BasicDBObject bdo = new BasicDBObject("$set", dbl);
                mongoCollection.countDocuments();
                UpdateResult ur = mongoCollection.updateMany((Bson)new BasicDBObject("_id", new ObjectId(id)), (Bson)bdo);
                if (ur.wasAcknowledged()) {
                    info("updated");
                }

                success = (int)ur.getModifiedCount();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.close();
            }
        } else {
            return -1;
        }

        return success;
    }

    public List<PrajAlertLog> findAlerts(RequestMessage rm, List<Account> accountList, boolean ascsort) {
        List<PrajAlertLog> logList = null;
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
            }

            BasicDBObject codeObj = null;
            if (checkNullAndEmpty(rm.getType()) && (rm
                    .getType().equalsIgnoreCase("S") || rm.getType().equalsIgnoreCase("E"))) {

                List<DBObject> criteria = new ArrayList<>();
                codeObj = new BasicDBObject();
                if (rm.getType().equalsIgnoreCase("E")) {
                    criteria.add(new BasicDBObject("sms", new BsonNull()));
                    criteria.add(new BasicDBObject("email", new BasicDBObject("$ne", new BsonNull())));
                } else {
                    criteria.add(new BasicDBObject("email", new BsonNull()));
                    criteria.add(new BasicDBObject("sms", new BasicDBObject("$ne", new BsonNull())));
                }
                codeObj.put("$or", criteria);
            }

            debug("PrajAlertLogDaoImpl Line 236.................... " + ", " + alertData);
            if (checkNullAndEmpty(rm.getStartDate()) && checkNullAndEmpty(rm.getEndDate())) {
                BasicDBObject dates = new BasicDBObject();


                SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDate = dbPattern.parse(rm.getStartDate());
                Date endDate = dbPattern.parse(rm.getEndDate());

                info("PrajAlertLogDaoImpl .... startDate.getTime() "+startDate.getTime());
                // Convert to IST
                dates.put("$gte", startDate.getTime() + timeDiff);
                dates.put("$lte", endDate.getTime() + timeDiff);

                alertData.put(Constants.NODEORIGINTSMILLI, dates);
                info("PrajAlertLogDaoImpl .... alertData.get(Constants.NODEORIGINTSMILLI) = "+alertData.get(Constants.NODEORIGINTSMILLI));

                MongoConnection conn = MongoConnection.getInstance();
                try {
                    String entity = "";
                    String client = rm.getVendorcode();
                    if (checkNullAndEmpty(client)){
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

                    if (checkNullAndEmpty(rm.getType()) && (rm.getType().equalsIgnoreCase("S")
                            || rm.getType().equalsIgnoreCase("E"))) {
                        info("findAlerts 1.................... " + entity + ", " + alertData + ", " + sort);

                        trackIterable = mongoCollection.find(Filters.and(new Bson[] { (Bson)alertData, (Bson)codeObj })).sort((Bson)new BasicDBObject(Constants.NODEORIGINTSMILLI, Integer.valueOf(sort)));
                    } else {
                    //    info("findAlerts 2....................Collection Name " + entity + ", " + alertData + ", " + sort);

                        trackIterable = mongoCollection.find((Bson)alertData).sort((Bson)new BasicDBObject(Constants.NODEORIGINTSMILLI, sort));
                    }

                    logList = PrajAlertLog.createList(trackIterable);
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

    public Document createDocument(PrajAlertLog log) {
        Document doc = new Document();
        try {
            Double lat = Double.valueOf(0.0D);
            try {
                lat = log.getLatitude();
            } catch (Exception e) {
                lat = Double.valueOf(0.0D);
            }
            Double lng = Double.valueOf(0.0D);
            try {
                lng = log.getLongitude();
            } catch (Exception e) {
                lat = Double.valueOf(0.0D);
            }

            Double[] geo = { lng, lat };
            doc.append("geocode", Arrays.asList(geo));
            doc.append("alertid", nullAndEmpty(log.getAlertid(), "0"));
            doc.append("accid", log.getAccId());
            doc.append("deviceid", log.getAssetId());
            doc.append("vehnum", nullAndEmpty(log.getVehicleNumber()));
            doc.append("type", nullAndEmpty(log.getType()));
            doc.append("code", nullAndEmpty(log.getCode()));
            doc.append("destination", nullAndEmpty(log.getDestination()));
            doc.append("lat", lat);
            doc.append("lng", lng);
            doc.append("address", nullAndEmpty(log.getAddress()));
            doc.append("message", nullAndEmpty(log.getMessage()));
            doc.append("status", nullAndEmpty(log.getStatusCode(), "A"));
            doc.append("cts", nullAndEmpty(log.getCreatedOn(), this.dbPattern.format(new Date())));
            doc.append("client", nullAndEmpty(log.getClient(), "TM"));
            doc.append("misc", nullAndEmpty(log.getMisc(), "NA"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public Document createNonFunc(Device device, String devRedisData,  Calendar cal) {
        Document doc = new Document();
        try {
            doc.append("accid", device.getAccId());
            doc.append("deviceid", device.getId());
            doc.append("vehnum", nullAndEmpty(device.getLicenseno(),"NA"));
            if(checkNullAndEmpty(devRedisData)) {
                Long orgTs = Long.parseLong(TagValues.getNodeValue(devRedisData,Constants.NODEORIGINTSMILLI));
                if (orgTs > cal.getTimeInMillis()){
                    doc.append("type", "NFI");
                    doc.append("code", "NFI");
                    doc.append(Constants.NODEORIGINTSMILLI, orgTs);
                    doc.append(Constants.NODEMESSAGE, "IOT Device non-functional from 24 hrs.");
                }else{
                    doc.append("type", "NFS");
                    doc.append("code", "NFS");
                    doc.append(Constants.NODEORIGINTSMILLI, cal.getTimeInMillis());
                    doc.append(Constants.NODEMESSAGE, "Scheme non-functional from more than 24 hrs.");
                }
                doc.append("lat", Double.parseDouble(TagValues.getNodeValue(devRedisData,Constants.NODELATITUDE)));
                doc.append("lng", Double.parseDouble(TagValues.getNodeValue(devRedisData,Constants.NODELONGITUDE)));
                doc.append("address", nullAndEmpty(TagValues.getNodeValue(devRedisData,Constants.NODEADDRESS), "NA"));
                doc.append(Constants.NODECREATEDON, nullAndEmpty(null, dbPattern.format(cal.getTime())));
                String oprTime = TagValues.getNodeValue(devRedisData,Constants.NODEIGNTIME);
                if (checkNullAndEmpty(oprTime)){
                    doc.append(Constants.NODEVALUE, dbPattern.format(new Date(Long.parseLong(oprTime))));
                }else{
                    doc.append(Constants.NODEVALUE, "NA");
                }
            }else{
                doc.append("type", "NFS");
                doc.append("code", "NFS");
                doc.append("lat", 0.0D);
                doc.append("lng", 0.0D);
                doc.append("address", "NA");
                doc.append("cts", this.dbPattern.format(cal.getTime()));
                doc.append(Constants.NODEORIGINTSMILLI, cal.getTimeInMillis());
                doc.append(Constants.NODEMESSAGE, "Scheme non-functional from more than 24 hrs.");
                doc.append(Constants.NODEVALUE, "NA");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public Document createDocument(String xmlData) {
        Document doc = new Document();
        try {
            Double lat = Double.valueOf(0.0D);
            try {
                lat = Double.valueOf(Double.parseDouble(TagValues.getNodeValue(xmlData, "lat")));
            } catch (Exception e) {
                lat = Double.valueOf(0.0D);
            }
            Double lng = Double.valueOf(0.0D);
            try {
                lng = Double.valueOf(Double.parseDouble(TagValues.getNodeValue(xmlData, "lng")));
            } catch (Exception e) {
                lng = Double.valueOf(0.0D);
            }

            Double[] geo = { lng, lat };
            doc.append("geocode", Arrays.asList(geo));
            doc.append("alertid",
                    Long.valueOf(Long.parseLong(nullAndEmpty(TagValues.getNodeValue(xmlData, "alertid"), "0"))));
            doc.append("accid",
                    Long.valueOf(Long.parseLong(nullAndEmpty(TagValues.getNodeValue(xmlData, "accid"), "0"))));
            doc.append("deviceid",
                    Long.valueOf(Long.parseLong(nullAndEmpty(TagValues.getNodeValue(xmlData, "deviceid"), "0"))));
            doc.append("vehnum",
                    nullAndEmpty(TagValues.getNodeValue(xmlData, "vehnum"), "NA"));
            doc.append("type", nullAndEmpty(TagValues.getNodeValue(xmlData, "type"), "NA"));
            doc.append("code", nullAndEmpty(TagValues.getNodeValue(xmlData, "code"), "NA"));
            doc.append("value", nullAndEmpty(TagValues.getNodeValue(xmlData, "val"), "NA"));
            doc.append("destination", nullAndEmpty(TagValues.getNodeValue(xmlData, "destination"), "NA"));
            doc.append("lat", lat);
            doc.append("lng", lng);
            doc.append("address", nullAndEmpty(TagValues.getNodeValue(xmlData, "address"), "NA"));
            doc.append("message", nullAndEmpty(TagValues.getNodeValue(xmlData, "message"), "NA"));
            doc.append("failure", nullAndEmpty(TagValues.getNodeValue(xmlData, "failure"), "NA"));
            doc.append("vendorresult", nullAndEmpty(TagValues.getNodeValue(xmlData, "vendorresult"), "NA"));
            doc.append("status", nullAndEmpty(TagValues.getNodeValue(xmlData, "status"), "A"));
            doc.append("cts", nullAndEmpty(TagValues.getNodeValue(xmlData, "cts"), this.dbPattern.format(new Date())));
            doc.append("deliveredts", nullAndEmpty(TagValues.getNodeValue(xmlData, "deliveredts"), "0000-00-00 00:00:00"));
            doc.append("client", nullAndEmpty(TagValues.getNodeValue(xmlData, "client"), "TM"));
            doc.append("misc", nullAndEmpty(TagValues.getNodeValue(xmlData, "misc"), "NA"));
            doc.append("count", Integer.valueOf(Integer.parseInt(nullAndEmpty(TagValues.getNodeValue(xmlData, "count"), "0"))));
            doc.append("comments", "NA");
            doc.append("readstatus", "U");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }
}

