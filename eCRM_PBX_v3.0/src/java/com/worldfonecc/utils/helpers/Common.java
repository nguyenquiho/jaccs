/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfonecc.utils.helpers;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author MitsuyoRai
 */
public class Common {

    public static HashMap<String, String> JSONToHashMap(String jsonString) throws JSONException {
        JSONObject jdata = new JSONObject(jsonString);
        Iterator<String> nameItr = jdata.keys();
        HashMap<String, String> outMap = new HashMap<>();
        while (nameItr.hasNext()) {
            String name = nameItr.next();
            outMap.put(name, jdata.getString(name));
        }
        return outMap;
    }

    public static ArrayList<String> JSONToArrayList(String jsonString) throws JSONException {
        ArrayList<String> outList = new ArrayList<>();
        JSONArray jArray = new JSONArray(jsonString);
        for (int i = 0; i < jArray.length(); i++) {
            outList.add(jArray.getString(i));
        }
        return outList;
    }

    public static ArrayList<HashMap> JSONToArrayListHashMap(String jsonString) throws JSONException {
        ArrayList<HashMap> outList = new ArrayList<>();
        JSONArray jArray = new JSONArray(jsonString);
        for (int i = 0; i < jArray.length(); i++) {
            outList.add(JSONToHashMap(jArray.getJSONObject(i).toString()));
        }
        return outList;
    }

    public static String diffTime(SimpleDateFormat sdf, String datetime1, String datetime2) {
        Date dtime1, dtime2;
        try {
            dtime1 = sdf.parse(datetime1);
            dtime2 = sdf.parse(datetime2);
            return String.valueOf((dtime1.getTime() - dtime2.getTime()) / 1000);
        } catch (ParseException | NullPointerException ex) {
            System.out.println(ex.getMessage());
        }
        return "0";
    }

    public static String getIpAddress() {
        String ip = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    ip = address.getHostAddress();
                    if (Pattern.matches("^10.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}$", ip)) {
                        return ip;
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }
}
