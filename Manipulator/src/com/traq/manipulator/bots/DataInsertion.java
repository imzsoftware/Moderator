package com.traq.manipulator.bots;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.traq.common.apihandler.TagValues;
import com.traq.common.base.BaseInitializer;
import com.traq.common.base.Constants;
import org.bson.Document;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Amit on 5/6/19.
 */
public class DataInsertion extends BaseInitializer {

    public boolean insertTrackData(MongoDatabase database, String client,
                                   String data) throws IOException, InterruptedException {

        Document doc = new Document();
        try {
            Double lat = Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELATITUDE));
            Double lng = Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELONGITUDE));
            Double[] geo = new Double[]{lng, lat};
            doc.append(Constants.NODEGEOCODE, Arrays.asList(geo));
            doc.append(Constants.NODELATITUDE, lat);
            doc.append(Constants.NODELONGITUDE, lng);
            doc.append(Constants.NODEACCOUNTID, Long.parseLong(TagValues.getNodeValue(data, Constants.NODEACCOUNTID)));
            doc.append(Constants.NODEDEVICEID, Long.parseLong(TagValues.getNodeValue(data, Constants.NODEDEVICEID)));
            doc.append(Constants.NODEVEHICLENUMBER, TagValues.getNodeValue(data, Constants.NODEVEHICLENUMBER));
            doc.append(Constants.NODEIMEI, TagValues.getNodeValue(data, Constants.NODEIMEI));
            doc.append(Constants.NODEALTITUDE, TagValues.getNodeValue(data, Constants.NODEALTITUDE));
            doc.append(Constants.NODEDISHA, TagValues.getNodeValue(data, Constants.NODEDISHA));
            doc.append(Constants.NODETAMPER, TagValues.getNodeValue(data, Constants.NODETAMPER));
            String speed = TagValues.getNodeValue(data, Constants.NODESPEED);
            if (!checkNullAndEmpty(speed)) {
                speed = "0.0";
            }
            doc.append(Constants.NODESPEED, Double.parseDouble(speed.trim()));

            doc.append(Constants.NODEIGNITION, TagValues.getNodeValue(data, Constants.NODEIGNITION));
            String fuel = TagValues.getNodeValue(data, Constants.NODEFUEL);
            if (fuel.isEmpty()) {
                fuel = "0.00";
            } else {
                fuel = twoDecimalPlaces.format(Double.parseDouble(fuel));
            }
            doc.append(Constants.NODEFUEL, Double.parseDouble(fuel));
            String load = TagValues.getNodeValue(data, Constants.NODELOADS);
            if (load.isEmpty()) {
                load = "0.00";
            } else {
                load = twoDecimalPlaces.format(Double.parseDouble(load));
            }
            doc.append(Constants.NODELOADS, Double.parseDouble(load));
            doc.append(Constants.NODEIMMOBILISER, TagValues.getNodeValue(data, Constants.NODEIMMOBILISER));
            doc.append(Constants.NODEAC, TagValues.getNodeValue(data, Constants.NODEAC));
            //doc.append(Constants.NODEDOOR, TagValues.getNodeValue(data, Constants.NODEDOOR));
            doc.append(Constants.NODEBATTERY, TagValues.getNodeValue(data, Constants.NODEBATTERY));
            doc.append(Constants.NODELOCK, TagValues.getNodeValue(data, Constants.NODELOCK));

            doc.append(Constants.NODERFID, TagValues.getNodeValue(data, Constants.NODERFID));
            doc.append(Constants.NODEREASON, TagValues.getNodeValue(data, Constants.NODEREASON));
            doc.append(Constants.NODEPACKETTYPE, TagValues.getNodeValue(data, Constants.NODEPACKETTYPE));
            if (!TagValues.getNodeValue(data, Constants.NODEPACKETTYPE).isEmpty()) {
                doc.append(Constants.NODEEVENTSOURCETYPE, TagValues.getNodeValue(data, Constants.NODEEVENTSOURCETYPE));
            }
            doc.append(Constants.NODEGPS, TagValues.getNodeValue(data, Constants.NODEGPS));
            doc.append(Constants.NODESATELLITES, TagValues.getNodeValue(data, Constants.NODESATELLITES));
            doc.append(Constants.NODEORIGINTS, TagValues.getNodeValue(data, Constants.NODEORIGINTS));
            doc.append(Constants.NODECREATEDON, TagValues.getNodeValue(data, Constants.NODECREATEDON));
            doc.append(Constants.NODEPOWSTATUS, TagValues.getNodeValue(data, Constants.NODEPOWSTATUS));
            doc.append(Constants.NODEDEVICETYPEID, TagValues.getNodeValue(data, Constants.NODEDEVICETYPEID));
            doc.append(Constants.NODEADDRESS, TagValues.getNodeValue(data, Constants.NODEADDRESS).trim());

            // Added New Parameters
            String accuracy = TagValues.getNodeValue(data, Constants.NODEACCURACY);
            if (!checkNullAndEmpty(accuracy)) {
                accuracy = "0.0";
            }
            doc.append(Constants.NODEACCURACY, Double.parseDouble(accuracy.trim()));
//            String distance = TagValues.getNodeValue(data, Constants.NODEDISTANCE);
//            if(!checkNullAndEmpty(distance)){
//                distance = "0.0";
//            }
//            doc.append(Constants.NODEDISTANCE, Double.parseDouble(distance.trim()));
            try {
                String orgmillis = TagValues.getNodeValue(data, Constants.NODEORIGINTSMILLI);
                if (orgmillis.isEmpty()) {
                    orgmillis = "0";
                }
                doc.append(Constants.NODEORIGINTSMILLI, Long.valueOf(orgmillis));
            } catch (Exception e) {
            }
            doc.append(Constants.NODEUNLOCKSTATUS, TagValues.getNodeValue(data, Constants.NODEUNLOCKSTATUS));
//            doc.append(Constants.NODEDEVICESTATUS, TagValues.getNodeValue(data, Constants.NODEDEVICESTATUS));
//            doc.append(Constants.NODENAME, TagValues.getNodeValue(data, Constants.NODENAME));
//            doc.append(Constants.NODEEVENT, TagValues.getNodeValue(data, Constants.NODEEVENT));
//            doc.append(Constants.NODECLIENT, TagValues.getNodeValue(data, Constants.NODECLIENT));
            doc.append(Constants.NODEBATCHARGE, TagValues.getNodeValue(data, Constants.NODEBATCHARGE));
//            doc.append(Constants.NODEDEVICEIP, TagValues.getNodeValue(data, Constants.NODEDEVICEIP));
//            doc.append(Constants.NODEDEVICEPORT, TagValues.getNodeValue(data, Constants.NODEDEVICEPORT));
//            doc.append(Constants.NODESERVERIP, TagValues.getNodeValue(data, Constants.NODESERVERIP));
//            doc.append(Constants.NODESERVERPORT, TagValues.getNodeValue(data, Constants.NODESERVERPORT));
//            String client = TagValues.getNodeValue(request, Constants.NODECLIENT);
            MongoCollection trackdata = database.getCollection(client + "_TRACKDATA");
            trackdata.insertOne(doc);

            return true;
        } catch (Exception e) {
            error("DataInsertion........ insertTrackData Fail !!! " + e.getMessage(), e);
//            e.printStackTrace();
            return false;
        } finally {

        }
    }

    public boolean insertTrackData(MongoDatabase database, String client, JSONObject data) throws IOException, InterruptedException {

        Document doc = new Document();
        Boolean isInserted = false;
        try {
            Double lat = Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELATITUDE));
            Double lng = Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELONGITUDE));
            Double[] geo = new Double[]{lng, lat};
            doc.append(Constants.NODEGEOCODE, Arrays.asList(geo));
            doc.append(Constants.NODELATITUDE, lat);
            doc.append(Constants.NODELONGITUDE, lng);
            doc.append(Constants.NODEACCOUNTID, Long.parseLong(TagValues.getNodeValue(data, Constants.NODEACCOUNTID)));
            doc.append(Constants.NODEDEVICEID, Long.parseLong(TagValues.getNodeValue(data, Constants.NODEDEVICEID)));
            doc.append(Constants.NODEVEHICLENUMBER, TagValues.getNodeValue(data, Constants.NODEVEHICLENUMBER));
            doc.append(Constants.NODEIMEI, TagValues.getNodeValue(data, Constants.NODEIMEI));
            doc.append(Constants.NODEDISHA, TagValues.getNodeValue(data, Constants.NODEDISHA));
            doc.append(Constants.NODETAMPER, TagValues.getNodeValue(data, Constants.NODETAMPER));
            String speed = TagValues.getNodeValue(data, Constants.NODESPEED);
            if (!checkNullAndEmpty(speed)) {
                speed = "0.0";
            }
            doc.append(Constants.NODESPEED, Double.parseDouble(speed.trim()));
            String ignition = TagValues.getNodeValue(data, Constants.NODEIGNITION);
            if (ignition.isEmpty()) {
                ignition = "N";
            }
            doc.append(Constants.NODEIGNITION, ignition);
            String fuel = TagValues.getNodeValue(data, Constants.NODEFUEL);
            if (fuel.isEmpty()) {
                fuel = "0.00";
            } else {
                fuel = twoDecimalPlaces.format(Double.parseDouble(fuel));
            }
            doc.append(Constants.NODEFUEL, Double.parseDouble(fuel));
            String load = TagValues.getNodeValue(data, Constants.NODELOADS);
            if (load.isEmpty()) {
                load = "0.00";
            } else {
                load = twoDecimalPlaces.format(Double.parseDouble(load));
            }
            doc.append(Constants.NODELOADS, Double.parseDouble(load));
            doc.append(Constants.NODEGPS, TagValues.getNodeValue(data, Constants.NODEGPS));
            doc.append(Constants.NODESATELLITES, TagValues.getNodeValue(data, Constants.NODESATELLITES));
            doc.append(Constants.NODEORIGINTS, TagValues.getNodeValue(data, Constants.NODEORIGINTS));
            doc.append(Constants.NODECREATEDON, TagValues.getNodeValue(data, Constants.NODECREATEDON));
            doc.append(Constants.NODEDEVICETYPEID, TagValues.getNodeValue(data, Constants.NODEDEVICETYPEID));
            doc.append(Constants.NODEADDRESS, TagValues.getNodeValue(data, Constants.NODEADDRESS).trim());

            if (!TagValues.getNodeValue(data, Constants.NODEALTITUDE).isEmpty()) {
                doc.append(Constants.NODEALTITUDE, TagValues.getNodeValue(data, Constants.NODEALTITUDE));
            }
            if (!TagValues.getNodeValue(data, Constants.NODEIMMOBILISER).isEmpty()) {
                doc.append(Constants.NODEIMMOBILISER, TagValues.getNodeValue(data, Constants.NODEIMMOBILISER));
            }
            if (!TagValues.getNodeValue(data, Constants.NODEAC).isEmpty()) {
                doc.append(Constants.NODEAC, TagValues.getNodeValue(data, Constants.NODEAC));

            }
            if (!TagValues.getNodeValue(data, Constants.NODEDOOR).isEmpty()) {
                doc.append(Constants.NODEDOOR, TagValues.getNodeValue(data, Constants.NODEDOOR));

            }
            if (!TagValues.getNodeValue(data, Constants.NODEBATTERY).isEmpty()) {
                doc.append(Constants.NODEBATTERY, TagValues.getNodeValue(data, Constants.NODEBATTERY));

            }
            if (!TagValues.getNodeValue(data, Constants.NODELOCK).isEmpty()) {
                doc.append(Constants.NODELOCK, TagValues.getNodeValue(data, Constants.NODELOCK));

            }
            if (!TagValues.getNodeValue(data, Constants.NODERFID).isEmpty()) {
                doc.append(Constants.NODERFID, TagValues.getNodeValue(data, Constants.NODERFID));

            }
            if (!TagValues.getNodeValue(data, Constants.NODEPOWSTATUS).isEmpty()) {
                doc.append(Constants.NODEPOWSTATUS, TagValues.getNodeValue(data, Constants.NODEPOWSTATUS));

            }
            if (!TagValues.getNodeValue(data, Constants.NODEREASON).isEmpty()) {
                doc.append(Constants.NODEREASON, TagValues.getNodeValue(data, Constants.NODEREASON));

            }
            if (!TagValues.getNodeValue(data, Constants.NODEPACKETTYPE).isEmpty()) {
                doc.append(Constants.NODEPACKETTYPE, TagValues.getNodeValue(data, Constants.NODEPACKETTYPE));

            }
            if (!TagValues.getNodeValue(data, Constants.NODEPACKETTYPE).isEmpty()) {
                doc.append(Constants.NODEEVENTSOURCETYPE, TagValues.getNodeValue(data, Constants.NODEEVENTSOURCETYPE));
            }
            MongoCollection trackdata = database.getCollection(client + "_TRACKDATA");
            if (!TagValues.getNodeValue(data, Constants.NODEGPS).equals("0")) {
                trackdata.insertOne(doc);
                isInserted = true;
            }
        } catch (Exception e) {
            error("DataInsertion........ insertTrackData-2 Fail !!! " + e.getMessage(), e);
        }
        return isInserted;

    }

    public void insertRawData(MongoDatabase database, String client, String data) throws IOException, InterruptedException {
        Document doc = new Document();

        try {
            doc.append(Constants.NODEDATA, TagValues.getNodeValue(data, Constants.NODEDATA));
            doc.append(Constants.NODECREATEDON, TagValues.getNodeValue(data, Constants.NODECREATEDON));
            doc.append(Constants.NODEACCOUNTID, Long.parseLong(TagValues.getNodeValue(data, Constants.NODEACCOUNTID)));
            doc.append(Constants.NODEDEVICEID, Long.parseLong(TagValues.getNodeValue(data, Constants.NODEDEVICEID)));
            doc.append(Constants.NODEDEVICETYPEID, TagValues.getNodeValue(data, Constants.NODEDEVICETYPEID));

            MongoCollection trackdata = database.getCollection(client + "_RAWDATA");
            trackdata.insertOne(doc);

            //System.out.println("Data Inserted");
        } catch (Exception e) {
            error("DataInsertion........ insertRawData Fail !!! " + e.getMessage(), e);
        } finally {

        }
    }


    public void insertRawData(MongoDatabase database, String client, String data, String deviceDtl) throws IOException, InterruptedException {
        Document doc = new Document();

        try {

            doc.append(Constants.NODEDATA, TagValues.getNodeValue(data, Constants.NODEDATA));

            try {
                String cts = TagValues.getNodeValue(deviceDtl, Constants.NODECREATEDON);
                if (cts.trim().isEmpty()) {
                    cts = TagValues.getNodeValue(data, Constants.NODECREATEDON);
                }
                doc.append(Constants.NODECREATEDON, cts);
            } catch (Exception e) {

            }

            try {
                String acc = TagValues.getNodeValue(deviceDtl, Constants.NODEACCOUNTID);
                if (acc.trim().isEmpty()) {
                    acc = TagValues.getNodeValue(data, Constants.NODEACCOUNTID);
                }
                doc.append(Constants.NODEACCOUNTID, Long.parseLong(acc));
            } catch (NumberFormatException e) {

            }

            try {
                String device = TagValues.getNodeValue(deviceDtl, Constants.NODEDEVICEID);
                if (device.trim().isEmpty()) {
                    device = TagValues.getNodeValue(data, Constants.NODEDEVICEID);
                }
                doc.append(Constants.NODEDEVICEID, Long.parseLong(device));
            } catch (NumberFormatException e) {

            }

            try {
                String typeId = TagValues.getNodeValue(deviceDtl, Constants.NODEDEVICETYPEID);
                if (typeId.trim().isEmpty()) {
                    typeId = TagValues.getNodeValue(data, Constants.NODEDEVICETYPEID);
                }
                doc.append(Constants.NODEDEVICETYPEID, Long.parseLong(typeId));
            } catch (NumberFormatException e) {

            }

            try {
                String imei = TagValues.getNodeValue(deviceDtl, Constants.NODEIMEI);
                if (imei.trim().isEmpty()) {
                    imei = TagValues.getNodeValue(data, Constants.NODEIMEI);
                }
                doc.append(Constants.NODEIMEI, imei);
            } catch (Exception e) {

            }

            try {
                String vehnum = TagValues.getNodeValue(deviceDtl, Constants.NODEVEHICLENUMBER);
                if (vehnum.trim().isEmpty()) {
                    vehnum = TagValues.getNodeValue(data, Constants.NODEVEHICLENUMBER);
                }
                doc.append(Constants.NODEVEHICLENUMBER, vehnum);
            } catch (Exception e) {

            }

            try {
                String name = TagValues.getNodeValue(deviceDtl, Constants.NODENAME);
                if (name.trim().isEmpty()) {
                    name = TagValues.getNodeValue(data, Constants.NODENAME);
                }
                doc.append(Constants.NODENAME, name);
            } catch (Exception e) {

            }

            debug("RawData - DeviceData " + deviceDtl + ", Document" + doc.toJson());
//            System.out.println("RawData :: " + data + ", DeviceData " + deviceDtl + ", Document" + doc.toJson());
            MongoCollection trackdata = database.getCollection(client + "_RAWDATA");
            trackdata.insertOne(doc);

            //System.out.println("Data Inserted");
        } catch (Exception e) {
            error("DataInsertion........ insertRawData 2 Fail !!! " + e.getMessage(), e);
//            System.out.println("insertRawData/4 Raw Data Insertion Fail.........." + e.getMessage());
        } finally {

        }
    }

    public void insertLockEventsData(MongoDatabase database, String client, String data) throws IOException, InterruptedException {
        Document doc = new Document();
        try {
            Double lat = Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELATITUDE));
            Double lng = Double.parseDouble(TagValues.getNodeValue(data, Constants.NODELONGITUDE));
            Double[] geo = new Double[]{lng, lat};
            doc.append(Constants.NODEGEOCODE, Arrays.asList(geo));
            doc.append(Constants.NODELATITUDE, lat);
            doc.append(Constants.NODELONGITUDE, lng);
            doc.append(Constants.NODEEVENTSOURCETYPE, TagValues.getNodeValue(data, Constants.NODEEVENTSOURCETYPE));
            doc.append(Constants.NODEIMEI, TagValues.getNodeValue(data, Constants.NODEIMEI));
            doc.append(Constants.NODERFID, TagValues.getNodeValue(data, Constants.NODERFID));
            doc.append(Constants.NODEACCOUNTID, Long.parseLong(TagValues.getNodeValue(data, Constants.NODEACCOUNTID)));
            doc.append(Constants.NODEDEVICEID, Long.parseLong(TagValues.getNodeValue(data, Constants.NODEDEVICEID)));
            doc.append(Constants.NODEVEHICLENUMBER, TagValues.getNodeValue(data, Constants.NODEVEHICLENUMBER));
            doc.append(Constants.NODEADDRESS, TagValues.getNodeValue(data, Constants.NODEADDRESS).trim());
            //doc.append(Constants.NODEDISHA, TagValues.getNodeValue(data, Constants.NODEDISHA));
            //doc.append(Constants.NODESPEED, TagValues.getNodeValue(data, Constants.NODESPEED));
            doc.append(Constants.NODELOCK, TagValues.getNodeValue(data, Constants.NODELOCK));
            doc.append(Constants.NODEPASSWORDSTATUS, TagValues.getNodeValue(data, Constants.NODEPASSWORDSTATUS));
            doc.append(Constants.NODEPIN, TagValues.getNodeValue(data, Constants.NODEPIN));
            doc.append(Constants.NODEGPS, TagValues.getNodeValue(data, Constants.NODEGPS));
            doc.append(Constants.NODEORIGINTS, TagValues.getNodeValue(data, Constants.NODEORIGINTS));
            doc.append(Constants.NODECREATEDON, TagValues.getNodeValue(data, Constants.NODECREATEDON));
            doc.append(Constants.NODECLIENT, client);
            doc.append(Constants.NODEDEVICETYPEID, TagValues.getNodeValue(data, Constants.NODEDEVICETYPEID));

            MongoCollection trackdata = database.getCollection(client.toUpperCase() + "_LOCKUNLOCK");
            trackdata.insertOne(doc);

            //System.out.println("Data Inserted");
        } catch (Exception e) {
            error("DataInsertion........ insertLockEventsData Fail !!! " + e.getMessage(), e);
        } finally {

        }
    }

    public void insertDataPushResp(MongoDatabase database, Document doc, String col) {
        try {
            MongoCollection trackdata = database.getCollection(col);
            trackdata.insertOne(doc);
        } catch (Exception ee) {
            error("insertDataPushResp......" + ee.getMessage());
        } finally {

        }
    }


    public void insertRespData(MongoDatabase database, String client,
                               String respData, List<String> respRecordList,
                               String deviceDtl) throws IOException, InterruptedException {
        Document doc = new Document();

        try {
//            info("Test RESPDATA : " + respRecordList.size() + "\n DeviceData : " + deviceDtl);

            if (null != respRecordList && respRecordList.size() > 0) {

                try {
                    doc.append(Constants.NODEACCOUNTID, Long.parseLong(TagValues.getNodeValue(deviceDtl, Constants.NODEACCOUNTID)));
                } catch (NumberFormatException e) {

                }
                doc.append(Constants.NODERESPONSETYPE, TagValues.getNodeValue(respData, Constants.NODERESPONSETYPE));
                try {
                    doc.append(Constants.NODEDEVICEID, Long.parseLong(TagValues.getNodeValue(deviceDtl, Constants.NODEDEVICEID)));
                } catch (NumberFormatException e) {

                }
                try {
                    doc.append(Constants.NODEDEVICETYPEID, Long.parseLong(TagValues.getNodeValue(deviceDtl, Constants.NODEDEVICETYPEID)));
                } catch (NumberFormatException e) {

                }
                doc.append(Constants.NODEIMEI, TagValues.getNodeValue(deviceDtl, Constants.NODEIMEI));
                doc.append(Constants.NODENAME, TagValues.getNodeValue(deviceDtl, Constants.NODENAME));
//                doc.append(Constants.NODECLIENT, TagValues.getNodeValue(deviceDtl, Constants.NODECLIENT));
                doc.append(Constants.NODEVEHICLENUMBER, TagValues.getNodeValue(deviceDtl, Constants.NODEVEHICLENUMBER));
                doc.append(Constants.NODECREATEDON, TagValues.getNodeValue(deviceDtl, Constants.NODECREATEDON));

                List<Document> records = new ArrayList<>(respRecordList.size());
                for (String data : respRecordList) {
//                    info("Records Data :: " + data);
                    Document dataDoc = new Document();
                    String type = TagValues.getNodeValue(data, Constants.NODETYPE);
                    dataDoc.append(Constants.NODETYPE, TagValues.getNodeValue(data, Constants.NODETYPE));
                    if (!type.trim().isEmpty()) {
                        String commandCode = getAppConfig().getRespTypeMap().get(type);
                        if (commandCode == null || commandCode.trim().isEmpty()) {
                            commandCode = type;
                        }
                        dataDoc.append(Constants.NODECOMMANDCODE, commandCode);
                    }
                    dataDoc.append(Constants.NODEVALUE, TagValues.getNodeValue(data, Constants.NODEVALUE));
                    dataDoc.append(Constants.NODEMISC, TagValues.getNodeValue(data, Constants.NODEMISC));
                    dataDoc.append(Constants.NODERESULTCODE, TagValues.getNodeValue(data, Constants.NODERESULTCODE));
                    dataDoc.append(Constants.NODERESULTDESC, TagValues.getNodeValue(data, Constants.NODERESULTDESC));

                    records.add(dataDoc);
                }
                doc.append(Constants.NODERECORDS, records);

            }
            debug("RESP Records : " + respRecordList.size() + "Document RespData :: " + doc.toJson());
//            System.out.println("RESP Records : " + respRecordList.size() + "\n DeviceData : " + deviceDtl +
//                    "Document RespData :: " + doc.toJson());
            MongoCollection trackdata = database.getCollection(client + "_RESPDATA");
            trackdata.insertOne(doc);

            //System.out.println("Data Inserted");
        } catch (Exception e) {
            error("DataInsertion........ insertRespData Fail !!! " + e.getMessage(), e);
//            System.out.println("insertRawData/4 Raw Data Insertion Fail.........." + e.getMessage());
        } finally {

        }
    }

    public void insertAlertData(MongoDatabase database, String client,
                                String alertData, List<String> alertRecordList,
                                String deviceDtl, String liveData) throws IOException, InterruptedException {
        Document doc = new Document();

        try {
//            info("Test RESPDATA : " + respRecordList.size() + "\n DeviceData : " + deviceDtl);

            if (null != alertRecordList && alertRecordList.size() > 0) {

                if (null != liveData && !liveData.trim().isEmpty()) {
                    Double lat = Double.parseDouble(TagValues.getNodeValue(liveData, Constants.NODELATITUDE));
                    Double lng = Double.parseDouble(TagValues.getNodeValue(liveData, Constants.NODELONGITUDE));
                    Double[] geo = new Double[]{lng, lat};
                    doc.append(Constants.NODEGEOCODE, Arrays.asList(geo));
                    doc.append(Constants.NODELATITUDE, lat);
                    doc.append(Constants.NODELONGITUDE, lng);
                    doc.append(Constants.NODEALTITUDE, TagValues.getNodeValue(liveData, Constants.NODEALTITUDE));
                    doc.append(Constants.NODEGPS, TagValues.getNodeValue(liveData, Constants.NODEGPS));
                    doc.append(Constants.NODEADDRESS, TagValues.getNodeValue(liveData, Constants.NODEADDRESS).trim());
                    try {
                        String orgmillis = TagValues.getNodeValue(liveData, Constants.NODEORIGINTSMILLI);
                        if (orgmillis.isEmpty()) {
                            orgmillis = "0";
                        }
                        doc.append(Constants.NODEORIGINTSMILLI, Long.valueOf(orgmillis));
                    } catch (Exception e) {
                    }
                }

                try {
                    doc.append(Constants.NODEACCOUNTID, Long.parseLong(TagValues.getNodeValue(deviceDtl, Constants.NODEACCOUNTID)));
                } catch (NumberFormatException e) {

                }
                try {
                    doc.append(Constants.NODEDEVICEID, Long.parseLong(TagValues.getNodeValue(deviceDtl, Constants.NODEDEVICEID)));
                } catch (NumberFormatException e) {

                }
                try {
                    doc.append(Constants.NODEDEVICETYPEID, Long.parseLong(TagValues.getNodeValue(deviceDtl, Constants.NODEDEVICETYPEID)));
                } catch (NumberFormatException e) {

                }
                doc.append(Constants.NODEIMEI, TagValues.getNodeValue(deviceDtl, Constants.NODEIMEI));
                doc.append(Constants.NODENAME, TagValues.getNodeValue(deviceDtl, Constants.NODENAME));
//                doc.append(Constants.NODECLIENT, TagValues.getNodeValue(deviceDtl, Constants.NODECLIENT));
                doc.append(Constants.NODEVEHICLENUMBER, TagValues.getNodeValue(deviceDtl, Constants.NODEVEHICLENUMBER));
                doc.append(Constants.NODECREATEDON, TagValues.getNodeValue(deviceDtl, Constants.NODECREATEDON));

                for (String data : alertRecordList) {

                    try {
                        doc.remove(Constants.NODETYPE);
                    } catch (Exception e) {
//                        error(Constants.NODETYPE + "......... !!!", e);
                    }
                    try {
                        doc.remove(Constants.NODEVALUE);
                    } catch (Exception e) {
//                        error(Constants.NODEVALUE + "......... !!!", e);
                    }
                    try {
                        doc.remove(Constants.NODEEVENTSOURCETYPE);
                    } catch (Exception e) {
//                        error(Constants.NODEEVENTSOURCETYPE + "......... !!!", e);
                    }
                    try {
                        doc.remove(Constants.NODERFID);
                    } catch (Exception e) {
//                        error(Constants.NODERFID + "......... !!!", e);
                    }
                    try {
                        doc.remove(Constants.NODEPIN);
                    } catch (Exception e) {
//                        error(Constants.NODEPIN + "......... !!!", e);
                    }
                    try {
                        doc.remove(Constants.NODECOUNT);
                    } catch (Exception e) {
//                        error(Constants.NODECOUNT + "......... !!!", e);
                    }
                    try {
                        doc.remove(Constants.NODEPASSWORDSTATUS);
                    } catch (Exception e) {
//                        error(Constants.NODEPASSWORDSTATUS + "......... !!!", e);
                    }

                    doc.append(Constants.NODETYPE, TagValues.getNodeValue(data, Constants.NODETYPE));
                    doc.append(Constants.NODEVALUE, TagValues.getNodeValue(data, Constants.NODEVALUE));
                    doc.append(Constants.NODEEVENTSOURCETYPE, TagValues.getNodeValue(data, Constants.NODEEVENTSOURCETYPE));
                    doc.append(Constants.NODERFID, TagValues.getNodeValue(data, Constants.NODERFID));
                    doc.append(Constants.NODEPIN, TagValues.getNodeValue(data, Constants.NODEPIN));
                    doc.append(Constants.NODECOUNT, TagValues.getNodeValue(data, Constants.NODECOUNT));
                    doc.append(Constants.NODEPASSWORDSTATUS, TagValues.getNodeValue(data, Constants.NODEPASSWORDSTATUS));

//                    records.add(dataDoc);
                    info("ALERT Records : " + alertRecordList.size() + "Document AlertData :: " + doc.toJson());
                    MongoCollection trackdata = database.getCollection(client + "_ALERTDATA");
                    trackdata.insertOne(doc);

                }
//                doc.append(Constants.NODERECORDS, records);

            }
        } catch (Exception e) {
            error("DataInsertion........ insertAlertData Fail !!! " + e.getMessage(), e);
//            System.out.println("insertRawData/4 Raw Data Insertion Fail.........." + e.getMessage());
        } finally {

        }
    }

    @Deprecated
    public void insertAlertDataMultiRecords(MongoDatabase database, String client,
                                            String alertData, List<String> alertRecordList,
                                            String deviceDtl, String liveData) throws IOException, InterruptedException {
        Document doc = new Document();

        try {
//            info("Test RESPDATA : " + respRecordList.size() + "\n DeviceData : " + deviceDtl);

            if (null != alertRecordList && alertRecordList.size() > 0) {

                if (null != liveData && !liveData.trim().isEmpty()) {
                    Double lat = Double.parseDouble(TagValues.getNodeValue(liveData, Constants.NODELATITUDE));
                    Double lng = Double.parseDouble(TagValues.getNodeValue(liveData, Constants.NODELONGITUDE));
                    Double[] geo = new Double[]{lng, lat};
                    doc.append(Constants.NODEGEOCODE, Arrays.asList(geo));
                    doc.append(Constants.NODELATITUDE, lat);
                    doc.append(Constants.NODELONGITUDE, lng);
                    doc.append(Constants.NODEALTITUDE, TagValues.getNodeValue(liveData, Constants.NODEALTITUDE));
                    doc.append(Constants.NODEGPS, TagValues.getNodeValue(liveData, Constants.NODEGPS));
                    doc.append(Constants.NODEADDRESS, TagValues.getNodeValue(liveData, Constants.NODEADDRESS).trim());
                }

                try {
                    doc.append(Constants.NODEACCOUNTID, Long.parseLong(TagValues.getNodeValue(deviceDtl, Constants.NODEACCOUNTID)));
                } catch (NumberFormatException e) {

                }
                try {
                    doc.append(Constants.NODEDEVICEID, Long.parseLong(TagValues.getNodeValue(deviceDtl, Constants.NODEDEVICEID)));
                } catch (NumberFormatException e) {

                }
                try {
                    doc.append(Constants.NODEDEVICETYPEID, Long.parseLong(TagValues.getNodeValue(deviceDtl, Constants.NODEDEVICETYPEID)));
                } catch (NumberFormatException e) {

                }
                doc.append(Constants.NODEIMEI, TagValues.getNodeValue(deviceDtl, Constants.NODEIMEI));
                doc.append(Constants.NODENAME, TagValues.getNodeValue(deviceDtl, Constants.NODENAME));
//                doc.append(Constants.NODECLIENT, TagValues.getNodeValue(deviceDtl, Constants.NODECLIENT));
                doc.append(Constants.NODEVEHICLENUMBER, TagValues.getNodeValue(deviceDtl, Constants.NODEVEHICLENUMBER));
                doc.append(Constants.NODECREATEDON, TagValues.getNodeValue(deviceDtl, Constants.NODECREATEDON));

                List<Document> records = new ArrayList<>(alertRecordList.size());
                for (String data : alertRecordList) {
//                    info("Records Data :: " + data);
                    Document dataDoc = new Document();
//                    String type = TagValues.getNodeValue(data, Constants.NODETYPE);
                    dataDoc.append(Constants.NODETYPE, TagValues.getNodeValue(data, Constants.NODETYPE));
                    dataDoc.append(Constants.NODEVALUE, TagValues.getNodeValue(data, Constants.NODEVALUE));
                    dataDoc.append(Constants.NODEEVENTSOURCETYPE, TagValues.getNodeValue(data, Constants.NODEEVENTSOURCETYPE));
                    dataDoc.append(Constants.NODERFID, TagValues.getNodeValue(data, Constants.NODERFID));
                    dataDoc.append(Constants.NODEPIN, TagValues.getNodeValue(data, Constants.NODEPIN));
                    dataDoc.append(Constants.NODECOUNT, TagValues.getNodeValue(data, Constants.NODECOUNT));
                    dataDoc.append(Constants.NODEPASSWORDSTATUS, TagValues.getNodeValue(data, Constants.NODEPASSWORDSTATUS));

                    records.add(dataDoc);
                }
                doc.append(Constants.NODERECORDS, records);

            }
            info("ALERT Records : " + alertRecordList.size() + "Document AlertData :: " + doc.toJson());
//            System.out.println("RESP Records : " + respRecordList.size() + "\n DeviceData : " + deviceDtl +
//                    "Document RespData :: " + doc.toJson());
            MongoCollection trackdata = database.getCollection(client + "_ALERTDATA");
            trackdata.insertOne(doc);

            //System.out.println("Data Inserted");
        } catch (Exception e) {
            error("DataInsertion........ insertAlertData Fail !!! " + e.getMessage(), e);
//            System.out.println("insertRawData/4 Raw Data Insertion Fail.........." + e.getMessage());
        } finally {

        }
    }


}
