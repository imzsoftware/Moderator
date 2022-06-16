package com.traq.utility;

import org.json.JSONObject;
import org.json.XML;



import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: amit
 * Date: 25 Sep, 2015
 * Time: 10:52:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class XMLtoJsonConverter {

        public static void main(String[] args) throws Exception
        {
            xmlToJson("");
        }

   private static JSONObject xmlToJson(String path){
        try
            {

                File file = new File ("F:\\Amit\\Distances\\out\\ResponseXMLFile.xml");
                InputStream inputStream = new FileInputStream(file);
                StringBuilder builder =  new StringBuilder();
                int ptr = 0;
                while ((ptr = inputStream.read()) != -1 )
                {
                    builder.append((char) ptr);
                }

                String xml  = builder.toString();
                JSONObject jsonObj = XML.toJSONObject(xml);
                System.out.println("1----"+jsonObj);

                //String xmlString  = "<?xml version=\"1.0\"?><ASF_Service_ResponseVO id=\"1\"><service type=\"String\">OnboardingV2</service><operation type=\"String\">start_onboarding_session</operation><requested_version type=\"String\">1.0</requested_version><actual_version type=\"String\">1.0</actual_version><server_info type=\"String\">onboardingv2serv:start_onboarding_session&CalThreadId=85&TopLevelTxnStartTime=13b40fe91c4&Host=L-BLR-00438534&pid=3564</server_info><result type=\"Onboarding::StartOnboardingSessionResponse\" id=\"2\"><onboarding_id type=\"String\">137</onboarding_id><success type=\"bool\">true</success></result></ASF_Service_ResponseVO>";

                /*jsonObj = XML.toJSONObject(xmlString);
                System.out.println(jsonObj.toString());*/
                return jsonObj;
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
       return null;
    }

   public static String xmlToJson(String responseXML, String path){
       JSONObject jsonObj;
        try
            {

                if(path != null){
                    File file = new File (path);
                    InputStream inputStream = new FileInputStream(file);
                    StringBuilder builder =  new StringBuilder();
                    int ptr = 0;
                    while ((ptr = inputStream.read()) != -1 )
                    {
                        builder.append((char) ptr);
                    }

                    responseXML = builder.toString();
                }
                //jsonObj = XML.toJSONObject(responseXML.replaceAll("ns1:",""));
                //System.out.println("1----"+jsonObj);

                //String xmlString  = "<?xml version=\"1.0\"?><ASF_Service_ResponseVO id=\"1\"><service type=\"String\">OnboardingV2</service><operation type=\"String\">start_onboarding_session</operation><requested_version type=\"String\">1.0</requested_version><actual_version type=\"String\">1.0</actual_version><server_info type=\"String\">onboardingv2serv:start_onboarding_session&CalThreadId=85&TopLevelTxnStartTime=13b40fe91c4&Host=L-BLR-00438534&pid=3564</server_info><result type=\"Onboarding::StartOnboardingSessionResponse\" id=\"2\"><onboarding_id type=\"String\">137</onboarding_id><success type=\"bool\">true</success></result></ASF_Service_ResponseVO>";

                /*jsonObj = XML.toJSONObject(xmlString);
                System.out.println(jsonObj.toString());*/
                //JSON json = jsonObj.
                return responseXML; //jsonObj.toString();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
       return null;
    }
}
