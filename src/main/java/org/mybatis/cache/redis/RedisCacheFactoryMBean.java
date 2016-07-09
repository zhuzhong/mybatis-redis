/**
 * 
 */
package org.mybatis.cache.redis;

/**增加jmx管理
 * @author Administrator
 *
 */
public interface RedisCacheFactoryMBean {

    public void clear(String id); //根据mapperid清理缓存
    public void clearByTableName(String tablename);//根据表名清量缓存
}
