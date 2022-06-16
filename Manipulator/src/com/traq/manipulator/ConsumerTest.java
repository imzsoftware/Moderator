package com.traq.manipulator;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.Constants;
import com.traq.common.data.entity.RespData;
import com.traq.common.handler.RespDataHandler;
import com.traq.manipulator.bots.DataInsertion;
import org.bson.Document;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsumerTest {

    static private String entityRespName = "RESPDATA";
    static MongoDatabase database = null;

    public static void main(String[] args) {
        ConsumerTest test = new ConsumerTest();

//        test.insertRespData();

        test.fetchRespData();
    }

    public void insertRespData() {
        DataInsertion dataInsertion = new DataInsertion();
        String data = "<traq><raw><data>7E03110005095039599946000C000B014100B67E</data>" +
                "</raw><devicedata><cts>2020-06-24 14:18:55</cts><imei>95039599946</imei><vehnum>95039599946</vehnum><deviceid>7561</deviceid><accid>1</accid><name>Test-1</name><devicetypeid>55</devicetypeid><client>NEET</client></devicedata><resp><records><record><type>41</type><resultcode>0</resultcode><resultdesc>Success</resultdesc></record></records></resp></traq>";

        data = "<traq><raw><data>292984006E24D78E0849503A332E372E3233302E3132312C3830" +
                "31332C312C6D73642C2C2C49443A33363837313430382C4D3A362C4750533A412C382C4C42533A312C4353513A33312C4347523A312C4143433A302C4241543A3539252C5645523A415431302D563" +
                "32E38285047505329D70D</data></raw><devicedata><cts>2020-06-25 15:41:20</cts><imei>36871408</imei><vehnum>36871408</vehnum><deviceid>7564</deviceid><accid>1</" +
                "accid><name>Test-AT10</name><devicetypeid>63</devicetypeid><client>NEET</client></devicedata><resp><responsetype>84</responsetype><records><record><type>NA</" +
                "type><value>IP:3.7.230.121,8013,1,msd,,,ID:36871408,M:6,GPS:A,8,LBS:1,CSQ:31,CGR:1,ACC:0,BAT:59%,VER:AT10-V3.8(PGPS)</value><resultcode>0</resultcode><result" +
                "desc>Success</resultdesc></record></records></resp></traq>";

        if (database == null) {
//            info("getAppConfig().getMongoDB()....." + getAppConfig().getMongoDB());
//            database = getMongoDBClient().getDatabase(getAppConfig().getMongoDB());
            try {
                database = mongoDbConnection().getDatabase("imz_iot_devices");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(data);
        String liveData = TagValues.getNodeValue(data, Constants.NODELIVE);
        String rawData = TagValues.getNodeValue(data, Constants.NODERAW);
        String lockUnlockData = TagValues.getNodeValue(data, Constants.NODEELOCK);

        String historyData = TagValues.getNodeValue(data, Constants.NODEHIST);
        String deviceTypeId = TagValues.getNodeValue(data, Constants.NODEDEVICETYPEID);
        String deviceDetails = TagValues.getNodeValue(data, Constants.NODEDEVICEDATA);
        String respData = TagValues.getNodeValue(data, Constants.NODERESP);

        try {
            if (!rawData.isEmpty() && !TagValues.getNodeValue(rawData, Constants.NODEDATA).isEmpty()) {
                dataInsertion.insertRawData(database, "NEET", rawData, deviceDetails);
                //consumer.commitAsync();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            if (!respData.isEmpty() && !TagValues.getNodeValue(respData, Constants.NODERECORDS).isEmpty()) {
                System.out.println("Resp data ....." + respData);
                String respRecords = TagValues.getNodeValue(respData, Constants.NODERECORDS);
                List<String> respRecordList = TagValues.getAllNodeValue(respRecords, Constants.NODERECORD);
                if (null != respRecordList && respRecordList.size() > 0) {
                    dataInsertion.insertRespData(database, "NEET", respData, respRecordList, deviceDetails);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

        }

    }

    public void fetchRespData() {
        RequestMessage rm = new RequestMessage();

        rm.setClient("NEET");

        rm.setStartDate("2020-06-12 01:01:01");
        rm.setEndDate("2020-06-30 23:59:59");

        rm.setAccId(1L);
        rm.setDevTypeId(63L);

        List<String> imeiList = new ArrayList<>();
        imeiList.add("36871408");

        List<String> commandCodes = new ArrayList<>();
//        commandCodes.add("AUL");
//        commandCodes.add("SCH");

        List<RespData> respDataList = this.findResp(rm.getStartDate(), rm.getEndDate(),
                rm.getClient(), rm.getAccId(), rm.getDevTypeId(),
                imeiList, commandCodes);

        Collections.sort(respDataList, RespData.timeComparator);

        JSONArray respDataArray = RespData.toJsonArr(respDataList);
        System.out.println(respDataArray.toString());

    }

    public List<RespData> findResp(String startDate, String endDate,
                                   String client, Long accId,
                                   Long devTypeId, List<String> imeiList,
                                   List<String> commandCodes) {

        BasicDBObject respData = new BasicDBObject();

        // Date between start & end date
        if (null != startDate && !startDate.trim().isEmpty()
                && null != endDate && !endDate.trim().isEmpty()) {
            BasicDBObject dates = new BasicDBObject();
            dates.put("$gte", startDate);
            dates.put("$lte", endDate);
            respData.put(Constants.NODECTS, dates);
        }

        // IMEI Validation
        BasicDBObject imeiObj = new BasicDBObject();
        if (imeiList != null && imeiList.size() > 0) {
            imeiObj.put("$in", imeiList);       // Types not in the List
            respData.put(Constants.NODEIMEI, imeiObj);
        }

        // Account Validation
        if (checkNullAndEmpty(accId) && accId > 0) {
            BasicDBObject accObject = new BasicDBObject();
            accObject.put("$eq", accId);       // AccountType
            respData.put(Constants.NODEACCOUNTID, accObject);
        }

        // DeviceType Validation
        if (checkNullAndEmpty(devTypeId) && devTypeId > 0) {
            BasicDBObject devTypeObject = new BasicDBObject();
            devTypeObject.put("$eq", devTypeId);       // DeviceType
            respData.put(Constants.NODEDEVICETYPEID, devTypeObject);
        }

        String entity = "";

        if (checkNullAndEmpty(client)) {
            if (client.endsWith("_"))
                entity = client + entityRespName;
            else
                entity = client + "_" + entityRespName;
        } else {
            entity = entityRespName.toLowerCase();
        }
        System.out.println("entity name" + entity + " RespData :: " + respData);
        List<RespData> respDataList = null;
//        MongoConnection conn = MongoConnection.getInstance();
        try {
//            conn.init();
            MongoCollection mongoCollection = mongoDbConnection().getDatabase("imz_iot_devices").getCollection(entity);

            // Sort in ascending order
            FindIterable<Document> trackIterable = mongoCollection.find(respData)
                    .sort(new BasicDBObject(Constants.NODECTS, 1));

            RespDataHandler tdh = new RespDataHandler();

            respDataList = tdh.createList(trackIterable, commandCodes);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //conn.close();
        }

        return respDataList;
    }


    public static MongoClient mongoDbConnection() {

        String DB_SRV_USR = "imzcorpadmin"; //getAppConfig().getMongoUser();
        String DB_SRV_PWD = "1m2Adm!n!@#$"; //getAppConfig().getMongoPassword();

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
        credential = MongoCredential.createCredential(DB_SRV_USR, "imz_iot_devices",
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
