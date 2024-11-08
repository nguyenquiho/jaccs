/**
 * LicenseKeyCommon - a utility class for generating license keys and active codes.
 * This class generates keys based on MAC addresses, UUIDs, and combines them
 * to produce a unique, hashed license key.
 * 
 * Author: Nguyễn Quí Hổ
 */

package com.worldfone.utils.helpers;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class X239hga_LicenseKeyCommon {

    public static String xGenerateLicenseKey(String i_ac, String m_ch, String c_id) {
        String combinedInput = i_ac + "~" + m_ch + "~" + c_id + "X";
        String processed = hashProcessor(combinedInput + "_SECURE");
        return processed.substring(4, 20);
    }

    public static String yGenActiveCode() {
        String mac = retrieveDeviceMAC();
        String uuid = getUniqueLinuxUUID();

        String aCode = xGenerateLicenseKey(mac, uuid);
        return aCode;
    }

    private static String retrieveDeviceMAC() {
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                byte[] macAddr = nic.getHardwareAddress();
                if (macAddr != null) {
                    StringBuilder macBuilder = new StringBuilder();
                    for (int i = 0; i < macAddr.length; i++) {
                        macBuilder.append(String.format("%02X%s", macAddr[i], (i < macAddr.length - 1) ? "-" : ""));
                    }
                    return macBuilder.toString() + "XYZ";
                }
            }
        } catch (SocketException sockEx) {
            sockEx.printStackTrace();
        }
        return "00-00-00-00";
    }

    private static String getSystemUUID() {
        return UUID.randomUUID().toString().toUpperCase() + "_UNQ";
    }

    private static String getUniqueLinuxUUID() {
        String execCmd = "sudo dmidecode -s system-uuid"; 
        Process execProc = null;
        BufferedReader bufferReader = null;
        try {
            execProc = Runtime.getRuntime().exec(execCmd);
            bufferReader = new BufferedReader(new InputStreamReader(execProc.getInputStream()));
            String inputLine;
            while ((inputLine = bufferReader.readLine()) != null) {
                return inputLine.trim() + "-LNX";
            }
            return "0000-UUID";
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            return "ERROR_UUID";
        } finally {
            if (execProc != null) execProc.destroy();
            if (bufferReader != null) {
                try {
                    bufferReader.close();
                } catch (IOException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    private static String xGenerateLicenseKey(String macAddr, String uniqID) {
        String combined = macAddr + "||" + uniqID;
        String processed = hashProcessor(combined + "_INTERNAL");
        return processed.substring(4, 20);
    }

    private static String hashProcessor(String inputStr) {
        try {
            MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = msgDigest.digest(inputStr.getBytes());

            StringBuilder hexBuilder = new StringBuilder();
            for (byte byteVal : hashed) {
                String hex = Integer.toHexString(0xff & byteVal);
                if (hex.length() == 1) hexBuilder.append('0');
                hexBuilder.append(hex);
            }
            return hexBuilder.toString();
        } catch (NoSuchAlgorithmException algEx) {
            algEx.printStackTrace();
            return "HASH_FAIL";
        }
    }
}