package com.imz.praj.data.obj;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.data.entity.AlertMongoLog;
import org.bson.Document;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class PrajReportData extends BaseInitializer{
    private String date;
    private String startDate;
    private String endDate;
    private Long quantity;
    private String value;
    private Long orgMillis;
    private String cts;
    private Long totalQty=0L;
    private Long accId;
    private Long blockId;
    private Long distId;
    private Long devId;
    private Long funcCount;
    private Long nonFuncCount;
    private Long nonFuncSchCount;
    private String vehNum;
    private JSONArray data;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getOrgMillis() {
        return orgMillis;
    }

    public void setOrgMillis(Long orgMillis) {
        this.orgMillis = orgMillis;
    }

    public Long getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(Long totalQty) {
        this.totalQty = totalQty;
    }

    public String getCts() {
        return cts;
    }

    public void setCts(String cts) {
        this.cts = cts;
    }

    public Long getAccId() {
        return accId;
    }

    public void setAccId(Long accId) {
        this.accId = accId;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public Long getDistId() {
        return distId;
    }

    public void setDistId(Long distId) {
        this.distId = distId;
    }

    public Long getDevId() {
        return devId;
    }

    public void setDevId(Long devId) {
        this.devId = devId;
    }

    public Long getFuncCount() {
        return funcCount;
    }

    public void setFuncCount(Long funcCount) {
        this.funcCount = funcCount;
    }

    public Long getNonFuncCount() {
        return nonFuncCount;
    }

    public void setNonFuncCount(Long nonFuncCount) {
        this.nonFuncCount = nonFuncCount;
    }

    public Long getNonFuncSchCount() {
        return nonFuncSchCount;
    }

    public void setNonFuncSchCount(Long nonFuncSchCount) {
        this.nonFuncSchCount = nonFuncSchCount;
    }

    public String getVehNum() {
        return vehNum;
    }

    public void setVehNum(String vehNum) {
        this.vehNum = vehNum;
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
    }

    public static List<PrajReportData> createList(FindIterable<Document> resultList) {
        List<PrajReportData> alertLogList = new ArrayList<PrajReportData>();
        try {
            for (MongoCursor<Document> mongoCursor = resultList.iterator(); mongoCursor.hasNext(); ) {
                Document doc = mongoCursor.next();
                PrajReportData data = new PrajReportData();
                data.setAccId(doc.getLong("accid"));
                data.setDevId(doc.getLong("deviceid"));
                data.setVehNum(doc.getString("vehnum"));
                data.setValue(doc.getString("value"));
                data.setCts(doc.getString("cts"));
                try {
                    data.setOrgMillis(doc.getLong("orgmillis"));
                } catch (Exception exception) {

                }
                alertLogList.add(data);
            }
        }catch (Exception ee){
            ee.printStackTrace();
        }
        return alertLogList;
    }

    public static List<PrajReportData> createGenReport(FindIterable<Document> resultList) {
        List<PrajReportData> alertLogList = new ArrayList<PrajReportData>();
        try {

            for (MongoCursor<Document> mongoCursor = resultList.iterator(); mongoCursor.hasNext(); ) {
                Document doc = mongoCursor.next();
                PrajReportData data = new PrajReportData();
                data.setAccId(doc.getLong(Constants.NODEPANCHAYAT));
                data.setBlockId(doc.getLong(Constants.NODEBLOCK));
                data.setDistId(doc.getLong(Constants.NODEDISTRICT));
                data.setDevId(doc.getLong(Constants.NODEDEVICEID));
                data.setVehNum(doc.getString(Constants.NODEVEHICLENUMBER));
                data.setDate(doc.getString(Constants.NODEDATE));
                data.setTotalQty(doc.getLong(Constants.NODETOTAL));
                data.setData((JSONArray) doc.get(Constants.NODEDATA));

                alertLogList.add(data);
            }
        }catch (Exception ee){
            ee.printStackTrace();
        }
        return alertLogList;
    }

    public static PrajReportData wardGenReport(FindIterable<Document> resultList) {
        PrajReportData data = new PrajReportData();
        try {
            Long total = 0L;
            MongoCursor<Document> mongoCursor = resultList.iterator();
            while (mongoCursor.hasNext()) {
                Document doc = mongoCursor.next();
                data.setAccId(doc.getLong(Constants.NODEPANCHAYAT));
                data.setBlockId(doc.getLong(Constants.NODEBLOCK));
                data.setDistId(doc.getLong(Constants.NODEDISTRICT));
                data.setDevId(doc.getLong(Constants.NODEDEVICEID));
                //data.setVehNum(doc.getString(Constants.NODEVEHICLENUMBER));
                //data.setDate(doc.getString(Constants.NODEDATE));
                total = total + doc.getLong(Constants.NODETOTAL);
                data.setTotalQty(total);
            }

        }catch (Exception ee){
            ee.printStackTrace();
        }
        return data;
    }

    @Override
    public String toString() {
        return "PrajReportData{" +
                "date='" + date + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", quantity=" + quantity +
                ", value='" + value + '\'' +
                ", orgMillis=" + orgMillis +
                ", cts='" + cts + '\'' +
                ", accId=" + accId +
                ", devId=" + devId +
                ", vehNum='" + vehNum + '\'' +
                ", totalQty='" + totalQty + '\'' +
                '}';
    }
}
