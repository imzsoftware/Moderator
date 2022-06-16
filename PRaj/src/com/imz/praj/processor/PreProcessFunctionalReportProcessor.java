package com.imz.praj.processor;

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
import com.traq.common.data.model.mongodao.mongoimpl.ProcessedReportMongoDaoImpl;
import com.traq.common.data.model.mongodao.mongoimpl.TrackDataMongoDaoImpl;
import com.traq.common.exceptions.EntityException;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.exceptions.ResultCodeExceptionInterface;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.ReportHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class PreProcessFunctionalReportProcessor extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private RequestMessage requestMessage;
    private String request;
    SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    RequestBean rb;
    User user = null;
    Map<Long, List<WorkingHourData>> finalIgnMap = null;
    Map<Long, Device> deviceMap = null;
    Map<Long, WorkingHourData> workingHourDataMap = null;

    public ResponseMessage getResponseMessage() {

        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public PreProcessFunctionalReportProcessor() {
    }
    public PreProcessFunctionalReportProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String executeXML(RequestBean rb) {
        return null;
    }

    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();

        ReportHandler dh = new ReportHandler();
        String response = "";

        boolean isSuccess = false;
        try {
            responseMessage = new ResponseMessage();
            JSONObject object = new JSONObject(request);

            String messageType = TagValues.getNodeValue(object, "requesttype");
            responseMessage.setMessagetype(messageType);
            responseMessage.setResultcode(ResultCodeExceptionInterface._RECORD_NOT_FOUND);
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(ResultCodeExceptionInterface._RECORD_NOT_FOUND));


            requestMessage = dh.getRequest(object.getJSONObject("request"), messageType);

            UserDao userDao = (UserDao) ApplicationBeanContext.getInstance().getBean("userDao");
            user = userDao.login(requestMessage.getUsername());

            if (this.user != null) {
                boolean isValid = validateUser(user, requestMessage.getPin());
                if (isValid) {
                    Date startDate = newPattern.parse(requestMessage.getStartDate());
                    requestMessage.setStartDate(dbPattern.format(startDate));
                    Date endDate = newPattern.parse(requestMessage.getEndDate());
                    requestMessage.setEndDate(dbPattern.format(endDate));
                    requestMessage.setVendorcode("PRAJ");
                    requestMessage.setClient(requestMessage.getVendorcode() +"_");
                    requestMessage.setCodes(Arrays.asList(new String[]{"IGN"}));

                    AccountDao accountDao = (AccountDao) ApplicationBeanContext.getInstance().getBean("accountDao");
                    List <Account> accList = accountDao.findByClient(requestMessage.getVendorcode());
                    DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
                    List<Device> deviceList = deviceDao.getDeviceByAcc(accList);

                    //AlertDataDaoImpl alertDataDao = new AlertDataDaoImpl();
                    TrackDataMongoDaoImpl trackDataMongoDao = new TrackDataMongoDaoImpl();
                    //List<PrajReportData> reportDataList = null;
                    List<TrackData> reportDataList = null;


                    ProcessedReportMongoDaoImpl prDao = new ProcessedReportMongoDaoImpl();
                    try {
                        String date = requestMessage.getStartDate();
                        date = date.substring(0, date.indexOf(" "));
                        info("deviceList ............ "+deviceList.size());
                        int count = 1;
                        for (Device device : deviceList) {
                            Long pId = BaseInitializer.getAccountMap().get(device.getAccId()).getParentAccountId();
                            Account acc = BaseInitializer.getAccountMap().get(pId);
                            if(acc == null ){
                                info("Parent Account is  ............ "+acc);
                                continue;
                            }
                            Long gpId = acc.getParentAccountId();
                            if(gpId == 0){
                                continue;
                            }
                            acc = BaseInitializer.getAccountMap().get(gpId);
                            if(acc == null ){
                                continue;
                            }
                            Long ggpId = acc.getParentAccountId();
                            if(ggpId == 0){
                                continue;
                            }
                            requestMessage.setAssetId(device.getId());
                            reportDataList = trackDataMongoDao.prajReport(requestMessage);
                            //reportDataList = alertDataDao.findAlerts(requestMessage, null, true);
                            Document respObj = new Document();
                            Document document = null;
                            List<Document> array = new ArrayList<Document>();
                            info("Before reportDataList .....  ............ ");
                            if (reportDataList != null || reportDataList.size() > 0) {
                                //String client = BaseInitializer.getAccountMap().get(device.getAccId()).getClient();
                                Long totalQty = 0L;
                                Long orgTime = 0L;
                                boolean isStart = false;
                                for(TrackData data : reportDataList){
                                    if(!isStart && "Y".equals(data.getIgnition())){
                                        document = new Document();
                                        document.put(Constants.NODESTARTTIME,data.getOrgTs());
                                        document.put(Constants.NODEQUANTITY,0L);
                                        orgTime = data.getOrgtsMillis();
                                        isStart = true;
                                    } else if(isStart && "N".equals(data.getIgnition())){
                                        document.put(Constants.NODEENDTIME,data.getOrgTs());
                                        // Calculation based on 85 liter per minute
                                        Long time = (data.getOrgtsMillis() - orgTime)/1000;
                                        Long qty = Math.round (time*1.41);
                                        totalQty = totalQty + qty;
                                        document.put(Constants.NODEQUANTITY,qty);
                                        document.put(Constants.NODEIGNTIME,Math.round(time/60));

                                        array.add(document);
                                        isStart = false;
                                    }
                                }
                                if(document != null && document.getLong(Constants.NODEQUANTITY) > 0){
                                    respObj.put(Constants.NODERUNNING,1);
                                }else{
                                    respObj.put(Constants.NODERUNNING,0);
                                }

                                respObj.put(Constants.NODEDISTRICT, ggpId);
                                respObj.put(Constants.NODEBLOCK, gpId);
                                respObj.put(Constants.NODEPANCHAYAT, pId);
                                respObj.put(Constants.NODEACCOUNTID, device.getAccId());
                                respObj.put(Constants.NODEDEVICEID, device.getId());
                                respObj.put(Constants.NODEIMEI, device.getIMEI());
                                respObj.put(Constants.NODEDATE, date);
                                respObj.put(Constants.NODETOTAL,totalQty);

                                respObj.put(Constants.NODEDATA, array);

                                prDao.saveReport(respObj, requestMessage.getVendorcode());
                            }else{
                                respObj.put(Constants.NODEDISTRICT, ggpId);
                                respObj.put(Constants.NODEBLOCK, gpId);
                                respObj.put(Constants.NODEPANCHAYAT, pId);
                                respObj.put(Constants.NODEACCOUNTID, device.getAccId());
                                respObj.put(Constants.NODEDEVICEID, device.getId());
                                respObj.put(Constants.NODEIMEI, device.getIMEI());
                                respObj.put(Constants.NODEDATE, date);
                                respObj.put(Constants.NODETOTAL,0);
                                respObj.put(Constants.NODERUNNING,-1);
                                respObj.put(Constants.NODEDATA, array);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    responseMessage.setResultcode(0);
                    responseMessage.setResultDescription("Success");
                } else if (!user.getStatus().getCode().equals("A")) {
                    responseMessage.setResultcode(ResultCodeExceptionInterface._INACTIVE_USER);
                    responseMessage.setResultDescription(ResultCodeDescription.getDescription(ResultCodeExceptionInterface._INACTIVE_USER));

                } else if (!user.getPin().equals(requestMessage.getPin())) {
                    responseMessage.setResultcode(Integer.valueOf(27));
                    responseMessage.setResultDescription("Invalid Password");
                }
            } else {
                responseMessage.setResultcode(ResultCodeExceptionInterface._USER_NOT_FOUND);
                responseMessage.setResultDescription(ResultCodeDescription.getDescription(ResultCodeExceptionInterface._USER_NOT_FOUND));
            }
        } catch (NoRecordFoundException ex) {
            responseMessage.setResultcode(ResultCodeExceptionInterface._RECORD_NOT_FOUND);
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(ResultCodeExceptionInterface._RECORD_NOT_FOUND));
            ex.printStackTrace();

        } catch (EntityException ee) {
            responseMessage.setResultcode(ee.getResultCode(Integer.valueOf(0)));
            responseMessage.setResultDescription(ee.getDescription());
            ee.printStackTrace();

        } catch (Exception ex) {
            responseMessage.setResultcode(Integer.valueOf(206));
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            ex.printStackTrace();
        } finally {
            if (responseMessage.getResultcode() == 0) {
                response = generateJSONResponse();
            } else {
                response = generateFailureResponse(this.responseMessage);
            }
        }
        return response;
    }

    public String generateJSONResponse() {

        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = responseHeader(requestMessage, responseMessage);
            mainObj.put("response", jsonObject);

        } catch (JSONException je) {
            je.printStackTrace();
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
            mainObj.put("response", jsonObject);
        } catch (JSONException jSONException) {
        }
        return mainObj.toString();
    }


}
