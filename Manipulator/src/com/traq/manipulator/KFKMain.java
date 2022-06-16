package com.traq.manipulator;


import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.net.httpserver.HttpServer;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.base.RequestID;
import com.traq.common.exceptions.*;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.config.CoreConfig;
import com.traq.logger.TraqLog;
import com.traq.manipulator.bots.ProducerTask;
import com.traq.manipulator.config.ProducerCreator;
import com.traq.util.HeartBeat;
import org.apache.kafka.clients.producer.Producer;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


@Path(value="/traqmatix/kfk")
public class KFKMain extends BaseInitializer {

    public KFKMain()
    {
        super("CORE_LOGGER");
    }
    static CoreConfig coreConfig = null;
    private ResponseMessage rm;
    static Producer<Long, String> producer = null;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @Path(value="producer/{req}")
    public String jsonGetRequest(@PathParam("req") String request){

        String response = "";
        try
        {
            String transId = RequestID.next();
            Thread.currentThread().setName(transId);

            System.out.println("User Request (Get Method)........... "+request);

            //JSONObject request = (JSONObject) jsonObject.get("request");

            //JsonValidator validator = new JsonValidator();

            //boolean isValid = validator.validate(request.toString());
            //String vendorCode = TagValues.getNodeValue(request, Constants.NODEVENDORCODE);
            //String messageType = TagValues.getNodeValue(request,Constants.NODEREQUESTTYPE);

            Future<String> future = null;
            ExecutorService executor = null;

            executor = Executors.newSingleThreadExecutor();
            try{
                ProducerTask task = new ProducerTask(request, transId, "TM");
                future = executor.submit(task);

                String resp = future.get(20000, TimeUnit.MILLISECONDS);
                System.out.println("resp........."+resp);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                executor.shutdown();
            }

        }catch(Exception ex)
        {
            int resultCode = ResultCodeExceptionInterface._INVALID_REQUEST;
            response = invalidRequest(resultCode, "");
            error("Core Exception");
        }
        //String finalResponse = XMLProcessor.removeJunkCharacters(response);
        //debug("CoreMain..Final response..................."+finalResponse);

        return response;
    }


    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @Path(value="prod/")
    public String jsonRequest(InputStream incomingData){

        String response = "Success";
        String messageType = "";
        BufferedReader reader = null;
        try
        {
            StringBuffer userRequest = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(incomingData));
            String line;
            while((line = reader.readLine()) != null){
                userRequest.append(line);
            }
            //info("KfkProducer.......Request="+userRequest);
            String transId = RequestID.next();
            Thread.currentThread().setName(transId);
            String req = userRequest.toString();
            KFKProducer prod = new KFKProducer();
            response = prod.producerRequest(req, transId);
            info("KfkProducer.......response="+response);

        }catch(Exception ex)
        {
            ex.printStackTrace();
            int resultCode = ResultCodeExceptionInterface._INVALID_REQUEST;
            response = invalidRequest(resultCode, messageType, ex.getMessage());
            error("ProducerMain Core Exception...."+response);
        }finally {
            try{
                reader.close();
            }catch (Exception e){
                reader = null;
            }
            incomingData = null;
        }
        /*return Response.ok(response)
                .header("Access-Control-Allow-Origin", "*")
                .build();*/

        return response;
    }

    private String invalidRequest(int resultCode, String messageType){

        JSONObject jsonObject = new JSONObject();
        JSONObject mainObj = new JSONObject();
        try{
            jsonObject.put(Constants.NODERESPONSETYPE, messageType);
            jsonObject.put(Constants.NODERESULTCODE, resultCode);
            jsonObject.put(Constants.NODERESULTDESCRIPTION, ResultCodeDescription.getDescription(resultCode));

            mainObj.put(Constants.NODERESPONSE, jsonObject);
        }catch (JSONException je){

        }

        return mainObj.toString();
    }

    private String invalidRequest(int resultCode, String messageType, String msg){

        JSONObject jsonObject = new JSONObject();
        JSONObject mainObj = new JSONObject();
        try{
            jsonObject.put(Constants.NODERESPONSETYPE, messageType);
            jsonObject.put(Constants.NODERESULTCODE, resultCode);
            jsonObject.put(Constants.NODERESULTDESCRIPTION, ResultCodeDescription.getDescription(resultCode));
            if(msg != null)
                jsonObject.put(Constants.NODEMESSAGE, msg.replaceAll("\"",""));

            mainObj.put(Constants.NODERESPONSE, jsonObject);
        }catch (JSONException je){

        }

        return mainObj.toString();
    }

    public static void main(String args[]) throws IOException {
        KFKMain coreMain = new KFKMain();
        coreMain.startAll();
    }

    public void startAll() {
        TraqLog logger = TraqLog.getInstance("CORE_LOGGER");
        try{
            Boolean isLoadBean = false;
            info("Bean Initialization starting.....");
            InitBean initbean = new InitBean();
            isLoadBean = initbean.getInitBean();

            if (!isLoadBean) {
                isLoadBean = initbean.getInitBean();
            }

            if (!isLoadBean) {
                isLoadBean = initbean.getInitBean();
            }
            if (isLoadBean) {
                //setAppConfig(LoadedBeans.getCoreConfiguration());

                initbean.redisPoolInitialized();
                // Create MogoDB connection
                initbean.mongoDbConnection();
                // Set Operator in Config
                coreConfig = getAppConfig();
                String seperator = "/";
                int port = coreConfig.getPort();
                info("port...."+port);
                String host = coreConfig.getHost();
                System.out.println("Host..."+host);
                ResourceConfig traqResourceConfig = new PackagesResourceConfig("com.traq.manipulator");
                if(getAppConfig().getDisableWADL() == 1){
                    traqResourceConfig.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, true);
                }else{
                    traqResourceConfig.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, false);
                }

                if(!checkNullAndEmpty(host)){
                    initbean.mongoDbConnection();
                    KFKConsumer consumer = new KFKConsumer();
                    consumer.startConsumersV2();
                }else if(port == 0){
                    initbean.mongoDbConnection();
                    KFKConsumer consumer = new KFKConsumer();
                    consumer.startConsumer(host.toUpperCase());
                }else{
                    HttpServer server = HttpServerFactory.create((new StringBuilder()).append("http://").append(host).append(":").append(port).append(seperator).toString(), traqResourceConfig);
                    server.start();
                    producer = ProducerCreator.createProducer(getAppConfig().getKfkBroker());
                    info((new StringBuilder()).append("Visit: http://").append(host).append(":").append(port).append("/traqmatix/kfk/prod/").toString());
                    info("Hit return to stop...");
                }

                System.in.read();
            }

            if (isLoadBean) {
                HeartBeat hb = new HeartBeat(10*60000, logger);
            }

        }catch(SocketException se)
        {
            error("SocketException......"+se);
            System.exit(0);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
            info((new StringBuilder()).append("IOE.....").append(ioe).toString());
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            error("CoreMain.Exception......"+ex.getMessage());
            System.exit(0);
        }
    }

}
