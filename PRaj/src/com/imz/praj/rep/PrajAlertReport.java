package com.imz.praj.rep;

import com.imz.praj.data.impl.PrajAlertLogDaoImpl;
import com.imz.praj.entity.PrajAlertLog;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.*;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.AlertTypeDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.exceptions.EntityException;
import com.traq.common.exceptions.NoRecordFoundException;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.ReportHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import org.json.JSONException;
import org.json.JSONObject;
import utility.Custom;

import java.text.SimpleDateFormat;
import java.util.*;

public class PrajAlertReport extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    List<AlertLog> dataList = null; private RequestMessage requestMessage; private String request;
    List<PrajAlertLog> alertlist = new ArrayList<>();

    SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    List<Account> accountList = new ArrayList<>();
    Map<Long, Custom> devmap = new HashMap<>();
    Map<Long, Custom> accmap = new HashMap<>();
    Map<String, Custom> altmap = new HashMap<>();

    RequestBean rb;
    User user = null;

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public PrajAlertReport(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public PrajAlertReport() {}

    public String executeXML(RequestBean _rb) {
        return this.request;
    }


    public String executeJSON(RequestBean _rb) {
        String response = "";
        try {
            this.rb = _rb;
            this.request = this.rb.getRequest();
            this.responseMessage = new ResponseMessage();
            ReportHandler dh = new ReportHandler();
            JSONObject object = new JSONObject(this.request);

            AccountDao accountDao = (AccountDao)ApplicationBeanContext.getInstance().getBean("accountDao");

            this.requestMessage = dh.getRequest(object.getJSONObject("request"), TagValues.getNodeValue(object, "requesttype"));
            this.requestMessage.setVendorcode(TagValues.getNodeValue(object, "vendorcode"));

            UserDao userDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");
            this.user = userDao.login(this.requestMessage.getUsername());
            if (this.user != null) {
                boolean isValid = validateUser(this.user, this.requestMessage.getPin());
                if (isValid) {
                    try {
                        if(requestMessage.getMessagetype().equals("VIEWALERTLOG")) {
                            Date endDate = new Date();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(endDate);
                            int alertActiveTime = Integer.parseInt(getAppConfig().getAlertActiveTime());
                            calendar.add(Calendar.HOUR, -alertActiveTime);
                            Date startDate = calendar.getTime();
                            requestMessage.setStartDate(this.dbPattern.format(startDate));
                            requestMessage.setEndDate(this.dbPattern.format(endDate));
                        }else{
                            Date startDate =newPattern.parse(requestMessage.getStartDate());
                            Date endDate = newPattern.parse(requestMessage.getEndDate());

                            requestMessage.setStartDate(dbPattern.format(startDate));
                            requestMessage.setEndDate(dbPattern.format(endDate));
                        }
                    }
                    catch (Exception exception) {}

                    requestMessage.setType("");
                    List<String> codes = new ArrayList<>();
                    codes.add("EWA");
                    codes.add("NFI");
                    codes.add("NFS");
                    requestMessage.setCodes(codes);

                    if ("VIEWALERTCOUNT".equals(this.requestMessage.getMessagetype())) {
                        Account account = null;
                        if (checkNullAndEmpty(this.requestMessage.getAccId())) {
                            account = accountDao.getAccountById(this.requestMessage.getAccId());
                        } else {
                            account = accountDao.getAccountById(this.user.getAccId());
                        }

                        if (account != null) {
                            try {
                                if (account.getType().equalsIgnoreCase("HO")) {
                                    this.accountList = accountDao.dropdownFindAll(Boolean.valueOf(true));
                                } else {
                                    this.accountList = accountDao.AllChildren(account.getId());
                                }
                            } catch (Exception exception) {}

                            this.accountList.add(account);
                        }

                        PrajAlertLogDaoImpl prajAlertLogDao = new PrajAlertLogDaoImpl();
                        this.alertlist = prajAlertLogDao.findAlerts(this.requestMessage, this.accountList, false);

                        if (this.alertlist == null || this.alertlist.size() == 0) {
                            throw new NoRecordFoundException();
                        }

                        for (PrajAlertLog alert : this.alertlist) {
                            deviceCheck(alert);
                            accountCheck(alert);
                            alertCheck(alert);
                        }

                        this.responseMessage.setResultcode(Integer.valueOf(0));
                        this.responseMessage.setResultDescription("Success");
                    }
                    else if ("VIEWALERTLOG".equals(this.requestMessage.getMessagetype())) {

                        PrajAlertLogDaoImpl prajAlertLogDao = new PrajAlertLogDaoImpl();
                        if (!this.requestMessage.getTranstype().equalsIgnoreCase("DASHBOARD")) {
                            Account account = null;

                            if (checkNullAndEmpty(this.requestMessage.getAccId())) {
                                account = accountDao.getAccountById(this.requestMessage.getAccId());
                            } else {
                                account = accountDao.getAccountById(this.user.getAccId());
                            }
                            if (account != null) {
                                try {
                                    if (account.getType().equalsIgnoreCase("HO")) {
                                        this.accountList = accountDao.dropdownFindAll(Boolean.valueOf(true));
                                    } else {
                                        requestMessage.setVendorcode(account.getClient());
                                        this.accountList = accountDao.AllChildren(account.getId());
                                    }
                                } catch (Exception exception) {}

                                this.accountList.add(account);
                            }
                        }
                        this.alertlist = prajAlertLogDao.findAlerts(requestMessage, this.accountList, false);

                        if (this.alertlist == null || this.alertlist.size() == 0) {
                            throw new NoRecordFoundException();
                        }

                        this.responseMessage.setResultcode(Integer.valueOf(0));
                        this.responseMessage.setResultDescription("Success");

                    } else {
                        PrajAlertLogDaoImpl prajAlertLogDao = new PrajAlertLogDaoImpl();
                        Account account = accountDao.getAccountById(this.requestMessage.getAccId());
                        requestMessage.setVendorcode(account.getClient());
                        this.alertlist = prajAlertLogDao.findAlerts(this.requestMessage, null, false);
                        if (this.alertlist == null || this.alertlist.size() == 0) {
                            throw new NoRecordFoundException();
                        }
                        this.responseMessage.setResultcode(0);
                        this.responseMessage.setResultDescription("Success");
                    }

                }
                else if (!this.user.getStatus().getCode().equals("A")) {
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
        } catch (NoRecordFoundException ee) {
            this.responseMessage.setResultcode(Integer.valueOf(36));
            this.responseMessage.setResultDescription("Record Not Found");
        } catch (EntityException ee) {
            this.responseMessage.setResultcode(ee.getResultCode(Integer.valueOf(0)));
            this.responseMessage.setResultDescription(ee.getDescription());
            this.responseMessage.setTimestamp((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(new Date()));

        }
        catch (Exception e) {
            this.responseMessage.setResultcode(Integer.valueOf(206));
            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            e.printStackTrace();
        } finally {
            if (this.responseMessage.getResultcode() == 0) {
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
            if ("VIEWALERTCOUNT".equals(this.requestMessage.getMessagetype())) {
                jsonObject.put("DEVICE".toLowerCase(), Custom.toJsonArr(new ArrayList(this.devmap.values())));
                jsonObject.put("ACCOUNT".toLowerCase(), Custom.toJsonArr(new ArrayList(this.accmap.values())));
                jsonObject.put("ALERT".toLowerCase(), Custom.toJsonArr(new ArrayList(this.altmap.values())));
            }else if ("VIEWALERTLOG".equals(this.requestMessage.getMessagetype())) {
                jsonObject.put(Constants.NODERECORDS, PrajAlertLog.toJsonArr(this.alertlist));
            } else {
                Account acc = BaseInitializer.getAccountMap().get(requestMessage.getAccId());
                jsonObject.put(Constants.NODEACCOUNT, Account.toJson(acc));
                jsonObject.put(Constants.NODEREPORT, PrajAlertLog.toJsonArr(this.alertlist));
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
        } catch (JSONException jSONException) {}


        return mainObj.toString();
    }

    public void deviceCheck(PrajAlertLog alert) {
        Long devid = alert.getAssetId();
        int msgcount = alert.getCount();
        String vehicleNumber = alert.getVehicleNumber();
        String type = alert.getType();
        Custom custom = null;

        if (this.devmap.containsKey(devid)) {
            custom = this.devmap.get(devid);
        } else {
            custom = new Custom();
            custom.setRef(devid + "");
            custom.setName(vehicleNumber);
        }
        custom = incrementer(custom, type, msgcount);
        this.devmap.put(devid, custom);
    }

    public Custom incrementer(Custom custom, String type, int msgcount) {
        switch (type) {
            case "S":
                custom.setSmscount(custom.getSmscount() + msgcount);
                break;
            case "E":
                custom.setEmailcount(custom.getEmailcount() + 1);
                break;
            case "A":
                custom.setSmscount(custom.getSmscount() + msgcount);
                custom.setEmailcount(custom.getEmailcount() + 1);
                break;
        }
        return custom;
    }

    public void accountCheck(PrajAlertLog alert) {
        Long accid = alert.getAccId();
        int msgcount = alert.getCount();
        String type = alert.getType();
        Custom custom = null;

        if (this.accmap.containsKey(accid)) {
            custom = this.accmap.get(accid);
        } else {
            custom = new Custom();
            custom.setRef(accid + "");
            try {
                custom.setName(((Account)BaseInitializer.getAccountMap().get(accid)).getName());
            } catch (Exception e) {
                AccountDao accountDao = (AccountDao)ApplicationBeanContext.getInstance().getBean("accountDao");
                Account account = accountDao.getAccountById(accid);
                if (checkNullAndEmpty(account)) {
                    BaseInitializer.getAccountMap().put(accid, account);
                    custom.setName(account.getName());
                } else {
                    custom.setName("NA");
                }
            }
        }

        custom = incrementer(custom, type, msgcount);
        this.accmap.put(accid, custom);
    }

    public void alertCheck(PrajAlertLog alert) {
        String code = alert.getCode();
        int msgcount = alert.getCount();
        String type = alert.getType();
        Custom custom = null;

        if (this.altmap.containsKey(code)) {
            custom = this.altmap.get(code);
        } else {
            custom = new Custom();
            custom.setRef(code);
            try {
                custom.setName(((AlertType)BaseInitializer.getAlertTypeMap().get(code)).getName());
            } catch (Exception e) {
                AlertTypeDao alertTypeDao = (AlertTypeDao)ApplicationBeanContext.getInstance().getBean("alertTypeDao");
                AlertType alertType = alertTypeDao.getAlertTypeByCode(code);
                if (alertType != null) {
                    custom.setName(alertType.getName());
                } else {
                    custom.setName(code);
                }
            }
        }

        custom = incrementer(custom, type, msgcount);
        this.altmap.put(code, custom);
    }
}

