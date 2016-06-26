package com.saasxx.framework.data;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.saasxx.framework.Lang;
import com.saasxx.framework.convert.Converter;
import com.saasxx.framework.convert.Converters;
import com.saasxx.framework.data.copier.FastCopier;
import com.saasxx.framework.data.copier.FastCopierConverter;
import com.saasxx.framework.lang.Errors;

/**
 * 用于操作Bean的工具类
 * 
 * @author lujijiang
 *
 */
public class Beans {

	private static final Map<Class<?>, Map<String, PropertyDescriptor>> PROPERTY_DESCRIPTORS_CASEINSENSITIVE_MAP = new ConcurrentHashMap<Class<?>, Map<String, PropertyDescriptor>>();

	private static final Map<Class<?>, Map<String, PropertyDescriptor>> PROPERTY_DESCRIPTORS_CASESENSITIVE_MAP = new ConcurrentHashMap<Class<?>, Map<String, PropertyDescriptor>>();

	/**
	 * 拷贝器
	 * 
	 * @author lujijiang
	 *
	 */
	public static class Copier {

		@SuppressWarnings("rawtypes")
		private final static Map<Class, Map<Class, FastCopier>> COPIERS_CASESENSITIVE_MAP = new ConcurrentHashMap<Class, Map<Class, FastCopier>>();

		@SuppressWarnings("rawtypes")
		private final static Map<Class, Map<Class, FastCopier>> COPIERS_CASEINSENSITIVE_MAP = new ConcurrentHashMap<Class, Map<Class, FastCopier>>();

		/**
		 * 转换器
		 */
		Converters converters;
		/**
		 * 包含字段
		 */
		Set<String> includes;

		/**
		 * 排除字段
		 */
		Set<String> excludes;

		/**
		 * 是否大小写敏感，默认否
		 * 
		 * @return
		 */
		boolean caseSensitive = true;

		/**
		 * 是否包含null值属性
		 * 
		 * @return
		 */
		boolean includeNull = true;

		/**
		 * 是否包含null值属性
		 * 
		 * @return
		 */
		boolean includeEmpty = true;

		/**
		 * 是否包含基类型的0
		 * 
		 * @return
		 */
		boolean includePrimitiveZero = true;

		/**
		 * 是否包含基类型的false
		 * 
		 * @return
		 */
		boolean includePrimitiveFalse = true;

		/**
		 * 数据来源的bean
		 */
		Object from;

		/**
		 * 数据目标bean
		 */
		List<Object> toList;

		/**
		 * 新增转换器
		 * 
		 * @param fromType
		 *            源类型
		 * @param toType
		 *            目标类型
		 * @param converter
		 *            转换器
		 * @return
		 */
		public <S, T> Copier converter(Class<S> fromType, Class<T> toType,
				Converter converter) {
			if (converters == null) {
				converters = Converters.create();
			}
			converters.add(fromType, toType, converter);
			return this;
		}

		/**
		 * 新增转换器
		 * 
		 * @param fromType
		 *            源类型
		 * @param toType
		 *            目标类型
		 * @param converter
		 *            转换器
		 * @return
		 */
		public <S, T> Copier converters(Converters converters) {
			this.converters = converters;
			return this;
		}

		/**
		 * 包含null字段<br>
		 * copier.includeNull();
		 * 
		 * @return 复印机对象本身
		 */
		public Copier includeNull() {
			this.includeNull = true;
			return this;
		}

		/**
		 * 排除null字段<br>
		 * copier.excludeNull();
		 * 
		 * @return 复印机对象本身
		 */
		public Copier excludeNull() {
			this.includeNull = false;
			return this;
		}

		/**
		 * 排除空对象字段<br>
		 * copier.includeEmpty();
		 * 
		 * @return 复印机对象本身
		 */
		public Copier includeEmpty() {
			this.includeEmpty = true;
			return this;
		}

		/**
		 * 排除空对象字段<br>
		 * copier.excludeEmpty();
		 * 
		 * @return 复印机对象本身
		 */
		public Copier excludeEmpty() {
			this.includeEmpty = false;
			return this;
		}

		/**
		 * 包含基类型的false
		 * 
		 * @return
		 */
		public Copier includePrimitiveFalse() {
			this.includePrimitiveFalse = true;
			return this;
		}

		/**
		 * 排除基类型的false
		 * 
		 * @return
		 */
		public Copier excludePrimitiveFalse() {
			this.includePrimitiveFalse = false;
			return this;
		}

		/**
		 * 包含基类型的0
		 * 
		 * @return
		 */
		public Copier includePrimitiveZero() {
			this.includePrimitiveZero = true;
			return this;
		}

		/**
		 * 排除基类型的0
		 * 
		 * @return
		 */
		public Copier excludePrimitiveZero() {
			this.includePrimitiveZero = false;
			return this;
		}

		/**
		 * 字段名大小写敏感
		 * 
		 * @return
		 */
		public Copier caseSensitive() {
			this.caseSensitive = true;
			return this;
		}

		/**
		 * 字段名忽略大小写
		 * 
		 * @return
		 */
		public Copier caseInsensitive() {
			this.caseSensitive = false;
			return this;
		}

		/**
		 * 指定包含的字段名称<br>
		 * 
		 * <pre>
		 * Copier copier = Lang.newCopier();
		 * copier.includes(&quot;name&quot;);
		 * </pre>
		 * 
		 * @param names
		 *            字段名
		 * @return 复印机对象本身
		 */
		public Copier includes(String... names) {
			for (String name : names) {
				if (name == null) {
					continue;
				}
				name = name.trim();
				if (this.includes == null) {
					includes = new HashSet<String>();
				}
				this.includes.add(name);
			}
			return this;
		}

		/**
		 * 指定排除的字段名称<br>
		 * 
		 * <pre>
		 * Copier copier = Lang.newCopier();
		 * copier.excludes(&quot;name&quot;);
		 * </pre>
		 * 
		 * @param names
		 *            字段名
		 * @return 复印机对象本身
		 */
		public Copier excludes(String... names) {
			for (String name : names) {
				if (name == null) {
					continue;
				}
				name = name.trim();
				if (this.excludes == null) {
					excludes = new HashSet<String>();
				}
				this.excludes.add(name);
			}
			return this;
		}

		public Copier from(Object from) {
			this.from = from;
			done();
			return this;
		}

		public Copier to(Object... tos) {
			toList = new ArrayList<Object>(tos.length);
			for (Object to : tos) {
				if (to != null) {
					toList.add(to);
				}
			}
			done();
			return this;
		}

		/**
		 * 清除内含对象，必须重新调用from和to方法才能再次拷贝数据
		 * 
		 * @return
		 */
		public Copier clear() {
			this.from = null;
			this.toList = null;
			return this;
		}

		/**
		 * 完成拷贝
		 */
		private void done() {
			if (from == null || toList == null || toList.isEmpty()) {
				return;
			}
			fastCopy();
		}

		@SuppressWarnings("rawtypes")
		private boolean isEmpty(Object obj) {
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
			return false;
		}

		@SuppressWarnings("rawtypes")
		private void fastCopy() {
			for (Object to : toList) {
				if (to == null) {
					continue;
				}
				Class fromClass = from.getClass();
				Class toClass = to.getClass();

				Map<Class, FastCopier> copierMap = caseSensitive ? COPIERS_CASESENSITIVE_MAP
						.get(fromClass) : COPIERS_CASEINSENSITIVE_MAP
						.get(fromClass);
				if (copierMap == null) {
					copierMap = new ConcurrentHashMap<Class, FastCopier>();
					if (caseSensitive) {
						COPIERS_CASESENSITIVE_MAP.put(fromClass, copierMap);
					} else {
						COPIERS_CASEINSENSITIVE_MAP.put(fromClass, copierMap);
					}
				}

				FastCopier cglibCopier = copierMap.get(toClass);
				if (cglibCopier == null) {
					cglibCopier = FastCopier.create(fromClass, toClass, true,
							caseSensitive);
					copierMap.put(toClass, cglibCopier);
				}

				cglibCopier.copy(from, to, new FastCopierConverter() {
					@SuppressWarnings({ "unchecked" })
					public Object convert(Object fromValue, Object target,
							Class sourceClass, Class targetClass, Object context) {
						if (converters == null) {
							if (!getWrapper(targetClass).isAssignableFrom(
									getWrapper(sourceClass))) {
								return target;
							}
						}
						if (includes != null && !includes.contains(context)) {
							return target;
						}
						if (excludes != null && excludes.contains(context)) {
							return target;
						}
						if (!includeNull && fromValue == null) {
							return target;
						}
						if (!includeEmpty && fromValue != null
								&& isEmpty(fromValue)) {
							return target;
						}
						if (fromValue != null) {
							if (!includePrimitiveFalse
									&& sourceClass.equals(boolean.class)
									&& Beans.equals(fromValue, false)) {
								return target;
							}
							if (!includePrimitiveZero
									&& sourceClass.isPrimitive()
									&& fromValue instanceof Number
									&& Beans.equals(fromValue, 0)) {
								return target;
							}
						}
						Object toValue = converters != null ? converters
								.convert(fromValue, targetClass) : fromValue;
						return toValue;
					}

					private Class<?> getWrapper(Class<?> type) {
						if (!type.isPrimitive()) {
							return type;
						}
						if (boolean.class.equals(type)) {
							return Boolean.class;
						} else if (float.class.equals(type)) {
							return Float.class;
						} else if (long.class.equals(type)) {
							return Long.class;
						} else if (int.class.equals(type)) {
							return Integer.class;
						} else if (short.class.equals(type)) {
							return Short.class;
						} else if (byte.class.equals(type)) {
							return Byte.class;
						} else if (double.class.equals(type)) {
							return Double.class;
						} else if (char.class.equals(type)) {
							return Character.class;
						} else {
							return Void.class;
						}
					}
				});
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean equals(Object a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		if (a.equals(b)) {
			return true;
		}
		// 比较
		if (a instanceof Comparable && b instanceof Comparable) {
			return ((Comparable) a).compareTo((Comparable) b) != 0;
		}
		return false;
	}

	/**
	 * 从数据来源对象创建一个Copier
	 * 
	 * @param from
	 * @return
	 */
	public static Copier from(Object from) {
		return new Copier().from(from);
	}

	/**
	 * 从数据目标对象创建一个Copier
	 * 
	 * @param tos
	 * @return
	 */
	public static Copier to(Object... tos) {
		return new Copier().to(tos);
	}

	private static Map<String, PropertyDescriptor> getPropertyMap(
			Class<?> type, boolean caseSensitive) {
		Map<String, PropertyDescriptor> propertyDescriptors = caseSensitive ? PROPERTY_DESCRIPTORS_CASESENSITIVE_MAP
				.get(type) : PROPERTY_DESCRIPTORS_CASEINSENSITIVE_MAP.get(type);
		if (propertyDescriptors == null) {
			propertyDescriptors = new ConcurrentHashMap<String, PropertyDescriptor>();
			try {
				for (PropertyDescriptor propertyDescriptor : Introspector
						.getBeanInfo(type).getPropertyDescriptors()) {
					if (propertyDescriptor.getPropertyType() == null) {
						continue;
					}
					if ("class".equals(propertyDescriptor.getName())) {
						continue;
					}
					if (caseSensitive) {
						propertyDescriptors.put(propertyDescriptor.getName(),
								propertyDescriptor);
					} else {
						propertyDescriptors.put(propertyDescriptor.getName()
								.toLowerCase(), propertyDescriptor);
					}
				}
			} catch (IntrospectionException e) {
				throw Lang.unchecked(e);
			}
			if (caseSensitive) {
				PROPERTY_DESCRIPTORS_CASESENSITIVE_MAP.put(type,
						propertyDescriptors);
			} else {
				PROPERTY_DESCRIPTORS_CASEINSENSITIVE_MAP.put(type,
						propertyDescriptors);
			}
		}
		return propertyDescriptors;

	}

	/**
	 * 为bean设置属性值
	 * 
	 * @param bean
	 *            要设置的对象
	 * @param converters
	 *            属性的类型转换器
	 * @param nameString
	 *            属性名，使用空格、逗号等隔开多个字段名，忽略大小写
	 * @param args
	 *            属性值
	 */
	public static void set(Object bean, Converters converters,
			String nameString, Object... args) {
		Errors.notNull(bean, "The bean should not be null");
		Errors.notNull(converters, "The converters should not be null");
		Errors.notNull(args, "The args should not be null");
		if (nameString == null || nameString.trim().length() == 0) {
			throw new IllegalStateException(
					String.format("The nameString should not be empty"));
		}
		String[] names = nameString.split("[\\s,，]+");
		Errors.isTrue(names.length == args.length,
				"The names length is %d but got %d arguments", names.length,
				args.length);
		Class<?> beanType = bean.getClass();
		Map<String, PropertyDescriptor> propertyMap = getPropertyMap(beanType,
				false);
		for (int i = 0; i < names.length; i++) {
			String name = names[i].trim();
			PropertyDescriptor propertyDescriptor = propertyMap.get(name);
			Errors.notNull(propertyDescriptor,
					"The property %s of bean %s is not exist", name, bean);
			Method writeMethod = propertyDescriptor.getWriteMethod();
			Errors.notNull(writeMethod,
					"The property %s of bean %s is read only", name, bean);
			Object value = converters.convert(args[i],
					writeMethod.getParameterTypes()[0]);
			try {
				writeMethod.invoke(bean, value);
			} catch (IllegalAccessException e) {
				throw Lang.unchecked(e);
			} catch (IllegalArgumentException e) {
				throw Lang.unchecked(e);
			} catch (InvocationTargetException e) {
				throw Lang.unchecked(e);
			}
		}
	}

	/**
	 * 为bean设置属性值
	 * 
	 * @param bean
	 *            要设置的对象
	 * @param nameString
	 *            属性名，使用空格、逗号等隔开多个字段名，忽略大小写
	 * @param args
	 *            属性值
	 */
	public static void set(Object bean, String nameString, Object... args) {
		set(bean, Converters.BASE, nameString, args);
	}
}
