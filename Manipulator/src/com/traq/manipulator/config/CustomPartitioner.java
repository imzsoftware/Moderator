package com.traq.manipulator.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

/**
 * Created by Amit on 30/5/19.
 */
public class CustomPartitioner implements Partitioner{
    private static final int PARTITION_COUNT=1000;
    private static Map <String, Integer> partitionMap = null;
    @Override
    public void configure(Map<String, ?> configs) {
        System.out.println("Inside CountryPartitioner.configure " + configs);
        partitionMap = new HashMap<String, Integer>();
        for(Map.Entry<String,?> entry: configs.entrySet()){
            if(entry.getKey().startsWith("partitions.")){
                String keyName = entry.getKey();
                String value = (String)entry.getValue();
                System.out.println( keyName.substring(11));
                int paritionId = Integer.parseInt(keyName.substring(11));
                partitionMap.put(value,paritionId);
            }
        }
    }
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
/*        Integer keyInt=Integer.parseInt(key.toString());
        return keyInt % PARTITION_COUNT;*/

        String topicName = ((String) value).split(":")[0];
        if(partitionMap.containsKey(topicName)){
            //If the country is mapped to particular partition return it
            return partitionMap.get(topicName);
        }else {
            //If no country is mapped to particular partition distribute between remaining partitions
            int noOfPartitions = cluster.topics().size();
            return  value.hashCode()%noOfPartitions + partitionMap.size() ;
        }
    }
    @Override
    public void close() {
    }
}
