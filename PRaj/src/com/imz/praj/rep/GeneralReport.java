package com.imz.praj.rep;

import com.imz.praj.data.impl.PrajAlertLogDaoImpl;
import com.imz.praj.data.impl.RedisToMongoDaoImpl;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.*;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.exceptions.*;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.RequestHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class GeneralReport extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    private List<LiveTrack> liveTrackListprimary = null;
    private RequestMessage requestMessage;
    private String request;

    RequestBean rb;

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public GeneralReport(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public GeneralReport() {}

    public String executeXML(RequestBean _rb) {
        return this.request;
    }

    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();

        RequestHandler rh = new RequestHandler();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat redisKeyFormat = new SimpleDateFormat("yyyy-MM-dd");
        String response = "";

        try {
            JSONObject object = new JSONObject(this.request);
            JSONObject reqObj = object.getJSONObject("request");
            requestMessage = rh.getRequest(reqObj, TagValues.getNodeValue(object, "requesttype"));
            requestMessage.setVendorcode(TagValues.getNodeValue(object, Constants.NODEVENDORCODE));
            requestMessage.setVendorcode(TagValues.getNodeValue(object, Constants.NODEVENDORCODE));
            requestMessage.setUsedDate(TagValues.getNodeValue(reqObj, Constants.NODEDATE));

            responseMessage = new ResponseMessage();
            responseMessage.setMessagetype(requestMessage.getMessagetype());
            responseMessage.setResponsects(sdf.format(new Date()));
            responseMessage.setTransid(rb.getTransid());

            UserDao userDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");
            User user = userDao.login(requestMessage.getUsername());

            //boolean isValid = validateUser(user, requestMessage.getPin());
            boolean isValid = true;

            if (isValid) {
                Calendar cal = Calendar.getInstance();
                if(checkNullAndEmpty(requestMessage.getUsedDate())){
                    String[] dt = requestMessage.getUsedDate().split("-");
                    cal.set(Calendar.DATE,Integer.parseInt(dt[0]));
                    cal.set(Calendar.MONTH,Integer.parseInt(dt[1])-1);
                    cal.set(Calendar.YEAR,Integer.parseInt(dt[2]));
                }else{
                    cal.add(Calendar.DATE, -1);
                }
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);

                String repKey = requestMessage.getVendorcode()+"_REP_"+redisKeyFormat.format(cal.getTime());
                info("repKey........... "+repKey);
                Map<String, String> reportMap = hgetAll(repKey);

                if(reportMap != null && reportMap.size()>0){
                    RedisToMongoDaoImpl redisToMongoDao = new RedisToMongoDaoImpl();
                    redisToMongoDao.saveGeneralReport(reportMap,requestMessage.getVendorcode(),redisKeyFormat.format(cal.getTime()));

                    /* For Non Functional device & Schemes **/
                    PrajAlertLogDaoImpl alertLogDao = new PrajAlertLogDaoImpl();
                    AccountDao accountDao = (AccountDao)ApplicationBeanContext.getInstance().getBean("accountDao");
                    DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
                    List<Account> accounts = accountDao.findByClient("PRAJ");

                    Map<Long, Device> deviceMap = deviceDao.getModifiedDeviceMapByAcc(accounts);

                    for (Map.Entry<Long, Device> mapp : deviceMap.entrySet()) {
                        expireKey("EWA_"+mapp.getValue().getId());
                        String value = reportMap.get(mapp.getKey()+"");
                        if(!checkNullAndEmpty(value)) {
                            String devData = hget(mapp.getValue().getAccId()+"", mapp.getKey()+"");
                            alertLogDao.saveNonFuncAlert(mapp.getValue(),devData,cal);
                            redisToMongoDao.saveNonFuncReport(mapp.getValue(),devData,cal);
                        }else{
                            continue;
                        }
                    }
                    this.responseMessage.setResultcode(0);
                    this.responseMessage.setResultDescription("Success");
                    expireKey(repKey);
                }else{
                    this.responseMessage.setResultcode(ResultCodeExceptionInterface._RECORD_NOT_FOUND);
                    this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(ResultCodeExceptionInterface._RECORD_NOT_FOUND));
                }
            } else {
                if (!user.getPin().equals(this.requestMessage.getPin()))
                    throw new InvalidPassword();
                if (!user.getStatus().getCode().equals("A")) {
                    throw new InActiveUser();
                }
                throw new InvalidUser();
            }


        } catch (EntityException ee) {
            this.responseMessage.setResultcode(ee.getResultCode(Integer.valueOf(0)));
            this.responseMessage.setResultDescription(ee.getDescription());
            this.responseMessage.setTimestamp((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(new Date()));
        }
        catch (Exception ex) {
            this.responseMessage.setResultcode(Integer.valueOf(206));
            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            ex.printStackTrace();
        } finally {
            if (responseMessage.getResultcode() == 0) {
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
            JSONObject jsonObject = responseHeader(requestMessage, this.responseMessage);
            jsonObject.put("transid", responseMessage.getTransid());
            jsonObject.put(Constants.NODEUSERNAME, requestMessage.getUsername());
            jsonObject.put(Constants.NODERESPONSETYPE, requestMessage.getMessagetype());
            jsonObject.put(Constants.NODERESULTCODE, responseMessage.getResultcode());
            jsonObject.put(Constants.NODERESULTDESC, responseMessage.getResultDescription());

            mainObj.put("response", jsonObject);
        }
        catch (JSONException je) {
            je.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mainObj.toString();
    }

    public String generateFailureResponse(ResponseMessage rm) {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("responsetype", rm.getMessagetype());
            jsonObject.put("resultcode", rm.getResultcode());
            jsonObject.put("resultdescription", rm.getResultDescription());
            jsonObject.put("transid", rm.getTransid());

            mainObj.put("response", jsonObject);
        } catch (JSONException jSONException) {}
        return mainObj.toString();
    }

}
