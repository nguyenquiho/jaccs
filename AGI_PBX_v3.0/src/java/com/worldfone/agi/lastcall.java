/*
 * This is Tin 
 */
package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
import com.worldfone.utils.helpers.AsteriskCommon;
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
public class lastcall extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            String src = ar.getArguments()[0];
            String dst = ar.getArguments()[1];
            HashMap lastcall = DBSelectionSet.getInstance().LastCall(dst);
            // System.out.println(lastcall);
            if (lastcall != null) {
                ac.verbose("had last call", 1);
                HashMap rs = null;
                if (lastcall.containsKey("lastcall_direction") && ("0".equals(lastcall.get("lastcall_direction").toString()) || "1".equals(lastcall.get("lastcall_direction").toString()))) {
                    if ("0".equals(lastcall.get("lastcall_direction").toString())) {
                        rs = DBSelectionSet.getInstance().getLastCDR(src, dst, "outbound");
                    } else if ("1".equals(lastcall.get("lastcall_direction").toString())) {
                        rs = DBSelectionSet.getInstance().getLastCDR(src, dst, "inbound");
                    }
                } else {
                    rs = DBSelectionSet.getInstance().getLastCDR(src, dst);
                }
                if (rs != null) {
                    String newdst = rs.get("dst").toString();
                    String newsrc = rs.get("src").toString();
                    String disposition = rs.get("disposition").toString();
                    String CDRdirection = rs.get("direction").toString();
                    String CDRchannel = rs.get("channel").toString();
                    String ext;
                    String lastExt = "";
                    if ("inbound".equalsIgnoreCase(CDRdirection) || "I".equalsIgnoreCase(CDRdirection)) {
                        if (DBSelectionSet.getInstance().isextension(newdst)) {
                            lastExt = newdst;
                        }
                        if ("".equals(lastExt) && rs.get("dstchannel").toString().contains("/")) {
                            String[] a = rs.get("dstchannel").toString().split("-");
                            String[] b = a[0].split("/");
                            ext = b[1];
                            if (DBSelectionSet.getInstance().isextension(ext)) {
                                lastExt = ext;
                            }
                        }
                    } else if (DBSelectionSet.getInstance().isextension(newsrc)) {
                        lastExt = newsrc;
                    }
                    if ("".equals(lastExt) && CDRchannel.startsWith("Local/")) {
                        String[] a = CDRchannel.split("@");
                        String[] b = a[0].split("/");
                        ext = b[1];
                        if (DBSelectionSet.getInstance().isextension(ext)) {
                            lastExt = ext;
                        }
                    }
                    if (!"".equals(lastExt)) {
                        Boolean flag = true;
                        if (lastcall.containsKey("lastcall_disposition") && "1".equals(lastcall.get("lastcall_disposition").toString()) && !"ANSWERED".equals(disposition)) {
                            flag = false;
                            ac.verbose("Last Call Disposition: UNANSWERED", 1);
                        } else if (lastcall.containsKey("lastcall_agentstatus") && "1".equals(lastcall.get("lastcall_agentstatus").toString()) && !(DBSelectionSet.getInstance().checkIDLEExtCRM(lastExt) || AsteriskCommon.checkIDLE(lastExt))) {
                            flag = false;
                            ac.verbose("Last Call AgentStatus: Not Available", 1);
                        }
                        if (flag) {
                            String destid = DBSelectionSet.getInstance().checkExternal(lastExt);
                            if (destid != null) {
                                String context_goto = "ecrm-" + destid + ",s,1";
                                ac.exec("Goto", context_goto);
                            } else {
                                String context_goto = "internal," + lastExt + ",1";
                                ac.exec("Goto", context_goto);
                            }
                        } else {
                            ac.verbose("Pass Last Call - Ko phu hop dieu kien", 1);
                        }
                    } else {
                        ac.verbose("Pass Last Call - Ko co Extension", 1);
                    }
                }
            } else {
                ac.verbose("Vuot qua last call! ", 1);
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

}
