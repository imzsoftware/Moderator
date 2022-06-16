package com.traq.manipulator.bots;

import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Alert;
import com.traq.common.data.entity.AlertLog;
import com.traq.common.data.model.dao.AlertDao;
import com.traq.common.data.model.dao.AlertLogDao;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.service.AlertFormat;
import org.bson.Document;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Amit on 5/6/19.
 */
public class AlertInsertion extends BaseInitializer{


    public void insertAlerts(String client, String records, String liveData, String deviceDetail) throws IOException, InterruptedException{
        AlertLogDao alertLogDao = (AlertLogDao) ApplicationBeanContext.getInstance().getBean("alertLogDao");
        DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
        try{
            List<String> alerts = TagValues.getAllNodeValue(records, Constants.NODERECORD);
            AlertFormat format = new AlertFormat();
            for(String alert : alerts) {
                Alert alrt = new Alert();
                alrt.setCode(TagValues.getNodeValue(alert, Constants.NODETYPE));
                String accId = TagValues.getNodeValue(liveData, Constants.NODEACCOUNTID);
                String devId = TagValues.getNodeValue(liveData, Constants.NODEDEVICEID);
/*                if(!hget(alrt.getCode()+"_" +accId, devId).isEmpty()){
                    continue;
                }else{
                    hset(alrt.getCode()+"_" +accId, devId, TagValues.getNodeValue(alert, Constants.NODEVALUE));
                }*/
                AlertLog alertLog = new AlertLog();
                alertLog.setClient(client);
                alertLog.setAccId(Long.parseLong(accId));
                alertLog.setAssetId(Long.parseLong(devId));
                alertLog.setLatitude(Double.parseDouble(TagValues.getNodeValue(liveData, Constants.NODELATITUDE)));
                alertLog.setLongitude(Double.parseDouble(TagValues.getNodeValue(liveData, Constants.NODELONGITUDE)));
                alertLog.setDestination("DASHBOARD");
                alertLog.setStatus_code("A");
                alertLog.setType("G");
                alertLog.setMisc("NA");
                alertLog.setAlert_id("0");
                alertLog.setDevice(deviceDao.getDeviceById(alertLog.getAssetId()));
                alertLog.setAlert(alrt);
                alertLog.setMessage(format.alertFormatString(liveData, alrt.getCode(), "", gcmFormatFile));
                info("Message..........."+alertLog.getMessage());
                TagValues.getNodeValue(deviceDetail, Constants.NODECREATEDON);

                Long.parseLong(TagValues.getNodeValue(deviceDetail, Constants.NODEDEVICEID));
                TagValues.getNodeValue(deviceDetail, Constants.NODEDEVICETYPEID);
                alertLogDao.saveAlertLog(alertLog);
            }
        }catch (Exception e)  {
            e.printStackTrace();
            error("AlertInsertion.........."+e.getMessage());
        }finally {

        }
    }

}
