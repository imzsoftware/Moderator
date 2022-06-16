package com.imz.praj.processor;

import com.imz.praj.data.impl.PrajReportDaoImpl;
import com.imz.praj.data.impl.RedisToMongoDaoImpl;
import com.imz.praj.data.obj.PrajReportData;
import com.imz.praj.entity.PrajReport;
import com.imz.praj.entity.ViewDetailRep;
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
import com.traq.common.xmlutils.XMLProcessor;
import com.traq.util.RequestBean;
import com.traq.util.Utility;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class GenReportProcessor extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    private RequestMessage requestMessage;
    private String request;

    SimpleDateFormat outputPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    RequestBean rb;
    long curTime = (new Date()).getTime();
    long timeDiff = 19800*1000;  // Value in milliseconds 5Hr 30min from GMT
    long oneDay = (24*60*60*1000);

    private String stdData = "<traq><lat>0.0</lat><lng>0.0</lng><disha>0</disha><speed>0</speed><ign>N</ign><distance></distance><fuel></fuel><temp></temp><ac></ac><door></door><reserved></reserved><gps></gps><satellites>0</satellites><orgts></orgts><cts></cts><address></address><deviceid></deviceid><accid></accid><imb>N</imb><powsts></powsts><battery>0</battery><devip></devip><devport></devport><servip></servip><servport></servport><devicetypeid></devicetypeid></traq>";

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public GenReportProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public GenReportProcessor() {}

    public String executeXML(RequestBean _rb) {
        return this.request;
    }

    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();

        RequestHandler rh = new RequestHandler();
        Map<String, String> dataMap = new HashMap<>();
        Map<Long,PrajReport> prajReports = new HashMap<>();

        String response = "";
        //Map<Long, Account> accountMap = null;
        try {
            JSONObject object = new JSONObject(this.request);
            JSONObject reqObj = object.getJSONObject("request");
            requestMessage = rh.getRequest(reqObj, TagValues.getNodeValue(object, "requesttype"));
            requestMessage.setStartDate(TagValues.getNodeValue(reqObj, Constants.NODESTARTDATE));
            requestMessage.setEndDate(TagValues.getNodeValue(reqObj, Constants.NODEENDDATE));
            JSONArray accArray =  TagValues.getArrayValue(reqObj, Constants.NODEACCOUNT);
            JSONArray devArray =  TagValues.getArrayValue(reqObj, Constants.NODEASSET);

            boolean isToday = false;
            String today = sdf.format(new Date());
            if(today.equals(requestMessage.getStartDate().substring(0,requestMessage.getStartDate().indexOf(" ")).trim())){
                isToday = true;
            }
            Calendar calendar = Calendar.getInstance();

            Date date = getDataPattern().parse(requestMessage.getStartDate());
            calendar.setTime(date);
            calendar.add(Calendar.DATE,-1);
            requestMessage.setStartDate(getDbPattern().format(date));
            date = getDataPattern().parse(requestMessage.getEndDate());
            requestMessage.setEndDate(getDbPattern().format(date));


            if (requestMessage.getIpaddress() == null) {
                throw new InvalidRequest("Ipaddess should not be null");
            }
            responseMessage = new ResponseMessage();
            responseMessage.setMessagetype(requestMessage.getMessagetype());
            responseMessage.setResponsects(sdf.format(new Date()));
            responseMessage.setTransid(rb.getTransid());

            UserDao userDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");
            User user = userDao.login(requestMessage.getUsername());

            boolean isValid = validateUser(user, requestMessage.getPin());

            if (isValid) {
                DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
                AccountDao accountDao = (AccountDao)ApplicationBeanContext.getInstance().getBean("accountDao");

                List<Account> accountList = null;

                if(isToday){
                    SimpleDateFormat redisKeyFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String reportDate = redisKeyFormat.format(new Date());
                    String repKey ="PRAJ_REP_"+reportDate;

                    Map<String, String> reportMap = hgetAll(repKey);
                    Map<Long, Device> deviceMap = new HashMap<Long, Device>();
                    if (devArray != null && devArray.length() > 0) {
                        for (int i = 0; i < devArray.length(); i++) {
                            Device dev = deviceDao.getDeviceById(devArray.getLong(i));
                            if(checkNullAndEmpty(reportMap.get(devArray.getLong(i)+""))) {
                                try {
                                    PrajReport report = createObject(dev, reportMap.get(devArray.getLong(i) + ""), reportDate);
                                    prajReports.put(devArray.getLong(i), report);
                                    String data = hget(dev.getAccId() + "", dev.getId() + "");
                                    dataMap.put(dev.getId() + "", data);
                                }catch (Exception e){

                                }
                            }else{
                                String data = hget(dev.getAccId() + "", dev.getId() + "");
                                dataMap.put(dev.getId() + "", data);
                                deviceMap.put(dev.getId(),dev);
                                continue;
                            }
                        }
                    } else if (accArray != null) {
                        List<Device> deviceList = findWardsList(accountDao, accArray);
                        if(deviceList != null && deviceList.size() > 0){
                            for (Device device : deviceList) {
                                if(checkNullAndEmpty(reportMap.get(device.getId()+""))) {
                                    PrajReport report = createObject(device, reportMap.get(device.getId() + ""), reportDate);
                                    prajReports.put(device.getId(), report);
                                    String data = hget(device.getAccId() + "", device.getId() + "");
                                    dataMap.put(device.getId() + "", data);
                                }else{
                                    String data = hget(device.getAccId() + "", device.getId() + "");
                                    dataMap.put(device.getId() + "", data);
                                    deviceMap.put(device.getId(),device);
                                    continue;
                                }
                            }
                        }
                    }
                    Map<Long,PrajReport> nonFuncReport =new RedisToMongoDaoImpl().nonFuncReport(deviceMap,"",getDbPattern().format(calendar.getTime()),requestMessage.getEndDate());
                    for (Map.Entry<Long, PrajReport> repMap : nonFuncReport.entrySet()) {
                        prajReports.putIfAbsent(repMap.getKey(),repMap.getValue());
                    }
                    info("prajReports.size() ..... "+ prajReports.size());
                }else {
                    Map<Long, Device> deviceMap = new HashMap<Long, Device>();
                    if (devArray != null && devArray.length() > 0) {
                        RedisToMongoDaoImpl redisToMongoDao = new RedisToMongoDaoImpl();
                        for (int i = 0; i < devArray.length(); i++) {
                            Device dev = deviceDao.getDeviceById(devArray.getLong(i));
                            deviceMap.put(devArray.getLong(i), dev);
                            String data = hget(dev.getAccId() + "", dev.getId() + "");
                            dataMap.put(dev.getId() + "", data);
                        }
                        prajReports = redisToMongoDao.wardReport(deviceMap,requestMessage.getStartDate(),requestMessage.getEndDate());
                    } else if (accArray != null) {
                        accountList = findLevel5Account(accountDao, accArray);
                        for (Account acc : accountList) {
                            List<Device> devices = deviceDao.getDeviceByAccount(acc.getId());
                            for (Device dev : devices) {
                                deviceMap.put(dev.getId(), dev);
                            }
                        }
                        RedisToMongoDaoImpl redisToMongoDao = new RedisToMongoDaoImpl();
                        prajReports = redisToMongoDao.wardReport(deviceMap,requestMessage.getStartDate(),requestMessage.getEndDate());
                        dataMap.putAll(hgetAll(accountList));
                    }
                    Map<Long,PrajReport> nonFuncReport =new RedisToMongoDaoImpl().nonFuncReport(deviceMap,"",requestMessage.getStartDate(),requestMessage.getEndDate());
                    for (Map.Entry<Long, PrajReport> repMap : nonFuncReport.entrySet()) {
                        prajReports.putIfAbsent(repMap.getKey(),repMap.getValue());
                    }
                }
            } else {
                if (!user.getPin().equals(this.requestMessage.getPin()))
                    throw new InvalidPassword();
                if (!user.getStatus().getCode().equals("A")) {
                    throw new InActiveUser();
                }
                throw new InvalidUser();
            }

            this.responseMessage.setResultcode(0);
            this.responseMessage.setResultDescription("Success");
        } catch (EntityException ee) {
            this.responseMessage.setResultcode(ee.getResultCode(Integer.valueOf(0)));
            this.responseMessage.setResultDescription(ee.getDescription());
            this.responseMessage.setTimestamp((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(new Date()));
        }
        catch (Exception ex) {
            this.responseMessage.setResultcode(206);
            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(206));
            ex.printStackTrace();
        } finally {
            if (this.responseMessage.getResultcode() == 0) {
                response = generateJSONResponse(dataMap,prajReports);
            } else {
                response = generateFailureResponse(this.responseMessage);
            }
        }
        return response;
    }

    public String generateJSONResponse(Map<String,String> dataMap, Map<Long, PrajReport> prajReports) {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = responseHeader(requestMessage, this.responseMessage);
            jsonObject.put("transid", responseMessage.getTransid());
            jsonObject.put(Constants.NODESTARTDATE, requestMessage.getStartDate());
            jsonObject.put(Constants.NODEENDDATE, requestMessage.getEndDate());

            JSONArray arrayprimary = new JSONArray();
            if (dataMap.size()>0 && prajReports.size()>0) {
                PrajReportDaoImpl prp = new PrajReportDaoImpl();
                Long curTime = System.currentTimeMillis();
                for (Map.Entry<Long, PrajReport> repMap : prajReports.entrySet()) {
                    String redisData = dataMap.get(repMap.getKey()+"");
                    //info("GenReportProcessor....repMap.getKey() "+repMap.getKey() +"redisData = "+redisData);
                    requestMessage.setAssetId(repMap.getKey());
                    JSONObject object = PrajReport.toJson(repMap.getValue());
                    object.put(Constants.NODEDEVICEID,repMap.getKey());
                    try {
                        object.put(Constants.NODEACCOUNTID, Long.parseLong(TagValues.getNodeValue(redisData, Constants.NODEACCOUNTID)));
                    }catch (NumberFormatException nfe){
                        DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
                        object.put(Constants.NODEACCOUNTID, deviceDao.getDeviceById(repMap.getKey()).getAccId());
                    }
                    Long oprTime = 0L;
                    try {
                        oprTime = Long.parseLong(TagValues.getNodeValue(redisData, Constants.NODEIGNTIME)) - timeDiff;
                        object.put(Constants.NODEIGNTIME, getDbPattern().format(new Date(oprTime)));
                    }catch (Exception e){
                        object.put(Constants.NODEIGNTIME, "NA");
                    }
                    object.put(Constants.NODELATITUDE, TagValues.getNodeValue(redisData, Constants.NODELATITUDE));
                    object.put(Constants.NODELONGITUDE, TagValues.getNodeValue(redisData, Constants.NODELONGITUDE));

                    if (TagValues.getNodeValue(redisData, Constants.NODEIGNITION) .equals("Y")) {
                        object.put(Constants.NODESTATUS, "ON");
                        object.put(Constants.NODEIGNITION, "Y");
                    } else {
                        if((oprTime +oneDay) >curTime) {
                            object.put(Constants.NODESTATUS, "IDLE");
                        }else {
                            object.put(Constants.NODESTATUS, "STOP");
                        }
                        object.put(Constants.NODEIGNITION, "N");
                    }
                    JSONArray arr =
                    arrayprimary.put(object);
                }
            }
            jsonObject.put(Constants.NODERECORDS, arrayprimary);

            mainObj.put(Constants.NODERESPONSE, jsonObject);
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

    private PrajReport createObject(Device device, String devData, String reportDate){
        PrajReport report = new PrajReport();
        try {
            report.setAnurakshak(device.getDriver());
        } catch (Exception exception) {
        }
        Account panchayat = BaseInitializer.getAccountMap().get(device.getAccId());
        Account block = BaseInitializer.getAccountMap().get(panchayat.getParentAccountId());
        Account district = BaseInitializer.getAccountMap().get(block.getParentAccountId());

        report.setPanchayat(panchayat.getName());
        report.setBlock(block.getName());
        report.setDistrict(district.getName());
        report.setWard(device.getLicenseno());
        report.setDate(reportDate);

        devData = XMLProcessor.makeTag(Constants.NODERECORDS,devData);
        List<String> record = TagValues.getAllNodeValue(devData, Constants.NODERECORD);
        List<ViewDetailRep> detailRepList = new ArrayList<>();
        Long totalQty = 0L;
        for(String rec : record){
            //info("rec.. "+rec);
            try {
                ViewDetailRep detailRep = new ViewDetailRep();
                totalQty = Long.parseLong(TagValues.getNodeValue(rec, Constants.NODETOTAL));
                Long sTime = Long.parseLong(TagValues.getNodeValue(rec, Constants.NODESTARTTIME));
                Long eTime = Long.parseLong(TagValues.getNodeValue(rec, Constants.NODEENDTIME));
                detailRep.setOnTime(getDbPattern().format(new Date(sTime - timeDiff)));
                detailRep.setOffTime(getDbPattern().format(new Date(eTime - timeDiff)));
                detailRep.setRunTime((eTime - sTime) / 60000);
                report.setLastOprTime(detailRep.getOffTime());
                detailRep.setQuantity(Long.parseLong(TagValues.getNodeValue(rec, Constants.NODEVALUE)));
                detailRep.setDate(sdf.format(new Date(sTime - timeDiff)));
                detailRepList.add(detailRep);
            }catch (Exception e){
                expireKey("EWA_"+device.getId());
            }
        }

        report.setDetailReports(detailRepList);
        report.setTotalQuantity(totalQty);

        return report;
    }

    private List<Device> findWardsList(AccountDao accountDao, JSONArray accArray) throws JSONException{
        List<Device> deviceList = new ArrayList<>();
        DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
        //info("GenReportProcessor .... accArray = "+accArray);
        for (int i = 0; i < accArray.length(); i++) {
            Account account = BaseInitializer.getAccountMap().get(accArray.getLong(i));
            if ("L5".equalsIgnoreCase(account.getType())) {
                List<Device> devices = deviceDao.getDeviceByAccount(accArray.getLong(i));
                deviceList.addAll(devices);
            } else {
                List<Account> accounts = accountDao.findAllChildren(accArray.getLong(i));
                for (Account acc : accounts) {
                    if ("L5".equalsIgnoreCase(acc.getType())) {
                        List<Device> devices = deviceDao.getDeviceByAccount(acc.getId());
                        deviceList.addAll(devices);
                    }
                }
            }
        }
        return deviceList;
    }

    private List<Account> findLevel5Account(AccountDao accountDao, JSONArray accArray) throws JSONException{
        List<Account> accList = new ArrayList<>();
        for (int i = 0; i < accArray.length(); i++) {
            Account account = BaseInitializer.getAccountMap().get(accArray.getLong(i));
            if ("L5".equalsIgnoreCase(account.getType())) {
                accList.add(account);
            } else {
                List<Account> accounts = accountDao.findAllChildren(accArray.getLong(i));
                for (Account acc : accounts) {
                    if ("L5".equalsIgnoreCase(acc.getType())) {
                        accList.add(acc);
                    }
                }
            }
        }
        return accList;
    }
}
