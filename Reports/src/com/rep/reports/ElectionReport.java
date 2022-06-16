package com.rep.reports;

import au.com.bytecode.opencsv.CSVWriter;
import com.rep.beanobject.InOutHistory;
import com.rep.beanobject.MahaElectionReport;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.*;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.data.model.mongodao.TrackDataMongoDao;
import com.traq.common.data.model.mongodao.mongoimpl.TrackDataMongoDaoImpl;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.exceptions.ResultCodeExceptionInterface;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.ReportHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import com.traq.util.Utility;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class ElectionReport extends BaseInitializer implements RequestProcessorInterface {

    List<Account> accountList = new ArrayList<>();
    Map<String, List<TrackData>> trackMap = new HashMap<>();
    Map<String, Device> deviceMap = new HashMap<>();
    List<MahaElectionReport> finalreport = new ArrayList<>();
    Map<Long, DistanceHistory> distanceMap = new HashMap<>();
    Map<String, List<MahaElectionReport>> csvreport = new HashMap<>();
    List<TrackData> dataList = new ArrayList<>();
    List<InOutHistory> lockReport = new ArrayList<>();
    SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    RequestBean rb;
    User user = null;
    private ResponseMessage responseMessage;
    private RequestMessage requestMessage;
    private String request;

    public ElectionReport(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public ElectionReport() {
    }

    public ResponseMessage getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String executeXML(RequestBean _rb) {
        return request;
    }


    public String executeJSON(RequestBean _rb) {
        rb = _rb;
        request = rb.getRequest();

        ReportHandler dh = new ReportHandler();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        String response = "";

        try {
            UserDao userDao = (UserDao) ApplicationBeanContext.getInstance().getBean("userDao");
            responseMessage = new ResponseMessage();
            JSONObject object = new JSONObject(request);
            requestMessage = dh.getRequest(object.getJSONObject(Constants.NODEREQUEST), TagValues.getNodeValue(object, Constants.NODEREQUESTTYPE));

            Date date = newPattern.parse(requestMessage.getStartDate());
            requestMessage.setStartDate(dbPattern.format(date));
            date = newPattern.parse(requestMessage.getEndDate());
            requestMessage.setEndDate(dbPattern.format(date));

            user = userDao.login(requestMessage.getUsername());
            boolean isValid = validateUser(user, requestMessage.getPin());

            if (isValid) {
                responseMessage.setResultcode(ResultCodeExceptionInterface._SUCCESS);
                responseMessage.setResultDescription(ResultCodeDescription._DES_SUCCESS);
                responseMessage.setMessagetype(requestMessage.getMessagetype());
                responseMessage.setResponsects(sdf.format(new Date()));

                    /*AccountDao accountDao = (AccountDao) ApplicationBeanContext.getInstance().getBean("accountDao");
                    Account account = accountDao.getAccountById(requestMessage.getAccId());
                    if (account.getType().equalsIgnoreCase("HO")) {
                        accountList = accountDao.dropdownFindAll(true);
                    } else {
                        accountList = accountDao.AllChildren(account.getId());
                        if (accountList != null) {
                            accountList.add(account);
                        } else {
                            accountList.add(account);
                        }
                    }*/

                    /*ProcessorDao processorDao = (ProcessorDao) ApplicationBeanContext.getInstance().getBean("processorDao");
                    List<DistanceHistory> distance = processorDao.findTrackByAccount(accountList, requestMessage.getStartDate(), requestMessage.getEndDate());
                    if (distance == null || distance.size() == 0) {
                        throw new NoRecordFoundException();
                    } else {
                        distance.stream().forEach(dist -> {
                            if (distanceMap.containsKey(dist.getDeviceId())) {
                                DistanceHistory distnce = distanceMap.get(dist.getDeviceId());
                                distnce.setDistance((Double.parseDouble(distnce.getDistance()) + Double.parseDouble(dist.getDistance()))+"");
                                distnce.setDuration((Double.parseDouble(distnce.getDuration()) + Double.parseDouble(dist.getDuration()))+"");
                                distanceMap.put(dist.getDeviceId(), distnce);
                            } else {
                                DistanceHistory distnce = dist;
                                distanceMap.put(distnce.getDeviceId(), distnce);
                            }
                        });
                        //System.out.println("distance data...."+distanceMap.size());
                    }*/

                    /*DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
                    List<Device> deviceList = deviceDao.getDeviceListByAccount(this.accountList,0l);

                    TrackDataMongoDao trackDataDao = new TrackDataMongoDaoImpl();
                    for(Device device : deviceList) {
                        try {
                            if (getAppConfig().getSwitchdb().equals("1")) {
                                if (checkNullAndEmpty(device.getAccount().getClient())) {
                                    requestMessage.setClient(device.getAccount().getClient() + "_");
                                } else {
                                    requestMessage.setClient("TM_");
                                }
                            }
                            //requestMessage.setAccId(device.getAccId());
                            requestMessage.setAssetId(device.getId());
                            List<TrackData> dataList = trackDataDao.findTrack(requestMessage, null);
                            if (dataList.size() > 0) {
                                trackMap.put(device.getId() + "", dataList);
                            }
                            deviceMap.put(device.getId() + "", device);
                        }catch (Exception e){e.printStackTrace();}
                    }*/

                    /*for (Map.Entry<String, List<TrackData>> devicedata : trackMap.entrySet()) {
                        evaluator(devicedata.getKey(), devicedata.getValue());
                    }*/
                TrackDataMongoDao trackDataDao = new TrackDataMongoDaoImpl();
                dataList = trackDataDao.findTrack(requestMessage, null);

            } else {
                responseMessage.setResultcode(ResultCodeExceptionInterface._USER_NOT_FOUND);
                responseMessage.setResultDescription(ResultCodeDescription._DES_USER_NOT_FOUND);
            }

        } catch (NoRecordFoundException nrfe) {
            responseMessage.setResultcode(ResultCodeExceptionInterface._RECORD_NOT_FOUND);
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(ResultCodeExceptionInterface._RECORD_NOT_FOUND));
        } catch (Exception ex) {
            responseMessage.setResultcode(ResultCodeExceptionInterface._TECHNICAL_FAILURE);
            responseMessage.setResultDescription(ResultCodeDescription.getDescription(ResultCodeExceptionInterface._TECHNICAL_FAILURE));
            ex.printStackTrace();
        } finally {
            if (responseMessage.getResultcode() == ResultCodeExceptionInterface._SUCCESS) {
                response = generateJSONResponse();
            } else {
                response = generateFailureResponse(responseMessage);
            }
        }
        //info("Final Response..............." + response);
        if (dataList != null && dataList.size() > 0) {
            String path = "/opt/traqmatix/tracking/core/core_test/report/";
            String[] header = {"S.No.", "Latitude", "Longitude", "Account Id", "Device id", "Vehicle No.", "IMEI", "Disha", "Speed", "Ignition", "Battery", "Lock status", "RFID", "GPS", "Satellite", "Originated", "Created", "Address"};
            /*List<MahaElectionReport> ll = new ArrayList<>();
            for(Map.Entry<String,List<MahaElectionReport>> csv : csvreport.entrySet()) {
                List<MahaElectionReport> lll = csv.getValue();
                ll.addAll(lll);
            }*/
            /*try {
                FileWriter outputfile = new FileWriter(path + "consolidated" + ".csv");
                CSVWriter writer = new CSVWriter(outputfile);
                writer.writeNext(header);

                int count = 1;
                for (MahaElectionReport maharepo : ll) {
                    String[] records = {count + "", maharepo.getVehicleNumber(), maharepo.getDriverName(), maharepo.getDriverNumber(), maharepo.getDistance(), maharepo.getStoppageCount(), maharepo.getRunningDuration(), maharepo.getStoppageDuration()};
                    writer.writeNext(records);
                    count++;
                }
                writer.close();
            }catch (Exception e){
                e.printStackTrace();
            }*/
            /*for(Map.Entry<String,List<MahaElectionReport>> csv : csvreport.entrySet()) {
                try {
                    List<MahaElectionReport> ll = csv.getValue();
                    String[] accno = {"Account No.", ll.get(0).getAccountId() + ""};
                    String[] accname = {"Account Name", csv.getKey().trim()};
                    FileWriter outputfile = new FileWriter(path + csv.getKey().trim() + ".csv");
                    CSVWriter writer = new CSVWriter(outputfile);
                    writer.writeNext(accno);
                    writer.writeNext(accname);
                    writer.writeNext(header);

                    int count = 1;
                    for (MahaElectionReport maharepo : ll) {
                        String[] records = {count + "", maharepo.getVehicleNumber(), maharepo.getDriverName(), maharepo.getDriverNumber(), maharepo.getDistance(), maharepo.getStoppageCount(), maharepo.getRunningDuration(), maharepo.getStoppageDuration()};
                        writer.writeNext(records);
                        count++;
                    }
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/
            CSVWriter writer = null;
            try {
                DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
                Device device = deviceDao.getDeviceById(requestMessage.getAssetId());
                FileWriter outputfile = new FileWriter(path + device.getIMEI().trim() + ".csv");
                writer = new CSVWriter(outputfile);
                writer.writeNext(header);
                int count = 1;
                for (TrackData track : dataList) {
                    try {
                        String[] records = {count + "", track.getLatitude() + "", track.getLongitude() + "", track.getAccId() + "", track.getDeviceId(), track.getLicenseNumber(), track.getIMEI(), track.getCourse(), track.getSpeed() + "", track.getIgnition(), track.getBattery(), track.getLockSts(), nullAndEmpty(track.getRfId(), "NA"), track.getGps(), track.getNos() + "", track.getOrgTs(), track.getInitTs(), track.getLocation()};
                        writer.writeNext(records);
                        count++;
                    } catch (Exception e) {
                    }
                }
                writer.close();
            } catch (Exception e) {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Exception ee) {
                    }
                }
            }
        }
        return response;
    }


    public String generateJSONResponse() {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = responseHeader(requestMessage, responseMessage);
            jsonObject.put(Constants.NODEREPORT, MahaElectionReport.toJsonArr(this.finalreport));
            mainObj.put(Constants.NODERESPONSE, jsonObject);
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (Exception e) {

        }

        return mainObj.toString();
    }

    public String generateFailureResponse(ResponseMessage rm) {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.NODERESPONSETYPE, rm.getMessagetype());
            jsonObject.put(Constants.NODERESULTCODE, rm.getResultcode());
            jsonObject.put(Constants.NODERESULTDESCRIPTION, rm.getResultDescription());

            mainObj.put(Constants.NODERESPONSE, jsonObject);
        } catch (JSONException je) {

        }
        return mainObj.toString();
    }

    public List<Long> idSeperator(String ids) {
        String[] arr = ids.split(",");
        List<Long> list = new ArrayList<>();
        for (String a : arr) {
            list.add(Long.parseLong(a));
        }
        return list;
    }


    public void evaluator(String deviceid, List<TrackData> dataList) {
        try {
            Long devid = Long.parseLong(deviceid);
            boolean isTSorFM = false;
            DistanceHistory devdist = distanceMap.get(devid);
            if (checkNullAndEmpty(devdist)) {
                Device device = deviceMap.get(deviceid);
                if (device == null) {
                    DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
                    device = deviceDao.getDeviceById(devid);
                }
                if (checkNullAndEmpty(device)) {
                    Double stoppageduration = 0.0D;
                    int stoppagecount = 0;
                    List<TrackHistory> track = new ArrayList<>();
                    switch (device.getDeviceType().getId().intValue()) {
                        case 14:
                        case 15:
                        case 57:
                            track = calculateIdling(dataList);
                            isTSorFM = true;
                            break;
                        default:
                            track = calculateStoppage(dataList);
                            break;
                    }

                    if (track.size() > 0) {
                        stoppagecount = track.size();
                        stoppageduration = calculateduration(track);
                    }

                    MahaElectionReport maha = new MahaElectionReport();
                    maha.setAccountId(device.getAccount().getId());
                    maha.setAccountName(device.getAccount().getName());
                    maha.setVehicleNumber(device.getLicenseno());
                    maha.setDriverName(device.getDriver().getName());
                    maha.setDriverNumber(device.getDriver().getPhoneno());
                    maha.setDistance(twoDecimalPlaces.format(Double.parseDouble(devdist.getDistance()) / 1000));
                    if (isTSorFM) {
                        Double dur = Double.parseDouble(devdist.getDuration()) - stoppageduration;
                        maha.setRunningDuration(twoDecimalPlaces.format(dur / 60.0));
                    } else {
                        maha.setRunningDuration(twoDecimalPlaces.format(Double.parseDouble(devdist.getDuration()) / 60.0));
                    }
                    maha.setStoppageCount(stoppagecount + "");
                    maha.setStoppageDuration(twoDecimalPlaces.format(stoppageduration / 60.0));
                    finalreport.add(maha);

                    if (csvreport.containsKey(maha.getAccountName().trim())) {
                        List<MahaElectionReport> rep = csvreport.get(maha.getAccountName().trim());
                        rep.add(maha);
                        csvreport.put(maha.getAccountName().trim(), rep);
                    } else {
                        List<MahaElectionReport> rep = new ArrayList<>();
                        rep.add(maha);
                        csvreport.put(maha.getAccountName().trim(), rep);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<TrackHistory> calculateIdling(List<TrackData> list) {

        TrackHistory th = new TrackHistory();
        List<TrackHistory> histories = new ArrayList<>();

        try {
            double duration = 0;
            Boolean isFirst = true;
            int count = 0;
            int ncount = 0;

            for (TrackData data : list) {
                count++;
                if (isFirst)   //check with first packet arrived
                {
                    if ("Y".equalsIgnoreCase(data.getIgnition()) && data.getSpeed() <= 7) {
                        th = new TrackHistory();

                        th.setDuration(0L);
                        th.setStartTime(data.getOrgTs());
                        th.setStopTime(data.getOrgTs());
                        if (data.getLocation().trim().length() > 100) {
                            th.setStartLocation(data.getLocation().trim().substring(0, 100));
                        } else {
                            th.setStartLocation(data.getLocation().trim());
                        }
                        th.setStopLocation(data.getLocation());
                        th.setLatitude(data.getLatitude());
                        th.setLongitude(data.getLongitude());
                        th.setDestLatitude(data.getLatitude());
                        th.setDestLongitude(data.getLongitude());
                        th.setLicenseNumber(data.getLicenseNumber());
                        th.setMaximumSpeed(data.getSpeed());
                        th.setIgnition(data.getIgnition());
                        th.setDevId(data.getDeviceId());
                        th.setStatus(Constants.ONLINE);
                        th.setAverageSpeed(data.getSpeed());
                        ncount = 0;
                        isFirst = false;
                    }
                } else {
                    if (th.getIgnition().equals("Y") && th.getAverageSpeed() <= 7) {

                        if (data.getIgnition().equals("Y") && data.getSpeed() <= 7) {
                            ncount++;
                            duration = th.getDuration() + Utility.calculateTravelTime(th.getStopTime(), data.getOrgTs());
                            th.setDevId(data.getDeviceId());
                            th.setDuration(duration);
                            th.setDestLatitude(data.getLatitude());
                            th.setDestLongitude(data.getLongitude());
                            th.setLicenseNumber(data.getLicenseNumber());
                            if (data.getLocation().trim().length() > 100) {
                                th.setStopLocation(data.getLocation().trim().substring(0, 100));
                            } else {
                                th.setStopLocation(data.getLocation().trim());
                            }
                            th.setStatus(Constants.ONLINE);
                            th.setIgnition(data.getIgnition());
                            th.setDevId(data.getDeviceId());
                            th.setAverageSpeed(data.getSpeed());
                            th.setStopTime(data.getOrgTs());

                        } else {
                            th.setIgnition(data.getIgnition());
                            th.setAverageSpeed(data.getSpeed());
                        }
                    } else {
                        th.setStartTime(data.getOrgTs());
                        th.setStopTime(data.getOrgTs());
                        th.setIgnition(data.getIgnition());
                        th.setAverageSpeed(data.getSpeed());
                    }


                    if (th.getIgnition().equals("Y") && th.getAverageSpeed() > 8 && ncount >= 1) {
                        histories.add(th);
                        isFirst = true;
                    } else if (ncount >= 1 && th.getIgnition().equals("N")) {
                        histories.add(th);
                        isFirst = true;
                    } else if (count == list.size() && ncount >= 1) {
                        histories.add(th);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return histories;

    }

    private List<TrackHistory> calculateStoppage(List<TrackData> list) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        TrackHistory th = new TrackHistory();
        List<TrackHistory> histories = new ArrayList<>();
        int count = 0;
        try {
            double distance = 0.00;
            double duration = 0;
            Boolean isFirst = true;

            for (TrackData data : list) {
                count++;
                if (isFirst)   //check with first packet arrived
                {
                    if ("N".equalsIgnoreCase(data.getIgnition())) {
                        th = new TrackHistory();

                        th.setDistance(0.00);
                        th.setDuration(0L);
                        th.setStartTime(data.getOrgTs());
                        th.setStopTime(data.getOrgTs());
                        if (data.getLocation().trim().length() > 100) {
                            th.setStartLocation(data.getLocation().trim().substring(0, 100));
                        } else {
                            th.setStartLocation(data.getLocation().trim());
                        }
                        th.setStopLocation(data.getLocation());
                        th.setLatitude(data.getLatitude());
                        th.setLongitude(data.getLongitude());
                        th.setDestLatitude(data.getLatitude());
                        th.setDestLongitude(data.getLongitude());
                        th.setLicenseNumber(data.getLicenseNumber());
                        th.setMaximumSpeed(data.getSpeed());
                        th.setIgnition(data.getIgnition());
                        th.setDevId(data.getDeviceId());
                        th.setDate(sdf.parse(data.getOrgTs().substring(0, data.getOrgTs().indexOf(" "))));
                        th.setAccountId(data.getAccId());

                        isFirst = false;
                    }
                } else {

                    duration = th.getDuration() + Utility.calculateTravelTime(th.getStopTime(), data.getOrgTs());
                    th.setDevId(data.getDeviceId());

                    th.setStopTime(data.getOrgTs());
                    th.setDate(sdf.parse(data.getOrgTs().substring(0, data.getOrgTs().indexOf(" "))));
                    th.setAccountId(data.getAccId());

                    th.setDestLatitude(data.getLatitude());
                    th.setDestLongitude(data.getLongitude());
                    th.setLicenseNumber(data.getLicenseNumber());

                    if (data.getLocation().trim().length() > 100) {
                        th.setStopLocation(data.getLocation().trim().substring(0, 100));
                    } else {
                        th.setStopLocation(data.getLocation().trim());
                    }
                    th.setStatus(Constants.OFFLINE);
                    th.setIgnition(data.getIgnition());
                    th.setDevId(data.getDeviceId());
                    th.setAverageSpeed(data.getSpeed());
                    th.setMaximumSpeed(0.0);

                    th.setDistance(0.0);
                    th.setDuration(duration);

                    if (th.getIgnition().equals("Y")) {
                        histories.add(th);
                        isFirst = true;

                    }
                    if (count == list.size()) {
                        histories.add(th);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return histories;

    }

    private Double calculateduration(List<TrackHistory> list) {
        Double duration = 0.0D;
        try {
            for (TrackHistory trh : list) {
                duration += trh.getDuration();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
    }


}


