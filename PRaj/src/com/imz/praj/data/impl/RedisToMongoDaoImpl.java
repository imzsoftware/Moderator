package com.imz.praj.data.impl;

import com.imz.praj.data.obj.GenReportData;
import com.imz.praj.data.obj.PrajReportData;
import com.imz.praj.entity.PrajReport;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.Device;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.xmlutils.XMLProcessor;
import com.traq.db.MongoConnection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class RedisToMongoDaoImpl extends BaseInitializer {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String entityName = "PRAJ_GEN_REPORT";
    private SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int asc = 1;
    private static final int desc = -1;

    public void saveGeneralReport(Map<String, String> reportMap, String client, String date) {
        MongoConnection conn = MongoConnection.getInstance();
        DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
        try {
            conn.init();

            MongoCollection mongoCollection = conn.getDatastore().getCollection(entityName);
            for (Map.Entry<String, String> rep : reportMap.entrySet()) {
                String devData =  reportMap.get(rep.getKey().toString());
                Document document = createDocument(deviceDao, rep.getKey().toString(), devData, date);
                BasicDBObject alertObj = new BasicDBObject();
                alertObj.put(Constants.NODEPANCHAYAT,document.getLong(Constants.NODEPANCHAYAT));
                alertObj.put(Constants.NODEDEVICEID,document.getLong(Constants.NODEDEVICEID));
                alertObj.put(Constants.NODEDATE,document.getString(Constants.NODEDATE));
                FindIterable<Document> trackIterable = mongoCollection.find(alertObj);
                if(!document.isEmpty()) {
                    if (trackIterable == null || (trackIterable != null && !trackIterable.iterator().hasNext())) {
                        info("save General Report ... " + document);
                        mongoCollection.insertOne(document);
                    } else {
                        mongoCollection.deleteOne(alertObj);
                        mongoCollection.insertOne(document);
                    }
                }else{
                    info("Document empty save General Report ... " + document);
                }
                expireKey("EWA_"+document.getLong(Constants.NODEDEVICEID));
            }

        } catch (Exception exception) {
        } finally {
        }
    }

    public void saveNonFuncReport(Device device, String devRedisData, Calendar cal) {
        MongoConnection conn = MongoConnection.getInstance();
        try {
            Document document = createNonFunc(device, devRedisData,cal);
            conn.init();
            MongoCollection mongoCollection = conn.getDatastore().getCollection("PRAJ_NONFUNC_REPORT");
            BasicDBObject alertObj = new BasicDBObject();
            alertObj.put(Constants.NODEPANCHAYAT,document.getLong(Constants.NODEPANCHAYAT));
            alertObj.put(Constants.NODEDEVICEID,document.getLong(Constants.NODEDEVICEID));
            alertObj.put(Constants.NODEDATE,document.getString(Constants.NODEDATE));
            FindIterable<Document> trackIterable = mongoCollection.find(alertObj);
            if(!document.isEmpty()) {
                if (trackIterable == null || (trackIterable != null && !trackIterable.iterator().hasNext())) {
                    info("save Non Functional Report ... " + document);
                    mongoCollection.insertOne(document);
                } else {
                    mongoCollection.deleteOne(alertObj);
                    mongoCollection.insertOne(document);
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
        }
    }

    private Document createDocument(DeviceDao deviceDao, String deviceId, String devData, String date){
        Document doc = new Document();

        devData = XMLProcessor.makeTag(Constants.NODERECORDS,devData);

        Device device = deviceDao.getDeviceById(Long.parseLong(deviceId));
        Long pId = BaseInitializer.getAccountMap().get(device.getAccId()).getParentAccountId();
        Account acc = BaseInitializer.getAccountMap().get(pId);
        if(acc == null ){
            info("Parent Account is  ............ "+acc);
            return doc;
        }
        Long gpId = acc.getParentAccountId();
        if(gpId == 0){
            return doc;
        }
        doc.put(Constants.NODEDISTRICT, gpId);
        doc.put(Constants.NODEBLOCK, pId);
        doc.put(Constants.NODEPANCHAYAT, device.getAccId());
        doc.put(Constants.NODEDEVICEID, device.getId());
        doc.put(Constants.NODEIMEI, device.getIMEI());
        doc.put(Constants.NODEDATE, date);
        List<Document> dateArray = new ArrayList<Document>();
        List<String> record = TagValues.getAllNodeValue(devData, Constants.NODERECORD);
        Long totalQty = 0L;
        Date dt = null;
        for(String rec : record){
            try{
                Document dataObj = new Document();
                totalQty = Long.parseLong(TagValues.getNodeValue(rec,Constants.NODETOTAL));
                Long sTime = Long.parseLong(TagValues.getNodeValue(rec,Constants.NODESTARTTIME));
                Long eTime = 0L;
                try {
                    eTime = Long.parseLong(TagValues.getNodeValue(rec, Constants.NODEENDTIME));
                }catch (NumberFormatException nfe){
                    String data = hget(device.getAccId()+"",device.getId()+"");
                    eTime = Long.parseLong(TagValues.getNodeValue(data, Constants.NODEORIGINTSMILLI));
                }
                dt = new Date(sTime);
                dataObj.put(Constants.NODESTARTTIME,dbPattern.format(dt));
                dt = new Date(eTime);
                dataObj.put(Constants.NODEENDTIME,dbPattern.format(dt));
                dataObj.put(Constants.NODEQUANTITY,Long.parseLong(TagValues.getNodeValue(rec,Constants.NODEVALUE)));
                dataObj.put(Constants.NODEIGNTIME,Math.round((eTime-sTime)/60000));
                dateArray.add(dataObj);
            }catch (Exception e){

            }
        }
        doc.put(Constants.NODETOTAL,totalQty);
        doc.put(Constants.NODEDATA, dateArray);
        return doc;
    }

    public Map<Long, PrajReport> wardReport(Map<Long, Device> deviceMap, String startDate, String endDate){
        List wardReportList = null;
        Map<Long,PrajReport> reportMap = null;
        BasicDBObject reportData = new BasicDBObject();
        List<Long> wards = new ArrayList<>(deviceMap.keySet());
        if(wards != null && wards.size() > 0){
            BasicDBObject assetObj = new BasicDBObject();
            assetObj.put("$in", wards);
            reportData.put("deviceid", assetObj);
        }
        BasicDBObject dates = new BasicDBObject();
        if (checkNullAndEmpty(startDate) && checkNullAndEmpty(endDate)) {
            dates.put("$gte", startDate.substring(0, startDate.indexOf(" ")));
            dates.put("$lte", endDate.substring(0, endDate.indexOf(" ")));
            reportData.put(Constants.NODEDATE, dates);

            MongoConnection conn = MongoConnection.getInstance();
            try {
                conn.init();
                MongoCollection mongoCollection = conn.getDatastore().getCollection(entityName);
                int sort = 1;

                FindIterable<Document> trackIterable = null;
                trackIterable = mongoCollection.find(reportData).sort(new BasicDBObject(Constants.NODEDEVICEID, sort));
                PrajReport report = new PrajReport();
                reportMap = report.wardGenReport(trackIterable,deviceMap);
                //wardReportList = new ArrayList(reportMap.values());

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {}
        }
        return reportMap;
    }

    public PrajReport deviceReport(Long wardId, String startDate, String endDate){
        PrajReport report = new PrajReport();
        BasicDBObject reportData = new BasicDBObject();
        reportData.put("deviceid", wardId);
        BasicDBObject dates = new BasicDBObject();
        if (checkNullAndEmpty(startDate) && checkNullAndEmpty(endDate)) {
            dates.put("$gte", startDate);
            dates.put("$lte", endDate);
            reportData.put(Constants.NODEDATE, dates);

            MongoConnection conn = MongoConnection.getInstance();
            try {
                conn.init();
                MongoCollection mongoCollection = conn.getDatastore().getCollection(entityName);

                FindIterable<Document> trackIterable = null;
                trackIterable = mongoCollection.find(reportData);
                report = report.wardGenReport(trackIterable);

            }
            catch (Exception e) {
                report = null;
                e.printStackTrace();
            }
            finally {}
        }
        return report;
    }

    public Long nonFuncIotCount(Map<Long, Device> deviceMap, String type, String startDate, String endDate){
        Long nonFuncIotCount = 0L;
        Map<Long,PrajReport> reportMap = null;
        BasicDBObject reportData = new BasicDBObject();
        List<Long> wards = new ArrayList<>(deviceMap.keySet());
        if(wards != null && wards.size() > 0){
            BasicDBObject assetObj = new BasicDBObject();
            assetObj.put("$in", wards);
            reportData.put("deviceid", assetObj);
        }
        reportData.put(Constants.NODETYPE,type);
        BasicDBObject dates = new BasicDBObject();
        if (checkNullAndEmpty(startDate) && checkNullAndEmpty(endDate)) {
            dates.put("$gte", startDate.substring(0, startDate.indexOf(" ")));
            dates.put("$lte", endDate.substring(0, endDate.indexOf(" ")));
            reportData.put(Constants.NODEDATE, dates);

            MongoConnection conn = MongoConnection.getInstance();
            try {
                conn.init();
                String entity = "PRAJ_NONFUNC_REPORT";
                MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);
                nonFuncIotCount = mongoCollection.countDocuments(reportData);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {}
        }
        return nonFuncIotCount;
    }

    public Long nonFuncCount(List<Account> accList, Map<String, String> reportMap, String type, String startDate, String endDate){
        Long nonFuncCount = 0L;
        BasicDBObject reportData = new BasicDBObject();
        List<Long> panchayats = new ArrayList<>();
        for(Account acc : accList){
            panchayats.add(acc.getId());
        }
        if(panchayats != null && panchayats.size() > 0){
            BasicDBObject assetObj = new BasicDBObject();
            assetObj.put("$in", panchayats);
            reportData.put("panchayat", assetObj);
        }
        List<String> keySet = new ArrayList<>(reportMap.keySet());
        List<Long> wards = new ArrayList<>();
        for(int i=0; i<keySet.size();i++){
            wards.add(Long.parseLong(keySet.get(i)));
        }
        if(wards != null && wards.size() > 0){
            BasicDBObject assetObj = new BasicDBObject();
            assetObj.put("$nin", wards);
            reportData.put("deviceid", assetObj);
        }

        reportData.put(Constants.NODETYPE,type);
        BasicDBObject dates = new BasicDBObject();
        if (checkNullAndEmpty(startDate) && checkNullAndEmpty(endDate)) {
            dates.put("$gte", startDate.substring(0, startDate.indexOf(" ")));
            dates.put("$lte", endDate.substring(0, endDate.indexOf(" ")));
            reportData.put(Constants.NODEDATE, dates);

            //info("REdisToMongoDaoImpl reportData ... "+reportData);
            MongoConnection conn = MongoConnection.getInstance();
            try {
                conn.init();
                String entity = "PRAJ_NONFUNC_REPORT";
                MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);
                nonFuncCount = mongoCollection.countDocuments(reportData);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {}
        }
        //info("REdisToMongoDaoImpl nonFuncCount ... "+nonFuncCount);
        return nonFuncCount;
    }

    public Map<Long, PrajReport> nonFuncReport(Map<Long, Device> deviceMap, String type, String startDate, String endDate){
        List wardReportList = null;
        Map<Long,PrajReport> reportMap = null;
        BasicDBObject reportData = new BasicDBObject();
        List<Long> wards = new ArrayList<>(deviceMap.keySet());
        if(wards != null && wards.size() > 0){
            BasicDBObject assetObj = new BasicDBObject();
            assetObj.put("$in", wards);
            reportData.put("deviceid", assetObj);
        }
        if(checkNullAndEmpty(type))
            reportData.put(Constants.NODETYPE,type);

        BasicDBObject dates = new BasicDBObject();
        if (checkNullAndEmpty(startDate) && checkNullAndEmpty(endDate)) {
            dates.put("$gte", startDate.substring(0, startDate.indexOf(" ")));
            dates.put("$lte", endDate.substring(0, endDate.indexOf(" ")));
            reportData.put(Constants.NODEDATE, dates);

            MongoConnection conn = MongoConnection.getInstance();
            try {
                conn.init();
                String entity = "PRAJ_NONFUNC_REPORT";
                MongoCollection mongoCollection = conn.getDatastore().getCollection(entity);
                int sort = 1;

                FindIterable<Document> trackIterable = null;
                trackIterable = mongoCollection.find(reportData).sort(new BasicDBObject(Constants.NODEDEVICEID, sort));
                PrajReport report = new PrajReport();
                reportMap = report.wardNonFuncReport(trackIterable,deviceMap);
                //wardReportList = new ArrayList(reportMap.values());

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {}
        }
        return reportMap;
    }


    public Document createNonFunc(Device device, String devRedisData,  Calendar cal) {
        Document doc = new Document();
        try {
            Long pId = BaseInitializer.getAccountMap().get(device.getAccId()).getParentAccountId();
            Account acc = BaseInitializer.getAccountMap().get(pId);
            if(acc == null ){
                info("Parent Account is  ............ "+acc);
                return doc;
            }
            Long gpId = acc.getParentAccountId();
            if(gpId == 0){
                return doc;
            }
            doc.put(Constants.NODEDISTRICT, gpId);
            doc.put(Constants.NODEBLOCK, pId);
            doc.append(Constants.NODEPANCHAYAT, device.getAccId());
            doc.append(Constants.NODEDEVICEID, device.getId());
            doc.append(Constants.NODEVEHICLENUMBER, nullAndEmpty(device.getLicenseno(),"NA"));
            if(checkNullAndEmpty(devRedisData)) {
                Long orgTs = Long.parseLong(TagValues.getNodeValue(devRedisData,Constants.NODEORIGINTSMILLI));
                if (orgTs > cal.getTimeInMillis()){
                    doc.append("type", "NFS");
                    doc.append(Constants.NODEORIGINTSMILLI, orgTs);

                }else{
                    // Change Logic based on discussion with Prashant, Dhiraj & Ashmeet
                    // Now if device date available then it goes in Non Functional scheme
                    //doc.append("type", "NFI");
                    doc.append("type", "NFS");
                    doc.append(Constants.NODEORIGINTSMILLI, cal.getTimeInMillis());
                }
                doc.append("lat", Double.parseDouble(TagValues.getNodeValue(devRedisData,Constants.NODELATITUDE)));
                doc.append("lng", Double.parseDouble(TagValues.getNodeValue(devRedisData,Constants.NODELONGITUDE)));
                doc.append(Constants.NODECREATEDON, nullAndEmpty(null, dbPattern.format(cal.getTime())));
//                String oprTime = TagValues.getNodeValue(devRedisData,Constants.NODEIGNTIME);
//                if (checkNullAndEmpty(oprTime)){
//                    doc.append(Constants.NODEVALUE, dbPattern.format(new Date(Long.parseLong(oprTime))));
//                }else{
//                    doc.append(Constants.NODEVALUE, "NA");
//                }
            }else{
                doc.append("type", "NFI");
                doc.append("lat", 0.0D);
                doc.append("lng", 0.0D);
                doc.append(Constants.NODECREATEDON, dbPattern.format(cal.getTime()));
                doc.append(Constants.NODEORIGINTSMILLI, cal.getTimeInMillis());
            }
            doc.append(Constants.NODEDATE, dateFormat.format(cal.getTime()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

}
