package com.traq.manipulator.bots;

import com.mongodb.client.MongoDatabase;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.exceptions.CommonException;
import com.traq.manipulator.InitBean;
import com.traq.manipulator.config.ProducerCreator;
import com.traq.manipulator.dataparser.ParserMain;
import com.traq.util.RandomStringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.json.JSONArray;

import java.util.concurrent.Callable;


/**
 * Created by Amit on 29/5/19.
 */
public class ProducerTask extends BaseInitializer implements Callable<String> {

    private String topic;
    private String request;
    private String transid;
    static MongoDatabase database = null;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getTransid() {
        return transid;
    }

    public void setTransid(String transid) {
        this.transid = transid;
    }

    public ProducerTask(String request, String transid, String topic) {
        this.request = request;
        this.transid  = transid;
        this.topic  = topic;
    }

    public synchronized String call() throws CommonException {

        String resp = null;
        Thread.currentThread().setName(transid);
        debug("ProducerTask... topic......"+topic);
        resp = runProducer(topic,request);

        return resp;

    }

    public String runProducer(String topicName, String req){
        final Producer<Long, String> producer = ProducerCreator.createProducer(getAppConfig().getKfkBroker());

        try{
            Long deviceId = Long.parseLong(TagValues.getNodeValue(request, Constants.NODEDEVICEID));
            Long partition = (deviceId%3);
            //Integer partition = Integer.parseInt(RandomStringUtils.randomNumeric(1));
            /*ProducerRecord<Long, String> record =
                    new ProducerRecord<>(topicName, System.currentTimeMillis(),  req);*/
            ProducerRecord<Long, String> record =
                    new ProducerRecord<>(topicName, partition.intValue(), System.currentTimeMillis(),  request);

            //producer.send(record);
            RecordMetadata metadata = producer.send(record).get();
            //System.out.println("AMIT topicName.............."+topicName);
            //if(topicName.equals("DEL")) {
/*                ThirdParty tp = new ThirdParty();
                tp.pushData(req, record.topic());*/

//            }
            info(record + ", Partition.." + metadata.partition() + ", Offset..." + metadata.offset());

/*            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    System.out.println("Message sent to topic ->" + metadata.topic()+ " ,parition->" + metadata.partition() +" stored at offset->" + metadata.offset());
                    ;
                }
            });*/
            //producer.close();
            //info("Message sent successfully....."+topicName);
            if(database == null){
                try {
                    InitBean initbean = new InitBean();
                    initbean.mongoDbConnection();
                    info("getAppConfig().getMongoDB()....." + getAppConfig().getMongoDB());
                    database = getMongoDBClient().getDatabase(getAppConfig().getMongoDB());
                }catch (Exception e){

                }
            }
        }catch (Exception e){
            error(req);
            e.printStackTrace();

            try {
                DataInsertion dataInsertion = new DataInsertion();
                String liveData = TagValues.getNodeValue(request, Constants.NODELIVE);
                String rawData = TagValues.getNodeValue(request, Constants.NODERAW);
                String lockUnlockData = TagValues.getNodeValue(request, Constants.NODEELOCK);
                String historyData = TagValues.getNodeValue(request,Constants.NODEHIST);
                String deviceTypeId = TagValues.getNodeValue(request,Constants.NODEDEVICETYPEID);

                if(database == null){
                    info("getAppConfig().getMongoDB()....."+getAppConfig().getMongoDB());
                    database = getMongoDBClient().getDatabase(getAppConfig().getMongoDB());
                }
                if (!liveData.isEmpty()){
                   boolean isInserted = dataInsertion.insertTrackData(database, request, liveData);
                    if(isInserted){
                        info("Inserted in DB ....." +request);
                    }else{
                        error("Failed to insert in DB ....." +request);
                    }
                }
                if(!lockUnlockData.isEmpty()){
                    dataInsertion.insertLockEventsData(database, request, lockUnlockData);
                }
                if (!rawData.isEmpty()){
                    dataInsertion.insertRawData(database, request, rawData);
                }
                if(!historyData.isEmpty()){
                    ParserMain historyParser = new ParserMain(deviceTypeId,historyData);
                    JSONArray histjsonArray = historyParser.parse();  // final data in xml format <history></history>
                    for(int i=0;i<histjsonArray.length();i++) {
                        boolean isInserted = dataInsertion.insertTrackData(database, request,histjsonArray.getJSONObject(i));
                        if(isInserted){
                            info("Inserted in DB ....." +request);
                        }else{
                            error("Failed to insert in DB ....." +request);
                        }
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }finally {
            producer.flush();
            producer.close();
        }
        return "success";
    }

}
