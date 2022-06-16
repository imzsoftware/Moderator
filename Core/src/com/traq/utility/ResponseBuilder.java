package com.traq.utility;

import com.traq.common.apihandler.ResponseMessage;
import com.traq.common.xmlutils.XMLProcessor;
import com.traq.common.base.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: Amit
 * Date: 27 Nov, 2015
 * Time: 1:39:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResponseBuilder {


    public TreeMap getTrainResponseApi() {

        TreeMap<String, String[]> xmlKeys = new TreeMap<String, String[]>();
        String[] headerElement = {Constants.NODERESPONSETYPE};

        xmlKeys.put(Constants.ELEMENTHEADER, headerElement);


        String[] responseElement = {Constants.NODERESULTCODE, Constants.NODERESULTDESCRIPTION

        };

        xmlKeys.put(Constants.ELEMENTRESPONSE, responseElement);

        return xmlKeys;
    }

    public String GenerateXMLResponse(ResponseMessage rm) {


        String xmlResponse = new XMLProcessor().textXMLMessage(this.getTrainResponseApi());
        HashMap responseMap = new HashMap();
        XMLProcessor xp = new XMLProcessor(xmlResponse);

        responseMap.put(Constants.NODERESPONSETYPE, rm.getMessagetype());
        responseMap.put(Constants.NODERESULTCODE, rm.getResultcode());
        responseMap.put(Constants.NODERESULTDESCRIPTION, rm.getResultDescription());

        String newResponse = xp.insertInXML(responseMap);

        return newResponse;

    }

    public static String timeConversion(Long time){
        long totalTime=0;
        totalTime = time/3600;
        String hr = totalTime + " hour ";

        totalTime = time%60;
        if(time>3600){
            return hr + totalTime +" mins";
        }else{
            totalTime = time/60;
            return totalTime +" mins";    
        }
    }

    public static String distanceConversion(Long distance){
        long totalDistance=0;
        totalDistance = distance/1000;
        String hr = totalDistance + " km ";
        totalDistance = distance%1000;

        if(distance>1000){
            return hr + totalDistance +" mtrs";
        }else{
            return totalDistance +" mtrs";
        }
    }

}
