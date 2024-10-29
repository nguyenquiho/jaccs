/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.worldfone.access;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

/**
 *
 * @author MitsuyoRai
 */
public final class RedisConnection {

    private static volatile RedisConnection instance;

    public static RedisConnection getInstance() {
        RedisConnection _instance = RedisConnection.instance;
        if (_instance == null) {
            synchronized (RedisConnection.class) {
                _instance = RedisConnection.instance;
                if (null == _instance) {
                    RedisConnection.instance = _instance = new RedisConnection();
                }
            }
        }
        return _instance;
    }

    private static String rHost = "127.0.0.1";
    private static int rPort = 6799;
    private static String rPwd = "";
    private static int rTimeout = 30000;
    private JedisPool jedisPool = null;

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

    private RedisConnection() {
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

    final public void select(int db) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(db);
        }
    }

    final public String get(String key) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    final public boolean set(String key, String value) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
            return true;
        }
    }

    final public long del(String key) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(key);
        }
    }

    final public Set<String> keys(String pattern) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(pattern);
        }
    }

    final public boolean exists(String key) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    final public long HSet(String key, Map<String, String> map) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hset(key, map);
        }
    }

    final public String HGet(String key, String field) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }

    final public String HMSet(String key, Map<String, String> map) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hmset(key, map);
        }
    }

    final public List<String> HMGet(String key, String... fields) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hmget(key, fields);
        }
    }

    final public long HDel(String key, String... fields) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hdel(key, fields);
        }
    }

    final public Set<String> HMKeys(String key) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hkeys(key);
        }
    }

    final public boolean sIsMember(String keys, String member) throws JedisException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sismember(keys, member);
        }
    }
}
