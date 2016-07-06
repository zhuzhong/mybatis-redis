/**
 * 
 */
package org.mybatis.cache.redis;

import java.io.InputStream;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 实现原理：<br>
 * 存储两种数据结构， 第一种key,value <br>
 * 第二种与id,相关的key集合
 * 
 * @author Administrator
 *
 */
public class RedisCacheFactory {

    private JedisPool jedisPool;

    private static RedisCacheFactory instance = new RedisCacheFactory();

    public static RedisCacheFactory instance() {
        return instance;
    }

    private RedisCacheFactory() {
        // 目前只针对单机版redis
        initJedisPool();
    }

    private void initJedisPool() {
        // 读取关于redis的配置文件，然后实例化redis
        InputStream input = null;
        if ((input = getClass().getClassLoader().getResourceAsStream(Configs.default_config)) == null) {
            if (System.getProperty(Configs.system_path) == null) {
                throw new RuntimeException("redis配置文件不存在");
            }
            input = getClass().getResourceAsStream(System.getProperty(Configs.system_path));
        }
        if (input == null) {
            throw new RuntimeException("redis配置文件不存在");
        }
        Configs config = new Configs(input);

        jedisPool = new JedisPool(config.host(), config.port());
    }

    private String newId(String id) {
        return "MYBATIS-REDIS-KEY-" + id;
    }

    public int getSizeById(String id) {

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 缓存值
            return jedis.scard(SerializeUtil.serialize(newId(id))).intValue();

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void putObject(Object key, Object value, String id) {

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 缓存值
            jedis.set(SerializeUtil.serialize(key), SerializeUtil.serialize(value));
            // 缓存key列表
            jedis.sadd(SerializeUtil.serialize(newId(id)), SerializeUtil.serialize(key));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    public Object getObject(Object key) {

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 缓存值
            return jedis.get(SerializeUtil.serialize(key)) == null ? null : SerializeUtil.deserialize(jedis
                    .get(SerializeUtil.serialize(key)));

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    public Object removeObject(Object key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 缓存值
            jedis.del(SerializeUtil.serialize(key));
            return true;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void clear(String id) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Set<byte[]> keys = jedis.smembers(SerializeUtil.serialize(newId(id))); // 获取
            for (byte[] k : keys) {
                jedis.del(k); // 根据key删除value
            }

            // 删除key
            jedis.del(SerializeUtil.serialize(newId(id)));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
