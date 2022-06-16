package com.rep.beanobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.traq.common.base.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class NearestVehicleInfo
{
    private Double latitude;
    private Double longitude;
    private String vehicleName;
    private String licenseNumber;
    private String location;
    private String driverName;
    private String contactNumber;

    public NearestVehicleInfo()
    {

    }

    public NearestVehicleInfo(Double latitude, Double longitude, String vehicleName, String licenseNumber, String location, String driverName, String contactNumber) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.vehicleName = vehicleName;
        this.licenseNumber = licenseNumber;
        this.location = location;
        this.driverName = driverName;
        this.contactNumber = contactNumber;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public static JSONObject toJson(NearestVehicleInfo info) {
        JSONObject object = new JSONObject();
        try
        {
            object.put(Constants.NODELATITUDE, info.getLatitude());
            object.put(Constants.NODELONGITUDE, info.getLongitude());
            object.put(Constants.NODEVEHICLENAME, info.getVehicleName());
            object.put(Constants.NODELICENSENO, info.getLicenseNumber());
            object.put(Constants.NODEADDRESS, info.getLocation());
            object.put(Constants.NODEDRIVER, info.getDriverName());
            object.put(Constants.NODEPHONENO, info.getContactNumber());
        }
        catch (JSONException je)
        {
            je.printStackTrace();
            return null;

        }catch (NullPointerException nfe){
            return null;
        }
        return object;
    }

    public static String toJsonArray(List<NearestVehicleInfo> infoList) throws JsonProcessingException
    {
        String jsonObj = null;
        JSONArray jsonArray = new JSONArray();

        for(NearestVehicleInfo info : infoList)
        {
            JSONObject obj = NearestVehicleInfo.toJson(info);
            jsonArray.put(obj);
        }

        jsonObj = jsonArray.toString();
        return jsonObj;
    }

    @Override
    public String toString()
    {
        return "NearestVehicleInfo{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", vehicleName='" + vehicleName + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", location='" + location + '\'' +
                ", driverName='" + driverName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                '}';
    }
}
