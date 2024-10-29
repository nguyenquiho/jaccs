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
public class queueAfterTime extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            HashMap<String, String> rs = DBSelectionSet.getInstance().getQueueByName(ar.getArguments()[0], "aftertime");
            if (rs != null && rs.containsKey("aftertime")) {
                String time = rs.get("aftertime");
                if ("".equals(time) || "0".equals(time)) {
                    ac.setVariable("aftertime", "120");
                } else {
                    ac.setVariable("aftertime", time);
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

}
