/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfonecc.queue;

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
public class JobCdrEvent {

    private JSONObject payload = new JSONObject();
    private long priority = 0;

    public JobCdrEvent() {
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
        QueuePut(Beanstemc.DEFAULT_DELAY);
    }

    public void QueuePut(int delay) {
        try {
            Beanstemc queue = new Beanstemc("127.0.0.1", 11300);
            queue.use("cdr_sync_event").put(payload.toString().getBytes(), priority, delay);
            queue.close();
        } catch (IOException | BeanstemcException ex) {
            Logger.getLogger(JobExternal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
