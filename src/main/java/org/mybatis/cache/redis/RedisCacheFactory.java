/**
 * 
 */
package org.mybatis.cache.redis;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.cache.CacheKey;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcUtils;

/**
 * 实现原理：<br>
 * 存储两种数据结构， 1.key,value <br>
 * 2.mapper id与相关的statement id 生成的key集合 <br>
 * 3.table 表名 与statement id生成的key集合
 * 
 * @autthor Administrator
 *
 */
public class RedisCacheFactory {

    private Configs config;
    private JedisPool jedisPool;
    private ObjectSerializer serializer;
    private static RedisCacheFactory instance = new RedisCacheFactory();

    public static RedisCacheFactory instance() {
        return instance;
    }

    private RedisCacheFactory() {
        // 目前只针对单机版redis
        initJedisPool();
    }

    private void initJedisPool() {
        Properties p = new Properties();
        // 读取关于redis的配置文件，然后实例化redis
        InputStream input = getClass().getClassLoader().getResourceAsStream(Configs.default_config);
        loadProperties(p, input);
        if (System.getProperty(Configs.system_path) != null) {
            input = getClass().getResourceAsStream(System.getProperty(Configs.system_path));
            loadProperties(p, input);
        }
        if (getClass().getClassLoader().getResourceAsStream(Configs.user_config) != null) {
            input = getClass().getClassLoader().getResourceAsStream(Configs.user_config);
            loadProperties(p, input);
        }

        /*
         * if (input == null) { throw new RuntimeException("redis配置文件不存在"); }
         */
        config = new Configs(p);
        serializer = config.getSerialize();
        jedisPool = new JedisPool(config.host(), config.port());
    }

    private void loadProperties(Properties p, InputStream in) {
        if (in != null) {
            try {
                p.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String idKey(String id) {
        return "MYBATIS-REDIS-MAPPER-" + id;
    }

    private String tableKey(String tableName) {
        return "MYBATIS-REDIS-TABLE-" + tableName;
    }

    public int getSizeById(String id) {

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 缓存值
            return jedis.scard(serializer.serialize(idKey(id))).intValue();

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
            jedis.set(serializer.serialize(key), serializer.serialize(value));
            // 缓存key列表
            jedis.sadd(serializer.serialize(idKey(id)), serializer.serialize(key));
            if (config.useGrainedCahe()) {
                // 缓存table与key 列表
                Set<String> tables = getTables(key);
                for (String name : tables) {
                    jedis.sadd(serializer.serialize(tableKey(name)), serializer.serialize(key));
                }
            }

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    // 根据cacheKey获取 table表名
    private Set<String> getTables(Object key) {
        CacheKey cacheKey = (CacheKey) key;
        String[] strs = cacheKey.toString().split(":");
        String sql = strs[5];
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, config.dbtype());
        SQLStatement statement = parser.parseStatement();
        SchemaStatVisitor statVisitor = config.dbtype().equalsIgnoreCase(JdbcUtils.MYSQL) ? new MySqlSchemaStatVisitor()
                : new OracleSchemaStatVisitor();
        statement.accept(statVisitor);

        Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
        Set<String> tableString = new HashSet<String>();
        for (TableStat.Name name : tables.keySet()) {
            tableString.add(name.toString());
        }
        return tableString;
    }

    public Object getObject(Object key) {

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // 缓存值
            return jedis.get(serializer.serialize(key)) == null ? null : serializer.deserialize(jedis.get(serializer
                    .serialize(key)));

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
            jedis.del(serializer.serialize(key));
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
            Set<byte[]> keys = jedis.smembers(serializer.serialize(idKey(id))); // 获取
            for (byte[] k : keys) {
                jedis.del(k); // 根据key删除value
            }

            if (config.useGrainedCahe()) {
                Set<String> tableNames=new HashSet<String>();
                for (byte[] k : keys) {
                    Object key = serializer.deserialize(k);
                    Set<String> tables = getTables(key);
                    tableNames.addAll(tables); //找到所有表
                }
                
                for (String name : tableNames) {
                    Set<byte[]> tableKeys = jedis.smembers(serializer.serialize(tableKey(name)));
                    for (byte[] k : tableKeys) {
                        jedis.del(k); // 删除 与这个相关的所有关联查询
                    }
                }
                
            }

            // 删除key
            jedis.del(serializer.serialize(idKey(id)));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
