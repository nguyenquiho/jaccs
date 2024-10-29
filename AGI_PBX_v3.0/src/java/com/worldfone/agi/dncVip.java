/*
 * This is Tin java
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
public class dncVip extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        System.out.print("welcome to agi dnc list");
        try {
            String src = ar.getArguments()[0];
            String status = DBSelectionSet.getInstance().getStatusDNC(src);
            if ("1".equals(status)) {
                ac.verbose("So cam goi vao! ", 1);
                ac.hangup();
            } else {
                ac.verbose("Vuot qua dnc list! ", 1);
                String type = "";
                String des = "";
                status = "0";
                HashMap<String, String> rs = DBSelectionSet.getInstance().getVipList(src);
                if (rs != null && rs.containsKey("status") && rs.containsKey("destinatype") && rs.containsKey("destination")) {
                    status = rs.get("status");
                    type = rs.get("destinatype");
                    des = rs.get("destination");
                }
                if ("1".equals(status)) {
                    String goToContext = DBSelectionSet.getInstance().createContextGoto(type, des);
                    ac.verbose("So trong viplist", 1);
                    ac.exec("Goto", goToContext);
                } else {
                    ac.verbose("Vuot qua viplist", 1);
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

}
