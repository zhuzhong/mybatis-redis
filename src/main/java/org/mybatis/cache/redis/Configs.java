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

	public static final String host = "redis.host";
	public static final String port = "redis.port";

	public static final String system_path="redis.cache.path";
	
	public static final String default_config="rediscache.properties";
	private Properties p = new Properties();

	public Configs(InputStream input) {
		try {
			p.load(input);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public String host(){
		return p.getProperty(host);
	}
	
	public int port(){
		return Integer.valueOf(p.getProperty(port));
	}
}
