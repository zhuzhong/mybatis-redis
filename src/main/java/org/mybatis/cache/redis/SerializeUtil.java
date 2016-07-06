package org.mybatis.cache.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

public class SerializeUtil {
	/*
	 * public static byte[] serialize(Object object) { ObjectOutputStream oos =
	 * null; ByteArrayOutputStream baos = null; try { // 序列化 baos = new
	 * ByteArrayOutputStream(); oos = new ObjectOutputStream(baos);
	 * oos.writeObject(object); byte[] bytes = baos.toByteArray(); return bytes;
	 * } catch (Exception e) { e.printStackTrace(); } return null; }
	 */

	public static byte[] serialize(Object data) {
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

	/*
	 * private void test(Object obj) { Kryo k = new Kryo(); Output output = new
	 * Output();
	 * 
	 * }
	 */

	/*
	 * public static Object unserialize(byte[] bytes) { if (bytes == null)
	 * return null; ByteArrayInputStream bais = null; try { // 反序列化 bais = new
	 * ByteArrayInputStream(bytes); ObjectInputStream ois = new
	 * ObjectInputStream(bais); return ois.readObject(); } catch (Exception e) {
	 * e.printStackTrace(); } return null; }
	 */

	public static Object deserialize(byte[] bytes) {
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