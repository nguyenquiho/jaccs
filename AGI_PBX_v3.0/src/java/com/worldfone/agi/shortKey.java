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
public class shortKey extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            ac.verbose("Welcome short key", 1);
            String sk = ar.getArguments()[0];
            String type;
            String detail;
            int check_login;
            HashMap<String, String> rs = DBSelectionSet.getInstance().checkShortKey(sk);
            if (rs != null) {
                type = rs.get("destination");
                detail = rs.get("details");
                check_login = Integer.parseInt(rs.get("check_login"));
                ac.verbose("Di theo short key", 1);
                ac.verbose("type: " + type, 1);
                if ("loginqueue".equalsIgnoreCase(type)
                        || "logoutqueue".equalsIgnoreCase(type)
                        || "pausequeue".equalsIgnoreCase(type)
                        || "unpausequeue".equalsIgnoreCase(type)) {
                    if (check_login == 0) {
                        String queue = DBSelectionSet.getInstance().pbxQueues(detail);
                        String chanel = ar.getChannel();
                        String[] a = chanel.split("-");
                        String[] b = a[0].split("/");
                        String src = b[1];
                        ac.setVariable("QueueIO", queue);
                        ac.verbose("QueueIO: " + queue, 1);
                        String text = "loginqueue";
                        String action = "Login Queue";
                        String fi;
                        if (type.equalsIgnoreCase("loginqueue")) {
                            fi = "sip/" + src;
                            String data = fi + " (Not in use) has taken 0 calls";
                            if (DBSelectionSet.getInstance().checkQueue(queue, fi)) {
                                DBSelectionSet.getInstance().updateAgent(queue, fi);
                            } else {
                                DBSelectionSet.getInstance().insertAgent(queue, fi, data);
                            }
                        }
                        if (type.equalsIgnoreCase("logoutqueue")) {
                            text = "logoutqueue";
                            action = "Logout Queue";
//                        fi = "sip/" + src;
//                        DBSelectionSet.getInstance().deleteAgent(queue, fi);
                        }
                        if (type.equalsIgnoreCase("pausequeue")) {
                            text = "pausequeue";
                            action = "Pause Queue";
                        }
                        if (type.equalsIgnoreCase("unpausequeue")) {
                            text = "unpausequeue";
                            action = "Unpause Queue";
                        }
                        DBSelectionSet.getInstance().insertQueueMessage(src, action, queue);
                        String context_goto = text + ",s,1";
                        ac.verbose("goto: " + context_goto, 1);
                        ac.exec("Goto", context_goto);
                    } else {
                        ac.setVariable("shortkey", sk);
                        ac.exec("Goto", "authen_login_queue,s,1");
                    }
                } else {
                    String contextGoTo = DBSelectionSet.getInstance().createContextGoto(type, detail);
                    ac.exec("Goto", contextGoTo);
                }
            } else {
                ac.verbose("Dang kiem tra", 1);
                rs = DBSelectionSet.getInstance().checkShortKeyP2(sk);
                if (rs != null) {
                    String ext = rs.get("extension");
                    type = rs.get("destination");
                    ac.setVariable(type, ext);
                    ac.exec("Goto", type + ",s,1");
                } else {
                    ac.verbose("Khong co short key nay", 1);
                    ac.hangup();
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}
