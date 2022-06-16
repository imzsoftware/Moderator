package com.imz.praj.processor.utility;

import com.imz.praj.data.impl.AlertDataDaoImpl;
import com.imz.praj.data.obj.PrajReportData;
import com.lambdaworks.redis.GeoArgs;
import com.lambdaworks.redis.GeoWithin;
import com.lambdaworks.redis.cluster.api.StatefulRedisClusterConnection;
import com.lambdaworks.redis.cluster.api.sync.RedisAdvancedClusterCommands;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.User;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.exceptions.EntityException;
import com.traq.common.exceptions.MandatoryValuesNull;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.ReportHandler;
import com.traq.common.handler.RequestHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.db.ManageRedisConnection;
import com.traq.util.RequestBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemoveGarbageAddress extends BaseInitializer  implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    private RequestMessage requestMessage;
    private String request;
    SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    RequestBean rb;
    User user = null;

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public RemoveGarbageAddress(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public RemoveGarbageAddress() {}

    public String executeXML(RequestBean _rb) {
        return this.request;
    }


    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();
        RequestHandler rh = new RequestHandler();
        String response = "";
        UserDao userDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");

        try {
            responseMessage = new ResponseMessage();
            JSONObject object = new JSONObject(this.request);
            requestMessage = rh.getRequest(object.getJSONObject("request"), TagValues.getNodeValue(object, "requesttype"));

            user = userDao.login(requestMessage.getUsername());
            boolean isValid = validateUser(user, requestMessage.getPin());
            if (isValid) {
                List<GeoWithin<String>> addList = geoRadiusAddress(requestMessage.getLongitude(),requestMessage.getLatitude());
                for(GeoWithin add : addList){
                    String address = (String)add.getMember();
                    info("zrem .................. address = "+address);
                    if(address.length() > 100){
                        Long result = zrem(address);
                        info("zrem .................. result = "+result);
                    }
                }
                responseMessage.setMessagetype(requestMessage.getMessagetype());
                responseMessage.setResponsects(newPattern.format(new Date()));
                responseMessage.setResultcode(Integer.valueOf(0));
                responseMessage.setResultDescription("Success");

            } else {
                responseMessage.setResultcode(Integer.valueOf(11));
                responseMessage.setResultDescription("User Not Found");
            }
        } catch (EntityException ee) {
            responseMessage.setResultcode(Integer.valueOf(24));
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(24)));
        }
        catch (Exception ex) {
            error("RemoveGarbageAddress ......... ex "+ex.getMessage());
            responseMessage.setResultcode(Integer.valueOf(206));
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            ex.printStackTrace();
        } finally {
            if (responseMessage.getResultcode().intValue() == 0) {
                response = generateJSONResponse();
            } else {
                response = generateFailureResponse(responseMessage);
            }
        }
        return response;
    }

    public String generateJSONResponse() {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = responseHeader(this.requestMessage, this.responseMessage);
            jsonObject.put(Constants.NODESTARTDATE, requestMessage.getStartDate());
            jsonObject.put(Constants.NODEENDDATE, requestMessage.getEndDate());
            mainObj.put("response", jsonObject);
        }
        catch (JSONException je) {
            je.printStackTrace();
        } catch (Exception exception) {}
        return mainObj.toString();
    }

    public String generateFailureResponse(ResponseMessage rm) {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("responsetype", rm.getMessagetype());
            jsonObject.put("resultcode", rm.getResultcode());
            jsonObject.put("resultdescription", rm.getResultDescription());

            mainObj.put("response", jsonObject);
        } catch (JSONException jSONException) {}


        return mainObj.toString();
    }

    public List<GeoWithin<String>> geoRadiusAddress(Double lng, Double lat) {
        StatefulRedisClusterConnection<String, String> connection = null;
        List<GeoWithin<String>> list = null;
        ManageRedisConnection redisConnection = null;
        try {
            connection = (StatefulRedisClusterConnection<String, String>) getRedisCluster().borrowObject();
            GeoArgs args = new GeoArgs();
            //args.withCount(1L);
            RedisAdvancedClusterCommands redisAdvancedClusterCommands = connection.sync();
            list = redisAdvancedClusterCommands.georadius("geocode", lng.doubleValue(), lat.doubleValue(), getAppConfig().getNearestLocationRadius().intValue() / 1.0D, GeoArgs.Unit.m, args.withDistance().asc());
        } catch (Exception ex) {
            error(ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                connection.close();
                connection = null;
            } catch (Exception e) {
                connection = null;
            }
        }
        return list;
    }

}
