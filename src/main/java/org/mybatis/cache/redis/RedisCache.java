package org.mybatis.cache.redis;

import java.util.concurrent.locks.ReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.cache.Cache;

public class RedisCache implements Cache {
    private static Log log = LogFactory.getLog(RedisCache.class);
    /** The ReadWriteLock. */
    // private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final RedisCacheFactory factory = new RedisCacheFactory();

    private String id;

    public RedisCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("no ID");
        }
        log.debug("RedisCache:id=" + id);
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getSize() {
        return factory.getSizeById(this.id);

    }

    @Override
    public void putObject(Object key, Object value) {
        if (log.isDebugEnabled())
            log.debug("putObject:" + key.hashCode() + "=" + value);
        if (log.isInfoEnabled())
            log.info("put to redis sql :" + key.toString());
        factory.putObject(key, value, id);

    }

    @Override
    public Object getObject(Object key) {
        return factory.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        return factory.removeObject(key);
    }

    @Override
    public void clear() {
        factory.clear(id);
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

}