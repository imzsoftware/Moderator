package com.traq.manipulator.retry;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.TrackData;
import com.traq.common.data.entity.User;
import com.traq.common.exceptions.ResultCodeExceptionInterface;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.ReportHandler;
import com.traq.common.handler.TrackDataHandler;
import com.traq.common.xmlutils.XMLProcessor;
import com.traq.db.MongoConnection;
import com.traq.manipulator.bots.DataInsertion;
import com.traq.util.RequestBean;
import org.json.JSONException;
import org.json.JSONObject;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class TrackDataDump  {

    private ResponseMessage responseMessage;
    private RequestMessage requestMessage;
    private static MongoClient mongoClient;
    private static String dbName = "iot_traq";
    private static String dbHost = "traqmatixdbsrv1";
    private static String mdbHost = "192.168.100.21";
    private static String client = "BSFC";
    final static private String entityName = "TRACKDATA";
    private String request;
    List<com.traq.common.data.entity.TrackData> dataList =  null;
    SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    RequestBean rb;
    User user = null;

    public ResponseMessage getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public TrackDataDump(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public TrackDataDump() {
    }


    public String executeJSON(String request, String devType, String client){

        ReportHandler dh = new ReportHandler();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        String response = "";

        try{
            responseMessage = new ResponseMessage();
            JSONObject object = new JSONObject(request);
            requestMessage = dh.getRequest(object.getJSONObject(Constants.NODEREQUEST), TagValues.getNodeValue(object, Constants.NODEREQUESTTYPE));

            Date date = newPattern.parse(requestMessage.getStartDate());
            requestMessage.setStartDate(dbPattern.format(date));
            date = newPattern.parse(requestMessage.getEndDate());
            requestMessage.setEndDate(dbPattern.format(date));
            requestMessage.setClient(client);

            boolean isValid = true;
            if(isValid){
                if(requestMessage.getAccId() != null) {
                    responseMessage.setMessagetype(requestMessage.getMessagetype());
                    responseMessage.setResponsects(sdf.format(new Date()));
                    //info("Track History Report....Client="+account.getClient());
                    if(client != null && !client.isEmpty() ){
                        requestMessage.setClient(client+"_");
                    }
                    else{
                        requestMessage.setClient("TM_");
                    }

                    dataList = findTrack(requestMessage, null);

                    if(dataList!= null && dataList.size()>0) {
                        responseMessage.setResultcode(ResultCodeExceptionInterface._SUCCESS);
                        responseMessage.setResultDescription(ResultCodeDescription._DES_SUCCESS);
                    }else{
                        responseMessage.setResultcode(ResultCodeExceptionInterface._NOT_FOUND);
                        responseMessage.setResultDescription(ResultCodeDescription._DES_NOT_FOUND);
                    }
                }
                else{
                    responseMessage.setResultcode(ResultCodeExceptionInterface._USER_NOT_FOUND);
                    responseMessage.setResultDescription(ResultCodeDescription._DES_USER_NOT_FOUND);
                }
            }else{
                responseMessage.setResultcode(ResultCodeExceptionInterface._USER_NOT_FOUND);
                responseMessage.setResultDescription(ResultCodeDescription._DES_USER_NOT_FOUND);
            }

        }catch(Exception ex){
            responseMessage.setResultcode(ResultCodeExceptionInterface._TECHNICAL_FAILURE);
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(ResultCodeExceptionInterface._TECHNICAL_FAILURE));
            ex.printStackTrace();
        }finally{
            if(responseMessage.getResultcode() == ResultCodeExceptionInterface._SUCCESS){
                for(TrackData data : dataList) {
                    response = GenerateXMLResponse(data,client,devType);
                    try{
                        DataInsertion dataInsertion = new DataInsertion();
                        dataInsertion.insertTrackData(mongoClient.getDatabase(dbName), "BSFC", response);
                    }catch (Exception ee){
                        ee.printStackTrace();
                    }
                }
            }else{
                response = generateFailureResponse(responseMessage);
            }
        }
        return response;
    }


    public String generateFailureResponse(ResponseMessage rm){
        JSONObject mainObj = new JSONObject();
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.NODERESPONSETYPE, rm.getMessagetype());
            jsonObject.put(Constants.NODERESULTCODE, rm.getResultcode());
            jsonObject.put(Constants.NODERESULTDESCRIPTION, rm.getResultDescription());

            mainObj.put(Constants.NODERESPONSE, jsonObject);
        }catch (JSONException je){

        }
        return mainObj.toString();
    }

    public static void main(String [] args){

        TrackDataDump td = new TrackDataDump();
        List <Device> deviceList = td.deviceList();
        System.out.println("deviceList...." + deviceList.size());

        mongoClient = td.mongoDbConnection();
        for(Device device : deviceList){
            StringBuffer req = new StringBuffer("{\"requesttype\":\"TRACKDATAREPORT\",\"vendorcode\":\"BSFC\",\"request\":{\"username\":\"msdadmin\",\"pin\":\"e3857aac377384131c73da439311b26a\",\"transid\":\"\",\"ipaddress\":\"203.92.44.162\",\"type\":\"\",\"clienttype\":\"web\",\"accid\":\"")
                    .append(device.getAccId()).append("\",\"assetid\":\"").append(device.getId())
                    .append("\",\"startdate\":\"26-09-2019 09:35:00\",\"enddate\":\"26-09-2019 16:26:00\"}}");

/*
            req = new StringBuffer("{\"requesttype\":\"TRACKDATAREPORT\",\"vendorcode\":\"TM\",\"request\":{\"username\":\"msdadmin\",\"pin\":\"e3857aac377384131c73da439311b26a\",\"transid\":\"\",\"ipaddress\":\"203.92.44.162\",\"type\":\"\",\"clienttype\":\"web\",\"accid\":\"")
                    .append("43").append("\",\"assetid\":\"").append("47")
                    .append("\",\"startdate\":\"25-09-2019 09:35:00\",\"enddate\":\"26-09-2019 23:26:00\"}}");
*/

            td.executeJSON(req.toString(),device.getUdv1(),client);

        }


    }

    public String GenerateXMLResponse(TrackData data, String client, String devType) {

        String xmlResponse = new XMLProcessor().textXMLMessage(this.getResponseApi());
        HashMap responseMap = new HashMap();
        XMLProcessor xp = new XMLProcessor(xmlResponse);

        responseMap.put(Constants.NODELATITUDE, data.getLatitude().toString());
        responseMap.put(Constants.NODELONGITUDE, data.getLongitude().toString());
        responseMap.put(Constants.NODEDISHA, data.getCourse());
        responseMap.put(Constants.NODESPEED, data.getSpeed().toString());
        responseMap.put(Constants.NODEIGNITION, data.getIgnition());
        responseMap.put(Constants.NODEFUEL, data.getFuel().toString());
        responseMap.put(Constants.NODELOADS, data.getLoads().toString());
        responseMap.put(Constants.NODEAC, data.getAc());
        responseMap.put(Constants.NODEDOOR, data.getDoor());
        responseMap.put(Constants.NODEGPS, data.getGps());
        responseMap.put(Constants.NODESATELLITES, data.getNos().toString());
        responseMap.put(Constants.NODEORIGINTS, data.getOrgTs());

        responseMap.put(Constants.NODECREATEDON, data.getInitTs());
        responseMap.put(Constants.NODEADDRESS, data.getLocation());
        //responseMap.put(Constants.NODEDEVICETYPEID, data.get);
        responseMap.put(Constants.NODEDEVICEID, data.getDeviceId());

        responseMap.put(Constants.NODEACCOUNTID, data.getAccId().toString());
        responseMap.put(Constants.NODEIMMOBILISER, data.getImb());
        responseMap.put(Constants.NODEVEHICLENUMBER, data.getLicenseNumber());
        responseMap.put(Constants.NODEIMEI, data.getIMEI());

        responseMap.put(Constants.NODEPOWSTATUS, data.getPowStatus());
        responseMap.put(Constants.NODECLIENT, client);

        String newResponse = xp.insertInXML(responseMap);

        return newResponse;

    }

    public TreeMap getResponseApi() {

        TreeMap<String, String[]> xmlKeys = new TreeMap<String, String[]>();
        String[] headerElement = {Constants.NODERESPONSETYPE};

        xmlKeys.put(Constants.ELEMENTHEADER, headerElement);

/*        "<live><lat>25.995375</lat><lng>86.792750</lng><disha>0</disha><speed>0</speed><ign>N</ign><distance>530.4669</distance>" +
                "<fuel>4.75800000000000000711e+00</fuel><load>4.75800000000000000711e+00</load><temp></temp><ac>N</ac><door>N</door><reserved></reserved><gps>A</gps><satellites>9</satellites>" +
                "<orgts>2019-10-30 10:06:10</orgts><orgmillis>63739649170</orgmillis><cts>2019-10-30 10:06:15</cts><address>SH66, Singheshwar, Bihar 852128, India</address>" +
                "<devicetypeid>15</devicetypeid><deviceid>4132</deviceid><accid>236</accid><imb>N</imb><vehnum>BR50G5401</vehnum>" +
                "<imei>352094081256205</imei><powsts>Y</powsts><servip>46.4.251.240</servip><servport>6001</servport><client>BSFC</client></live>"
               */
        String[] responseElement = {Constants.NODELATITUDE, Constants.NODELONGITUDE, Constants.NODEDISHA,
                Constants.NODESPEED, Constants.NODEIGNITION, Constants.NODEDISTANCE, Constants.NODEFUEL, Constants.NODELOADS,
                Constants.NODEAC, Constants.NODEDOOR, Constants.NODEGPS, Constants.NODESATELLITES, Constants.NODEORIGINTS,
                Constants.NODECREATEDON, Constants.NODEADDRESS, Constants.NODEDEVICETYPEID, Constants.NODEDEVICEID,
                Constants.NODEACCOUNTID, Constants.NODEIMMOBILISER, Constants.NODEVEHICLENUMBER, Constants.NODEIMEI,
                Constants.NODEPOWSTATUS, Constants.NODECLIENT
        };

        xmlKeys.put(Constants.NODELIVE, responseElement);

        return xmlKeys;
    }


    private static String callApi(String request) throws Exception{
        StringBuilder response = new StringBuilder();
        // String url = "http://205.147.99.109:4022/traqmatix/api-1.0/json/request/";
        String url = "https://localhost:6150/traqmatix/api-1.0/json/request/";
        url = "http://3.92.17.237:6162/traqmatix/api-1.0/json/request/";
        url = "http://3.92.17.237:9091/traqmatix/kafka/producer/";
        URL endPoint = new URL(url);
        OutputStreamWriter writer = null;
        OutputStream output = null;

        HttpURLConnection connection = (HttpURLConnection) endPoint.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "text/plain");
        //connection.setRequestProperty("Content-Type", "application/json");

        writer = new OutputStreamWriter(connection.getOutputStream());
        output = connection.getOutputStream();

        output.write(request.getBytes("UTF-8"));
        writer.flush();

        String line;
        InputStreamReader is = null;
        BufferedReader reader = null;

        if (connection.getResponseCode() >= 400) {
            is = new InputStreamReader(connection.getErrorStream());
        } else {
            is = new InputStreamReader(connection.getInputStream());
        }
        reader = new BufferedReader(is);
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        return response.toString();
    }


    public MongoClient mongoDbConnection(){

        String DB_SRV_USR = "traqmatix";
        String DB_SRV_PWD = "tr@qm@t1X";

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
        credential = MongoCredential.createCredential(DB_SRV_USR, dbName,
                DB_SRV_PWD.toCharArray());

        List<ServerAddress> seeds = new ArrayList<ServerAddress>();
        seeds.add(new ServerAddress(mdbHost));        // Primary
        //seeds.add(new ServerAddress("192.168.100.23"));        // Primary

        MongoClient mongo = new MongoClient(seeds, credential, opts);
        //setMongoDBClient(mongo);

        System.out.println("Connected to the Mongo successfully") ;
        // Accessing the admin
        //setMongoDatabase(mongo.getDatabase(getAppConfig().getMongoDB()));

        return mongo;

    }


    private static Connection createNewConnection(){
        Connection conn = null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://"+dbHost +":3306/prod_traq", "traqmatix", "tr@qmAt1x");

        }catch(Exception ex){
            return null;
        }
        return conn;
    }

    List <Device> deviceList(){
        String query = "SELECT dev.ID as ID, IMEI, ASSET_NUMBER, ACCOUNT_ID, DEVICE_TYPE_ID  " +
                "FROM device dev, account ac WHERE STATUS_CODE = 'A' and ac.id=dev.ACCOUNT_ID and ac.CLIENT = 'BSFC'";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<Device> deviceList = new ArrayList<Device>();
        try {
            conn = createNewConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                Device device = new Device();
                device.setId(rs.getLong("ID"));
                device.setIMEI(rs.getString("IMEI"));
                device.setLicenseno(rs.getString("ASSET_NUMBER"));
                device.setAccId(rs.getLong("ACCOUNT_ID"));
                device.setUdv1(rs.getString("DEVICE_TYPE_ID"));

                deviceList.add(device);

            }
        }catch (Exception ee){
            ee.printStackTrace();
        }finally {
            try{
                rs.close();
            }catch (Exception e){
                rs = null;
            }
            try{
                stmt.close();
            }catch (Exception e){
                stmt = null;
            }
            try{
                conn.close();
            }catch (Exception e){
                conn = null;
            }
        }
        return deviceList;
    }


    public List<TrackData> findTrack(RequestMessage rm, List<Account> accountList){

        BasicDBObject trackData = new BasicDBObject();

        // Date between start & end date
        BasicDBObject dates = new BasicDBObject();
        dates.put("$gte", rm.getStartDate());
        dates.put("$lte", rm.getEndDate());

        trackData.put(Constants.NODEORIGINTS,dates);

        // Account Validation
        BasicDBObject accObj = new BasicDBObject();
        if(accountList != null){
            List<Long> accIds = new ArrayList<Long>();
            for(Account account : accountList){
                accIds.add(account.getId());
            }
            accObj.put("$in", accIds);       // Types not in the List
            trackData.put(Constants.NODEACCOUNTID, accObj);
        }else if(rm.getAccId() != null && rm.getAccId()>0){
            accObj.put("$eq", rm.getAccId());       // Types not in the List
            trackData.put(Constants.NODEACCOUNTID, accObj);
        }

        // Device Validation
        if(rm.getAssetId() != null && rm.getAssetId()>0){
            BasicDBObject assetObj = new BasicDBObject();
            assetObj.put("$eq", rm.getAssetId());       // Types not in the List
            trackData.put(Constants.NODEDEVICEID, assetObj);
        }
        if(rm.getAssetids() != null && rm.getAssetids().size()>0){
            BasicDBObject assetObj = new BasicDBObject();
            /*List<String> devList = new ArrayList<String>();
            for(int i=0; i<rm.getAssetids().size(); i++){
                devList.add(rm.getAssetids().get(i).toString());
            }*/
            assetObj.put("$in", rm.getAssetids());       // Types not in the List
            trackData.put(Constants.NODEDEVICEID, assetObj);
        }


        String entity = "";

        if(rm.getClient() != null){
            entity = rm.getClient() + entityName;
        }
        else{
            entity = entityName.toLowerCase();
        }

        List<TrackData> trackList = null;

        try{
            MongoCollection mongoCollection = mongoClient.getDatabase(dbName).getCollection(entity);
            System.out.println("mongoCollection.."+mongoCollection.countDocuments(trackData));

            // Sort in ascending order
            FindIterable<Document> trackIterable = mongoCollection.find(trackData)
                    .sort(new BasicDBObject(Constants.NODEORIGINTS, 1));

            trackList = createList(trackIterable);

        }catch (Exception e){
            e.printStackTrace();
        }finally {

        }

        return trackList;
    }


    public List<TrackData> createList(FindIterable<Document> resultList) {
        List<TrackData> dataList = new ArrayList<TrackData>();
        try {
            for(Document doc : resultList){
                try {
                    TrackData track = new TrackData();
                    track.setDocId(doc.getObjectId("_id"));
                    track.setAccId(doc.getLong(Constants.NODEACCOUNTID));
                    Long speed = Math.round(doc.getDouble(Constants.NODESPEED));
                    track.setLatitude(doc.getDouble(Constants.NODELATITUDE));
                    track.setLongitude(doc.getDouble(Constants.NODELONGITUDE));
                    track.setLocation(doc.getString(Constants.NODEADDRESS));
                    track.setAltitude(doc.getString(Constants.NODEALTITUDE));
                    track.setCourse(doc.getString(Constants.NODEDISHA));
                    track.setSpeed(Math.round(speed));
                    track.setIgnition(doc.getString(Constants.NODEIGNITION));
                    track.setGps(doc.getString(Constants.NODEGPS));
                    //track.setFuel(doc.getString(Constants.NODEFUEL));
                    Object obj = doc.get(Constants.NODEFUEL);
                    if (obj instanceof Double)
                        track.setFuel(doc.getDouble(Constants.NODEFUEL));
                    else {
                        try {
                            track.setFuel(Double.parseDouble(doc.getString(Constants.NODEFUEL)));
                        } catch (Exception e) {
                            track.setFuel(0.0);
                        }
                    }
                    ///Double load = 0.0D;

                    Object loadobj = doc.get(Constants.NODELOADS);
                    if (loadobj != null) {
                        if (loadobj instanceof Double)
                            track.setLoads(doc.getDouble(Constants.NODELOADS));
                        else
                            track.setLoads(Double.parseDouble(doc.getString(Constants.NODELOADS)));
                    }
                    track.setDeviceId(doc.getLong(Constants.NODEDEVICEID).toString());
                    try {
                        if(doc.containsKey(Constants.NODEVEHICLENUMBER)) {
                            track.setLicenseNumber(doc.getString(Constants.NODEVEHICLENUMBER));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        track.setIMEI(doc.getString(Constants.NODEIMEI));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    track.setImb(doc.getString(Constants.NODEIMMOBILISER));
                    track.setInitTs(doc.getString(Constants.NODECREATEDON).replaceAll("-09-26", "-10-30"));
                    track.setOrgTs(doc.getString(Constants.NODEORIGINTS).replaceAll("-09-26","-10-28"));
                    //track.setTemp(doc.getString(Constants.NODETEMP));
                    track.setBattery(doc.getString(Constants.NODEBATTERY));
                    track.setLockSts(doc.getString(Constants.NODELOCK));
                    track.setType(doc.getString(Constants.NODEPACKETTYPE));
                    track.setAc(doc.getString(Constants.NODEAC));
                    try {
                        track.setDoor(doc.getString(Constants.NODEDOOR));
                    } catch (Exception e) {

                    }
                    track.setNos(Integer.parseInt(doc.getString(Constants.NODESATELLITES)));

                    if (doc.containsKey(Constants.NODERFID)) {
                        track.setRfId(doc.getString(Constants.NODERFID));
                    }
                    if (doc.containsKey(Constants.NODEREASON)) {
                        track.setReason(doc.getString(Constants.NODEREASON));
                    }
                    dataList.add(track);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return dataList;
    }


}
