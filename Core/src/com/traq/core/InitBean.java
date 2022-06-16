package com.traq.core;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.cluster.ClusterClientOptions;
import com.lambdaworks.redis.cluster.ClusterTopologyRefreshOptions;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.support.ConnectionPoolSupport;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.traq.common.base.BaseInitializer;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.beanloader.ContextFactory;
import com.traq.common.beanloader.LoadedBeans;
import com.traq.common.data.factory.BeanFactoryManager;
import com.traq.config.CoreConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.BeanFactory;

import java.sql.Connection;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by Amit on 30/6/17.
 */
public class InitBean extends BaseInitializer {

    RedisClusterClient client = null;
    RedisClient redisClient = null;

    public boolean getInitBean() {

        return initBean();
    }


    private Boolean initBean() {
        Boolean loadBeanFlag = true;
        Connection conn = null;
        try {
            info("Load ApplicationContext with 'core.xml'.....");
            BeanFactory beanFactory = BeanFactoryManager.manager("core.xml");
            ApplicationBeanContext factory = new ApplicationBeanContext();
            factory.setAppContext(beanFactory);
            ContextFactory.init(factory);

            info("Loading Complete.....");
            // check connection and Bean
            CoreConfig coreConfig = LoadedBeans.getCoreConfiguration();
            setAppConfig(coreConfig);

/*            if(coreConfig != null){
                ConnectionPool connectionPool = new ConnectionPool(coreConfig);
                conn = connectionPool.getConnectionFromPool();
            }
            if (conn != null) {
                info("DB Connection.....: " + conn);
                conn.close();
            }*/

/*        } catch (SQLException e) {
            loadBeanFlag = false;
            stack2string(e);
            error("InitBean..........................[SQLException]", e);*/
        }finally{
            conn = null;
        }
        return loadBeanFlag;
    }

    public void redisPoolInitialized(){
        try {
            client = RedisClusterClient.create(Arrays.asList(RedisURI.create(getAppConfig().getRedisHost(), getAppConfig().getRedisPort())));
            ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder().enablePeriodicRefresh().refreshPeriod(10, TimeUnit.MINUTES).enableAllAdaptiveRefreshTriggers().build();
            client.setOptions(ClusterClientOptions.builder().validateClusterNodeMembership(false).topologyRefreshOptions(topologyRefreshOptions).build());

            /**
            ** Adding for Falcon
            **/
            redisClient = RedisClient.create(RedisURI.create(getAppConfig().getRedisHost(), getAppConfig().getRedisPort()));


            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMinIdle(getAppConfig().getRedisMinIdle());
            poolConfig.setMaxIdle(getAppConfig().getRedisMaxIdle());
            poolConfig.setMaxTotal(getAppConfig().getRedisMaxSize());

            info("poolConfig.."+poolConfig);
            setRedisCluster(ConnectionPoolSupport.createGenericObjectPool(() -> client.connect(), poolConfig));

            setRedisPool(ConnectionPoolSupport.createGenericObjectPool(() -> redisClient.connect(), poolConfig));
        }
        catch(Exception e){
            info("EXCEPTION IN InitBean redisPool : " + e.getMessage());
            e.printStackTrace();
        }

    }


    // Creating a Mongo client
    public void mongoDbConnection(){

        String DB_SRV_USR = getAppConfig().getMongoUser();
        String DB_SRV_PWD = getAppConfig().getMongoPassword();

        int DB_PORT = getAppConfig().getMongoPort();

        //connectionPoolListener.
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        //builder.minConnectionsPerHost(2);
        //builder.connectionsPerHost(20);

        //build the connection options
        builder.minConnectionsPerHost(getAppConfig().getMongoPool());//set the max wait time in (ms)
        builder.maxConnectionIdleTime(60000);//set the max wait time in (ms)
        //builder.socketTimeout(120000);//set the max wait time in (ms)
        MongoClientOptions opts = builder.build();

        MongoCredential credential = null;
        credential = MongoCredential.createCredential(DB_SRV_USR, getAppConfig().getMongoDB(),
                DB_SRV_PWD.toCharArray());

        MongoClient mongo = new MongoClient(new ServerAddress(getAppConfig().getMongoHost()), credential, opts);


        //System.out.println("Connected to the database successfully") ;
        // Accessing the admin
        //setMongoDatabase(mongo.getDatabase(getAppConfig().getMongoDB()));

    }

}

