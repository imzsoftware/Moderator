package com.traq.manipulator;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.Constants;
import com.traq.common.data.entity.RespData;
import com.traq.common.data.entity.TrackData;
import com.traq.common.handler.RespDataHandler;
import com.traq.common.handler.TrackDataHandler;
import com.traq.manipulator.bots.DataInsertion;
import org.bson.Document;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsumerTrackTest {

    static private String entityName = "TRACKDATA";
    static MongoDatabase database = null;

    public static void main(String[] args) {
        ConsumerTrackTest test = new ConsumerTrackTest();

        test.fetchTrackData();
    }

    public void fetchTrackData() {
        RequestMessage rm = new RequestMessage();

        rm.setClient("BSFC");

        rm.setStartDate("2020-07-06 00:00:01");
        rm.setEndDate("2020-07-10 1:10:10");

        List<String> imeiList = new ArrayList<>();
//        imeiList.add("36871408");

        List<Long> assetIds = new ArrayList<>();
        assetIds.add(18945L);

        List<TrackData> dataList = this.findDeviceTrack(rm.getStartDate(), rm.getEndDate(),
                rm.getClient(), rm.getAccId(), assetIds, imeiList);

        Collections.sort(dataList, TrackData.timeComparator);

        JSONArray jsonArr = TrackData.toJsonArr(dataList);
        System.out.println(jsonArr.toString());

    }

    public List<TrackData> findDeviceTrack(String startDate, String endDate,
                                           String client, Long assetId, List<Long> assetids,
                                           List<String> imeiList) {

        BasicDBObject trackData = new BasicDBObject();

        // Date between start & end date
        if (null != startDate && !startDate.trim().isEmpty()
                && null != endDate && !endDate.trim().isEmpty()) {
            BasicDBObject dates = new BasicDBObject();
            dates.put("$gte", startDate);
            dates.put("$lte", endDate);
            trackData.put(Constants.NODEORIGINTS, dates);
        }

        // Device Validation
        if (checkNullAndEmpty(assetId) && assetId > 0) {
            BasicDBObject assetObj = new BasicDBObject();
            assetObj.put("$eq", assetId);       // Types not in the List
            trackData.put(Constants.NODEDEVICEID, assetObj);
        } else if (assetids != null && assetids.size() > 0) {
            BasicDBObject assetObj = new BasicDBObject();
            assetObj.put("$in", assetids);       // Types not in the List
            trackData.put(Constants.NODEDEVICEID, assetObj);
        }

        // IMEI Validation
        BasicDBObject imeiObj = new BasicDBObject();
        if (imeiList != null && imeiList.size() > 0) {
            imeiObj.put("$in", imeiList);       // Types not in the List
            trackData.put(Constants.NODEIMEI, imeiObj);
        }

        String entity = "";

        if (checkNullAndEmpty(client)) {
            if (client.endsWith("_"))
                entity = client + entityName;
            else
                entity = client + "_" + entityName;
        } else {
            entity = entityName.toUpperCase();
        }
        System.out.println("entity name" + entity + " TrackData :: " + trackData);

        List<TrackData> trackList = null;
//        MongoConnection conn = MongoConnection.getInstance();
        try {
//            conn.init();
//            MongoCollection mongoCollection = conn.getDatastore().getCollection(entityName);
            MongoCollection mongoCollection = mongoDbConnection().getDatabase("iot_traq")
                    .getCollection(entity);

            // Sort in ascending order
            FindIterable<Document> trackIterable = mongoCollection.find(trackData)
                    .sort(new BasicDBObject(Constants.NODEORIGINTS, 1));

            TrackDataHandler tdh = new TrackDataHandler();

            trackList = tdh.createList(trackIterable);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //conn.close();
        }

        return trackList;
    }

    public static MongoClient mongoDbConnection() {

        String DB_SRV_USR = "traqmatix";  //"imzcorpadmin"; //getAppConfig().getMongoUser();
        String DB_SRV_PWD = "tr@qm@t1X"; //"1m2Adm!n!@#$"; //getAppConfig().getMongoPassword();

        int DB_PORT = 27017;

        //connectionPoolListener.
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        //builder.minConnectionsPerHost(2);
        //builder.connectionsPerHost(20);

        //build the connection options
        builder.minConnectionsPerHost(3);//set the max wait time in (ms)
        builder.maxConnectionIdleTime(60000);//set the max wait time in (ms)
        //builder.socketTimeout(120000);//set the max wait time in (ms)
        MongoClientOptions opts = builder.build();

        MongoCredential credential = null;
//        credential = MongoCredential.createCredential(DB_SRV_USR, "imz_iot_devices",
//                DB_SRV_PWD.toCharArray());

        credential = MongoCredential.createCredential(DB_SRV_USR, "iot_traq",
                DB_SRV_PWD.toCharArray());

        List<ServerAddress> seeds = new ArrayList<ServerAddress>();
        String[] hostArr = "127.0.0.1".split(",");
        for (int i = 0; i < hostArr.length; i++) {
            seeds.add(new ServerAddress(hostArr[i]));        // Primary
        }

        MongoClient mongo = new MongoClient(seeds, credential, opts);
//        setMongoDBClient(mongo);

        System.out.println("Connected to the Mongo successfully");
        // Accessing the admin
        //setMongoDatabase(mongo.getDatabase(getAppConfig().getMongoDB()));
        mongo.setWriteConcern(WriteConcern.ACKNOWLEDGED);

        return mongo;
    }

    static boolean checkNullAndEmpty(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return true;
    }

    static boolean checkNullAndEmpty(Long str) {
        if (str == null || str.compareTo(0L) == 0) {
            return false;
        }
        return true;
    }
}
