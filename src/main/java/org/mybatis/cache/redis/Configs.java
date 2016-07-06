/**
 * 
 */
package org.mybatis.cache.redis;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author Administrator
 *
 */
public class Configs {

    public static final String default_config = "default-rediscache.properties";
    public static final String system_path = "redis.config.path";
    public static final String user_config = "rediscache.properties";

    public static final String host = "redis.host";
    public static final String port = "redis.port";

    public static final String dbtype = "db.type"; // 数据库类型

    public static final String fine_grained_cache="fine.grained.cache"; //细粒度缓存
    
    private Properties p = new Properties();

    public Configs(InputStream input) {
        try {
            p.load(input);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    
    public boolean fineGrainedCahe(){
        return Boolean.valueOf(p.getProperty(fine_grained_cache));
    }
}
