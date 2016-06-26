package com.saasxx.framework;

import java.beans.PropertyDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.saasxx.framework.convert.Converters;
import com.saasxx.framework.io.UnsafeStringWriter;
import com.saasxx.framework.lang.Mirrors;
import com.saasxx.framework.lang.OID;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

/**
 * Java通用工具类
 * 
 * @author lujijiang
 * 
 */
public class Lang {

	private static final String DATE_FORMAT_YYYYMMDDHHMMSS_SSS = "yyyyMMddHHmmssSSS";

	/**
	 * 固定日志器，类似System.out
	 */
	public static final Log log = Logs.getLog();

	private Lang() {
	}

	/**
	 * 空对象
	 */
	public final static Object EMPTY = new Object();
	/**
	 * 空数组
	 */
	public final static Object[] EMPTY_ARRAY = new Object[] {};

	/**
	 * 获取对象系统引用哈希值（不为负数）
	 * 
	 * @param x
	 * @return
	 */
	public static long identityHashCode(Object x) {
		return (long) System.identityHashCode(x) + (long) Integer.MAX_VALUE;
	}

	/**
	 * 获取一个对象的HashCode
	 * 
	 * @param x
	 * @return
	 */
	public static long hashCode(Object x) {
		return x == null ? 0 : x.hashCode() + 10000000000L;
	}

	/**
	 * 将UUID转换为22位字符串，依据Base64编码（URL安全）
	 * 
	 * @return
	 */
	public static String id() {
		OID oid = new OID();
		return oid.toString();
	}

	/**
	 * 将CheckedException转换为RuntimeException.
	 */
	public static RuntimeException unchecked(Exception e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}

	/**
	 * 将CheckedException转换为RuntimeException.
	 */
	public static RuntimeException unchecked(Exception e, String message, Object... args) {
		return new RuntimeException(String.format(message, args), e);
	}

	/**
	 * 判断一个对象是否是空对象
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object obj) {
		if (obj == null) {
			return true;
		}
		if (obj instanceof Map) {
			return ((Map) obj).isEmpty();
		}
		if (obj instanceof Collection) {
			return ((Collection) obj).isEmpty();
		}
		if (obj.getClass().isArray()) {
			return Array.getLength(obj) == 0;
		}
		if (obj instanceof CharSequence) {
			return obj.toString().trim().length() == 0;
		}
		return Object.class.equals(obj.getClass());
	}

	/**
	 * 判断一个类型是否是基本类型
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isBaseType(Class<?> type) {
		if (type.isPrimitive()) {
			return true;
		}
		if (type.isEnum()) {
			return true;
		}
		if (type.isArray()) {
			return true;
		}
		if (Modifier.isFinal(type.getModifiers())) {
			return true;
		}
		if (CharSequence.class.isAssignableFrom(type)) {
			return true;
		}
		if (Number.class.isAssignableFrom(type)) {
			return true;
		}
		if (Date.class.isAssignableFrom(type)) {
			return true;
		}
		if (Boolean.class.equals(type)) {
			return true;
		}
		if (Character.class.equals(type)) {
			return true;
		}
		if (Class.class.equals(type)) {
			return true;
		}
		if (StringBuilder.class.equals(type)) {
			return true;
		}
		if (StringBuffer.class.equals(type)) {
			return true;
		}
		if (Object.class.equals(type)) {
			return true;
		}
		if (Void.class.equals(type)) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否是数字类型
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isNumber(Class<?> type) {
		if (Number.class.isAssignableFrom(type)) {
			return true;
		}
		if (type.equals(int.class)) {
			return true;
		}
		if (type.equals(short.class)) {
			return true;
		}
		if (type.equals(long.class)) {
			return true;
		}
		if (type.equals(float.class)) {
			return true;
		}
		if (type.equals(double.class)) {
			return true;
		}
		if (type.equals(byte.class)) {
			return true;
		}
		return false;
	}

	/**
	 * 获得本源异常信息
	 * 
	 * @param e
	 * @return
	 */
	public static Throwable getCause(Throwable e) {
		return e.getCause() == null ? e : getCause(e.getCause());
	}

	/**
	 * 输出对象字符串格式
	 * 
	 * @param obj
	 * @return
	 */
	public static String toString(Object obj) {
		return toString(obj, null);
	}

	/**
	 * 输出对象字符串格式
	 * 
	 * @param obj
	 * @return
	 */
	public static String toString(Object obj, String format) {
		if (obj == null) {
			return "null";
		}
		if (obj instanceof Exception) {
			Exception throwable = (Exception) obj;
			UnsafeStringWriter sw = new UnsafeStringWriter();
			PrintWriter pw = new PrintWriter(sw);
			throwable.printStackTrace(pw);
			pw.flush();
			pw.close();
			sw.flush();
			return sw.toString();
		}
		if (obj instanceof Date) {
			return new SimpleDateFormat(
					format == null || format.trim().length() == 0 ? DATE_FORMAT_YYYYMMDDHHMMSS_SSS : format)
							.format((Date) obj);
		}
		if (isNumber(obj.getClass())) {
			if (format != null && format.trim().length() != 0) {
				return new DecimalFormat(format).format(obj);
			}
		}
		return String.valueOf(obj);
	}

	/**
	 * 将一个对象转换为指定类型
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	public static <T> T convert(Object value, Class<T> type) {
		return convert(value, type, null);
	}

	/**
	 * 将一个对象转换为指定类型
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	public static <T> T convert(Object value, Class<T> type, String format) {
		return Converters.BASE.convert(value, type, format);
	}

	/**
	 * 抛出一个带消息的异常
	 * 
	 * @param type
	 * @param message
	 * @param args
	 * @return
	 */
	public static <T extends Exception> T newException(Class<T> type, String message, Object... args) {
		try {
			return type.getConstructor(String.class).newInstance(String.format(message, args));
		} catch (InstantiationException e) {
			throw Lang.unchecked(e, message, args);
		} catch (IllegalAccessException e) {
			throw Lang.unchecked(e, message, args);
		} catch (IllegalArgumentException e) {
			throw Lang.unchecked(e, message, args);
		} catch (InvocationTargetException e) {
			throw Lang.unchecked(e, message, args);
		} catch (NoSuchMethodException e) {
			throw Lang.unchecked(e, message, args);
		} catch (SecurityException e) {
			throw Lang.unchecked(e, message, args);
		}
	}

	/**
	 * 抛出一个带消息的运行时异常
	 * 
	 * @param message
	 * @param args
	 * @return
	 */
	public static IllegalStateException newException(String message, Object... args) {
		return newException(IllegalStateException.class, message, args);
	}

	/**
	 * 新建一个Set
	 * 
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Set<T> newSet(T... args) {
		int length = args == null ? 1 : args.length;
		Set<T> set = new HashSet<T>(length);
		if (args == null) {
			set.add(null);
		} else {
			for (int i = 0; i < args.length; i++) {
				set.add(args[i]);
			}
		}
		return set;
	}

	/**
	 * 新建一个List
	 * 
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> newList(T... args) {
		int length = args == null ? 1 : args.length;
		List<T> list = new ArrayList<T>(length);
		if (args == null) {
			list.add(null);
		} else {
			for (int i = 0; i < args.length; i++) {
				list.add(args[i]);
			}
		}
		return list;
	}

	/**
	 * 新建一个Map，必须是偶数个参数 注意，这是一个同步的Map，且key和value不能为null
	 * 
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> newMap(Object... args) {
		Map<K, V> map = new HashMap<K, V>();
		if (args != null) {
			if (args.length % 2 != 0) {
				throw new IllegalArgumentException("The number of arguments must be an even number");
			}
			for (int i = 0; i < args.length; i += 2) {
				map.put((K) args[i], (V) args[i + 1]);
			}
		}
		return map;
	}

	/**
	 * 生成一个固定容量的LRU策略的Map
	 * 
	 * @param capacity
	 *            容量
	 * @param args
	 *            参数列表，通newMap
	 * @return
	 */
	public static <K, V> Map<K, V> newLRUMap(final int capacity, Object... args) {
		return new LinkedHashMap<K, V>(newMap(args)) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5820354698308020916L;

			protected boolean removeEldestEntry(Entry<K, V> eldest) {
				return size() > capacity;
			}
		};
	}

	/**
	 * 比较两个对象是否相同，对于数字、日期等按照大小进行比较，自动兼容包装器实例
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equals(Object a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		if (a.equals(b)) {
			return true;
		}
		if (isNumber(a.getClass()) && isNumber(b.getClass())) {
			return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString())) == 0;
		}
		return false;
	}

	/**
	 * 迭代验证对象，符合JSR 303 - Bean Validation框架
	 */
	public static <T> void validate(T domain) {
		Set<String> messageSet = newSet();
		validate(null, domain, messageSet, new HashSet<Integer>());
		if (!messageSet.isEmpty()) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("domain object validate failure:");
			for (String message : messageSet) {
				messageBuilder.append("\r\n");
				messageBuilder.append(message);
			}
			throw new IllegalStateException(messageBuilder.toString());
		}
	}

	/**
	 * 迭代验证对象，符合JSR 303 - Bean Validation框架
	 */
	@SuppressWarnings("rawtypes")
	private static <T> void validate(String prefixPath, T domain, Set<String> messageSet, Set<Integer> idHashs) {
		if (domain == null) {
			return;
		}
		if (isBaseType(domain.getClass())) {
			return;
		}

		// 判断是否循环校验了
		Integer id = System.identityHashCode(domain);
		if (idHashs.contains(id)) {
			return;
		}
		idHashs.add(id);

		prefixPath = prefixPath == null ? "$" : prefixPath;

		if (domain.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(domain); i++) {
				validate(prefixPath.concat("[").concat(String.valueOf(i)).concat("]"), Array.get(domain, i), messageSet,
						idHashs);
			}
			return;
		}

		if (domain instanceof Collection) {
			int i = 0;
			for (Object subDomain : (Collection) domain) {
				validate(prefixPath.concat("[").concat(String.valueOf(i)).concat("]"), subDomain, messageSet, idHashs);
				i++;
			}
			return;
		}

		if (domain instanceof Map) {
			for (Object key : ((Map) domain).keySet()) {
				validate(prefixPath.concat("<").concat(String.valueOf(key)).concat(">"), key, messageSet, idHashs);
				Object value = ((Map) domain).get(key);
				validate(prefixPath.concat("[").concat(String.valueOf(key)).concat("]"), value, messageSet, idHashs);
			}
			return;
		}

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		Set<ConstraintViolation<T>> constraintViolations = validator.validate(domain);

		for (ConstraintViolation<T> constraintViolation : constraintViolations) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append(prefixPath);
			messageBuilder.append(".");
			messageBuilder.append(constraintViolation.getPropertyPath());
			messageBuilder.append(":");
			messageBuilder.append(constraintViolation.getMessage());
			messageSet.add(messageBuilder.toString());
		}

		try {
			PropertyDescriptor[] propertyDescriptors = Mirrors.getPropertys(domain.getClass());
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				if (!isBaseType(propertyDescriptor.getPropertyType())) {
					Method readMethod = propertyDescriptor.getReadMethod();
					if (readMethod != null) {
						if (!readMethod.isAccessible()) {
							readMethod.setAccessible(true);
						}
						Object value = readMethod.invoke(domain);
						validate(prefixPath.concat(".").concat(propertyDescriptor.getName()), value, messageSet,
								idHashs);
					}
				}
			}
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 计时执行
	 * 
	 * @return 返回runnable的执行时间
	 */
	public static long timing(Runnable runnable) {
		long begin = System.currentTimeMillis();
		try {
			runnable.run();
			return System.currentTimeMillis() - begin;
		} catch (Exception e) {
			throw unchecked(e);
		}
	}

	/**
	 * 浅层克隆，如果传入对象的类型实现了Cloneable接口，则优先调用对象的clone方法
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T clone(T obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Cloneable) {
			try {
				return (T) obj.getClass().getMethod("clone").invoke(obj);
			} catch (NoSuchMethodException e) {

			} catch (SecurityException e) {
				throw Lang.unchecked(e);
			} catch (IllegalAccessException e) {
				throw Lang.unchecked(e);
			} catch (IllegalArgumentException e) {
				throw Lang.unchecked(e);
			} catch (InvocationTargetException e) {
				throw Lang.unchecked(e);
			}
		}
		try {
			T newObject;
			if (obj.getClass().isArray()) {
				int length = Array.getLength(obj);
				newObject = (T) Array.newInstance(obj.getClass().getComponentType(), length);
				for (int i = 0; i < length; i++) {
					Array.set(newObject, i, Array.get(obj, i));
				}
			} else {
				newObject = (T) obj.getClass().newInstance();
				if (obj instanceof Collection) {
					((Collection<Object>) newObject).addAll((Collection<Object>) obj);
				} else {
					PropertyDescriptor[] propertyDescriptors = Mirrors.getPropertys(obj.getClass());
					for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
						Method readMethod = propertyDescriptor.getReadMethod();
						if (readMethod == null) {
							continue;
						}
						Method writeMethod = propertyDescriptor.getWriteMethod();
						if (writeMethod == null) {
							continue;
						}
						writeMethod.invoke(newObject, readMethod.invoke(obj));
					}
				}
			}
			return newObject;
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 获取最初的消息异常
	 * 
	 * @param e
	 * @return
	 */
	public static Throwable getMessageCause(Throwable e) {
		while (e != null && e.getMessage() == null && e.getCause() != null) {
			e = e.getCause();
		}
		return e;
	}

}
