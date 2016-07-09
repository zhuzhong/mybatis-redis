/**
 * 
 */
package org.mybatis.cache.redis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.mybatis.cache.redis.support.JavaObjectSerializer;

import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;

/**
 * @author Administrator
 *
 */
public class Configs {

    public static final String default_config = "default-rediscache.properties";
    public static final String system_path = "redis.config.path";
    public static final String user_config = "rediscache.properties";

    public static final String serialze_clazz = "serializer.clazz";

    public static final String host = "redis.host";
    public static final String port = "redis.port";

    public static final String dbtype = "db.type"; // 数据库类型

    public static final String use_grained_cache = "grained.cache"; // 细粒度缓存

    private final Properties p;
    public Configs(// Properties p
    ) {
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
        this.p = p;
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

    public String host() {
        return p.getProperty(host);
    }

    public int port() {
        return Integer.valueOf(p.getProperty(port));
    }

    public String dbtype() {
        return p.getProperty(dbtype);
    }

    public boolean useGrainedCahe() {
        return p.getProperty(use_grained_cache) == null ? false : true;
    }

    public String value(String key) {
        return p.getProperty(key) == null ? null : p.getProperty(key);
    }

  

    public ObjectSerializer getSerialize() {
        ObjectSerializer serialize;
        /*
         * if (serialize != null) { return serialize; }
         */
        if (p.getProperty(serialze_clazz) == null) {
            serialize = new JavaObjectSerializer();
            return serialize;
        }

        try {
            Class clazz;
            clazz = Thread.currentThread().getContextClassLoader().loadClass(p.getProperty(serialze_clazz));
            serialize = (ObjectSerializer) clazz.newInstance();
            return serialize;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("serialize实例化失败", e);
        }
    }



    public SchemaStatVisitor getSchemaStatVisitor() {
        SchemaStatVisitor schemaStatVisitor;
        /*
         * if (schemaStatVisitor != null) { return schemaStatVisitor; }
         */

        if (p.getProperty(dbtype) == null || p.getProperty(p.getProperty(dbtype)) == null) {
            schemaStatVisitor = new MySqlSchemaStatVisitor();
            return schemaStatVisitor;
        }

        Class clazz;
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(p.getProperty(p.getProperty(dbtype)));
            schemaStatVisitor = (SchemaStatVisitor) clazz.newInstance();
            return schemaStatVisitor;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("SchemaStatVisitor实例化失败", e);
        }

    }
}
