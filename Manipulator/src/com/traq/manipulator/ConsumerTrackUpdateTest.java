package com.traq.manipulator;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.base.Constants;
import com.traq.common.data.entity.TrackData;
import com.traq.common.handler.TrackDataHandler;
import com.traq.db.MongoConnection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class ConsumerTrackUpdateTest {

    static private String entityRespName = "TRACKDATA";
    static MongoDatabase database = null;

    public static void main(String[] args) {
        ConsumerTrackUpdateTest test = new ConsumerTrackUpdateTest();

        test.fetchTrackData();
    }

    public void fetchTrackData() {
        RequestMessage rm = new RequestMessage();

        rm.setClient("BSFC_");

        rm.setStartDate("2020-08-01 03:01:01");
        rm.setEndDate("2020-08-01 04:00:00");

//        rm.setAccId(2L);
//        rm.setDevTypeId(14L);
        rm.setAssetId(23L);


        List<TrackData> trackDataList = this.findTrack(rm);
        for (TrackData trackData : trackDataList) {
//            if (trackData.getLatitude().compareTo(35.00) > 0) {
            System.out.println("TrackData ................... Latitude :" + trackData.getLatitude() + ", Switch Lattitude and Longitude");
            this.updateLatnLngByObjectId(trackData.getDocId(), rm.getClient(), trackData.getLatitude(),
                    trackData.getLongitude());
            break;
//            }
        }

    }

    public List<TrackData> findTrack(RequestMessage rm) {

        BasicDBObject trackData = new BasicDBObject();

        // Date between start & end date
        BasicDBObject dates = new BasicDBObject();
        dates.put("$gte", rm.getStartDate());
        dates.put("$lte", rm.getEndDate());

        trackData.put(Constants.NODEORIGINTS, dates);

        // Device Validation
        if (checkNullAndEmpty(rm.getAssetId()) && rm.getAssetId() > 0) {
            BasicDBObject assetObj = new BasicDBObject();
            assetObj.put("$eq", rm.getAssetId());       // Types not in the List
            trackData.put(Constants.NODEDEVICEID, assetObj);
        }

//        //Speed Validation
//        if (checkNullAndEmpty(rm.getSpeed()) && rm.getSpeed()>0) {
//            BasicDBObject speedObj = new BasicDBObject();
//            speedObj.put("$eq",rm.getSpeed());
//            trackData.put(Constants.NODESPEED ,speedObj);
//        }
//
        if (rm.getAssetids() != null && rm.getAssetids().size() > 0) {
            BasicDBObject assetObj = new BasicDBObject();
            /*List<String> devList = new ArrayList<String>();
            for(int i=0; i<rm.getAssetids().size(); i++){
                devList.add(rm.getAssetids().get(i).toString());
            }*/
            assetObj.put("$in", rm.getAssetids());       // Types not in the List
            trackData.put(Constants.NODEDEVICEID, assetObj);
        }

        List<TrackData> trackList = null;

        String entity = "";
        if (checkNullAndEmpty(rm.getClient())) {
            entity = rm.getClient() + entityRespName;
        } else {
            entity = entityRespName;
        }
        System.out.println("entity name" + entity + " TrackData :: " + trackData);

//        MongoConnection conn = MongoConnection.getInstance();
        try {
//            conn.init();
            MongoCollection mongoCollection = mongoDbConnection().getDatabase("iot_traq").getCollection(entity);

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

    public int updateLatnLngByObjectId(ObjectId id,
                                       String client, Double lat,
                                       Double lng) {
        int success = 0;
        MongoConnection conn = MongoConnection.getInstance();
        try {
            conn.init();
            String entity = "";

            if (checkNullAndEmpty(client)) {
                if (client.endsWith("_"))
                    entity = client + entityRespName;
                else
                    entity = client + "_" + entityRespName;
            } else {
                entity = entityRespName.toLowerCase();
            }
            System.out.println("entity name" + entity);

            MongoCollection mongoCollection = mongoDbConnection().getDatabase("iot_traq")
                    .getCollection(entity);

            // Sort in ascending order
//            UpdateResult ur = mongoCollection.updateOne(new BasicDBObject("_id", new ObjectId(id)),
//                    new BasicDBObject("$set", new BasicDBObject("address", address)));

            // findOneAndUpdate
            Bson filter = eq("_id", id);
            Bson updateLat = set("lat", lat);
            Bson updateLng = set("lng", lng);
            Bson updategeolng = set("geocode.0", lng);
            Bson updategeolat = set("geocode.1", lat);
            Bson updateall = combine(updateLat, updateLng, updategeolat, updategeolng);

//            Bson update1 = inc("x", 10); // increment x by 10. As x doesn't exist yet, x=10.
//            Bson update2 = rename("class_id", "new_class_id"); // rename variable "class_id" in "new_class_id".
//            Bson update3 = mul("scores.0.score", 2); // multiply the first score in the array by 2.
//            Bson update4 = addToSet("comments", "This comment is uniq"); // creating an array with a comment.
//            Bson update5 = addToSet("comments", "This comment is uniq"); // using addToSet so no effect.
//            Bson updates = combine(update1, update2, update3, update4, update5);

            UpdateResult ur = mongoCollection.updateMany(filter, updateall);

            if (ur.wasAcknowledged()) {
                System.out.println("updated");
            }
            success = 1;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //conn.close();
        }

        return success;
    }


    public static MongoClient mongoDbConnection() {

        String DB_SRV_USR = "traqmatix"; //getAppConfig().getMongoUser();
        String DB_SRV_PWD = "tr@qm@t1X"; //getAppConfig().getMongoPassword();

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
