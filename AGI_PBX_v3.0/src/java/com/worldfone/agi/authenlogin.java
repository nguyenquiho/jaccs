/*
 * This is Tin java
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
public class authenlogin extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            ac.verbose("Authen login", 1);
            String extension = ar.getArguments()[0];
            String agent = ar.getArguments()[1];
            String pass = ar.getArguments()[2];
            String sk = ar.getArguments()[3];
            String type = "";
            String detail = "";
            boolean flag = false;
            HashMap<String, String> rs = DBSelectionSet.getInstance().checkShortKey(sk);
            if (rs != null) {
                ac.verbose("pass shortkey ", 1);
                type = rs.get("destination");
                detail = rs.get("details");
            }
            boolean flag2 = DBSelectionSet.getInstance().checkAgent(agent, pass);
            if (flag && flag2) {
                ac.verbose("pass agent check ", 1);
                ac.verbose("Di theo short key", 1);
                ac.verbose("type: " + type, 1);
                if ("loginqueue".equalsIgnoreCase(type) || "logoutqueue".equalsIgnoreCase(type) || "pausequeue".equalsIgnoreCase(type) || "unpausequeue".equalsIgnoreCase(type)) {
                    String queue = DBSelectionSet.getInstance().pbxQueues(detail);
                    if (queue != null) {
                        String chanel = ar.getChannel();
                        String[] a = chanel.split("-");
                        String[] b = a[0].split("/");
                        String src = b[1];
                        ac.setVariable("QueueIO", queue);
                        ac.verbose("QueueIO: " + queue, 1);
                        String text = "loginqueue";
                        String action = "Login Queue";
                        String fi;
                        if ("loginqueue".equalsIgnoreCase(type)) {
                            fi = "sip/" + src;
                            String data = fi + " (Not in use) has taken 0 calls";
                            if (DBSelectionSet.getInstance().checkQueue(queue, fi)) {
                                DBSelectionSet.getInstance().updateAgent(queue, fi);
                            } else {
                                DBSelectionSet.getInstance().insertAgent(queue, fi, data);
                            }
                            if (!DBSelectionSet.getInstance().checkAgentActivity(agent, extension)) {
                                DBSelectionSet.getInstance().insertAgentActivity(agent, extension);
                            }
                        }
                        if (type.equalsIgnoreCase("logoutqueue")) {
                            text = "logoutqueue";
                            action = "Logout Queue";
                            //fi = "sip/" + src;
                            DBSelectionSet.getInstance().updateAgentActivity(agent, extension);
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
                        ac.verbose("Queue ko ton tai", 1);
                        ac.hangup();
                    }
                } else {
                    String contextGoTo = DBSelectionSet.getInstance().createContextGoto(type, detail);
                    ac.exec("Goto", contextGoTo);
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}
