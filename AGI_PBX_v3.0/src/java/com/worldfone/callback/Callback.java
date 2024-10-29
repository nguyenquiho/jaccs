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
//import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.asteriskjava.util.Log;
import org.asteriskjava.util.LogFactory;
import org.apache.http.concurrent.FutureCallback;
import org.json.JSONObject;

/**
 *
 * @author nguyenngocbinh
 */
public class Callback {

    private final Log logger = LogFactory.getLog(getClass());
//    private static final AtomicLong idCounter = new AtomicLong();
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private List<NameValuePair> nvps = new ArrayList<>();
    private String callbackURL = null;
//    private Object objectBean = null;

    public Callback() {

    }

    public Callback(String url) {
        this.callbackURL = url;
    }

    public void setCallbackURL(String url) {
        this.callbackURL = url;
    }

//    public void setObjectBean(Object bean) {
//        this.objectBean = bean;
//    }

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

    public String sendGet2(long receivedTimestamp0) throws URISyntaxException {
//        CredentialsProvider credsProvider = new BasicCredentialsProvider();
//   
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setSocketTimeout(3000)
//                .setConnectTimeout(3000).build();
//
//        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
//                .setDefaultRequestConfig(requestConfig)
//                .build();
//        client.start();
//        JSONObject payload = new JSONObject();
        //URI uri = new HttpGet(callbackURL).getURI();
        //  URI newUri = new URI.Builder()(uri.getScheme(), uri.getAuthority(),uri.getPath(),nvps , uri.getFragment());
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
//        final HttpUriRequest request = RequestBuilder.get(uri).build();
//       // System.out.println(request);
//        String responseBody="";
//       
//        client.execute(request, new FutureCallback<HttpResponse>() {
//            @Override
//            public void completed(final HttpResponse response) {
//                
//                // Process update sending status and update to database ...
//                int statusCode = response.getStatusLine().getStatusCode();
//                System.out.println(request.getRequestLine()+" "+ statusCode);
//                if (statusCode != 200) {
//                    System.out.println("Fail. Status code = " + statusCode);
//                } else {
//                    try {
//                        String responseBody = EntityUtils.toString(response.getEntity());
//                        //System.out.println(request.getRequestLine() + responseBody);
//                        
//                    } catch (IOException ex) {
//                        try {
//                            client.close();
//                            //Network error, please try again or try with other api host
//                            System.out.println(request.getRequestLine()+"->"+ ex);
//                        } catch (IOException ex1) {
//                            Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex1);
//                        }
//                    } 
//                }
//                try {
//                    client.close();
//                } catch (IOException ex) {
//                    System.out.println( ex);
//                }
//            }
//
//            @Override
//            public void failed(final Exception ex) {
//                System.out.println(request.getRequestLine() + "completed time out ->" + ex);
//                try {
//                    client.close();
//                } catch (IOException ex1) {
//                    System.out.println(ex1);
//                }
//            }
//
//            @Override
//            public void cancelled() {
//                try {
//                    client.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                System.out.println(request.getRequestLine() + " cancelled");
//            }
//
//        });
//         
//        try {
//            if(client.isRunning()){
//                synchronized(client){
//                     client.wait(1000);
//                     client.close();
//                }
//               
//                
//            }
//            
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        catch (IllegalMonitorStateException ex) {
//            Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        catch (IOException ex) {
//            Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return "add queue";
    }

    public String sendGet() throws URISyntaxException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000)
                .setConnectTimeout(3000).build();

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
        client.start();
        JSONObject payload = new JSONObject();
        //URI uri = new HttpGet(callbackURL).getURI();
        //  URI newUri = new URI.Builder()(uri.getScheme(), uri.getAuthority(),uri.getPath(),nvps , uri.getFragment());
        URI uri = new URIBuilder(callbackURL)
                .addParameters(nvps)
                .build();

        final HttpUriRequest request = RequestBuilder.get(uri).build();
        // System.out.println(request);
        String responseBody = "";

        client.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse response) {

                // Process update sending status and update to database ...
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println(request.getRequestLine() + " " + statusCode);
                if (statusCode != 200) {
                    System.out.println("Fail. Status code = " + statusCode);
                } else {
                    try {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        //System.out.println(request.getRequestLine() + responseBody);

                    } catch (IOException ex) {
                        try {
                            client.close();
                            //Network error, please try again or try with other api host
                            System.out.println(request.getRequestLine() + "->" + ex);
                        } catch (IOException ex1) {
                            Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }
                }
                try {
                    client.close();
                } catch (IOException ex) {
                    System.out.println(ex);
                }
            }

            @Override
            public void failed(final Exception ex) {
                System.out.println(request.getRequestLine() + "completed time out ->" + ex);
                try {
                    client.close();
                } catch (IOException ex1) {
                    System.out.println(ex1);
                }
            }

            @Override
            public void cancelled() {
                try {
                    client.close();
                } catch (IOException ex) {
                    Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println(request.getRequestLine() + " cancelled");
            }

        });

        try {
            if (client.isRunning()) {
                synchronized (client) {
                    client.wait(1000);
                    client.close();
                }
            }
        } catch (InterruptedException | IllegalMonitorStateException | IOException ex) {
            Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex);
        }
        return responseBody;
    }
//    public void sendAsync()
//    {
//        Thread t;
//
//        t = new Thread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                try
//                {
//                    sendGet();
//                }
//                catch (IOException e)
//                {
//                    logger.error("Exception running CallbackVtiger.", e);
//                } catch (URISyntaxException ex) {
//                    logger.error("URISyntaxException", ex);
//                } catch (NoSuchAlgorithmException ex) {
//                    Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (KeyStoreException ex) {
//                    Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (KeyManagementException ex) {
//                    Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        });
//        t.setName("CallbackVtiger-" + idCounter.getAndIncrement());
//        t.setDaemon(true);
//        t.setUncaughtExceptionHandler(new CallbackThreadUncaughtExceptionHanlder());
//        t.start();
//    }
//    public void sendAsync()
//    {
//        Thread t;
//
//        t = new Thread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//
//                try {
//                    sendGet();
//                } catch (URISyntaxException ex) {
//                    Logger.getLogger(Callback.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        });
//        t.setName("CallbackVtiger-" + idCounter.getAndIncrement());
//        t.setDaemon(true);
//        t.setUncaughtExceptionHandler(new CallbackThreadUncaughtExceptionHanlder());
//        t.start();
//    }

    class CallbackThreadUncaughtExceptionHanlder implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            logger.error("Uncaught exception in CallbackVtiger Async", e);
        }
    }

}
