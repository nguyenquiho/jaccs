/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfonecc.cache;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author tin
 */
public class MyMemcached {

    private static volatile MyMemcached instance;

    public static MyMemcached getInstance() {
        MyMemcached _instance = MyMemcached.instance;
        if (_instance == null) {
            synchronized (MyMemcached.class) {
                _instance = MyMemcached.instance;
                if (_instance == null) {
                    MyMemcached.instance = _instance = new MyMemcached();
                }
            }
        }
        return _instance;
    }

    private static final SockIOPool pool = SockIOPool.getInstance();
    private static final MemCachedClient mcc = createMemcacheClient();

    private static MemCachedClient createMemcacheClient() {
        try {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("/opt/tomcat9/webapps/ECRM_PBX_Config.properties");
            props.load(in);
            String host = props.getProperty("HOST");

            pool.setServers(host.split(","));
            pool.setWeights(getWeightsProperty(props.getProperty("WEIGHTS")));                 // 3,3
            pool.setInitConn(Integer.parseInt(props.getProperty("INIT_CONN")));                // 5
            pool.setMinConn(Integer.parseInt(props.getProperty("MIN_CONN")));                  // 5
            pool.setMaxConn(Integer.parseInt(props.getProperty("MAX_CONN")));                  // 250
            pool.setMaintSleep(Integer.parseInt(props.getProperty("MAINT_SLEEP")));            // 30
            pool.setNagle(Boolean.parseBoolean(props.getProperty("NAGLE")));                   // FALSE
            pool.setMaxIdle(Long.parseLong(props.getProperty("MAX_IDLE")));                    // 21600000
            pool.setSocketTO(Integer.parseInt(props.getProperty("SOCKET_TO")));                // 3000
            pool.setSocketConnectTO(Integer.parseInt(props.getProperty("SOCKET_CONNECT_TO"))); // 0
            pool.initialize();

            return new MemCachedClient();
        } catch (IOException ex) {
            System.out.println("Error: Can not connect to memcached: " + ex.getMessage());
        }
        return null;
    }

    // sử lý tách các weights ra set vào một mảng Integer
    private static Integer[] getWeightsProperty(String weights) {
        String[] strWeights = weights.split(",");
        Integer[] IntWeights = new Integer[strWeights.length];
        int i = 0;
        for (String strWeight : strWeights) {
            IntWeights[i] = Integer.valueOf(strWeight);
            i++;
        }
        return IntWeights;
    }

    // Khởi tạo và kết nối đến Memcached server
    private MyMemcached() {
    }

    @Override
    protected void finalize() {
        try {
            pool.shutDown();
            super.finalize();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

    //Hàm get một Object từ Memcached server thông qua key.
    public Object get(String key) {
        // System.out.println(this.getClass().getName() + " get(key) : " + key);
        Object obj = mcc.get(key);
//        if (obj == null) {
//            System.out.println(this.getClass().getName() + " get : Object NULL");
//        } else {
//            System.out.println(this.getClass().getName() + " get : " + obj.toString());
//        }
        return obj;
    }

    //Hàm set một Object vào Memcached server
    public void set(String key, Object value, Date date) {
        //date= new Date(System.currentTimeMillis()+5*60*1000);
        // System.out.println(date);
        boolean seted = mcc.set(key, value, date);
        if (!seted) {
            System.out.println(this.getClass().getName() + " set key error");
        }
    }

    //Hàm update một Object vào Memcached server
    public void update(String key, Object value) {
        System.out.println(this.getClass().getName() + " set(key) : " + key);
        //Date date= new Date(System.currentTimeMillis()+5*60*1000);
        boolean updated = mcc.replace(key, value);
        if (updated) {
            System.out.println(this.getClass().getName() + " update key success");
        } else {
            System.out.println(this.getClass().getName() + " update key error");
        }
    }
    
    //Hàm update một Object vào Memcached server
    public void update(String key, Object value, Date date) {
        System.out.println(this.getClass().getName() + " set(key) : " + key);
        //Date date= new Date(System.currentTimeMillis()+5*60*1000);
        boolean updated = mcc.replace(key, value, date);
        if (updated) {
            System.out.println(this.getClass().getName() + " update key success");
        } else {
            System.out.println(this.getClass().getName() + " update key error");
        }
    }

    //Hàm get một Object từ Memcached server thông qua key.
    public void delete(String key) {
        System.out.println(this.getClass().getName() + " delete(key) : " + key);
        boolean deleted = mcc.delete(key);
        if (deleted) {
            System.out.println(this.getClass().getName() + " delete key success");
        } else {
            System.out.println(this.getClass().getName() + " delete key error");
        }
    }

}
