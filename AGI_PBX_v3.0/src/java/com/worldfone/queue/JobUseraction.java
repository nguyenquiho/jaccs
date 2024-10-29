/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfone.queue;

import dk.safl.beanstemc.Beanstemc;
import dk.safl.beanstemc.BeanstemcException;
import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Dai Le
 */
public class JobUseraction {

    private static final String BEANSTALKD_HOST = "127.0.0.1";
    private static final int BEANSTALKD_PORT = 11300;
    private static final String BEANSTALKD_TUBENAME = "ivr_user_action";

    private JSONObject payload = new JSONObject();
    private long priority = 0;

    public JobUseraction() {
//        ZonedDateTime nowZoned = ZonedDateTime.now();
//        Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
//        Duration duration = Duration.between(midnight, Instant.now());
//        priority = duration.getSeconds();

        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        priority = now.toSecondOfDay();
    }

    public void addParam(String name, String key) {
        try {
            if (payload == null) {
                payload = new JSONObject();
            }
            payload.put(name, key);
        } catch (JSONException ex) {
            Logger.getLogger(JobExternal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void QueuePut() {
        try {
            Beanstemc queue = new Beanstemc(BEANSTALKD_HOST, BEANSTALKD_PORT);
            queue.use(BEANSTALKD_TUBENAME).put(payload.toString().getBytes(), priority);
            queue.close();
        } catch (IOException | BeanstemcException ex) {
            Logger.getLogger(JobExternal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
