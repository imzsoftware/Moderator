package com.imz.praj.processor;

import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.*;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.data.model.mongodao.mongoimpl.TrackDataMongoDaoImpl;
import com.traq.common.exceptions.EntityException;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.ReportHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import com.traq.util.Utility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class FunctionalReportProcessor extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private RequestMessage requestMessage;
    private String request;
    SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    RequestBean rb;
    User user = null;
    List<WorkingHourData> onlineList = null;
    List<WorkingHourData> offlineList = null;
    Map<Long, List<WorkingHourData>> finalIgnMap = null;
    Map<Long, Device> deviceMap = null;
    Map<Long, WorkingHourData> workingHourDataMap = null;
    Map<String, List<WorkingHourData>> accWorkingDataMap = null;
    Map<Long, JSONObject> accountHeirarchyMap = new HashMap<>();
    Map<Account, List<Device>> mapChildAccount;
    List<Account> childAccountList;

    public ResponseMessage getResponseMessage() {

        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public FunctionalReportProcessor(ResponseMessage responseMessage) {
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
        String loginLevel = " ";

        boolean isSuccess = false;
        try {
            this.responseMessage = new ResponseMessage();
            JSONObject object = new JSONObject(this.request);

            String messageType = TagValues.getNodeValue(object, "requesttype");
            this.responseMessage.setMessagetype(messageType);

            this.requestMessage = dh.getRequest(object.getJSONObject("request"), messageType);


            UserDao userDao = (UserDao) ApplicationBeanContext.getInstance().getBean("userDao");

            this.user = userDao.login(this.requestMessage.getUsername());

            if (this.user != null) {

                boolean isValid = validateUser(this.user, this.requestMessage.getPin());

                if (isValid) {

                    Date startDate = this.newPattern.parse(this.requestMessage.getStartDate());

                    this.requestMessage.setStartDate(this.dbPattern.format(startDate));

                    Date endDate = this.newPattern.parse(this.requestMessage.getEndDate());

                    this.requestMessage.setEndDate(this.dbPattern.format(endDate));


                    if (this.requestMessage.getTranstype().equalsIgnoreCase("DASHBOARD")) {

                        if (checkNullAndEmpty(this.requestMessage.getAccId())) {

                            AccountDao accountDao = (AccountDao) ApplicationBeanContext.getInstance().getBean("accountDao");

                            Account account = accountDao.getAccountById(this.requestMessage.getAccId());

                            loginLevel = account.getType();

                            if (account != null) {

                                this.childAccountList = accountDao.findChildren(this.requestMessage.getAccId());

                                this.mapChildAccount = new HashMap<>();

                                List<Account> accountList = getChildrenByAccount(account);

                                DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");

                                this.deviceMap = deviceDao.getModifiedDeviceMapByAcc(accountList);


                                for (int i = 0; i < this.childAccountList.size(); i++) {

                                    if (!this.mapChildAccount.containsKey(this.childAccountList.get(i))) {

                                        List<Account> accountListForChile = getChildrenByAccount(this.childAccountList.get(i));

                                        List<Device> deviceList = deviceDao.getDeviceByAcc(accountListForChile);

                                        this.mapChildAccount.put(this.childAccountList.get(i), deviceList);
                                    }
                                }



                                if (this.deviceMap != null && this.deviceMap.size() > 0) {


                                    TrackDataMongoDaoImpl trackDataMongoDaoImpl = new TrackDataMongoDaoImpl();

                                    this.requestMessage.setAssetids(new ArrayList(this.deviceMap.keySet()));

                                    this.requestMessage.setVendorcode("PRAJ");

                                    this.requestMessage.setCodes(Arrays.asList(new String[]{"IGN"}));


                                    List<WorkingHourData> alertlog = trackDataMongoDaoImpl.findPreprocessedBorewell(this.requestMessage, null, true);


                                    if (alertlog != null && alertlog.size() > 0) {

                                        this.accWorkingDataMap = new HashMap<>();


                                        Map<Long, List<WorkingHourData>> map = dataSorter(alertlog);


                                        alertlog = null;

                                        this.workingHourDataMap = new HashMap<>();

                                        for (Map.Entry<Long, List<WorkingHourData>> entry : map.entrySet()) {

                                            Device device = this.deviceMap.get(entry.getKey());

                                            if (device != null) {

                                                prepareWorkingHourReport(entry.getValue(), this.workingHourDataMap, device, true);
                                            }
                                        }

                                        if (this.mapChildAccount != null && this.mapChildAccount.size() != 0) {

                                            for (Map.Entry<Account, List<Device>> entry1 : this.mapChildAccount.entrySet()) {

                                                if (!this.accWorkingDataMap.containsKey(((Account) entry1.getKey()).getName())) {

                                                    List<WorkingHourData> list = new ArrayList<>();

                                                    for (Device device : entry1.getValue()) {

                                                        if (this.workingHourDataMap != null && this.workingHourDataMap.size() > 0) {

                                                            for (Long workingDataDeviceid : this.workingHourDataMap.keySet()) {

                                                                if (device.getId().equals(workingDataDeviceid)) {

                                                                    list.add(this.workingHourDataMap.get(workingDataDeviceid));
                                                                }
                                                            }
                                                        }
                                                    }




                                                    this.accWorkingDataMap.put(((Account) entry1.getKey()).getName(), list);
                                                    continue;
                                                }

                                                List<WorkingHourData> listWorkingHourdataByChildAccount = this.accWorkingDataMap.get(((Account) entry1.getKey()).getName());

                                                for (Device device : entry1.getValue()) {

                                                    if (this.workingHourDataMap != null && this.workingHourDataMap.size() > 0) {

                                                        for (Long workingDataDeviceid : this.workingHourDataMap.keySet()) {

                                                            if (device.getId().equals(workingDataDeviceid)) {

                                                                listWorkingHourdataByChildAccount.add(this.workingHourDataMap.get(workingDataDeviceid));
                                                            }
                                                        }

                                                    }
                                                }

                                            }

                                        } else {


                                            for (Map.Entry<Long, List<WorkingHourData>> entry : map.entrySet()) {

                                                String parentName = "";

                                                Device device = this.deviceMap.get(entry.getKey());

                                                if (device != null) {

                                                    if (this.workingHourDataMap.containsKey(device.getId()) &&
                                                            loginLevel.equalsIgnoreCase("L5")) {

                                                        WorkingHourData data = this.workingHourDataMap.get(device.getId());

                                                        parentName = data.getVehicleNumber();


                                                        if (this.accWorkingDataMap.containsKey(parentName)) {

                                                            List<WorkingHourData> DeviceListGroupedByPanchiyat = this.accWorkingDataMap.get(parentName);

                                                            DeviceListGroupedByPanchiyat.add(this.workingHourDataMap.get(device.getId()));
                                                            continue;
                                                        }

                                                        if (!this.accWorkingDataMap.containsKey(parentName)) {

                                                            List<WorkingHourData> DeviceListGroupedByPanchiyat = new ArrayList<>();

                                                            DeviceListGroupedByPanchiyat.add(this.workingHourDataMap.get(device.getId()));

                                                            this.accWorkingDataMap.put(parentName, DeviceListGroupedByPanchiyat);
                                                        }
                                                    }
                                                }
                                            }
                                        }



                                        map = null;





                                        if (this.workingHourDataMap != null && this.workingHourDataMap.size() > 0) {

                                            if (checkNullAndEmpty(this.requestMessage.getComments()) && this.requestMessage
                                                    .getComments().equalsIgnoreCase("ASC")) {


                                                this.onlineList = new ArrayList<>(this.workingHourDataMap.values());

                                                this.offlineList = new ArrayList<>(this.workingHourDataMap.values());


                                                Collections.sort(this.onlineList, Comparator.comparing(WorkingHourData::getOnDuration));

                                                Collections.sort(this.offlineList, Comparator.comparing(WorkingHourData::getOffDuration));
                                            } else {

                                                this.onlineList = new ArrayList<>(this.workingHourDataMap.values());

                                                this.offlineList = new ArrayList<>(this.workingHourDataMap.values());


                                                Collections.sort(this.onlineList, (o1, o2) -> o2.getOnDuration().compareTo(o1.getOnDuration()));

                                                Collections.sort(this.offlineList, (o1, o2) -> o2.getOffDuration().compareTo(o1.getOffDuration()));
                                            }

                                            isSuccess = true;
                                        }

                                        this.workingHourDataMap = null;
                                    } else {

                                        this.workingHourDataMap = new HashMap<>();

                                        addDeviceIfAbsent();
                                    }


                                } else if (null != accountList) {

                                    info("No Device Mapped with account [" + accountList.toString() + "]");
                                } else {

                                    info("No Device Mapped");
                                }

                                accountList = null;
                            } else {

                                info("No Account Fetch From DB");
                            }
                        } else {

                            error("AccId Not Received");

                            this.responseMessage.setResultcode(Integer.valueOf(24));

                            this.responseMessage.setResultDescription("Some fields missing");
                        }

                    } else if (this.requestMessage.getTranstype().equalsIgnoreCase("REPORT")) {

                        if (this.requestMessage.getAssetids() != null && this.requestMessage.getAssetids().size() > 0) {

                            this.requestMessage.setVendorcode("PRAJ");

                            this.requestMessage.setCodes(Arrays.asList(new String[]{"IGN"}));


                            TrackDataMongoDaoImpl trackDataMongoDaoImpl = new TrackDataMongoDaoImpl();

                            List<WorkingHourData> alertlog = trackDataMongoDaoImpl.findPreprocessedBorewell(this.requestMessage, null, true);

                            DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");


                            if (alertlog != null || alertlog.size() != 0) {

                                Map<Long, List<WorkingHourData>> map = dataSorter(alertlog);

                                this.finalIgnMap = new HashMap<>();

                                this.workingHourDataMap = new HashMap<>();

                                for (Map.Entry<Long, List<WorkingHourData>> entry : map.entrySet()) {

                                    Device device = deviceDao.getDeviceById(entry.getKey());

                                    if (device != null) {

                                        WorkingHourData workingHourData = prepareWorkingHourReport(entry.getValue(), this.workingHourDataMap, device, false);
                                    }
                                }


                                map = null;

                                if (this.workingHourDataMap == null || this.workingHourDataMap.size() == 0) {

                                    throw new NoRecordFoundException();
                                }

                                isSuccess = true;
                            } else {

                                throw new NoRecordFoundException();
                            }
                        } else {

                            this.responseMessage.setResultcode(Integer.valueOf(24));

                            this.responseMessage.setResultDescription("Some fields missing");
                        }
                    }




                    if (isSuccess) {

                        this.responseMessage.setResultcode(Integer.valueOf(0));

                        this.responseMessage.setResultDescription("Success");
                    } else {

                        this.responseMessage.setResultcode(Integer.valueOf(36));

                        this.responseMessage.setResultDescription("Record Not Found");
                    }


                } else if (!this.user.getStatus().getCode().equals("A")) {

                    this.responseMessage.setResultcode(Integer.valueOf(37));

                    this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(37)));

                } else if (!this.user.getPin().equals(this.requestMessage.getPin())) {

                    this.responseMessage.setResultcode(Integer.valueOf(27));

                    this.responseMessage.setResultDescription("Invalid Password");
                }
            } else {


                this.responseMessage.setResultcode(Integer.valueOf(11));

                this.responseMessage.setResultDescription("User Not Found");
            }

        } catch (NoRecordFoundException ex) {

            this.responseMessage.setResultcode(Integer.valueOf(36));

            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(36)));

            ex.printStackTrace();

        } catch (EntityException ee) {

            this.responseMessage.setResultcode(ee.getResultCode(Integer.valueOf(0)));

            this.responseMessage.setResultDescription(ee.getDescription());

            ee.printStackTrace();

        } catch (Exception ex) {

            this.responseMessage.setResultcode(Integer.valueOf(206));

            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));

            ex.printStackTrace();
        } finally {

            if (this.responseMessage.getResultcode().intValue() == 0) {

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

            JSONObject jsonObject = responseHeader(this.requestMessage, this.responseMessage);

            if (this.requestMessage.getTranstype().equalsIgnoreCase("DASHBOARD")) {

                jsonObject.put("online", onlineReport());

                jsonObject.put("offline", offlineReport());

                jsonObject.put("summary", dashSummary());

            } else if (this.requestMessage.getTranstype().equalsIgnoreCase("REPORT")) {

                jsonObject.put("data", detailedReport());

                jsonObject.put("summary", summaryReport());
            }


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

    public JSONObject detailedReport() {
        JSONObject deviceObj = new JSONObject();
        try {
            if (this.workingHourDataMap != null && this.workingHourDataMap.size() > 0) {
                this.workingHourDataMap.forEach((key, value) -> {
                    try {
                        WorkingHourData finalIgnobj = value;

                        JSONArray jsonArray = new JSONArray();

                        try {
                            WorkingHourData data = finalIgnobj;

                            JSONObject jsonObject = new JSONObject();

                            jsonObject.put("vehnum", data.getVehicleNumber());
                            jsonObject.put("geocode", data.getLatitude() + "," + data.getLongitude());
                            jsonObject.put("address", data.getAddress());
                            jsonObject.put("ign", data.getIgnStatus());
                            jsonObject.put("startdate", nullAndEmpty(data.getStartDate(), "-"));
                            jsonObject.put("enddate", data.getEndDate());
//                            jsonObject.put("duration", Utility.convertSecToDuration(data.getStateDuration().longValue()));
                            jsonObject.put("duration", Utility.convertSecToDurationHH(data.getStateDuration().longValue()));
                            jsonObject.put("seconds", data.getStateDuration());
                            jsonArray.put(jsonObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        deviceObj.put(key + "", jsonArray);
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceObj;
    }

    public JSONArray summaryReport() {

        JSONArray finalJsonArray = new JSONArray();
        try {
            if (this.workingHourDataMap != null && this.workingHourDataMap.size() > 0) {
                for (Map.Entry<Long, WorkingHourData> consolidatedReport : this.workingHourDataMap.entrySet()) {
                    try {

                        WorkingHourData data = consolidatedReport.getValue();

                        JSONObject jsonObject = new JSONObject();

                        jsonObject.put("id", data.getAssetId() + "");

                        jsonObject.put("vehnum", data.getVehicleNumber());

                        jsonObject.put("accid", data.getAccId());

                        jsonObject.put("accheirarchy", heirarchyBuilder(data.getAccId()));

                        jsonObject.put("accname", data.getUdv1());

                        if (checkNullAndEmpty(this.requestMessage.getIgnStatus())) {

                            Long duration = Long.valueOf(0L);

                            if (this.requestMessage.getIgnStatus().equalsIgnoreCase("Y")) {

                                duration = data.getOnDuration();

                            } else if (this.requestMessage.getIgnStatus().equalsIgnoreCase("N")) {

                                duration = data.getOffDuration();
                            }
                            try {

//                                jsonObject.put("duration", Utility.convertSecToDuration(duration.longValue()));
                                jsonObject.put("duration", Utility.convertSecToDurationHH(duration.longValue()));

                                jsonObject.put("seconds", duration);

                            } catch (Exception exception) {
                            }
                        } else {


                            jsonObject.put("online", data.getOnDuration());

                            jsonObject.put("offline", data.getOffDuration());

//                            jsonObject.put("duration",
//                  Utility.convertSecToDuration(data.getOnDuration().longValue() + data.getOffDuration().longValue()));
                            jsonObject.put("duration",
                                    Utility.convertSecToDurationHH(data.getOnDuration().longValue() + data.getOffDuration().longValue()));
                        }

                        finalJsonArray.put(jsonObject);

                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return finalJsonArray;
    }

    public JSONArray dashSummary() {
        JSONArray jsonArray = new JSONArray();
        try {

            Map<String, WorkingHourData> LowerloginLevelWorkingMap = new HashMap<>();

            Long onDuration = Long.valueOf(0L);

            Long offDuration = Long.valueOf(0L);

            for (Map.Entry<String, List<WorkingHourData>> entry : this.accWorkingDataMap.entrySet()) {

                onDuration = Long.valueOf(0L);

                offDuration = Long.valueOf(0L);

                for (int i = 0; i < ((List) entry.getValue()).size(); i++) {

                    WorkingHourData obj = ((List<WorkingHourData>) entry.getValue()).get(i);

                    onDuration = Long.valueOf(onDuration.longValue() + obj.getOnDuration().longValue());

                    offDuration = Long.valueOf(offDuration.longValue() + obj.getOffDuration().longValue());
                }

                WorkingHourData data = new WorkingHourData();

                data.setOnDuration(onDuration);

                data.setOffDuration(offDuration);

                data.setParentName(entry.getKey());


                if (!LowerloginLevelWorkingMap.containsKey(entry.getKey())) {

                    LowerloginLevelWorkingMap.put(entry.getKey(), data);
                }
            }

            for (WorkingHourData data : LowerloginLevelWorkingMap.values()) {

                Device device = this.deviceMap.get(data.getAccId().toString());
                try {

                    JSONObject jsonObject = new JSONObject();

                    jsonObject.put("accname", data.getParentName());

                    jsonObject.put("seconds", data.getOnDuration());
                    //TODO : AG update
//                    jsonObject.put("duration", Utility.convertSecToDuration(data.getOnDuration().longValue()));
                    jsonObject.put("duration", Utility.convertSecToDurationHH(data.getOnDuration().longValue()));

                    jsonArray.put(jsonObject);

                } catch (Exception e) {

                    e.printStackTrace();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public JSONArray onlineReport() {
        JSONArray jsonArray = new JSONArray();
        if (this.onlineList != null && this.onlineList.size() > 0) {
            int count = 1;

            for (WorkingHourData data : this.onlineList) {
                try {

                    JSONObject jsonObject = new JSONObject();

                    jsonObject.put("id", data.getAssetId() + "");

                    jsonObject.put("name", data.getVehicleNumber());

                    jsonObject.put("seconds", data.getOnDuration());

                    //TODO : AG
//                    jsonObject.put("duration", Utility.convertSecToDuration(data.getOnDuration().longValue()));
                    jsonObject.put("duration", Utility.convertSecToDurationHH(data.getOnDuration().longValue()));

                    jsonArray.put(jsonObject);

                    if (this.requestMessage.getFlag().equalsIgnoreCase(count + "")) {
                        break;
                    }

                    count++;

                } catch (Exception exception) {
                }
            }



            this.onlineList = null;
        }

        return jsonArray;
    }

    public JSONArray offlineReport() {

        JSONArray jsonArray = new JSONArray();

        if (this.offlineList != null && this.offlineList.size() > 0) {

            int count = 1;

            for (WorkingHourData data : this.offlineList) {
                try {

                    JSONObject jsonObject = new JSONObject();

                    jsonObject.put("id", data.getAssetId() + "");

                    jsonObject.put("name", data.getVehicleNumber());

                    jsonObject.put("seconds", data.getOffDuration());

                    // TODO : AG
//                    jsonObject.put("duration", Utility.convertSecToDuration(data.getOffDuration().longValue()));
                    jsonObject.put("duration", Utility.convertSecToDurationHH(data.getOffDuration().longValue()));
                    jsonArray.put(jsonObject);

                    if (this.requestMessage.getFlag().equalsIgnoreCase(count + "")) {
                        break;
                    }

                    count++;

                } catch (Exception exception) {
                }
            }

            this.offlineList = null;
        }

        return jsonArray;
    }

    public List<WorkingHourData> oldPrepareWorkingHourReport(List<AlertMongoLog> alertLogList, Device device, boolean forDashboard) {
        List<WorkingHourData> dataList = new ArrayList<>();
        this.workingHourDataMap = new HashMap<>();
        int prevPointer = 0;
        try {

            AlertMongoLog firstData = alertLogList.get(0);

            WorkingHourData firstwhdata = new WorkingHourData();


            firstwhdata.setLatitude(firstData.getLatitude());

            firstwhdata.setLongitude(firstData.getLongitude());

            firstwhdata.setVehicleNumber(device.getLicenseno());

            firstwhdata.setAddress(firstData.getAddress());

            firstwhdata.setAccId(firstData.getAccId());

            firstwhdata.setAssetId(firstData.getAssetId());

            firstwhdata.setUdv1(device.getAccount().getName());


            firstwhdata.setStartDate(this.requestMessage.getStartDate());

            firstwhdata.setEndDate(firstData.getCreatedOn());

            try {

                Long durInSec = Long.valueOf(Utility.calculateTravelTime(firstwhdata.getStartDate(), firstwhdata.getEndDate()));

                firstwhdata.setStateDuration(durInSec);
            }
            catch (Exception exception) {
            }



            if (firstData.getValue().trim().toUpperCase().equalsIgnoreCase("Y")) {

                firstwhdata.setIgnStatus("N");

                firstwhdata.setOffDuration(firstwhdata.getStateDuration());
            } else {

                firstwhdata.setIgnStatus("Y");

                firstwhdata.setOnDuration(firstwhdata.getStateDuration());
            }

            this.workingHourDataMap.put(firstwhdata.getAssetId(), firstwhdata);


            if (dataSelector(this.requestMessage.getIgnStatus(), firstwhdata.getIgnStatus())) {

                dataList.add(firstwhdata);
            }


            for (int i = 1; i < alertLogList.size(); i++) {
                try {

                    AlertMongoLog currentData = alertLogList.get(i);

                    AlertMongoLog previousData = alertLogList.get(prevPointer);


                    if (!previousData.getValue().equalsIgnoreCase(currentData.getValue())) {

                        firstwhdata = this.workingHourDataMap.get(currentData.getAssetId());


                        WorkingHourData data = new WorkingHourData();

                        data.setLatitude(previousData.getLatitude());

                        data.setLongitude(previousData.getLongitude());

                        data.setVehicleNumber(device.getLicenseno());

                        data.setAddress(previousData.getAddress());

                        data.setAccId(previousData.getAccId());

                        data.setAssetId(previousData.getAssetId());

                        data.setStartDate(previousData.getCreatedOn());

                        data.setEndDate(currentData.getCreatedOn());

                        data.setIgnStatus(previousData.getValue().trim().toUpperCase());

                        data.setIMEI(previousData.getIMEI());

                        data.setOnDuration(firstwhdata.getOnDuration());

                        data.setOffDuration(firstwhdata.getOffDuration());
                        try {

                            Long durInSec = Long.valueOf(Utility.calculateTravelTime(data.getStartDate(), data.getEndDate()));

                            data.setStateDuration(durInSec);

                        } catch (Exception exception) {
                        }



                        if (data.getIgnStatus().trim().toUpperCase().equalsIgnoreCase("N")) {

                            data.setOffDuration(Long.valueOf(data.getOffDuration().longValue() + data.getStateDuration().longValue()));
                        } else {

                            data.setOnDuration(Long.valueOf(data.getOnDuration().longValue() + data.getStateDuration().longValue()));
                        }

                        data.setUdv1(device.getAccount().getName());

                        this.workingHourDataMap.put(data.getAssetId(), data);


                        if (dataSelector(this.requestMessage.getIgnStatus(), data.getIgnStatus())) {

                            dataList.add(data);
                        }


                        prevPointer = i;
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }


            AlertMongoLog lastdata = alertLogList.get(alertLogList.size() - 1);

            firstwhdata = this.workingHourDataMap.get(lastdata.getAssetId());


            WorkingHourData lastwhdata = new WorkingHourData();

            lastwhdata.setLatitude(lastdata.getLatitude());

            lastwhdata.setLongitude(lastdata.getLongitude());

            lastwhdata.setVehicleNumber(device.getLicenseno());

            lastwhdata.setAddress(lastdata.getAddress());

            lastwhdata.setAccId(lastdata.getAccId());

            lastwhdata.setAssetId(lastdata.getAssetId());

            lastwhdata.setStartDate(lastdata.getCreatedOn());

            lastwhdata.setIgnStatus(lastdata.getValue());

            lastwhdata.setEndDate(this.requestMessage.getEndDate());


            lastwhdata.setOffDuration(firstwhdata.getOffDuration());

            lastwhdata.setOnDuration(firstwhdata.getOnDuration());

            lastwhdata.setIMEI(device.getIMEI());
            try {

                Long durInSec = Long.valueOf(Utility.calculateTravelTime(lastwhdata.getStartDate(), lastwhdata.getEndDate()));
                /* 649 */
                lastwhdata.setStateDuration(durInSec);

            } catch (Exception exception) {
            }



            if (lastwhdata.getIgnStatus().trim().toUpperCase().equalsIgnoreCase("N")) {

                lastwhdata.setOffDuration(Long.valueOf(lastwhdata.getOffDuration().longValue() + lastwhdata.getStateDuration().longValue()));
            } else {

                lastwhdata.setOnDuration(Long.valueOf(lastwhdata.getOnDuration().longValue() + lastwhdata.getStateDuration().longValue()));
            }


            lastwhdata.setUdv1(device.getAccount().getName());

            this.workingHourDataMap.put(lastwhdata.getAssetId(), lastwhdata);


            if (dataSelector(this.requestMessage.getIgnStatus(), lastwhdata.getIgnStatus())) {

                dataList.add(lastwhdata);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (forDashboard) {
            return null;
        }
        return dataList;
    }


    public WorkingHourData prepareWorkingHourReport(List<WorkingHourData> alertLogList, Map<Long, WorkingHourData> workingHourDataMap, Device device, boolean forDashboard) {
        WorkingHourData whdata = new WorkingHourData();
        Long onduration = Long.valueOf(0L);
        Long offduration = Long.valueOf(0L);
        for (int i = 0; i < alertLogList.size(); i++) {
            WorkingHourData obj = alertLogList.get(i);
            onduration = Long.valueOf(onduration.longValue() + obj.getOnDuration().longValue());

            offduration = Long.valueOf(offduration.longValue() + obj.getOffDuration().longValue());
        }

        whdata.setAccId(((WorkingHourData) alertLogList.get(0)).getAccId());
        whdata.setIMEI(((WorkingHourData) alertLogList.get(0)).getIMEI());
        whdata.setIgn(((WorkingHourData) alertLogList.get(0)).getIgn());
        whdata.setParentName(((WorkingHourData) alertLogList.get(0)).getParentName());
        whdata.setVehicleNumber(((WorkingHourData) alertLogList.get(0)).getVehicleNumber());
        whdata.setVehicleName(((WorkingHourData) alertLogList.get(0)).getVehicleName());
        whdata.setAddress(((WorkingHourData) alertLogList.get(0)).getAddress());
        whdata.setAssetId(((WorkingHourData) alertLogList.get(0)).getAssetId());
        whdata.setStartDate(((WorkingHourData) alertLogList.get(0)).getStartDate());
        whdata.setEndDate(((WorkingHourData) alertLogList.get(0)).getEndDate());
        whdata.setOnDuration(onduration);
        whdata.setOffDuration(offduration);
        whdata.setUdv1(device.getAccount().getName());

        if (!workingHourDataMap.containsKey(device.getId())) {
            workingHourDataMap.put(device.getId(), whdata);
        }

        return whdata;
    }


    private boolean dataSelector(String userWant, String evaluated) {
        boolean toAdd = true;
        if (checkNullAndEmpty(userWant) && !userWant.equalsIgnoreCase(evaluated)) {
            toAdd = false;
        }
        return toAdd;
    }

    private void addDeviceIfAbsent() {
        WorkingHourData dummyData = new WorkingHourData();
        for (Device device : this.deviceMap.values()) {
            dummyData.setVehicleNumber(device.getLicenseno().trim());
            dummyData.setAccId(device.getAccId());
            dummyData.setAssetId(device.getId());
            dummyData.setStartDate(this.requestMessage.getStartDate());
            dummyData.setIgnStatus("N");
            try {
                dummyData.setEndDate(this.requestMessage.getEndDate());
                Long durInSec = Long.valueOf(Utility.calculateTravelTime(dummyData.getStartDate(), dummyData.getEndDate()));
                dummyData.setOffDuration(durInSec);
            } catch (Exception exception) {
            }

            this.workingHourDataMap.putIfAbsent(device.getAssetId(), dummyData);
        }
    }

    private Map<Long, List<WorkingHourData>> dataSorter(List<WorkingHourData> alertlog) {
        Map<Long, List<WorkingHourData>> map = new HashMap<>();
        alertlog.stream().forEach(alertMongoLog -> {
            if (map.containsKey(alertMongoLog.getAssetId())) {
                List<WorkingHourData> logList = (List<WorkingHourData>) map.get(alertMongoLog.getAssetId());
                logList.add(alertMongoLog);
                map.put(alertMongoLog.getAssetId(), logList);
            } else {
                List<WorkingHourData> logList = new ArrayList<>();
                logList.add(alertMongoLog);
                map.put(alertMongoLog.getAssetId(), logList);
            }
        });
        return map;
    }

    private WorkingHourData workingHourDataObj(AlertMongoLog data, Device device) {
        WorkingHourData whdata = new WorkingHourData();
        whdata.setLatitude(data.getLatitude());
        whdata.setLongitude(data.getLongitude());
        whdata.setVehicleNumber(device.getLicenseno());
        whdata.setAddress(data.getAddress());
        whdata.setAccId(device.getAccId());
        whdata.setAssetId(data.getAssetId());
        whdata.setStartDate(data.getCreatedOn());
        whdata.setEndDate(data.getCreatedOn());
        whdata.setIgnStatus(data.getValue().trim().toUpperCase());
        whdata.setUdv1(device.getAccount().getName());
        whdata.setIMEI(device.getIMEI());
        return whdata;
    }

    private JSONObject heirarchyBuilder(Long accId) {
        JSONObject object = new JSONObject();
        if (this.accountHeirarchyMap.containsKey(accId)) {
            object = this.accountHeirarchyMap.get(accId);
        } else {
            try {
                String[] hiearch = {"district", "block", "panchayat"};

                Long currentLevelId = accId;
                int j = 0;
                int i;

                for (i = 3; i > 0; i--) {
                    try {

                        Account account = getObjFromAccountMap(currentLevelId);

                        if (account != null) {

                            object.put(hiearch[i - 1], account.getName());

                            currentLevelId = account.getParentAccountId();
                        } else {

                            j = i;
                            break;
                        }

                    } catch (Exception e) {

                        j = i;

                        break;
                    }
                }
                if (j > 0) {
                    for (i = 0; i < j; i++) {
                        object.put(hiearch[i], "NA");
                    }
                }
                this.accountHeirarchyMap.put(accId, object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return object;
    }

    public FunctionalReportProcessor() {
    }
}
