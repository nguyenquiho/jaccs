/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * Develop by Nguyen Qui Ho
 */
package com.worldfone.access;

import com.worldfone.cache.MyMemcached;
import com.worldfone.utils.helpers.BasedCommon;
import com.worldfone.wrg.AESCipher;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
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
                if (null == _instance) {
                    DBSelectionSet.instance = _instance = new DBSelectionSet();
                }
            }
        }
        return _instance;
    }

    private DBSelectionSet() {
    }

    /**
     * getSiteById -> get sites by id
     *
     * @param id
     * @return ArrayList
     */
    public HashMap<String, String> getSiteById(String id) {
        try {
            List<String> crm_sites = RedisConnection.getInstance().HMGet("pbx_con_crm_sites:" + id, "site_id", "secret", "callback_url", "crm_type", "crm_external_version", "connect_info", "report_voicemail", "callcenter_type", "callcenter_type_text", "mailbox");
            if (crm_sites.get(0) != null) {
                HashMap<String, String> site = new HashMap<>();
                site.put("site_id", crm_sites.get(0));
                site.put("secret", crm_sites.get(1));
                site.put("callback_url", crm_sites.get(2));
                site.put("crm_type", crm_sites.get(3));
                site.put("crm_external_version", crm_sites.get(4));
                site.put("connect_info", crm_sites.get(5));
                site.put("report_voicemail", crm_sites.get(6));
                site.put("callcenter_type", crm_sites.get(7));
                site.put("callcenter_type_text", crm_sites.get(8));
                site.put("mailbox", crm_sites.get(9));
                return site;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `con_crm_sites` WHERE `site_id`=? LIMIT 1", id);
    }

    /**
     * getRoutingInfo -> get based routing by id
     *
     * @param id
     * @return HashMap
     */
    public HashMap<String, String> getRoutingInfo(String id) {
        try {
            List<String> based_route = RedisConnection.getInstance().HMGet("pbx_based_routing:" + id, "id", "rout_url", "secret", "default_type", "default_id");
            if (based_route.get(0) != null) {
                HashMap<String, String> based = new HashMap();
                based.put("id", based_route.get(0));
                based.put("rout_url", based_route.get(1));
                based.put("secret", based_route.get(2));
                based.put("default_type", based_route.get(3));
                based.put("default_id", based_route.get(4));
                return based;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `pbx_based_routing` WHERE `id`=?", id);
    }

    /**
     * is extension -> check extension exists
     *
     * @param extension
     * @return Boolean
     */
    public boolean isextension(String extension) {
        try {
            String exten = RedisConnection.getInstance().HGet("pbx_sipusers_" + extension, "extension");
            if (exten != null) {
                return true;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        if (extension.length() < 8) {
            String id = MysqlConnection.getInstance().getColumnByQuery("SELECT `id` FROM `sipusers` WHERE `extension`='" + extension + "'", "id");
            return id != null && !"".equals(id);
        }
        return false;
    }

    /**
     * isqueue -> check queue exists
     *
     * @param queue
     * @return Boolean
     */
    public boolean isqueue(String queue) {
        try {
            String name = RedisConnection.getInstance().HGet("pbx_queues_" + queue, "name");
            if (name != null) {
                return true;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        if (queue.length() < 8) {
            String id = MysqlConnection.getInstance().getColumnByQuery("SELECT `id` FROM `queues` WHERE `name`='" + queue + "'", "id");
            return id != null && !"".equals(id);
        }
        return false;
    }

    /**
     * getQueueByName -> get queue info by queuename
     *
     * @param name
     * @param select
     * @return
     */
    public HashMap<String, String> getQueueByName(String name, String... select) {
        try {
            List<String> pbx_queue = RedisConnection.getInstance().HMGet("pbx_queues_" + name, select);
            if (pbx_queue.get(0) != null) {
                HashMap<String, String> respones = new HashMap();
                Iterator<String> keysItr = Arrays.asList(select).iterator();
                int idx = 0;
                while (keysItr.hasNext()) {
                    respones.put(keysItr.next(), pbx_queue.get(idx));
                    idx++;
                }
                return respones;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        if (name.length() < 8) {
            return MysqlConnection.getInstance().getOneByQuery("SELECT " + String.join(",", Arrays.asList(select)) + " FROM `queues` WHERE `name`='" + name + "' LIMIT 1");
        }
        return null;
    }

    /**
     * checkonCall -> check extension on call
     *
     * @param extension
     * @return Boolean
     */
    public boolean checkIDLEExtCRM(String extension) {
        try {
            String state = RedisConnection.getInstance().get("ast_state_external_" + extension);
            return "IDLE".equals(state);
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return false;
    }

    /**
     * checkExternal -> check extension is externalcrm
     *
     * @param ext
     * @return String
     */
    public String checkExternal(String ext) {
        String id = checkExternal(ext, "extension");
        return id != null ? id : checkExternal(ext, "queue");
    }

    /**
     *
     * @param ext
     * @param type
     * @return
     */
    public String checkExternal(String ext, String type) {
        try {
            String id = RedisConnection.getInstance().HGet("pbx_crm_destination_" + type + "_" + ext, "id");
            if (id != null) {
                return id;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        if (ext.length() < 8) {
            return MysqlConnection.getInstance().getColumnByQuery("SELECT `id` FROM `pbx_crm_destination` WHERE `dest_id`='" + ext + "' AND `dest_type`='" + type + "' LIMIT 1", "id");
        }
        return null;
    }

    /**
     * checkExistCall -> check on call by calluuid
     *
     * @param calluuid
     * @return boolean
     */
    public boolean checkExistCall(String calluuid) {
        try {
            String cache_key = "cur_external_incoming_calluuid_" + calluuid;
            return MyMemcached.getInstance().get(cache_key) != null;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        String rowCount = MysqlConnection.getInstance().getColumnByQuery("SELECT COUNT(*) AS rowCount FROM `cur_external_incoming` WHERE `calluuid`=?", "rowCount", calluuid);
        return rowCount != null && !"0".equals(rowCount);
    }

    /**
     * getCrmDestionation -> get crm destination by id
     *
     * @param dest_id
     * @return HashMap
     */
    public HashMap<String, String> getCrmDestionation(String dest_id) {
        try {
            List<String> crm = RedisConnection.getInstance().HMGet("pbx_crm_destination:" + dest_id, "id", "dest_type", "dest_id", "site_id", "name", "callcenter_type");
            if (crm.get(0) != null) {
                HashMap<String, String> dest = new HashMap();
                dest.put("id", crm.get(0));
                dest.put("dest_type", crm.get(1));
                dest.put("dest_id", crm.get(2));
                dest.put("site_id", crm.get(3));
                dest.put("name", crm.get(4));
                dest.put("callcenter_type", crm.get(5));
                return dest;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `pbx_crm_destination` WHERE `id`=?", dest_id);
    }

    /**
     * getCrmDestionationByName -> get crm destination by name
     *
     * @param dest_id
     * @return HashMap
     */
    public HashMap<String, String> getCrmDestionationByName(String dest_id) {
        try {
            List<String> crm_dest = RedisConnection.getInstance().HMGet("pbx_crm_destination_extension_" + dest_id, "id", "dest_type", "dest_id", "site_id", "name", "callcenter_type");
            if (crm_dest.get(0) != null) {
                HashMap<String, String> dest = new HashMap();
                dest.put("id", crm_dest.get(0));
                dest.put("dest_type", crm_dest.get(1));
                dest.put("dest_id", crm_dest.get(2));
                dest.put("site_id", crm_dest.get(3));
                dest.put("name", crm_dest.get(4));
                dest.put("callcenter_type", crm_dest.get(5));
                return dest;
            } else {
                crm_dest = RedisConnection.getInstance().HMGet("pbx_crm_destination_queue_" + dest_id, "id", "dest_type", "dest_id", "site_id", "name", "callcenter_type");
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
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `pbx_crm_destination` WHERE `dest_id`=?", dest_id);
    }

    /**
     * getCrmDestionationByName -> get crm destination by name
     *
     * @param dest_id
     * @param dest_type
     * @return
     */
    public HashMap<String, String> getCrmDestionationByName(String dest_id, String dest_type) {
        try {
            HashMap<String, String> dest = new HashMap();
            List<String> crm_dest = RedisConnection.getInstance().HMGet("pbx_crm_destination_" + dest_type + "_" + dest_id, "id", "dest_type", "dest_id", "site_id", "name", "callcenter_type");
            if (crm_dest.get(0) != null) {
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
     * LastCall -> get lastcall
     *
     * @param did_number
     * @return HashMap
     */
    public HashMap LastCall(String did_number) {
        try {
            HashMap sipuser = new HashMap();
            List<String> direct_inward_dial = RedisConnection.getInstance().HMGet("pbx_direct_inward_dial_" + did_number, "id", "did_number", "description", "record_calls", "caller_callerid_prefix", "default_destination", "default_destination_detail", "lastcall", "lastcall_disposition", "lastcall_direction", "lastcall_agentstatus");
            if (direct_inward_dial.get(0) != null && direct_inward_dial.get(7) != null && "1".equals(direct_inward_dial.get(7))) {
                sipuser.put("id", direct_inward_dial.get(0));
                sipuser.put("did_number", direct_inward_dial.get(1));
                sipuser.put("description", direct_inward_dial.get(2));
                sipuser.put("record_calls", direct_inward_dial.get(3));
                sipuser.put("caller_callerid_prefix", direct_inward_dial.get(4));
                sipuser.put("default_destination", direct_inward_dial.get(5));
                sipuser.put("default_destination_detail", direct_inward_dial.get(6));
                sipuser.put("lastcall", direct_inward_dial.get(7));
                sipuser.put("lastcall_disposition", direct_inward_dial.get(8));
                sipuser.put("lastcall_direction", direct_inward_dial.get(9));
                sipuser.put("lastcall_agentstatus", direct_inward_dial.get(10));
                return sipuser;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT `lastcall_disposition`, `lastcall_direction`, `lastcall_agentstatus` FROM  `pbx_direct_inward_dial` WHERE `did_number`=? AND `lastcall`='1'", did_number);
    }

    /**
     * getLastCDR -> get last CDR of number
     *
     * @param callerNum
     * @param did_number
     * @return HashMap
     */
    public HashMap getLastCDR(String callerNum, String did_number) {
        return MysqlConnection.getInstance().getOneByQuery("SELECT `src`,`dst`,`direction`,`channel`,`dstchannel`,`disposition` FROM `cdr_lastcall` WHERE (`src`='" + callerNum + "' OR `dst`='" + callerNum + "') AND `did_number`='" + did_number + "' ORDER BY `calldate` DESC LIMIT 1");
    }

    /**
     * getLastCDR -> get last CDR of number
     *
     * @param callerNum
     * @param did_number
     * @param direction
     * @return HashMap
     */
    public HashMap getLastCDR(String callerNum, String did_number, String direction) {
        return MysqlConnection.getInstance().getOneByQuery("SELECT `src`,`dst`,`direction`,`channel`,`dstchannel`,`disposition` FROM `cdr_lastcall` WHERE (`src`='" + callerNum + "' OR `dst`='" + callerNum + "') AND `did_number`='" + did_number + "' AND `direction`='" + direction + "' ORDER BY `calldate` DESC LIMIT 1");
    }

    /**
     * logCurrentEvent -> write log current event
     *
     * @param calluuid
     * @param site_id
     * @param calltype
     */
    public void logCurrentEvent(String calluuid, String site_id, String calltype) {
        try {
            Date dt = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(dt);
            HashMap data_cache = new HashMap();
            data_cache.put("call_uuid", calluuid);
            data_cache.put("call_status", "Start");
            data_cache.put("direction", "inbound");
            data_cache.put("site_id", site_id);
            data_cache.put("updated_datetime", currentTime);
            data_cache.put("calltype", calltype);
            data_cache.put("starttime", currentTime);
            String cache_key = "con_crm_curevents_call_uuid_" + calluuid;
            String json = JSONObject.valueToString(data_cache);
            MyMemcached.getInstance().set(cache_key, json, new Date(System.currentTimeMillis() + 1000000));
        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
        }
        MysqlConnection.getInstance().executeUpdateNonQuery("INSERT INTO `con_crm_curevents`(`call_uuid`, `call_status`, `direction`, `site_id`, `updated_datetime`, `calltype`,`starttime`) VALUES (?,'Start','inbound',?, NOW(),?,NOW())", calluuid, site_id, calltype);
    }

    /**
     * logCurrentIncoming -> write log current incoming
     *
     * @param number
     * @param calluuid
     */
    public void logCurrentIncoming(String number, String calluuid) {
        try {
//            Date dt = new Date();
            //java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //String currentTime = sdf.format(dt);
            HashMap data_cache = new HashMap();
            data_cache.put("number", number);
            data_cache.put("calluuid", calluuid);
            String cache_key = "cur_external_incoming_number_" + number;
            String cache_key2 = "cur_external_incoming_calluuid_" + calluuid;
            String json = JSONObject.valueToString(data_cache);
            MyMemcached.getInstance().set(cache_key, json, new Date(System.currentTimeMillis() + 60000));
            MyMemcached.getInstance().set(cache_key2, json, new Date(System.currentTimeMillis() + 60000));
        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
        }
        MysqlConnection.getInstance().executeUpdateNonQuery("INSERT INTO `cur_external_incoming`(`number`,`calluuid`) VALUES (?,?)", number, calluuid);
    }

    /**
     * checkExternallc -> check licency
     *
     * @return boolean
     */
    public boolean checkExternallc() {
        int maxChannels = BasedCommon.maxChannels();
        try {
            String str_total = RedisConnection.getInstance().get("pbx_crm_destination_total_count");
            if (str_total != null) {
                int total = Integer.parseInt(str_total);
                return (total < maxChannels);
            }
        } catch (NumberFormatException | JedisException ex) {
//            System.out.println(ex);
        }
        String rowCount = MysqlConnection.getInstance().getColumnByQuery("SELECT COUNT(*) AS rowCount FROM `pbx_crm_destination` WHERE `dest_type`='extension'", "rowCount");
        return (Integer.parseInt(rowCount) < maxChannels);
    }

    /**
     * getAgentID -> get agent id by extension
     *
     * @param ext
     * @return String
     */
    public String getAgentID(String ext) {
        try {

            String id = RedisConnection.getInstance().HGet("pbx_sipusers_" + ext, "id");
            if (id != null) {
                return id;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getColumnByQuery("SELECT `id` FROM `sipusers` WHERE `extension`='" + ext + "'", "id");
    }

    /**
     * getAgentName -> get agent name by extension
     *
     * @param ext
     * @return String
     */
    public String getAgentName(String ext) {
        try {
            String callername = RedisConnection.getInstance().HGet("pbx_sipusers_" + ext, "callername");
            if (callername != null) {
                return callername;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getColumnByQuery("SELECT `callername` FROM `sipusers` WHERE `extension`=" + ext, "callername");
    }

    /**
     * getAgentCol -> get agent missing 1 info by extension
     *
     * @param column
     * @param ext
     * @return String
     */
    public String getAgentCol(String ext, String column) {
        try {
            String cl = RedisConnection.getInstance().HGet("pbx_sipusers_" + ext, column);
            if (cl != null) {
                return cl;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getColumnByQuery("SELECT `" + column + "` FROM `sipusers` WHERE `extension`='" + ext + "'", column);
    }

    /**
     * getExtensionInfo
     *
     * @param extension
     * @return HashMap
     */
    public HashMap<String, String> getExtensionInfo(String extension) {
        try {
            HashMap<String, String> info = new HashMap();
            List<String> user = RedisConnection.getInstance().HMGet("pbx_sipusers_" + extension, "callername", "dnd");
            if (user.get(0) != null || user.get(1) != null) {
                info.put("callername", user.get(0));
                info.put("dnd", user.get(1));
                return info;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT `callername`, `dnd` FROM `sipusers` WHERE `extension`=? LIMIT 1", extension);
    }

    public HashMap<String, String> getExtensionForwanding(String extension) {
        try {
            HashMap<String, String> info = new HashMap();
            List<String> user = RedisConnection.getInstance().HMGet("pbx_sipusers_" + extension, "forwanding", "destinatype", "destination", "dnd");
            if (user.get(0) != null) {
                info.put("forwanding", user.get(0));
                info.put("destinatype", user.get(1));
                info.put("destination", user.get(2));
                info.put("dnd", user.get(3));
                return info;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT `forwanding`,`destinatype`,`destination`,`dnd` FROM `sipusers` WHERE `extension`=? LIMIT 1", extension);
    }

    public String getStatusDNC(String number) {
        try {
            String status = RedisConnection.getInstance().HGet("pbx_dnc_list_" + number, "status");
            if (status == null && !Pattern.matches("^0(.*)", number)) {
                status = RedisConnection.getInstance().HGet("pbx_dnc_list_0" + number, "status");
            }
            return status;
        } catch (JedisException ex) {
            System.out.println(ex);
            return MysqlConnection.getInstance().getColumnByQuery("SELECT `status` FROM `dnc_list` WHERE `number`='" + number + "' OR `number`='0" + number + "'", "status");
        }
    }

    public String getStatusDNCO(String number) {
        try {
            String status = RedisConnection.getInstance().HGet("pbx_dnco_list_" + number, "status");
            if (status != null && !Pattern.matches("^0(.*)", number)) {
                status = RedisConnection.getInstance().HGet("pbx_dnco_list_0" + number, "status");
            }
            return status;
        } catch (JedisException ex) {
            System.out.println(ex);
            return MysqlConnection.getInstance().getColumnByQuery("SELECT `status` FROM `dnco_list` WHERE `number`='" + number + "' OR `number`='0" + number + "'", "status");
        }
    }

    public HashMap<String, String> getVipList(String number) {
        try {
            List<String> vip = RedisConnection.getInstance().HMGet("pbx_viplist_" + number, "destinatype", "destination", "status");
            if (vip.get(0) == null && !Pattern.matches("^0(.*)", number)) {
                vip = RedisConnection.getInstance().HMGet("pbx_viplist_0" + number, "destinatype", "destination", "status");
            }
            if (vip.get(0) != null) {
                HashMap<String, String> info = new HashMap();
                info.put("destinatype", vip.get(0));
                info.put("destination", vip.get(1));
                info.put("status", vip.get(2));
                return info;
            }
            return null;
        } catch (JedisException ex) {
            System.out.println(ex);
            return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `viplist` WHERE `number`='" + number + "'");
        }
    }

    public HashMap<String, String> getIvrMenuDestination(String menu_id) {
        try {
            List<String> menu_dest = RedisConnection.getInstance().HMGet("pbx_ivr_menu_destination:" + menu_id, "no_sel", "no_sel_detail", "invalid_sel", "invalid_sel_detail");
            if (menu_dest.get(0) != null) {
                HashMap<String, String> response = new HashMap();
                response.put("no_sel", menu_dest.get(0));
                response.put("no_sel_detail", menu_dest.get(1));
                response.put("invalid_sel", menu_dest.get(2));
                response.put("invalid_sel_detail", menu_dest.get(3));
                return response;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT `no_sel`,`no_sel_detail`,`invalid_sel`,`invalid_sel_detail` FROM `pbx_ivr_menu_destination` WHERE `menu_id`=?", menu_id);
    }

    public HashMap<String, String> getIvrMenuOptionsKey(String menu_id, String digito) {
        try {
            List<String> option_keys = RedisConnection.getInstance().HMGet("pbx_ivr_menu_option_keys:" + menu_id + ":" + digito, "menu_id", "key", "destination", "details", "useraction");
            if (option_keys.get(0) != null) {
                HashMap<String, String> rs = new HashMap<>();
                rs.put("menu_id", option_keys.get(0));
                rs.put("key", option_keys.get(1));
                rs.put("destination", option_keys.get(2));
                rs.put("details", option_keys.get(3));
                rs.put("useraction", option_keys.get(4));
                return rs;
            } else {
                option_keys = RedisConnection.getInstance().HMGet("pbx_ivr_menu_option_keys:" + menu_id + ":?", "menu_id", "key", "destination", "details", "useraction");
                if (option_keys.get(0) != null) {
                    HashMap<String, String> rs = new HashMap<>();
                    rs.put("menu_id", option_keys.get(0));
                    rs.put("key", option_keys.get(1));
                    rs.put("destination", option_keys.get(2));
                    rs.put("details", option_keys.get(3));
                    rs.put("useraction", option_keys.get(4));
                    rs.put("hasw", "1");
                    return rs;
                }
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `pbx_ivr_menu_option_keys` WHERE `menu_id`=? AND (`key`=? OR `key`='?')", menu_id, digito);
    }

    public HashMap<String, String> checkShortKey(String sortkey) {
        try {
            String short_keys = RedisConnection.getInstance().get("pbx_short_keys");
            if (short_keys != null) {
                HashMap<String, String> response = new HashMap();
                JSONArray arr = new JSONArray(short_keys);
                String[] destination = new String[]{"pickup", "listen", "whisper", "barge"};
                for (int i = 0; i < arr.length(); i++) {
                    if (arr.getJSONObject(i).has("destination")
                            && !Arrays.stream(destination).anyMatch(arr.getJSONObject(i).get("destination").toString().trim()::equals)
                            && arr.getJSONObject(i).has("short_key")
                            && arr.getJSONObject(i).has("details")
                            && arr.getJSONObject(i).has("check_login")
                            && sortkey.equals(arr.getJSONObject(i).get("short_key").toString().trim())) {
                        response.put("short_key", arr.getJSONObject(i).get("short_key").toString().trim());
                        response.put("destination", arr.getJSONObject(i).get("destination").toString().trim());
                        response.put("details", arr.getJSONObject(i).get("details").toString().trim());
                        response.put("check_login", arr.getJSONObject(i).get("check_login").toString().trim());
                        return response;
                    }
                }
                return null;
            }
        } catch (JSONException | JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT `short_key`,`destination`,`details`,`check_login` FROM `pbx_short_keys` WHERE `destination` NOT IN ('pickup', 'listen', 'whisper', 'barge') AND `short_key`='" + sortkey + "'");
    }

    public HashMap<String, String> checkShortKeyP2(String sortkey) {
        try {
            String short_keys = RedisConnection.getInstance().get("pbx_short_keys");
            if (short_keys != null) {
                HashMap<String, String> response = new HashMap();
                JSONArray json = new JSONArray(short_keys);
                String[] destination = new String[]{"pickup", "listen", "whisper", "barge"};
                for (int i = 0; i < json.length(); i++) {
                    if (json.getJSONObject(i).has("destination")
                            && Arrays.stream(destination).anyMatch(json.getJSONObject(i).get("destination").toString().trim()::equals)
                            && json.getJSONObject(i).has("short_key")
                            && json.getJSONObject(i).has("details")
                            && json.getJSONObject(i).has("check_login")) {
                        String skdb = json.getJSONObject(i).get("short_key").toString();
                        String dssk = sortkey.substring(0, skdb.length());
                        String ext = sortkey.substring(skdb.length());
                        String checkext = getAgentID(ext);
                        if (checkext != null && skdb.equals(dssk)) {
                            response.put("extension", ext);
                            response.put("destination", json.getJSONObject(i).get("destination").toString());
                            return response;
                        }
                    }
                }
                return null;
            }
        } catch (JSONException | JedisException ex) {
            System.out.println(ex);
        }
        HashMap<String, String> rs = MysqlConnection.getInstance().getOneByQuery("SELECT `short_key`,`destination`,`details`,`check_login` FROM `pbx_short_keys` WHERE `destination` IN ('pickup', 'listen', 'whisper', 'barge')");
        if (rs != null) {
            HashMap<String, String> response = new HashMap();
            if (rs.containsKey("short_key") && rs.containsKey("destination")) {
                String skdb = rs.get("short_key");
                String dssk = sortkey.substring(0, skdb.length());
                String ext = sortkey.substring(skdb.length());
                String checkext = getAgentID(ext);
                if (checkext != null && skdb.equals(dssk)) {
                    response.put("extension", ext);
                    response.put("destination", rs.get("destination"));
                    return response;
                }
            }
        }
        return null;
    }

    public boolean checkAgent(String agent, String pass) {
        HashMap rs = MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `agent` WHERE `user`='" + agent + "'");
        return rs != null && rs.containsKey("pass") && pass.equals(rs.get("pass").toString());
    }

    public boolean checkAgentActivity(String agent, String extension) {
        String og = MysqlConnection.getInstance().getColumnByQuery("SELECT `agent` FROM `agent_activity_logs` WHERE `extension`='" + extension + "' AND `agent`='" + agent + "' AND `logout_time` IS NULL", "agent");
        return og != null;
    }

    public void insertAgentActivity(String agent, String extension) {
        MysqlConnection.getInstance().executeNonQuery("INSERT INTO `agent_activity_logs`(`agent`,`extension`,`login_time`) VALUES (?,?,NOW())", agent, extension);
    }

    public void updateAgentActivity(String agent, String extension) {
        MysqlConnection.getInstance().executeUpdateNonQuery("UPDATE `agent_activity_logs` SET `logout_time`=NOW() WHERE `agent`=? AND `extension`=? AND `logout_time` IS NULL", agent, extension);
    }

    public String pbxQueues(String qid) {
        try {
            String name = RedisConnection.getInstance().HGet("pbx_queues:" + qid, "name");
            if (name != null) {
                return name;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getColumnByQuery("SELECT `name` FROM `queues` WHERE `id`=" + qid, "name");
    }

    public boolean checkQueue(String queue, String agent) {
        String og = MysqlConnection.getInstance().getColumnByQuery("SELECT `id` FROM `queue_agent` WHERE `queuename`='" + queue + "' AND `agent`='" + agent + "'", "id");
        return og != null;
    }

    public void updateAgent(String queue, String agent) {
        MysqlConnection.getInstance().executeUpdateNonQuery("UPDATE `queue_agent` SET `agent_status`='In use' WHERE `queuename`=? AND `agent`=?", queue, agent);
    }

    public void insertAgent(String queue, String agent, String data) {
        MysqlConnection.getInstance().executeNonQuery("INSERT INTO `queue_agent`(`queuename`,`agent`,`agent_status`,`data`) VALUES (?,?,'Not in use',?)", queue, agent, data);
    }

    public void deleteAgent(String queue, String agent) {
        MysqlConnection.getInstance().executeUpdateNonQuery("DELETE FROM `queue_agent` WHERE `queuename`=? AND `agent`=?", queue, agent);
    }

    public void insertQueueMessage(String src, String action, String queue) {
        MysqlConnection.getInstance().executeNonQuery("INSERT INTO `queue_msg`(`src`,`action`,`queue`) VALUES (?,?,?)", src, action, queue);
    }

    public HashMap<String, String> checkRecordDirection(String extension) {
        try {
            String record = RedisConnection.getInstance().get("pbx_record_derection");
            String record_calls = RedisConnection.getInstance().HGet("pbx_sipusers_" + extension, "record_calls");
            if (record != null && record_calls != null) {
                HashMap<String, String> context = new HashMap();
                JSONArray arr1 = new JSONArray(record);
                for (int i = 0; i < arr1.length(); i++) {
                    if (record_calls.equals(arr1.getJSONObject(i).get("id").toString())) {
                        context.put("inbound", arr1.getJSONObject(i).get("inbound").toString());
                        context.put("outbound", arr1.getJSONObject(i).get("outbound").toString());
                        context.put("internal", arr1.getJSONObject(i).get("internal").toString());
                        context.put("external", arr1.getJSONObject(i).get("external").toString());
                        return context;
                    }
                }
                return null;
            }
        } catch (JSONException | JedisException ex) {
            System.out.println(ex);
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT `rc`.`inbound`,`rc`.`outbound`,`rc`.`internal`,`rc`.`external` ");
        sql.append("FROM `sipusers` ");
        sql.append("LEFT JOIN `pbx_record_derection` AS `rc` ON `sipusers`.`record_calls`=`rc`.`id` ");
        sql.append("WHERE `extension`=?");
        return MysqlConnection.getInstance().getOneByQuery(sql.toString(), extension);
    }

    public String checkRecordCall(String ext, String lnao) {
        String record_calls = null;
        switch (lnao) {
            case "meetme":
                try {
                record_calls = RedisConnection.getInstance().HGet("pbx_meetme_" + ext, "record_calls");
                if (record_calls != null) {
                    break;
                }
            } catch (JedisException ex) {
                System.out.println(ex);
            }
            record_calls = MysqlConnection.getInstance().getColumnByQuery("SELECT `record_calls` FROM `meetme` WHERE `confno`='" + ext + "'", "record_calls");
            break;
            case "queue":
                try {
                record_calls = RedisConnection.getInstance().HGet("pbx_queues_" + ext, "record_calls");
                if (record_calls != null) {
                    break;
                }
            } catch (JedisException ex) {
                System.out.println(ex);
            }
            record_calls = MysqlConnection.getInstance().getColumnByQuery("SELECT `record_calls` FROM `queues` WHERE `name`='" + ext + "'", "record_calls");
        }
        return record_calls != null ? record_calls : "";
    }

    public HashMap<String, String> getPincode(String pincode) {
        try {
            List<String> pbx_pincode = RedisConnection.getInstance().HMGet("pbx_pincode_" + pincode, "id", "outbound_group", "groupid", "codeId", "pincode", "name", "extension", "use_in", "email", "autosend", "record_calls", "status", "timeout", "expiry");
            if (pbx_pincode.get(0) != null && pbx_pincode.get(11) != null && "1".equals(pbx_pincode.get(11)) && pincode.equals(pbx_pincode.get(4))) {
                HashMap<String, String> response = new HashMap();
                response.put("id", pbx_pincode.get(0));
                response.put("outbound_group", pbx_pincode.get(1));
                response.put("groupid", pbx_pincode.get(2));
                response.put("codeId", pbx_pincode.get(3));
                response.put("pincode", pbx_pincode.get(4));
                response.put("name", pbx_pincode.get(5));
                response.put("extension", pbx_pincode.get(6));
                response.put("use_in", pbx_pincode.get(7));
                response.put("email", pbx_pincode.get(8));
                response.put("autosend", pbx_pincode.get(9));
                response.put("record_calls", pbx_pincode.get(10));
                response.put("status", pbx_pincode.get(11));
                response.put("timeout", pbx_pincode.get(12));
                response.put("expiry", pbx_pincode.get(13));
                return response;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `pbx_pincode` WHERE `status`=1 AND `pincode`='" + pincode + "' LIMIT 1");
    }

    private String checkOutboundPrefix(String group, String dst) {
        try {
            String out_pre = RedisConnection.getInstance().get("pbx_outbound_prefix");
            if (out_pre != null) {
                JSONArray arr = new JSONArray(out_pre);
                for (int i = 0; i < arr.length(); i++) {
                    if (arr.getJSONObject(i).has("outbound_group") && group.equals(arr.getJSONObject(i).get("outbound_group").toString())) {
                        String rf = arr.getJSONObject(i).get("prefix").toString();
                        String checkrf = dst.substring(0, rf.length());
                        //System.out.println(checkrf);
                        if (rf.equals(checkrf) && "1".equals(arr.getJSONObject(i).get("status").toString())) {
                            return "1";
                        }
                    }
                }
                return "0";
            }
        } catch (JSONException | JedisException ex) {
            System.out.println(ex);
        }
        ArrayList<HashMap> rs = MysqlConnection.getInstance().getListByQuery("SELECT * FROM `pbx_outbound_prefix` WHERE `outbound_group`='" + group + "'");
        String rf, checkrf;
        for (HashMap<String, String> data : rs) {
            rf = data.get("prefix");
            if (rf.length() < dst.length()) {
                checkrf = dst.substring(0, rf.length());
                //System.out.println(checkrf);
                if (rf.equals(checkrf) && "1".equals(data.get("status"))) {
                    return "1";
                }
            }
        }
        return "0";
    }

    public String checkPincode(String key, String dst, String src) {
        try {
            String og = RedisConnection.getInstance().HGet("pbx_pincode_" + key, "outbound_group");
            if (og != null) {
                return checkOutboundPrefix(og, dst);
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        String og = MysqlConnection.getInstance().getColumnByQuery("SELECT * FROM `pbx_pincode` WHERE `pincode`='" + key + "'", "outbound_group");
        String expiry = MysqlConnection.getInstance().getColumnByQuery("SELECT * FROM `pbx_pincode` WHERE `pincode`='" + key + "'", "expiry");
//        Integer cursecond = Integer.valueOf(MysqlConnection.getInstance().getColumnByQuery("SELECT * FROM `pbx_pincode` WHERE `pincode`='" + key + "'", "cursecond"));
//        Integer timeout = Integer.valueOf(MysqlConnection.getInstance().getColumnByQuery("SELECT * FROM `pbx_pincode` WHERE `pincode`='" + key + "'", "cursecond"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(expiry, formatter);
        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        LocalDateTime timeFromDatabase = timestamp.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        
        if (timeFromDatabase.isBefore(now)) {
            System.out.println("Pincode het han!");
            return "0";
        } else if (timeFromDatabase.isAfter(now)) {
            System.out.println("Pincode con han!");
        } else {
            System.out.println("Sau thong bao nay la pincode het han luon, kkk!");
        }
        
        if (og != null) {
            ArrayList<HashMap> rs2 = MysqlConnection.getInstance().getListByQuery("SELECT * FROM `pbx_outbound_prefix` WHERE `outbound_group`='" + og + "'");
            String rf;
            String checkrf;
            for (HashMap<String, String> data : rs2) {
                rf = data.get("prefix");
                if (rf.length() < dst.length()) {
                    checkrf = dst.substring(0, rf.length());
                    if (rf.equals(checkrf) && "1".equals(data.get("status"))) {
                        
                        return "1";
                    }
                }
            }
        } else {
            System.out.println("K lay duoc outbound group");
        }
        return "0";
    }

    public String checkAllowDenyNumber(String src, String dst) {
        try {
            String og = RedisConnection.getInstance().HGet("pbx_sipusers_" + src, "outbound_group");
            if (og != null) {
                return checkOutboundPrefix(og, dst);
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        String og = MysqlConnection.getInstance().getColumnByQuery("SELECT `outbound_group` FROM `sipusers` WHERE extension='" + src + "'", "outbound_group");
        if (og != null) {
            ArrayList<HashMap> rs = MysqlConnection.getInstance().getListByQuery("SELECT * FROM `pbx_outbound_prefix` WHERE `outbound_group`='" + og + "'");
            String rf, checkrf;
            for (HashMap<String, String> data : rs) {
                rf = data.get("prefix");
                if (rf.length() < dst.length()) {
                    checkrf = dst.substring(0, rf.length());
                    if (rf.equals(checkrf) && "1".equals(data.get("status"))) {
                        return "1";
                    }
                }
            }
        } else {
            System.out.println("K lay duoc outbound group");
        }
        return "0";
    }

    public String meetme(String confno) {
        try {
            String id = RedisConnection.getInstance().HGet("pbx_meetme_" + confno, "id");
            if (id != null) {
                return id;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getColumnByQuery("SELECT `id` FROM `meetme` WHERE `confno`='" + confno + "'", "id");
    }

    public HashMap<String, String> getIvrPlayback(String id) {
        try {
            List<String> ivr_playback = RedisConnection.getInstance().HMGet("pbx_ivr_playback_config:" + id, "url", "method", "voice", "context", "lastaction", "actiondefault");
            if (ivr_playback.get(0) != null) {
                HashMap<String, String> response = new HashMap();
                response.put("url", ivr_playback.get(0).trim());
                response.put("method", ivr_playback.get(1).trim());
                response.put("voice", ivr_playback.get(2).trim());
                response.put("context", ivr_playback.get(3).trim());
                response.put("lastaction", ivr_playback.get(4).trim());
                response.put("actiondefault", ivr_playback.get(5).trim());
                return response;
            }
        } catch (JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getOneByQuery("SELECT `url`,`method`, `voice`,`context`,`lastaction`,`actiondefault` FROM `ivr_playback_config` WHERE `id`=?", id);
    }

    public ArrayList<HashMap> getAllFieldsPlayback() {
        try {
            String fields = RedisConnection.getInstance().get("pbx_ivr_playback_field_config");
            if (fields != null) {
                ArrayList<HashMap> response = new ArrayList<>();
                JSONArray arr = new JSONArray(fields);
                for (int i = 0; i < arr.length(); i++) {
                    HashMap<String, String> hm = new HashMap();
                    Iterator<String> keysItr = arr.getJSONObject(i).keys();
                    while (keysItr.hasNext()) {
                        String key = keysItr.next();
                        String value = arr.getJSONObject(i).get(key).toString();
                        hm.put(key, value);
                    }
                    response.add(hm);
                }
                return response;
            }
        } catch (JSONException | JedisException ex) {
            System.out.println(ex);
        }
        return MysqlConnection.getInstance().getListByQuery("SELECT * FROM `ivr_playback_field_config` WHERE `customer_code`='C0001'");
    }

    public String getPatchVoiceACS(String voice) {
        HashMap pvoice = MysqlConnection.getInstance().getOneByQuery("SELECT `path` FROM `acs_voice` WHERE `id`=?", voice);
        if (pvoice != null) {
            return pvoice.get("path").toString().trim();
        }
        return "";
    }

    public HashMap<String, String> getInfoLcrgroup(String ext) {
        try {
            String callgroup = RedisConnection.getInstance().HGet("pbx_sipusers_" + ext, "callgroup");
            String usergroup = RedisConnection.getInstance().get("pbx_user_groups");
            if (callgroup != null && usergroup != null) {
                HashMap<String, String> lcr_group = new HashMap<>();
                JSONArray arr = new JSONArray(usergroup);
                for (int i = 0; i < arr.length(); i++) {
                    if (callgroup.equals(arr.getJSONObject(i).get("groupid").toString())) {
                        lcr_group.put("lcrgroup", arr.getJSONObject(i).get("lcrgroup").toString());
                        if (arr.getJSONObject(i).has("nDNC")) {
                            lcr_group.put("nDNC", arr.getJSONObject(i).get("nDNC").toString());
                        }
                        break;
                    }
                }
                return lcr_group;
            }
        } catch (JSONException | JedisException ex) {
            System.out.println(ex);
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT `ug`.`lcrgroup`, `ug`.`nDNC` ");
        sql.append("FROM `sipusers` AS sip ");
        sql.append("LEFT JOIN `user_groups` AS ug ON `sip`.`callgroup`=`ug`.`groupid` ");
        sql.append("WHERE `sip`.`extension`=?");
        HashMap<String, String> rs = MysqlConnection.getInstance().getOneByQuery(sql.toString(), ext);
        return rs;
    }

    public ArrayList<HashMap> getLcrByCustomerID(String lcrgroup) {
        try {
            String lcr = RedisConnection.getInstance().get("pbx_lcr");
            String trunk = RedisConnection.getInstance().get("pbx_trunk");
            if (lcr != null && trunk != null) {
                ArrayList<HashMap> response = new ArrayList<>();
                JSONArray arr_lcr = new JSONArray(lcr);
                JSONArray arr_trunk = new JSONArray(trunk);
                for (int i = 0; i < arr_lcr.length(); i++) {
                    if (arr_lcr.getJSONObject(i).get("status").toString().equals("1")) {
                        String group = arr_lcr.getJSONObject(i).get("lcrgroup").toString();
                        if (lcrgroup.equals(group)) {
                            String trunk_id = arr_lcr.getJSONObject(i).get("trunk_id").toString();
                            HashMap<String, String> hm = new HashMap();
                            for (int j = 0; j < arr_trunk.length(); j++) {
                                String id_cmp = arr_trunk.getJSONObject(j).get("id").toString();
                                if (trunk_id.equals(id_cmp)) {
                                    String trunk_name = arr_trunk.getJSONObject(j).get("name").toString();
                                    String record_calls = arr_trunk.getJSONObject(j).get("record_calls").toString();
                                    Iterator<String> keysItr = arr_lcr.getJSONObject(i).keys();
                                    while (keysItr.hasNext()) {
                                        String key = keysItr.next();
                                        String value = arr_lcr.getJSONObject(i).get(key).toString();
                                        hm.put(key, value);
                                    }
                                    hm.put("trunk_name", trunk_name);
                                    hm.put("record_calls", record_calls);
                                    break;
                                }
                            }
                            if (!hm.isEmpty()) {
                                response.add(hm);
                            }
                        }
                    }
                }
                /*
                Comparator<HashMap> priorityComparator = (HashMap o1, HashMap o2) -> {
                    // Get the distance and compare the distance.
                    Integer distance1 = Integer.parseInt(o1.get("priority").toString());
                    Integer distance2 = Integer.parseInt(o2.get("priority").toString());
                    return distance1.compareTo(distance2);
                };
                // And then sort it using collections.sort().
                Collections.sort(response, priorityComparator);
                 */
                return response;
            }
        } catch (JSONException | JedisException ex) {
            System.out.println(ex);
        }
        StringBuilder build = new StringBuilder();
        build.append("SELECT `pbx_lcr`.*,`pbx_trunk`.`name` AS `trunk_name`,`pbx_trunk`.`record_calls` ");
        build.append("FROM `pbx_lcr` ");
        build.append("LEFT JOIN `pbx_trunk` ON `pbx_lcr`.`trunk_id`=`pbx_trunk`.`id` ");
        build.append("WHERE `pbx_lcr`.`lcrgroup`=? AND `pbx_lcr`.`status`=1 ");
        build.append("ORDER BY `pbx_lcr`.`priority` ASC");
        return MysqlConnection.getInstance().getListByQuery(build.toString(), lcrgroup);
    }

    public String checkMNP(String phonenumber) {
        try {
            String carrier = RedisConnection.getInstance().get(phonenumber);
            if (carrier != null) {
                carrier = AESCipher.decrypt("C3fE" + phonenumber, carrier);
                return carrier;
            }
        } catch (JedisException ex) {
            System.out.println(Thread.currentThread().getName().concat("--").concat(ex.getMessage()));
        }
        return null;
    }

    public boolean checkDNC(String phonenumber) {
        try {
            return RedisDNCConnection.getInstance().sIsMember("nDNC", phonenumber);
        } catch (JedisException ex) {
            System.out.println(Thread.currentThread().getName().concat("--").concat(ex.getMessage()));
        }
        return false;
    }

    /**
     * createContextGoto
     *
     * @param type
     * @param dest
     * @return String
     */
    public String createContextGoto(String type, String dest) {
        String t = "";
        String d = "";
        switch (type) {
            case "hangup":
                t = type;
                d = "s";
                break;
            case "lcr":
                t = type;
                d = dest;
                break;
            case "internal":
                t = type;
                try {
                    d = RedisConnection.getInstance().HGet("pbx_sipusers:" + dest, "extension");
                    if (d != null) {
                        break;
                    }
                } catch (JedisException ex) {
                    System.out.println(ex);
                }
                d = MysqlConnection.getInstance().getColumnByQuery("SELECT `extension` FROM `sipusers` WHERE `id`=" + dest, "extension");
                break;
            case "queue":
                t = type;
                try {
                    d = RedisConnection.getInstance().HGet("pbx_queues:" + dest, "name");
                    if (d != null) {
                        break;
                    }
                } catch (JedisException ex) {
                    System.out.println(ex);
                }
                d = MysqlConnection.getInstance().getColumnByQuery("SELECT `name` FROM `queues` WHERE `id`=" + dest, "name");
                break;
            case "meetme":
                t = "confroomext";
                d = MysqlConnection.getInstance().getColumnByQuery("SELECT `confno` FROM `meetme` WHERE `id`=" + dest, "confno");
                break;
            case "voice-mail":
            case "voice-mail-main":
                t = type;
                d = MysqlConnection.getInstance().getColumnByQuery("SELECT `mailbox` FROM `voicemail` WHERE `id`=" + dest, "mailbox");
                break;
            case "ivr-menu":
                try {
                String name = RedisConnection.getInstance().HGet("pbx_ivr_menu_general:" + dest, "name");
                if (name != null) {
                    t = type + "-" + name;
                    d = "s";
                    break;
                }
            } catch (JedisException ex) {
                System.out.println(ex);
            }
            String temp = MysqlConnection.getInstance().getColumnByQuery("SELECT `name` FROM `pbx_ivr_menu_general` WHERE `id`=" + dest, "name");
            t = type + "-" + temp;
            d = "s";
            break;
            case "externalcrm":
                t = "ecrm-" + dest;
                d = "s";
                break;
            case "based-routing":
                t = "base-routing-" + dest;
                d = "s";
                break;
            case "ivr-playback":
                t = "playback_dynamic_" + dest;
                d = "s";
                break;
            case "acs":
                t = "auto_callout_mongo";
                d = "s";
                break;
        }
        return t + "," + d + ",1";
    }
    
    //
    public boolean setCallidWeb360(String uniqueid, String callid_web360) {
        //Mysql
        MysqlConnection.getInstance().executeNonQuery("INSERT INTO `pbx_callid_web360_temp`(`uniqueid`,`callid_web360`) VALUES (?,?)", uniqueid, callid_web360);
        return true;        
        //Memcached
//        MyMemcached.getInstance().set("callid_web360_" + uniqueid, callid_web360, new Date(System.currentTimeMillis() + 60000));
//        return RedisConnection.getInstance().set("callid_web360_" + uniqueid, callid_web360);
    }
    
    public String getCallidWeb360(String uniqueid) {
        String callid_web360 = null;
        try {
            return callid_web360 = MysqlConnection.getInstance().getColumnByQuery("SELECT `callid_web360` FROM `pbx_callid_web360_temp` WHERE `uniqueid`=? LIMIT 1", "callid_web360", uniqueid);
        } catch (JedisException ex) {
            System.out.println(Thread.currentThread().getName().concat("--").concat(ex.getMessage()));
        }
        return null;
    }
    
    public Integer getNcall() {
        try {
            HashMap rs = MysqlConnection.getInstance().getOneByQuery("SELECT `ncalls` FROM `pbx_ncalls` ORDER BY `id` DESC LIMIT 1");
            return Integer.valueOf((rs.get("ncalls").toString()));
        } catch (JedisException ex) {
            System.out.println(Thread.currentThread().getName().concat("--").concat(ex.getMessage()));
        }
        return null;
    }
    
    public Integer getCountCurCall() {
        try {
            HashMap rs = MysqlConnection.getInstance().getOneByQuery("SELECT `curcalls` FROM `pbx_ncalls` ORDER BY `id` DESC LIMIT 1");
            if(rs.get("curcalls") != null){
                return Integer.valueOf((rs.get("curcalls").toString()));
            }else{
                return null;
            }
        } catch (JedisException ex) {
            System.out.println(Thread.currentThread().getName().concat("--").concat(ex.getMessage()));
        }
        return null;
    }
    
    public boolean getNcallConfig() {
        try {
            HashMap rs = MysqlConnection.getInstance().getOneByQuery("SELECT `enable` FROM `pbx_ncalls` ORDER BY `id` DESC LIMIT 1");
            String enable = rs.get("enable").toString();
            Integer number_e = Integer.valueOf(enable);
            if(number_e > 0){
                return true;
            }else{
                return false;
            }
        } catch (JedisException ex) {
            System.out.println(Thread.currentThread().getName().concat("--").concat(ex.getMessage()));
        }
        return false;
    }
    
    //
    
    public Integer getMaxChannelConfig() {
        try {
            HashMap rs = MysqlConnection.getInstance().getOneByQuery("SELECT `value` FROM `pbx_setting` where `keyboard` = 'channel' LIMIT 1");
            String channel_str = rs.get("value").toString();
            Integer max_channel = Integer.valueOf(channel_str);
            return max_channel;
        } catch (JedisException ex) {
            System.out.println(Thread.currentThread().getName().concat("--").concat(ex.getMessage()));
        }
        return 0;
    }
    
    public String getCustomerID() {
    try {
        return "C001";
        } catch (JedisException ex) {
            System.out.println(Thread.currentThread().getName().concat("--").concat(ex.getMessage()));
        }
        return "C001";
    }
    
    public HashMap<String, String> getAppInfo(String extension) throws IOException {
        try {
            HashMap rs = MysqlConnection.getInstance().getOneByQuery("SELECT * FROM `app_login` where `extension` = " + extension + " LIMIT 1");
            return rs;
        } catch (JedisException ex) {
            System.out.println(Thread.currentThread().getName().concat("--").concat(ex.getMessage()));
        }
        return null;
    }
    
    public void logDialId(String calluuid, String dialid) {
        try {
            String cache_key = "dialid_" + calluuid;
            HashMap data_cache = new HashMap();
            data_cache.put("dialid", dialid);
            
            String json = JSONObject.valueToString(data_cache);
            MyMemcached.getInstance().set(cache_key, json, new Date(System.currentTimeMillis() + 60000));
//            System.out.println("Set Dialid successfully! " + "dialid_"+ calluuid + ":" + dialid);
       } catch (JSONException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public String getDialId(String calluuid) {
        try {
            String cache_key = "dialid_" + calluuid;
            Object oCache = MyMemcached.getInstance().get(cache_key); // Lấy Object thay vì gọi toString ngay
            if (oCache == null) {
//                System.out.println("Cache key not found: " + cache_key);
                return "";
            }

            String oCacheStr = oCache.toString();
            JSONObject dataCache = new JSONObject(oCacheStr);
            String dialId = dataCache.optString("dialid", null); // Sử dụng optString để tránh NullPointer
            if (dialId == null) {                               
//                System.out.println("Dialid not found in cache for key: " + cache_key);
                return "";
            }

//            System.out.println("Get Dialid successfully! " + cache_key + ":" + dialId);
            return dialId;
        } catch (JSONException ex) {
            System.out.println("JSONException: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
        }
        return "";
    }
    
}
