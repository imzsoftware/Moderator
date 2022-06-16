package com.traq.manipulator.handler;



import com.traq.common.apihandler.RequestMessage;
import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.Constants;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Amit on 22/2/18.
 */
public class DLHHandler {

    public RequestMessage getRequest(String message, String messageType) {
        RequestMessage req = new RequestMessage();
        try {
            req.setMessagetype(messageType);
            req.setTransid(TagValues.getNodeValue(message, Constants.NODETRANSID));
            req.setType(TagValues.getNodeValue(message, Constants.NODETYPE));
            req.setMobileno(TagValues.getNodeValue(message, Constants.NODEMOBILENO));
            req.setComments(TagValues.getNodeValue(message, Constants.NODEMESSAGE));
            req.setVendorcode(TagValues.getNodeValue(message, Constants.NODEVENDORCODE));


        } catch (Exception e) {
            e.printStackTrace();
        }

        return req;
    }

    public RequestMessage getRequest(JSONObject message, String messageType) {
        RequestMessage req = new RequestMessage();
        try {
            req.setMessagetype(messageType);
            req.setTransid(TagValues.getNodeValue(message, Constants.NODETRANSID));
            req.setType(TagValues.getNodeValue(message, Constants.NODETYPE));
            req.setMobileno(TagValues.getNodeValue(message, Constants.NODEMOBILENO));
            req.setComments(TagValues.getNodeValue(message, Constants.NODEMESSAGE));
            req.setVendorcode(TagValues.getNodeValue(message, Constants.NODEVENDORCODE));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return req;
    }

    public static JSONObject responseHeader(ResponseMessage respMsg){
        JSONObject header = new JSONObject();
        try{
            header.put(Constants.NODERESPONSETYPE, respMsg.getMessagetype());
            header.put(Constants.NODERESULTCODE, respMsg.getResultcode());
            header.put(Constants.NODERESULTDESCRIPTION, respMsg.getResultDescription());
            header.put(Constants.NODETRANSID, respMsg.getTransid());
            header.accumulate(Constants.NODEVENDORCODE, respMsg.getVendorcode());

        }catch (Exception ee){

        }
        return header;
    }

    public static String buildResponse(ResponseMessage respMsg){

        JSONObject mainObj = responseHeader(respMsg);
        JSONObject object = new JSONObject();
        try{
            object.accumulate(Constants.NODEMESSAGE, respMsg.getComments());
            object.accumulate(Constants.NODEMOBILENO, respMsg.getMobileno());
            object.accumulate(Constants.NODEVENDORRESULTCODE, respMsg.getVendorresultcode());
            object.accumulate(Constants.NODEVENDORRESULTDESCRIPTION, respMsg.getVendorResultDesc());
            object.accumulate(Constants.NODERESPONSECTS, respMsg.getResponsects());

            mainObj.accumulate(Constants.NODERESPONSE, object);
        }catch (JSONException jpe){

        }
        return mainObj.toString();
    }


}
