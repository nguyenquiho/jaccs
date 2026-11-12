///*
// * This is Tin 
// */
//package com.worldfone.agi;
//
//import com.worldfone.access.DBSelectionSet;
//import com.worldfone.callback.Callback2;
//import java.net.URISyntaxException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.HashMap;
//import org.asteriskjava.fastagi.AgiChannel;
//import org.asteriskjava.fastagi.AgiException;
//import org.asteriskjava.fastagi.AgiRequest;
//import org.asteriskjava.fastagi.BaseAgiScript;
//
///**
// *
// * @author Tin
// * @deverlop MitsuyoRai
// *
// */
//public class crmoutbound extends BaseAgiScript {
//
//    @Override
//    public void service(AgiRequest ar, AgiChannel ac) {
//        long receivedTimestamp0 = System.currentTimeMillis() / 1000;
//        String now = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(Calendar.getInstance().getTime());
//        try {
//            ac.verbose("checkCrmExternal", 1);
//            if (!ar.getChannel().contains("trunk") && ar.getArguments().length < 5) {
////                ac.verbose("ifffffffffffff 1", 1);
//                ac.setVariable("external_crm", "1");
//                if (!DBSelectionSet.getInstance().checkExternallc()) {
//                    ac.verbose("Limit ExternalCRM!!!!!!!!!!!", 1);
//                    ac.hangup();
//                } else {
////                    ac.verbose("ifffffffffffff 2", 1);
//                    String src = ar.getArguments()[0].trim();
////                    ac.verbose("SRCCCCCCCCCCCC " + src, 1);
//                    String dst = ar.getDnid();
//                    if (ar.getChannel().toLowerCase().contains("local")) {
//                        dst = ar.getArguments()[1].trim();
//                    }
//
//                    String dialid = "";
//                    String calldest = dst;
//                    if (ar.getArguments().length > 3 && ar.getArguments()[3] != null) {
//                        dialid = ar.getArguments()[3];
//                    }
//
//                    String calluuid = ar.getUniqueId();
//                    String callernumber = "";
//                    HashMap<String, String> dest = DBSelectionSet.getInstance().getCrmDestionationByName(src);
//                    if (dest != null) {
////                        ac.verbose("ifffffffffffff 4", 1);
//                        callernumber = src;
//                        calldest = dst;
//                    } else {
////                        ac.verbose("ifffffffffffff 5", 1);
//                        dest = DBSelectionSet.getInstance().getCrmDestionationByName(dst);
//                        if (dest != null) {
////                            ac.verbose("ifffffffffffff 6", 1);
//                            callernumber = dst;
//                            calldest = src;
//                        }
//                    }
//                    if (dest != null) {
////                        ac.verbose("ifffffffffffff 7", 1);
//                        String dnis = ar.getCallerIdNumber();
//                        String calltype = "Outbound_non-ACD";
//                        try {
//                            if (!callernumber.equalsIgnoreCase(calldest)) {
////                                ac.verbose("ifffffffffffff 8", 1);
//                                String agent_name = ar.getCallerIdName();
//                                if (agent_name == null || "".equals(agent_name)) {
//                                    HashMap extension_info = DBSelectionSet.getInstance().getExtensionInfo(callernumber);
//                                    if (extension_info != null) {
//                                        agent_name = extension_info.get("callername").toString();
//                                    }
//                                }
////                                if (dnis.equalsIgnoreCase(calldest)) { // line 82
//////                                    ac.verbose("ifffffffffffff 9", 1);
////                                    String variable = ac.getVariable("ARG1");
////                                    String[] arr_temp = variable.split("/");
////                                    calldest = arr_temp[2];
////                                }
//
//                                // thay doan code  tren
//                                if (dnis != null && calldest != null && dnis.equalsIgnoreCase(calldest)) { 
//                                    // ac.verbose("ifffffffffffff 9", 1);
//                                    String variable = ac.getVariable("ARG1");
//
//                                    // Kiểm tra biến variable từ Asterisk có tồn tại không
//                                    if (variable != null && variable.contains("/")) {
//                                        String[] arr_temp = variable.split("/");
//                                        // Kiểm tra mảng có đủ phần tử không trước khi truy cập index 2
//                                        if (arr_temp.length >= 3) {
//                                            calldest = arr_temp[2];
//                                        }
//                                    }
//                                }
//                                DBSelectionSet.getInstance().logCurrentEvent(calluuid, dest.get("site_id"), calltype);
//                                if (dest.containsKey("callcenter_type")) {
////                                    ac.verbose("ifffffffffffff 10", 1);
//                                    ac.setVariable("CDR(callcenter_type)", dest.get("callcenter_type"));
//                                }
//                                HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(dest.get("site_id"));
//                                if (site != null) {
////                                    ac.verbose("ifffffffffffff 11", 1);
//                                    try {
//                                        String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
////                                        ac.verbose("URLLLLLLLLL: " + url, 1);
//                                        Callback2 c = new Callback2(url);
//                                        c.addParam("secret", site.get("secret"));
//                                        c.addParam("callstatus", "Dialing");
//                                        c.addParam("calluuid", calluuid);
//                                        c.addParam("direction", "outbound");
//                                        c.addParam("callernumber", callernumber);
//                                        c.addParam("destinationnumber", calldest);
//                                        c.addParam("agentname", agent_name);
//                                        c.addParam("starttime", now);
//                                        c.addParam("dnis", dnis);
//                                        c.addParam("calltype", calltype);
//                                        if (dialid != null && !"".equals(dialid)) {
//                                            c.addParam("dialid", dialid);
//                                            DBSelectionSet.getInstance().logDialId(calluuid, dialid);
//                                        }
//                                        c.addParam("version", "3");
//                                        c.sendGet(receivedTimestamp0);
//                                    } catch (URISyntaxException ex) {
//                                        System.out.println(ex);
//                                    }
//                                }
//                            }
//                        } catch (AgiException ex) {
//                            System.out.println("Error: " + ex);
//                        }
//                    }
//                }
//            }
////            ac.verbose("ifffffffffffff 3", 1);
//        } catch (AgiException ex) {
//            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
//        }
//    }
//}


package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
import com.worldfone.callback.Callback2;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

public class crmoutbound extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        long receivedTimestamp0 = System.currentTimeMillis() / 1000;
        String now = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(Calendar.getInstance().getTime());
        
        try {
            ac.verbose("================= [DEBUG crmoutbound START] =================", 1);
            ac.verbose("Channel: " + ar.getChannel(), 1);
            ac.verbose("UniqueId: " + ar.getUniqueId(), 1);
            ac.verbose("CallerID Num: " + ar.getCallerIdNumber() + " | Name: " + ar.getCallerIdName(), 1);
            ac.verbose("DNID: " + ar.getDnid(), 1);
            ac.verbose("Arguments: " + Arrays.toString(ar.getArguments()), 1);

            // 1. Kiểm tra điều kiện trunk và số lượng arguments
            boolean isTrunk = ar.getChannel() != null && ar.getChannel().contains("trunk");
            int argLength = ar.getArguments() != null ? ar.getArguments().length : 0;
            ac.verbose("Check Condition: !isTrunk=" + (!isTrunk) + " | argLength=" + argLength + " (< 5 is " + (argLength < 5) + ")", 1);

            if (!isTrunk && argLength < 5) {
                ac.setVariable("external_crm", "1");
                
                boolean checkLicense = DBSelectionSet.getInstance().checkExternallc();
                ac.verbose("Check External License checkExternallc(): " + checkLicense, 1);
                
                if (!checkLicense) {
                    ac.verbose("[STOP] Limit ExternalCRM reached! Calling hangup()", 1);
                    ac.hangup();
                    return;
                }

                String src = (argLength > 0 && ar.getArguments()[0] != null) ? ar.getArguments()[0].trim() : "";
                String dst = ar.getDnid();
                if (dst == null || dst.trim().isEmpty() || ar.getChannel().toLowerCase().contains("local")) {
                    if (argLength > 1 && ar.getArguments()[1] != null) {
                        dst = ar.getArguments()[1].trim();
                    }
                }

                String dialid = "";
                if (argLength > 3 && ar.getArguments()[3] != null) {
                    dialid = ar.getArguments()[3].trim();
                }

                String calluuid = ar.getUniqueId();
                String callernumber = "";
                String calldest = dst;

                ac.verbose("Parsed Parameters -> SRC: [" + src + "] | DST: [" + dst + "] | DialID: [" + dialid + "] | UUID: [" + calluuid + "]", 1);

                // 2. Debug getCrmDestionationByName với SRC
                ac.verbose("Querying getCrmDestionationByName for SRC [" + src + "]...", 1);
                HashMap<String, String> dest = null;
                try {
                    dest = DBSelectionSet.getInstance().getCrmDestionationByName(src);
                    ac.verbose("Result for SRC [" + src + "]: " + (dest != null ? dest.toString() : "NULL"), 1);
                } catch (Exception exSrc) {
                    ac.verbose("[EXCEPTION] in getCrmDestionationByName(src): " + exSrc.getMessage(), 1);
                }

                if (dest != null) {
                    callernumber = src;
                    calldest = dst;
                } else {
                    // Fallback với DST
                    ac.verbose("Result for SRC is NULL. Querying getCrmDestionationByName for DST [" + dst + "]...", 1);
                    try {
                        dest = DBSelectionSet.getInstance().getCrmDestionationByName(dst);
                        ac.verbose("Result for DST [" + dst + "]: " + (dest != null ? dest.toString() : "NULL"), 1);
                    } catch (Exception exDst) {
                        ac.verbose("[EXCEPTION] in getCrmDestionationByName(dst): " + exDst.getMessage(), 1);
                    }
                    if (dest != null) {
                        callernumber = dst;
                        calldest = src;
                    }
                }

                // 3. Đánh giá kết quả tìm kiếm site_id
                if (dest == null) {
                    ac.verbose("[STOP] Both SRC and DST returned NULL from getCrmDestionationByName. Bỏ qua ghi log con_crm_curevents.", 1);
                    ac.verbose("================= [DEBUG crmoutbound END] =================", 1);
                    return;
                }

                String siteId = dest.get("site_id");
                ac.verbose("Found Destination Mapping! site_id: " + siteId, 1);

                String dnis = ar.getCallerIdNumber();
                String calltype = "Outbound_non-ACD";

                if (!callernumber.equalsIgnoreCase(calldest)) {
                    String agent_name = ar.getCallerIdName();
                    if (agent_name == null || "".equals(agent_name)) {
                        HashMap extension_info = DBSelectionSet.getInstance().getExtensionInfo(callernumber);
                        if (extension_info != null && extension_info.containsKey("callername")) {
                            agent_name = extension_info.get("callername").toString();
                        }
                    }

                    if (dnis != null && calldest != null && dnis.equalsIgnoreCase(calldest)) {
                        String variable = ac.getVariable("ARG1");
                        if (variable != null && variable.contains("/")) {
                            String[] arr_temp = variable.split("/");
                            if (arr_temp.length >= 3) {
                                calldest = arr_temp[2];
                            }
                        }
                    }

                    // 4. Ghi log bảng con_crm_curevents
                    ac.verbose("Executing logCurrentEvent(UUID: " + calluuid + ", SiteID: " + siteId + ", Type: " + calltype + ")...", 1);
                    try {
                        DBSelectionSet.getInstance().logCurrentEvent(calluuid, siteId, calltype);
                        ac.verbose("[SUCCESS] Logged into con_crm_curevents!", 1);
                    } catch (Exception exLog) {
                        ac.verbose("[EXCEPTION] logCurrentEvent FAILED: " + exLog.getMessage(), 1);
                    }

                    if (dest.containsKey("callcenter_type")) {
                        ac.setVariable("CDR(callcenter_type)", dest.get("callcenter_type"));
                    }

                    // 5. Gửi Webhook Dialing
                    HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(siteId);
                    if (site != null) {
                        try {
                            String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                            ac.verbose("Sending Webhook Dialing to: " + url, 1);
                            
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
                            ac.verbose("[SUCCESS] Webhook Dialing sent.", 1);
                        } catch (URISyntaxException ex) {
                            ac.verbose("[EXCEPTION] Webhook Dialing URISyntaxException: " + ex.getMessage(), 1);
                        }
                    } else {
                        ac.verbose("[WARNING] Site not found in DB for site_id: " + siteId, 1);
                    }
                } else {
                    ac.verbose("[STOP] callernumber equals calldest (" + callernumber + "). Bỏ qua.", 1);
                }
            } else {
                ac.verbose("[STOP] Điều kiện (!isTrunk && argLength < 5) bị FALSE.", 1);
            }
            ac.verbose("================= [DEBUG crmoutbound END] =================", 1);
        } catch (AgiException ex) {
            System.out.println("[CRITICAL AgiException]: " + ex.getMessage());
        }
    }
}