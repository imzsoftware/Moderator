package com.traq.manipulator.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * Created by Amit on 30/5/19.
 */
public class ProducerCreator {

    //Assign topicName to string variable

    // create instance for properties to access producer configs
/*    Properties props = new Properties();

    //Assign localhost id
    props.put("bootstrap.servers", IKafkaConstants.KAFKA_BROKERS);

    //Set acknowledgements for producer requests.
    props.put("acks", IKafkaConstants.ACK);

    //If the request fails, the producer can automatically retry,
    props.put("retries", IKafkaConstants.RETRIES);

    props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());

    //Specify buffer size in config*/
    //props.put("batch.size", 16384);

    //Reduce the no of requests less than 0
    //props.put("linger.ms", 1);

    //The buffer.memory controls the total amount of memory available to the producer for buffering.
    //props.put("buffer.memory", 33554432);

    public static Producer<Long, String> createProducer(String broker) {
        Properties props = new Properties();
        //Assign localhost id
        if(broker != null){
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        }else {
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, IKafkaConstants.KAFKA_BROKERS);
        }

        //Set acknowledgements for producer requests.
        props.put(ProducerConfig.ACKS_CONFIG, IKafkaConstants.ACK);

        //If the request fails, the producer can automatically retry,
        props.put(ProducerConfig.RETRIES_CONFIG, IKafkaConstants.RETRIES);

        //the producer will wait for up to the given delay to allow other records to be sent
        // so that the sends can be batched together
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);

        //props.put(ProducerConfig.CLIENT_ID_CONFIG, IKafkaConstants.CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        //props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getCanonicalName());

        return new KafkaProducer<>(props);
    }
}
