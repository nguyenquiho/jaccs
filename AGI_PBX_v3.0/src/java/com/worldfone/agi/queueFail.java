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
public class queueFail extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            ac.verbose("Di vao queue after ring time", 1);
            HashMap<String, String> rs = DBSelectionSet.getInstance().getQueueByName(ar.getArguments()[0], "destinatype", "destination");
            if (rs != null && rs.containsKey("destinatype") && rs.containsKey("destination")) {
                String type = rs.get("destinatype");
                String detail = rs.get("destination");
                String goToContext = DBSelectionSet.getInstance().createContextGoto(type, detail);
                ac.exec("Goto", goToContext);
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + "" + ex.getMessage());
        }
    }

}
