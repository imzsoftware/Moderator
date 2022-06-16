package com.rep.beanobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traq.common.base.Constants;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.Driver;
import com.traq.common.data.entity.LiveTrack;
import com.traq.util.Utility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by Amit on 27/9/18.
 */
public class DashBoardSummary {
    private List<Device> deviceList;
    private List<Driver> driverList;
    private Map<Long, AccountWiseSummary> accDetail = new HashMap<Long, AccountWiseSummary>();

    public List<Device> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    public List<Driver> getDriverList() {
        return driverList;
    }

    public void setDriverList(List<Driver> driverList) {
        this.driverList = driverList;
    }

    public Map<Long, AccountWiseSummary> getAccDetail() {
        return accDetail;
    }

    public void setAccDetail(Map<Long, AccountWiseSummary> accDetail) {
        this.accDetail = accDetail;
    }

    public JSONArray toJsonArray(List<Device> deviceList){
        JSONArray jsonArray = new JSONArray();

        for(Device device : deviceList){
            try{
                JSONObject object = new JSONObject();
                object.put(Constants.NODENAME, device.getName());
                object.put(Constants.NODEID, device.getId());
                object.put(Constants.NODEIMEI, device.getIMEI());
                object.put(Constants.NODEIMSI, device.getImsi());
                object.put(Constants.NODEACCOUNTNAME, device.getAccount().getName());
                object.put(Constants.NODEACCOUNTID, device.getAccount().getAccId());
                object.put(Constants.NODEPARENTACCOUNT, device.getAccount().getParentAccountId());
                object.put(Constants.NODETYPE, device.getDeviceType().getName());
                object.put(Constants.NODEVEHICLENUMBER, device.getLicenseno());
                object.put(Constants.NODEVEHICLENAME, device.getAssetType().getName());
                object.put(Constants.NODEVEHICLETYPE, device.getAssetType().getType());
                if(device.getDriver() != null)
                    object.put(Constants.NODEDRIVERID, device.getDriver().getId());
                else
                    object.put(Constants.NODEDRIVERID, 0);

                jsonArray.put(object);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    public JSONArray toAccDetailJson(){
        JSONArray jsonArray = new JSONArray();
        Iterator<Long> itr = this.getAccDetail().keySet().iterator();

        while(itr.hasNext()){
            AccountWiseSummary summary = this.getAccDetail().get(itr.next());
            try{
                JSONObject object = new JSONObject();
                object.put(Constants.NODENAME, summary.getName());
                object.put(Constants.OFFLINE, summary.getOffLine());
                object.put(Constants.ONLINE, summary.getOnline());
                object.put(Constants.NODERECORDCOUNT, summary.getTotal());

                jsonArray.put(object);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    public JSONArray toJsonArray(List<Device> deviceList, Map<String, LiveTrack> trackMap){
        JSONArray jsonArray = new JSONArray();

        for(Device device : deviceList){
            try{
                JSONObject object = new JSONObject();
                object.put(Constants.NODENAME, device.getName());
                object.put(Constants.NODEID, device.getId());
                object.put(Constants.NODEIMEI, device.getIMEI());
                object.put(Constants.NODEIMSI, device.getImsi());
                object.put(Constants.NODEACCOUNTNAME, device.getAccount().getName());
                object.put(Constants.NODEACCOUNTID, device.getAccount().getAccId());
                object.put(Constants.NODEPARENTACCOUNT, device.getAccount().getParentAccountId());
                object.put(Constants.NODETYPE, device.getDeviceType().getName());
                object.put(Constants.NODEVEHICLENUMBER, device.getLicenseno());
                object.put(Constants.NODEVEHICLENAME, device.getAssetType().getName());
                object.put(Constants.NODEVEHICLETYPE, device.getAssetType().getType());
                try{
                    String devStatus = Utility.deviceStatus((String) trackMap.get(device.getIMEI()).getOrgTs());
                    object.put(Constants.NODEDEVICESTATUS, devStatus);
                    if(accDetail.containsKey(device.getAccount().getAccId())){
                        AccountWiseSummary summary = accDetail.get(device.getAccount().getAccId());
                        if(devStatus.equals(Constants.ONLINE)){
                            summary.setOnline(summary.getOnline() + 1);
                        }else{
                            summary.setOffLine(summary.getOffLine()+1);
                        }
                        summary.setTotal(summary.getTotal() +1);
                        accDetail.put(device.getAccount().getAccId(), summary);
                    }else{
                        AccountWiseSummary summary = new AccountWiseSummary();
                        if(devStatus.equals(Constants.ONLINE)){
                            summary.setOnline(1L);
                        }else{
                            summary.setOffLine(1L);
                        }
                        summary.setName(device.getAccount().getName());
                        summary.setTotal(1L);
                        accDetail.put(device.getAccount().getAccId(), summary);
                    }
                }catch (Exception e){
                    object.put(Constants.NODEDEVICESTATUS, Constants.OFFLINE);
                    if(accDetail.containsKey(device.getAccount().getAccId())){
                        AccountWiseSummary summary = accDetail.get(device.getAccount().getAccId());
                        summary.setOffLine(summary.getOffLine()+1);
                        summary.setTotal(summary.getTotal() +1);
                        accDetail.put(device.getAccount().getAccId(), summary);
                    }else{
                        AccountWiseSummary summary = new AccountWiseSummary();
                        summary.setOffLine(1L);
                        summary.setTotal(1L);
                        summary.setName(device.getAccount().getName());
                        accDetail.put(device.getAccount().getAccId(), summary);
                    }
                }


                if(device.getDriver() != null){
                    object.put(Constants.NODEDRIVERID, device.getDriver().getId());
                    object.put(Constants.NODEDRIVERNAME, device.getDriver().getName());
                    object.put(Constants.NODEDRIVERPHONE, device.getDriver().getPhoneno());
                }else{
                    object.put(Constants.NODEDRIVERID, 0);
                    object.put(Constants.NODEDRIVERNAME, "NA");
                    object.put(Constants.NODEDRIVERPHONE, "NA");
                }

                jsonArray.put(object);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        setAccDetail(accDetail);
        return jsonArray;
    }

    public String toJson() throws JsonProcessingException{
        ObjectMapper mapperObj = new ObjectMapper();

        String jsonStr = mapperObj.writeValueAsString(this);

        return jsonStr;
    }

}
