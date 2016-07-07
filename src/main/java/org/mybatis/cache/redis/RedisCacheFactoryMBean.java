/**
 * 
 */
package org.mybatis.cache.redis;

/**增加jmx管理
 * @author Administrator
 *
 */
public interface RedisCacheFactoryMBean {

    public void clear(String id);
}
