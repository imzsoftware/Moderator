package com.traq.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Amit on 26/7/16.
 */
public class Test {


    public static void main(String[] args) {
        String value = "17-10-2020";
        String toDt = "30-10-2020";
        SimpleDateFormat reqPattern = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Date fromDate = reqPattern.parse(value);
            Date toDate = reqPattern.parse(toDt);
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            String day = cal.get(Calendar.DAY_OF_MONTH) + "";
            int month = cal.get(Calendar.MONTH);
//            month++;
            String monthStr = "";
            if (month < 10)
                monthStr = "0" + month;
            else
                monthStr = "" + month;

            Date tempDate = cal.getTime();
            System.out.println(tempDate.toString());
            System.out.println(reqPattern.format(tempDate));

            String year = cal.get(Calendar.YEAR) + "";
            String deviceFromDate = day + monthStr + year + "";


            System.out.println(deviceFromDate);
            System.out.printf(value.replaceAll("-", ""));
        } catch (ParseException e) {
            e.printStackTrace();
        }

//        Test tt = new Test();
//        List<String> list = new ArrayList<String>();
//        //list.add("Test");
//        System.out.println("List1="+list);
//        tt.testPost(list);
//        System.out.println(list);
    }

    public String testPost(List<String> stringList) {
        stringList.add("Kamboj");
        stringList.add("Amit");

        return "";
    }

    private static final String ENC_TYPE = "SHA-512";
    /** paramstr = <mechantId>|<uniqueSalt>|<password>
     Example if merchantId = 16, uniqueSalt=QWIO9345, password=agent123
     then paramstr = “16| QWIO9345|agent123”
     and auth = hashCal (paramstr);
     **/


}
