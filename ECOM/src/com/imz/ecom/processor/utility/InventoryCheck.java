package com.imz.ecom.processor.utility;

import com.traq.common.apihandler.TagValues;
import com.traq.common.base.Constants;
import com.traq.common.beanloader.ApplicationBeanContext;
import com.traq.common.data.entity.Device;
import com.traq.common.data.entity.GeoFence;
import com.traq.common.data.model.dao.GeoFenceDao;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.traq.common.base.BaseInitializer.checkNullAndEmpty;

public class InventoryCheck {
    private final static long timeDiff = 19800*1000;  // Value in milliseconds 5Hr 30min from GMT
    private final static long oneDay = (24*60*60*1000);
    private static String ecomURL = "http://localhost:7008/";

    public static JSONArray activeTrips(){
        JSONArray result = null;
        try {

            URL url = new URL(ecomURL+"trips/locks?key="+System.currentTimeMillis());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "  + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
            result = new JSONArray(output.toString());

            conn.disconnect();

        } catch (MalformedURLException e) {

        } catch (IOException e) {

        }finally {
            return result;
        }
    }

    public static JSONArray tripByLock(String imei){
        JSONArray result = null;
        try {

            URL url = new URL(ecomURL+"trips/find/lock?lock="+imei);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "  + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
            result = new JSONArray(output.toString());

            conn.disconnect();

        } catch (MalformedURLException e) {

        } catch (IOException e) {

        }finally {
            return result;
        }
    }

    public static Boolean isAtLocation(Device device, String data) throws Exception {
        StringBuilder GEOIN = new StringBuilder().append("GEO_IN_").append(device.getAccId());

        Double lat = Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELATITUDE));
        Double lng = Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELONGITUDE));

        GeoFenceDao geoFenceDao = (GeoFenceDao) ApplicationBeanContext.getInstance().getBean("geoFenceDao");
        String geoName = null;
        List<GeoFence> geoFenceList = geoFenceDao.getGeofenceByAccount(Long.parseLong(TagValues.getNodeValue(data, Constants.NODEACCOUNTID)));
        int count = 0;
        for (GeoFence geoFence : geoFenceList) {
            String type = geoFence.getType();
            if (checkNullAndEmpty(type)) {
                if (type.equals("POLYGON")) {
                    count = geoFenceDao.validateGeoFence(geoFence.getId(), lat, lng);
                } else if (type.equals("CIRCLE")) {
                    count = geoFenceDao.validateInCircle(lat, lng, 200, geoFence.getId());
                } else if (type.equals("POI")) {
                    count = geoFenceDao.validateInCircle(lat, lng, 500, geoFence.getId());
                } else {
                    count = geoFenceDao.validateGeoFence(geoFence.getId(), lat, lng);
                }

                if (count > 0) {
                    geoName = geoFence.getDescription();
                    if (geoName.equals("NA") && geoName.length() > 5) {
                        geoName = geoFence.getName().substring(0, 5).toUpperCase();
                    } else if (geoName.equals("NA")) {
                        geoName = geoFence.getName();
                    }
                    break;
                }
            }
        }
        if (count > 0){
            liveData.put(Constants.NODESTATUS, "in");
            liveData.put(Constants.NODECODE, geoName);
        }else {
            liveData.put(Constants.NODESTATUS, "out");
        }

    }

}
