package com.rep.reports;

import com.rep.beanobject.InOutHistory;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.*;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.RFIDDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.data.model.mongodao.TrackDataMongoDao;
import com.traq.common.data.model.mongodao.mongoimpl.TrackDataMongoDaoImpl;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.exceptions.ResultCodeExceptionInterface;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.ReportHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class InOutReport extends BaseInitializer implements RequestProcessorInterface{

    private ResponseMessage responseMessage;
    private RequestMessage requestMessage;
    private String request;
    List<TrackData> dataList =  null;

    List<InOutHistory> lockReport = new ArrayList<>();
    SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    RequestBean rb;
    User user = null;

    public ResponseMessage getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public InOutReport(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public InOutReport() {
    }

    public String executeXML(RequestBean _rb){
        return request;
    }


    public String executeJSON(RequestBean _rb){
        rb = _rb;
        request = rb.getRequest();

        ReportHandler dh = new ReportHandler();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        String response = "";

        try{
            UserDao userDao =  (UserDao) ApplicationBeanContext.getInstance().getBean("userDao");
            responseMessage = new ResponseMessage();
            JSONObject object = new JSONObject(request);
            requestMessage = dh.getRequest(object.getJSONObject(Constants.NODEREQUEST), TagValues.getNodeValue(object, Constants.NODEREQUESTTYPE));

            Date date = newPattern.parse(requestMessage.getStartDate());
            requestMessage.setStartDate(dbPattern.format(date));
            date = newPattern.parse(requestMessage.getEndDate());
            requestMessage.setEndDate(dbPattern.format(date));

            user = userDao.login(requestMessage.getUsername());
            boolean isValid = validateUser(user, requestMessage.getPin());

            if(isValid){
                if(checkNullAndEmpty(requestMessage.getAccId())) {
                    responseMessage.setMessagetype(requestMessage.getMessagetype());
                    responseMessage.setResponsects(sdf.format(new Date()));
                    if(getAppConfig().getSwitchdb().equals("1")){
                        AccountDao accountDao =  (AccountDao) ApplicationBeanContext.getInstance().getBean("accountDao");
                        Account account = accountDao.getAccountById(requestMessage.getAccId(),false);
                        if(checkNullAndEmpty(account.getClient())){
                            requestMessage.setClient(account.getClient()+"_");
                        }
                        else{
                            requestMessage.setClient("TM_");
                        }
                    }
                    TrackDataMongoDao trackDataDao = new TrackDataMongoDaoImpl();
                    dataList = trackDataDao.findTrack(requestMessage, null);

                    responseMessage.setResultcode(ResultCodeExceptionInterface._SUCCESS);
                    responseMessage.setResultDescription(ResultCodeDescription._DES_SUCCESS);
                    if(dataList.size() == 0){
                        throw new NoRecordFoundException();
                    }else {
                        generateInOutReport(dataList);
                    }

                    responseMessage.setResultcode(ResultCodeExceptionInterface._SUCCESS);
                    responseMessage.setResultDescription(ResultCodeDescription._DES_SUCCESS);
                }
                else{
                    responseMessage.setResultcode(ResultCodeExceptionInterface._MANDATORY_VALUES_NULL);
                    responseMessage.setResultDescription(ResultCodeDescription._DES_MANDATORY_VALUES_NULL);
                }
            }else{
                responseMessage.setResultcode(ResultCodeExceptionInterface._USER_NOT_FOUND);
                responseMessage.setResultDescription(ResultCodeDescription._DES_USER_NOT_FOUND);
            }

        }catch (NoRecordFoundException nrfe){
            responseMessage.setResultcode(ResultCodeExceptionInterface._RECORD_NOT_FOUND);
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(ResultCodeExceptionInterface._RECORD_NOT_FOUND));
        }
        catch(Exception ex){
            responseMessage.setResultcode(ResultCodeExceptionInterface._TECHNICAL_FAILURE);
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(ResultCodeExceptionInterface._TECHNICAL_FAILURE));
            ex.printStackTrace();
        }finally{
            if(responseMessage.getResultcode() == ResultCodeExceptionInterface._SUCCESS){
                response = generateJSONResponse();
            }else{
                response = generateFailureResponse(responseMessage);
            }
        }
        info("Final Response..............." + response);
        return response;
    }


    private void generateInOutReport(List<TrackData> dataList){
        int isInTime = 0;
        InOutHistory inOutHistory = null;
        RFIDDao rfidDao =  (RFIDDao) ApplicationBeanContext.getInstance().getBean("rfidDao");
        for(TrackData trackData : dataList) {
            try {
                switch (isInTime){
                    case 0:
                        if ("0".equalsIgnoreCase(trackData.getLockSts())) {
                            isInTime = 1;
                            inOutHistory = new InOutHistory();
                            inOutHistory.setLockStatus("Unlocked");
                            inOutHistory.setStartDate(trackData.getOrgTs());
                            inOutHistory.setLatlng(trackData.getLatitude() + "," + trackData.getLongitude());
                            inOutHistory.setGps(trackData.getGps().equals("A") ? "Fix" : "Not Fix");
                            inOutHistory.setRfid(trackData.getRfId());
                            inOutHistory.setEndDate("NA");
                            inOutHistory.setLocation(trackData.getLocation());
                            inOutHistory.setEventtype("IN");
                            if(trackData.getReason() != null ) {
                                inOutHistory.setReason(trackData.getReason());
                            }else{
                                inOutHistory.setReason("NA");
                            }
                            if(rfidDao.getRfid(trackData.getRfId()) != null ) {
                                inOutHistory.setOprName(rfidDao.getRfid(trackData.getRfId()).getName());
                            }else{
                                inOutHistory.setOprName(trackData.getRfId());
                            }
                        }
                        break;
                    case 1:
                        if ("1".equalsIgnoreCase(trackData.getLockSts())) {
                            isInTime = 0;
                            inOutHistory.setEventtype("OUT");
                            inOutHistory.setEndDate(trackData.getOrgTs());

                            if(checkNullAndEmpty(trackData.getRfId())) {
                                if (rfidDao.getRfid(trackData.getRfId()) != null) {
                                    inOutHistory.setOprName(rfidDao.getRfid(trackData.getRfId()).getName());
                                }
                            }/*else{
                                inOutHistory.setOprName(trackData.getRfId());
                            }*/
                            lockReport.add(inOutHistory);
                        }
                        break;

                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(isInTime == 1){
            lockReport.add(inOutHistory);
        }
    }


    public String generateJSONResponse(){
        JSONObject mainObj = new JSONObject();
        try{
            JSONObject jsonObject = responseHeader(requestMessage, responseMessage);
            jsonObject.put(Constants.NODEREPORT, InOutHistory.toJsonArr(this.lockReport));
            mainObj.put(Constants.NODERESPONSE, jsonObject);
        }catch (JSONException je){
            je.printStackTrace();
        }catch (Exception e){

        }

        return mainObj.toString();
    }

    public String generateFailureResponse(ResponseMessage rm){
        JSONObject mainObj = new JSONObject();
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.NODERESPONSETYPE, rm.getMessagetype());
            jsonObject.put(Constants.NODERESULTCODE, rm.getResultcode());
            jsonObject.put(Constants.NODERESULTDESCRIPTION, rm.getResultDescription());

            mainObj.put(Constants.NODERESPONSE, jsonObject);
        }catch (JSONException je){

        }
        return mainObj.toString();
    }

    public List<Long> idSeperator(String ids){
        String[] arr = ids.split(",");
        List<Long> list = new ArrayList<>();
        for (String a: arr)
        {
            list.add(Long.parseLong(a));
        }
        return list;
    }



}


