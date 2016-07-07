/**
 * 
 */
package org.mybatis.cache.redis;

import java.util.Properties;

import org.mybatis.cache.redis.support.JavaObjectSerializer;

/**
 * @author Administrator
 *
 */
public class Configs {

    public static final String default_config = "default-rediscache.properties";
    public static final String system_path = "redis.config.path";
    public static final String user_config = "rediscache.properties";

    public static final String serialze_clazz = "serialze.class";

    public static final String host = "redis.host";
    public static final String port = "redis.port";

    public static final String dbtype = "db.type"; // 数据库类型

    public static final String use_grained_cache = "use.grained.cache"; // 细粒度缓存

    private final Properties p;
    public Configs(Properties p) {
        this.p = p;
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

    private ObjectSerializer serialize;

    public ObjectSerializer getSerialize() {
        if (serialize != null) {
            return serialize;
        } else {
            if (p.get(serialze_clazz) == null) {
                serialize = new JavaObjectSerializer();
                return serialize;
            } else {
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
        }
    }
}
