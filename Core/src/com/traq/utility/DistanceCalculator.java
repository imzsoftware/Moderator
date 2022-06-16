package com.traq.utility;

/**
 * Created by Amit Kamboj on 13/7/16.
 */
public class DistanceCalculator {



    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }else{
            dist = dist * 1.609344;
        }

        return (dist);
    }


    public static long distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344; // distance in kilometers
        dist = dist * 1000;     // convert in meters

        long roundedDist = Math.round(dist);
        return (roundedDist);
    }


    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


    public static void main(String[] args) throws Exception {
        // DELHI 28.6139391, 77.2090212
        // KARNAL 29.6856929, 76.9904825

        //System.out.println(distance(28.6139391, 77.2090212, 29.6856929, 76.9904825, "M") + " Miles\n");
        System.out.println(distance(28.6327426, 77.2195969, 29.0715883, 75.4898156) + " Kilometers\n");
        //System.out.println(distance(28.6139391, 77.2090212, 29.6856929, 76.9904825, "N") + " Nautical Miles\n");
    }

}
