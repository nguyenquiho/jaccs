package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
import com.worldfone.callback.Callback2;
import static com.worldfone.utils.helpers.BasedCommon.isJSONValid;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class lcr extends BaseAgiScript {

    public String version = "V3.2.0";
    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            ac.verbose("Welcome agi new lcr", 1);
//            System.out.println(version);
            String[] args = ar.getArguments();
            String src = args[2];
            String dst = args[3];
            String channel = ar.getChannel();
            ac.verbose("channel " + channel, 1);
           
            if (channel.startsWith("Local/")) {
                String[] a = channel.split("@");
                String[] b = a[0].split("/");
                if (DBSelectionSet.getInstance().isextension(b[1])) {
                    src = b[1];
                }
            }
            if (channel.startsWith("SIP/")) {
                String[] a = channel.split("-");
                String[] b = a[0].split("/");
                if (DBSelectionSet.getInstance().isextension(b[1])) {
                    src = b[1];
                }
            }
            ac.verbose("src " + src, 1);
            if (dst.indexOf("/") > 0) {
                String[] b = dst.split("/");
                dst = b[2];
            }
            int fwd = 0;
            try {
                if (args[0] != null && isNumeric(args[0])) {
                    fwd = Integer.parseInt(args[0]);
                }
            } catch (NumberFormatException e) {
                System.out.println(e);
            }
            int priority = 0;
            try {
                if (args.length > 1 && args[1] != null && !args[1].trim().isEmpty()) {
                    priority = Integer.parseInt(args[1].trim());
                    System.out.println("priority parsed successfully: " + priority);
                } else {
                    System.out.println("priority empty or null, set default0");
                }
            } catch (NumberFormatException e) {
                System.out.println("Lỗi NumberFormatException khi parse priority: " + e.getMessage());
                priority = 0;
            }

            String rfwd = args[0];
            if ((fwd != 0) && (!rfwd.equalsIgnoreCase(dst))) {
                src = String.valueOf(fwd);
            }
            int lcr_count = 0;
            try {
                lcr_count = Integer.parseInt(ac.getVariable("LCR_COUNT"));
                System.out.println("args[5]" + args[5]);
            } catch (NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                System.out.println("LCR COUNT failed");
            }
            lcr_count++;
            String DIALSTATUS = ac.getVariable("DIALSTATUS");
            System.out.println("DIALSTATUS java 1: " + DIALSTATUS);
            ac.setVariable("LCR_COUNT", String.valueOf(lcr_count));
            ac.verbose("LCR_COUNT: " + lcr_count, 1);
            Boolean check_did = false;
            HashMap<String, String> lcr_group = DBSelectionSet.getInstance().getInfoLcrgroup(src);
            if( !"CHANUNAVAIL".equals(DIALSTATUS) ){
                System.out.println("DIALSTATUS java 2 & DID_FAILED 1: " + DIALSTATUS);
                if (lcr_group != null) {
                    if (lcr_group.containsKey("callhours") && !"".equals(lcr_group.get("callhours")) && !lcr_group.get("callhours").isEmpty() && isJSONValid(lcr_group.get("callhours"))) {
                        try {
                            boolean range_valid = false;
                            JSONObject obj_json = new JSONObject(lcr_group.get("callhours"));
                            JSONArray starthours = new JSONArray(obj_json.get("starthour").toString());
                            JSONArray endhours = new JSONArray(obj_json.get("endhour").toString());
                            long timestamp = System.currentTimeMillis() / 1000;
                            for (int i = 0; i < endhours.length(); i++) {
                                Date date = Calendar.getInstance().getTime();
                                String starthour = new SimpleDateFormat("yyyy-MM-dd").format(date) + " " + starthours.get(i) + ":00";
                                String endhour = new SimpleDateFormat("yyyy-MM-dd").format(date) + " " + endhours.get(i) + ":00";
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date start = sdf.parse(starthour);
                                Date end = sdf.parse(endhour);
                                long millis_start = start.getTime() / 1000;
                                long millis_end = end.getTime() / 1000;

                                if (millis_start < timestamp && timestamp < millis_end) {
                                    range_valid = true;
                                    break;
                                }
                            }
                            if (!range_valid) {
                                ac.hangup();
                                return;
                            }
                        } catch (JSONException | ParseException ex) {
                            Logger.getLogger(lcr.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    String rf;
                    String trunk_name = "";
                    String strip;
                    String spre;
                    int pro;
                    String carrier = "";
                    String lg = lcr_group.get("lcrgroup");
                    ArrayList<HashMap> rs = DBSelectionSet.getInstance().getLcrByCustomerID(lg);
                    if (rs != null) {
                        if (lcr_count > rs.size()) {
                            ac.hangup();
                            return;
                        }
                        for (HashMap<String, String> data : rs) {
                            if (data.containsKey("carrier") && data.get("carrier") != null && !"".equalsIgnoreCase(data.get("carrier")) && data.containsKey("trunk_name") && data.containsKey("record_calls") && data.containsKey("strip") && data.containsKey("spre") && data.containsKey("priority")) {
                                pro = Integer.parseInt(data.get("priority"));
                                ac.verbose("dst: " + dst, 1);
                                ac.verbose("rf: ", 1);
                                ac.verbose("pro: " + pro, 1);
                                ac.verbose("priority: " + priority, 1);
                                strip = data.get("strip");
                                String new_dst = dst;
                                if (!strip.isEmpty() && !channel.startsWith("Local")) {
                                    if (Pattern.matches("^" + strip.trim() + "(.*)", dst)) {
                                        int bg = 0;
                                        try {
                                            bg = strip.length();
                                        } catch (NumberFormatException e) {
                                            System.out.println(e);
                                        }
                                        new_dst = dst.substring(bg, dst.length());
                                        HashMap<String, String> dnco = DBSelectionSet.getInstance().getDNCO(new_dst);
                                        if (dnco != null && "1".equals(dnco.get("status")) && (dnco.get("groups").isEmpty() || (!dnco.get("groups").isEmpty() && dnco.get("groups").contains(lcr_group.get("groupid"))))) {
                                            ac.verbose("So cam goi ra! ", 1);
                                            ac.hangup();
                                            return;
                                        }
                                        carrier = DBSelectionSet.getInstance().FindOperator(new_dst);
                                        System.out.println("carrier null Roi! : " + carrier);
                                        ac.setVariable("CARRIER", carrier);
                                        ac.setVariable("CDR(carrier)", carrier);
                                        ac.verbose("NEW CARRIER: " + carrier, 1);
                                    }
                                } else {
                                    HashMap<String, String> dnco = DBSelectionSet.getInstance().getDNCO(dst);
                                    if (dnco != null && "1".equals(dnco.get("status")) && (dnco.get("groups").isEmpty() || (!dnco.get("groups").isEmpty() && dnco.get("groups").contains(lcr_group.get("groupid"))))) {
                                        ac.verbose("So cam goi ra! ", 1);
                                        ac.hangup();
                                        return;
                                    }
                                    carrier = DBSelectionSet.getInstance().FindOperator(dst);
                                    ac.setVariable("MNP", "no");
                                    ac.setVariable("CDR(mnp)", "no");
                                    ac.setVariable("CARRIER", carrier);
                                    ac.verbose("MNP no", 1);
                                    ac.setVariable("CDR(carrier)", carrier);
                                    ac.verbose("CARRIER: " + carrier, 1);
                                }

                                if (!"".equals(carrier) && data.get("carrier").contains(carrier) && pro > priority) {
                                    ac.setVariable("priority", data.get("priority"));
                                    if ("".equals(trunk_name)) {
                                        trunk_name = data.get("trunk_name");
                                        String trunk_id = data.get("trunk_id");
                                        String did_number = DBSelectionSet.getInstance().getRandDidInTrunkGroup(trunk_id);
                                        if (did_number == null) {
                                            did_number = data.get("did_number");
                                        }
                                        ac.setVariable("CALLERID(num)", did_number);
                                        ac.setVariable("CDR(did_number)", did_number);
                                        ac.verbose("CALLERID(num)-CDR(did_number): " + did_number, 1);
                                    }
                                    String rc = data.get("record_calls");
                                    spre = data.get("spre");
                                    ac.setVariable("priority", data.get("priority"));
                                    new_dst = spre + new_dst;
                                    if ("1".equals(rc)) {
                                        ac.setVariable("RECORDING", "yes");
                                    }
                                    System.out.println("Round 1 - LCR ");
                                    ac.setVariable("CHECKDID", "1");
                                    check_did = true;
                                    ac.verbose("Round 1 - LCR: " + trunk_name +" -- " + new_dst, 1);
                                    ac.setVariable("IN_OUT", "callout");
                                    ac.setVariable("AGENTNUMBER", src);
                                    ac.setVariable("PHONENUMBER", new_dst);
                                    ac.exec("Goto", trunk_name + "," + new_dst + ",1");
                                    return;
                                }
                            }
                       }

                        for (HashMap<String, String> data : rs) {
                            System.out.println("Key: " + data + ", Value: " + data);
                            if (data.containsKey("prefix") && data.containsKey("priority") && data.containsKey("trunk_name") && data.containsKey("record_calls") && data.containsKey("strip") && data.containsKey("spre")) {
                                rf = data.get("prefix");
                                pro = Integer.parseInt(data.get("priority"));
                                ac.verbose("dst: " + dst, 1);
                                ac.verbose("rf: " + rf, 1);
                                ac.verbose("pro: " + pro, 1);
                                ac.verbose("priority: " + priority, 1);
//                                System.out.println("Toi day line 238");
                                if (pro >= priority) {
//                                    System.out.println("Toi day line 240");
                                    System.out.println(rf);
                                    if (data.containsKey("interstation") && "1".equals(data.get("interstation"))) {
                                        System.out.println("CO BAT : interstation");
                                        ac.setVariable("CALLERID(num)", data.get("did_number") + src);
                                        trunk_name = data.get("trunk_name");

                                        String rc = data.get("record_calls");
                                        if ("1".equals(rc)) {
                                            ac.setVariable("RECORDING", "yes");
                                        }
                                        ac.setVariable("IN_OUT", "callout");
                                        ac.setVariable("AGENTNUMBER", src);
                                        ac.setVariable("PHONENUMBER", dst);
                                        ac.exec("Goto", trunk_name + "," + dst + ",1");
                                        return;
                                    } else {
                                        System.out.println("KHONG CO BAT : interstation");
                                        if ("".equals(trunk_name)) {
                                            trunk_name = data.get("trunk_name");
                                            String trunk_id = data.get("trunk_id");
                                            String did_number = DBSelectionSet.getInstance().getRandDidInTrunkGroup(trunk_id);
                                            System.out.println("trunk_id: " + trunk_id);
                                            System.out.println("did_number: " + did_number);
                                            if (did_number == null) {
                                                did_number = data.get("did_number");
                                            }
                                            ac.setVariable("CALLERID(num)", did_number);
                                            ac.setVariable("CDR(did_number)", did_number);
                                            ac.verbose("CALLERID(num)-CDR(did_number): " + did_number, 1);
                                        }
                                        String rc = data.get("record_calls");
                                        spre = data.get("spre");
                                        strip = data.get("strip");
                                        if (!channel.startsWith("Local") && !strip.isEmpty() && Pattern.matches("^9(.*)", dst)) {
                                            int bg = 0;
                                            try {
                                                bg = Integer.parseInt(strip);
                                            } catch (NumberFormatException e) {
                                                System.out.println(e);
                                            }
                                            dst = dst.substring(bg, dst.length());
                                        }
                                        HashMap<String, String> dnco = DBSelectionSet.getInstance().getDNCO(dst);
                                        if (dnco != null && "1".equals(dnco.get("status")) && (dnco.get("groups").isEmpty() || (!dnco.get("groups").isEmpty() && dnco.get("groups").contains(lcr_group.get("groupid"))))) {
                                            ac.verbose("So cam goi ra! ", 1);
                                            ac.hangup();
                                            return;
                                        }
                                        ac.setVariable("priority", data.get("priority"));
                                        dst = spre + dst;
                                        System.out.println("spre : "+ spre + " dst :" + dst);
                                        if ("1".equals(rc)) {
                                            ac.setVariable("RECORDING", "yes");
                                        }
                                        ac.setVariable("IN_OUT", "callout");
                                        ac.setVariable("AGENTNUMBER", src);
                                        ac.setVariable("PHONENUMBER", dst);
                                        carrier = DBSelectionSet.getInstance().FindOperator(dst);
                                        ac.setVariable("MNP", "no");
                                        ac.setVariable("CDR(mnp)", "no");
                                        ac.setVariable("CARRIER", carrier);
                                        ac.verbose("MNP no", 1);
                                        ac.setVariable("CDR(carrier)", carrier);
                                        ac.verbose("CARRIER: " + carrier, 1);
                                        ac.exec("Goto", trunk_name + "," + dst + ",1");
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    if ("".equals(trunk_name)) {
                        System.out.println("1");
                        HashMap<String, String> dest = DBSelectionSet.getInstance().getCrmDestionationByName(src);
                        if (dest != null) {
                            System.out.println("2");
                            HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(dest.get("site_id"));
                            if (site != null) {
                                System.out.println("3");
                                try {
                                    long receivedTimestamp0 = System.currentTimeMillis() / 1000;
                                    String starttime = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(Calendar.getInstance().getTime());
                                    String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                                    Callback2 c = new Callback2(url);
                                    c.addParam("secret", site.get("secret"));
                                    c.addParam("callstatus", "PutCDR");
                                    c.addParam("calluuid", ac.getUniqueId());
                                    c.addParam("direction", "outbound");
                                    c.addParam("callernumber", src);
                                    c.addParam("destinationnumber", dst);
                                    c.addParam("starttime", starttime);
                                    c.addParam("answertime", "");
                                    c.addParam("endtime", starttime);
                                    c.addParam("billduration", "0");
                                    c.addParam("totalduration", "0");
                                    c.addParam("calltype", "");
                                    String dialid = ac.getVariable("DIALID");
                                    if (dialid != null && !dialid.isEmpty()) {
                                        c.addParam("dialid", dialid);
                                    }
                                    c.addParam("dnis", "");
                                    c.addParam("disposition", "FAILED");
                                    c.addParam("version", "3");
                                    c.sendGet(receivedTimestamp0);
                                } catch (URISyntaxException ex) {
                                    System.out.println(ex);
                                }
                            }
                        }

                        ac.setVariable("CDR(acs_disposition)", "FAILED");
                        ac.verbose("src khong trunk", 1);
                        System.out.println(Arrays.toString(args));
                        ac.hangup();
                    }
                } else {
//                    String check_lcr = DBSelectionSet.getInstance().checkLcrCarriers(dst);
//                    System.out.println("dst transfer : " + dst);
//                    System.out.println("check_lcr : lcr" + check_lcr);
//                    if (check_lcr != null && !check_lcr.trim().equalsIgnoreCase("null")) {
//                        ac.verbose("transfer", 1);
//                        String channel_transfer = "";
//                        String ext_transfer = "";
//                        System.out.println("args[4]:"+ args[4]);
//                        
//                        if(!"".equals(args[4])){
//                            channel_transfer = args[4];
//                            System.out.println("args[4]:"+ args[4]);
//                            if (channel_transfer.startsWith("SIP") || channel_transfer.startsWith("OOH323")) {
//                                Thread.sleep(900);
//                                ext_transfer = DBSelectionSet.getInstance().getExtByChannel(channel_transfer);
//                                System.out.println("ext_transfer :" + ext_transfer);
//                            } else {
//                                System.out.println("NOT LOCAL: không phải 'SIP' hoặc 'OOH323'");
//                            }
//                        }else {
//                            System.out.println("GOODBYE:");
//                            ac.verbose("GOODBYE: ", 1);
//                            System.out.println("args[4] : " + args[4]);
//                            ac.hangup();
//                        }
//                        
//                        if("null".equals(ext_transfer) || ext_transfer == null){
//                            System.out.println("GOODBYE: ext_transfer");
//                            ac.verbose("GOODBYE: ", 1);
//                            System.out.println("args[4] : " + args[4]);
//                            ac.hangup();
//                        }
//                        String extractedValue = "";
//                        int start = ext_transfer.indexOf("/") + 1;
//                        int end = ext_transfer.indexOf("-", start);
//                        if (start > 0 && end > start) {
//                            extractedValue = ext_transfer.substring(start, end);
//                            System.out.println("Extracted Value: " + extractedValue);
//                        } else {
//                            System.out.println("Invalid ext_transfer format.");
//                        }
//
//                        HashMap<String, String> lcr_group_transfer = DBSelectionSet.getInstance().getInfoLcrgroup(extractedValue);
//                        if (lcr_group_transfer == null || lcr_group_transfer.isEmpty()) {
//                            System.out.println("No LCR group information found for ext_transfer: " + extractedValue);
//                            ac.verbose("GOODBYE: "+ args[4], 1);
//                            return;
//                        }
//                        String lg = lcr_group_transfer.get("lcrgroup");
//                        ArrayList<HashMap> rs_transfer = DBSelectionSet.getInstance().getLcrByCustomerID(lg);
//                        System.out.println("lg transfer : " + lg);
//                        for (HashMap<String, String> data : rs_transfer) {
//                            int pro = Integer.parseInt(data.get("priority"));
//                            ac.verbose("LCR TRANSFER: ", 1);
//                            ac.verbose("dst: " + dst, 1);
//                            ac.verbose("rf: ", 1);
//                            ac.verbose("pro: " + pro, 1);
//                            ac.verbose("priority: " + priority, 1);
//                            String trunk_name = data.get("trunk_name");
//                            ac.verbose("trunk_name TRANSFER: "+ trunk_name, 1);
//                            ac.verbose("pro TRANSFER: "+ pro, 1);
//                            ac.verbose("dst TRANSFER: "+ dst, 1);
//                            System.out.println("pro transfer : " + pro);
//                            System.out.println("dst transfer : " + dst);
//                            System.out.println("trunk_name transfer : " + trunk_name);
//                            ac.setVariable("IN_OUT", "callin");
//                            ac.setVariable("AGENTNUMBER", src);
//                            ac.setVariable("PHONENUMBER", dst);
//                            ac.exec("Goto", trunk_name +"," + dst + ",1");
//                            return;
//                        }
//                    }
//                    System.out.println("no lcr -: 'SIP'");
//                    ac.verbose("no lcr -", 1);
//                    ac.hangup();
                }
            }else {
                System.out.println("DID_FAILED");
                if(check_did){
                    String context_goto = "didnumberfailed" + ",s,1";
                    ac.verbose("goto: " + context_goto, 1);
                    ac.exec("Goto", context_goto);
                }else {
                    String CHECKDID = ac.getVariable("CHECKDID");
                    System.out.println("CHECKDID : ---------- " + CHECKDID);

                    String rf;
                    String trunk_name = "";
                    String strip;
                    String spre;
                    int pro;
                    String carrier = "";
                    String lg = lcr_group.get("lcrgroup");
                    ArrayList<HashMap> rs = DBSelectionSet.getInstance().getLcrByCustomerID(lg);
                    if (rs != null) {
                        if (lcr_count > rs.size()) {
                            ac.hangup();
                            return;
                        }
                        for (HashMap<String, String> data : rs) {
                            if (data.containsKey("carrier") && data.get("carrier") != null && !"".equalsIgnoreCase(data.get("carrier")) && data.containsKey("trunk_name") && data.containsKey("record_calls") && data.containsKey("strip") && data.containsKey("spre") && data.containsKey("priority")) {
                                pro = Integer.parseInt(data.get("priority"));
                                ac.verbose("dst: " + dst, 1);
                                ac.verbose("rf: ", 1);
                                ac.verbose("pro: " + pro, 1);
                                ac.verbose("priority: " + priority, 1);
                                strip = data.get("strip");
                                String new_dst = dst;
                                if (!strip.isEmpty() && !channel.startsWith("Local")) {
                                    if (Pattern.matches("^" + strip.trim() + "(.*)", dst)) {
                                        int bg = 0;
                                        try {
                                            bg = strip.length();
                                        } catch (NumberFormatException e) {
                                            System.out.println(e);
                                        }
                                        new_dst = dst.substring(bg, dst.length());
                                        HashMap<String, String> dnco = DBSelectionSet.getInstance().getDNCO(new_dst);
                                        if (dnco != null && "1".equals(dnco.get("status")) && (dnco.get("groups").isEmpty() || (!dnco.get("groups").isEmpty() && dnco.get("groups").contains(lcr_group.get("groupid"))))) {
                                            ac.verbose("So cam goi ra! ", 1);
                                            ac.hangup();
                                            return;
                                        }
                                        carrier = DBSelectionSet.getInstance().FindOperator(new_dst);
                                        System.out.println("carrier null Roi! : " + carrier);
                                        ac.setVariable("CARRIER", carrier);
                                        ac.setVariable("CDR(carrier)", carrier);
                                        ac.verbose("NEW CARRIER: " + carrier, 1);
                                    }
                                } else {
                                    HashMap<String, String> dnco = DBSelectionSet.getInstance().getDNCO(dst);
                                    if (dnco != null && "1".equals(dnco.get("status")) && (dnco.get("groups").isEmpty() || (!dnco.get("groups").isEmpty() && dnco.get("groups").contains(lcr_group.get("groupid"))))) {
                                        ac.verbose("So cam goi ra! ", 1);
                                        ac.hangup();
                                        return;
                                    }
                                    carrier = DBSelectionSet.getInstance().FindOperator(dst);
                                    ac.setVariable("MNP", "no");
                                    ac.setVariable("CDR(mnp)", "no");
                                    ac.setVariable("CARRIER", carrier);
                                    ac.verbose("MNP no", 1);
                                    ac.setVariable("CDR(carrier)", carrier);
                                    ac.verbose("CARRIER: " + carrier, 1);
                                }

                                if (!"".equals(carrier) && data.get("carrier").contains(carrier) && pro > priority) {
                                    ac.setVariable("priority", data.get("priority"));
                                    if ("".equals(trunk_name)) {
                                        trunk_name = data.get("trunk_name");
                                        String trunk_id = data.get("trunk_id");
                                        String did_number = DBSelectionSet.getInstance().getRandDidInTrunkGroup(trunk_id);
                                        if (did_number == null) {
                                            did_number = data.get("did_number");
                                        }
                                        ac.setVariable("CALLERID(num)", did_number);
                                        ac.setVariable("CDR(did_number)", did_number);
                                        ac.verbose("CALLERID(num)-CDR(did_number): " + did_number, 1);
                                    }
                                    String rc = data.get("record_calls");
                                    spre = data.get("spre");
                                    ac.setVariable("priority", data.get("priority"));
                                    new_dst = spre + new_dst;
                                    if ("1".equals(rc)) {
                                        ac.setVariable("RECORDING", "yes");
                                    }

                                    System.out.println("Round 2 - LCR ");
                                    ac.setVariable("LCRONE", "lcrone");
                                    ac.verbose("Round 2 - LCR: " + trunk_name +" -- " + new_dst, 1);
                                    ac.setVariable("IN_OUT", "callout");
                                    ac.setVariable("AGENTNUMBER", src);
                                    ac.setVariable("PHONENUMBER", new_dst);
                                    ac.exec("Goto", trunk_name + "," + new_dst + ",1");
                                    return;
                                }
                            }
                        }

                        for (HashMap<String, String> data : rs) {
                            System.out.println("Key: " + data + ", Value: " + data);
                            if (data.containsKey("prefix") && data.containsKey("priority") && data.containsKey("trunk_name") && data.containsKey("record_calls") && data.containsKey("strip") && data.containsKey("spre")) {
                                rf = data.get("prefix");
                                pro = Integer.parseInt(data.get("priority"));
                                ac.verbose("dst: " + dst, 1);
                                ac.verbose("rf: " + rf, 1);
                                ac.verbose("pro: " + pro, 1);
                                ac.verbose("priority: " + priority, 1);
                                System.out.println("Toi day line 238");
                                if (pro >= priority) {
                                    System.out.println("Toi day line 240");
                                    System.out.println(rf);
                                    if (data.containsKey("interstation") && "1".equals(data.get("interstation"))) {
                                        System.out.println("CO BAT : interstation");
                                        ac.setVariable("CALLERID(num)", data.get("did_number") + src);
                                        trunk_name = data.get("trunk_name");

                                        String rc = data.get("record_calls");
                                        if ("1".equals(rc)) {
                                            ac.setVariable("RECORDING", "yes");
                                        }
                                        ac.setVariable("IN_OUT", "callout");
                                        ac.setVariable("AGENTNUMBER", src);
                                        ac.setVariable("PHONENUMBER", dst);
                                        ac.exec("Goto", trunk_name + "," + dst + ",1");
                                        return;
                                    } else {
                                        System.out.println("KHONG CO BAT : interstation");
                                        if ("".equals(trunk_name)) {
                                            trunk_name = data.get("trunk_name");
                                            String trunk_id = data.get("trunk_id");
                                            String did_number = DBSelectionSet.getInstance().getRandDidInTrunkGroup(trunk_id);
                                            System.out.println("trunk_id: " + trunk_id);
                                            System.out.println("did_number: " + did_number);
                                            if (did_number == null) {
                                                did_number = data.get("did_number");
                                            }
                                            ac.setVariable("CALLERID(num)", did_number);
                                            ac.setVariable("CDR(did_number)", did_number);
                                            ac.verbose("CALLERID(num)-CDR(did_number): " + did_number, 1);
                                        }
                                        String rc = data.get("record_calls");
                                        spre = data.get("spre");
                                        strip = data.get("strip");
                                        if (!channel.startsWith("Local") && !strip.isEmpty() && Pattern.matches("^9(.*)", dst)) {
                                            int bg = 0;
                                            try {
                                                bg = Integer.parseInt(strip);
                                            } catch (NumberFormatException e) {
                                                System.out.println(e);
                                            }
                                            dst = dst.substring(bg, dst.length());
                                        }
                                        HashMap<String, String> dnco = DBSelectionSet.getInstance().getDNCO(dst);
                                        if (dnco != null && "1".equals(dnco.get("status")) && (dnco.get("groups").isEmpty() || (!dnco.get("groups").isEmpty() && dnco.get("groups").contains(lcr_group.get("groupid"))))) {
                                            ac.verbose("So cam goi ra! ", 1);
                                            ac.hangup();
                                            return;
                                        }
                                        ac.setVariable("priority", data.get("priority"));
                                        dst = spre + dst;
                                        System.out.println("spre : "+ spre + " dst :" + dst);
                                        if ("1".equals(rc)) {
                                            ac.setVariable("RECORDING", "yes");
                                        }
                                        ac.setVariable("IN_OUT", "callout");
                                        ac.setVariable("AGENTNUMBER", src);
                                        ac.setVariable("PHONENUMBER", dst);
                                        carrier = DBSelectionSet.getInstance().FindOperator(dst);
                                        ac.setVariable("MNP", "no");
                                        ac.setVariable("CDR(mnp)", "no");
                                        ac.setVariable("CARRIER", carrier);
                                        ac.verbose("MNP no", 1);
                                        ac.setVariable("CDR(carrier)", carrier);
                                        ac.verbose("CARRIER: " + carrier, 1);
                                        ac.exec("Goto", trunk_name + "," + dst + ",1");
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    if ("".equals(trunk_name)) {
                        System.out.println("1");
                        HashMap<String, String> dest = DBSelectionSet.getInstance().getCrmDestionationByName(src);
                        if (dest != null) {
                            System.out.println("2");
                            HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(dest.get("site_id"));
                            if (site != null) {
                                System.out.println("3");
                                try {
                                    long receivedTimestamp0 = System.currentTimeMillis() / 1000;
                                    String starttime = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(Calendar.getInstance().getTime());
                                    String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                                    Callback2 c = new Callback2(url);
                                    c.addParam("secret", site.get("secret"));
                                    c.addParam("callstatus", "PutCDR");
                                    c.addParam("calluuid", ac.getUniqueId());
                                    c.addParam("direction", "outbound");
                                    c.addParam("callernumber", src);
                                    c.addParam("destinationnumber", dst);
                                    c.addParam("starttime", starttime);
                                    c.addParam("answertime", "");
                                    c.addParam("endtime", starttime);
                                    c.addParam("billduration", "0");
                                    c.addParam("totalduration", "0");
                                    c.addParam("calltype", "");
                                    String dialid = ac.getVariable("DIALID");
                                    if (dialid != null && !dialid.isEmpty()) {
                                        c.addParam("dialid", dialid);
                                    }
                                    c.addParam("dnis", "");
                                    c.addParam("disposition", "FAILED");
                                    c.addParam("version", "3");
                                    c.sendGet(receivedTimestamp0);
                                } catch (URISyntaxException ex) {
                                    System.out.println(ex);
                                }
                            }
                        }

                        ac.setVariable("CDR(acs_disposition)", "FAILED");
                        ac.verbose("src khong trunk", 1);
                        ac.hangup();
                    }
                }
                
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
    public static boolean isNumeric(String str) { 
        try {  
          Double.valueOf(str);  
          return true;
        } catch(NumberFormatException e){  
          return false;  
        }  
    }
}