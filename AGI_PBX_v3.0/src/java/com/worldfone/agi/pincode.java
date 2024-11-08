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
public class pincode extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            ac.verbose("Welcome agi pincode", 1);
            String dst = ar.getArguments()[2];
            String src = ar.getArguments()[1];
            if (ar.getArguments()[0] == null) {
                ac.verbose("K nhap pincode", 1);
                ac.exec("Goto", "check-pincode," + dst + ",1");
            } else {
                String timeout = null;
                String cursecond = null;
                String rc = "0";
                String key = ar.getArguments()[0];
                HashMap<String, String> rs = DBSelectionSet.getInstance().getPincode(key);
                if (rs != null && rs.containsKey("timeout") && rs.containsKey("record_calls")) {
                    timeout = rs.get("timeout");
                    cursecond = rs.get("cursecond");
                    rc = rs.get("record_calls");
                }
                if (timeout == null) {
                    ac.verbose("Sai pincode", 1);
                    ac.exec("Goto", "check-pincode," + dst + ",1");
                } else {
                    String status = DBSelectionSet.getInstance().checkPincode(key, dst, src);
                    if ("1".equals(status)) {
                        if(cursecond != null){
                            if(Integer.valueOf(cursecond) >= Integer.valueOf(timeout)){
                                ac.verbose("Pincode da dat gioi han thoi luong goi!", 1);
                                ac.hangup();
                            }
                        }else{
                            ac.verbose("Di theo pincode", 1);
                            ac.setVariable("TIMEOUT(absolute)", timeout);
                            if ("1".equals(rc)) {
                                ac.setVariable("RECORDING", "yes");
                            }
                            ac.exec("Goto", "lcr," + dst + ",1");
                        }
                    } else {
                        ac.verbose("Pincode het han hoac Prefix khong duoc goi ra!", 1);
                        ac.hangup();
                    }
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}
