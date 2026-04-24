/*
 * This is Tin 
 */
package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
import com.worldfone.callback.Callback2;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

/**
 *
 * @author Tin
 * @deverlop MitsuyoRai
 *
 */
public class crmoutbound extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        long receivedTimestamp0 = System.currentTimeMillis() / 1000;
        String now = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(Calendar.getInstance().getTime());
        try {
            ac.verbose("checkCrmExternal", 1);
            if (!ar.getChannel().contains("trunk") && ar.getArguments().length < 5) {
//                ac.verbose("ifffffffffffff 1", 1);
                ac.setVariable("external_crm", "1");
                if (!DBSelectionSet.getInstance().checkExternallc()) {
                    ac.verbose("Limit ExternalCRM!!!!!!!!!!!", 1);
                    ac.hangup();
                } else {
//                    ac.verbose("ifffffffffffff 2", 1);
                    String src = ar.getArguments()[0].trim();
//                    ac.verbose("SRCCCCCCCCCCCC " + src, 1);
                    String dst = ar.getDnid();
                    if (ar.getChannel().toLowerCase().contains("local")) {
                        dst = ar.getArguments()[1].trim();
                    }

                    String dialid = "";
                    String calldest = dst;
                    if (ar.getArguments().length > 3 && ar.getArguments()[3] != null) {
                        dialid = ar.getArguments()[3];
                    }

                    String calluuid = ar.getUniqueId();
                    String callernumber = "";
                    HashMap<String, String> dest = DBSelectionSet.getInstance().getCrmDestionationByName(src);
                    if (dest != null) {
//                        ac.verbose("ifffffffffffff 4", 1);
                        callernumber = src;
                        calldest = dst;
                    } else {
//                        ac.verbose("ifffffffffffff 5", 1);
                        dest = DBSelectionSet.getInstance().getCrmDestionationByName(dst);
                        if (dest != null) {
//                            ac.verbose("ifffffffffffff 6", 1);
                            callernumber = dst;
                            calldest = src;
                        }
                    }
                    if (dest != null) {
//                        ac.verbose("ifffffffffffff 7", 1);
                        String dnis = ar.getCallerIdNumber();
                        String calltype = "Outbound_non-ACD";
                        try {
                            if (!callernumber.equalsIgnoreCase(calldest)) {
//                                ac.verbose("ifffffffffffff 8", 1);
                                String agent_name = ar.getCallerIdName();
                                if (agent_name == null || "".equals(agent_name)) {
                                    HashMap extension_info = DBSelectionSet.getInstance().getExtensionInfo(callernumber);
                                    if (extension_info != null) {
                                        agent_name = extension_info.get("callername").toString();
                                    }
                                }
//                                if (dnis.equalsIgnoreCase(calldest)) { // line 82
////                                    ac.verbose("ifffffffffffff 9", 1);
//                                    String variable = ac.getVariable("ARG1");
//                                    String[] arr_temp = variable.split("/");
//                                    calldest = arr_temp[2];
//                                }

                                // thay doan code  tren
                                if (dnis != null && calldest != null && dnis.equalsIgnoreCase(calldest)) { 
                                    // ac.verbose("ifffffffffffff 9", 1);
                                    String variable = ac.getVariable("ARG1");

                                    // Kiểm tra biến variable từ Asterisk có tồn tại không
                                    if (variable != null && variable.contains("/")) {
                                        String[] arr_temp = variable.split("/");
                                        // Kiểm tra mảng có đủ phần tử không trước khi truy cập index 2
                                        if (arr_temp.length >= 3) {
                                            calldest = arr_temp[2];
                                        }
                                    }
                                }
                                DBSelectionSet.getInstance().logCurrentEvent(calluuid, dest.get("site_id"), calltype);
                                if (dest.containsKey("callcenter_type")) {
//                                    ac.verbose("ifffffffffffff 10", 1);
                                    ac.setVariable("CDR(callcenter_type)", dest.get("callcenter_type"));
                                }
                                HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(dest.get("site_id"));
                                if (site != null) {
//                                    ac.verbose("ifffffffffffff 11", 1);
                                    try {
                                        String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
//                                        ac.verbose("URLLLLLLLLL: " + url, 1);
                                        Callback2 c = new Callback2(url);
                                        c.addParam("secret", site.get("secret"));
                                        c.addParam("callstatus", "Dialing");
                                        c.addParam("calluuid", calluuid);
                                        c.addParam("direction", "outbound");
                                        c.addParam("callernumber", callernumber);
                                        c.addParam("destinationnumber", calldest);
                                        c.addParam("agentname", agent_name);
                                        c.addParam("starttime", now);
                                        c.addParam("dnis", dnis);
                                        c.addParam("calltype", calltype);
                                        if (dialid != null && !"".equals(dialid)) {
                                            c.addParam("dialid", dialid);
                                            DBSelectionSet.getInstance().logDialId(calluuid, dialid);
                                        }
                                        c.addParam("version", "3");
                                        c.sendGet(receivedTimestamp0);
                                    } catch (URISyntaxException ex) {
                                        System.out.println(ex);
                                    }
                                }
                            }
                        } catch (AgiException ex) {
                            System.out.println("Error: " + ex);
                        }
                    }
                }
            }
//            ac.verbose("ifffffffffffff 3", 1);
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}
