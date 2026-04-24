/*
 * This is Tin 
 */
package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
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
public class outbound extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            ac.verbose("welcome agi outbound", 1);
            String src = ar.getArguments()[0];
            String dst = ar.getArguments()[1];
            String channel = ar.getArguments()[2];
            if (channel.startsWith("Local/")) {
                String[] a = channel.split("@");
                String[] b = a[0].split("/");
                src = b[1];
                ac.setVariable("CDR(direction)", "O");
            }
            ac.verbose("welcome agi outbound1", 1);
            String status = DBSelectionSet.getInstance().getStatusDNCO(dst);
            ac.verbose("welcome agi outbound 2", 1);
            if ("1".equals(status)) {
                ac.verbose("So cam goi ra! ", 1);
                ac.hangup();
            } else {
                ac.verbose("Vuot qua dnco list! ", 1);
                String id = null;
                HashMap<String, String> rs = DBSelectionSet.getInstance().getQueueByName(dst, "id");
                if (rs != null) {
                    id = rs.get("id");
                }
                if (id != null) {
                    ac.verbose("Goi vao queue", 1);
                    ac.exec("Goto", "queue," + dst + ",1");
                } else {
                    id = DBSelectionSet.getInstance().getAgentID(dst);
                    if (id != null) {
                        ac.verbose("Goi vao extention", 1);
                        ac.exec("Goto", "internal," + dst + ",1");
                    } else {
                        String pincode = DBSelectionSet.getInstance().getAgentCol(src, "pincode");
                        if (pincode == null) {
                            ac.verbose("src la so ben ngoai", 1);
                            ac.setVariable("forwanding", dst);
                            ac.exec("Goto", "lcr," + dst + ",1");
                        } else {
                            if ("yes".equals(pincode)) {
                                ac.verbose("src check picode", 1);
                                ac.exec("Goto", "check-pincode," + dst + ",1");
                            } else {
                                ac.verbose("src khong dung picode", 1);
                                status = DBSelectionSet.getInstance().checkAllowDenyNumber(src, dst);
                                if ("1".equals(status)) {
                                    HashMap<String, String> rs2 = DBSelectionSet.getInstance().checkRecordDirection(src);
                                    String src1 = "0";
                                    String src2 = "0";
                                    if (rs2 != null && rs2.containsKey("outbound") && rs2.containsKey("external")) {
                                        src1 = rs2.get("outbound");
                                        src2 = rs2.get("external");
                                    }
                                    if (src1.equals("1") && src2.equals("1")) {
                                        ac.setVariable("RECORDING", "yes");
                                    }
                                    String timeout = DBSelectionSet.getInstance().getAgentCol(src, "timeout");
                                    if (timeout != null) {
                                        ac.setVariable("TIMEOUT(absolute)", timeout);
                                    } 
                                    ac.verbose("Di qua agi outbound", 1);
                                    ac.exec("Goto", "lcr," + dst + ",1");
                                } else {
                                    ac.verbose("Prefix khong duoc goi ra", 1);
                                    ac.hangup();
                                }
                            }
                        }
                    }
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}
