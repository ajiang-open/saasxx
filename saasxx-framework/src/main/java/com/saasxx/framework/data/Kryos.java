package com.saasxx.framework.data;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.saasxx.framework.io.UnsafeByteArrayInputStream;
import com.saasxx.framework.io.UnsafeByteArrayOutputStream;

/**
 * Kryo序列化工具类
 * 
 * @author lujijiang
 *
 */
public class Kryos {

	private static final ThreadLocal<Kryo> THREAD_LOCAL_KRYO = new ThreadLocal<Kryo>();

	private static Kryo getKryo() {
		Kryo kryo = THREAD_LOCAL_KRYO.get();
		if (kryo == null) {
			kryo = new Kryo();
			kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(
					new StdInstantiatorStrategy()));
			THREAD_LOCAL_KRYO.set(kryo);
		}
		return kryo;
	}

	/**
	 * 将对象序列化为字节数组
	 * 
	 * @param object
	 * @return
	 */
	public static byte[] toBytes(Object object) {
		Kryo kryo = getKryo();
		UnsafeByteArrayOutputStream outputStream = new UnsafeByteArrayOutputStream();
		Output output = new Output(outputStream);
		try {
			kryo.writeObject(output, object);
		} finally {
			output.close();
		}
		return outputStream.toByteArray();
	}

	/**
	 * 将字节数组反序列化为制定类型的对象
	 * 
	 * @param bytes
	 * @param type
	 * @return
	 */
	public static <T> T fromBytes(byte[] bytes, Class<T> type) {
		Kryo kryo = getKryo();
		UnsafeByteArrayInputStream inputStream = new UnsafeByteArrayInputStream(
				bytes);
		Input input = new Input(inputStream);
		try {
			return kryo.readObject(input, type);
		} finally {
			input.close();
		}
	}

}
