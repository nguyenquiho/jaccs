/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfonecc.admin;

import com.worldfonecc.cache.MyMemcached;
import com.worldfonecc.utils.helpers.Common;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.exceptions.JedisException;

/**
 *
 * @author MitsuyoRai
 */
public class DBSelectionSet {

    private static volatile DBSelectionSet instance;

    public static DBSelectionSet getInstance() {
        DBSelectionSet _instance = DBSelectionSet.instance;
        if (_instance == null) {
            synchronized (DBSelectionSet.class) {
                _instance = DBSelectionSet.instance;
                if (_instance == null) {
                    DBSelectionSet.instance = _instance = new DBSelectionSet();
                }
            }
        }
        return _instance;
    }

    private DBSelectionSet() {
    }

    public ArrayList<HashMap> getAllSite() {
        try {
            Set<String> allSites = RedisConnection.getInstance().keys("pbx_con_crm_sites:*");
            if (allSites != null) {
                ArrayList<HashMap> sites = new ArrayList<>();
                allSites.stream().map((site) -> RedisConnection.getInstance().HMGet(site, "site_id", "secret", "callback_url", "crm_type", "crm_external_version", "connect_info", "report_voicemail", "callcenter_type", "callcenter_type_text", "mailbox")).map((crm) -> {
                    HashMap<String, String> row = new HashMap<>();
                    row.put("site_id", crm.get(0));
                    row.put("secret", crm.get(1));
                    row.put("callback_url", crm.get(2));
                    row.put("crm_type", crm.get(3));
                    row.put("crm_external_version", crm.get(4));
                    row.put("connect_info", crm.get(5));
                    row.put("report_voicemail", crm.get(6));
                    row.put("callcenter_type", crm.get(7));
                    row.put("callcenter_type_text", crm.get(8));
                    row.put("mailbox", crm.get(9));
                    return row;
                }).forEachOrdered((row) -> {
                    sites.add(row);
                });
                return sites;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getListByQuery("SELECT * FROM `con_crm_sites`");
    }

    /**
     * getCrmDestionationByName -> get crm destination by name
     *
     * @param dest_id
     * @param dest_type
     * @return HashMap
     */
    public HashMap<String, String> getCrmDestionationByName(String dest_id, String dest_type) {
        try {
            List<String> crm_dest = RedisConnection.getInstance().HMGet("pbx_crm_destination_" + dest_type + "_" + dest_id, "id", "dest_type", "dest_id", "site_id", "name", "callcenter_type");
            if (crm_dest.get(0) != null) {
                HashMap<String, String> dest = new HashMap();
                dest.put("id", crm_dest.get(0));
                dest.put("dest_type", crm_dest.get(1));
                dest.put("dest_id", crm_dest.get(2));
                dest.put("site_id", crm_dest.get(3));
                dest.put("name", crm_dest.get(4));
                dest.put("callcenter_type", crm_dest.get(5));
                return dest;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `pbx_crm_destination` WHERE `dest_id`=? AND `dest_type`=?", dest_id, dest_type);
    }

    /**
     * CDRbyCalluuid -> get current event by call id
     *
     * @param calluuid
     * @return HashMap
     */
    public HashMap<String, String> CDRbyCalluuid(String calluuid) {
        HashMap<String, String> curevent = null;
        String cache_key = "con_crm_curevents_call_uuid_" + calluuid;
        if (MyMemcached.getInstance().get(cache_key) != null) {
            try {
                curevent = Common.JSONToHashMap(MyMemcached.getInstance().get(cache_key).toString());
            } catch (JSONException ex) {
                System.out.println(ex.getMessage());
            }
        }
        if (curevent == null || curevent.isEmpty() || curevent.get("site_id") == null) {
            curevent = MysqlConnection.getInstance().getOneByQuery("SELECT `id`, `parent_calluuid`, `call_status`, `direction`, `site_id`, `calltype`, `call_uuid` FROM `con_crm_curevents` WHERE `call_uuid`=? LIMIT 1", calluuid);
        }
        return curevent;
    }

    /**
     * CDRbyChildCalluuid -> get current event by child call id
     *
     * @param calluuid
     * @return HashMap
     */
    public HashMap<String, String> CDRbyChildCalluuid(String calluuid) {
        return MysqlConnection.getInstance().getOneByQuery("SELECT `call_uuid`,`id`,`parent_calluuid`,`call_status`,`site_id`,`calltype` FROM `con_crm_curevents` WHERE `childcalluuid`=?", calluuid);
    }

    /**
     * getSiteById -> get site by id
     *
     * @param id
     * @return ArrayList
     */
    public HashMap<String, String> getSiteById(String id) {
        try {
            List<String> crm = RedisConnection.getInstance().HMGet("pbx_con_crm_sites:" + id, "site_id", "secret", "callback_url", "crm_type", "crm_external_version", "connect_info", "report_voicemail", "callcenter_type", "callcenter_type_text", "mailbox");
            if (crm.get(0) != null) {
                HashMap<String, String> site = new HashMap<>();
                site.put("site_id", crm.get(0));
                site.put("secret", crm.get(1));
                site.put("callback_url", crm.get(2));
                site.put("crm_type", crm.get(3));
                site.put("crm_external_version", crm.get(4));
                site.put("connect_info", crm.get(5));
                site.put("report_voicemail", crm.get(6));
                site.put("callcenter_type", crm.get(7));
                site.put("callcenter_type_text", crm.get(8));
                site.put("mailbox", crm.get(9));
                return site;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `con_crm_sites` WHERE `site_id`=? LIMIT 1", id);
    }

    /**
     * updateCallDialAnswer ->update call dial answer
     *
     * @param calluuid
     * @param event
     * @param childcalluuid
     * @param channel1
     * @param channel2
     * @param curcdr
     * @return boolean
     */
    public boolean updateCallDialAnswer(String calluuid, String event, String childcalluuid, String channel1, String channel2, HashMap curcdr) {
        try {
            Date dt = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(dt);
            HashMap data_cache = new HashMap();
            data_cache.put("call_uuid", calluuid);
            data_cache.put("call_status", "DialAnswer");
            data_cache.put("direction", curcdr.get("direction"));
            data_cache.put("site_id", curcdr.get("site_id"));
            data_cache.put("updated_datetime", currentTime);
            data_cache.put("calltype", curcdr.get("calltype"));
            data_cache.put("childcalluuid", childcalluuid);
            data_cache.put("dialing_start_time", "1");
            data_cache.put("channel1", channel1);
            data_cache.put("channel2", channel2);
            data_cache.put("answertime", currentTime);
            String cache_key = "con_crm_curevents_call_uuid_" + calluuid;
            String json = JSONObject.valueToString(data_cache);
            MyMemcached.getInstance().update(cache_key, json);
        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
        }
        return MysqlConnection.getInstance().executeUpdateQuery("UPDATE `con_crm_curevents` SET `call_status`=?, `childcalluuid`=?, `dialing_start_time`=1, `channel1`=? , `channel2`=?, `answertime`=NOW() WHERE `call_uuid`=?", event, childcalluuid, channel1, channel2, calluuid);
    }

    public boolean updateAnswerTimeByUuid(String calluuid, String time) {
        return MysqlConnection.getInstance().executeUpdateQuery("UPDATE `con_crm_curevents` SET `answertime`=? WHERE `call_uuid`=?", time, calluuid);
    }

    /**
     * updateAnswerUuid -> update answer uuid incoming
     *
     * @param calluuid
     * @param childcalluuid
     * @return boolean
     */
    public boolean updateAnswerUuid(String calluuid, String childcalluuid) {
        return MysqlConnection.getInstance().executeUpdateQuery("UPDATE `cur_external_incoming` SET `answer_uuid`=?  WHERE `calluuid`=?", childcalluuid, calluuid);
    }

    /**
     * getAgentName -> get agent name by extension
     *
     * @param extension
     * @return String
     */
    public String getAgentName(String extension) {
        String agentname = null;
        try {
            agentname = RedisConnection.getInstance().HGet("pbx_sipusers_" + extension, "callername");
        } catch (JedisException ex) {
            System.out.println(ex.getMessage());
        }
        if (agentname == null) {
            agentname = MysqlConnection.getInstance().getColumnByQuery("SELECT `callername` FROM `sipusers` WHERE `extension`=? LIMIT 1", "callername", extension);
        }
        return agentname != null ? agentname : "";
    }

    /**
     * getCurrentCallBycalluuid
     *
     * @param calluuid
     * @return HashMap
     */
    public HashMap<String, String> getCurrentCallBycalluuid(String calluuid) {
        return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `con_crm_curevents` WHERE `call_uuid`=? LIMIT 1", calluuid);
    }

    /**
     * deleteCallLog -> delete current external incoming by phone number
     *
     * @param number
     * @return boolean
     */
    public boolean deleteCallLog(String... number) {
        if (number != null) {
            String numbers = String.join("','", Arrays.asList(number));
            return MysqlConnection.getInstance().executeUpdateQuery("DELETE FROM `cur_external_incoming` WHERE `number` IN ('" + numbers + "') AND `skip`=0");
        }
        return false;
    }

    /**
     * deleteCallLog -> delete current external incoming by call id
     *
     * @param calluuid
     * @return boolean
     */
    public boolean deleteCallLogbyUUid(String calluuid) {
        if (calluuid != null) {
            return MysqlConnection.getInstance().executeUpdateQuery("DELETE FROM `cur_external_incoming` WHERE `calluuid`=? AND `skip`=0", calluuid);
        }
        return false;
    }

    /**
     * updateCall -> update current event by call id
     *
     * @param calluuid
     * @param event
     * @return boolean
     */
    public boolean updateCall(String calluuid, String event) {
        return MysqlConnection.getInstance().executeUpdateQuery("UPDATE `con_crm_curevents` SET `call_status`=? WHERE `call_uuid`=?", event, calluuid);
    }

    /**
     * deleteCallHold -> delete call hold
     *
     * @param calluuid
     * @return boolean
     */
    public boolean deleteCallHold(String calluuid) {
        if (calluuid != null) {
            return MysqlConnection.getInstance().executeUpdateQuery("DELETE FROM `con_crm_hold_channel` WHERE `uniqueid`=?", calluuid);
        }
        return false;
    }

    /**
     * getCurrentCall -> get current of call
     *
     * @param calluuid
     * @return HashMap
     */
    public HashMap<String, String> getCurrentCall(String calluuid) {
        return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `cur_external_incoming` WHERE `answer_uuid`=? LIMIT 1", calluuid);
    }

    public HashMap<String, String> getSiteByDesCRM(String callback, String callbacktype) {
        try {
            if ("queue".equalsIgnoreCase(callbacktype) || "extension".equalsIgnoreCase(callbacktype)) {
                //-----------------------------------------------------------------------------------------
                List<String> dest = RedisConnection.getInstance().HMGet("pbx_crm_destination_" + callbacktype + "_" + callback, "id", "dest_type", "dest_id", "site_id", "name", "callcenter_type");
                if (dest.get(0) != null) {
                    HashMap<String, String> sipuser = new HashMap<>();
                    sipuser.put("id", dest.get(0));
                    sipuser.put("dest_type", dest.get(1));
                    sipuser.put("dest_id", dest.get(2));
                    sipuser.put("site_id", dest.get(3));
                    sipuser.put("name", dest.get(4));
                    sipuser.put("callcenter_type", dest.get(5));
                    //-----------------------------------------------------------------------------------------
                    List<String> site = RedisConnection.getInstance().HMGet("pbx_con_crm_sites:" + dest.get(3), "secret", "callback_url", "crm_type", "crm_external_version");
                    sipuser.put("secret", site.get(0));
                    sipuser.put("callback_url", site.get(1));
                    sipuser.put("crm_type", site.get(2));
                    sipuser.put("crm_external_version", site.get(3));
                    //-----------------------------------------------------------------------------------------
                    return sipuser;
                }
            } else {
                return null;
            }
        } catch (JedisException ex) {
            System.out.println(ex.getMessage());
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `con_crm_sites`, `pbx_crm_destination` WHERE `pbx_crm_destination`.`site_id`=`con_crm_sites`.`site_id` AND `pbx_crm_destination`.`dest_id`=?", callback);
    }

    public String checkCallHangup(String calluuid) {
        String cache_key = "con_crm_curevents_call_uuid_" + calluuid;
        if (MyMemcached.getInstance().get(cache_key) != null) {
            try {
                HashMap<String, String> curevent = Common.JSONToHashMap(MyMemcached.getInstance().get(cache_key).toString());
                if (curevent.containsKey("transfer") && "0".equals(curevent.get("transfer"))) {
                    return curevent.get("childcalluuid");
                }
            } catch (JSONException ex) {
                System.out.println(ex.getMessage());
            }
        }
        String child = MysqlConnection.getInstance().getColumnByQuery("SELECT `childcalluuid` FROM `con_crm_curevents` WHERE `call_uuid`=? AND `transfer`=0 LIMIT 1", "childcalluuid", calluuid);
        if (child == null || child.isEmpty()) {
            child = "0";
        }
        return child;
    }

    public String getSiteIdBycalluuid(String calluuid) {
        String cache_key = "con_crm_curevents_call_uuid_" + calluuid;
        if (MyMemcached.getInstance().get(cache_key) != null) {
            try {
                HashMap<String, String> curevent = Common.JSONToHashMap(MyMemcached.getInstance().get(cache_key).toString());
                if (curevent.containsKey("site_id")) {
                    return curevent.get("site_id");
                }
            } catch (JSONException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return MysqlConnection.getInstance().getColumnByQuery("SELECT `site_id` FROM `con_crm_curevents` WHERE `call_uuid`=? LIMIT 1", "site_id", calluuid);
    }

    public String getParentCalluuid(String caluuid) {
        return MysqlConnection.getInstance().getColumnByQuery("SELECT `call_uuid` FROM `con_crm_curevents` WHERE `childcalluuid`=?", "call_uuid", caluuid);
    }

    public boolean checkUuid(String uuid) {
        String rowCount = MysqlConnection.getInstance().getColumnByQuery("SELECT COUNT(*) AS rowCount FROM `con_crm_curevents` WHERE `parent_calluuid` IS NOT NULL AND `call_uuid`=?", "rowCount", uuid);
        return Integer.parseInt(rowCount) > 0;
    }

    public boolean checkAnswerChilduuid(String calluuid) {
        String cache_key = "con_crm_curevents_childcalluuid_" + calluuid;
        if (MyMemcached.getInstance().get(cache_key) != null) {
            return true;
        }
        String rowCount = MysqlConnection.getInstance().getColumnByQuery("SELECT COUNT(*) AS rowCount FROM `con_crm_curevents` WHERE `childcalluuid`=? AND `call_status`='DialAnswer'", "rowCount", calluuid);
        return Integer.parseInt(rowCount) > 0;
    }

    public boolean checkNumber(String number) {
        String cache_key = "cur_external_incoming_number_" + number;
        if (MyMemcached.getInstance().get(cache_key) != null) {
            try {
                HashMap<String, String> cache = Common.JSONToHashMap(MyMemcached.getInstance().get(cache_key).toString());
                if (cache.containsKey("number")) {
                    return true;
                }
            } catch (JSONException ex) {
                System.out.println(ex.getMessage());
            }
        }
        String rowCount = MysqlConnection.getInstance().getColumnByQuery("SELECT COUNT(*) AS rowCount FROM `cur_external_incoming` WHERE `number`=?", "rowCount", number);
        return Integer.parseInt(rowCount) > 0;
    }

    public boolean checkDialing(String calluuid) {
        String cache_key = "con_crm_curevents_call_uuid_" + calluuid;
        if (MyMemcached.getInstance().get(cache_key) != null) {
            try {
                HashMap<String, String> cache = Common.JSONToHashMap(MyMemcached.getInstance().get(cache_key).toString());
                if (cache.containsKey("call_status") && "Dialing".equals(cache.get("call_status"))) {
                    return true;
                }
            } catch (JSONException ex) {
                System.out.println(ex.getMessage());
            }
        }
        String rowCount = MysqlConnection.getInstance().getColumnByQuery("SELECT COUNT(*) AS rowCount FROM `con_crm_curevents` WHERE `call_uuid`=? AND `call_status`='Dialing'", "rowCount", calluuid);
        return Integer.parseInt(rowCount) > 0;
    }

    /**
     * checkExtension -> check extension exists
     *
     * @param extension
     * @return boolean
     */
    public boolean checkExtension(String extension) {
        try {
            String dest = RedisConnection.getInstance().HGet("pbx_crm_destination_extension_" + extension, "dest_id");
            if (dest != null && extension.equals(dest)) {
                return true;
            }
        } catch (JedisException ex) {
            System.out.println(ex.getMessage());
        }
        String rowCount = MysqlConnection.getInstance().getColumnByQuery("SELECT COUNT(*) AS rowCount FROM `pbx_crm_destination` WHERE `dest_id`=? AND `dest_type`='extension'", "rowCount", extension);
        return Integer.parseInt(rowCount) > 0;
    }

    /**
     * getCalluuid -> get call uuid by number
     *
     * @param number
     * @return String
     */
    public String getCalluuid(String number) {
        return MysqlConnection.getInstance().getColumnByQuery("SELECT `calluuid` FROM `cur_external_incoming` WHERE `number`=?", "calluuid", number);
    }

    /**
     * logExtension -> write current event
     *
     * @param calluuid
     * @param direction
     * @param site_id
     * @param parentUid
     */
    public void logExtension(String calluuid, String direction, String site_id, String parentUid) {
        try {
            Date dt = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(dt);
            HashMap data_cache = new HashMap();
            data_cache.put("call_uuid", calluuid);
            data_cache.put("call_status", "Dialing");
            data_cache.put("direction", direction);
            data_cache.put("site_id", site_id);
            data_cache.put("updated_datetime", currentTime);
            data_cache.put("parent_calluuid", parentUid);
            //data_cache.put("calltype", calltype);
            String cache_key = "con_crm_curevents_call_uuid_" + calluuid;
            String json = JSONObject.valueToString(data_cache);
            MyMemcached.getInstance().set(cache_key, json, new Date(System.currentTimeMillis() + 480000));
        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
        }
        MysqlConnection.getInstance().executeNonQuery("INSERT INTO `con_crm_curevents`(`call_uuid`,`call_status`,`direction`,`site_id`,`updated_datetime`,`parent_calluuid`) VALUES ( ?, 'Dialing', ?, ?, NOW(),?)", calluuid, direction, site_id, parentUid);
    }

    /**
     * InsertMonitor -> write log monitor file
     *
     * @param calluuid
     * @param filename
     * @return boolean
     */
    public boolean InsertMonitor(String calluuid, String filename) {
        return MysqlConnection.getInstance().executeQuery("INSERT INTO `external_monitor_file`(`calluuid`,`monitor_filename`) VALUES(?,?)", calluuid, filename);
    }

    /**
     * startHoldMain -> start hold main
     *
     * @param calluuid
     * @param receivedTime
     * @return boolean
     */
    public boolean startHoldMain(String calluuid, String receivedTime) {
        return MysqlConnection.getInstance().executeUpdateQuery("UPDATE `con_crm_curevents` SET `startholdtime`=?, `stopHold`=2 WHERE `call_uuid`=?", receivedTime, calluuid);
    }

    /**
     * stopHoldMain -> stop hold main
     *
     * @param calluuid
     * @param receivedTime
     * @return boolean
     */
    public boolean stopHoldMain(String calluuid, String receivedTime) {
        return MysqlConnection.getInstance().executeUpdateQuery("UPDATE `con_crm_curevents` SET `holdtime`=(`holdtime` + TIME_TO_SEC(TIMEDIFF(?,`startholdtime`))),`stopHold`=1 WHERE `call_uuid`=? AND `stopHold`=2", receivedTime, calluuid);
    }

    /**
     * startHold -> start hold
     *
     * @param calluuid
     * @param receivedTime
     * @return boolean
     */
    public boolean startHold(String calluuid, String receivedTime) {
        return MysqlConnection.getInstance().executeUpdateQuery("UPDATE `con_crm_curevents` SET `startholdtime`=?,`stopHold`=2 WHERE (`childcalluuid`=? OR `call_uuid`=? ) AND `call_status`='DialAnswer'", receivedTime, calluuid, calluuid);
    }

    /**
     * stopHold -> stop hold
     *
     * @param calluuid
     * @param receivedTime
     * @return boolean
     */
    public boolean stopHold(String calluuid, String receivedTime) {
        return MysqlConnection.getInstance().executeUpdateQuery("UPDATE `con_crm_curevents` SET `holdtime`=(`holdtime`+TIME_TO_SEC(TIMEDIFF(?,`startholdtime`))),`stopHold`=1 WHERE(`childcalluuid`=? OR `call_uuid`=?) AND `call_status`='DialAnswer' AND `stopHold`=2", receivedTime, calluuid, calluuid);
    }

    /**
     * updateCDRHangupBy -> update hangup by in CDR
     *
     * @param calluuid
     * @param hangupby
     * @return
     */
    public boolean updateCDRHangupBy(String calluuid, String hangupby) {
        return MysqlConnection.getInstance().executeUpdateQuery("UPDATE `cdr` SET `hangupby`=? WHERE `uniqueid`=?", hangupby, calluuid);
    }

    public void insertBlindTransfer(String channel, String value, String uniqueid) {
        //Memcached
        MyMemcached.getInstance().set("cache_blindtransfer_" + uniqueid, channel, new Date(System.currentTimeMillis() + 2000));
        MyMemcached.getInstance().set("blindtransfer_" + uniqueid, channel.trim() + "," + value.trim(), new Date(System.currentTimeMillis() + 3600000));
        //Logging
        MysqlConnection.getInstance().executeNonQuery("INSERT INTO `blindtransfer`(`channel`,`value`,`uniqueid`) VALUES (?,?,?)", channel, value, uniqueid);
    }

    public void insertTransferEvent(String type, String channel, String uniqueid, String targetuniqueid, String targetchannel, String exten, String context) {
        try {
            //Memcached
            JSONObject obj = new JSONObject();
            obj.put("type", type);
            obj.put("channel", channel);
            obj.put("uniqueid", uniqueid);
            obj.put("targetuniqueid", targetuniqueid);
            obj.put("targetchannel", targetchannel);
            obj.put("exten", exten);
            obj.put("context", context);
            String key1 = "transfer_" + channel.replaceAll("/|-|@|;", "");
            String key2 = "transfer_" + targetchannel.replaceAll("/|-|@|;", "");
            MyMemcached.getInstance().set(key1, obj.toString(), new Date(System.currentTimeMillis() + 3600000));
            MyMemcached.getInstance().set(key2, obj.toString(), new Date(System.currentTimeMillis() + 3600000));
        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
        }
        //Logging
        MysqlConnection.getInstance().executeNonQuery("INSERT INTO `transfer_event`(`type`,`channel`,`uniqueid`,`targetuniqueid`,`targetchannel`,`exten`,`context`) VALUES (?,?,?,?,?,?,?)", type, channel, uniqueid, targetuniqueid, targetchannel, exten, context);
    }

    /**
     *
     * @param calluuid
     * @param channel
     */
    public void updateLastChannel(String calluuid, String channel) {
        MyMemcached.getInstance().set("channel_start_" + calluuid, channel, new Date(System.currentTimeMillis() + 1800000));
        MysqlConnection.getInstance().executeUpdateQuery("UPDATE `con_crm_curevents` SET `channel2`=? WHERE `call_uuid`=?", channel, calluuid);
    }

    /**
     *
     * @param calluuid
     * @return
     */
    public Object getLastChannel(String calluuid) {
        return MyMemcached.getInstance().get("channel_start_" + calluuid);
    }

    /**
     *
     * @param calluuid
     * @return
     */
    public String getLastChannel2(String calluuid) {
        return MysqlConnection.getInstance().getColumnByQuery("SELECT `channel2` FROM `con_crm_curevents` WHERE `call_uuid`=? LIMIT 1", "channel2", calluuid);
    }

    /**
     *
     * @param calluuid
     */
    public void deleteLastChannel(String calluuid) {
        Object channel_start = MyMemcached.getInstance().get("channel_start_" + calluuid);
        if (channel_start != null) {
            MyMemcached.getInstance().update("channel_start_" + calluuid, channel_start, new Date(System.currentTimeMillis() + 5000));
        }
    }

}
