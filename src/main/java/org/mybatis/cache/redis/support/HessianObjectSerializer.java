/**
 * 
 */
package org.mybatis.cache.redis.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.mybatis.cache.redis.ObjectSerializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

/**
 * @author Administrator
 *
 */
public class HessianObjectSerializer implements ObjectSerializer {

    @Override
    public byte[] serialize(Object data) {
        HessianOutput oos = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            oos = new HessianOutput(out);
            oos.writeObject(data);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                oos.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Object deserialize(byte[] bytes) {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);

        HessianInput ois = null;
        try {
            ois = new HessianInput(in);
            return ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                ois.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
