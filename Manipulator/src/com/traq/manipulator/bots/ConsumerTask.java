package com.traq.manipulator.bots;

import com.mongodb.client.MongoDatabase;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.exceptions.CommonException;
import com.traq.manipulator.KFKConsumer;
import com.traq.manipulator.config.ConsumerCreator;
import com.traq.manipulator.dataparser.ParserMain;
import com.traq.util.RandomStringUtils;
import com.traqm.dp.obj.TrkData;
import com.traqm.ds.tp.TPDataPush;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.bson.Document;
import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by Amit on 29/5/19.
 */
public class ConsumerTask extends BaseInitializer implements Callable<String> {

    private String topic;
    private String groupName;
    private String request;
    private String transid;
    private KafkaConsumer<Long, String> consumer = null;
    static MongoDatabase database = null;
    private ResponseMessage rm;

    public String getTransid() {
        return transid;
    }

    public void setTransid(String transid) {
        this.transid = transid;
    }

    public ConsumerTask(String transid, String topic, String groupName) {
        this.transid = transid;
        this.topic = topic;
        this.groupName = groupName;
    }

    public ConsumerTask(String transid, String topic, KafkaConsumer<Long, String> consumer) {
        this.transid = transid;
        this.topic = topic;
        this.consumer = consumer;
    }

    public synchronized String call() throws CommonException {

        Thread.currentThread().setName(transid);
        runConsumer(topic, groupName);
        return "Success";

    }

    private Map<TopicPartition, OffsetAndMetadata> currentOffsets =
            new HashMap<>();


    private class HandleRebalance implements ConsumerRebalanceListener {
        public void onPartitionsAssigned(Collection<TopicPartition>
                                                 partitions) {
        }

        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            System.out.println("Lost partitions in rebalance. " +
                    "Committing current offsets:" + currentOffsets);
            consumer.commitSync(currentOffsets);
        }
    }

    public void runConsumer(String topicName, String group) {
        //info("runConsumer="+topicName);
        //KafkaConsumer<String, String> consumer = new KafkaConsumer <String, String>(props);
        //info("getAppConfig().getKfkBroker()....."+getAppConfig().getKfkBroker());
        if (consumer == null)
            consumer = ConsumerCreator.createConsumer(group, getAppConfig().getKfkBroker());
        //print the topic name
        try {
            //Kafka Consumer subscribes list of topics here.
            //consumer.subscribe(Arrays.asList(topicName), new HandleRebalance());
//            info("groupName....."+consumer.listTopics());

            TopicPartition tpp = new TopicPartition(topicName, getAppConfig().getTopicPartition());
/*            TopicPartition tp1 = new TopicPartition(topicName, 1);
            TopicPartition tp2 = new TopicPartition(topicName, 2);*/
            Collection<TopicPartition> partitionList = new ArrayList<>();
            partitionList.add(tpp);
/*            partitionList.add(tp1);
            partitionList.add(tp2);*/
            consumer.assign(partitionList);

            /*TopicPartition topicPartition = new TopicPartition(topic, 0);
            consumer.assign(Arrays.asList(topicPartition));
            HashMap<TopicPartition, Long> beginningOffsets = new HashMap<>();
            beginningOffsets.put(topicPartition, 0L);*/
            //consumer.updateBeginningOffsets(beginningOffsets);
//            info("consumer.partitionsFor(topicName)  " + consumer.partitionsFor(topicName));
            consumer.partitionsFor(topicName);
            //addRecord(new ConsumerRecord<>(topic, 0, 0L, "some-key", "some-value"));

            ConsumerRecords<Long, String> records = consumer.poll(1000); // 1000 milliseconds poll block if data not available
            DataInsertion dataInsertion = new DataInsertion();
            int count = 0;

            List<TrkData> dataList = new ArrayList<TrkData>();
            JSONArray liveDataArray = new JSONArray();
            SimpleDateFormat redisFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();

            Long sysTime = System.currentTimeMillis();

            for (TopicPartition partition : records.partitions()) {
                //TopicPartition part = new TopicPartition(topicName, getAppConfig().getTopicPartition());
                info("TopicPartition : " + partition.partition());
                // info("part TopicPartition : " +part.partition());
                List<ConsumerRecord<Long, String>> partitionRecords = records.records(partition);
                for (ConsumerRecord<Long, String> record : partitionRecords) {
                    //info(record.partition() +": " +record.offset() + ": " + record.value());

                    //for (ConsumerRecord<Long, String> record : records){
                    // print the offset,key and value for the consumer records.
                /*System.out.printf("offset = %d, key = %s, value = %s, topic = %s, partition = %d\n",
                        record.offset(), record.key(), record.value(), record.topic(), record.partition());*/
                    //info("Record....." +record.value());
                    if (count == 0) {
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

                    if (!deviceDetails.isEmpty()) {
                        info("deviceDetails......deviceTypeId = " + deviceTypeId + "      " + deviceDetails);
                    }


                    String alertData = TagValues.getNodeValue(record.value(), Constants.NODEALERT);
               /* if(!alertData.isEmpty() && !TagValues.getNodeValue(alertData, Constants.NODERECORDS).isEmpty()){
                    AlertInsertion alertInsertion = new AlertInsertion();
                    alertInsertion.insertAlerts(record.topic(), alertData, liveData, deviceDetails);
                    //consumer.commitAsync();
                }*/

                    if (!rawData.isEmpty() && !TagValues.getNodeValue(rawData, Constants.NODEDATA).isEmpty()) {
                        dataInsertion.insertRawData(database, record.topic(), rawData);
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

/*                    if(TagValues.getNodeValue(liveData, Constants.NODEACCOUNTID).equalsIgnoreCase("1277")){
                        try {// for intugine (traqelock client)
                            info("Intugine.......pushDataWithAlert "+liveData);
                            TPDataPush tp = new TPDataPush();
                            tp.pushDataWithAlert(liveData, alertData, "INGN");

                        }catch (Exception e){
                            System.out.println("Intugine Error message ... "+e.getMessage());
                            e.printStackTrace();
                        }
                    }*/
                        //}
                        //consumer.commitAsync();
                    }
                    if (!historyData.isEmpty() && !TagValues.getNodeValue(historyData, Constants.NODEDATA).isEmpty()) {
                        info("History data ....." + historyData);
                        ParserMain historyParser = new ParserMain(deviceTypeId, historyData);
                        historyParser.setDeviceDetail(deviceDetails);
                        JSONArray histjsonArray = historyParser.parse();  // final data in xml format <history></history>
                        info("History data for insertion ....." + histjsonArray);
                        for (int i = 0; i < histjsonArray.length(); i++) {
                            debug("History data for insertion ....." + histjsonArray.getJSONObject(i));
                            //info("database ....." +database);
                            boolean isInserted = dataInsertion.insertTrackData(database, record.topic(), histjsonArray.getJSONObject(i));
/*                        if(TagValues.getNodeValue(liveData, Constants.NODEACCOUNTID).equalsIgnoreCase("1277")){
                            try {// for intugine (traqelock client)
                                ThirdParty tp = new ThirdParty();
                                tp.accountData(histjsonArray.getJSONObject(i).toString());
                            }catch (Exception e){
                                System.out.println("Intugine Error message ... "+e.getMessage());
                                e.printStackTrace();
                            }
                        }*/
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

                consumer.commitAsync(currentOffsets, null);
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
            ex.printStackTrace();
            error("ConsumerTask.........." + ex.getMessage());
        } finally {
            try {
                KFKConsumer.topicList.remove(topicName);
                consumer.commitSync(currentOffsets);
                //consumer.commitSync();
            } finally {
                //consumer.close();
            }
        }
    }

}
