package com.imz.praj.processor;

import com.imz.praj.data.AccountTargetDao;
import com.imz.praj.data.impl.PrajReportDaoImpl;
import com.imz.praj.data.impl.RedisToMongoDaoImpl;
import com.imz.praj.data.obj.AccountTarget;
import com.imz.praj.entity.PrajReport;
import com.imz.praj.processor.utility.ReportUtils;
import com.traq.beanobject.QuickViewObj;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.*;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.FuelCalibraterDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.exceptions.*;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.RequestHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.common.xmlutils.XMLProcessor;
import com.traq.util.RequestBean;
import com.traq.util.Utility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class DBSummaryProcessor extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    private List<LiveTrack> liveTrackListprimary = null;
    private RequestMessage requestMessage;
    private String request;

    private Map<Long, Device> deviceMap = new HashMap<>();
    private Map<String, String> dataList = new HashMap<>();
    JSONArray accArray = null;
    JSONArray devArray = null;

    SimpleDateFormat outputPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    RequestBean rb;

    Long totalDevice = 0L;
    Long nonFuncIOT = 0L;
    Long nonFuncSCH = 0L;
    long curTime = (new Date()).getTime();
    long timeDiff = 19800*1000;  // Value in milliseconds 5Hr 30min from GMT
    long offLineTime = 1000*60*60*24;
    List<AccountTarget> targetList = null;
    Map<String, String> reportMap = new HashMap<>();
    Map<String, String> actualReportMap = new HashMap<>();

    boolean isDetailed = false;

    private String stdData = "<traq><lat>0.0</lat><lng>0.0</lng><disha>0</disha><speed>0</speed><ign>N</ign><distance></distance><fuel></fuel><temp></temp><ac></ac><door></door><reserved></reserved><gps></gps><satellites>0</satellites><orgts></orgts><cts></cts><address></address><deviceid></deviceid><accid></accid><imb>N</imb><powsts></powsts><battery>0</battery><devip></devip><devport></devport><servip></servip><servport></servport><devicetypeid></devicetypeid></traq>";

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {

        this.responseMessage = responseMessage;
    }

    public DBSummaryProcessor(ResponseMessage responseMessage) {

        this.responseMessage = responseMessage;
    }

    public DBSummaryProcessor() {
    }

    public String executeXML(RequestBean _rb) {
        return request;
    }

    public String executeJSON(RequestBean _rb) {
        rb = _rb;
        this.request = this.rb.getRequest();

        RequestHandler rh = new RequestHandler();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        String response = "";

        Map<Long, Account> accountMap = null;
        try {
            JSONObject object = new JSONObject(request);
            JSONObject reqObj = object.getJSONObject("request");
            requestMessage = rh.getRequest(reqObj, TagValues.getNodeValue(object, "requesttype"));

            if (requestMessage.getIpaddress() == null) {
                throw new InvalidRequest("Ipaddess should not be null");
            }

            responseMessage = new ResponseMessage();
            responseMessage.setMessagetype(requestMessage.getMessagetype());
            responseMessage.setResponsects(sdf.format(new Date()));
            responseMessage.setTransid(rb.getTransid());
            responseMessage.setVendorcode(rb.getVendorCode());

            UserDao userDao = (UserDao) ApplicationBeanContext.getInstance().getBean("userDao");
            User user = userDao.login(requestMessage.getUsername());
            boolean isValid = validateUser(user, requestMessage.getPin());
            if (isValid) {
                AccountDao accountDao = (AccountDao) ApplicationBeanContext.getInstance().getBean("accountDao");
                DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
                AccountTargetDao accountTargetDao = (AccountTargetDao)ApplicationBeanContext.getInstance().getBean("accountTargetDao");

                boolean isHo = false;
                List<Account> accountList = null;
                List<Long> accIds = new ArrayList<>();
                accArray =  TagValues.getArrayValue(reqObj, Constants.NODEACCOUNT);
                devArray =  TagValues.getArrayValue(reqObj, Constants.NODEASSET);

                SimpleDateFormat redisKeyFormat = new SimpleDateFormat("yyyy-MM-dd");
                String reportDate = redisKeyFormat.format(new Date());
                String repKey ="PRAJ_REP_"+reportDate;
                reportMap = hgetAll(repKey);

                if (devArray != null && devArray.length()>0) {
                    for (int i=0;i<devArray.length();i++){
                        Device dev = deviceDao.getDeviceById(devArray.getLong(i));
                        deviceMap.put(devArray.getLong(i), dev);
                        String data = hget(dev.getAccId()+"", dev.getId()+"");
                        dataList.put(dev.getId()+"", data);
                        if(reportMap.containsKey(devArray.getLong(i)+"")) {
                            actualReportMap.put(devArray.getLong(i) + "", reportMap.get(devArray.getLong(i)+""));
                        }
                    }
                    targetList = accountTargetDao.findByPanchayat(accIds);
                }else if (accArray != null) {
                    accountList = new ArrayList<>();
                    //info("DBSummaryProcessor .. accArray.length() .... "+accArray.length());
                    for (int i=0;i<accArray.length();i++){
                        Account account = BaseInitializer.getAccountMap().get(accArray.getLong(i));
                        if("L5".equalsIgnoreCase(account.getType())){
                            accountList.add(BaseInitializer.getAccountMap().get(accArray.getLong(i)));
                            List<Device> devices = deviceDao.getDeviceByAccount(accArray.getLong(i));
                            for(Device dev : devices){
                                deviceMap.put(dev.getId(), dev);
                                if(reportMap.containsKey(dev.getId()+"")) {
                                    actualReportMap.put(dev.getId() + "", reportMap.get(dev.getId()+""));
                                }
                            }
                            accIds.add(accArray.getLong(i));
                        }else{
                            //accIds.add(accArray.getLong(i));
                            List<Account> accounts = accountDao.findAllChildren(accArray.getLong(i));
                            //info("DBSummaryProcessor .. accounts .... "+accounts);
                            for(Account acc : accounts){
                                if("L5".equalsIgnoreCase(acc.getType())){
                                    accountList.add(BaseInitializer.getAccountMap().get(acc.getId()));
                                    accIds.add(acc.getId());
                                    List<Device> devices = deviceDao.getDeviceByAccount(acc.getId());
                                    for(Device dev : devices){
                                        deviceMap.put(dev.getId(), dev);
                                        if(reportMap.containsKey(dev.getId()+"")) {
                                            actualReportMap.put(dev.getId() + "", reportMap.get(dev.getId()+""));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    targetList = accountTargetDao.findByPanchayat(accIds);
                    dataList.putAll(hgetAll(accountList));
                    //deviceMap = deviceDao.getModifiedDeviceMapByAcc(accountList);
                }else{
                    throw new NoRecordFoundException();
                }
                RedisToMongoDaoImpl redisToMongoDao = new RedisToMongoDaoImpl();
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE,-1);
                String date = getDbPattern().format(cal.getTime());
                nonFuncSCH = redisToMongoDao.nonFuncCount(accountList,actualReportMap,"NFS",date, date);
                //nonFuncIOT = ReportUtils.nonFunctionalSch(reportMap, dataList);
            } else {
                if (!user.getPin().equals(this.requestMessage.getPin()))
                    throw new InvalidPassword();
                if (!user.getStatus().getCode().equals("A")) {
                    throw new InActiveUser();
                }
                throw new InvalidUser();
            }

            this.responseMessage.setResultcode(Integer.valueOf(0));
            this.responseMessage.setResultDescription("Success");
        }catch (EntityException ee) {
            this.responseMessage.setResultcode(ee.getResultCode(Integer.valueOf(0)));
            this.responseMessage.setResultDescription(ee.getDescription());
            this.responseMessage.setTimestamp((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")).format(new Date()));

        }catch (Exception ex) {
            this.responseMessage.setResultcode(Integer.valueOf(206));
            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            ex.printStackTrace();
        } finally {
            if (this.responseMessage.getResultcode().intValue() == 0) {
                response = generateJSONResponse(accountMap);
            } else {
                response = generateFailureResponse(this.responseMessage);
            }
        }
        return response;
    }

        public String generateJSONResponse(Map<Long, Account> accountMap) {
            JSONObject mainObj = new JSONObject();
            Long offline = 0L, online = 0L;
            try {
                JSONObject jsonObject = responseHeader(this.requestMessage, this.responseMessage);
                jsonObject.put(Constants.NODETRANSID, this.responseMessage.getTransid());

                //Long nonFuncIot = 0L;

                Long target = 0L;
                if(targetList != null && targetList.size()>0){
                    for(AccountTarget accountTarget : targetList){
                        target = target + accountTarget.getTotalDevices();
                    }
                }else{
                    target = devArray.length() +0L;
                }

                online = actualReportMap.size()+nonFuncSCH;
                nonFuncIOT = deviceMap.size()-online;
                JSONObject summary = new JSONObject();
                summary.put(Constants.NODETOTAL, target);
                summary.put(Constants.NODERUNNING, deviceMap.size());
                //summary.put(Constants.NODERUNNING, quickView.getTotal());
                summary.put(Constants.NODEOFFLINE, nonFuncIOT);
                //summary.put(Constants.NODEONLINE, reportMap.size());
                summary.put(Constants.NODEONLINE,online);

                summary.put("outofreach", nonFuncIOT);
                jsonObject.put(Constants.NODESUMMARY,summary);
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
                    jsonObject.put(Constants.NODEVENDORCODE, rm.getVendorcode());
                    mainObj.put("response", jsonObject);
                } catch (JSONException jSONException) {
                }
                return mainObj.toString();
            }
}
