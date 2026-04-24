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
public class check_externalcrm extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        long receivedTimestamp0 = System.currentTimeMillis() / 1000;
        try {
            ac.verbose("check external crm", 1);
            String src = ar.getArguments()[0];
            String crm_dest = ar.getArguments()[1];
            
            //EVNHCM
            String callid_web360 = "";
            
            if(ar.getArguments().length > 2){
                callid_web360 = ar.getArguments()[2];
//                System.out.println("CALLID_WEB360 " + callid_web360);
            }
//            System.out.println("CALLID_WEB360 " + callid_web360);
            String now = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(Calendar.getInstance().getTime());
            String channel = ar.getChannel();
            //System.out.println("channel " + channel);
            if (!channel.contains("Local")) {
                String calluuid = ar.getUniqueId();
                if (!DBSelectionSet.getInstance().checkExistCall(calluuid)) {
                    HashMap<String, String> dest = DBSelectionSet.getInstance().getCrmDestionationByName(crm_dest);
                    //System.out.println(dest);
                    boolean isextension = DBSelectionSet.getInstance().isextension(src);
                    if (dest != null && !isextension) {
                        String callernumber = ar.getCallerIdNumber();
                        String calltype = "Inbound_non-ACD";
                        if ("queue".equalsIgnoreCase(dest.get("dest_type"))) {
                            calltype = "Inbound_ACD";
                        }
                        HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(dest.get("site_id"));
                        if (site != null) {
                            String dnis = ar.getDnid();
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
                                if(!"".equalsIgnoreCase(callid_web360)){ // EVNHCM
                                    c.addParam("callid_web360", callid_web360);
                                    boolean setCallidWeb360 = DBSelectionSet.getInstance().setCallidWeb360(calluuid, callid_web360);
                                    if(!setCallidWeb360) System.out.println("DONT SET CALLID_WEB360" + calluuid);
                                }else{
                                    c.addParam("callid_web360", "no_web360");
                                }
                                c.addParam("version", "3");
                                c.sendGet(receivedTimestamp0);
                            } catch (URISyntaxException ex) {
                                Logger.getLogger(check_externalcrm.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        DBSelectionSet.getInstance().logCurrentEvent(calluuid, dest.get("site_id"), calltype);
                        DBSelectionSet.getInstance().logCurrentIncoming(callernumber, calluuid);
                    }
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

}
