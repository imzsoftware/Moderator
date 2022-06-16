/*     */ package com.imz.praj.processor;

import com.traq.beanobject.AccountWiseSummary;
import com.traq.beanobject.DashBoardSummary;
import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.*;
import com.traq.common.data.model.dao.*;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.SummaryHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

    public class PrajSummaryProcessor extends BaseInitializer implements RequestProcessorInterface {
        private ResponseMessage responseMessage;
        private RequestMessage requestMessage;
        private String request;
        private AccountWiseSummary accSummary = new AccountWiseSummary();
        List<Driver> driverList = null;
        Map<String, LiveTrack> trackMap = null;
        Map<Long, Driver> driverMap = null;
        RequestBean rb;

        public ResponseMessage getResponseMessage() {
/*  42 */     return this.responseMessage;
/*     */   }

        public void setResponseMessage(ResponseMessage responseMessage) {
/*  46 */     this.responseMessage = responseMessage;
/*     */   }

        public PrajSummaryProcessor(ResponseMessage responseMessage) {
/*  50 */     this.responseMessage = responseMessage;
/*     */   }

        public PrajSummaryProcessor() {}

        public String executeXML(RequestBean _rb) {
/*  57 */     return request;
/*     */   }

        public String executeJSON(RequestBean _rb) {
            rb = _rb;
            request = rb.getRequest();

            SummaryHandler rh = new SummaryHandler();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
            String response = "";

            DashBoardSummary dbSummary = new DashBoardSummary();

            try {
                JSONObject object = new JSONObject(request);
                requestMessage = rh.getRequest(object.getJSONObject("request"), TagValues.getNodeValue(object, "requesttype"));

                responseMessage = new ResponseMessage();
                responseMessage.setMessagetype(requestMessage.getMessagetype());
                responseMessage.setResponsects(sdf.format(new Date()));

                UserDao customerDao = (UserDao)ApplicationBeanContext.getInstance().getBean("userDao");
                User user = customerDao.login(this.requestMessage.getUsername());

                AccountDao accountDao = (AccountDao)ApplicationBeanContext.getInstance().getBean("accountDao");
                Account account = accountDao.getAccountById(user.getAccId(), true);

                DeviceDao deviceDao = (DeviceDao)ApplicationBeanContext.getInstance().getBean("deviceDao");
                LiveTrackDao liveTrackDao = (LiveTrackDao)ApplicationBeanContext.getInstance().getBean("liveTrackDao");
                trackMap = liveTrackDao.findMapByAccount(user.getAccId());

                List<Device> deviceList = null;

                dbSummary.setDeviceList(deviceList);

                responseMessage.setResultcode(Integer.valueOf(0));
                responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(0)));
            }
            catch (Exception ex) {
                responseMessage.setResultcode(Integer.valueOf(206));
                responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
                //ex.printStackTrace();
            } finally {
                if (responseMessage.getResultcode().intValue() == 0) {
                    response = generateJSONResponse(dbSummary);
                } else {
                    response = generateFailureResponse(responseMessage);
                }
            }
            info("Final Response..............." + response);
            return response;
        }

        public JSONObject responseHeader() {
            JSONObject header = new JSONObject();
            try {
                header.put("responsetype", this.responseMessage.getMessagetype());
                header.put("resultcode", this.responseMessage.getResultcode());
                header.put("resultdescription", this.responseMessage.getResultDescription());
            } catch (Exception exception) {}

            return header;
        }

        public String generateJSONResponse(DashBoardSummary dbSummary) {
            JSONObject mainObj = new JSONObject();
            try {
                JSONObject jsonObject = new JSONObject();
                mainObj = responseHeader(this.requestMessage, this.responseMessage);
                jsonObject.put("summary", dbSummary.toJsonArray(dbSummary.getDeviceList(), this.trackMap));
                jsonObject.put("driver", Driver.toJson(this.driverList));
                jsonObject.put("devicedata", LiveTrack.toJson(this.trackMap));
                jsonObject.put("accdetail", dbSummary.toAccDetailJson());

                mainObj.put("response", jsonObject);
            }
            catch (JSONException jSONException) {

            } catch (Exception exception) {}

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
}
