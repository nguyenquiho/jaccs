/*
 * This is Tin 
 */
package com.worldfone.agi;

import com.worldfone.utils.helpers.AsteriskCommon;
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
public class queue_status extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            ac.verbose("welcome agi queue status", 1);
            String queue = ar.getArguments()[0];
            int queue_status = AsteriskCommon.QueueCountAvailable(queue);
            if (queue_status == 0) {
                ac.setVariable("CHANNEL(musicclass)", "busy");
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ":" + ex.getMessage());
        }

    }
}
