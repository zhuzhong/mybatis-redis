/**
 * 
 */
package org.mybatis.cache.redis;

/**
 * @author Administrator
 *
 */
public interface ObjectSerializer {

    public byte[] serialize(Object data); //序列化

    public Object deserialize(byte[] bytes);//反序列化
}
