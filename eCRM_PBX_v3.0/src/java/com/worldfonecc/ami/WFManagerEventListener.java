/*
 * Copyright © 2014 South Telecom
 */
package com.worldfonecc.ami;

import com.worldfonecc.queue.JobExternal;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import org.asteriskjava.manager.event.AbstractChannelEvent;
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
    
    // Khởi tạo Thread Pool với giới hạn 50 luồng đồng thời để tránh sập RAM/CPU
    private static final ExecutorService executorPool = Executors.newFixedThreadPool(50);
    
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
        managerConnection.login();
        managerConnection.registerUserEventClass(WorldfoneUserEvent.class);
        managerConnection.addEventListener(this);
        asteriskServer = new DefaultAsteriskServer(managerConnection);
        if (log) {
            logger.info("Manager Event Listener has startup");
        }
    }

    public void shutdown() throws Exception {
        if (managerConnection != null) {
            managerConnection.removeEventListener(this);
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

    private static String enc(String val) {
        if (val == null) return "";
        try {
            return URLEncoder.encode(val, StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            return val;
        }
    }

    @Override
    public void onManagerEvent(ManagerEvent event) {
        Runnable task = () -> {
            long receivedTimestamp0 = System.currentTimeMillis() / 1000;
            String eventName = event.getClass().getSimpleName();
            
            String uniqueId = "";
            if (event instanceof AbstractChannelEvent) {
                AbstractChannelEvent channelEvent = (AbstractChannelEvent) event;
                uniqueId = channelEvent.getUniqueId();
            }
            // Chỉ log những event quan trọng để tránh rác log
//            if(!eventName.equals("VarSetEvent") && !eventName.equals("RtcpReceivedEvent")) {
                System.out.println(uniqueId + " " + eventName + " " + System.currentTimeMillis() / 1000);
//            }
            
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
                        handleExtensionStatusEvent((ExtensionStatusEvent) event, receivedTimestamp0);
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
            } catch (Exception ex) {
                System.out.println("[DEBUG] ERROR processing " + eventName + ": " + ex.getMessage());
            }
        };
        executorPool.execute(task);
    }

//    private void handleCelEvent(CelEvent ev) throws URISyntaxException {
//        String linkedId = ev.getLinkedID();
//        String uniqueId = ev.getUniqueID();
//
//        if (linkedId == null) linkedId = "";
//        if (uniqueId == null) uniqueId = "";
// 
//        if (ev.getEventName() == null) return;
//
//        if (ev.getEventName().equalsIgnoreCase(CelEvent.CEL_EVENT_CHAN_START) && ev.getChannel() != null && !ev.getChannel().endsWith(";2")) {
//            DBSelectionSet.getInstance().updateLastChannel(linkedId, ev.getChannel());
//        } else if (ev.getEventName().equalsIgnoreCase(CelEvent.CEL_EVENT_BRIDGE_ENTER) && ev.getChannel() != null && !ev.getChannel().endsWith(";2") && ("Dial".equals(ev.getApplication()) || "Queue".equals(ev.getApplication()))) {
//            DBSelectionSet.getInstance().updateAnswerTimeByUuid(uniqueId, ev.getEventTime());
//
//            if (ev.getPeer() != null && !"".equals(ev.getPeer()) && (ev.getPeer().startsWith("SIP") || ev.getPeer().startsWith("OOH323"))) {
//                DBSelectionSet.getInstance().updateLastChannel(linkedId, ev.getPeer());
//            }
//        }
//    }
    
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
        if ("Ringing".equalsIgnoreCase(ev.getChannelStateDesc())) {
            System.out.println("[DEBUG] NewStateEvent -> Channel is Ringing. UniqueID: " + ev.getUniqueId());

            boolean chkSIP = ev.getChannel() != null && (ev.getChannel().startsWith("SIP") || ev.getChannel().startsWith("OOH323") || ev.getChannel().startsWith("PJSIP"));
            if (!chkSIP) {
                return;
            }

            String datereceived = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getDateReceived());
            String callerNum = ev.getCallerIdNum() != null ? ev.getCallerIdNum().trim() : "";
            String connectedNum = ev.getConnectedLineNum() != null ? ev.getConnectedLineNum().trim() : "";

            // 1. Kiểm tra Outbound Call (Extension gọi ra ngoài)
            HashMap<String, String> curCDR = DBSelectionSet.getInstance().CDRbyCalluuid(ev.getUniqueId());
            if (curCDR != null) {
                String site_id = curCDR.get("site_id");
                if (site_id != null && !site_id.isEmpty()) {
                    HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                    if (site != null) {
                        String baseUrl = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                        String calltype = curCDR.get("calltype") != null ? curCDR.get("calltype") : "Outbound_non-ACD";
                        String destNum = connectedNum.isEmpty() ? curCDR.get("destinationnumber") : connectedNum;

                        String fullUrl = baseUrl + "?secret=" + enc(site.get("secret"))
                                + "&callstatus=Ringing"
                                + "&calluuid=" + enc(ev.getUniqueId())
                                + "&direction=outbound"
                                + "&callernumber=" + enc(callerNum)
                                + "&destinationnumber=" + enc(destNum)
                                + "&starttime=" + enc(datereceived)
                                + "&receivedtime=" + enc(datereceived)
                                + "&agentname=" + enc(ev.getCallerIdName())
                                + "&calltype=" + enc(calltype)
                                + "&version=3";

                        JobExternal job = new JobExternal();
                        job.addParam("url", fullUrl);
                        job.addParam("method", "get");
                        job.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                        job.addParam("headers", "");
                        job.QueuePut();

                        System.out.println("[DEBUG] JobExternal QueuePut Ringing Outbound (UniqueID: " + ev.getUniqueId() + ")");
                        return;
                    }
                }
            }

            // 2. Inbound Call (Khách ngoài / Queue gọi vào Extension)
            boolean chkNum = DBSelectionSet.getInstance().checkNumber(connectedNum);
            boolean chkDial = !DBSelectionSet.getInstance().checkDialing(ev.getUniqueId());
            boolean chkExtCaller = DBSelectionSet.getInstance().checkExtension(callerNum);
            boolean chkExtConn = !DBSelectionSet.getInstance().checkExtension(connectedNum);

            if (chkNum && chkDial && chkExtCaller && chkExtConn) {
                String parentcalluuid = DBSelectionSet.getInstance().getCalluuid(connectedNum);
                HashMap<String, String> parentCDR = DBSelectionSet.getInstance().CDRbyCalluuid(parentcalluuid);

                if (parentCDR != null) {
                    String site_id = parentCDR.get("site_id");
                    HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);

                    if (site != null) {
                        DBSelectionSet.getInstance().logExtension(ev.getUniqueId(), "inbound", site.get("site_id"), parentcalluuid);

                        String baseUrl = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                        String fullUrl = baseUrl + "?secret=" + enc(site.get("secret"))
                                + "&callstatus=Ringing"
                                + "&calluuid=" + enc(ev.getUniqueId())
                                + "&direction=inbound"
                                + "&callernumber=" + enc(connectedNum)
                                + "&destinationnumber=" + enc(callerNum)
                                + "&starttime=" + enc(datereceived)
                                + "&receivedtime=" + enc(datereceived)
                                + "&parentcalluuid=" + enc(parentcalluuid)
                                + "&agentname=" + enc(ev.getCallerIdName())
                                + "&calltype=" + enc(parentCDR.get("calltype"))
                                + "&version=3";

                        JobExternal job = new JobExternal();
                        job.addParam("url", fullUrl);
                        job.addParam("method", "get");
                        job.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                        job.addParam("headers", "");
                        job.QueuePut();

                        System.out.println("[DEBUG] JobExternal QueuePut Ringing Inbound (UniqueID: " + ev.getUniqueId() + ")");
                    }
                } else {
                    System.out.println("[DEBUG] FAILED: parentCDR is NULL for Ringing. parentcalluuid: " + parentcalluuid);
                }
            } else {
                System.out.println("[DEBUG] FAILED conditions for Inbound Ringing. UniqueID: " + ev.getUniqueId());
            }
        }
    }

    private void handleBridgeEvent(BridgeEvent ev, long receivedTimestamp0) throws URISyntaxException {
        if (ev.isLink() && !ev.getChannel1().endsWith(";2")) {
            System.out.println("[DEBUG] BridgeEvent isLink -> DialAnswer. UniqueID1: " + ev.getUniqueId1());
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
                String dialid = DBSelectionSet.getInstance().getDialId(calluuid);

                String baseUrl = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                String fullUrl = baseUrl + "?secret=" + enc(site.get("secret"))
                        + "&callstatus=DialAnswer"
                        + "&calluuid=" + enc(calluuid)
                        + "&childcalluuid=" + enc(ev.getUniqueId2())
                        + "&callernumber=" + enc(callernumber2)
                        + "&answertime=" + enc(answertime)
                        + (agentname != null && !agentname.isEmpty() ? "&agentname=" + enc(agentname) : "")
                        + "&destinationnumber=" + enc(call_des)
                        + "&dialid=" + enc(dialid)
                        + "&version=3";

                JobExternal job = new JobExternal();
                job.addParam("url", fullUrl);
                job.addParam("method", "get");
                job.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                job.addParam("headers", "");
                job.QueuePut();

                System.out.println("[DEBUG] JobExternal QueuePut DialAnswer (UniqueID: " + calluuid + ")");
            } else {
                System.out.println("[DEBUG] FAILED to process DialAnswer directly. oCache_answer=" + (oCache_answer!=null) + ", curcdr!=null=" + (curcdr!=null));
                Object oCache = MyMemcached.getInstance().get(ev.getChannel1());
                if (oCache != null) {
                    try {
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
                                
                                String baseUrl = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                                String fullUrl = baseUrl + "?secret=" + enc(site.get("secret"))
                                        + "&callstatus=DialAnswerTransfer"
                                        + "&calluuid=" + enc(calluuid)
                                        + "&childcalluuid=" + enc(ev.getUniqueId2())
                                        + "&callernumber=" + enc(callernumber2)
                                        + "&answertime=" + enc(answertime)
                                        + "&agentname=" + enc(agentname)
                                        + "&destinationnumber=" + enc(call_des)
                                        + "&version=3";

                                JobExternal job = new JobExternal();
                                job.addParam("url", fullUrl);
                                job.addParam("method", "get");
                                job.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                                job.addParam("headers", "");
                                job.QueuePut();

                                System.out.println("[DEBUG] JobExternal QueuePut DialAnswerTransfer (Click2Call) (UniqueID: " + calluuid + ")");
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

                            String baseUrl = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                            String fullUrl = baseUrl + "?secret=" + enc(site.get("secret"))
                                    + "&callstatus=DialAnswerTransfer"
                                    + "&calluuid=" + enc(calluuid)
                                    + "&childcalluuid=" + enc(child_calluuid)
                                    + "&callernumber=" + enc(callernumber2)
                                    + "&answertime=" + enc(answertime)
                                    + "&agentname=" + enc(agentname)
                                    + "&destinationnumber=" + enc(call_des)
                                    + "&version=3";

                            JobExternal job = new JobExternal();
                            job.addParam("url", fullUrl);
                            job.addParam("method", "get");
                            job.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                            job.addParam("headers", "");
                            job.QueuePut();

                            System.out.println("[DEBUG] JobExternal QueuePut DialAnswerTransfer (UniqueID: " + calluuid + ")");
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
        System.out.println("[DEBUG] HangupEvent triggered for UniqueID: " + ev.getUniqueId());

        String childcalluuid = DBSelectionSet.getInstance().checkCallHangup(ev.getUniqueId());
        if (childcalluuid != null) {
            String datereceived = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getDateReceived());
            
            // 1. Lấy site_id trực tiếp theo UUID hiện tại
            String site_id = DBSelectionSet.getInstance().getSiteIdBycalluuid(ev.getUniqueId());
            String parentcalluuid = null;

            // 2. Fallback tìm theo parent UUID nếu là leg con (trunk)
            if (site_id == null || "".equals(site_id)) {
                parentcalluuid = DBSelectionSet.getInstance().getParentCalluuid(ev.getUniqueId());
                if (parentcalluuid != null && !"".equals(parentcalluuid)) {
                    site_id = DBSelectionSet.getInstance().getSiteIdBycalluuid(parentcalluuid);
                }
            }

            // 3. Fallback tiếp từ CDR nếu chưa thấy
            if (site_id == null || "".equals(site_id)) {
                HashMap<String, String> cdr = DBSelectionSet.getInstance().CDRbyCalluuid(ev.getUniqueId());
                if (cdr == null) {
                    cdr = DBSelectionSet.getInstance().CDRbyChildCalluuid(ev.getUniqueId());
                }
                if (cdr != null && cdr.containsKey("site_id")) {
                    site_id = cdr.get("site_id");
                    if (parentcalluuid == null && cdr.containsKey("parent_calluuid")) {
                        parentcalluuid = cdr.get("parent_calluuid");
                    }
                }
            }

            if (site_id != null && !"".equals(site_id)) {
                HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                if (site != null) {
                    String baseUrl = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                    
                    if (DBSelectionSet.getInstance().checkUuid(ev.getUniqueId())) {
                        DBSelectionSet.getInstance().updateCall(ev.getUniqueId(), "HangUp");
                        
                        String fullUrlTrim = baseUrl + "?secret=" + enc(site.get("secret"))
                                + "&callstatus=Trim"
                                + "&calluuid=" + enc(ev.getUniqueId())
                                + "&version=3";

                        JobExternal jobTrim = new JobExternal();
                        jobTrim.addParam("url", fullUrlTrim);
                        jobTrim.addParam("method", "get");
                        jobTrim.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                        jobTrim.addParam("headers", "");
                        jobTrim.QueuePut();
                        System.out.println("[DEBUG] JobExternal QueuePut Trim (UniqueID: " + ev.getUniqueId() + ")");

                        if (DBSelectionSet.getInstance().checkAnswerChilduuid(ev.getUniqueId())) {
                            if (parentcalluuid == null || "".equals(parentcalluuid)) {
                                parentcalluuid = DBSelectionSet.getInstance().getParentCalluuid(ev.getUniqueId());
                            }
                            
                            String fullUrlHangup = baseUrl + "?secret=" + enc(site.get("secret"))
                                    + "&callstatus=HangUp"
                                    + "&calluuid=" + enc(parentcalluuid)
                                    + "&datereceived=" + enc(datereceived)
                                    + "&causetxt=" + enc(ev.getCause() != null ? ev.getCause().toString() : "");

                            JobExternal jobHangup = new JobExternal();
                            jobHangup.addParam("url", fullUrlHangup);
                            jobHangup.addParam("method", "get");
                            jobHangup.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                            jobHangup.addParam("headers", "");
                            jobHangup.QueuePut();
                            System.out.println("[DEBUG] JobExternal QueuePut HangUp (Parent UniqueID: " + parentcalluuid + ")");
                        }
                    } else {
                        if (!ev.getChannel().startsWith("Transfered")) {
                            DBSelectionSet.getInstance().updateCall(ev.getUniqueId(), "HangUp");
                        }
                        DBSelectionSet.getInstance().deleteCallLog(ev.getConnectedLineNum(), ev.getCallerIdNum());
                        DBSelectionSet.getInstance().deleteCallLogbyUUid(ev.getUniqueId());

                        String fullUrlHangup = baseUrl + "?secret=" + enc(site.get("secret"))
                                + "&callstatus=HangUp"
                                + "&calluuid=" + enc(ev.getUniqueId())
                                + "&datereceived=" + enc(datereceived)
                                + "&causetxt=" + enc(ev.getCause() != null ? ev.getCause().toString() : "")
                                + "&version=3";

                        JobExternal jobHangup = new JobExternal();
                        jobHangup.addParam("url", fullUrlHangup);
                        jobHangup.addParam("method", "get");
                        jobHangup.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                        jobHangup.addParam("headers", "");
                        jobHangup.QueuePut();
                        System.out.println("[DEBUG] JobExternal QueuePut HangUp (UniqueID: " + ev.getUniqueId() + ")");
                    }
                }
            } else {
                System.out.println("[DEBUG] Hangup FAILED: site_id is null/empty for UniqueID: " + ev.getUniqueId());
            }
        } else {
            System.out.println("[DEBUG] Hangup FAILED: childcalluuid is null (checkCallHangup returned null) for UniqueID: " + ev.getUniqueId());
        }
        
        Object oCache = MyMemcached.getInstance().get("LocalBridgeEvent_" + ev.getUniqueId());
        if (oCache != null) {
            MyMemcached.getInstance().delete("LocalBridgeEvent_" + ev.getUniqueId());
        }
    }

    private void handleCdrEvent(CdrEvent ev, long receivedTimestamp0) throws URISyntaxException {
        System.out.println("[DEBUG] Processing CdrEvent -> UniqueID: " + ev.getUniqueId() + " | Disposition: " + ev.getDisposition());
        try {
            JobCdrEvent jobCdr = new JobCdrEvent();
            jobCdr.addParam("event", "CDR");
            jobCdr.addParam("uniqueid", ev.getUniqueId());
            System.out.println("[DEBUG] QueuePut CDR sync");

            jobCdr.QueuePut(2);
        } catch (Exception e) {
            logger.error(e);
        }
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

        try {
            if (ev.getDestinationChannel() == null || "".equals(ev.getDestinationChannel())) {
                String[] lastApps = new String[]{"agi", "goto", "read", "hangup"};
                if (Arrays.stream(lastApps).anyMatch(ev.getLastApplication().trim().toLowerCase()::equals) && ev.getDestinationContext().contains("trunk")) {
                    return;
                }
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
                    boolean is_answer = "1".equals(current_call.get("dialing_start_time"));
                    DBSelectionSet.getInstance().deleteCallLog(ev.getCallerId(), ev.getDestination());
                    DBSelectionSet.getInstance().deleteCallLogbyUUid(ev.getUniqueId());
                    DBSelectionSet.getInstance().updateCall(ev.getUniqueId(), "CdrEvent");
                    DBSelectionSet.getInstance().deleteCallHold(ev.getUniqueId());
                    int stopHold = 0;
                    int holdtime = 0;
                    try {
                        stopHold = Integer.parseInt(current_call.get("stopHold"));
                        holdtime = Integer.parseInt(current_call.get("holdtime"));
                    } catch (NullPointerException | NumberFormatException ex) {
                    }
                    if (stopHold == 2) {
                        SimpleDateFormat dateValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date startHoldTime;
                        try {
                            startHoldTime = dateValue.parse(current_call.get("startholdtime"));
                            holdtime = holdtime + (ev.getEndTimeAsDate().getSeconds() - startHoldTime.getSeconds());
                        } catch (ParseException | NullPointerException ex) {
                        }
                    }
                    try {
                        String site_id = current_call.get("site_id");
                        HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                        String baseUrl = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                        
                        String disposition = ev.getDisposition();
                        String monitorParam = "";
                        String billdurationVal = "0";

                        if (is_answer) {
                            billdurationVal = billduration;
                            if (ev.getDisposition().equalsIgnoreCase(CdrEvent.DISPOSITION_ANSWERED)) {
                                String monitor_filename = ev.getRecordfile();
                                if (monitor_filename != null && !"".equals(monitor_filename)) {
                                    monitorParam = "&monitorfilename=" + enc(monitor_filename);
                                }
                            }
                        } else {
                            if (ev.getDisposition().equalsIgnoreCase(CdrEvent.DISPOSITION_ANSWERED)) {
                                disposition = CdrEvent.DISPOSITION_NO_ANSWER;
                            } else {
                                String sipcause = " ";
                                try {
                                    if (ev.getSipcause() != null) {
                                        sipcause = ev.getSipcause();
                                    }
                                } catch (Exception ex) {
                                    System.out.println(ex.getMessage());
                                }
                                if (sipcause.contains("503") || sipcause.contains("480") || sipcause.contains("487")) {
                                    disposition = CdrEvent.DISPOSITION_FAILED;
                                }
                            }
                            hangup_by = "";
                        }

                        String fullUrl = baseUrl + "?secret=" + enc(site.get("secret"))
                                + "&callstatus=CDR"
                                + "&calluuid=" + enc(ev.getUniqueId())
                                + "&starttime=" + enc(starttime)
                                + "&answertime=" + enc(answertime)
                                + "&endtime=" + enc(endtime)
                                + "&totalduration=" + enc(totalduration)
                                + "&billduration=" + enc(billdurationVal)
                                + "&disposition=" + enc(disposition)
                                + "&hangup_by=" + enc(hangup_by)
                                + monitorParam
                                + "&holdtime=" + enc(String.valueOf(holdtime))
                                + "&calltype=" + enc(ev.getCalltype())
                                + "&dialid=" + enc(ev.getDialid())
                                + "&version=3";

                        if (!ev.getLastApplication().trim().toLowerCase().contains("musiconhold")) {
                            JobExternal jobCdrExt = new JobExternal();
                            jobCdrExt.addParam("url", fullUrl);
                            jobCdrExt.addParam("method", "get");
                            jobCdrExt.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                            jobCdrExt.addParam("headers", "");
                            jobCdrExt.QueuePut();
                            System.out.println("[DEBUG] JobExternal QueuePut CDR (UniqueID: " + ev.getUniqueId() + ")");
                        }
                    } catch (NullPointerException ex) {
                    }
                } else {
                    current_call = DBSelectionSet.getInstance().getCurrentCall(ev.getUniqueId());
                    if (current_call != null) {
                        
                        // Kiểm tra null trước khi format
                        SimpleDateFormat printtime = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
                        String starttime = ev.getStartTimeAsDate() != null ? printtime.format(ev.getStartTimeAsDate()) : "";
                        String answertime = ev.getAnswerTimeAsDate() != null ? printtime.format(ev.getAnswerTimeAsDate()) : "";
                        String endtime = ev.getEndTimeAsDate() != null ? printtime.format(ev.getEndTimeAsDate()) : "";
                        
                        String site_id = current_call.get("site_id");
                        HashMap<String, String> site = DBSelectionSet.getInstance().getSiteById(site_id);
                        String baseUrl = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");

                        String disposition = ev.getDisposition();
                        String sipcause = " ";
                        if (sipcause.contains("503") || sipcause.contains("480") || sipcause.contains("487")) {
                            disposition = CdrEvent.DISPOSITION_FAILED;
                        }

                        String fullUrl = baseUrl + "?secret=" + enc(site.get("secret"))
                                + "&callstatus=CDR"
                                + "&calluuid=" + enc(current_call.get("transfered_uuid"))
                                + "&starttime=" + enc(starttime)
                                + "&answertime=" + enc(answertime)
                                + "&endtime=" + enc(endtime)
                                + "&billduration=" + enc(ev.getBillableSeconds().toString())
                                + "&totalduration=" + enc(ev.getDuration().toString())
                                + "&holdtime=0"
                                + "&calltype=" + enc(ev.getCalltype())
                                + "&dialid=" + enc(ev.getDialid())
                                + "&disposition=" + enc(disposition)
                                + (ev.getDisposition().equalsIgnoreCase(CdrEvent.DISPOSITION_ANSWERED) ? "&hangup_by=" + enc(hangup_by) : "")
                                + "&version=3";

                        JobExternal jobCdrExt = new JobExternal();
                        jobCdrExt.addParam("url", fullUrl);
                        jobCdrExt.addParam("method", "get");
                        jobCdrExt.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                        jobCdrExt.addParam("headers", "");
                        jobCdrExt.QueuePut();
                        System.out.println("[DEBUG] JobExternal QueuePut CDR (Transfered UUID)");
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
                                String baseUrl = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");

                                String disposition = ev.getDisposition();
                                if (ev.getDisposition().equalsIgnoreCase(CdrEvent.DISPOSITION_ANSWERED)) {
                                    disposition = CdrEvent.DISPOSITION_NO_ANSWER;
                                } else {
                                    String sipcause = " ";
                                    try {
                                        if (ev.getSipcause() != null) {
                                            sipcause = ev.getSipcause();
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                    if (sipcause.contains("503") || sipcause.contains("480") || sipcause.contains("487")) {
                                        disposition = CdrEvent.DISPOSITION_FAILED;
                                    }
                                }

                                String fullUrl = baseUrl + "?secret=" + enc(site.get("secret"))
                                        + "&callstatus=PutCDR"
                                        + "&calluuid=" + enc(calluuid)
                                        + "&direction=outbound"
                                        + "&callernumber=" + enc(callback)
                                        + "&destinationnumber=" + enc(callto)
                                        + "&starttime=" + enc(starttime)
                                        + "&answertime="
                                        + "&endtime=" + enc(endtime)
                                        + "&billduration=0"
                                        + "&totalduration=" + enc(ev.getDuration().toString())
                                        + "&calltype=" + enc(ev.getCalltype())
                                        + "&dialid=" + enc(ev.getDialid())
                                        + "&dnis=" + enc(ev.getCallerId())
                                        + "&disposition=" + enc(disposition)
                                        + "&version=3";

                                JobExternal jobCdrExt = new JobExternal();
                                jobCdrExt.addParam("url", fullUrl);
                                jobCdrExt.addParam("method", "get");
                                jobCdrExt.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                                jobCdrExt.addParam("headers", "");
                                jobCdrExt.QueuePut();
                                System.out.println("[DEBUG] JobExternal QueuePut PutCDR (Outbound Callback)");
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
            String queue = ev.getQueue();
            ArrayList<HashMap> sites = DBSelectionSet.getInstance().getAllSite();
            for (HashMap<String, String> site : sites) {
                String baseUrl = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                String fullUrl = baseUrl + "?secret=" + enc(site.get("secret"))
                        + "&callstatus=QueueMemberAdd"
                        + "&queue=" + enc(queue)
                        + "&member=" + enc(extension)
                        + "&actiontime=" + enc(datereceived)
                        + "&version=3";

                JobExternal job = new JobExternal();
                job.addParam("url", fullUrl);
                job.addParam("method", "get");
                job.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                job.addParam("headers", "");
                job.QueuePut();
            }
        }
    }

    private void handleQueueMemberRemove(QueueMemberRemovedEvent ev, long receivedTimestamp0) throws URISyntaxException {
        String datereceived = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(ev.getDateReceived());
        String member = ev.getMemberName();
        String[] temp = member.split("/");
        String extension = temp[1];
        if (DBSelectionSet.getInstance().checkExtension(extension)) {
            String queue = ev.getQueue();
            ArrayList<HashMap> sites = DBSelectionSet.getInstance().getAllSite();
            for (HashMap<String, String> site : sites) {
                String baseUrl = site.get("callback_url").contains("http") ? site.get("callback_url") : "http://" + site.get("callback_url");
                String fullUrl = baseUrl + "?secret=" + enc(site.get("secret"))
                        + "&callstatus=QueueMemberRemove"
                        + "&queue=" + enc(queue)
                        + "&member=" + enc(extension)
                        + "&actiontime=" + enc(datereceived)
                        + "&version=3";

                JobExternal job = new JobExternal();
                job.addParam("url", fullUrl);
                job.addParam("method", "get");
                job.addParam("receivedTimestamp", String.valueOf(receivedTimestamp0));
                job.addParam("headers", "");
                job.QueuePut();
            }
        }
    }

    private void handleQueueMemberStatus(QueueMemberStatusEvent ev, long receivedTimestamp0) {
        try {
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
        }
    }

    private void handlePeerStatusEvent(PeerStatusEvent ev, long receivedTimestamp0) throws URISyntaxException {
        String _channeltype = ev.getChannelType();
        String _peer = ev.getPeer();
        String _peerstatus = ev.getPeerStatus();
        String __port = "0";
        try {
            int _port = ev.getPort();
            __port = String.valueOf(_port);
        } catch (NumberFormatException | NullPointerException ex) {
        }

        String _address = ev.getAddress();
        try {
            String[] arrAddr = _address.split(":");
            _address = arrAddr[0];
            if ("0".equals(__port) && arrAddr.length > 1) {
                __port = arrAddr[1];
            }
        } catch (ArrayIndexOutOfBoundsException | NullPointerException ex) {
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
            if (ev.isStart()) {
                DBSelectionSet.getInstance().startHold(ev.getUniqueId(), datereceived);
            }
            if (ev.isStop()) {
                DBSelectionSet.getInstance().stopHold(ev.getUniqueId(), datereceived);
            }
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

    private void handleExtensionStatusEvent(ExtensionStatusEvent ev, long receivedTimestamp0) {
        try {
            JobInternal ext_status = new JobInternal();
            ext_status.addParam("event", "ExtensionStatus");
            ext_status.addParam("hint", ev.getHint());
            ext_status.addParam("exten", ev.getExten());
            ext_status.addParam("status", String.valueOf(ev.getStatus()));    
            ext_status.addParam("status_ext", ev.getStatustext());
            ext_status.QueuePut();
        } catch (Exception ex) {
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