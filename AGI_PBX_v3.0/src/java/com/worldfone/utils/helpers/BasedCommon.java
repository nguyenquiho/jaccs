/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfone.utils.helpers;

import com.worldfone.utils.libraries.ConvertDateToText;
import com.worldfone.utils.libraries.ConvertNumToText;
import com.worldfone.utils.libraries.ConvertTextToText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author MitsuyoRai
 */
public final class BasedCommon {

    public static int maxChannels() {
//        try {
//            return Integer.parseInt(System.getenv("PBX_EXTERNALCRM_MAX_CHANNELS"));
//        } catch (NumberFormatException ex) {
//            System.out.println("PBX_EXTERNALCRM_MAX_CHANNELS FAILED: " + ex.getMessage());
//            return 500;
//        }
        return 3000;
    }

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

    public static boolean isJSONValid(String test) {
        try {
            JSONObject jsonObject = new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                JSONArray jsonArray = new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public static String text2speak(JSONArray context, JSONObject content, String path, ArrayList<HashMap> fields) throws JSONException {
        String speek_context = "";
        for (int i = 0; i < context.length(); i++) {
            if (context.getJSONObject(i).has("audio")) {
                speek_context = speek_context.concat("&/etc/asterisk/Customer/audio/vi/").concat(context.getJSONObject(i).getString("audio").trim()).concat("&");
            } else if (context.getJSONObject(i).has("field")) {
                String value = "", type = "";
                for (int k = 0; k < fields.size(); k++) {
                    HashMap<String, String> map = fields.get(k);
                    if (map.getOrDefault("columns", "").equals(context.getJSONObject(i).getString("field").trim())) {
                        value = content.getString(map.get("columns"));
                        type = map.get("type");
                        break;
                    }
                }
                String speek = "";
                switch (type) {
                    case "date":
                        speek = ConvertDateToText.DateToText(value, path);
                        break;
                    case "datetime":
                        speek = ConvertDateToText.DateTimeToText(value, path);
                        break;
                    case "text":
                        speek = ConvertTextToText.TextToText(value.toLowerCase(Locale.ENGLISH), path);
                        break;
                    case "money":
                        speek = ConvertNumToText.num2String(Long.parseLong(value), path).concat("&").concat(path).concat("dong");
                        break;
                    case "number":
                        speek = ConvertNumToText.num2String(Long.parseLong(value), path);
                        break;
                    case "voice":
                        speek = "".concat(path).concat(value);
                        break;
                }
                speek_context = speek_context.concat(speek);
            }
        }

        speek_context = speek_context.startsWith("&") ? speek_context.substring(1) : speek_context;
        speek_context = speek_context.endsWith("&") ? speek_context.substring(0, speek_context.length() - 1) : speek_context;

        return speek_context;
    }

    public static String FindOperator(String phonenumber) {
        //st = st.replaceAll("[^\\d\\+]+","");
        phonenumber = phonenumber.replaceAll("[^\\d\\+]+", "");
        if (phonenumber.matches("^(?:(?:\\+84|84)|0)(?:(?:9[678]|3[2-9])|86)\\d{7}$")) {
            return "viettel";
        }
        if (phonenumber.matches("^(?:(?:\\+84|84)|0)(?:(?:9[03]|7[06789])|89)\\d{7}$")) {
            return "mobifone";
        }
        if (phonenumber.matches("^(?:(?:\\+84|84)|0)(?:9[14]|8[123458])\\d{7}$")) {
            return "vinaphone";
        }
        if (phonenumber.matches("^(?:(?:\\+84|84)|0)(?:92|5[268])\\d{7}$")) {
            return "vietnamobile";
        }
        if (phonenumber.matches("^(?:(?:\\+84|84)|0)(?:9[59]|59)\\d{7}$")) {
            return "gmobile";
        }

        if (phonenumber.matches("^(?:(?:\\+84|84)|0)(87)\\d{7}$")) {
            return "itelecom";
        }

        if (phonenumber.matches("^(?:(?:\\+84|84)|0)(55)\\d{7}$")) {
            return "reddi";
        }

        if (phonenumber.matches("^(?:(?:\\+84|84)|0)(2)\\d{8}$") || phonenumber.matches("^(?:(?:\\+84|84)|0)(80)\\d{7}$")) {
            return "fix";
        }

        if (phonenumber.matches("^(?:(\\+)|00)\\d{8,13}$")) {
            return "international";
        }
        return "unknown";
    }

}
