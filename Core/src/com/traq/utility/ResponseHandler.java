package com.traq.utility;


import com.traq.beanobjects.Route;
import com.traq.common.base.Constants;
import org.apache.commons.lang.StringUtils;

import java.util.TreeMap;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Amit Kamboj
 * Date: 11 May, 2015
 * Time: 4:57:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResponseHandler {
    private String xmlString;

    public String makeOpeningTag(String tagName){
        StringBuffer sb = new StringBuffer();
        sb.append(Constants.OPEN_ANGLE).append(tagName).append(Constants.CLOSE_ANGLE);
        return sb.toString();
    }

    public String makeClosingTag(String tagName){
        StringBuffer sb = new StringBuffer();
        sb.append(Constants.OPNE_CLOSETAG_ANGLE).append(tagName).append(Constants.CLOSE_ANGLE);
        return sb.toString();
    }

    public String makeTag(String tagName, String value){
        StringBuffer sb = new StringBuffer();
        sb.append(Constants.OPEN_ANGLE).append(tagName)
                .append(Constants.CLOSE_ANGLE);
                if(value!= null && !value.equals("")){
                    sb.append(value);
                }else{
                    sb.append("NA");
                }
                sb.append(Constants.OPNE_CLOSETAG_ANGLE)
                .append(tagName)
                .append(Constants.CLOSE_ANGLE);
        return sb.toString();
    }

    public String makeTag(String tagName, int value){
        StringBuffer sb = new StringBuffer();
        sb.append(Constants.OPEN_ANGLE).append(tagName)
                .append(Constants.CLOSE_ANGLE)
                .append(value)
                .append(Constants.OPNE_CLOSETAG_ANGLE)
                .append(tagName)
                .append(Constants.CLOSE_ANGLE);
        return sb.toString();
    }

    public String makeTag(String tagName, long value){
        StringBuffer sb = new StringBuffer();
        sb.append(Constants.OPEN_ANGLE).append(tagName)
                .append(Constants.CLOSE_ANGLE)
                .append(value)
                .append(Constants.OPNE_CLOSETAG_ANGLE)
                .append(tagName)
                .append(Constants.CLOSE_ANGLE);
        return sb.toString();
    }

        public String makeTag(String tagName, double value){
        StringBuffer sb = new StringBuffer();
        sb.append(Constants.OPEN_ANGLE).append(tagName)
                .append(Constants.CLOSE_ANGLE)
                .append(value)
                .append(Constants.OPNE_CLOSETAG_ANGLE)
                .append(tagName)
                .append(Constants.CLOSE_ANGLE);
        return sb.toString();
    }

    public TreeMap getResponseReversalApi() {

        TreeMap<String, String[]> xmlKeys = new TreeMap<String, String[]>();
/*        String[] headerElement = {Constants.NODERESPONSETYPE};

        xmlKeys.put(Constants.ELEMENTHEADER, headerElement);
        //customeruserid tag not avilable in new balance api,udv1 and udv2 also
        String[] responseElement = {Constants.TAGAIRCRAFT, Constants.TAGAIRLINE,Constants.TAGAIRPORT, Constants.TAGPLACE,
                };
        xmlKeys.put(Constants.ELEMENTRESPONSE, responseElement);*/

        return xmlKeys;
    }


    public String GenerateXMLRouteResponse(Route route) {

        this.xmlString = textXMLMessage(this.getResponseReversalApi());
        HashMap responseMap = new HashMap();

        responseMap.put(Constants.NODEDISTANCE, route.getDistance());
        responseMap.put(Constants.NODEDURATION, route.getDuration());

        String newResponse = insertInXML(responseMap);
        return newResponse;
    }

    public String textXMLMessage(TreeMap xmlMap) {
        StringBuffer message = new StringBuffer();
        //message.append(Constants.XMLROOT);
        Iterator iterator = xmlMap.keySet().iterator();
        while (iterator.hasNext()) {

            String key = (String) iterator.next();
            String[] xVal = (String[]) xmlMap.get(key);
            message.append("<" + key + ">");
            for (int i = 0; i < xVal.length; i++) {
                message.append("<" + xVal[i] + ">");
                message.append("</" + xVal[i] + ">");
            }
            message.append("</" + key + ">");

        }
        //message.append(Constants.CLOSINGXMLROOT);
        return message.toString();
    }

    public static String insertTag(String response, String tagName, String tagValue) {

        String result = "";
        String tagElementValue = "<" + tagName + ">" + tagValue + "</" + tagName + ">";
        try {

            result = response.replace(Constants.XMLROOT , Constants.XMLROOT + tagElementValue);
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
        return result;
    }


    public String searchInXML(String searchNode) {
        try {

            int firstpos = this.xmlString.indexOf("<" + searchNode + ">");
            int endpos = this.xmlString.indexOf("</" + searchNode + ">");
            String nodeValue = this.xmlString.substring(firstpos + searchNode.length() + 2, endpos);
            return nodeValue;

        } catch (Exception e1) {
            // e1.printStackTrace();
            return "";
        }
    }

    public String searchInXML(String responseXML, String searchNode) {
        try {

            int firstpos = responseXML.indexOf("<" + searchNode + ">");
            int endpos = responseXML.indexOf("</" + searchNode + ">");
            String nodeValue = responseXML.substring(firstpos + searchNode.length() + 2, endpos);
            return nodeValue;

        } catch (Exception e1) {
            // e1.printStackTrace();
            return "";
        }
    }

    public String replaceInXML(String message, String nodeName, String value) {
        try {
            String openTag = "<" + nodeName + ">";
            String closeTag = "</" + nodeName + ">";

            int tagOccurence = StringUtils.countMatches(message, openTag);
            if (tagOccurence > 0) {
                String[] replaceValues = StringUtils.substringsBetween(message, openTag, closeTag);


                for (int j = 0; j < replaceValues.length; j++) {
                    message = message.replace(openTag + replaceValues[j] + closeTag, openTag + value + closeTag);
                }

            }
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
        return message;
    }

    public String insertInXML(HashMap valueMap) {
        try {
            StringBuffer xmlBuffer = new StringBuffer(this.xmlString);
            Iterator iterator = valueMap.keySet().iterator();
            String rstr = "";
            while (iterator.hasNext()) {

                String nodeName = (String) iterator.next();
                String insertValue = (String) valueMap.get(nodeName);
                int firstposition = xmlBuffer.indexOf("<" + nodeName + ">");
                int getPosition = xmlBuffer.indexOf("</" + nodeName + ">");
                int lastPosition = xmlBuffer.indexOf("</" + nodeName + ">") + nodeName.length() + 3;
                if (insertValue != null && !insertValue.isEmpty()) {
                    xmlBuffer.insert(getPosition, insertValue);

                } else {
                    if (nodeName.compareToIgnoreCase(Constants.NODEDISTANCE) != 0)
                        xmlBuffer = xmlBuffer.replace(firstposition, lastPosition, "");
                }
            }


            return xmlBuffer.toString();

        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
    }
}
