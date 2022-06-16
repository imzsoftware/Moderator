package com.traq.beanobjects;

import com.traq.common.apihandler.RequestMessage;


/**
 * Created by IntelliJ IDEA.
 * User: Amit
 * Date: 27 Nov, 2015
 * Time: 3:32:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class Route extends RequestMessage{
    private Long routeId;
    private Location startLocation;
    private Location endLocation;
    private String sourceId;
    private String encryptedSourceId;
    private String destinationId;
    private String encryptedDestinationId;
    private String startPoint;
    private String shortStartPoint;
    private String startLatitude;
    private String startLongitude;
    private String endPoint;
    private String shortEndPoint;
    private String startCountry;
    private String startState;
    private String startCity;
    private String endLatitude;
    private String endLongitude;
    private String startPostalCode;
    private String endCountry;
    private String endState;
    private String endCity;
    private String endPostalCode;
    private String sourceType;
    private String destType;

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getShortStartPoint() {
        return shortStartPoint;
    }

    public void setShortStartPoint(String shortStartPoint) {
        this.shortStartPoint = shortStartPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getShortEndPoint() {
        return shortEndPoint;
    }

    public void setShortEndPoint(String shortEndPoint) {
        this.shortEndPoint = shortEndPoint;
    }


    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getEncryptedSourceId() {
        return encryptedSourceId;
    }

    public void setEncryptedSourceId(String encryptedSourceId) {
        this.encryptedSourceId = encryptedSourceId;
    }

    public String getEncryptedDestinationId() {
        return encryptedDestinationId;
    }

    public void setEncryptedDestinationId(String encryptedDestinationId) {
        this.encryptedDestinationId = encryptedDestinationId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getStartCountry() {
        return startCountry;
    }

    public void setStartCountry(String startCountry) {
        this.startCountry = startCountry;
    }

    public String getStartState() {
        return startState;
    }

    public void setStartState(String startState) {
        this.startState = startState;
    }

    public String getStartCity() {
        return startCity;
    }

    public void setStartCity(String startCity) {
        this.startCity = startCity;
    }

    public String getStartPostalCode() {
        return startPostalCode;
    }

    public void setStartPostalCode(String startPostalCode) {
        this.startPostalCode = startPostalCode;
    }

    public String getEndCountry() {
        return endCountry;
    }

    public void setEndCountry(String endCountry) {
        this.endCountry = endCountry;
    }

    public String getEndState() {
        return endState;
    }

    public void setEndState(String endState) {
        this.endState = endState;
    }

    public String getEndCity() {
        return endCity;
    }

    public void setEndCity(String endCity) {
        this.endCity = endCity;
    }

    public String getEndPostalCode() {
        return endPostalCode;
    }

    public void setEndPostalCode(String endPostalCode) {
        this.endPostalCode = endPostalCode;
    }

    public String getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(String startLatitude) {
        this.startLatitude = startLatitude;
    }

    public String getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(String startLongitude) {
        this.startLongitude = startLongitude;
    }

    public String getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(String endLatitude) {
        this.endLatitude = endLatitude;
    }

    public String getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(String endLongitude) {
        this.endLongitude = endLongitude;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getDestType() {
        return destType;
    }

    public void setDestType(String destType) {
        this.destType = destType;
    }

    @Override
    public String toString() {
        return "Route{" +
                "routeId=" + routeId +
                ", startLocation=" + startLocation +
                ", endLocation=" + endLocation +
                ", sourceId='" + sourceId + '\'' +
                ", destinationId='" + destinationId + '\'' +
                ", startPoint='" + startPoint + '\'' +
                ", shortStartPoint='" + shortStartPoint + '\'' +
                ", startLatitude='" + startLatitude + '\'' +
                ", startLongitude='" + startLongitude + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", shortEndPoint='" + shortEndPoint + '\'' +
                ", startCountry='" + startCountry + '\'' +
                ", startState='" + startState + '\'' +
                ", startCity='" + startCity + '\'' +
                ", endLatitude='" + endLatitude + '\'' +
                ", endLongitude='" + endLongitude + '\'' +
                ", startPostalCode='" + startPostalCode + '\'' +
                ", endCountry='" + endCountry + '\'' +
                ", endState='" + endState + '\'' +
                ", endCity='" + endCity + '\'' +
                ", endPostalCode='" + endPostalCode + '\'' +
                '}';
    }

/*    public String toXMLString(){
        ResponseHandler rh = new ResponseHandler();
        StringBuffer sb = new StringBuffer();
        sb.append(rh.makeOpeningTag(Constants.TAGROUTE))
                .append(rh.makeTag(Constants.TAGID,this.routeId))
                .append(rh.makeTag(Constants.TAGSRCNAME,this.startPoint))
                .append(rh.makeTag(Constants.TAGDESTNAME,this.endPoint))
                .append(rh.makeTag(Constants.TAGSRC,this.shortStartPoint))
                .append(rh.makeTag(Constants.TAGDEST,this.shortEndPoint))
                .append(rh.makeTag(Constants.TAGDISTANCE, this.distance))
                .append(rh.makeTag(Constants.TAGDURATION,this.duration))
                .append(startLocation.toXMLString())
                .append(endLocation.toXMLString())
                .append(startCountry.toXMLString())
                .append(endCountry.toXMLString())
                .append(rh.makeClosingTag(Constants.TAGROUTE));

        return sb.toString();

    }*/
}
