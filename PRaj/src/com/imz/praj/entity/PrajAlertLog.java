package com.imz.praj.entity;



import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.AlertLog;
import com.traq.common.data.entity.AlertType;
import com.traq.common.data.entity.Device;
import com.traq.common.data.model.dao.AlertTypeDao;
import com.traq.common.data.model.dao.DeviceDao;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class PrajAlertLog extends RequestMessage {
    private ObjectId docId;
    private String delivered;
    private String failure;
    private String vehicleNumber;
    private static Map<Long, String> accnamemap = new HashMap<>(); private Long alertid; private Long deviceTypeId; private String misc; private int count; private String value;
    private static Map<String, String> alerttypemap = new HashMap<>();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Long timeDiff = 19800000L;     // GMT and IST Difference in milliseconds

    public String getMisc() {
        return this.misc;
    }

    public void setMisc(String misc) {
        this.misc = misc;
    }

    public String getDelivered() {
        return this.delivered;
    }

    public void setDelivered(String delivered) {
        this.delivered = delivered;
    }

    public String getFailure() {
        return this.failure;
    }

    public void setFailure(String failure) {
        this.failure = failure;
    }

    public ObjectId getDocId() {
        return this.docId;
    }

    public void setDocId(ObjectId docId) {
        this.docId = docId;
    }

    public String getVehicleNumber() {
        return this.vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Long getAlertid() {
        return this.alertid;
    }

    public void setAlertid(Long alertid) {
        this.alertid = alertid;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getDeviceTypeId() {
        return this.deviceTypeId;
    }

    public void setDeviceTypeId(Long deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }


    public static JSONObject toJson(PrajAlertLog alertLog) {
        JSONObject object = new JSONObject();
        try {
            //object.put("id", alertLog.getDocId());
            object.put("lat", alertLog.getLatitude() + "");
            object.put("lng", alertLog.getLongitude() + "");
            //object.put("accid", alertLog.getAccId());
            //object.put("assetid", alertLog.getAssetId());
            object.put("vehnum", alertLog.getVehicleNumber());
            //object.put("address", alertLog.getAddress());

            try {
                Date date = new Date(alertLog.getOrgtsMillis() - timeDiff);
                object.put("cts", sdf.format(date));
            }catch (Exception ee){
                object.put("cts", alertLog.getCreatedOn());
            }
            object.put("type", BaseInitializer.nullAndEmpty(alertLog.getCode(), "NA"));
            object.put("code", BaseInitializer.nullAndEmpty(alertLog.getCode(), "NA"));
            object.put("value", BaseInitializer.nullAndEmpty(alertLog.getValue(), "NA"));
            if (object.getString(Constants.NODETYPE).equals("EWA")) {
                object.put("message", "Volume of Water used more than 1,00,000 ltr per day.");
            }else if (object.getString(Constants.NODETYPE).equals("NFS")){
                object.put("message", "Scheme non-functional from more than 24 hrs");
            }else if (object.getString(Constants.NODETYPE).equals("NFI")){
                object.put("message", "IOT Device non-functional from more than 24 hrs");
            }else if (object.getString(Constants.NODETYPE).equals("TAMP")){
                object.put("message", "Tamper Alert");
            }else {
                object.put("message", BaseInitializer.nullAndEmpty(alertLog.getMessage(), "Misc Alert"));
            }

            Account acc = BaseInitializer.getAccountMap().get(alertLog.getAccId());
            if (acc != null) {
                object.accumulate("accname", acc.getName());
                Account parent = BaseInitializer.getAccountMap().get(acc.getParentAccountId());
                if(parent.getId() > 0) {
                    object.accumulate(Constants.NODEBLOCK, parent.getName());
                }else{
                    object.accumulate(Constants.NODEBLOCK, "NA");
                }
                Account gpAcc = BaseInitializer.getAccountMap().get(parent.getParentAccountId());
                if(gpAcc.getId() > 0) {
                    object.accumulate(Constants.NODEDISTRICT, gpAcc.getName().replaceAll("PRAJ","").trim());
                }else{
                    object.accumulate(Constants.NODEDISTRICT, "NA");
                }

            }else{
                object.put("accname", "NA");
                object.accumulate(Constants.NODEBLOCK, "NA");
                object.accumulate(Constants.NODEDISTRICT, "NA");
            }


            if (alerttypemap.get(alertLog.getCode()) != null) {
                object.put("name", alerttypemap.get(alertLog.getCode()));
            } else {
                AlertType alt = null;
                if("TMP".equals(alertLog.getCode())){
                    alertLog.setCode("TAMP");
                }
                if (BaseInitializer.getAlertTypeMap().get(alertLog.getCode()) != null) {
                    alt = (AlertType)BaseInitializer.getAlertTypeMap().get(alertLog.getCode());
                } else {
                    AlertTypeDao alertTypeDao = (AlertTypeDao)ApplicationBeanContext.getInstance().getBean("alertTypeDao");
                    alt = alertTypeDao.getAlertTypeByCode(alertLog.getCode());
                }
                if (alt != null) {
                    object.put("name", alt.getName());
                    BaseInitializer.getAlertTypeMap().put(alertLog.getCode(), alt);
                    alerttypemap.put(alertLog.getCode(), alt.getName());
                } else {
                    object.put("name", "NA");
                }

            }
        } catch (JSONException je) {
            je.printStackTrace();
            return null;
        }
        catch (NullPointerException nfe) {
            nfe.printStackTrace();
            return null;
        }
        return object;
    }

    public static JSONObject toJsonCust(PrajAlertLog alertLog) {
        JSONObject object = new JSONObject();
        try {
            SimpleDateFormat dataPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            try {
                object.put("deliveredts", BaseInitializer.nullAndEmpty(alertLog.getDelivered(), dataPattern.format(new Date())));
            } catch (JSONException e) {
                object.put("deliveredts", BaseInitializer.nullAndEmpty(alertLog.getDelivered(), "Y"));
            }
            object.put("alertId", alertLog.getDocId());
            object.put("data", BaseInitializer.nullAndEmpty(alertLog.getIMEI(), "DASHBOARD"));

            object.put("id", alertLog.getDocId());
            object.put("lat", alertLog.getLatitude() + "");
            object.put("lng", alertLog.getLongitude() + "");
            object.put("accid", alertLog.getAccId());
            object.put("assetid", alertLog.getAssetId());
            object.put("comments", BaseInitializer.nullAndEmpty(alertLog.getComments(), "NA"));
            object.put("statusdesc", BaseInitializer.nullAndEmpty(alertLog.getStatusCode(), "SUCCESS"));
            try {
                if (null == alertLog.getVehicleNumber() || alertLog.getVehicleNumber().trim().isEmpty()) {
                    DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
                    Device device = deviceDao.getDeviceById(Long.valueOf(alertLog.getAssetId().longValue()));
                    object.put("assetid", device.getId());
                    object.put("vehnum", device.getLicenseno());
                }
            } catch (Exception exception) {}


            object.put("vehnum", alertLog.getVehicleNumber());

            object.put("lat", alertLog.getLatitude() + "");
            object.put("lng", alertLog.getLongitude() + "");
            object.put("address", alertLog.getAddress());
            object.put("message", BaseInitializer.nullAndEmpty(alertLog.getAddress(), "Default Message."));
            object.put("cts", alertLog.getCreatedOn());
            object.put("type", BaseInitializer.nullAndEmpty(alertLog.getCode(), "NA"));
            object.put("code", BaseInitializer.nullAndEmpty(alertLog.getCode(), "NA"));
            object.put("value", BaseInitializer.nullAndEmpty(alertLog.getValue(), "NA"));
            object.put("failuremessage", BaseInitializer.nullAndEmpty(alertLog.getFailure(), "Y"));
            object.put("status", "P");

            if (accnamemap.get(alertLog.getAccId()) != null) {
                object.put("accname", accnamemap.get(alertLog.getAccId()));
            } else {
                Account acc = BaseInitializer.getObjFromAccountMap(alertLog.getAccId());

                if (acc != null) {
                    object.put("accname", acc.getName());
                    BaseInitializer.getAccountMap().put(alertLog.getAccId(), acc);
                    accnamemap.put(alertLog.getAccId(), acc.getName());
                } else {
                    object.put("accname", "NA");
                }
            }

            if (alerttypemap.get(alertLog.getCode()) != null) {
                object.put("name", alerttypemap.get(alertLog.getCode()));
            } else {
                AlertType alt = null;
                if (BaseInitializer.getAlertTypeMap().get(alertLog.getCode()) != null) {
                    alt = (AlertType)BaseInitializer.getAlertTypeMap().get(alertLog.getCode());
                } else {
                    AlertTypeDao alertTypeDao = (AlertTypeDao)ApplicationBeanContext.getInstance().getBean("alertTypeDao");
                    alt = alertTypeDao.getAlertTypeByCode(alertLog.getCode());
                }
                if (alt != null) {
                    object.put("name", alt.getName());
                    BaseInitializer.getAlertTypeMap().put(alertLog.getCode(), alt);
                    alerttypemap.put(alertLog.getCode(), alt.getName());
                } else {
                    object.put("name", "NA");
                }

            }
        } catch (JSONException je) {
            je.printStackTrace();
            return null;
        }
        catch (NullPointerException nfe) {
            nfe.printStackTrace();
            return null;
        }
        return object;
    }

    public static JSONObject toJsonPraj(PrajAlertLog alertLog) {
        JSONObject object = new JSONObject();
        try {
            object.put("accid", alertLog.getAccId());
            object.put("assetid", alertLog.getAssetId());

            if (object.getString(Constants.NODETYPE).equals("EWA")) {
                object.put("message", "Excess Water");
            }
            object.put("vehnum", alertLog.getVehicleNumber());
            object.put("lat", alertLog.getLatitude() + "");
            object.put("lng", alertLog.getLongitude() + "");
            object.put("address", alertLog.getAddress());
            object.put("cts", alertLog.getCreatedOn());
            object.put("orgmillis", alertLog.getOrgtsMillis());
            object.put("type", BaseInitializer.nullAndEmpty(alertLog.getCode(), "NA"));
            object.put("code", BaseInitializer.nullAndEmpty(alertLog.getCode(), "NA"));
            object.put("value", BaseInitializer.nullAndEmpty(alertLog.getValue(), "NA"));

            if (accnamemap.get(alertLog.getAccId()) != null) {
                object.put("accname", accnamemap.get(alertLog.getAccId()));
            } else {
                Account acc = BaseInitializer.getObjFromAccountMap(alertLog.getAccId());
                if (acc != null) {
                    object.put("accname", acc.getName());
                    BaseInitializer.getAccountMap().put(alertLog.getAccId(), acc);
                    accnamemap.put(alertLog.getAccId(), acc.getName());
                } else {
                    object.put("accname", "NA");
                }
            }
        } catch (JSONException je) {
            je.printStackTrace();
            return null;
        }
        catch (NullPointerException nfe) {
            nfe.printStackTrace();
            return null;
        }
        return object;
    }

    public static JSONArray toJsonArr(List<PrajAlertLog> alertLogs) {
        JSONArray jsonArray = new JSONArray();
        try {
            if (alertLogs != null)
                for (PrajAlertLog alert : alertLogs) {
                    JSONObject jsonObj = toJson(alert);
                    if (jsonObj != null) {
                        jsonArray.put(jsonObj);
                    }
                }
        } catch (Exception je) {
            je.printStackTrace();
        }

        return jsonArray;
    }


    public static JSONArray toJsonArrPraj(List<PrajAlertLog> alertLogs) {
        JSONArray jsonArray = new JSONArray();
        try {
            if (alertLogs != null)
                for (PrajAlertLog alert : alertLogs) {
                    JSONObject jsonObj = toJsonPraj(alert);
                    if (jsonObj != null) {
                        jsonArray.put(jsonObj);
                    }
                }
        } catch (Exception je) {
            je.printStackTrace();
        }
        return jsonArray;
    }

    public static JSONArray toJsonArrCust(List<PrajAlertLog> alertLogs) {
        JSONArray jsonArray = new JSONArray();
        try {
            if (alertLogs != null)
                for (PrajAlertLog alert : alertLogs) {
                    JSONObject jsonObj = toJsonCust(alert);
                    if (jsonObj != null) {
                        jsonArray.put(jsonObj);
                    }
                }
        } catch (Exception je) {
            je.printStackTrace();
        }

        return jsonArray;
    }

    public static JSONObject toJson(Map<String, List<PrajAlertLog>> alertLogs) {
        JSONObject mainObj = new JSONObject();

        try {
            if (alertLogs != null) {
                Iterator<String> itr = alertLogs.keySet().iterator();
                while (itr.hasNext()) {
                    JSONArray jsonArray = new JSONArray();
                    String key = itr.next();
                    for (PrajAlertLog alert : alertLogs.get(key)) {
                        JSONObject jsonObj = toJson(alert);
                        if (jsonObj != null)
                            jsonArray.put(jsonObj);
                    }
                    mainObj.put(key, jsonArray);
                }
            }
        } catch (Exception je) {
            je.printStackTrace();
        }

        return mainObj;
    }

    public static Comparator<PrajAlertLog> timeComparator = new Comparator<PrajAlertLog>()
    {
        public int compare(PrajAlertLog t1, PrajAlertLog t2) {
            String time1 = t1.getCreatedOn();
            String time2 = t2.getCreatedOn();


            return time1.compareTo(time2);
        }
    };




    public static List<PrajAlertLog> createList(FindIterable<Document> resultList) {
        List<PrajAlertLog> alertLogList = new ArrayList<>();
        try {
            for (MongoCursor<Document> mongoCursor = resultList.iterator(); mongoCursor.hasNext(); ) { Document doc = mongoCursor.next();
                PrajAlertLog log = new PrajAlertLog();
                //         log.setDocId(doc.getObjectId("_id"));
                log.setAccId(doc.getLong("accid"));
                log.setAssetId(doc.getLong("deviceid"));
                log.setVehicleNumber(doc.getString("vehnum"));
                log.setCode(doc.getString("type"));
                log.setValue(doc.getString("value"));

                try {
                    log.setLatitude(doc.getDouble("lat"));
                } catch (Exception exception) {}

                try {
                    log.setLongitude(doc.getDouble("lng"));
                } catch (Exception exception) {}

                log.setAddress(doc.getString("address"));
                log.setCreatedOn(doc.getString("cts"));

                try {
                    log.setOrgtsMillis(Long.valueOf(Long.parseLong(doc.getString(Constants.NODEORIGINTSMILLI))));
                } catch (Exception exception) {
                    try {
                        log.setOrgtsMillis(doc.getLong(Constants.NODEORIGINTSMILLI));
                    } catch (Exception ex) {
                    }
                }

                try {
                    log.setReason(doc.getString("eventsourcetype"));
                } catch (Exception exception) {
                    log.setReason("NA");
                }
                try {
                    log.setRfId(doc.getString("rfid"));
                } catch (Exception exception) {
                    log.setRfId("NA");
                }
                try {
                    log.setMessage(doc.getString("message"));
                } catch (Exception exception) {}

                alertLogList.add(log); }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return alertLogList;
    }



    public static List<AlertLog> createListForAlertLog(FindIterable<Document> resultList) {
        List<AlertLog> alertLogList = new ArrayList<>();
        try {
            for (MongoCursor<Document> mongoCursor = resultList.iterator(); mongoCursor.hasNext(); ) { Document doc = mongoCursor.next();
                AlertLog log = new AlertLog();
                log.setDocId(doc.getObjectId("_id"));
                log.setAccId(doc.getLong("accid"));
                log.setAssetId(doc.getLong("deviceid"));
                log.setCode(doc.getString("type"));
                log.setValue(doc.getString("value"));
                try {
                    log.setLatitude(doc.getDouble("lat"));
                } catch (Exception exception) {}

                try {
                    log.setLongitude(doc.getDouble("lng"));
                } catch (Exception exception) {}

                log.setAddress(doc.getString("address"));
                log.setCreatedOn(doc.getString("cts"));

                try {
                    log.setOrgtsMillis(Long.valueOf(Long.parseLong(doc.getString(Constants.NODEORIGINTSMILLI))));
                } catch (Exception exception) {
                    try {
                        log.setOrgtsMillis(doc.getLong(Constants.NODEORIGINTSMILLI));
                    } catch (Exception ex) {
                    }
                }


                try {
                    log.setMessage(doc.getString("message"));
                } catch (Exception exception) {}


                alertLogList.add(log); }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return alertLogList;
    }
}
