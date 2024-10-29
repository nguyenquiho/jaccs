/*
 * This is Tin 
 */
package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
import com.worldfone.callback.Callback2;
import com.worldfone.utils.helpers.AsteriskCommon;
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
public class crminbound extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        long receivedTimestamp0 = System.currentTimeMillis() / 1000;
        try {
            ac.verbose("welcome agi inbound", 1);
            if (!DBSelectionSet.getInstance().checkExternallc()) {
                ac.verbose("Limit ExternalCRM!!!!!!!!!!!", 1);
                ac.hangup();
            } else {
                String crm_dest = ar.getArguments()[0];
                String now = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(Calendar.getInstance().getTime());
                HashMap<String, String> dest = DBSelectionSet.getInstance().getCrmDestionation(crm_dest);
                if (dest != null) {
                    String site_id = dest.get("site_id");
                    HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                    String calluuid = ar.getUniqueId();
                    String callernumber = ar.getCallerIdNumber();
                    String dnis = ar.getDnid();
                    String calltype = "Inbound_non-ACD";
                    String queue = null;
                    List<String> ext_available = null;
                    if (callernumber.length() > 6) {
                        if ("queue".equalsIgnoreCase(dest.get("dest_type"))) {
                            calltype = "Inbound_ACD";
                            ext_available = AsteriskCommon.QueueShow(dest.get("dest_id"));
                            queue = dest.get("dest_id");
                            //System.out.println(ext_available);
                        }
                        DBSelectionSet.getInstance().logCurrentEvent(calluuid, dest.get("site_id"), calltype);
                        DBSelectionSet.getInstance().logCurrentIncoming(callernumber, calluuid);
                        if (dest.containsKey("callcenter_type")) {
                            ac.setVariable("CDR(callcenter_type)", dest.get("callcenter_type"));
                        }
                        String key_press = ac.getVariable("KEY_PRESS");
                        try {
                            String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                            Callback2 c = new Callback2(url);
                            c.addParam("secret", site.get("secret"));
                            c.addParam("callstatus", "Start");
                            c.addParam("calluuid", calluuid);
                            c.addParam("direction", "inbound");
                            c.addParam("callernumber", callernumber);
                            c.addParam("destinationnumber", dest.get("dest_id"));
                            c.addParam("receivedtime", now);
                            c.addParam("starttime", now);
                            c.addParam("dnis", dnis);
                            if (key_press != null) {
                                c.addParam("key_press", key_press);
                            }

                            if (queue != null) {
                                c.addParam("queue", queue);
                            }
                            if (ext_available != null) {
                                for (String extension : ext_available) {
                                    c.addParam("extension_available[]", extension);
                                }
                            }
                            c.addParam("version", "3");
                            c.sendGet(receivedTimestamp0);
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(crminbound.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        String agent_name = DBSelectionSet.getInstance().getAgentName(dest.get("dest_id"));
                        agent_name = agent_name != null ? agent_name : "";
                        DBSelectionSet.getInstance().logCurrentEvent(calluuid, dest.get("site_id"), "internal");
                        DBSelectionSet.getInstance().logCurrentIncoming(callernumber, calluuid);
                        try {
                            String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                            Callback2 c = new Callback2(url);
                            c.addParam("secret", site.get("secret"));
                            c.addParam("callstatus", "Dialing");
                            c.addParam("calluuid", calluuid);
                            c.addParam("direction", "internal");
                            c.addParam("callernumber", callernumber);
                            c.addParam("destinationnumber", dest.get("dest_id"));
                            c.addParam("starttime", now);
                            c.addParam("agentname", agent_name);
                            c.addParam("internal", "1");
                            c.addParam("version", "3");
                            c.sendGet(receivedTimestamp0);
                        } catch (URISyntaxException ex) {
                            System.out.println(ex);
                        }
                    }
                    ac.setVariable("EXTERNAL_CRM_SITE_ID", site_id);
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}
