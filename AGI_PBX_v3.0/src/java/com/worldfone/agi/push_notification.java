package com.worldfone.agi;

import com.google.gson.Gson;
import com.worldfone.access.DBSelectionSet;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import java.util.Properties;
/**
 *
 * @author honguyen
 */
public class push_notification extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            ac.verbose("Notification !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", 1);
            ac.verbose("UM BA LA XI BUA", 1);
            String src = ar.getArguments()[0];
            String exten = ar.getArguments()[1];
            HashMap<String, String> appInfo = DBSelectionSet.getInstance().getAppInfo(exten);
            
            if (appInfo != null || !appInfo.isEmpty()) {
                String deviceToken = appInfo.get("push_key");
                String osName = appInfo.get("os_name");

                if (Arrays.asList("ios", "worldfone4biz").contains(osName) && deviceToken != null && !deviceToken.isEmpty()) {
//                    ac.setMusic(true, "default");
                    String calluuid = ar.getUniqueId();
                    String sipFrom = DBSelectionSet.getInstance().getAgentName(src);
                    String sipTo = DBSelectionSet.getInstance().getAgentName(exten);

                    HashMap<String, String> dataPush = new HashMap<String, String>();
                    dataPush.put("id", calluuid);
                    dataPush.put("deviceToken", deviceToken);
                    dataPush.put("remoteURL", src);
                    dataPush.put("from", String.format("\\\"%s\\\" <%s;transport=tcp>", sipFrom, src));
                    dataPush.put("to", String.format("\\\"%s\\\" <%s;transport=tcp>", sipTo, exten));
                    dataPush.put("app_name", osName);

                    boolean pushNotificationResult;
                    
                    Properties props = new Properties();
                    FileInputStream in = new FileInputStream("/opt/tomcat9/webapps/AGI_PBX_Config.properties");
                    props.load(in);
                    
                    String app_url = "";
                    String api_url = "";
                    if (props.containsKey("app.url")) {
                        app_url = props.getProperty("app.url");
                        api_url = app_url + "/softphoneapi/push_notification.php";
                    }
                    System.out.println("API url: " + api_url);
                    if (api_url != ""){
                        pushNotificationResult = pushNotification(api_url, dataPush, ac);
                        
                        if (pushNotificationResult) {
                            ac.verbose("PUSH NOTIFICATION SUCCESSFULLY!", 1);
                            Thread.sleep(6000); // 6 seconds in milliseconds
    //                        ac.setMusic(false);
                        } else {
                            ac.verbose("PUSH NOTIFIACTION FAILED!", 1);
                        }
                    }else{
                        ac.verbose("URL PUSH NOTICE INCORRECT!", 1);
                    }
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(push_notification.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(push_notification.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static boolean pushNotification(String url, HashMap<String, String> data, AgiChannel ac) {
        //CKBKHN
        return pushNotificationHelper(data, url, ac);
    }

    private static boolean pushNotificationSandbox(HashMap<String, String> data, AgiChannel ac) {
        //CKBKHN
        return pushNotificationHelper(data, "http://voicecenter.kbsec.com.vn/softphoneapi/push_notification.sandbox.php", ac);
    }

    private static boolean pushNotificationHelper(HashMap<String, String> data, String urlString, AgiChannel ac) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString("softphoneapi:Admin@St3l37779".getBytes()));
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = new Gson().toJson(data);
//            System.out.println("JSON PUSH: " + jsonInputString);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    Map<String, Object> responseObj = new Gson().fromJson(response.toString(), Map.class);
                    if ((Double) responseObj.get("status") == 1) {
                        System.out.println(responseObj.get("message"));
                        return true;
                    }
                }
            }else{
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                    System.out.println("Error code: " + code);
                    System.out.println("Error response message: " + errorResponse.toString());
                } catch (IOException e) {
                    System.out.println("Error while reading error response: " + e.getMessage());
                }
                return false;
            }
        } catch (Exception e) {
            System.out.println("Exception Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private static void setMusic(boolean on, String musicClass) {
        if (on) {
            System.out.println("Music set to: " + musicClass);
        } else {
            System.out.println("Music stopped.");
        }
    }
}