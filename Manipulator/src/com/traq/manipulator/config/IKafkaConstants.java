package com.traq.manipulator.config;

/**
 * Created by Amit on 30/5/19.
 */
public interface IKafkaConstants {
    //public static String KAFKA_BROKERS = "10.10.1.43:9092";
    public static String KAFKA_BROKERS = "192.168.100.1:9092,192.168.100.2:9092,192.168.100.3:9092";
    public static Integer MESSAGE_COUNT=1000;
    public static String CLIENT_ID="client1";
    public static String TOPIC_NAME="demo";
    public static String GROUP_ID_CONFIG="consumerGroup1";
    public static Integer MAX_NO_MESSAGE_FOUND_COUNT=100;
    public static String OFFSET_RESET_LATEST="latest";
    public static String OFFSET_RESET_EARLIER="earliest";
    public static Integer MAX_POLL_RECORDS=1000;
    public static String ACK="all";
    public static Integer RETRIES=0;
}
