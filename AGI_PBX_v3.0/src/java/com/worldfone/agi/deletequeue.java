/*
 * This is Tin java
 */
package com.worldfone.agi;

import com.worldfone.access.DBSelectionSet;
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
public class deletequeue extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            ac.verbose("delete queue", 1);
            String queue = ar.getArguments()[0];
            String agent = "sip/" + ar.getArguments()[1];
            DBSelectionSet.getInstance().deleteAgent(queue, agent);
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

}
