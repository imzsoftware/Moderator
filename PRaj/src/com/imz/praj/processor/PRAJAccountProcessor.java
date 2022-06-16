package com.imz.praj.processor;

import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.User;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.AccountTrailDao;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.AccountHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class PRAJAccountProcessor extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    private Account requestMessage;
    private String request;
    RequestBean rb;
    List<Account> accountList = new ArrayList<>();
    Account level2 = null;
    Account level1 = null;

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public PRAJAccountProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }
    public PRAJAccountProcessor() {}

    public String executeXML(RequestBean _rb) {
        return this.request;
    }

    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();

        AccountHandler rh = new AccountHandler();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        String response = "";

        try {
            JSONObject object = new JSONObject(this.request);
            requestMessage = rh.getRequest(object.getJSONObject("request"), TagValues.getNodeValue(object, "requesttype"));
            requestMessage.setVendorcode(object.getString(Constants.NODEVENDORCODE));

            this.responseMessage = new ResponseMessage();
            this.responseMessage.setMessagetype(requestMessage.getMessagetype());
            this.responseMessage.setResponsects(sdf.format(new Date()));
            this.responseMessage.setTransid(rb.getTransid());

            AccountDao accountDao = (AccountDao) ApplicationBeanContext.getInstance().getBean("accountDao");
            //DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
            UserDao userDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");


            boolean isSuccess = false;
            User user = userDao.login(requestMessage.getUsername());
            boolean isValid = validateUser(user, requestMessage.getPin());

            if (isValid) {
                    if (checkNullAndEmpty(requestMessage.getUdv3())) {
                        List<Long> list = new ArrayList<>();
                        String[] ids = requestMessage.getUdv3().split(",");
                        for (int i = 0; i < ids.length; i++) {
                            list.add(Long.parseLong(ids[i]));
                        }
                        List<Account> accList = accountDao.Children(list);
                        for(Account account: accList) {
                            if (account.getType().equalsIgnoreCase(requestMessage.getType())) {
                                // info("AMIT account ..... "+account);
                                accountList.add(account);
                            }
                        };
                    }else{
                        List<Long> list = new ArrayList<>();
                        list.add(requestMessage.getAccId());
                        List<Account> accList = accountDao.Children(requestMessage.getAccId());
                        for(Account account: accList) {
                            if (account.getType().equalsIgnoreCase(requestMessage.getType())) {
                                accountList.add(account);
                            }
                        };
                    }
                    isSuccess = true;


                if (isSuccess) {
                    responseMessage.setResultcode(0);
                    responseMessage.setResultDescription("Success");
                } else {
                    responseMessage.setResultcode(1);
                    responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(1)));
                }
            } else {
                responseMessage.setResultcode(11);
                responseMessage.setResultDescription("User Not Found");
            }

        } catch (Exception ex) {
            responseMessage.setResultcode(206);
            responseMessage.setResultDescription(dataDuplicacyException(ex));
            ex.printStackTrace();
        } finally {
            if (responseMessage.getResultcode() == 0) {
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
            JSONObject jsonObject = responseHeader(requestMessage, this.responseMessage);
            if (accountList == null) {
                accountList = new ArrayList<>();
            }
            List<Long> accIds = new ArrayList<>();
            for (Account account : accountList) {
                if (account != null) {
                    accIds.add(account.getId());
                }
            }
            info("accountList SIZE -- "+accountList.size());
            jsonObject.put("data", Account.toPrajJsonArr(accountList));

            if (this.level1 != null) {
                jsonObject.put("level1", Account.toJson(this.level1));
            } else {
                jsonObject.put("level1", "");
            }
            if (this.level2 != null) {
                jsonObject.put("level2", Account.toJson(this.level2));
            } else {
                jsonObject.put("level2", "");
            }
            mainObj.put("response", jsonObject);
        }
        catch (JSONException je) {
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
            jsonObject.put("transid", rm.getTransid());

            mainObj.put("response", jsonObject);
        } catch (JSONException jSONException) {}


        return mainObj.toString();
    }


}


