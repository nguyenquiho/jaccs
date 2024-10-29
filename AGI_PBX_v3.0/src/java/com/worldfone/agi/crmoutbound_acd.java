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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class crmoutbound_acd extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        long receivedTimestamp0 = System.currentTimeMillis() / 1000;
        try {
            ac.verbose("welcome agi outbound acd", 1);
            if (!DBSelectionSet.getInstance().checkExternallc()) {
                ac.verbose("Limit ExternalCRM!!!!!!!!!!!", 1);
                ac.hangup();
            } else {
                String callback = ar.getArguments()[0];
                String callback_type = ar.getArguments()[1];
                String dialid = ar.getArguments()[2];
                String callto = ar.getArguments()[3];
                String now = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(Calendar.getInstance().getTime());
                //----------------------------------------------------------------------------------------------
                HashMap<String, String> dest = DBSelectionSet.getInstance().getCrmDestionationByName(callback);
                String site_id = dest.get("site_id");
                HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                String calluuid = ar.getUniqueId();
                String callernumber = ar.getCallerIdNumber();
                String dnis = ar.getDnid();
                String calltype = "Outbound_non-ACD";
                List<String> ext_available = null;
                String queue = null;
                if (callernumber.length() > 6) {
                    if ("queue".equalsIgnoreCase(dest.get("dest_type"))) {
                        calltype = "Outbound_ACD";
                        queue = dest.get("dest_id");
                        //System.out.println(ext_available);
                    }
                    DBSelectionSet.getInstance().logCurrentEvent(calluuid, site_id, calltype);
                    DBSelectionSet.getInstance().logCurrentIncoming(callto, calluuid);
                    ac.setVariable("CDR(callcenter_type)", dest.get("callcenter_type"));
                    if (site != null) {
                        try {
                            String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                            Callback2 c = new Callback2(url);
                            c.addParam("secret", site.get("secret"));
                            c.addParam("callstatus", "Start");
                            c.addParam("calluuid", calluuid);
                            c.addParam("direction", "outbound");
                            c.addParam("callernumber", callback);
                            c.addParam("destinationnumber", callto);
                            c.addParam("receivedtime", now);
                            c.addParam("dialid", dialid);
                            c.addParam("starttime", now);
                            c.addParam("dnis", dnis);
                            c.addParam("calltype", calltype);
                            if (queue != null) {
                                c.addParam("queue", queue);
                            }
                            if (ext_available != null) {
                                ext_available.forEach(extension -> {
                                    c.addParam("extension_available[]", extension);
                                });
                            }
                            c.addParam("version", "3");
                            c.sendGet(receivedTimestamp0);
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(crmoutbound_acd.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (callback_type.equals("extension")) {
                        callback_type = "internal";
                    }
                    String goToContext = callback_type + "," + callback + ",1";
                    ac.exec("Goto", goToContext);
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}
