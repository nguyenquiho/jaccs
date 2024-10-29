/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
import com.worldfone.utils.helpers.BasedCommon;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author MitsuyoRai
 */
public class ivrPlayback extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            String[] args = ar.getArguments();
            String id = "";
            String dialid = "";
            String type = "";
            String customerphone = "";
            String digito = "";
            if (args.length > 0 && args[0] != null) {
                id = args[0];
                //ac.verbose("ID: " + id, 1);
            }
            if (args.length > 1 && args[1] != null) {
                dialid = args[1];
                //ac.verbose("DialID: " + dialid, 1);
            }
            if (args.length > 2 && args[2] != null) {
                type = "outbound";
                customerphone = args[2];
                //ac.verbose("Customerphone outbound: " + customerphone, 1);
            } else if (args.length > 3 && args[3] != null) {
                type = "inbound";
                customerphone = args[3];
                //ac.verbose("Customerphone inbound: " + customerphone, 1);
            }
            if (args.length > 4 && args[4] != null) {
                //type = "inbound";
                digito = args[4];
                //ac.verbose("Customerphone inbound: " + customerphone, 1);
            }
            String callernumber = ar.getCallerIdNumber();
            String calluuid = ar.getUniqueId();
            if (!"".equalsIgnoreCase(id)) {
                //ac.verbose("Check IVR Playback ID: " + id, 1);
                ac.exec("Answer", "");
                try {
                    HashMap<String, String> rs = DBSelectionSet.getInstance().getIvrPlayback(id);
                    if (rs != null) {
                        String voice = rs.get("voice");
                        String context = rs.get("context");
                        String url = rs.get("url");
                        String method = rs.get("method");

                        HttpURLConnection httpClient;
                        if ("post".equalsIgnoreCase(method)) {
                            URL obj = new URL(url);
                            httpClient = (HttpURLConnection) obj.openConnection();
                            httpClient.setRequestMethod("POST");
                            httpClient.setRequestProperty("Content-Type", "application/json");
                            httpClient.setRequestProperty("Accept", "application/json");
                            httpClient.setDoInput(true);
                            httpClient.setDoOutput(true);
                            httpClient.setUseCaches(false);
                            //Create JSONObject here
                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("id", id);
                            if ("outbound".equalsIgnoreCase(type)) {
                                jsonParam.put("dialid", dialid);
                            }
                            jsonParam.put("callernumber", callernumber);
                            jsonParam.put("calluuid", calluuid);
                            jsonParam.put("digito", digito);
                            jsonParam.put("customerphone", customerphone);
                            try (DataOutputStream wr = new DataOutputStream(httpClient.getOutputStream())) {
                                //wr.writeUTF(URLEncoder.encode(jsonParam.toString(), "UTF-8"));            
                                wr.write(jsonParam.toString().getBytes());
                                wr.flush();
                                wr.close();
                            }
                        } else {
                            StringBuilder strUrl = new StringBuilder();
                            strUrl.append(url);
                            strUrl.append("?id=");
                            strUrl.append(id);
                            strUrl.append("&callernumber=");
                            strUrl.append(callernumber);
                            strUrl.append("&calluuid=");
                            strUrl.append(calluuid);
                            if (!"".equals(customerphone)) {
                                strUrl.append("&customerphone=");
                                strUrl.append(customerphone);
                            }
                            if (!"".equals(digito)) {
                                strUrl.append("&digito=");
                                strUrl.append(digito);
                            }
                            if ("outbound".equalsIgnoreCase(type)) {
                                strUrl.append("&dialid=");
                                strUrl.append(dialid);
                            }
                            url = strUrl.toString();
                            ac.verbose("New URL: " + url, 1);
                            URL obj = new URL(url);
                            httpClient = (HttpURLConnection) obj.openConnection();
                            httpClient.setRequestMethod("GET");
                            httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");
                        }
                        StringBuilder response;
                        int responseCode = httpClient.getResponseCode();
                        if (responseCode == 200) {
                            try (BufferedReader iny = new BufferedReader(new InputStreamReader(httpClient.getInputStream()))) {
                                String output;
                                response = new StringBuilder();
                                while ((output = iny.readLine()) != null) {
                                    response.append(output);
                                }
                            }
                            if (BasedCommon.isJSONValid(context) && BasedCommon.isJSONValid(response.toString())) {
                                JSONArray context_obj = new JSONArray(context);
                                JSONObject content_obj = new JSONObject(response.toString());
                                for (int i = 0; i < context_obj.length(); i++) {
                                    if (context_obj.getJSONObject(i).has("script")) {
                                        JSONObject scriptObject = context_obj.getJSONObject(i).getJSONObject("script");
                                        if (content_obj.has("type")) {
                                            if (content_obj.getString("type").equals(scriptObject.getString("name"))) {
                                                context_obj = scriptObject.getJSONArray("context");
                                                break;
                                            } else {
                                                continue;
                                            }
                                        } else {
                                            context_obj = scriptObject.getJSONArray("context");
                                            break;
                                        }
                                    }
                                    break;
                                }
                                //ac.verbose("Context: " + context_obj, 1);
                                String path;
                                if (!"".equalsIgnoreCase(voice) && !"default".equalsIgnoreCase(voice)) {
                                    path = DBSelectionSet.getInstance().getPatchVoiceACS(voice).concat("/");
                                } else {
                                    path = "/etc/asterisk/Customer/audio/vi/";
                                }
                                ArrayList<HashMap> fields = DBSelectionSet.getInstance().getAllFieldsPlayback();
                                String speek = BasedCommon.text2speak(context_obj, content_obj, path, fields);
                                //ac.verbose("Speek : " + speek, 1);
                                ac.exec("Playback", speek);
                                //ac.verbose("YEAHYEAH", 1);
                            }
                            String ivr = "";
                            if (BasedCommon.isJSONValid(rs.get("lastaction").trim())) {
                                //String lastaction = rs.get("lastaction").toString().trim();
                                //  JSONParser parser = new JSONParser();
                                JSONObject lastaction = new JSONObject(rs.get("lastaction"));
                                ivr = lastaction.getString("ivr");
                            }
                            //ac.verbose("Lastaction: " + ivr, 1);
                            if (!"".equalsIgnoreCase(ivr)) {
                                ac.exec("Goto", ivr);
                            }
                        } else {
                            if (BasedCommon.isJSONValid(rs.get("actiondefault"))) {
                                JSONObject actiondefault = new JSONObject(rs.get("actiondefault"));
                                String go_type = actiondefault.getString("default_type");
                                String go_to = actiondefault.getString("default_id");
                                String goToContext = DBSelectionSet.getInstance().createContextGoto(go_type, go_to);
                                ac.exec("Goto", goToContext);
                            }
                        }
                    }
                } catch (UnknownHostException ex) {
                    ac.verbose("UnknownHostException: " + ex.getMessage(), 1);
                    Logger.getLogger(ivrPlayback.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JSONException ex) {
                    ac.verbose("JSONException: " + ex.getMessage(), 1);
                    Logger.getLogger(ivrPlayback.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    ac.verbose("IOException: " + ex.getMessage(), 1);
                    Logger.getLogger(ivrPlayback.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

}
