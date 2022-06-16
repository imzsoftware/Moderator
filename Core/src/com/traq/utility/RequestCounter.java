package com.traq.utility;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Amit Kamboj on 3/8/16.
 */
public class RequestCounter {
    public static long DAILY_COUNT;
    public static long HOURLY_COUNT;

    public static long getDAILY_COUNT() {
        return DAILY_COUNT;
    }

    public static void setDAILY_COUNT(long DAILY_COUNT) {
        RequestCounter.DAILY_COUNT = DAILY_COUNT;
    }

    public static long getHOURLY_COUNT() {
        return HOURLY_COUNT;
    }

    public static void setHOURLY_COUNT(long HOURLY_COUNT) {
        RequestCounter.HOURLY_COUNT = HOURLY_COUNT;
    }

    public static void resetHourlyCounter(){
        HOURLY_COUNT=0;
    }

    public static void resetDailyCounter(){
        DAILY_COUNT=0;
    }



    public static void writeFile(boolean isDaily,long hourly, long daily){
        FileWriter f2 = null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
        String today = sdf1.format(new Date());
        try{
            f2 = new FileWriter("/home/tomcat/distancebetween/AppProblemLogs/hourlyCount_"+today+".txt", true);
            f2.write(sdf.format(new Date()) +"\t"+hourly +"\n");
            System.out.println("writeFile() HOURLY_COUNT="+hourly);
            if(isDaily){
                f2.write(today +"\t"+daily +"\n");
            }
            Thread.sleep(1);
        }catch (Exception ex){
            f2 = null;
        }finally{
            try{
                f2.close();
                f2= null;
            }catch(Exception ex){
                f2 = null;
            }
        }


    }

    public void flushCounter(boolean isBoth, long hourly, long daily){
        if(!isBoth){
            System.out.println("HOURLY_COUNT="+HOURLY_COUNT);
            writeFile(isBoth, hourly, daily);
            resetHourlyCounter();
        }else{
            writeFile(isBoth, hourly, daily);
            resetHourlyCounter();
            resetDailyCounter();
        }
    }

    public static void main(String args[]){
        args=new String[]{"0","1"};
        if(Integer.parseInt(args[0]) == 0){
            System.out.println("HOURLY_COUNT="+HOURLY_COUNT);
            writeFile(false,0,0);
            //resetHourlyCounter();
        }else{
            writeFile(true,0,0);
            resetHourlyCounter();
            resetDailyCounter();
        }
        System.exit(0);
    }

}
