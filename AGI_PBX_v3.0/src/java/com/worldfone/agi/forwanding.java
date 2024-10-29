/*
 * This is Tin java
 */
package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
import com.worldfone.callback.Callback2;
import java.net.URISyntaxException;
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
public class forwanding extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) throws AgiException {
        try {
            HashMap<String, String> extension_info = DBSelectionSet.getInstance().getExtensionForwanding(ar.getArguments()[0]);
            String fwd = "";
            String type = "";
            String detail = "";
            String dnd = "0";

            if (extension_info != null) {
                fwd = extension_info.get("forwanding");
                type = extension_info.get("destinatype");
                detail = extension_info.get("destination");
                dnd = extension_info.get("dnd");
            }
            //if("1".equals(dnd)){
            //    ac.exec("Playback", "agent-loggedoff");
            //    ac.hangup();
            // }
            String k = ar.getArguments()[1];
            if ("0".equals(type)) {
                type = "lcr";
            }
            boolean flg = false;
            //System.out.println("k " + k);
            //System.out.println("fwd " + fwd);
            if (k.equals("1")) {
                if (fwd.contains("anytime")) {
                    flg = true;
                }
            } else {
                String dt = ar.getArguments()[2];
                if ("BUSY".equals(dt) && fwd.contains("busy")) {
                    flg = true;
                } else if ("CHANUNAVAIL".equals(dt) && fwd.contains("invalid")) {
                    flg = true;
                } else if ("NOANSWER".equals(dt) && fwd.contains("timeout")) {
                    flg = true;
                }
                //System.out.println("dt " + k);
            }
            //System.out.println("fwd " + fwd);
            if (flg) {
                int j;
                if ("1".equals(k)) {
                    j = 2;
                } else {
                    j = 3;
                }
                if (ar.getArguments().length > j && ar.getArguments()[j] != null && !"".equalsIgnoreCase(ar.getArguments()[j])) {
                    long receivedTimestamp0 = System.currentTimeMillis() / 1000;
                    String site_id = ar.getArguments()[j];
                    HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                    if (site != null) {
                        try {
                            String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                            Callback2 c = new Callback2(url);
                            c.addParam("secret", site.get("secret"));
                            c.addParam("callstatus", "Forwarding");
                            c.addParam("calluuid", ar.getUniqueId());
                            c.addParam("callernumber", ar.getArguments()[0]);
                            c.addParam("destinationnumber", detail);
                            c.addParam("version", "3");
                            c.sendGet(receivedTimestamp0);
                        } catch (URISyntaxException ex) {
                            System.out.println(ex);
                        }
                    }
                }
                ac.setVariable("EXT_SRC", ar.getArguments()[0]);
                String goToContext = DBSelectionSet.getInstance().createContextGoto(type, detail);
                ac.exec("Goto", goToContext);
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

}
