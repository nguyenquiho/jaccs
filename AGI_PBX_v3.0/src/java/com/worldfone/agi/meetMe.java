/*
 * This is Tin 
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
public class meetMe extends BaseAgiScript {

    @Override
    public void service(AgiRequest ar, AgiChannel ac) {
        try {
            String conf = ar.getArguments()[0];
            ac.verbose("Vao agi meetme " + conf, 1);
            String confno = DBSelectionSet.getInstance().meetme(conf);
            if (confno == null) {
                ac.verbose("Sai so hoi nghi", 1);
                ac.hangup();
            } else {
                ac.verbose("Di vao hoi nghi", 1);
                ac.exec("Goto", "confroomext," + ar.getArguments()[0] + ",1");
            }
        } catch (AgiException ex) {
            System.out.println(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

}
