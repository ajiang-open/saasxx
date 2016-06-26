package com.saasxx.framework.convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;

import com.saasxx.framework.Lang;
import com.saasxx.framework.convert.converters.array.BlobToByteArrayConverter;
import com.saasxx.framework.convert.converters.array.EnumerationToArrayConverter;
import com.saasxx.framework.convert.converters.array.IterableToArrayConverter;
import com.saasxx.framework.convert.converters.array.IteratorToArrayConverter;
import com.saasxx.framework.convert.converters.date.ObjectToDateConverter;
import com.saasxx.framework.convert.converters.date.ObjectToSqlDateConverter;
import com.saasxx.framework.convert.converters.date.ObjectToTimestampConverter;
import com.saasxx.framework.convert.converters.map.ObjectToMapConverter;
import com.saasxx.framework.convert.converters.primitive.ObjectToBigDecimalConverter;
import com.saasxx.framework.convert.converters.primitive.ObjectToBigIntegerConverter;
import com.saasxx.framework.convert.converters.primitive.ObjectToBooleanConverter;
import com.saasxx.framework.convert.converters.primitive.ObjectToByteConverter;
import com.saasxx.framework.convert.converters.primitive.ObjectToCharacterConverter;
import com.saasxx.framework.convert.converters.primitive.ObjectToClassConverter;
import com.saasxx.framework.convert.converters.primitive.ObjectToDoubleConverter;
import com.saasxx.framework.convert.converters.primitive.ObjectToFloatConverter;
import com.saasxx.framework.convert.converters.primitive.ObjectToIntegerConverter;
import com.saasxx.framework.convert.converters.primitive.ObjectToLongConverter;
import com.saasxx.framework.convert.converters.primitive.ObjectToShortConverter;
import com.saasxx.framework.convert.converters.string.ClobToStringConverter;
import com.saasxx.framework.convert.converters.string.DateToStringConverter;
import com.saasxx.framework.convert.converters.string.NumberToStringConverter;
import com.saasxx.framework.convert.converters.string.ObjectToStringConverter;
import com.saasxx.framework.lang.Mirrors;

/**
 * 转换工具类
 * 
 * @author lujijiang
 *
 */
public class Converters {

	/**
	 * 创建带有匹配缓存的转换器
	 * 
	 * @param cache
	 * @return
	 */
	public static Converters create(int cache) {
		return new Converters(cache);
	}

	/**
	 * 创建带有默认匹配缓存的转换器（默认缓存为1024）
	 * 
	 * @return
	 */
	public static Converters create() {
		return create(1024);
	}

	/**
	 * 转换元数据
	 * 
	 * @author lujijiang
	 *
	 */
	class ConverterMeta {
		Class<?> fromType;
		Class<?> toType;
		Converter converter;
	}

	/**
	 * 匹配到的转换器
	 */
	Map<Integer, ConverterMeta> matchConverterMap;

	/**
	 * 转换元列表
	 */
	List<ConverterMeta> converterMetas = new ArrayList<Converters.ConverterMeta>();

	private Converters(int cache) {
		matchConverterMap = Lang.newLRUMap(cache);
	}

	/**
	 * 添加一个转换器
	 * 
	 * @param fromType
	 * @param toType
	 * @param converter
	 * @return
	 */
	public <S, T> Converters add(Class<S> fromType, Class<T> toType,
			Converter converter) {
		ConverterMeta converterMeta = new ConverterMeta();
		converterMeta.fromType = fromType;
		converterMeta.toType = toType;
		converterMeta.converter = converter;
		converterMetas.add(converterMeta);
		return this;
	}

	/**
	 * 转换一个对象到另一类型
	 * 
	 * @param from
	 *            源对象
	 * @param toType
	 *            目标类型
	 * @param args
	 *            转换器需要的额外参数
	 * @return 目标类型对象
	 */
	@SuppressWarnings("unchecked")
	public <S, T> T convert(S from, Class<T> toType, Object... args) {
		if (from == null) {
			return null;
		}
		if (toType.isPrimitive()) {
			toType = (Class<T>) MethodUtils.getPrimitiveWrapper(toType);
		}
		if (toType.isAssignableFrom(from.getClass())) {
			return (T) from;
		}
		Class<S> fromType = (Class<S>) from.getClass();
		int key = Arrays.hashCode(new Object[] { fromType, toType });
		ConverterMeta matchConverterMeta = matchConverterMap.get(key);
		if (matchConverterMeta == null) {
			float cost = Float.MAX_VALUE;
			for (ConverterMeta converterMeta : converterMetas) {
				float converterCost = Mirrors.getTotalTransformationCost(
						new Class<?>[] { fromType, converterMeta.toType },
						new Class<?>[] { converterMeta.fromType, toType });
				if (converterCost == -1) {
					continue;
				}
				if (converterCost < cost) {
					cost = converterCost;
					matchConverterMeta = converterMeta;
				}
			}
			if (matchConverterMeta == null) {
				throw new IllegalArgumentException(String.format(
						"Coudn't convert object %s to type %s", from, toType));
			}
			matchConverterMap.put(key, matchConverterMeta);
		}

		Converter converter = (Converter) matchConverterMeta.converter;
		return (T) converter.convert(from, matchConverterMeta.toType, args);
	}

	/**
	 * 转换一个对象到另一类型
	 * 
	 * @param from
	 *            源对象
	 * @param toType
	 *            目标类型
	 * @return 目标类型对象
	 */
	public <S, T> T convert(S from, Class<T> toType) {
		return convert(from, toType, Lang.EMPTY_ARRAY);
	}

	/**
	 * 锁定转换规则
	 */
	public void lock() {
		this.converterMetas = Collections.unmodifiableList(this.converterMetas);
	}

	/**
	 * 从别的转换器工具继承转换规则
	 * 
	 * @param other
	 * @return
	 */
	public Converters extend(final Converters extendConverters) {
		final List<ConverterMeta> converterMetas = this.converterMetas;
		this.converterMetas = new AbstractList<Converters.ConverterMeta>() {

			public ConverterMeta get(int index) {
				if (index < extendConverters.converterMetas.size()) {
					return extendConverters.converterMetas.get(index);
				}
				return converterMetas.get(index);
			}

			@Override
			public int size() {
				return extendConverters.converterMetas.size()
						+ converterMetas.size();
			}

			@Override
			public boolean add(ConverterMeta e) {
				return converterMetas.add(e);
			}

		};
		return this;
	}

	/**
	 * 默认转换器，自带有大量的默认转换功能
	 */
	public static final Converters BASE = create();
	static {
		// 输出字符串
		BASE.add(Object.class, String.class, new ObjectToStringConverter());
		BASE.add(Date.class, String.class, new DateToStringConverter());
		BASE.add(Number.class, String.class, new NumberToStringConverter());
		BASE.add(Clob.class, String.class, new ClobToStringConverter());
		// 输出基础类型
		BASE.add(Object.class, Byte.class, new ObjectToByteConverter());
		BASE.add(Object.class, Short.class, new ObjectToShortConverter());
		BASE.add(Object.class, Integer.class, new ObjectToIntegerConverter());
		BASE.add(Object.class, Long.class, new ObjectToLongConverter());
		BASE.add(Object.class, Float.class, new ObjectToFloatConverter());
		BASE.add(Object.class, Double.class, new ObjectToDoubleConverter());
		BASE.add(Object.class, Boolean.class, new ObjectToBooleanConverter());
		BASE.add(Object.class, Character.class,
				new ObjectToCharacterConverter());
		BASE.add(Object.class, BigInteger.class,
				new ObjectToBigIntegerConverter());
		BASE.add(Object.class, BigDecimal.class,
				new ObjectToBigDecimalConverter());
		BASE.add(Object.class, Class.class, new ObjectToClassConverter());
		// 输出时间
		BASE.add(Object.class, Date.class, new ObjectToDateConverter());
		BASE.add(Object.class, java.sql.Date.class,
				new ObjectToSqlDateConverter());
		BASE.add(Object.class, Timestamp.class,
				new ObjectToTimestampConverter());
		// 输出数组
		BASE.add(Blob.class, byte[].class, new BlobToByteArrayConverter());
		// 集合转换为常规类型数组
		BASE.add(Iterable.class, byte[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, short[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, int[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, long[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, float[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, double[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, boolean[].class,
				new IterableToArrayConverter());
		BASE.add(Iterable.class, char[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, Byte[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, Short[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, Integer[].class,
				new IterableToArrayConverter());
		BASE.add(Iterable.class, Long[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, Float[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, Double[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, Boolean[].class,
				new IterableToArrayConverter());
		BASE.add(Iterable.class, Character[].class,
				new IterableToArrayConverter());
		BASE.add(Iterable.class, BigDecimal[].class,
				new IterableToArrayConverter());
		BASE.add(Iterable.class, BigInteger[].class,
				new IterableToArrayConverter());
		BASE.add(Iterable.class, String[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, Date[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, Class[].class, new IterableToArrayConverter());
		BASE.add(Iterable.class, Object[].class, new IterableToArrayConverter());

		BASE.add(Iterator.class, byte[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, short[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, int[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, long[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, float[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, double[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, boolean[].class,
				new IteratorToArrayConverter());
		BASE.add(Iterator.class, char[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, Byte[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, Short[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, Integer[].class,
				new IteratorToArrayConverter());
		BASE.add(Iterator.class, Long[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, Float[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, Double[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, Boolean[].class,
				new IteratorToArrayConverter());
		BASE.add(Iterator.class, Character[].class,
				new IteratorToArrayConverter());
		BASE.add(Iterator.class, BigDecimal[].class,
				new IteratorToArrayConverter());
		BASE.add(Iterator.class, BigInteger[].class,
				new IteratorToArrayConverter());
		BASE.add(Iterator.class, String[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, Date[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, Class[].class, new IteratorToArrayConverter());
		BASE.add(Iterator.class, Object[].class, new IteratorToArrayConverter());

		BASE.add(Enumeration.class, byte[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, short[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, int[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, long[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, float[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, double[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, boolean[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, char[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, Byte[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, Short[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, Integer[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, Long[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, Float[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, Double[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, Boolean[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, Character[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, BigDecimal[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, BigInteger[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, String[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, Date[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, Class[].class,
				new EnumerationToArrayConverter());
		BASE.add(Enumeration.class, Object[].class,
				new EnumerationToArrayConverter());

		// 输出Map
		BASE.add(Object.class, Map.class, new ObjectToMapConverter());
		BASE.lock();
	}

}
