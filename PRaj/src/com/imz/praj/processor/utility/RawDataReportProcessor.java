package com.imz.praj.processor.utility;

import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Account;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.DeviceType;
import com.traq.common.data.entity.RawData;
import com.traq.common.data.entity.User;
import com.traq.common.data.model.dao.AccountDao;
import com.traq.common.data.model.dao.DeviceDao;
import com.traq.common.data.model.dao.DeviceTypeDao;
import com.traq.common.data.model.dao.UserDao;
import com.traq.common.data.model.mongodao.mongoimpl.RawDataMongoDaoImpl;
import com.traq.common.generator.ResultCodeDescription;
import com.traq.common.handler.ReportHandler;
import com.traq.common.processor.RequestProcessorInterface;
import com.traq.util.RequestBean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RawDataReportProcessor extends BaseInitializer implements RequestProcessorInterface {
    private ResponseMessage responseMessage;
    private RequestMessage requestMessage;
    private String request;
    AccountDao accountDao;
    List<RawData> dataList = null;
    DeviceType deviceType = null;
    Map<Long, Account> parentMap = new HashMap<>();
    SimpleDateFormat newPattern = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat dbPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    RequestBean rb;
    User user = null;

    public ResponseMessage getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public RawDataReportProcessor(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public RawDataReportProcessor() {
    }

    public String executeXML(RequestBean _rb) {
        return this.request;
    }

    public String executeJSON(RequestBean _rb) {
        this.rb = _rb;
        this.request = this.rb.getRequest();

        ReportHandler dh = new ReportHandler();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        String response = "";


        this.accountDao = (AccountDao) ApplicationBeanContext.getInstance().getBean("accountDao");
        UserDao userDao = (UserDao) ApplicationBeanContext.getInstance().getBean("userDao");

        try {
            this.responseMessage = new ResponseMessage();
            JSONObject object = new JSONObject(this.request);
            this.requestMessage = dh.getRequest(object.getJSONObject("request"), TagValues.getNodeValue(object, "requesttype"));
            requestMessage.setVendorcode(TagValues.getNodeValue(object, Constants.NODEVENDORCODE));

            Date date = this.newPattern.parse(this.requestMessage.getStartDate());
            this.requestMessage.setStartDate(this.dbPattern.format(date));
            date = this.newPattern.parse(this.requestMessage.getEndDate());
            this.requestMessage.setEndDate(this.dbPattern.format(date));
            this.user = userDao.login(this.requestMessage.getUsername());
            boolean isValid = validateUser(this.user, this.requestMessage.getPin());
            if (isValid) {
                if (checkNullAndEmpty(this.requestMessage.getAccId())) {
                    this.responseMessage.setMessagetype(this.requestMessage.getMessagetype());
                    this.responseMessage.setResponsects(sdf.format(new Date()));
                    if (getAppConfig().getSwitchdb().equals("1")) {
                        AccountDao accountDao = (AccountDao) ApplicationBeanContext.getInstance().getBean("accountDao");
                        Account account = accountDao.getAccountById(this.requestMessage.getAccId(), false);
                        info("Raw Data Report....Client=" + account.getClient());
                        if (checkNullAndEmpty(account.getClient())) {
                            this.requestMessage.setClient(account.getClient() + "_");
                        } else {

                            this.requestMessage.setClient("TM_");
                        }
                    }
                    DeviceDao deviceDao = (DeviceDao) ApplicationBeanContext.getInstance().getBean("deviceDao");
                    List<Device> deviceList = deviceDao.getDeviceByAccountprimary(requestMessage.getAccId());
                    for(Device device : deviceList) {
                        if(device.getId() == 7670){
                            continue;
                        }
                        requestMessage.setAssetId(device.getId());
                        RawDataMongoDaoImpl rawDataMongoDaoImpl = new RawDataMongoDaoImpl();
                        dataList = rawDataMongoDaoImpl.findRawData(this.requestMessage, null);
                        for (RawData data : dataList) {
                            if(data.getData().startsWith("7E0210")){
                                pushToKafka(data,requestMessage.getVendorcode());
                                //pushToKafka(data,"TM");
                            }
                        }
                    }
                    responseMessage.setResultcode(Integer.valueOf(0));
                    responseMessage.setResultDescription("Success");
                } else {

                    this.responseMessage.setResultcode(Integer.valueOf(11));
                    this.responseMessage.setResultDescription("User Not Found");
                }
            } else {
                this.responseMessage.setResultcode(Integer.valueOf(11));
                this.responseMessage.setResultDescription("User Not Found");
            }

        } catch (Exception ex) {
            this.responseMessage.setResultcode(Integer.valueOf(206));
            this.responseMessage.setResultDescription(ResultCodeDescription.getDescription(Integer.valueOf(206)));
            ex.printStackTrace();
        } finally {
            if (this.responseMessage.getResultcode().intValue() == 0) {
                response = generateJSONResponse();
            } else {
                response = generateFailureResponse(this.responseMessage);
            }
        }

        return response;
    }


    private JSONArray rawDataReport() throws JSONException, Exception {
        JSONArray inner = new JSONArray();
        try {
            for (RawData data : this.dataList) {
                DeviceTypeDao deviceTypeDao = (DeviceTypeDao) ApplicationBeanContext.getInstance().getBean("deviceTypeDao");
                JSONObject object = new JSONObject();
                object.accumulate("devname", this.deviceType.getName());


                object.accumulate("data", data.getData());
                Date datetime = this.dbPattern.parse(data.getCreatedOn());
                object.accumulate("cts", this.newPattern.format(datetime));
                inner.put(object);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return inner;
    }


    public String generateJSONResponse() {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("responsetype", this.responseMessage.getMessagetype());
            jsonObject.put("resultcode", this.responseMessage.getResultcode());
            jsonObject.put("resultdescription", this.responseMessage.getResultDescription());
            jsonObject.put("report", rawDataReport());

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
            JSONObject jsonObject = responseHeader(this.requestMessage, this.responseMessage);

            mainObj.put("response", jsonObject);
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mainObj.toString();
    }

    private void pushToKafka(RawData rawData, String client){
        String data = rawData.getData();
        try {
            data = data.substring(26);
            StringBuffer sb = new StringBuffer("<traq>");
            sb.append("<devicedata><client>").append(client).append("</client><cts>")
                    .append(rawData.getCreatedOn()).append("</cts><imei>")
                    .append(rawData.getIMEI()).append("</imei><vehnum>").append(rawData.getIMEI())
                    .append("</vehnum><deviceid>").append(rawData.getDeviceId()).append("</deviceid><accid>")
                    .append(rawData.getAccId()).append("</accid><devicetypeid>").append(rawData.getDeviceTypeId())
                    .append("</devicetypeid><name>").append(rawData.getName()).append("</name></devicedata><hist><data>")
                    .append(data).append("</data></hist></traq>");

            info("Sending to Karka   " +sb.toString());
            info("Karka Response " +sendToKafka(sb.toString()));

        }catch (Exception e) {
        }
    }

    private String sendToKafka(String request){
        StringBuilder response = new StringBuilder();
        BufferedReader reader = null;
        if(request != null && !request.isEmpty()) {
            try {
                String url = "http://localhost:9101/traqmatix/kfk/prod/";
                URL endPoint = new URL(url);
                OutputStreamWriter writer = null;
                OutputStream output = null;

                HttpURLConnection connection = (HttpURLConnection) endPoint.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("charset", "UTF-8");
                connection.setRequestProperty("Content-Type", "text/plain");
                connection.setRequestProperty("cache-control", "no-cache");

                writer = new OutputStreamWriter(connection.getOutputStream());
                output = connection.getOutputStream();

                output.write(request.getBytes("UTF-8"));
                writer.flush();

                String line;
                InputStreamReader is = null;

                if (connection.getResponseCode() >= 400) {
                    is = new InputStreamReader(connection.getErrorStream());
                } else {
                    is = new InputStreamReader(connection.getInputStream());
                }
                reader = new BufferedReader(is);
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return response.toString();
    }
}
