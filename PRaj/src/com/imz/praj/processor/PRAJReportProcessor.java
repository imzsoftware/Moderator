package com.imz.praj.processor;

import com.imz.praj.data.impl.AlertDataDaoImpl;
import com.imz.praj.data.obj.PrajReportData;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.User;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.exceptions.EntityException;
import com.traq.common.exceptions.MandatoryValuesNull;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.ReportHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PRAJReportProcessor  extends BaseInitializer  implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    private RequestMessage requestMessage;
    private String request;
    List<PrajReportData> alertlist = new ArrayList<>();
    Map <String, Long> totalQtyByDay = new ConcurrentHashMap<String, Long>();
    List<Account> accountList = null;


    SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Account account = null;
    RequestBean rb;
    User user = null;

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public PRAJReportProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public PRAJReportProcessor() {}

    public String executeXML(RequestBean _rb) {
        return this.request;
    }


    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();

        ReportHandler dh = new ReportHandler();
        String response = "";

        UserDao userDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");
        AccountDao accountDao = (AccountDao)ApplicationBeanContext.getInstance().getBean("accountDao");

        try {
            responseMessage = new ResponseMessage();
            JSONObject object = new JSONObject(this.request);
            requestMessage = dh.getRequest(object.getJSONObject("request"), TagValues.getNodeValue(object, "requesttype"));
            info("PRAJReportProcessor ..... requestMessage = "+requestMessage );
            requestMessage.setVendorcode(TagValues.getNodeValue(object, "vendorcode"));
            info("PRAJReportProcessor ..... requestMessage.getVendorcode() = "+requestMessage.getVendorcode() );

            if (!checkNullAndEmpty(requestMessage.getAssetids())) {
                throw new MandatoryValuesNull();
            }

            user = userDao.login(requestMessage.getUsername());
            if (!checkNullAndEmpty(requestMessage.getAccId())) {
                DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
                requestMessage.setAccId(deviceDao.getDeviceById(requestMessage.getAssetids().get(0)).getAccId());
                //throw new MandatoryValuesNull();
            }
            boolean isValid = validateUser(user, requestMessage.getPin());
            if (isValid) {
                if (checkNullAndEmpty(requestMessage.getAccId())) {
                    responseMessage.setMessagetype(requestMessage.getMessagetype());
                    Date date = newPattern.parse(requestMessage.getStartDate());
                    this.requestMessage.setStartDate(dbPattern.format(date));
                    date = newPattern.parse(requestMessage.getEndDate());
                    requestMessage.setEndDate(dbPattern.format(date));
                    responseMessage.setResponsects(newPattern.format(new Date()));
                    account = accountDao.getAccountById(requestMessage.getAccId());
                    accountList = new ArrayList<>();
                    accountList.add(account);

                    AlertDataDaoImpl alertDataDao = new AlertDataDaoImpl();
                    alertlist = alertDataDao.findAlerts(requestMessage, null, true);

                    if (alertlist == null || alertlist.size() == 0) {
                        throw new NoRecordFoundException();
                    }
                    responseMessage.setResultcode(Integer.valueOf(0));
                    responseMessage.setResultDescription("Success");
                } else {
                    responseMessage.setResultcode(Integer.valueOf(11));
                    responseMessage.setResultDescription("User Not Found");
                }
            } else {
                responseMessage.setResultcode(Integer.valueOf(11));
                responseMessage.setResultDescription("User Not Found");
            }
        } catch (NoRecordFoundException ex) {
            responseMessage.setResultcode(Integer.valueOf(36));
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(36)));
        } catch (EntityException ee) {
            responseMessage.setResultcode(Integer.valueOf(24));
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(24)));
        }
        catch (Exception ex) {
            error("PRAJReportProcess ......... ex "+ex.getMessage());
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

    private JSONArray refineData(List<PrajReportData> list){
        JSONArray array = new JSONArray();
        try {

            if (list != null){
                Long startTime = 0L;
                Long endTime = 0L;
                Long qty = 0L;
                String startDate = "";
                String endDate = "";
                String date = "";

                for(PrajReportData obj : list){
                    if("Y".equalsIgnoreCase(obj.getValue())){
                        startTime = obj.getOrgMillis();
                        startDate = obj.getCts();
                        try {
                            date = dbPattern.format(new Date(startTime));
                            date = date.substring(0, date.indexOf(" "));
                        }catch (Exception ee){
                            date = startDate;
                            date = date.substring(0, date.indexOf(" "));
                        }
                    }else if("N".equalsIgnoreCase(obj.getValue())){
                        endTime = obj.getOrgMillis();
                        if(startTime >0 && endTime > startTime) {
                            endDate = obj.getCts();
                            qty = Math.round (((endTime - startTime)/1000)*1.41);
                            if(totalQtyByDay.containsKey(date)){
                                Long quantity = qty + totalQtyByDay.get(date);
                                totalQtyByDay.put(date,quantity);
                            }else{
                                totalQtyByDay.put(date,qty);
                            }

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(Constants.NODESTARTTIME,startDate);
                            jsonObject.put(Constants.NODEENDDATE,endDate);
                            jsonObject.put(Constants.NODEQUANTITY,qty);
                            jsonObject.put(Constants.NODEDATE,date);

                            array.put(jsonObject);
                            startTime = 0L;
                            startDate ="";
                            endDate = "";
                            qty = 0L;
                        }
                    }
                }
            }
        }
        catch (JSONException je) {
            error("PRAJReportProcessor .... Line 189  "+je.getMessage());
            je.printStackTrace();
        } catch (Exception exception) {}
        return array;
    }

    private JSONArray noFunctionalData(List<PrajReportData> list){
        JSONArray array = new JSONArray();
        try {
            if (list != null){
                Long time = 0L;
                String date = "";
                String endDate = "";
                List <String> nonFuncData = new ArrayList<String>();
                List <String> funcDataList = new ArrayList<String>();

                for(PrajReportData obj : list){
                    if("Y".equalsIgnoreCase(obj.getValue())){
                        time = obj.getOrgMillis();
                            try {
                                date = dbPattern.format(new Date(time));
                                date = date.substring(0, date.indexOf(" "));
                                funcDataList.add(date);
                            }catch (Exception ee){
                            }
                    }else if("N".equalsIgnoreCase(obj.getValue())){
                        time = obj.getOrgMillis();
                        try {
                            endDate = dbPattern.format(new Date(time));
                            endDate = endDate.substring(0, endDate.indexOf(" "));
                        }catch (Exception ee){
                        }
                        if(!nonFuncData.contains(endDate))
                            nonFuncData.add(endDate);
                    }
                }
                info("funcDataList   "+funcDataList);
                info("nonFuncData   "+nonFuncData);
                for(String funcData : funcDataList){
                    nonFuncData.remove(funcData);
                }
                info("After Remove nonFuncData   "+nonFuncData);
                array.put(nonFuncData);
            }
        } catch (Exception exception) {}
        return array;
    }

    public String generateJSONResponse() {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = responseHeader(this.requestMessage, this.responseMessage);
            jsonObject.put(Constants.NODESTARTDATE, requestMessage.getStartDate());
            jsonObject.put(Constants.NODEENDDATE, requestMessage.getEndDate());
            if (alertlist != null)
                //jsonObject.put("report", AlertMongoLog.toJsonArrPraj(alertlist));
                if(requestMessage.getMessagetype().equals("FUNCREPORT")) {
                    jsonObject.put("report", refineData(alertlist));
                }else if(requestMessage.getMessagetype().equals("NONFUNCREPORT")) {
                    jsonObject.put("report", noFunctionalData(alertlist));
                }else if(requestMessage.getMessagetype().equals("GENREPORT")) {
                    jsonObject.put("report", noFunctionalData(alertlist));
                }
            if (account == null) {
                AccountDao accountDao = (AccountDao)ApplicationBeanContext.getInstance().getBean("accountDao");
                jsonObject.put("acc", Account.toJson(accountDao.getAccountById(this.requestMessage.getAccId())));
            } else {
                jsonObject.put("acc", Account.toJson(this.account));
            }
            if(totalQtyByDay.size()>0){
                JSONArray total = new JSONArray();
                Long avg = 0L;
                for (Map.Entry<String,Long> entry : totalQtyByDay.entrySet()){
                    JSONObject obj = new JSONObject();
                    obj.put(Constants.NODEDATE,entry.getKey());
                    obj.put(Constants.NODEQUANTITY,entry.getValue());
                    avg = avg + entry.getValue();
                    total.put(obj);
                }
                jsonObject.put("avg",avg/totalQtyByDay.size());
                jsonObject.put(Constants.NODETOTAL,total);
            }

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

    public List<Long> idSeperator(String ids) {
        String[] arr = ids.split(",");
        List<Long> list = new ArrayList<>();
        for (String a : arr) {
            list.add(Long.valueOf(Long.parseLong(a)));
        }
        return list;
    }

    public Long totalQuantity(Long accId, Long deviceId, String startDate, String endDate, String client){
        AlertDataDaoImpl alertDataDao = new AlertDataDaoImpl();
        RequestMessage rm = new RequestMessage();
        rm.setAssetId(deviceId);
        rm.setAccId(accId);
        rm.setStartDate(startDate);
        rm.setEndDate(endDate);
        rm.setClient(client);
        rm.setVendorcode(client);
        List<PrajReportData> list = alertDataDao.findAlerts(rm, null, true);
        Long qty = 0L;
        try {
            if (list != null){
                Long startTime = 0L;
                Long endTime = 0L;
                for(PrajReportData obj : list){
                    if("Y".equalsIgnoreCase(obj.getValue())){
                        startTime = obj.getOrgMillis();
                    }else if("N".equalsIgnoreCase(obj.getValue())){
                        endTime = obj.getOrgMillis();
                        if(startTime >0 && endTime > startTime) {
                            qty = qty + Math.round (((endTime - startTime)/1000)*1.41);
                        }
                    }
                }
            }
        } catch (Exception exception) {}
        return qty;
    }
}
