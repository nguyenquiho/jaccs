/*
 * Copyright © 2014 South Telecom
 */
package com.worldfonecc.callback;

import com.worldfonecc.queue.JobExternal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author nguyenngocbinh
 */
public class Callback {

    private List<NameValuePair> nvps = new ArrayList<>();
    private String callbackURL = null;

    public Callback() {

    }

    public Callback(String url) {
        this.callbackURL = url;
    }

    public void setCallbackURL(String url) {
        this.callbackURL = url;
    }

    public void addParam(String name, String key) {
        if (nvps == null) {
            nvps = new ArrayList<>();
        }
        nvps.add(new BasicNameValuePair(name, key));
    }

    public String sendGet(long receivedTimestamp0) throws URISyntaxException {
        URI uri = new URIBuilder(callbackURL).addParameters(nvps).build();
        JobExternal j = new JobExternal();
        long timestamp = System.currentTimeMillis() / 1000;
        j.addParam("receivedTimestamp0", receivedTimestamp0 + "");
        j.addParam("receivedTimestamp", timestamp + "");
        j.addParam("url", uri.toString());
        j.addParam("method", "get");
        j.addParam("headers", "");
        j.addParam("postfields", "");
        j.QueuePut();
        return "add queue";
    }

    public String sendGet(long receivedTimestamp0, int delay) throws URISyntaxException {
        URI uri = new URIBuilder(callbackURL).addParameters(nvps).build();
        JobExternal j = new JobExternal();
        long timestamp = System.currentTimeMillis() / 1000;
        j.addParam("receivedTimestamp0", receivedTimestamp0 + "");
        j.addParam("receivedTimestamp", timestamp + "");
        j.addParam("url", uri.toString());
        j.addParam("method", "get");
        j.addParam("headers", "");
        j.addParam("postfields", "");
        j.QueuePut(delay);
        return "add queue";
    }

}
