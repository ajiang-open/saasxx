package com.saasxx.framework.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.saasxx.framework.Lang;
import com.saasxx.framework.io.UnsafeByteArrayInputStream;
import com.saasxx.framework.io.UnsafeByteArrayOutputStream;

/**
 * Java序列化器
 * 
 * @author lujijiang
 *
 */
public class JavaSerializer {

	/**
	 * 将对象序列化为字节数组
	 * 
	 * @param object
	 * @return
	 */
	public static byte[] toBytes(Serializable object) {
		try {
			UnsafeByteArrayOutputStream byteArrayOutputStream = new UnsafeByteArrayOutputStream();
			try {
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(
						byteArrayOutputStream);
				try {
					objectOutputStream.writeObject(object);
				} finally {
					objectOutputStream.close();
				}
			} finally {
				byteArrayOutputStream.close();
			}
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 将字节数组反序列化为制定类型的对象
	 * 
	 * @param bytes
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T fromBytes(byte[] bytes) {
		try {
			UnsafeByteArrayInputStream is = new UnsafeByteArrayInputStream(
					bytes);
			try {
				ObjectInputStream ois = new ObjectInputStream(is);
				try {
					return (T) ois.readObject();
				} finally {
					ois.close();
				}
			} finally {
				is.close();
			}
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

}
