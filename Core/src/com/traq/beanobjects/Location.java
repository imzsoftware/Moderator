package com.traq.beanobjects;


/**
 * Created by IntelliJ IDEA.
 * User: Amit
 * Date: 27 Nov, 2015
 * Time: 3:32:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class Location {
    private Long id = 0L;
    private String name;
    private Country country;
    private String shortName;
    private Double latitude = 0.0;
    private Double longitude =0.0;
    private String closestAddress;
    private String adminLevel1;
    private String adminLevel2;
    private String locality;
    private String subLocality;
    private Integer noOfSearches;
    private String cabOperator;
    private String desc;
    private String placeId="";
    private String locationId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
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

    public String getClosestAddress() {
        return closestAddress;
    }

    public void setClosestAddress(String closestAddress) {
        this.closestAddress = closestAddress;
    }

    public String getAdminLevel1() {
        return adminLevel1;
    }

    public void setAdminLevel1(String adminLevel1) {
        this.adminLevel1 = adminLevel1;
    }

    public String getAdminLevel2() {
        return adminLevel2;
    }

    public void setAdminLevel2(String adminLevel2) {
        this.adminLevel2 = adminLevel2;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getSubLocality() {
        return subLocality;
    }

    public void setSubLocality(String subLocality) {
        this.subLocality = subLocality;
    }

    public Integer getNoOfSearches() {
        return noOfSearches;
    }

    public void setNoOfSearches(Integer noOfSearches) {
        this.noOfSearches = noOfSearches;
    }

    public String getCabOperator() {
        return cabOperator;
    }

    public void setCabOperator(String cabOperator) {
        this.cabOperator = cabOperator;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", Country=" + country +
                ", shortName='" + shortName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", closestAddress='" + closestAddress + '\'' +
                ", adminLevel1='" + adminLevel1 + '\'' +
                ", adminLevel2='" + adminLevel2 + '\'' +
                ", locality='" + locality + '\'' +
                ", subLocality='" + subLocality + '\'' +
                ", noOfSearches=" + noOfSearches +
                ", cabOperator='" + cabOperator + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
