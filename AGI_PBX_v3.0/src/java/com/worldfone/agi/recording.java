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
public class recording extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            ac.verbose("Welcome agi record", 1);
            String record = "no";
            try {
                record = ar.getArguments()[3];
            } catch (Exception e) {
                System.out.println(e);
            }
            if (!"yes".equals(record)) {
                ac.verbose("Bat dau kiem tra ghi am", 1);
                String src = ar.getArguments()[0];
                HashMap<String, String> rs = DBSelectionSet.getInstance().checkRecordDirection(src);
                String src1 = "0";
                String src2 = "0";
                if (rs != null && rs.containsKey("outbound") && rs.containsKey("internal")) {
                    src1 = rs.get("outbound");
                    src2 = rs.get("internal");
                }
                if ("1".equals(src1) && "1".equals(src2)) {
                    ac.verbose("src ghi am", 1);
                    ac.setVariable("RECORDING", "yes");
                } else {
                    String lnao = ar.getArguments()[2];
                    String dst = ar.getArguments()[1];
                    if (null == lnao) {
                        rs = DBSelectionSet.getInstance().checkRecordDirection(dst);
                        if (rs != null && rs.containsKey("inbound") && rs.containsKey("internal")) {
                            src1 = rs.get("inbound");
                            src2 = rs.get("internal");
                        }
                        if ("1".equals(src1) && "1".equals(src2)) {
                            record = "1";
                        }
                    } else {
                        switch (lnao) {
                            case "meetme":
                            case "queue":
                                record = DBSelectionSet.getInstance().checkRecordCall(dst, lnao);
                                ac.verbose("dst ghi am" + record, 1);
                                break;
                            default:
                                rs = DBSelectionSet.getInstance().checkRecordDirection(dst);
                                if (rs != null && rs.containsKey("inbound") && rs.containsKey("internal")) {
                                    src1 = rs.get("inbound");
                                    src2 = rs.get("internal");
                                }
                                if ("1".equals(src1) && "1".equals(src2)) {
                                    record = "1";
                                }
                                break;
                        }
                    }
                    if ("1".equals(record)) {
                        ac.verbose("co ghi am", 1);
                        ac.setVariable("RECORDING", "yes");
                    }
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

}
