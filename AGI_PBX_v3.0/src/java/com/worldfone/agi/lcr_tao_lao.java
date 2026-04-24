/*
* This is Tin 
*/
package com.worldfone.agi;
 
import com.worldfone.access.DBSelectionSet;
import com.worldfone.callback.Callback2;
import com.worldfone.utils.helpers.BasedCommon;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
 
public class lcr_tao_lao extends BaseAgiScript {
 
    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            ac.verbose("Welcome agi new lcr", 1);
            String[] args = ar.getArguments();
            String src = args[2];// ar.getArguments()[2];
            String dst = args[3]; //ar.getArguments()[3];
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
            if (dst.indexOf("/") > 0) { //line 46
                String[] b = dst.split("/");
                dst = b[2];
            }
            int fwd = 0;
            try {
                fwd = Integer.parseInt(args[0]);//fwd = Integer.parseInt(ar.getArguments()[0]);
            } catch (NumberFormatException e) {
                //System.out.println(e);
            }
            int priority = 0;
            try {
                priority = Integer.parseInt(args[1]);//priority = Integer.parseInt(ar.getArguments()[1]);
            } catch (NumberFormatException e) {
                //System.out.println(e);
            }
            String rfwd = args[0];//ar.getArguments()[0];
            if ((fwd != 0) && (!rfwd.equalsIgnoreCase(dst))) {
                src = String.valueOf(fwd);
            }
 
            String phonenumber = dst;
            if (phonenumber.startsWith("0")) {
                phonenumber = phonenumber.replaceFirst("0", "84");
            }

            // co che retry
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

 
            HashMap<String, String> lcr_group = DBSelectionSet.getInstance().getInfoLcrgroup(src);
            boolean enable_retry_call_fail = DBSelectionSet.getInstance().getRetryCallFailConfg();
            if( !"CHANUNAVAIL".equals(DIALSTATUS) && !enable_retry_call_fail ){
                if (lcr_group != null) {
                    if (lcr_group.containsKey("nDNC") && "1".equals(lcr_group.get("nDNC")) && DBSelectionSet.getInstance().checkDNC(phonenumber)) {
                        HashMap<String, String> crm_dest = DBSelectionSet.getInstance().getCrmDestionationByName(src);
                        if (crm_dest != null) {
                            HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(crm_dest.get("site_id"));
                            if (site != null) {
                                try {
                                    long receivedTimestamp0 = System.currentTimeMillis() / 1000;
                                    String calluuid = ar.getUniqueId();
                                    String dnis = ar.getCallerIdNumber();
                                    String now = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(Calendar.getInstance().getTime());
                                    String dialid = null;
                                    try {
                                        dialid = ar.getArguments()[5];
                                    } catch (ArrayIndexOutOfBoundsException | NullPointerException ex) {
                                    }
                                    String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                                    Callback2 c = new Callback2(url);
                                    c.addParam("secret", site.get("secret"));
                                    c.addParam("callstatus", "DNC");
                                    c.addParam("dnc_type", "national");
                                    c.addParam("calluuid", calluuid);
                                    c.addParam("direction", "outbound");
                                    c.addParam("callernumber", src);
                                    c.addParam("destinationnumber", dst);
                                    c.addParam("starttime", now);
                                    c.addParam("dnis", dnis);
                                    if (dialid != null && !"".equals(dialid)) {
                                        c.addParam("dialid", dialid);
                                    }
                                    c.addParam("version", "3");
                                    c.sendGet(receivedTimestamp0);
                                } catch (URISyntaxException ex) {
                                    System.out.println(ex);
                                }
                            }
                        }
                        ac.exec("Playback", "1tuchoicuocgoi");
                        ac.verbose("1tuchoicuocgoi", 1);
                        ac.hangup();
                    } else {
                        String lg = lcr_group.get("lcrgroup");
                        ArrayList<HashMap> rs = DBSelectionSet.getInstance().getLcrByCustomerID(lg);
                        //System.out.print(rs);
                        String rf;
                        //String checkrf;
                        String trunk_name = "";
                        String rc = "";
                        String strip = "";
                        String spre = "";
                        int pro;
                        //String tn = "";
                        String carrier = "";
                        try {
                            carrier = DBSelectionSet.getInstance().checkMNP(phonenumber);
                            if (carrier != null) {
                                setVariable("MNP", "yes");
                                setVariable("CARRIER", carrier);
                                ac.setVariable("CDR(mnp)", "yes");
                                ac.verbose("MNP yes", 1);
                            } else {
                                setVariable("MNP", "no");
                                ac.setVariable("CDR(mnp)", "no");
                                carrier = BasedCommon.FindOperator(dst);
                                setVariable("CARRIER", carrier);
                                ac.verbose("MNP no", 1);
                            }
                            ac.setVariable("CDR(carrier)", carrier);
                            ac.verbose("CARRIER: " + carrier, 1);
                        } catch (AgiException ex) {
                            System.out.println(ex);
                        }

                        if (rs != null) {
                            for (HashMap<String, String> data : rs) {
                                if ((!"".equals(carrier)) && data.containsKey("prefix") && data.containsKey("priority") && data.containsKey("trunk_name") && data.containsKey("record_calls") && data.containsKey("strip") && data.containsKey("spre")) {
                                    rf = data.get("prefix");
                                    pro = Integer.parseInt(data.get("priority"));
                                    ac.verbose("dst" + dst, 1);
                                    ac.verbose("rf" + rf, 1);
                                    ac.verbose("pro" + pro, 1);
                                    ac.verbose("priority" + priority, 1);
                                    //if(dst.startsWith(rf) && pro > priority ){//check lcr here
                                    //nếu có chuyển mạng đi theo chuyển mạng, và set carior ưu tiên chuyển mạng giử số, sau đó đi theo lcr
                                    String find_carrier = "\"" + carrier + "\"";
                                    if (data.containsKey("carrier") && data.get("carrier") != null && (!"".equalsIgnoreCase(data.get("carrier"))) && data.get("carrier").contains(find_carrier) && pro > priority) {
                                        //System.out.println("goto by carrier : " + data.get("carrier"));
                                        //tn = rf;
                                        trunk_name = data.get("trunk_name");
                                        rc = data.get("record_calls");
                                        strip = data.get("strip");
                                        spre = data.get("spre");
                                        ac.setVariable("priority", data.get("priority"));
                                        break;
                                    }
                                }
                            }
                            if ("".equals(trunk_name)) {
                                for (HashMap<String, String> data : rs) {
                                    if (data.containsKey("prefix") && data.containsKey("priority") && data.containsKey("trunk_name") && data.containsKey("record_calls") && data.containsKey("strip") && data.containsKey("spre")) {
                                        rf = data.get("prefix");
                                        pro = Integer.parseInt(data.get("priority"));
                                        ac.verbose("dst" + dst, 1);
                                        ac.verbose("rf" + rf, 1);
                                        ac.verbose("pro" + pro, 1);
                                        ac.verbose("priority" + priority, 1);
                                        if (Pattern.matches(rf, dst) && pro > priority) {
                                            //System.out.println(rf);
                                            //tn = rf;
                                            trunk_name = data.get("trunk_name");
                                            rc = data.get("record_calls");
                                            strip = data.get("strip");
                                            spre = data.get("spre");
                                            ac.setVariable("priority", data.get("priority"));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if ("".equals(trunk_name)) {
                            ac.verbose("src khong trunk", 1);
                            ac.hangup();
                        } else {
                            if ("1".equals(rc)) {
                                ac.setVariable("RECORDING", "yes");
                            }
                            int bg = 0;
                            try {
                                bg = Integer.parseInt(strip);
                            } catch (NumberFormatException e) {
                                //System.out.println(e);
                            }
                            dst = dst.substring(bg, dst.length());
                            dst = spre + dst;
                            ac.exec("Goto", trunk_name + "," + dst + ",1");
                        }
                    }
                } else {
                    ac.verbose("no lcr", 1);
                    ac.hangup();
                }
            }else {
                String rf;
                String trunk_name = "";
                String strip;
                String spre;
                int pro;
                String carrier = "";
                //String lg = DBSelectionSet.getInstance().getLcrgroup(src);
                String lg = lcr_group.get("lcrgroup");
                ArrayList<HashMap> rs = DBSelectionSet.getInstance().getLcrByCustomerID(lg);
                if (rs != null) {
                    if (lcr_count > rs.size()) {
                        //Canh bao thieu cau hinh
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
 
                                System.out.println("ROUND 2 - LCR ");
                                ac.setVariable("LCRONE", "lcrone");
                                ac.verbose("ROUND 2 - LCR: " + trunk_name +" -- " + new_dst, 1);
                                ac.setVariable("IN_OUT", "callout");
                                ac.setVariable("AGENTNUMBER", src);
                                ac.setVariable("PHONENUMBER", new_dst);
                                ac.exec("Goto", trunk_name + "," + new_dst + ",1");
                                return;
                            }
                        }
                    }
 
                    //Vượt qua check Carrier -> check theo prefix
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
                //}
                // Khong tim thay trunk hop le
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
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}