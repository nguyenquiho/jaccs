/*
 * This is Tin 
 */
package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
import com.worldfone.queue.JobUseraction;
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
public class ivrMenu extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            String menu_id = ar.getArguments()[1];
            HashMap rs = DBSelectionSet.getInstance().getIvrMenuDestination(menu_id);
            String no_sel = "";
            String no_sel_detail = "";
            String invalid_sel = "";
            String invalid_sel_detail = "";
            ac.verbose("Welcome ivr-menu", 1);
            if (rs != null) {
                no_sel = rs.get("no_sel").toString();
                no_sel_detail = rs.get("no_sel_detail").toString();
                invalid_sel = rs.get("invalid_sel").toString();
                invalid_sel_detail = rs.get("invalid_sel_detail").toString();
            }
            String digito = ar.getArguments()[0];
            if (digito == null || "".equals(digito)) {
                ac.verbose("k an phim", 1);
                String goToContext = DBSelectionSet.getInstance().createContextGoto(no_sel, no_sel_detail);
                ac.exec("Goto", goToContext);
            } else {
                String key;
                String type;
                String des;

                ac.setVariable("KEY_PRESS", digito);
                HashMap<String, String> rs2 = DBSelectionSet.getInstance().getIvrMenuOptionsKey(menu_id, digito);
                if (rs2 != null && rs2.containsKey("key") && rs2.containsKey("destination") && rs2.containsKey("details")) {
                    key = rs2.get("key");
                    type = rs2.get("destination");
                    des = rs2.get("details");

                    if (rs2.containsKey("useraction")) {
                        String user_action_conf = rs2.get("useraction");
                        if (user_action_conf != null && !"".equals(user_action_conf)) {
                            String[] arr_user_action_conf = user_action_conf.split(",");
                            if (arr_user_action_conf.length >= 1) {
                                String acs_var = null;
                                String ua_uuid = null;
                                try {
                                    acs_var = ar.getArguments()[2];
                                } catch (NullPointerException | ArrayIndexOutOfBoundsException ex) {
                                    System.out.println(ex.getMessage());
                                }
                                if (acs_var != null) {
                                    String[] acs_var1 = acs_var.split("@");
                                    ua_uuid = acs_var1[1];
                                }
                                for (String arr_user_action_conf1 : arr_user_action_conf) {
                                    String[] arr_user_action = arr_user_action_conf1.split("=");
                                    if (arr_user_action.length > 1) {
                                        JobUseraction jobAction = new JobUseraction();
                                        if (ua_uuid != null) {
                                            jobAction.addParam("calltype", "acs");
                                            jobAction.addParam("ua_uuid", ua_uuid);
                                        } else {
                                            jobAction.addParam("calltype", "survey");
                                        }
                                        jobAction.addParam("calluuid", ar.getUniqueId());
                                        jobAction.addParam("useraction", arr_user_action_conf1);
                                        jobAction.QueuePut();
                                    }
                                }
                            }
                        }
                    }
                    if (!"?".equals(key)) {
                        if (!"0".equals(des)) {
                            ac.verbose("An dung phim", 1);
                            String goToContext = DBSelectionSet.getInstance().createContextGoto(type, des);
                            ac.exec("Goto", goToContext);
                        }
                    } else {
                        if ("0".equals(des)) {
                            String sk = DBSelectionSet.getInstance().getAgentID(digito);
                            if (null == sk) {
                                String goToContext = DBSelectionSet.getInstance().createContextGoto(invalid_sel, invalid_sel_detail);
                                ac.exec("Goto", goToContext);
                                ac.verbose("An sai phim, set key nhung ma sai so noi bo", 1);
                            } else {
                                if (null == type) {
                                    ac.verbose("sai destination", 1);
                                } else {
                                    switch (type) {
                                        case "internal":
                                            ac.exec("Goto", "internal," + digito + ",1");
                                            ac.verbose("Di theo so noi bo", 1);
                                            break;
                                        case "externalcrm":
                                            String dest_id = DBSelectionSet.getInstance().checkExternal(digito);
                                            if (dest_id != null) {
                                                String goToContext = DBSelectionSet.getInstance().createContextGoto("externalcrm", dest_id);
                                                ac.exec("Goto", goToContext);
                                                ac.verbose("Di theo so noi bo - eCrm", 1);
                                            }
                                            break;
                                        default:
                                            ac.verbose("sai destination", 1);
                                            break;
                                    }
                                }
                            }
                        } else {
                            String goToContext = DBSelectionSet.getInstance().createContextGoto(type, des);
                            ac.exec("Goto", goToContext);
                            ac.verbose("An sai phim,co ? va khong set key", 1);
                        }
                    }
                } else {
                    ac.verbose("k set phim va k co ?", 1);
                    String goToContext = DBSelectionSet.getInstance().createContextGoto(invalid_sel, invalid_sel_detail);
                    ac.exec("Goto", goToContext);
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass() + ": " + ex.getMessage());
        }
    }
}
