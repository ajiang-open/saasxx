package com.saasxx.framework.lang;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.MethodUtils;
import org.springframework.util.AntPathMatcher;

import com.saasxx.framework.Lang;

/**
 * 反射工具类
 * 
 * @author lujijiang
 * 
 */
public class Mirrors {

	private Mirrors() {
	}

	private final static AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

	/**
	 * 字段处理器
	 * 
	 * @author lujijiang
	 * 
	 */
	public static interface FieldHandler {
		/**
		 * 处理方法
		 * 
		 * @param path
		 *            路径
		 * @param owner
		 *            所属对象
		 * @param field
		 *            字段信息
		 */
		public void handle(String path, Object owner, Field field);
	}

	/**
	 * 属性处理器
	 * 
	 * @author lujijiang
	 * 
	 */
	public static interface PropertyHandler {
		/**
		 * 处理方法
		 * 
		 * @param path
		 *            路径
		 * @param owner
		 *            所属对象
		 * @param propertyDescriptor
		 *            属性信息
		 */
		public void handle(String path, Object owner,
				PropertyDescriptor propertyDescriptor);
	}

	/**
	 * 获得方法返回类型的参数泛型
	 * 
	 * @param method
	 * @param index
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class getGenricReturnType(final Method method, final int index) {
		Type genericReturnType = method.getGenericReturnType();
		if (genericReturnType != null) {
			if (genericReturnType instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
				Type[] actualTypeArguments = parameterizedType
						.getActualTypeArguments();
				if (actualTypeArguments.length > index) {
					return (Class) actualTypeArguments[index];
				}
			}
		}
		return Object.class;
	}

	/**
	 * 依靠属性或者字段查找注解
	 * 
	 * @param propertyDescriptor
	 * @param annotationType
	 * @return
	 */
	public static <T extends Annotation> T getAnnotation(
			PropertyDescriptor propertyDescriptor, Class<T> annotationType) {
		Method readMethod = propertyDescriptor.getReadMethod();
		if (readMethod != null) {
			T annotation = readMethod.getAnnotation(annotationType);
			if (annotation != null) {
				return annotation;
			}
		}
		Method writeMethod = propertyDescriptor.getWriteMethod();
		if (writeMethod != null) {
			T annotation = writeMethod.getAnnotation(annotationType);
			if (annotation != null) {
				return annotation;
			}
		}
		Class<?> declaringClass = readMethod == null ? writeMethod
				.getDeclaringClass() : readMethod.getDeclaringClass();
		while (declaringClass != null) {
			try {
				Field field = declaringClass
						.getDeclaredField(propertyDescriptor.getName());
				T annotation = field.getAnnotation(annotationType);
				if (annotation != null) {
					return annotation;
				}
			} catch (Exception e) {
			} finally {
				declaringClass = declaringClass.getSuperclass();
			}
		}
		return null;
	}

	/**
	 * 获得指定类型及其父类的所有属性
	 * 
	 * @param type
	 * @return
	 */
	public static Field[] getFields(Class<? extends Object> type) {
		Set<Field> fields = Lang.newSet();
		while (type != null) {
			for (Field field : type.getDeclaredFields()) {
				fields.add(field);
			}
			type = type.getSuperclass();
		}
		return fields.toArray(new Field[0]);
	}

	public static Map<String, PropertyDescriptor> getPropertyMap(Class<?> type) {
		Map<String, PropertyDescriptor> propertyDescriptors = Lang.newMap();
		try {
			for (PropertyDescriptor propertyDescriptor : Introspector
					.getBeanInfo(type).getPropertyDescriptors()) {
				if (propertyDescriptor.getPropertyType() == null) {
					continue;
				}
				if ("class".equals(propertyDescriptor.getName())) {
					continue;
				}
				propertyDescriptors.put(propertyDescriptor.getName(),
						propertyDescriptor);
			}
		} catch (IntrospectionException e) {
			throw Lang.unchecked(e);
		}
		return propertyDescriptors;
	}

	public static PropertyDescriptor[] getPropertys(Class<?> type) {
		List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
		try {
			for (PropertyDescriptor propertyDescriptor : Introspector
					.getBeanInfo(type).getPropertyDescriptors()) {
				if (propertyDescriptor.getPropertyType() == null) {
					continue;
				}
				if ("class".equals(propertyDescriptor.getName())) {
					continue;
				}
				propertyDescriptors.add(propertyDescriptor);
			}
		} catch (IntrospectionException e) {
			throw Lang.unchecked(e);
		}
		return propertyDescriptors.toArray(new PropertyDescriptor[0]);
	}

	private static void handleProperty(String packagePattern, Object domain,
			PropertyHandler propertyHandler, Set<Integer> idHashs,
			String prefixPath) {
		if (domain == null) {
			return;
		}
		if (Lang.isBaseType(domain.getClass())) {
			return;
		}

		// 判断是否循环了
		Integer id = System.identityHashCode(domain);
		if (idHashs.contains(id)) {
			return;
		}
		idHashs.add(id);

		if (domain instanceof Collection) {
			domain = ((Collection<?>) domain).toArray();
		}

		if (domain.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(domain); i++) {
				handleProperty(packagePattern, Array.get(domain, i),
						propertyHandler, idHashs, prefixPath.concat("[")
								.concat(String.valueOf(i)).concat("]"));
			}
			return;
		}

		if (domain instanceof Map) {
			return;
		}

		if (!ANT_PATH_MATCHER.match(packagePattern.replace('.', '/'), domain
				.getClass().getPackage().getName().replace('.', '/'))) {
			return;
		}

		try {
			PropertyDescriptor[] propertyDescriptors = getPropertys(domain
					.getClass());
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				String newPath = prefixPath.concat(".").concat(
						propertyDescriptor.getName());
				propertyHandler.handle(newPath, domain, propertyDescriptor);
				if (!Lang.isBaseType(propertyDescriptor.getPropertyType())) {
					Method readMethod = propertyDescriptor.getReadMethod();
					if (readMethod != null) {
						handleProperty(packagePattern,
								readMethod.invoke(domain), propertyHandler,
								idHashs, newPath);
					}
				}
			}
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 处理属性
	 * 
	 * @param packagePattern
	 *            包模式，如com..aaa..service..
	 * @param domain
	 *            领域对象
	 * @param propertyHandler
	 *            属性处理器
	 */
	public static void handleProperty(String packagePattern, Object domain,
			PropertyHandler propertyHandler) {
		handleProperty(packagePattern, domain, propertyHandler,
				new HashSet<Integer>(), "$");
	}

	private static void handleField(String packagePattern, Object domain,
			FieldHandler fieldHandler, Set<Integer> idHashs, String prefixPath) {
		if (domain == null) {
			return;
		}
		if (Lang.isBaseType(domain.getClass())) {
			return;
		}

		// 判断是否循环了
		Integer id = System.identityHashCode(domain);
		if (idHashs.contains(id)) {
			return;
		}
		idHashs.add(id);

		if (domain instanceof Collection) {
			domain = ((Collection<?>) domain).toArray();
		}

		if (domain.getClass().isArray()) {
			for (int i = 0; i < Array.getLength(domain); i++) {
				handleField(packagePattern, Array.get(domain, i), fieldHandler,
						idHashs,
						prefixPath.concat("[").concat(String.valueOf(i))
								.concat("]"));
			}
			return;
		}

		if (domain instanceof Map) {
			return;
		}

		if (!ANT_PATH_MATCHER.match(packagePattern.replace('.', '/'), domain
				.getClass().getPackage().getName().replace('.', '/'))) {
			return;
		}

		try {
			Field[] fields = getFields(domain.getClass());
			for (Field field : fields) {
				String newPath = prefixPath.concat(".").concat(field.getName());
				fieldHandler.handle(newPath, domain, field);
				if (!Lang.isBaseType(field.getType())) {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					handleField(packagePattern, field.get(domain),
							fieldHandler, idHashs, newPath);
				}
			}
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 处理字段
	 * 
	 * @param packagePattern
	 *            包模式，如com..aaa..service..
	 * @param domain
	 *            领域对象
	 * @param fieldHandler
	 *            字段处理器
	 */
	public static void handleField(String packagePattern, Object domain,
			FieldHandler fieldHandler) {
		handleField(packagePattern, domain, fieldHandler,
				new HashSet<Integer>(), "$");
	}

	/**
	 * 获得超类的泛型
	 * 
	 * @param clazz
	 *            超类
	 * @param index
	 *            泛型序号
	 * @return
	 */
	public static Class<?> getSuperClassGenricType(Class<?> clazz, int index) {
		Type genType = clazz.getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}
		return (Class<?>) params[index];
	}

	/**
	 * 返回类型匹配转换需要的成本总和,越小越精确。用于匹配方法调用、类型转换等场景。 返回-1则说明无法匹配
	 * 
	 * @param srcArgs
	 *            源类型参数
	 * @param destArgs
	 *            目标类型参数
	 * @return 转换成本，-1则表示无法转换
	 */
	public static float getTotalTransformationCost(Class<?>[] srcArgs,
			Class<?>[] destArgs) {
		if (srcArgs.length != destArgs.length) {
			return -1;
		}
		float totalCost = 0.0f;
		for (int i = 0; i < srcArgs.length; i++) {
			Class<?> srcClass, destClass;
			srcClass = srcArgs[i];
			destClass = destArgs[i];
			if (!MethodUtils.isAssignmentCompatible(destClass, srcClass)) {
				return -1;
			}
			totalCost += getObjectTransformationCost(srcClass, destClass);
		}
		return totalCost;
	}

	private static float getObjectTransformationCost(Class<?> srcClass,
			Class<?> destClass) {
		float cost = 0.0f;
		while (srcClass != null && !destClass.equals(srcClass)) {
			if (destClass.isPrimitive()) {
				Class<?> destClassWrapperClazz = MethodUtils
						.getPrimitiveWrapper(destClass);
				if (destClassWrapperClazz != null
						&& destClassWrapperClazz.equals(srcClass)) {
					cost += 0.25f;
					break;
				}
			}
			if (destClass.isInterface()
					&& MethodUtils.isAssignmentCompatible(destClass, srcClass)) {
				cost += 0.25f;
				break;
			}
			cost++;
			srcClass = srcClass.getSuperclass();
		}

		if (srcClass == null) {
			cost += 1.5f;
		}

		return cost;
	}

}
