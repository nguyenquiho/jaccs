/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfone.utils.libraries;

/**
 *
 * @author MitsuyoRai
 */
public final class ConvertDateToText {

    public static String DateToText(String date) {
        String[] date_split = date.split("-");
        //return "ngay" + date_split[2] + "&thang" + date_split[1] + "&" + date_split[0];
        return "".concat("ngay").concat(date_split[2])
                .concat("&thang").concat(date_split[1])
                .concat("&").concat(date_split[0]);
    }

    public static String DateToText(String date, String refix) {
        String[] date_split = date.split("-");

        return "".concat(refix).concat("ngay").concat(date_split[2])
                .concat("&").concat(refix).concat("thang").concat(date_split[1])
                .concat("&").concat(refix).concat(date_split[0]);
    }

    public static String DateToText(Integer month) {
        if (month < 10) {
            return "".concat("thang0").concat(month.toString());
        } else {
            return "".concat("thang").concat(month.toString());
        }
    }

    public static String DateToText(Integer month, String refix) {
        if (month < 10) {
            return "".concat(refix).concat("thang0").concat(month.toString());
        } else {
            return "".concat(refix).concat("thang").concat(month.toString());
        }
    }

    public static String DateTimeToText(String datetime) {
        String[] datetime_split = datetime.split(" ");
        String date = datetime_split[0];
        String time = datetime_split[1];
        String[] date_split = date.split("-");
        String[] time_split = time.split(":");

        //return time_split[0] + "gio&" + time_split[1] + "phut&" + time_split[2] + "giay&ngay" + date_split[2] + "&thang" + date_split[1] + "&" + date_split[0];
        return "".concat(time_split[0]).concat("gio&")
                .concat(time_split[1]).concat("phut&")
                .concat(time_split[2]).concat("giay&")
                .concat("ngay").concat(date_split[2])
                .concat("&thang").concat(date_split[1])
                .concat("&").concat(date_split[0]);
    }

    public static String DateTimeToText(String datetime, String refix) {
        String[] datetime_split = datetime.split(" ");
        String date = datetime_split[0];
        String time = datetime_split[1];
        String[] date_split = date.split("-");
        String[] time_split = time.split(":");

        return "".concat(refix).concat(time_split[0]).concat("gio&")
                .concat(refix).concat(time_split[1]).concat("phut&")
                .concat(refix).concat(time_split[2]).concat("giay&")
                .concat(refix).concat("ngay").concat(date_split[2])
                .concat("&").concat(refix).concat("thang").concat(date_split[1])
                .concat("&").concat(refix).concat(date_split[0]);
    }

}
