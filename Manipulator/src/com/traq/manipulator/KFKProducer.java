package com.traq.manipulator;

import com.mongodb.client.MongoDatabase;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.manipulator.bots.DataInsertion;
import com.traq.manipulator.config.ProducerCreator;
import com.traq.manipulator.dataparser.ParserMain;
import com.traqm.ds.tp.TPDataPush;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.json.JSONArray;

import java.util.List;


public class KFKProducer extends BaseInitializer {

    private ResponseMessage rm;
    static MongoDatabase database = null;

    public String producerRequest(String req, String transId) {

        String response = "Success";
        try {
            String topic = TagValues.getNodeValue(req, Constants.NODECLIENT);

            if (topic.isEmpty()) {
                topic = "TM";
            }

/*            Future<String> future = null;
            ExecutorService executor = null;
            executor = Executors.newSingleThreadExecutor();*/
            try {
                info("KFKProducer...vendorCode............." + topic);
                runProducer(req, topic);
/*                ProducerTask task = new ProducerTask(req, transId, topic);
                future = executor.submit(task);
                String resp = future.get(3000, TimeUnit.MILLISECONDS);*/
                info("Message sent successfully....." + topic);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
/*                executor.shutdown();
                executor = null;*/
            }

        } catch (Exception ex) {
            response = "Fail.." + ex.getMessage();
            error("ProducerMain Core Exception...." + response);
        } finally {

        }

        return response;
    }

    public String runProducer(String request, String topicName) {
        if (KFKMain.producer == null) {
            info("KFKProducer.runProducer(String,String)..., KFKMain.producer=" + KFKMain.producer);
            KFKMain.producer = ProducerCreator.createProducer(getAppConfig().getKfkBroker());
            info("KFKProducer.runProducer(String,String)..1.., KFKMain.producer=" + KFKMain.producer);
        }

        try {
            if (database == null) {
                try {
                    InitBean initbean = new InitBean();
                    initbean.mongoDbConnection();
                    info("getAppConfig().getMongoDB()....." + getAppConfig().getMongoDB());
                    database = getMongoDBClient().getDatabase(getAppConfig().getMongoDB());
                } catch (Exception e) {

                }
            }
            Long deviceId = Long.parseLong(TagValues.getNodeValue(request, Constants.NODEDEVICEID));
            Long partition = (deviceId % 3);
            //Integer partition = Integer.parseInt(RandomStringUtils.randomNumeric(1));
            /*ProducerRecord<Long, String> record =
                    new ProducerRecord<>(topicName, System.currentTimeMillis(),  req);*/
            ProducerRecord<Long, String> record =
                    new ProducerRecord<>(topicName, partition.intValue(), System.currentTimeMillis(), request);

            //producer.send(record);
            RecordMetadata metadata = KFKMain.producer.send(record).get();
            info(record + ", Partition.." + metadata.partition() + ", Offset..." + metadata.offset());

/*            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    System.out.println("Message sent to topic ->" + metadata.topic()+ " ,parition->" + metadata.partition() +" stored at offset->" + metadata.offset());
                    ;
                }
            });*/
            //producer.close();
            //info("Message sent successfully....."+topicName);

        } catch (Exception e) {
            error(request);
            e.printStackTrace();

            try {
                DataInsertion dataInsertion = new DataInsertion();
                String liveData = TagValues.getNodeValue(request, Constants.NODELIVE);
                String rawData = TagValues.getNodeValue(request, Constants.NODERAW);
                String lockUnlockData = TagValues.getNodeValue(request, Constants.NODEELOCK);
                String historyData = TagValues.getNodeValue(request, Constants.NODEHIST);

                String deviceDetails = TagValues.getNodeValue(request, Constants.NODEDEVICEDATA);
                String respData = TagValues.getNodeValue(request, Constants.NODERESP);
                String alertData = TagValues.getNodeValue(request, Constants.NODEALERT);

                // Extra Logic
//                String deviceTypeId = TagValues.getNodeValue(request, Constants.NODEDEVICETYPEID);
                String deviceTypeId = TagValues.getNodeValue(deviceDetails, Constants.NODEDEVICETYPEID);
                String client = TagValues.getNodeValue(deviceDetails, Constants.NODECLIENT);

                if (database == null) {
                    info("getAppConfig().getMongoDB()....." + getAppConfig().getMongoDB());
                    database = getMongoDBClient().getDatabase(getAppConfig().getMongoDB());
                }



                if (!rawData.isEmpty() && !TagValues.getNodeValue(rawData, Constants.NODEDATA).isEmpty()) {
                    dataInsertion.insertRawData(database, client, rawData, deviceDetails);
                    //consumer.commitAsync();
                }
                if (!lockUnlockData.isEmpty() && !TagValues.getNodeValue(lockUnlockData, Constants.NODEDATA).isEmpty()) {
                    dataInsertion.insertLockEventsData(database, client, lockUnlockData);
                    //consumer.commitAsync();
                }

                if (!liveData.isEmpty()) {

                    boolean isInserted = dataInsertion.insertTrackData(database, client, liveData);
                    if (isInserted) {
                        info("Inserted in DB ....." + request);
                    } else {
                        error("Failed to insert in DB ....." + request);
                    }
                }
                /*if (!lockUnlockData.isEmpty()) {
                    dataInsertion.insertLockEventsData(database, request, lockUnlockData);
                }
                if (!rawData.isEmpty()) {
                    dataInsertion.insertRawData(database, request, rawData);
                }*/
                if (!respData.isEmpty() && !TagValues.getNodeValue(respData, Constants.NODERECORDS).isEmpty()) {
                    debug("Receive Resp data ....." + request);
                    String respRecords = TagValues.getNodeValue(respData, Constants.NODERECORDS);
                    List<String> respRecordList = TagValues.getAllNodeValue(respRecords, Constants.NODERECORD);

                    if (null != respRecordList && respRecordList.size() > 0) {
                        dataInsertion.insertRespData(database, client, respData, respRecordList, deviceDetails);
                    } else {
                        info("RespRecordList RESPDATA ..... null, No Insertion");
                    }
                }

                if (!alertData.isEmpty() && !TagValues.getNodeValue(alertData, Constants.NODERECORDS).isEmpty()) {
                    debug("Receive Alert data ....." + request);

                    String alertRecords = TagValues.getNodeValue(alertData, Constants.NODERECORDS);
                    List<String> alertRecordList = TagValues.getAllNodeValue(alertRecords, Constants.NODERECORD);
                    if (null != alertRecordList && alertRecordList.size() > 0) {
                        dataInsertion.insertAlertData(database, client, alertData, alertRecordList,
                                deviceDetails, liveData);
                    } else {
                        info("alertRecordList ALERTDATA ..... null, No Insertion");
                    }


//                        AlertInsertion alertInsertion = new AlertInsertion();
//                        alertInsertion.insertAlerts(record.topic(), alertData, liveData, deviceDetails);
                    //consumer.commitAsync();
                }

                if (!historyData.isEmpty() && !TagValues.getNodeValue(historyData, Constants.NODEDATA).isEmpty()) {
                    debug("History data ....." + historyData);
                    ParserMain historyParser = new ParserMain(deviceTypeId, historyData);
                    historyParser.setDeviceDetail(deviceDetails);
                    JSONArray histjsonArray = historyParser.parse();  // final data in xml format <history></history>
                    debug("History data for insertion ....." + histjsonArray);
                    for (int i = 0; i < histjsonArray.length(); i++) {
                        debug("History data for insertion ....." + histjsonArray.getJSONObject(i));
                        //info("database ....." +database);
                        boolean isInserted = dataInsertion.insertTrackData(database, client, histjsonArray.getJSONObject(i));
                        //consumer.commitAsync();
                    }
                    //info("getAppConfig().getDataPushToVendor() ....." +getAppConfig().getDataPushToVendor());
                    /*if (getAppConfig().getDataPushToVendor() == 1
                            && client.equalsIgnoreCase(getAppConfig().getHost())) {
                        TPDataPush tpDP = new TPDataPush();
                        tpDP.pushHistData(histjsonArray, client);
                    }*/
                }
                /*if (!historyData.isEmpty()) {
                    ParserMain historyParser = new ParserMain(deviceTypeId, historyData);
                    JSONArray histjsonArray = historyParser.parse();  // final data in xml format <history></history>
                    for (int i = 0; i < histjsonArray.length(); i++) {
                        boolean isInserted = dataInsertion.insertTrackData(database, request, histjsonArray.getJSONObject(i));
                        if (isInserted) {
                            info("Inserted in DB ....." + request);
                        } else {
                            error("Failed to insert in DB ....." + request);
                        }
                    }
                }*/
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } finally {
            KFKMain.producer.flush();
        }
        return "success";
    }


}
