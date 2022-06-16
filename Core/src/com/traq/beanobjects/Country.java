package com.traq.beanobjects;


/**
 * Created by IntelliJ IDEA.
 * User: Amit
 * Date: 27 Nov, 2015
 * Time: 3:32:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class Country {
    private Long id;
    private String name;
    private String region;
    private String visible;
    private Double neLatitude;
    private Double neLongitude;
    private Double swLatitude;
    private Double swLongitude;
    private String currencyCode;

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getVisible() {
        return visible;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public Double getNeLatitude() {
        return neLatitude;
    }

    public void setNeLatitude(Double neLatitude) {
        this.neLatitude = neLatitude;
    }

    public Double getNeLongitude() {
        return neLongitude;
    }

    public void setNeLongitude(Double neLongitude) {
        this.neLongitude = neLongitude;
    }

    public Double getSwLatitude() {
        return swLatitude;
    }

    public void setSwLatitude(Double swLatitude) {
        this.swLatitude = swLatitude;
    }

    public Double getSwLongitude() {
        return swLongitude;
    }

    public void setSwLongitude(Double swLongitude) {
        this.swLongitude = swLongitude;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }


    @Override
    public String toString() {
        return "Country{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", visible='" + visible + '\'' +
                ", neLatitude=" + neLatitude +
                ", neLongitude=" + neLongitude +
                ", swLatitude=" + swLatitude +
                ", swLongitude=" + swLongitude +
                ", currencyCode='" + currencyCode + '\'' +
                '}';
    }
}
