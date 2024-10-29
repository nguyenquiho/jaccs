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

public class lcr extends BaseAgiScript {

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
            if (dst.indexOf("/") > 0) {
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

            HashMap<String, String> lcr_group = DBSelectionSet.getInstance().getInfoLcrgroup(src);
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
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}
