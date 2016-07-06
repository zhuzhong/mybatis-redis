/**
 * 
 */
package org.mybatis.cache.redis;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
 * 2.mapper id与相关的statement id 生成的key集合 3.table 表名 与statement id生成的key集合
 * 
 * @autthor Administrator
 *
 */
public class RedisCacheFactory {

    private JedisPool jedisPool;

    private Configs config;
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
        InputStream input = getClass().getClassLoader().getResourceAsStream(Configs.default_config);
        if (System.getProperty(Configs.system_path) != null) {
            input = getClass().getResourceAsStream(System.getProperty(Configs.system_path));
        }
        if (getClass().getClassLoader().getResourceAsStream(Configs.user_config) != null) {
            input = getClass().getClassLoader().getResourceAsStream(Configs.user_config);
        }

        if (input == null) {
            throw new RuntimeException("redis配置文件不存在");
        }
        config = new Configs(input);
        jedisPool = new JedisPool(config.host(), config.port());
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
            return jedis.scard(SerializeUtil.serialize(idKey(id))).intValue();

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
            jedis.sadd(SerializeUtil.serialize(idKey(id)), SerializeUtil.serialize(key));
            if (config.fineGrainedCahe()) {
                // 缓存table与key 列表
                List<String> tables = getTables(key);
                for (String name : tables) {
                    jedis.sadd(SerializeUtil.serialize(tableKey(name)), SerializeUtil.serialize(key));
                }
            }

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    // 根据cacheKey获取 table表名
    private List<String> getTables(Object key) {
        CacheKey cacheKey = (CacheKey) key;
        String[] strs = cacheKey.toString().split(":");
        // String statementId=strs[2];
        String sql = strs[5];
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, config.dbtype());
        SQLStatement statement = parser.parseStatement();
        SchemaStatVisitor statVisitor = config.dbtype().equalsIgnoreCase(JdbcUtils.MYSQL) ? new MySqlSchemaStatVisitor()
                : new OracleSchemaStatVisitor();
        statement.accept(statVisitor);

        Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
        List<String> tableString = new ArrayList<String>();
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
            Set<byte[]> keys = jedis.smembers(SerializeUtil.serialize(idKey(id))); // 获取
            for (byte[] k : keys) {
                jedis.del(k); // 根据key删除value
            }

            if (config.fineGrainedCahe()) {
                for (byte[] k : keys) {
                    Object key = SerializeUtil.deserialize(k);
                    List<String> tables = getTables(key);
                    for (String name : tables) {
                        Set<byte[]> tableKeys = jedis.smembers(SerializeUtil.serialize(tableKey(name)));
                        for (byte[] j : tableKeys) {
                            jedis.del(j); // 删除 与这个相关的所有关联查询
                        }
                    }
                }
            }

            // 删除key
            jedis.del(SerializeUtil.serialize(idKey(id)));
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
