/*
 * This is Tin java
 */
package com.worldfone.agi;

//import access.RedisUtils;
import com.worldfone.access.DBSelectionSet;
import com.worldfone.callback.Callback;
import com.worldfone.utils.helpers.AsteriskCommon;
import com.worldfone.utils.helpers.BasedCommon;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Tin
 * @develop MitsuyoRai_DaiLe
 *
 */
public class baseskillrouting extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        //System.out.println("baseskillrouting......");
        try {
            String did = ar.getDnid();
            String id = ar.getArguments()[1];
            String src = ar.getArguments()[0];
            ac.verbose("Base Skill Routing: ", 1);
            HashMap<String, String> routing = DBSelectionSet.getInstance().getRoutingInfo(id);
            if (routing != null) {
                try {
                    String calluuid = ar.getUniqueId();
                    String ip = routing.get("rout_url");
                    String secret = routing.get("secret");
                    String default_type = routing.get("default_type");
                    String defaul_id = routing.get("default_id");
                    String go_to = "";
                    String go_type = "";
                    Callback c = new Callback(ip);
                    c.addParam("secret", secret);
                    c.addParam("callernumber", src);
                    c.addParam("didnumber", did);
                    c.addParam("calluuid", calluuid);
                    String result = c.getResponseGet();
                    //System.out.println(result);
                    if (BasedCommon.isJSONValid(result)) {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.has("extension") && !"".equalsIgnoreCase(jsonObject.get("extension").toString())) {
                            ac.verbose("extension: " + jsonObject.get("extension").toString(), 1);
                            String extension = jsonObject.get("extension").toString();
                            //this.checkIDLE(extension);
                            if (AsteriskCommon.checkIDLE(extension)) {
                                String destid = DBSelectionSet.getInstance().checkExternal(extension, "extension");
                                if (destid != null) {
                                    go_to = destid;
                                    go_type = "externalcrm";
                                } else {
                                    go_to = extension;
                                    go_type = "internal";
                                }
                            }
                        } else if (jsonObject.has("queue") && !"".equalsIgnoreCase(jsonObject.get("queue").toString())) {
                            ac.verbose("queue: " + jsonObject.get("queue").toString(), 1);
                            String queue = jsonObject.get("queue").toString();
                            if (DBSelectionSet.getInstance().isqueue(queue)) {
                                String destid = DBSelectionSet.getInstance().checkExternal(queue, "queue");
                                if (destid != null) {
                                    go_to = destid;
                                    go_type = "externalcrm";
                                } else {
                                    go_to = queue;
                                    go_type = "queue";
                                }
                            }
                        }
                    }
                    String context_goto;
                    if (!"".equalsIgnoreCase(go_to)) {
                        context_goto = DBSelectionSet.getInstance().createContextGoto(go_type, go_to);
                    } else {
                        context_goto = DBSelectionSet.getInstance().createContextGoto(default_type, defaul_id);
                    }
                    ac.exec("Goto", context_goto);
                } catch (URISyntaxException | JSONException | IOException ex) {
                    Logger.getLogger(baseskillrouting.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

}
