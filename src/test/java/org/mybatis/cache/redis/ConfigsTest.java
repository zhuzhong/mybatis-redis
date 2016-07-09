/**
 * 
 */
package org.mybatis.cache.redis;

import org.junit.Test;

import com.alibaba.druid.sql.visitor.SchemaStatVisitor;

/**
 * @author sunff
 *
 */
public class ConfigsTest {
    @Test
    public void getSerialize() {
        Configs c = new Configs();
        ObjectSerializer os = c.getSerialize();
        String result = (String) os.deserialize(os.serialize("string"));
        
        System.out.println(result);
        SchemaStatVisitor v=c.getSchemaStatVisitor();
    }
}
