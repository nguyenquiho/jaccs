/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfone.utils.helpers;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Enumeration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class LKCommon {

    public static String generateLicenseKey(String active_code, String max_channel, String customer_id) {
        String combinedString = active_code + max_channel + customer_id;
        String hashedString = hashString(combinedString);
        return hashedString.substring(0, 16);
    }
        
    public static String generateActiveCode() {
        String macAddress = getMACAddress();
        String uuid = getLinuxUUID();

        String activeCode = generateLicenseKey(macAddress, uuid);
        return activeCode;
    }
    
    private static String getMACAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        stringBuilder.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    return stringBuilder.toString();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getUUID() {
        return UUID.randomUUID().toString();
    }

    private static String getLinuxUUID() {
        String command = "sudo dmidecode -s system-uuid"; 
        Process process = null;
        BufferedReader reader = null;
        try {
            process = Runtime.getRuntime().exec(command);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                return line.trim();
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String generateLicenseKey(String macAddress, String uuid) {
        String combinedString = macAddress + uuid;
        String hashedString = hashString(combinedString);
        return hashedString.substring(0, 16);
    }

    private static String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}