/*
 * Copyright © 2014 South Telecom
 */
package com.worldfone.callback;

import com.worldfone.queue.JobExternal;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.asteriskjava.util.Log;
import org.asteriskjava.util.LogFactory;

/**
 *
 * @author nguyenngocbinh
 */
public class Callback2 {

    private final Log logger = LogFactory.getLog(getClass());
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private List<NameValuePair> nvps = new ArrayList<>();
    private String callbackURL = null;

    public Callback2() {
    }

    public Callback2(String url) {
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

    public String getResponseGet() throws URISyntaxException, IOException {
        URI uri = new HttpGet(callbackURL).getURI();
        HttpGet httpGet = new HttpGet(new URIBuilder().setScheme(uri.getScheme()).setHost(uri.getHost())
                .setPort(uri.getPort()).setPath(uri.getPath()).addParameters(nvps).build());

        logger.debug(nvps);
        System.out.println(httpGet);
        // Create a custom response handler from vtiger
        ResponseHandler<String> responseHandler = (final HttpResponse response) -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) + " Success" : "Success";
            } else {
                return "Failure";
            }
        };
        String responseBody = httpclient.execute(httpGet, responseHandler);
        return responseBody;
    }

    public String sendGet(long receivedTimestamp0) throws URISyntaxException {
        URI uri = new URIBuilder(callbackURL)
                .addParameters(nvps)
                .build();
        JobExternal j = new JobExternal();
        long timestamp = System.currentTimeMillis() / 1000;
        j.addParam("receivedTimestamp0", receivedTimestamp0 + "");
        j.addParam("receivedTimestamp", timestamp + "");
        j.addParam("url", uri.toString());
        j.addParam("method", "get");
        j.addParam("headers", "");
        j.addParam("postfields", "");
        j.QueuePut();
        System.out.println("ADDED QUEUEEEEEEEEEE");
        return "add queue";
    }

    class CallbackThreadUncaughtExceptionHanlder implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            logger.error("Uncaught exception in CallbackVtiger Async", e);
        }
    }

}
