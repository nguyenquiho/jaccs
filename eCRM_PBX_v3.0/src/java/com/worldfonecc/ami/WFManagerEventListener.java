/*
 * Copyright © 2014 South Telecom
 */
package com.worldfonecc.ami;

import com.worldfonecc.callback.Callback;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.asteriskjava.util.Log;
import org.asteriskjava.util.LogFactory;
import com.worldfonecc.cache.MyMemcached;
import com.worldfonecc.admin.DBSelectionSet;
import com.worldfonecc.queue.JobAcsEvent;
import com.worldfonecc.queue.JobCdrEvent;
import com.worldfonecc.queue.JobInternal;
import com.worldfonecc.utils.helpers.Common;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.event.BridgeEvent;
import org.asteriskjava.manager.event.CdrEvent;
import org.asteriskjava.manager.event.CelEvent;
import org.asteriskjava.manager.event.ExtensionStatusEvent;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.HangupRequestEvent;
import org.asteriskjava.manager.event.LocalBridgeEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewStateEvent;
import org.asteriskjava.manager.event.PeerStatusEvent;
import org.asteriskjava.manager.event.TransferEvent;
import org.asteriskjava.manager.event.MusicOnHoldEvent;
import org.asteriskjava.manager.event.QueueMemberAddedEvent;
import org.asteriskjava.manager.event.QueueMemberRemovedEvent;
import org.asteriskjava.manager.event.QueueMemberStatusEvent;
import org.asteriskjava.manager.event.RenameEvent;
import org.asteriskjava.manager.event.VarSetEvent;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author nguyenngocbinh
 */
public class WFManagerEventListener extends Object implements ManagerEventListener {

    private final Log logger = LogFactory.getLog(getClass());

    private ManagerConnection managerConnection;
    private DefaultAsteriskServer asteriskServer;

    private String hostname = "127.0.0.1";
    private int port = 5038;
    private String username = "admin";
    private String password = "Admin@Stel7779";
    private final boolean log = false;
    //private String callback;
//    private final String curCDRtable = "curcdr";

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void startup() throws Exception {
        if (log) {
            logger.info("Prepare startup Event Listener");
        }
        ManagerConnectionFactory factory = new ManagerConnectionFactory(hostname, port, username, password);
        this.managerConnection = factory.createManagerConnection();
        // connect to Asterisk and log in
        managerConnection.login();
        managerConnection.registerUserEventClass(WorldfoneUserEvent.class);
        // register for events listener
        managerConnection.addEventListener(this);
        asteriskServer = new DefaultAsteriskServer(managerConnection);
        if (log) {
            logger.info("Manager Event Listener has startup");
        }
    }

    public void shutdown() throws Exception {
        if (managerConnection != null) {
            //remove events listener
            managerConnection.removeEventListener(this);
            // and finally log off and disconnect
            if ((managerConnection.getState() == ManagerConnectionState.CONNECTED) || (managerConnection.getState() == ManagerConnectionState.RECONNECTING)) {
                managerConnection.logoff();
                asteriskServer.shutdown();
            }
            if (log) {
                logger.info("Manager Event Listener has shutdown");
            }
        }
    }

    public void restart() throws Exception {
        shutdown();
        startup();
    }

    @Override
    public void onManagerEvent(ManagerEvent event) {
        Runnable task = () -> {
            long receivedTimestamp0 = System.currentTimeMillis() / 1000;
            String eventName = event.getClass().getSimpleName();
            try {
                switch (eventName) {
                    case "CelEvent":
                        handleCelEvent((CelEvent) event);
                        break;
                    case "NewStateEvent":
                        handleNewStateEvent((NewStateEvent) event, receivedTimestamp0);
                        break;
                    case "BridgeEvent":
                        handleBridgeEvent((BridgeEvent) event, receivedTimestamp0);
                        break;
                    case "CdrEvent":
                        handleCdrEvent((CdrEvent) event, receivedTimestamp0);
                        break;
                    case "HangupEvent":
                        handleHangupEvent((HangupEvent) event, receivedTimestamp0);
                        break;
                    case "HangupRequestEvent":
                        handleHangupRequestEvent((HangupRequestEvent) event);
                        break;
                    case "VarSetEvent":
                        handleVarSetEvent((VarSetEvent) event);
                        break;
                    case "PeerStatusEvent":
                        handlePeerStatusEvent((PeerStatusEvent) event, receivedTimestamp0);
                        break;
                    case "LocalBridgeEvent":
                        handleLocalBridgeEvent((LocalBridgeEvent) event);
                        break;
                    case "ExtensionStatusEvent":
                        handleExtensionStatusEvent((ExtensionStatusEvent) event, (CdrEvent) event, receivedTimestamp0);
                        break;
                    case "TransferEvent":
                        handleTransferEvent((TransferEvent) event);
                        break;
                    case "MusicOnHoldEvent":
                        handleMusicOnHoldEvent((MusicOnHoldEvent) event);
                        break;
                    case "QueueMemberAddedEvent":
                        handleQueueMemberAdd((QueueMemberAddedEvent) event, receivedTimestamp0);
                        break;
                    case "QueueMemberRemovedEvent":
                        handleQueueMemberRemove((QueueMemberRemovedEvent) event, receivedTimestamp0);
                        break;
                    case "QueueMemberStatusEvent":
                        handleQueueMemberStatus((QueueMemberStatusEvent) event, receivedTimestamp0);
                        break;
                    case "RenameEvent":
                        handleRenameEvent((RenameEvent) event);
                        break;
                }
            } catch (URISyntaxException ex) {
                Logger.getLogger(WFManagerEventListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    private void handleCelEvent(CelEvent ev) throws URISyntaxException {
        try {
            JobInternal job = new JobInternal();
            job.addParam("event", "CEL");
            job.addParam("EventName", ev.getEventName());
            job.addParam("Application", ev.getApplication());
            job.addParam("Context", ev.getContext());
            job.addParam("EventName", ev.getEventName());
            job.addParam("AccountCode", ev.getAccountCode());
            job.addParam("CallerIDnum", ev.getCallerIdNum());
            job.addParam("CallerIDname", ev.getCallerIdName());
            job.addParam("CallerIDani", ev.getCallerIDani());
            job.addParam("CallerIDrdnis", ev.getCallerIDrdnis());
            job.addParam("CallerIDdnid", ev.getCallerIDdnid());
            job.addParam("Exten", ev.getExten());
            job.addParam("Channel", ev.getChannel());
            job.addParam("EventTime", ev.getEventTime());
            job.addParam("UniqueID", ev.getUniqueID());
            job.addParam("LinkedID", ev.getLinkedID());
            job.addParam("Userfield", ev.getUserField());
            job.addParam("Peer", ev.getPeer());
            job.addParam("PeerAccount", ev.getPeerAccount());
            job.addParam("Extra", ev.getExtra());
            job.QueuePut();
        } catch (Exception ex) {
            //System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
        if (ev.getEventName().equalsIgnoreCase(CelEvent.CEL_EVENT_CHAN_START) && !ev.getChannel().endsWith(";2")) {
            DBSelectionSet.getInstance().updateLastChannel(ev.getLinkedID(), ev.getChannel());
        } else if (ev.getEventName().equalsIgnoreCase(CelEvent.CEL_EVENT_BRIDGE_ENTER) && !ev.getChannel().endsWith(";2") && ("Dial".equals(ev.getApplication()) || "Queue".equals(ev.getApplication()))) {
            DBSelectionSet.getInstance().updateAnswerTimeByUuid(ev.getUniqueID(), ev.getEventTime());

            if (ev.getPeer() != null && !"".equals(ev.getPeer()) && (ev.getPeer().startsWith("SIP") || ev.getPeer().startsWith("OOH323"))) {
                DBSelectionSet.getInstance().updateLastChannel(ev.getLinkedID(), ev.getPeer());
            }
        } else if (ev.getEventName().equals(CelEvent.CEL_EVENT_LINKEDID_END)) {
            try {
                JobCdrEvent jobCdr = new JobCdrEvent();
                jobCdr.addParam("event", "CDR");
                jobCdr.addParam("uniqueid", ev.getLinkedID());
                jobCdr.QueuePut(10);
            } catch (Exception e) {
                logger.error(e);
            }

            try {
                if (!ev.getLinkedID().equals(ev.getUniqueID())) {
                    JobCdrEvent jobCdr2 = new JobCdrEvent();
                    jobCdr2.addParam("event", "CDR");
                    jobCdr2.addParam("uniqueid", ev.getUniqueID());
                    jobCdr2.QueuePut(10);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        } else if (ev.getEventName().equalsIgnoreCase(CelEvent.CEL_EVENT_HANGUP) && ev.getContext().contains("internal") && ev.getChannel().endsWith(";2")) {
            /* Gửi event cho Job CDR */
            try {
                if (!ev.getLinkedID().equals(ev.getUniqueID())) {
                    JobCdrEvent jobCdr2 = new JobCdrEvent();
                    jobCdr2.addParam("event", "CDR");
                    jobCdr2.addParam("uniqueid", ev.getUniqueID());
                    jobCdr2.QueuePut(10);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    private void handleNewStateEvent(NewStateEvent ev, long receivedTimestamp0) throws URISyntaxException {
        if (ev.getChannelStateDesc().equalsIgnoreCase("Ringing")
                && (ev.getChannel().startsWith("SIP") || ev.getChannel().startsWith("OOH323"))
                && DBSelectionSet.getInstance().checkNumber(ev.getConnectedLineNum())
                && !DBSelectionSet.getInstance().checkDialing(ev.getUniqueId())
                && DBSelectionSet.getInstance().checkExtension(ev.getCallerIdNum())
                && !DBSelectionSet.getInstance().checkExtension(ev.getConnectedLineNum())) {
            //System.out.println("extension ring ring");

            String datereceived = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getDateReceived());
            String parentcalluuid = DBSelectionSet.getInstance().getCalluuid(ev.getConnectedLineNum());
            HashMap<String, String> CurCDR = DBSelectionSet.getInstance().CDRbyCalluuid(parentcalluuid);
            if (CurCDR != null) {
                String calltype = CurCDR.get("calltype");
                String site_id = CurCDR.get("site_id");
                HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                DBSelectionSet.getInstance().logExtension(ev.getUniqueId(), "inbound", site.get("site_id"), parentcalluuid);
                //------------------------------------------------------------------------------------------------
                String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                Callback c = new Callback(url);
                c.addParam("secret", site.get("secret"));
                c.addParam("callstatus", "Dialing");
                c.addParam("calluuid", ev.getUniqueId());
                if (calltype.equalsIgnoreCase("Outbound_ACD") || calltype.equalsIgnoreCase("Outbound_non-ACD")) {
                    c.addParam("direction", "outbound");
                    c.addParam("callernumber", ev.getCallerIdNum());
                    c.addParam("destinationnumber", ev.getConnectedLineNum());
                } else {
                    c.addParam("direction", "inbound");
                    c.addParam("callernumber", ev.getConnectedLineNum());
                    c.addParam("destinationnumber", ev.getCallerIdNum());
                }
                c.addParam("starttime", datereceived);
                c.addParam("receivedtime", datereceived);
                c.addParam("parentcalluuid", parentcalluuid);
                c.addParam("agentname", ev.getCallerIdName());
                c.addParam("calltype", calltype);
                c.addParam("version", "3");
                c.sendGet(receivedTimestamp0);
                //------------------------------------------------------------------------------------------------
            }
        }
    }

    private void handleBridgeEvent(BridgeEvent ev, long receivedTimestamp0) throws URISyntaxException {
        if (ev.isLink() && !ev.getChannel1().endsWith(";2")) {
            String answertime = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(Calendar.getInstance().getTime());
            String answered_key = "PBX_Event_handleBridgeEvent_" + ev.getUniqueId1();
            Object oCache_answer = MyMemcached.getInstance().get(answered_key);
            HashMap<String, String> curcdr = DBSelectionSet.getInstance().CDRbyCalluuid(ev.getUniqueId1());
            if (oCache_answer == null && curcdr != null && (!curcdr.get("call_status").equalsIgnoreCase("DialAnswer"))) {
                MyMemcached.getInstance().set(answered_key, ev.getUniqueId2(), new Date(System.currentTimeMillis() + 3000));
                String site_id = curcdr.get("site_id");
                HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                DBSelectionSet.getInstance().updateCallDialAnswer(ev.getUniqueId1(), "DialAnswer", ev.getUniqueId2(), ev.getChannel1(), ev.getChannel2(), curcdr);
                DBSelectionSet.getInstance().updateAnswerUuid(ev.getUniqueId1(), ev.getUniqueId2());
                String calluuid = ev.getUniqueId1();
                if (curcdr.get("parent_calluuid") != null) {
                    calluuid = curcdr.get("parent_calluuid");
                }
                String call_des = ev.getCallerId2();
                if (ev.getCallerId2() == null || "".equals(ev.getCallerId2())) {
                    String channel2 = ev.getChannel2();
                    if (channel2.startsWith("Local")) {
                        String[] arr_channel = channel2.split("/");
                        call_des = arr_channel[1].split("@")[0];
                    }
                }
                HashMap currentcall = DBSelectionSet.getInstance().getCurrentCall(ev.getUniqueId2());
                String callernumber2 = ev.getCallerId1();
                if (currentcall != null && currentcall.containsKey("number")) {
                    callernumber2 = currentcall.get("number").toString();
                }
                String agentname = DBSelectionSet.getInstance().getAgentName(call_des);
                //----------------------------------------------------------------------------
                String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                Callback c = new Callback(url);
                c.addParam("secret", site.get("secret"));
                c.addParam("callstatus", "DialAnswer");
                c.addParam("calluuid", calluuid);
                c.addParam("childcalluuid", ev.getUniqueId2());
                c.addParam("callernumber", callernumber2);
                c.addParam("answertime", answertime);
                if (agentname != null && !"".equals(agentname)) {
                    c.addParam("agentname", agentname);
                }
                c.addParam("destinationnumber", call_des);
                c.addParam("version", "3");
                c.sendGet(receivedTimestamp0, 1);
            } else {
                Object oCache = MyMemcached.getInstance().get(ev.getChannel1());
                if (oCache != null) {
                    try {
                        String url;
                        Callback c;
                        String jobStr = oCache.toString();
                        JSONObject data_transfer = new JSONObject(jobStr);
                        if (data_transfer.get("site_id").toString().equalsIgnoreCase("click_to_call_transfer")) {
                            curcdr = DBSelectionSet.getInstance().CDRbyCalluuid(ev.getUniqueId1());
                            if (curcdr != null) {
                                String site_id = curcdr.get("site_id");
                                HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                                DBSelectionSet.getInstance().updateCallDialAnswer(ev.getUniqueId1(), "DialAnswer", ev.getUniqueId2(), ev.getChannel1(), ev.getChannel2(), curcdr);
                                DBSelectionSet.getInstance().updateAnswerUuid(ev.getUniqueId1(), ev.getUniqueId2());
                                String calluuid = ev.getUniqueId1();
                                if (curcdr.get("parent_calluuid") != null) {
                                    calluuid = curcdr.get("parent_calluuid");
                                }
                                String call_des = ev.getCallerId2();
                                if (ev.getCallerId2() == null || ev.getCallerId2().equalsIgnoreCase("")) {
                                    String channel2 = ev.getChannel2();
                                    if (channel2.startsWith("Local")) {
                                        String[] arr_channel = channel2.split("/");
                                        call_des = arr_channel[1].split("@")[0];
                                    }
                                }
                                String agentname = DBSelectionSet.getInstance().getAgentName(call_des);
                                HashMap currentcall = DBSelectionSet.getInstance().getCurrentCall(ev.getUniqueId2());
                                String callernumber2 = ev.getCallerId1();
                                if (currentcall != null && currentcall.containsKey("number")) {
                                    callernumber2 = currentcall.get("number").toString();
                                }
                                //----------------------------------------------------------
                                url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                                c = new Callback(url);
                                c.addParam("secret", site.get("secret"));
                                c.addParam("callstatus", "DialAnswerTransfer");
                                c.addParam("calluuid", calluuid);
                                c.addParam("childcalluuid", ev.getUniqueId2());
                                c.addParam("callernumber", callernumber2);
                                c.addParam("answertime", answertime);
                                c.addParam("agentname", agentname);
                                c.addParam("destinationnumber", call_des);
                                c.addParam("version", "3");
                                c.sendGet(receivedTimestamp0);
                                //String result = c.sendGet(receivedTimestamp0);
                                //System.out.println(result);
                                //----------------------------------------------------------                                
                                MyMemcached.getInstance().delete(ev.getChannel1());
                            }
                        } else {
                            HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(data_transfer.get("site_id").toString());
                            curcdr = DBSelectionSet.getInstance().CDRbyCalluuid(ev.getUniqueId1());
                            DBSelectionSet.getInstance().updateCallDialAnswer(ev.getUniqueId1(), "DialAnswer", ev.getUniqueId2(), ev.getChannel1(), ev.getChannel2(), curcdr);
                            DBSelectionSet.getInstance().updateAnswerUuid(ev.getUniqueId1(), ev.getUniqueId2());
                            String calluuid = data_transfer.get("parent_calluuid").toString();
                            String child_calluuid = data_transfer.get("child_calluuid").toString();
                            String call_des = ev.getCallerId2();
                            String agentname = DBSelectionSet.getInstance().getAgentName(call_des);
                            HashMap currentcall = DBSelectionSet.getInstance().getCurrentCall(ev.getUniqueId2());
                            String callernumber2 = ev.getCallerId1();
                            if (currentcall != null && currentcall.containsKey("number")) {
                                callernumber2 = currentcall.get("number").toString();
                            }
                            //------------------------------------------------------------------------
                            url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                            c = new Callback(url);
                            c.addParam("secret", site.get("secret"));
                            c.addParam("callstatus", "DialAnswerTransfer");
                            c.addParam("calluuid", calluuid);
                            c.addParam("childcalluuid", child_calluuid);
                            c.addParam("callernumber", callernumber2);
                            c.addParam("answertime", answertime);
                            c.addParam("agentname", agentname);
                            c.addParam("destinationnumber", call_des);
                            c.addParam("version", "3");
                            c.sendGet(receivedTimestamp0);
                            //------------------------------------------------------------------------
                            MyMemcached.getInstance().delete(ev.getChannel1());
                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(WFManagerEventListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    private void handleHangupRequestEvent(HangupRequestEvent ev) {
        if (!ev.getChannel().endsWith(";2")) {
            String hangup_key = "hangupby_request_" + ev.getUniqueId();
            MyMemcached.getInstance().set(hangup_key, ev.getChannel(), new Date(System.currentTimeMillis() + 5000));
        }
    }

    private void handleHangupEvent(HangupEvent ev, long receivedTimestamp0) throws URISyntaxException {
        String childcalluuid = DBSelectionSet.getInstance().checkCallHangup(ev.getUniqueId());
        if (childcalluuid != null) {
            String datereceived = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getDateReceived());
            String url;
            Callback c;
            String site_id = DBSelectionSet.getInstance().getSiteIdBycalluuid(ev.getUniqueId());
            if (site_id != null && !"".equals(site_id)) {
                HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                if (site != null) {
                    if (DBSelectionSet.getInstance().checkUuid(ev.getUniqueId())) {
                        DBSelectionSet.getInstance().updateCall(ev.getUniqueId(), "HangUp");
                        //------------------------------------------------
                        url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                        c = new Callback(url);
                        c.addParam("secret", site.get("secret"));
                        c.addParam("callstatus", "Trim");
                        c.addParam("calluuid", ev.getUniqueId());
                        c.addParam("version", "3");
                        c.sendGet(receivedTimestamp0);
                        //------------------------------------------------
                        if (DBSelectionSet.getInstance().checkAnswerChilduuid(ev.getUniqueId())) {
                            String parentcalluuid = DBSelectionSet.getInstance().getParentCalluuid(ev.getUniqueId());
                            //------------------------------------------------
                            url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                            c = new Callback(url);
                            c.addParam("secret", site.get("secret"));
                            c.addParam("callstatus", "HangUp");
                            c.addParam("calluuid", parentcalluuid);
                            c.addParam("datereceived", datereceived);
                            c.addParam("causetxt", ev.getCause().toString());
                            c.sendGet(receivedTimestamp0);
                            //------------------------------------------------
                        }
                    } else {
                        if (!ev.getChannel().startsWith("Transfered")) {
                            DBSelectionSet.getInstance().updateCall(ev.getUniqueId(), "HangUp");
                        }
                        //--------------------------------------------------------------------------------------
                        DBSelectionSet.getInstance().deleteCallLog(ev.getConnectedLineNum(), ev.getCallerIdNum());
                        DBSelectionSet.getInstance().deleteCallLogbyUUid(ev.getUniqueId());

                        url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                        c = new Callback(url);
                        c.addParam("secret", site.get("secret"));
                        c.addParam("callstatus", "HangUp");
                        c.addParam("calluuid", ev.getUniqueId());
                        c.addParam("datereceived", datereceived);
                        c.addParam("causetxt", ev.getCause().toString());
                        c.addParam("version", "3");
                        c.sendGet(receivedTimestamp0);
                        //--------------------------------------------------------------------------------------
                    }
                }
            }
        }
        Object oCache = MyMemcached.getInstance().get("LocalBridgeEvent_" + ev.getUniqueId());
        if (oCache != null) {
            MyMemcached.getInstance().delete("LocalBridgeEvent_" + ev.getUniqueId());
        }
    }

    private void handleCdrEvent(CdrEvent ev, long receivedTimestamp0) throws URISyntaxException {
        /* Xử lý event ACS */
        if ("acs".equals(ev.getCalltype())) {
            long timestamp = System.currentTimeMillis();
            JobAcsEvent acs_event = new JobAcsEvent();
            acs_event.addParam("receivedTimestamp0", receivedTimestamp0 + "");
            acs_event.addParam("receivedTimestamp", timestamp + "");
            acs_event.addParam("calltype", ev.getCalltype());
            acs_event.addParam("customer_code", ev.getCustomercode());
            acs_event.addParam("dialid", ev.getDialid());
            acs_event.addParam("diallistid", ev.getDiallistid());
            acs_event.addParam("destination", ev.getDestination());
            acs_event.addParam("starttime", ev.getStartTime());
            acs_event.addParam("answertime", ev.getAnswerTime());
            acs_event.addParam("endtime", ev.getEndTime());
            acs_event.addParam("disposition", ev.getDisposition());
            acs_event.addParam("billableseconds", ev.getBillableSeconds() + "");
            acs_event.addParam("duration", ev.getDuration() + "");
            acs_event.addParam("uniqueid", ev.getUniqueId());
            acs_event.addParam("channel", ev.getChannel());
            acs_event.addParam("userfield", ev.getUserField());
            acs_event.QueuePut();
        }

        /* Xử lý event CDR externalCRM */
        try {
            if (ev.getDestinationChannel() == null || "".equals(ev.getDestinationChannel())) {
                String[] lastApps = new String[]{"agi", "goto", "read", "hangup"};
                if (Arrays.stream(lastApps).anyMatch(ev.getLastApplication().trim().toLowerCase()::equals) && ev.getDestinationContext().contains("trunk")) {
                    return;
                }
                //Khong lam gi
            } else {
                String chanel = ev.getChannel();
                String dstchanel = ev.getDestinationChannel();
                Object cache_chan_start = DBSelectionSet.getInstance().getLastChannel(ev.getUniqueId());
                if (cache_chan_start != null) {
                    if (!chanel.equalsIgnoreCase(cache_chan_start.toString()) && !dstchanel.equalsIgnoreCase(cache_chan_start.toString())) {
                        return;
                    }
                    DBSelectionSet.getInstance().deleteLastChannel(ev.getUniqueId());
                } else {
                    String lastchan = DBSelectionSet.getInstance().getLastChannel2(ev.getUniqueId());
                    if (lastchan != null && !chanel.equalsIgnoreCase(lastchan) && !dstchanel.equalsIgnoreCase(lastchan)) {
                        return;
                    }
                }
            }
        } catch (Exception ex0) {
            System.out.println("Loi gi do: " + ex0.getMessage());
        }

        if (!ev.getChannel().endsWith(";2")) {
            String hangup_by = "";
            String hangup_key = "hangupby_request_" + ev.getUniqueId();
            Object cache_hangupby = MyMemcached.getInstance().get(hangup_key);
            if (cache_hangupby != null) {
                if (cache_hangupby.toString().contains("trunk")) {
                    hangup_by = "customer";
                } else {
                    hangup_by = "agent";
                }
                DBSelectionSet.getInstance().updateCDRHangupBy(ev.getUniqueId(), hangup_by);
            }

            //cache chặn sự kiện cdr cuộc gọi nhánh chính trong trường hợp transfer
            Object oCache = MyMemcached.getInstance().get(ev.getUniqueId());
            if (oCache == null) {
                HashMap<String, String> current_call = DBSelectionSet.getInstance().getCurrentCallBycalluuid(ev.getUniqueId());
                if (current_call != null) {
                    String starttime, answertime, endtime, totalduration, billduration;
                    SimpleDateFormat printtime = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                    try {
                        if (ev.getChannel().endsWith(";1")) {
                            starttime = printtime.format(ev.getStartTimeAsDate());
                        } else {
                            Date parsedStarttime = sdf.parse(current_call.get("starttime"));
                            starttime = printtime.format(parsedStarttime);
                        }
                    } catch (NullPointerException | ParseException ex2) {
                        starttime = "";
                    }
                    try {
                        Date parsedAnswertime = sdf.parse(current_call.get("answertime"));
                        answertime = printtime.format(parsedAnswertime);
                    } catch (ParseException | NullPointerException ex) {
                        try {
                            answertime = printtime.format(ev.getAnswerTimeAsDate());
                        } catch (NullPointerException ex2) {
                            answertime = "";
                        }
                    }
                    try {
                        endtime = printtime.format(ev.getEndTimeAsDate());
                    } catch (NullPointerException ex) {
                        endtime = "";
                    }
                    totalduration = Common.diffTime(printtime, endtime, starttime);
                    billduration = Common.diffTime(printtime, endtime, answertime);
                    //---------------------------------------------------------------------
                    boolean is_answer = "1".equals(current_call.get("dialing_start_time"));
                    //---------------------------------------------------------------------
                    DBSelectionSet.getInstance().deleteCallLog(ev.getCallerId(), ev.getDestination());
                    DBSelectionSet.getInstance().deleteCallLogbyUUid(ev.getUniqueId());
                    DBSelectionSet.getInstance().updateCall(ev.getUniqueId(), "CdrEvent");
                    DBSelectionSet.getInstance().deleteCallHold(ev.getUniqueId());
                    //stopHold=0 không có hold, 1 hold kết thúc,2 hold chưa kết thúc
                    //tính hold time
                    int stopHold = 0;
                    int holdtime = 0;
                    try {
                        stopHold = Integer.parseInt(current_call.get("stopHold"));
                        holdtime = Integer.parseInt(current_call.get("holdtime"));
                    } catch (NullPointerException | NumberFormatException ex) {
                        //Logger.getLogger(WFManagerEventListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (stopHold == 2) {
                        SimpleDateFormat dateValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date startHoldTime;
                        try {
                            startHoldTime = dateValue.parse(current_call.get("startholdtime"));
                            holdtime = holdtime + (ev.getEndTimeAsDate().getSeconds() - startHoldTime.getSeconds());
                        } catch (ParseException | NullPointerException ex) {
                            //Logger.getLogger(WFManagerEventListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //kết thúc tính holdtime
                    try {
                        String site_id = current_call.get("site_id");
                        HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                        String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                        Callback c = new Callback(url);
                        c.addParam("secret", site.get("secret"));
                        c.addParam("callstatus", "CDR");
                        c.addParam("calluuid", ev.getUniqueId());
                        c.addParam("starttime", starttime);
                        c.addParam("answertime", answertime);
                        c.addParam("endtime", endtime);
                        c.addParam("totalduration", totalduration);
                        String sipcause = " ";
                        if (is_answer) {
                            c.addParam("billduration", billduration);
                            c.addParam("disposition", ev.getDisposition());
                            if (ev.getDisposition().equalsIgnoreCase(CdrEvent.DISPOSITION_ANSWERED)) {
                                c.addParam("hangup_by", hangup_by);
                                String monitor_filename = ev.getRecordfile();
                                if (monitor_filename != null) {
                                    if (!"".equals(monitor_filename)) {
                                        c.addParam("monitorfilename", monitor_filename);
                                    }
                                }
                            }
                        } else {
                            c.addParam("billduration", "0");
                            if (ev.getDisposition().equalsIgnoreCase(CdrEvent.DISPOSITION_ANSWERED)) {
                                c.addParam("disposition", CdrEvent.DISPOSITION_NO_ANSWER);
                            } else {
                                try {
                                    if (ev.getSipcause() != null) {
                                        sipcause = ev.getSipcause();
                                    }
                                } catch (Exception ex) {
                                    System.out.println(ex.getMessage());
                                }
                                if (sipcause.contains("503") || sipcause.contains("480") || sipcause.contains("487")) {
                                    c.addParam("disposition", CdrEvent.DISPOSITION_FAILED);
                                } else {
                                    c.addParam("disposition", ev.getDisposition());
                                }
                            }
                            c.addParam("hangup_by", "");
                        }
                        c.addParam("holdtime", holdtime + "");
                        c.addParam("calltype", ev.getCalltype());
                        c.addParam("dialid", ev.getDialid());
                        c.addParam("version", "3");
                        c.sendGet(receivedTimestamp0);
                    } catch (NullPointerException ex) {
                        //System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
                    }
                } else {
                    current_call = DBSelectionSet.getInstance().getCurrentCall(ev.getUniqueId());
                    if (current_call != null) {
                        String starttime = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getStartTimeAsDate());
                        String answertime;
                        try {
                            answertime = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getAnswerTimeAsDate());
                        } catch (Exception ex) {
                            answertime = "";
                        }
                        String endtime = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getEndTimeAsDate());
                        String site_id = current_call.get("site_id");
                        HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                        String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                        Callback c = new Callback(url);
                        c.addParam("secret", site.get("secret"));
                        c.addParam("callstatus", "CDR");
                        c.addParam("calluuid", current_call.get("transfered_uuid"));
                        c.addParam("starttime", starttime);
                        c.addParam("answertime", answertime);
                        c.addParam("endtime", endtime);
                        c.addParam("billduration", ev.getBillableSeconds().toString());
                        c.addParam("totalduration", ev.getDuration().toString());
                        c.addParam("holdtime", "0");
                        c.addParam("calltype", ev.getCalltype());
                        c.addParam("dialid", ev.getDialid());
                        String sipcause = " ";
                        if (sipcause.contains("503") || sipcause.contains("480") || sipcause.contains("487")) {
                            c.addParam("disposition", CdrEvent.DISPOSITION_FAILED);
                        } else {
                            c.addParam("disposition", ev.getDisposition());
                        }
                        if (ev.getDisposition().equalsIgnoreCase(CdrEvent.DISPOSITION_ANSWERED)) {
                            c.addParam("hangup_by", hangup_by);
                        }
                        c.addParam("version", "3");
                        c.sendGet(receivedTimestamp0);
                    } else if (ev.getCallback() != null && ev.getCallbacktype() != null) {
                        String channel = ev.getChannel();
                        if (channel.startsWith("SIP")) {
                            String callback = ev.getCallback();
                            String callbacktype = ev.getCallbacktype();
                            HashMap<String, String> site = DBSelectionSet.getInstance().getSiteByDesCRM(callback, callbacktype);
                            if (site != null) {
                                String callto = ev.getCallto();
                                String calluuid = ev.getUniqueId();
                                String starttime = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getStartTimeAsDate());
                                String endtime = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getEndTimeAsDate());
                                String url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                                Callback c = new Callback(url);
                                c.addParam("secret", site.get("secret"));
                                c.addParam("callstatus", "PutCDR");
                                c.addParam("calluuid", calluuid);
                                c.addParam("direction", "outbound");
                                c.addParam("callernumber", callback);
                                c.addParam("destinationnumber", callto);
                                c.addParam("starttime", starttime);
                                c.addParam("answertime", "");
                                c.addParam("endtime", endtime);
                                c.addParam("billduration", "0");
                                c.addParam("totalduration", ev.getDuration().toString());
                                c.addParam("calltype", ev.getCalltype());
                                String dialid = ev.getDialid();
                                c.addParam("dialid", dialid);
                                c.addParam("dnis", ev.getCallerId());
                                String sipcause = " ";
                                if (ev.getDisposition().equalsIgnoreCase(CdrEvent.DISPOSITION_ANSWERED)) {
                                    c.addParam("disposition", CdrEvent.DISPOSITION_NO_ANSWER);
                                } else {
                                    try {
                                        if (ev.getSipcause() != null) {
                                            sipcause = ev.getSipcause();
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                    if (sipcause.contains("503") || sipcause.contains("480") || sipcause.contains("487")) {
                                        c.addParam("disposition", CdrEvent.DISPOSITION_FAILED);
                                    } else {
                                        c.addParam("disposition", ev.getDisposition());
                                    }
                                }
                                c.addParam("version", "3");
                                c.sendGet(receivedTimestamp0);
                            }
                        }
                    }
                }
            } else {
                MyMemcached.getInstance().delete(ev.getUniqueId());
            }
        }
    }

    private void handleVarSetEvent(VarSetEvent ev) {
        if ("BLINDTRANSFER".equalsIgnoreCase(ev.getVariable())) {
            DBSelectionSet.getInstance().insertBlindTransfer(ev.getChannel(), ev.getValue(), ev.getUniqueId());
        }
    }

    private void handleQueueMemberAdd(QueueMemberAddedEvent ev, long receivedTimestamp0) throws URISyntaxException {
        String datereceived = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getDateReceived());
        String member = ev.getMemberName();
        String[] temp = member.split("/");
        String extension = temp[1];
        if (DBSelectionSet.getInstance().checkExtension(extension)) {
            String url;
            Callback c;
            String queue = ev.getQueue();
            ArrayList<HashMap> sites = DBSelectionSet.getInstance().getAllSite();
            for (HashMap<String, String> site : sites) {
                url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                c = new Callback(url);
                c.addParam("secret", site.get("secret"));
                c.addParam("callstatus", "QueueMemberAdd");
                c.addParam("queue", queue);
                c.addParam("member", extension);
                c.addParam("actiontime", datereceived);
                c.addParam("version", "3");
                c.sendGet(receivedTimestamp0);
            }
        }
    }

    private void handleQueueMemberRemove(QueueMemberRemovedEvent ev, long receivedTimestamp0) throws URISyntaxException {
        String datereceived = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getDateReceived());
        String member = ev.getMemberName();
        String[] temp = member.split("/");
        String extension = temp[1];
        if (DBSelectionSet.getInstance().checkExtension(extension)) {
            String url;
            Callback c;
            String queue = ev.getQueue();
            ArrayList<HashMap> sites = DBSelectionSet.getInstance().getAllSite();
            for (HashMap<String, String> site : sites) {
                url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                c = new Callback(url);
                c.addParam("secret", site.get("secret"));
                c.addParam("callstatus", "QueueMemberRemove");
                c.addParam("queue", queue);
                c.addParam("member", extension);
                c.addParam("actiontime", datereceived);
                c.addParam("version", "3");
                c.sendGet(receivedTimestamp0);
            }
        }
    }

    private void handleQueueMemberStatus(QueueMemberStatusEvent ev, long receivedTimestamp0) {
        try {
            // JobInternal event sync queue member status
            JobInternal jobInternal = new JobInternal();
            jobInternal.addParam("event", "QueueMemberStatus");
            jobInternal.addParam("timestamp", receivedTimestamp0 + "");
            jobInternal.addParam("queue", ev.getQueue());
            jobInternal.addParam("location", ev.getInterface());
            jobInternal.addParam("callstaken", ev.getCallsTaken().toString());
            jobInternal.addParam("lastcall", ev.getLastCall().toString());
            jobInternal.addParam("status", ev.getStatus().toString());
            jobInternal.addParam("paused", ev.getPaused().toString());
            jobInternal.addParam("membership", ev.getMembership());
            jobInternal.QueuePut();
        } catch (Exception ex) {
            //System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    private void handlePeerStatusEvent(PeerStatusEvent ev, long receivedTimestamp0) throws URISyntaxException {
        //System.out.println(ev.toString());
        String _channeltype = ev.getChannelType();
        String _peer = ev.getPeer();
        String _peerstatus = ev.getPeerStatus();
        String __port = "0";
        try {
            int _port = ev.getPort();
            __port = String.valueOf(_port);
        } catch (NumberFormatException | NullPointerException ex) {
            //System.out.println("Exception PeerStatus Port: " + ex.getMessage());
        }

        String _address = ev.getAddress();
        try {
            String[] arrAddr = _address.split(":");
            _address = arrAddr[0];
            if ("0".equals(__port) && arrAddr.length > 1) {
                __port = arrAddr[1];
            }
        } catch (ArrayIndexOutOfBoundsException | NullPointerException ex) {
            //System.out.println("Exception PeerStatus Address: " + ex.getMessage());
        }

        String peername = _peer.toLowerCase();
        String status = _peerstatus.toLowerCase();
        String pbx_server = Common.getIpAddress();

        String _username = "";
        String[] arrPeer = _peer.split("/");
        if (arrPeer.length > 1) {
            _username = arrPeer[1];
        }
        try {
            JobInternal jobPeer = new JobInternal();
            jobPeer.addParam("timestamp", "" + receivedTimestamp0);
            jobPeer.addParam("event", "PeerStatus");
            jobPeer.addParam("peername", peername);
            jobPeer.addParam("username", _username);
            jobPeer.addParam("host", _address);
            jobPeer.addParam("dyn", "d");
            jobPeer.addParam("nat", "n");
            jobPeer.addParam("port", __port);
            jobPeer.addParam("status", status);
            jobPeer.addParam("freshtime", new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(Calendar.getInstance().getTime()));
            jobPeer.addParam("protocol", _channeltype);
            jobPeer.addParam("pbxserver", pbx_server);
            jobPeer.QueuePut();
        } catch (Exception ex) {
            //System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    private void handleMusicOnHoldEvent(MusicOnHoldEvent ev) {
        String datereceived = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ev.getDateReceived());
        Object oCache = MyMemcached.getInstance().get("LocalBridgeEvent_" + ev.getUniqueId());
        if (oCache != null) {
            String calluuid_main = oCache.toString();
            if (ev.isStart()) {
                DBSelectionSet.getInstance().startHoldMain(calluuid_main, datereceived);
            }
            if (ev.isStop()) {
                DBSelectionSet.getInstance().stopHoldMain(calluuid_main, datereceived);
            }
        } else {
            // if(curcalldb.checkCallParent(ev.getUniqueId())){
            if (ev.isStart()) {
                DBSelectionSet.getInstance().startHold(ev.getUniqueId(), datereceived);
            }
            if (ev.isStop()) {
                DBSelectionSet.getInstance().stopHold(ev.getUniqueId(), datereceived);
            }
            // }
        }
    }

    private void handleRenameEvent(RenameEvent ev) {
        if (ev.getNewname().startsWith("Transfered")) {
            String channel = ev.getChannel().replace("<MASQ>", "");
            Object oCache = MyMemcached.getInstance().get(channel);
            if (oCache == null) {
                try {
                    HashMap<String, String> curcall = DBSelectionSet.getInstance().CDRbyCalluuid(ev.getUniqueId());
                    if (curcall == null) {
                        curcall = DBSelectionSet.getInstance().CDRbyChildCalluuid(ev.getUniqueId());
                    }
                    if (curcall != null) {
                        JSONObject data_transfer = new JSONObject();
                        data_transfer.put("site_id", curcall.get("site_id"));
                        data_transfer.put("child_calluuid", ev.getUniqueId());
                        data_transfer.put("parent_calluuid", curcall.get("call_uuid"));
                        MyMemcached.getInstance().set(channel, data_transfer.toString(), new Date(System.currentTimeMillis() + 5000));
                        MyMemcached.getInstance().set(curcall.get("call_uuid"), "parent_calluuid", new Date(System.currentTimeMillis() + 5000));
                    } else if (channel.endsWith(";2")) {
                        JSONObject data_transfer = new JSONObject();
                        data_transfer.put("site_id", "click_to_call_transfer");
                        MyMemcached.getInstance().set(channel.replace(";2", ";1"), data_transfer.toString(), new Date(System.currentTimeMillis() + 5000));
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(WFManagerEventListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void handleLocalBridgeEvent(LocalBridgeEvent ev) {
        MyMemcached.getInstance().set("LocalBridgeEvent_" + ev.getUniqueId2(), ev.getUniqueId1(), new Date(System.currentTimeMillis() + 1800000));
    }

    private void handleExtensionStatusEvent(ExtensionStatusEvent ev, CdrEvent cdrEv, long receivedTimestamp0) {
        try {
            JobInternal ext_status = new JobInternal();
            ext_status.addParam("event", "ExtensionStatus");
            ext_status.addParam("hint", ev.getHint());
            ext_status.addParam("exten", ev.getExten());
            ext_status.addParam("status", String.valueOf(ev.getStatus()));
            ext_status.addParam("status_ext", ev.getStatustext());
            ext_status.QueuePut();
            
            
            String datereceived = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getDateReceived());
            String url;
            Callback c;
            String site_id = DBSelectionSet.getInstance().getSiteIdBycalluuid(cdrEv.getUniqueId());
            if (site_id != null && !"".equals(site_id)) {
                HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                if (site != null) {
                    //------------------------------------------------
                    url = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                    c = new Callback(url);
                    c.addParam("event", "ExtensionStatus");
                    c.addParam("hint", ev.getHint());
                    c.addParam("exten", ev.getExten());
                    c.addParam("status", String.valueOf(ev.getStatus()));
                    c.addParam("status_ext", ev.getStatustext());
                    c.sendGet(receivedTimestamp0);
                }
            }
        } catch (Exception ex) {
            //System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    private void handleTransferEvent(TransferEvent ev) {
        String type = ev.getTransferType();
        String channel = ev.getChannel();
        String uniqueid = ev.getUniqueId();
        String targetuniqueid = ev.getTargetUniqueId();
        String targetchannel = ev.getTargetChannel();
        String exten = ev.getTransferExten();
        String context = ev.getTransferContext();
        DBSelectionSet.getInstance().insertTransferEvent(type, channel, uniqueid, targetuniqueid, targetchannel, exten, context);
    }

    public static void main(String[] args) throws Exception {
        WFManagerEventListener wfManagerEventListener;
        wfManagerEventListener = new WFManagerEventListener();
        wfManagerEventListener.startup();
        while (true) {
        }
    }

}
