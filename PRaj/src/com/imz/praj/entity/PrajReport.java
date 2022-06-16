package com.imz.praj.entity;

import com.imz.praj.data.obj.GenReportData;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.AlertType;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.Driver;
import com.traq.common.data.model.dao.AlertTypeDao;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class PrajReport extends RequestMessage {
    private String district;
    private String block;
    private String panchayat;
    private String ward;
    private Driver anurakshak;
    private Long quantity = 0L;
    private Long totalQuantity = 0L;
    private Long totalTime = 0L;
    private String date;
    private String lastOprTime;
    private Integer average = 1;
    private List<ViewDetailRep> detailReports;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Long timeDiff = 19800000L;     // GMT and IST Difference in milliseconds

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getPanchayat() {
        return panchayat;
    }

    public void setPanchayat(String panchayat) {
        this.panchayat = panchayat;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public Driver getAnurakshak() {
        return anurakshak;
    }

    public void setAnurakshak(Driver anurakshak) {
        this.anurakshak = anurakshak;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLastOprTime() {
        return lastOprTime;
    }

    public void setLastOprTime(String lastOprTime) {
        this.lastOprTime = lastOprTime;
    }

    public Integer getAverage() {
        return average;
    }

    public void setAverage(Integer average) {
        this.average = average;
    }

    public List<ViewDetailRep> getDetailReports() {
        return detailReports;
    }

    public void setDetailReports(List<ViewDetailRep> detailReports) {
        this.detailReports = detailReports;
    }

    public static JSONObject toJson(PrajReport reort) {
        JSONObject object = new JSONObject();
        try {
            //object.put("id", alertLog.getDocId());
            object.put(Constants.NODEQUANTITY, reort.getTotalQuantity());
            object.put(Constants.NODETOTAL, reort.getTotalQuantity());
            object.put(Constants.NODEDISTRICT, reort.getDistrict().replaceAll(" PRAJ",""));
            object.put(Constants.NODEBLOCK, reort.getBlock());
            object.put(Constants.NODEACCOUNTNAME, reort.getPanchayat());
            object.put(Constants.NODEVEHICLENUMBER, reort.getWard());
            object.put(Constants.NODEDRIVER, Driver.toJson(reort.getAnurakshak()));
            object.put(Constants.NODEIGNTIME, reort.getLastOprTime());
            object.put("avg", reort.getTotalQuantity()/reort.getAverage());
            object.put(Constants.NODEDATA,ViewDetailRep.toJsonArr(reort.getDetailReports()));

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

    public static JSONObject toNonFuncJson(PrajReport reort) {
        JSONObject object = new JSONObject();
        try {
            //object.put("id", alertLog.getDocId());
            object.put(Constants.NODEQUANTITY, reort.getTotalQuantity());
            object.put(Constants.NODEDISTRICT, reort.getDistrict().replaceAll(" PRAJ",""));
            object.put(Constants.NODEBLOCK, reort.getBlock());
            object.put(Constants.NODEACCOUNTID, reort.getAccId());
            object.put(Constants.NODEACCOUNTNAME, reort.getPanchayat());
            object.put(Constants.NODEVEHICLENUMBER, reort.getWard());
            object.put(Constants.NODEDRIVER, Driver.toJson(reort.getAnurakshak()));
            object.put(Constants.NODEIGNTIME, reort.getLastOprTime());
            object.put(Constants.NODEDATA,ViewDetailRep.toJsonArr(reort.getDetailReports()));

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


    public static Comparator<PrajReport> timeComparator = new Comparator<PrajReport>()
    {
        public int compare(PrajReport t1, PrajReport t2) {
            String time1 = t1.getCreatedOn();
            String time2 = t2.getCreatedOn();


            return time1.compareTo(time2);
        }
    };

    public Map<Long, PrajReport> wardGenReport(FindIterable<Document> resultList, Map<Long, Device> deviceMap) {
        Map<Long, PrajReport> reportDataMap =  new HashMap<>();
        try {
            MongoCursor<Document> mongoCursor = resultList.iterator();
            while (mongoCursor.hasNext()) {
                PrajReport prajReport = new PrajReport();
                Document doc = mongoCursor.next();
                if(reportDataMap.containsKey(doc.getLong(Constants.NODEDEVICEID))){
                    prajReport = reportDataMap.get(doc.getLong(Constants.NODEDEVICEID));
                    Long totalQty = prajReport.getTotalQuantity();
                    List<Document> dataList = (List<Document>)doc.get(Constants.NODEDATA);
                    prajReport.setAverage(prajReport.getAverage()+1);
                    prajReport = internalData(prajReport, dataList,totalQty);
                }else {
                    prajReport.setPanchayat(BaseInitializer.getAccountMap().get(doc.getLong(Constants.NODEPANCHAYAT)).getName());
                    prajReport.setBlock(BaseInitializer.getAccountMap().get(doc.getLong(Constants.NODEBLOCK)).getName());
                    prajReport.setDistrict(BaseInitializer.getAccountMap().get(doc.getLong(Constants.NODEDISTRICT)).getName());
                    Long devId = doc.getLong(Constants.NODEDEVICEID);
                    prajReport.setWard(deviceMap.get(devId).getLicenseno());
                    prajReport.setAnurakshak(deviceMap.get(devId).getDriver());
                    prajReport.setAverage(1);
                    List<Document> dataList = (List<Document>)doc.get(Constants.NODEDATA);
                    Long totalQty = 0L;
                    prajReport = internalData(prajReport, dataList, totalQty);

                }
                reportDataMap.put(doc.getLong(Constants.NODEDEVICEID),prajReport);
            }
        }catch (Exception ee){
            ee.printStackTrace();
        }
        return reportDataMap;
    }

    public PrajReport wardGenReport(FindIterable<Document> resultList) {
        PrajReport prajReport = new PrajReport();
        try {
            MongoCursor<Document> mongoCursor = resultList.iterator();
            while (mongoCursor.hasNext()) {
                Document doc = mongoCursor.next();
                prajReport.setPanchayat(BaseInitializer.getAccountMap().get(doc.getLong(Constants.NODEPANCHAYAT)).getName());
                prajReport.setBlock(BaseInitializer.getAccountMap().get(doc.getLong(Constants.NODEBLOCK)).getName());
                prajReport.setDistrict(BaseInitializer.getAccountMap().get(doc.getLong(Constants.NODEDISTRICT)).getName());
                List<Document> dataList = (List<Document>)doc.get(Constants.NODEDATA);
                Long totalQty = 0L;
                prajReport = internalData(prajReport, dataList, totalQty);

            }
        }catch (Exception ee){
            ee.printStackTrace();
        }
        return prajReport;
    }

    public Map<Long, PrajReport> wardNonFuncReport(FindIterable<Document> resultList, Map<Long, Device> deviceMap) {
        Map<Long, PrajReport> reportDataMap =  new HashMap<>();
        try {
            MongoCursor<Document> mongoCursor = resultList.iterator();
            while (mongoCursor.hasNext()) {
                PrajReport prajReport = new PrajReport();
                Document doc = mongoCursor.next();
                if(reportDataMap.containsKey(doc.getLong(Constants.NODEDEVICEID))){
                    prajReport = reportDataMap.get(doc.getLong(Constants.NODEDEVICEID));
                    List<ViewDetailRep> detailRepList = prajReport.getDetailReports();
                    ViewDetailRep detailRep = new ViewDetailRep();
                    detailRep.setDate(doc.getString(Constants.NODEDATE));
                    detailRepList.add(detailRep);
                    prajReport.setDetailReports(detailRepList);
                }else {
                    prajReport.setAccId(doc.getLong(Constants.NODEPANCHAYAT));
                    prajReport.setPanchayat(BaseInitializer.getAccountMap().get(doc.getLong(Constants.NODEPANCHAYAT)).getName());
                    prajReport.setBlock(BaseInitializer.getAccountMap().get(doc.getLong(Constants.NODEBLOCK)).getName());
                    prajReport.setDistrict(BaseInitializer.getAccountMap().get(doc.getLong(Constants.NODEDISTRICT)).getName());
                    Long devId = doc.getLong(Constants.NODEDEVICEID);
                    prajReport.setWard(deviceMap.get(devId).getLicenseno());
                    prajReport.setAnurakshak(deviceMap.get(devId).getDriver());
                    List<ViewDetailRep> detailRepList = new ArrayList<>();
                    ViewDetailRep detailRep = new ViewDetailRep();
                    detailRep.setDate(doc.getString(Constants.NODEDATE));
                    detailRepList.add(detailRep);
                    prajReport.setDetailReports(detailRepList);
                }
                reportDataMap.put(doc.getLong(Constants.NODEDEVICEID),prajReport);
            }
        }catch (Exception ee){
            ee.printStackTrace();
        }
        return reportDataMap;
    }


    private PrajReport internalData(PrajReport prajRep, List<Document> dataList, Long totalQty){
        List<ViewDetailRep> detailRepList = prajRep.getDetailReports();
        if(detailRepList == null){
            detailRepList = new ArrayList<>();
        }
        Long totalTime = 0L;
        for(Document rec : dataList){
            ViewDetailRep detailRep = new ViewDetailRep();
            totalQty = totalQty +rec.getLong(Constants.NODEQUANTITY);
            detailRep.setOnTime(rec.getString(Constants.NODESTARTTIME));
            detailRep.setOffTime(rec.getString(Constants.NODEENDTIME));
            prajRep.setLastOprTime(detailRep.getOffTime());
            detailRep.setQuantity(rec.getLong(Constants.NODEQUANTITY));
            detailRep.setDate(rec.getString(Constants.NODESTARTTIME).substring(0,rec.getString(Constants.NODESTARTTIME).indexOf(" ")));
            try {
                detailRep.setRunTime(rec.getInteger(Constants.NODEIGNTIME).longValue());
            }catch (java.lang.ClassCastException ce){
                detailRep.setRunTime(rec.getLong(Constants.NODEIGNTIME));
            }
            totalTime += detailRep.getRunTime();
            detailRepList.add(detailRep);
        }
        prajRep.setDetailReports(detailRepList);
        prajRep.setTotalQuantity(totalQty);
        prajRep.setTotalTime(totalTime);
        return prajRep;
    }


    @Override
    public String toString() {
        return "PrajReport{" +
                "district='" + district + '\'' +
                ", block='" + block + '\'' +
                ", panchayat='" + panchayat + '\'' +
                ", ward='" + ward + '\'' +
                ", anurakshak=" + anurakshak +
                ", totalQuantity=" + totalQuantity +
                ", date='" + date + '\'' +
                ", lastOprTime='" + lastOprTime + '\'' +
                ", average=" + average +
                ", detailReports=" + detailReports +
                '}';
    }
}
