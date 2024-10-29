/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfone.access;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

/**
 *
 * @author MitsuyoRai
 */
public final class RedisDNCConnection {

    private static volatile RedisDNCConnection instance;

    public static RedisDNCConnection getInstance() {
        RedisDNCConnection _instance = RedisDNCConnection.instance;
        if (_instance == null) {
            synchronized (RedisDNCConnection.class) {
                _instance = RedisDNCConnection.instance;
                if (null == _instance) {
                    RedisDNCConnection.instance = _instance = new RedisDNCConnection();
                }
            }
        }
        return _instance;
    }

    private static String rHost = "127.0.0.1";
    private static int rPort = 6799;
    private static int rTimeout = 30000;
    private static String rPwd = "";
    private static JedisPool jedisPool;
    private static final int DB_IDX = 1;

    private static JedisPool createPool() {
        Properties props = new Properties();
        try {
            FileInputStream in = new FileInputStream("/opt/tomcat9/webapps/AGI_PBX_Config.properties");
            props.load(in);
            if (props.containsKey("redis.host")) {
                rHost = props.getProperty("redis.host");
            }
            if (props.containsKey("redis.port")) {
                rPort = Integer.parseInt(props.getProperty("redis.port"));
            }
            if (props.containsKey("redis.timeout")) {
                rTimeout = Integer.parseInt(props.getProperty("redis.timeout"));
            }
            if (props.containsKey("redis.pwd")) {
                rPwd = props.getProperty("redis.pwd");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(64);
        if (!"".equals(rPwd)) {
            return new JedisPool(poolConfig, rHost, rPort, rTimeout, rPwd);
        }
        return new JedisPool(poolConfig, rHost, rPort, rTimeout);
    }

    private RedisDNCConnection() {
        jedisPool = createPool();
    }

    @Override
    protected void finalize() {
        try {
            if (!jedisPool.isClosed()) {
                jedisPool.close();
            }
            super.finalize();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

    final public boolean sIsMember(String keys, String member) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(DB_IDX);
            return jedis.sismember(keys, member);
        }
    }
}
