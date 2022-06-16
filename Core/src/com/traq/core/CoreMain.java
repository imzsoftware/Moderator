package com.traq.core;


import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.net.httpserver.HttpServer;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.base.RequestID;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.beanloader.LoadedBeans;
import com.traq.common.data.model.dao.*;
import com.traq.common.encryption.CryptoJsCryptor;
import com.traq.common.exceptions.*;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.processor.ConfigureProcessor;
import com.traq.common.processor.RequestProcessorBean;
import com.traq.common.validator.JsonValidator;
import com.traq.common.xmlutils.XMLProcessor;
import com.traq.config.CoreConfig;
import com.traq.logger.TraqLog;
import com.traq.util.HeartBeat;
import com.traq.util.RequestBean;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URI;


@Path(value = "/traqmatix")
public class CoreMain extends BaseInitializer {

    public CoreMain() {
        super("CORE_LOGGER");
    }

    static CoreConfig coreConfig = null;
    private ResponseMessage rm;


    @POST
    @Produces(value = {"application/xml"})
    @Consumes(MediaType.APPLICATION_XML)
    @Path(value = "api-1.0/xml/request/")
    public String xmlRequest(InputStream incomingData) {
        StringBuilder userRequest = new StringBuilder();
        String response = "";
        try {
            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(incomingData));
            String line;
            while ((line = reader.readLine()) != null) {
                userRequest.append(line);
            }
            String req = userRequest.toString();
            String vendorCode = TagValues.getNodeValue(req, Constants.NODEVENDORCODE);
            String messageType = TagValues.getNodeValue(req, Constants.NODEREQUESTTYPE);

            String transId = RequestID.next();
            req = XMLProcessor.replaceInsertXML(req, Constants.NODETRANSID, transId);
            Thread.currentThread().setName(transId);

            if (messageType.trim().compareToIgnoreCase("UPDATELOC") == 0) {
                hideInfo("User Request........... " + req);
            } else {
                info("User Request........... " + req);
            }

            RequestProcessorBean requestProcessorBean = null;
            requestProcessorBean = new ConfigureProcessor(vendorCode, messageType).getProcessorBean();
            RequestBean rb = null;
            try {
                String ipAddress = TagValues.getNodeValue(userRequest.toString(), Constants.NODEIPADDRESS);
                if (ipAddress.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<traq><response><responsetype>").append(messageType)
                            .append("</requesttype><ipaddress></ipaddress><vendorcode>").append(vendorCode)
                            .append("</vendorcode><resultcode>").append(ResultCodeExceptionInterface._FAIL)
                            .append("</resultcode><resultdescription>IP Address can not be blank</resultdescription><responsevalue>")
                            .append("IP Address can not be blank</responsevalue></response></traq>");
                    response = sb.toString();
                } else {
                    rb = new RequestBean(req, messageType, vendorCode);
                    response = requestProcessorBean.getRequestProcessor().executeXML(rb);
                }
            } catch (EntityException e) {
                System.out.println(e.getResultCode(0));
                System.out.println(e.getDescription());
                throw e;
            } catch (CommonException e) {
                System.out.println(e.getResultCode(0));
                System.out.println(e.getDescription());
                throw e;
            } catch (InterruptedException e) {
                //System.out.println(e.getMessage());
                throw e;
            } finally {
                rb = null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            error("Core Exception");
        }
        String finalResponse = XMLProcessor.removeJunkCharacters(response);
        info("CoreMain..Final response..................." + finalResponse);
        return finalResponse;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @Path(value = "api-1.0/json/request/{req}")
    public String jsonGetRequest(@PathParam("req") String req) {

        String response = "";
        try {
            String transId = RequestID.next();
            Thread.currentThread().setName(transId);

            info("User Request........... " + req);

            JSONObject request = new JSONObject(req);
            //JSONObject request = (JSONObject) jsonObject.get("request");

            JsonValidator validator = new JsonValidator();

            boolean isValid = validator.validate(request.toString());
            if (!isValid) {
                int resultCode = ResultCodeExceptionInterface._INVALID_REQUEST;
                response = invalidRequest(resultCode, "");
            } else {

                String vendorCode = request.getString(Constants.NODEVENDORCODE);
                String messageType = request.getString(Constants.NODEREQUESTTYPE);

                RequestProcessorBean requestProcessorBean = null;
                requestProcessorBean = new ConfigureProcessor(vendorCode, messageType).getProcessorBean();
                RequestBean rb = null;
                try {
                    rb = new RequestBean(req, messageType, vendorCode);
                    response = requestProcessorBean.getRequestProcessor().executeJSON(rb);
                } catch (EntityException e) {
                    System.out.println(e.getResultCode(0));
                    System.out.println(e.getDescription());
                    throw e;
                } catch (CommonException e) {
                    System.out.println(e.getResultCode(0));
                    System.out.println(e.getDescription());
                    throw e;
                } catch (InterruptedException e) {
                    //System.out.println(e.getMessage());
                    throw e;
                } finally {
                    rb = null;
                }
            }

        } catch (JSONException je) {
            int resultCode = ResultCodeExceptionInterface._INVALID_REQUEST;
            response = invalidRequest(resultCode, "");
        } catch (InvalidVendorCode ce) {
            int resultCode = ResultCodeExceptionInterface._VENDOR_NOT_FOUND;
            ce.printStackTrace();

        } catch (CoreException ce) {
            int resultCode = ResultCodeExceptionInterface._TECHNICAL_FAILURE;
            ce.printStackTrace();

        } catch (Exception ex) {
            int resultCode = ResultCodeExceptionInterface._INVALID_REQUEST;
            response = invalidRequest(resultCode, "");
            error("Core Exception");
        }
        //String finalResponse = XMLProcessor.removeJunkCharacters(response);
        //debug("CoreMain..Final response..................."+finalResponse);

        return response;
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    //@Consumes(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(value = "api-1.0/json/request/")
    public Response jsonRequest(InputStream incomingData) {

        String response = "";
        String messageType = "";
        BufferedReader reader = null;
        try {
            StringBuffer userRequest = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(incomingData));
            String line;
            while ((line = reader.readLine()) != null) {
                userRequest.append(line);
            }
            String transId = RequestID.next();
            Thread.currentThread().setName(transId);
            String req = userRequest.toString();


            JSONObject request = new JSONObject(req);

            messageType = request.getString(Constants.NODEREQUESTTYPE);

            if (messageType.trim().compareToIgnoreCase("UPDATELOC") == 0) {
                hideInfo("User Request........... " + req);
            } else {
                info("User Request........... " + req);
            }

            JSONObject object = (JSONObject) request.get("request");

            JsonValidator validator = new JsonValidator();

            boolean isValid = true;

            if (!isValid) {
                int resultCode = ResultCodeExceptionInterface._INVALID_REQUEST;
                response = invalidRequest(resultCode, "");
            } else {
//                messageType = request.getString(Constants.NODEREQUESTTYPE);
                String vendorCode = request.getString(Constants.NODEVENDORCODE);
/*                if(!"TP".equalsIgnoreCase(vendorCode) && !"GENRATEOTP".equals(messageType) && !Constants.OTPLOGIN.equals(messageType) && !Constants.LOGIN.equals(messageType)){
                    SessionValidateDao sessionValidateDao =  (SessionValidateDao) ApplicationBeanContext.getInstance().getBean("sessionValidateDao");
                    isValid = sessionValidateDao.validate(object.getString(Constants.NODEUSERNAME));
                }*/
                if (isValid) {
                    RequestProcessorBean requestProcessorBean = null;
                    requestProcessorBean = new ConfigureProcessor(vendorCode, messageType).getProcessorBean();
                    RequestBean rb = null;
                    try {
                        rb = new RequestBean(req, messageType, vendorCode, "", transId);
                        response = requestProcessorBean.getRequestProcessor().executeJSON(rb);

                    } catch (EntityException e) {
                        error(e.getResultCode(0) + " : " + e.getDescription());
                        throw e;
                    } catch (CommonException e) {
                        error(e.getResultCode(0) + " : " + e.getDescription());
                        throw e;
                    } catch (InterruptedException e) {
                        error(e.getMessage());
                        throw e;
                    } finally {
                        rb = null;
                    }
                } else {
                    int resultCode = ResultCodeExceptionInterface._SESSION_EXPIRED;
                    response = invalidRequest(resultCode, "");
                }
            }
        } catch (EntityException ee) {
            response = invalidRequest(ee.getResultCode(0), messageType, ee.getMessage());
/*            responseMessage.setResultcode(ee.getResultCode(Constants.Core));
            responseMessage.setResultDescription(ee.getDescription());
            responseMessage.setTimestamp(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));*/
            ee.printStackTrace();
            error("EntityException...." + response);
        } catch (JSONException je) {
            int resultCode = ResultCodeExceptionInterface._INVALID_REQUEST;
            response = invalidRequest(resultCode, messageType, je.getMessage());
            je.printStackTrace();
        } catch (InvalidVendorCode ce) {
            response = invalidRequest(ResultCodeExceptionInterface._VENDOR_NOT_FOUND, messageType, ce.getMessage());
            ce.printStackTrace();

        } catch (CoreException ce) {
            ce.printStackTrace();
            response = invalidRequest(ce.getResultCode(Constants.Core), messageType, ce.getMessage());

        } catch (Exception ex) {
            int resultCode = ResultCodeExceptionInterface._INVALID_REQUEST;
            response = invalidRequest(resultCode, messageType, ex.getMessage());
            error("Core Exception...." + response);
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                reader = null;
            }
            incomingData = null;
        }
        //String finalResponse = XMLProcessor.removeJunkCharacters(response);
        //String finalResponse = response;
        //info("CoreMain..Final response..................."+response);
        return Response.ok(response)
                .header("Access-Control-Allow-Origin", "*")
                .build();

        //return finalResponse;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    //@Consumes(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path(value = "api-2.0/json/request/")
    public Response jsonEncryptedRequest(InputStream incomingData) {

        String response = "";
        String messageType = "";
        BufferedReader reader = null;
        try {
            StringBuffer userRequest = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(incomingData));
            String line;
            while ((line = reader.readLine()) != null) {
                userRequest.append(line);
            }
            String transId = RequestID.next();
            Thread.currentThread().setName(transId);
            String req = userRequest.toString();

            info("User Request........... " + req);

            JSONObject request = new JSONObject(req);

            String encryptedRequest = request.get("request").toString();

/*            if (coreConfig.getEncryptionEnable().equals("1")) {
                String decryptedString = new CryptoJsCryptor(coreConfig.getCryptorSecretKey(), encryptedRequest.trim()).decrypt();
                request.put("request", new JSONObject(decryptedString.trim()));
                req = request.toString();
            }*/

            JsonValidator validator = new JsonValidator();

            boolean isValid = true;

            if (!isValid) {
                int resultCode = ResultCodeExceptionInterface._INVALID_REQUEST;
                response = invalidRequest(resultCode, "");
            } else {

                messageType = request.getString(Constants.NODEREQUESTTYPE);
                String vendorCode = request.getString(Constants.NODEVENDORCODE);
/*                if(!"TP".equalsIgnoreCase(vendorCode) && !"GENRATEOTP".equals(messageType) && !Constants.OTPLOGIN.equals(messageType) && !Constants.LOGIN.equals(messageType)){
                    SessionValidateDao sessionValidateDao =  (SessionValidateDao) ApplicationBeanContext.getInstance().getBean("sessionValidateDao");
                    isValid = sessionValidateDao.validate(object.getString(Constants.NODEUSERNAME));
                }*/
                if (isValid) {
                    RequestProcessorBean requestProcessorBean = null;
                    requestProcessorBean = new ConfigureProcessor(vendorCode, messageType).getProcessorBean();
                    RequestBean rb = null;
                    try {
                        rb = new RequestBean(req, messageType, vendorCode, "", transId);
                        response = requestProcessorBean.getRequestProcessor().executeJSON(rb);

                    } catch (EntityException e) {
                        error(e.getResultCode(0) + " : " + e.getDescription());
                        throw e;
                    } catch (CommonException e) {
                        error(e.getResultCode(0) + " : " + e.getDescription());
                        throw e;
                    } catch (InterruptedException e) {
                        error(e.getMessage());
                        throw e;
                    } finally {
                        rb = null;
                    }
                } else {
                    int resultCode = ResultCodeExceptionInterface._SESSION_EXPIRED;
                    response = invalidRequest(resultCode, "");
                }
            }
        } catch (EntityException ee) {
            response = invalidRequest(ee.getResultCode(0), messageType, ee.getMessage());
/*            responseMessage.setResultcode(ee.getResultCode(Constants.Core));
            responseMessage.setResultDescription(ee.getDescription());
            responseMessage.setTimestamp(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));*/
            ee.printStackTrace();
            error("EntityException...." + response);
        } catch (JSONException je) {
            int resultCode = ResultCodeExceptionInterface._INVALID_REQUEST;
            response = invalidRequest(resultCode, messageType, je.getMessage());
            je.printStackTrace();
        } catch (InvalidVendorCode ce) {
            response = invalidRequest(ResultCodeExceptionInterface._VENDOR_NOT_FOUND, messageType, ce.getMessage());
            ce.printStackTrace();

        } catch (CoreException ce) {
            ce.printStackTrace();
            response = invalidRequest(ce.getResultCode(Constants.Core), messageType, ce.getMessage());

        } catch (Exception ex) {
            int resultCode = ResultCodeExceptionInterface._INVALID_REQUEST;
            response = invalidRequest(resultCode, messageType, ex.getMessage());
            error("Core Exception...." + response);
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                reader = null;
            }
            incomingData = null;
        }
        //String finalResponse = XMLProcessor.removeJunkCharacters(response);
        //String finalResponse = response;
        //info("CoreMain..Final response..................."+response);
        if (coreConfig.getEncryptionEnable().equals("1")) {
            response = new CryptoJsCryptor(coreConfig.getCryptorSecretKey(), response.trim()).encrypt();
        }

        return Response.ok(response)
                .header("Access-Control-Allow-Origin", "*")
                .build();

        //return finalResponse;
    }

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

    private String invalidRequest(EntityException exp, String messageType) {

        JSONObject jsonObject = new JSONObject();
        JSONObject mainObj = new JSONObject();
        try {
            jsonObject.put(Constants.NODERESPONSETYPE, messageType);
            jsonObject.put(Constants.NODERESULTCODE, exp.getResultCode(0));
            jsonObject.put(Constants.NODERESULTDESCRIPTION, exp.getDescription());
            jsonObject.put(Constants.NODEMESSAGE, exp.getMessage().replaceAll("\"", ""));
            mainObj.put(Constants.NODERESPONSE, jsonObject);
        } catch (JSONException je) {

        }

        return mainObj.toString();
    }

    public static void main(String args[]) throws IOException {
        CoreMain coreMain = new CoreMain();
        coreMain.startAll();
        //coreMain.startAllSSL();
    }

    // Start with SSL
   /* public void startAllSSL() {
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
                setAppConfig(LoadedBeans.getCoreConfiguration());

                initbean.redisPoolInitialized();

                coreConfig = getAppConfig();
                int port = coreConfig.getPort();
               // info("port...."+port);
                String host = coreConfig.getHost();
                //info("Host..."+host);
                ResourceConfig rc = new PackagesResourceConfig("com.traq.core");
                if(getAppConfig().getDisableWADL() == 1){
                    rc.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, true);
                }else{
                    rc.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, false);
                }

                rc.getProperties().put(
                        "com.sun.jersey.spi.container.ContainerRequestFilters",
                        "com.sun.jersey.api.container.filter.LoggingFilter"
                );
                SSLContextConfigurator sslCon=new SSLContextConfigurator();
*//*
                sslCon.setKeyStoreFile(ConfigLoader.getKeystoreLocation()); // contains server keypair
                sslCon.setKeyStorePass(ConfigLoader.getKeystorePassword());
                *//*
                sslCon.setKeyStoreFile("opt/traqmatix/tracking/core/core_traq/bsnleci.jks"); // contains server keypair
                sslCon.setKeyStorePass("msdtrak".toCharArray());

                rc.getFeatures().put(SSLContextConfigurator.KEY_STORE_FILE, true);
                rc.getFeatures().put(SSLContextConfigurator.KEY_STORE_PASSWORD, true);
                //System.out.println("Starting server on port "+ConfigLoader.getHttpsServerPort());
                //HttpHandler httpHandler = new GrizzlyHttpContainerProvider().createContainer(HttpHandler.class, rc);
                *//*HttpServer secure= GrizzlyServerFactory.createHttpServer(getBaseURISecured(),
                        ContainerFactory.createContainer(HttpHandler.class, rc),
                        true,  new SSLEngineConfigurator(sslCon));
*//*
                HttpServer secure= GrizzlyServerFactory.createHttpServer(getBaseURISecured(),
                        rc);
            *//*    SSLEngineConfigurator context = new SSLEngineConfigurator(sslCon);
                HttpServer secure = createHttpServer(getBaseURISecured().toString(), rc, true, context.toString());
                *//*
                secure.start();

                info((new StringBuilder()).append("Visit: https://").append(host).append(":").append(port).append("/traqmatix/api-1.0/json/request/").toString());
                info("Hit return to stop...");

                loadMasterTables();

                //SelectorThread secure = GrizzlyServerFactory.createHttpServer(getBaseURISecured(), rc);

                //HttpServer secure =HttpServerFactory.create("https://localhost:" + port + "/", rc);
                //HttpServer secure = HttpServerFactory.create((new StringBuilder()).append("https://").append(host).append(":").append(port).append("/").toString(), rc);

*//*                info("RestService .................................... apply Basic Authentication.");
                rc.getProperties().put(
                        "com.sun.jersey.spi.container.ContainerRequestFilters",
                        "com.sun.jersey.api.container.filter.LoggingFilter;com.estel.rest.engine.AuthFilter"
                );
                server = HttpServerFactory.create("http://localhost:" + port + "/", rc);  //http://localhost:9998*//*//*

     *//*                HashSet<NetworkListener> lists=new HashSet<NetworkListener>(secure.getListeners());
                for (NetworkListener listener : lists){
                    listener.setSecure(true);
                    SSLEngineConfigurator ssle=new SSLEngineConfigurator(sslCon);
                    listener.setSSLEngineConfig(ssle);
                    secure.addListener(listener);
                    System.out.println(listener);
                }*//*            }

            if (isLoadBean) {
                HeartBeat hb = new HeartBeat(30*60000, logger);
            }

        }catch (DBConnectionFailureException dbf){
            error("DBConnectionFailureException......"+dbf.getDescription());
            System.exit(0);
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
*/
/*
    public HttpServer createHttpServer(String url, ResourceConfig rc,
                                       boolean secure, String contextPath) throws Exception {
        // HttpServer result = GrizzlyServerFactory.createHttpServer(url, rc);
        // http://grepcode.com/file/repo1.maven.org/maven2/com.sun.jersey/jersey-grizzly2/1.6/com/sun/jersey/api/container/grizzly2/GrizzlyServerFactory.java#GrizzlyServerFactory.createHttpServer%28java.net.URI%2Ccom.sun.jersey.api.container.grizzly2.ResourceConfig%29
        HttpServer result = new HttpServer();
        final NetworkListener listener = new NetworkListener("grizzly",
                coreConfig.getHost(), coreConfig.getPort());
        result.addListener(listener);
        // do we need SSL?
        if (secure) {
            listener.setSecure(secure);
            SSLEngineConfigurator sslEngineConfigurator = createSSLConfig(true);
            listener.setSSLEngineConfig(sslEngineConfigurator);
        }
        // Map the path to the processor.
        final ServerConfiguration config = result.getServerConfiguration();
        final HttpHandler handler = ContainerFactory.createContainer(
                HttpHandler.class, rc);
        config.addHttpHandler(handler);
        return result;
    }
*/

    /**
     * create SSL Configuration
     *
     * @param //isServer true if this is for the server
     * @return
     * @throws Exception
     */
 /*   private SSLEngineConfigurator createSSLConfig(boolean isServer)
            throws Exception {
        final SSLContextConfigurator sslContextConfigurator = new SSLContextConfigurator();
        // override system properties
        *//*final File cacerts = getStoreFile("server truststore",
                "truststore_server.jks");*//*
        //if (cacerts != null) {
            sslContextConfigurator.setTrustStoreFile("opt/traqmatix/tracking/core/core_traq/bsnleci.jks");
            sslContextConfigurator.setTrustStorePass("msdtrak");
        //}

        // override system properties
        *//*final File keystore = getStoreFile("server keystore", "keystore_server.jks");
        if (keystore != null) {*//*
            sslContextConfigurator.setKeyStoreFile("opt/traqmatix/tracking/core/core_traq/bsnleci.jks");
            sslContextConfigurator.setKeyStorePass("msdtrak");
        //}

        //
        boolean clientMode = false;
        // force client Authentication ...
        boolean needClientAuth = false;
        boolean wantClientAuth = false;
        SSLEngineConfigurator result = new SSLEngineConfigurator(
                sslContextConfigurator.createSSLContext(), clientMode, needClientAuth,
                wantClientAuth);
        return result;
    }*/
    private URI getBaseURISecured() {
        return UriBuilder.fromUri("https://" + coreConfig.getHost() + "/").port(coreConfig.getPort()).build();
    }

    //private static final URI BASE_URI_SECURED = getBaseURISecured();

    // Start Without SSL
    public void startAll() {
        TraqLog logger = TraqLog.getInstance("CORE_LOGGER");
        try {
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
                setAppConfig(LoadedBeans.getCoreConfiguration());

                initbean.redisPoolInitialized();
                // Create MogoDB connection
                //initbean.mongoDbConnection();
                // Set Operator in Config
                coreConfig = getAppConfig();
                String seperator = "/";
                int port = coreConfig.getPort();
                info("port...." + port);
                String host = coreConfig.getHost();
                info("Host..." + host);
                ResourceConfig traqResourceConfig = new PackagesResourceConfig("com.traq.core");
                //traqResourceConfig.getContainerResponseFilters().add(TraqCORSFilter.class);
                if (getAppConfig().getDisableWADL() == 1) {
                    traqResourceConfig.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, true);
                } else {
                    traqResourceConfig.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, false);
                }

                HttpServer server = HttpServerFactory.create((new StringBuilder()).append("http://").append(host).append(":").append(port).append(seperator).toString(), traqResourceConfig);
                server.start();
                info((new StringBuilder()).append("Visit: http://").append(host).append(":").append(port).append("/traqmatix/api-1.0/json/request/").toString());
                info("Hit return to stop...");

                loadMasterTables();
                System.in.read();
                //info("Stopping server");
            }

            if (isLoadBean) {
                HeartBeat hb = new HeartBeat(30 * 60000, logger);
            }

/*        }catch (DBConnectionFailureException dbf){
            error("DBConnectionFailureException......"+dbf.getDescription());
            System.exit(0);*/
        } catch (SocketException se) {
            error("SocketException......" + se);
            System.exit(0);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            info((new StringBuilder()).append("IOE.....").append(ioe).toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            error("CoreMain.Exception......" + ex.getMessage());
            System.exit(0);
        }
    }


    private void loadMasterTables() throws DBConnectionFailureException {
        try {
            StatusDao statusDao = (StatusDao) ApplicationBeanContext.getInstance().getBean("statusDao");
            setStatusMap(statusDao.findMap());

            DeviceTypeDao deviceTypeDao = (DeviceTypeDao) ApplicationBeanContext.getInstance().getBean("deviceTypeDao");
            setDeviceTypeMap(deviceTypeDao.findMap());

            ManufacturerDao mfgDao = (ManufacturerDao) ApplicationBeanContext.getInstance().getBean("mfgDao");
            setMfgMap(mfgDao.findMap());

            SmsPackDao packDao = (SmsPackDao) ApplicationBeanContext.getInstance().getBean("smsPackDao");
            setSmsPackMap(packDao.findMap());

            ZoneDao zoneDao = (ZoneDao) ApplicationBeanContext.getInstance().getBean("zoneDao");
            setZoneMap(zoneDao.findMap());

            PermissionDao permissionDao = (PermissionDao) ApplicationBeanContext.getInstance().getBean("permissionDao");
            setServicesMap(permissionDao.findMap());

            AccountTypeDao accountTypeDao = (AccountTypeDao) ApplicationBeanContext.getInstance().getBean("accountTypeDao");
            setAccountTypeMap(accountTypeDao.findMap());

            AssetTypeDao assetTypeDao = (AssetTypeDao) ApplicationBeanContext.getInstance().getBean("assetTypeDao");
            setAssetTypeMap(assetTypeDao.findMap());

            VehicleDao vehicleDao = (VehicleDao) ApplicationBeanContext.getInstance().getBean("vehicleDao");
            setVehicleMap(vehicleDao.findMap(true));

            BrandDao brandDao = (BrandDao) ApplicationBeanContext.getInstance().getBean("brandDao");
            setBrandMap(brandDao.findMap());

            RoleDao roleDao = (RoleDao) ApplicationBeanContext.getInstance().getBean("roleDao");
            setRoleMap(roleDao.findMap());

        /*DriverDao driverDao =  (DriverDao) ApplicationBeanContext.getInstance().getBean("driverDao");
        setDriverMap(driverDao.findMap());*/

            VehicleTypeDao vehicleTypeDao = (VehicleTypeDao) ApplicationBeanContext.getInstance().getBean("vehicleTypeDao");
            setVehicleTypeMap(vehicleTypeDao.findMap());

            AccountDao accountDao = (AccountDao) ApplicationBeanContext.getInstance().getBean("accountDao");
            setAccountMap(accountDao.findMap());

            ClientDao clientDao = (ClientDao) ApplicationBeanContext.getInstance().getBean("clientDao");
            setClientMap(clientDao.findMap(false));

            AlertTypeDao alertTypeDao = (AlertTypeDao) ApplicationBeanContext.getInstance().getBean("alertTypeDao");
            setAlertTypeMap(alertTypeDao.findMap());

            ExciseTransporterDao exciseTransporterDao = (ExciseTransporterDao) ApplicationBeanContext.getInstance().getBean("exciseTransporterDao");
            setTransporterMap(exciseTransporterDao.findMap(false));

            MaterialDao materialDao = (MaterialDao) ApplicationBeanContext.getInstance().getBean("materialDao");
            setMaterialMap(materialDao.findMap(false));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
