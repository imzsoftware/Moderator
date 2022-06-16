package com.traq.manipulator;

import com.mongodb.client.MongoDatabase;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.base.RequestID;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.config.CoreConfig;
import com.traq.logger.TraqLog;
import com.traq.manipulator.bots.ConsumerTask;
import com.traq.manipulator.bots.DataInsertion;
import com.traq.manipulator.config.ConsumerCreator;
import com.traq.manipulator.dataparser.ParserMain;
import com.traq.util.RandomStringUtils;
import com.traqm.dp.obj.TrkData;
import com.traqm.ds.tp.TPDataPush;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class KFKConsumer extends BaseInitializer {

    public KFKConsumer() {
        super("CORE_LOGGER");
    }

    static CoreConfig coreConfig = null;
    public static List topicList = new ArrayList<String>();
    static MongoDatabase database = null;
    private ResponseMessage rm;


    private String invalidRequest(int resultCode, String messageType) {

        JSONObject jsonObject = new JSONObject();
        JSONObject mainObj = new JSONObject();
        try {
            jsonObject.put(Constants.NODERESPONSETYPE, messageType);
            jsonObject.put(Constants.NODERESULTCODE, resultCode);
            jsonObject.put(Constants.NODERESULTDESCRIPTION, ResultCodeDescription.getDescription(resultCode));

            mainObj.put(Constants.NODERESPONSE, jsonObject);
        } catch (JSONException je) {

        }

        return mainObj.toString();
    }

    private String invalidRequest(int resultCode, String messageType, String msg) {

        JSONObject jsonObject = new JSONObject();
        JSONObject mainObj = new JSONObject();
        try {
            jsonObject.put(Constants.NODERESPONSETYPE, messageType);
            jsonObject.put(Constants.NODERESULTCODE, resultCode);
            jsonObject.put(Constants.NODERESULTDESCRIPTION, ResultCodeDescription.getDescription(resultCode));
            if (msg != null)
                jsonObject.put(Constants.NODEMESSAGE, msg.replaceAll("\"", ""));

            mainObj.put(Constants.NODERESPONSE, jsonObject);
        } catch (JSONException je) {

        }

        return mainObj.toString();
    }

    private Map<TopicPartition, OffsetAndMetadata> currentOffsets =
            new HashMap<>();

    // Start Without SSL
    @Deprecated
    public void startConsumers() {
        TraqLog logger = TraqLog.getInstance("CORE_LOGGER");
        try {
            // Set Operator in Config
            coreConfig = getAppConfig();
            //database = getMongoDBClient().getDatabase(getAppConfig().getMongoDB());

            List<String> topics = null; // clientDao.findAll(true);

            while (true) {
                Future<String> future = null;
                ExecutorService executor = null;
                topics = hkeys("client_list");
                info("KFKConsumer....topics........." + topics);
                if (topics != null) {
                    try {
                        executor = Executors.newFixedThreadPool(topics.size());
                        for (String topic : topics) {
                            String transId = RequestID.next();

                            if (!topicList.contains(topic.trim())) {
                                topicList.add(topic.trim());
                                ConsumerTask task = new ConsumerTask(transId, topic, coreConfig.getConsumerGroup());
                                future = executor.submit(task);
                                Thread.sleep(coreConfig.getSleepTime());
                            }
                        }
                    } finally {
                        executor.shutdown();
                        executor = null;
                    }
                }
                Thread.sleep(coreConfig.getSleepTime() * 2);
            }
            //}

        } catch (Exception ex) {
            ex.printStackTrace();
            error("Consumer..Exception......" + ex.getMessage());
            System.exit(0);
        }
    }

    @Deprecated
    public void startConsumer(String topicName, String partition) {
        TraqLog logger = TraqLog.getInstance("CORE_LOGGER");
        KafkaConsumer<Long, String> consumer = null;
        try {
            coreConfig = getAppConfig();
            consumer = ConsumerCreator.createConsumer(coreConfig.getConsumerGroup(), getAppConfig().getKfkBroker());
            while (true) {
                Future<String> future = null;
                ExecutorService executor = null;
                if (topicName != null) {
                    try {
                        executor = Executors.newFixedThreadPool(1);
                        String transId = RequestID.next();
                        ConsumerTask task = new ConsumerTask(transId, topicName, consumer);
                        future = executor.submit(task);
                        Thread.sleep(coreConfig.getSleepTime());

                    } finally {
                        executor.shutdown();
                        executor = null;
                    }
                }
                Thread.sleep(coreConfig.getSleepTime() * 2);
            }
            //}

        } catch (Exception ex) {
            ex.printStackTrace();
            error("Consumer..Exception......" + ex.getMessage());
            System.exit(0);
        } finally {
            consumer.close();
        }
    }


    public void startConsumersV2() {
        TraqLog logger = TraqLog.getInstance("CORE_LOGGER");
        try {
            coreConfig = getAppConfig();
            List<String> topics = null; // clientDao.findAll(true);

            while (true) {
                Future<String> future = null;
                ExecutorService executor = null;
                topics = hkeys("client_list");
                info("KFKConsumer....topics........." + topics);
                if (topics != null) {
                    try {
                        executor = Executors.newFixedThreadPool(topics.size());
                        for (String topic : topics) {
                            String transId = RequestID.next();

                            if (!topicList.contains(topic.trim())) {
                                topicList.add(topic.trim());
//                                ConsumerTask task = new ConsumerTask(transId, topic, coreConfig.getConsumerGroup());
//                                future = executor.submit(task);
                                this.startConsumer(topic.trim());
                                Thread.sleep(coreConfig.getSleepTime());
                            }
                        }
                    } finally {
                        executor.shutdown();
                        executor = null;
                    }
                }
                Thread.sleep(coreConfig.getSleepTime() * 2);
            }
            //}

        } catch (Exception ex) {
            ex.printStackTrace();
            error("Consumer..Exception......" + ex.getMessage());
            System.exit(0);
        }
    }

    public void startConsumer(String topicName) {
        KafkaConsumer<Long, String> consumer = null;
        try {
            coreConfig = getAppConfig();
            consumer = ConsumerCreator.createConsumer(coreConfig.getConsumerGroup(), getAppConfig().getKfkBroker());
            while (true) {
                if (topicName != null) {
                    runConsumer(topicName, "", consumer);
                }
                consumer.commitAsync();
                Thread.sleep(coreConfig.getSleepTime());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error("Consumer..Exception......" + ex.getMessage());
            System.exit(0);
        } finally {
            consumer.close();
        }
    }


    public void runConsumer(String topicName, String group, KafkaConsumer<Long, String> consumer) {
        // if consumer closed, create object of consumer again
        if (consumer == null)
            consumer = ConsumerCreator.createConsumer(group, getAppConfig().getKfkBroker());

        try {
            TopicPartition tpp = new TopicPartition(topicName, getAppConfig().getTopicPartition());
            Collection<TopicPartition> partitionList = new ArrayList<>();
            partitionList.add(tpp);
            consumer.assign(partitionList);
            consumer.partitionsFor(topicName);
            ConsumerRecords<Long, String> records = consumer.poll(1000); // 1000 milliseconds poll block if data not available
            DataInsertion dataInsertion = new DataInsertion();
            int count = 0;

            List<TrkData> dataList = new ArrayList<TrkData>();
            JSONArray liveDataArray = new JSONArray();
            SimpleDateFormat redisFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();

            for (TopicPartition partition : records.partitions()) {
                debug("TopicPartition : " + partition.partition());
                List<ConsumerRecord<Long, String>> partitionRecords = records.records(partition);
                for (ConsumerRecord<Long, String> record : partitionRecords) {
                    if (count == 0) {
                        try {
                            Thread.currentThread().setName(record.offset() + "");
                        } catch (Exception e) {
                        }
                        info(record.partition() + ": " + record.offset() + ": " + record.value());
                    }
                    count++;
                    if (database == null) {
                        info("getAppConfig().getMongoDB()....." + getAppConfig().getMongoDB());
                        database = getMongoDBClient().getDatabase(getAppConfig().getMongoDB());
                    }

                    String liveData = TagValues.getNodeValue(record.value(), Constants.NODELIVE);
                    String rawData = TagValues.getNodeValue(record.value(), Constants.NODERAW);
                    String lockUnlockData = TagValues.getNodeValue(record.value(), Constants.NODEELOCK);

                    String historyData = TagValues.getNodeValue(record.value(), Constants.NODEHIST);
                    String deviceTypeId = TagValues.getNodeValue(record.value(), Constants.NODEDEVICETYPEID);
                    String deviceDetails = TagValues.getNodeValue(record.value(), Constants.NODEDEVICEDATA);
                    String respData = TagValues.getNodeValue(record.value(), Constants.NODERESP);

//
/*                    if(!deviceDetails.isEmpty()){
                        info("deviceDetails......deviceTypeId = "+deviceTypeId +"      "+deviceDetails);
                    }*/


                    String alertData = TagValues.getNodeValue(record.value(), Constants.NODEALERT);

                    if (!rawData.isEmpty() && !TagValues.getNodeValue(rawData, Constants.NODEDATA).isEmpty()) {
                        dataInsertion.insertRawData(database, record.topic(), rawData, deviceDetails);
                        //consumer.commitAsync();
                    }
                    if (!lockUnlockData.isEmpty() && !TagValues.getNodeValue(lockUnlockData, Constants.NODEDATA).isEmpty()) {
                        dataInsertion.insertLockEventsData(database, record.topic(), lockUnlockData);
                        //consumer.commitAsync();
                    }

                    if (!liveData.isEmpty()) {
                        //info("Inserting Live Data in Mongo..... ");
                        boolean isInserted = dataInsertion.insertTrackData(database, record.topic(), liveData);
                        if (isInserted) {
                            //info("getAppConfig().getDataPushToVendor() ....." +getAppConfig().getDataPushToVendor());
                            if (getAppConfig().getDataPushToVendor() == 1) {
                                TPDataPush tpDP = new TPDataPush();
                                liveDataArray.put(tpDP.createObj(liveData, record.topic()));
                            }
                        } else {
                            error("Failed to insert in DB ....." + record.value());
                        }
                        // if("Y".equals(TagValues.getNodeValue(liveData, Constants.NODEIGNITION))) {
                        //{"pp":[{"id":"868728030756594","h":35,"s":"0.0","y":30.285734,"x":77.992432,"t":"2019-02-01T04:26:24"}]}

                        if (getAppConfig().getDataPushToVendor() == 1) {
                            TrkData trkData = new TrkData();
                            trkData.setId(TagValues.getNodeValue(liveData, Constants.NODEIMEI));
                            trkData.setH((int) Double.parseDouble(TagValues.getNodeValue(liveData, Constants.NODEDISHA)));
                            trkData.setS(TagValues.getNodeValue(liveData, Constants.NODESPEED));
                            trkData.setY(Double.parseDouble(TagValues.getNodeValue(liveData, Constants.NODELATITUDE)));
                            trkData.setX(Double.parseDouble(TagValues.getNodeValue(liveData, Constants.NODELONGITUDE)));
                            Date time = redisFormat.parse(TagValues.getNodeValue(liveData, Constants.NODEORIGINTS));
                            //cal.setTime(time);
                            cal.setTime(new Date());
                            cal.add(Calendar.HOUR, -5);
                            cal.add(Calendar.MINUTE, -50);
                            Integer addDiff = Integer.parseInt(RandomStringUtils.randomNumeric(3));
                            cal.add(Calendar.SECOND, addDiff);
/*                    Integer diff = (int)(sysTime - cal.getTimeInMillis())/1000;
                    info("Time Difference ....." +diff);
                    if(diff > 20500){
                        RandomStringUtils.randomNumeric(4);
                        cal.add(Calendar.SECOND, (diff-20000));
                    }*/

                            trkData.setT(redisFormat.format(cal.getTime()).replace(" ", "T"));

                            dataList.add(trkData);
                        }

                        if (TagValues.getNodeValue(liveData, Constants.NODEACCOUNTID).equalsIgnoreCase("1277")) {
                            try {// for intugine (traqelock client)
                                debug("Intugine.......pushDataWithAlert " + liveData);
                                TPDataPush tp = new TPDataPush();
                                tp.pushDataWithAlert(liveData, alertData, "INGN");

                            } catch (Exception e) {
                                error("Intugine Error message ... " + e.getMessage());
//                                e.printStackTrace();
                            }
                        }
                        //}
                        //consumer.commitAsync();
                    }

                    if (!respData.isEmpty() && !TagValues.getNodeValue(respData, Constants.NODERECORDS).isEmpty()) {
                        debug("Receive Resp data ....." + record.value());
                        String respRecords = TagValues.getNodeValue(respData, Constants.NODERECORDS);
                        List<String> respRecordList = TagValues.getAllNodeValue(respRecords, Constants.NODERECORD);

                        if (null != respRecordList && respRecordList.size() > 0) {
                            dataInsertion.insertRespData(database, record.topic(), respData, respRecordList, deviceDetails);
                        } else {
                            info("RespRecordList RESPDATA ..... null, No Insertion");
                        }
                    }

                    if (!alertData.isEmpty() && !TagValues.getNodeValue(alertData, Constants.NODERECORDS).isEmpty()) {
                        debug("Receive Alert data ....." + record.value());

                        String alertRecords = TagValues.getNodeValue(alertData, Constants.NODERECORDS);
                        List<String> alertRecordList = TagValues.getAllNodeValue(alertRecords, Constants.NODERECORD);
                        if (null != alertRecordList && alertRecordList.size() > 0) {
                            dataInsertion.insertAlertData(database, record.topic(), alertData, alertRecordList,
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
                            boolean isInserted = dataInsertion.insertTrackData(database, record.topic(), histjsonArray.getJSONObject(i));
                            //consumer.commitAsync();
                        }
                        //info("getAppConfig().getDataPushToVendor() ....." +getAppConfig().getDataPushToVendor());
                        if (getAppConfig().getDataPushToVendor() == 1
                                && record.topic().equalsIgnoreCase(getAppConfig().getHost())) {
                            TPDataPush tpDP = new TPDataPush();
                            tpDP.pushHistData(histjsonArray, record.topic());
                        }
                    }
                    //}
                    currentOffsets.put(
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1, null));
                }   // End For loop Records

                //consumer.commitAsync(currentOffsets, null);
                if (liveDataArray.length() > 0) {
                    TPDataPush tp = new TPDataPush();
                    tp.pushData(liveDataArray, getAppConfig().getHost());
                }
                if (dataList != null && dataList.size() > 0) {
                    // info("ConsumerTask...dataList="+dataList);
                    TPDataPush tp = new TPDataPush();
                    tp.pushData(dataList, "HERE");
                    try {
                        Document doc = tp.getDocument();
                        if (doc.getInteger(Constants.NODERECORDCOUNT) > 0) {
                            dataInsertion.insertDataPushResp(database, doc, "HERE_RESPONSE");
                        }
                    } catch (Exception e) {

                    }

                }
                //long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                //consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
            }
        } catch (Exception ex) {
            error("ConsumerTask.........." + ex.getMessage(), ex);
        } finally {
            try {
                KFKConsumer.topicList.remove(topicName);
                //consumer.commitSync(currentOffsets);
                //consumer.commitSync();
            } finally {
                //consumer.close();
            }
        }
    }


}
