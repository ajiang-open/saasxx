package com.saasxx.framework.lang;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import com.saasxx.framework.Lang;

/**
 * 比较工具类
 * 
 * @author lujijiang
 * 
 */
public class Compares {

	private Compares() {
	}

	private final static AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

	private final static Logger log = LoggerFactory.getLogger(Compares.class);

	/**
	 * 属性值不同信息元
	 * 
	 * @author lujijiang
	 * 
	 */
	public static class ValueMeta {
		String path;
		Object sourceValue;
		Object targetValue;

		public String getPath() {
			return path;
		}

		public Object getSourceValue() {
			return sourceValue;
		}

		public Object getTargetValue() {
			return targetValue;
		}

		public String toString() {
			return "diff:path=" + path + ", sourceValue=" + sourceValue
					+ ", targetValue=" + targetValue;
		}

	}

	/**
	 * 比较对象 *
	 * 
	 * @param packagePattern
	 *            包路径Ant匹配式
	 * @param source
	 * @param target
	 * @return
	 */
	public static Set<ValueMeta> compareProperty(String packagePattern,
			Object source, Object target) {
		return compareProperty(packagePattern, source, target, "$",
				new HashSet<Integer>());
	}

	/**
	 * 比较对象 *
	 * 
	 * @param packagePattern
	 *            包路径Ant匹配式
	 * @param source
	 * @param target
	 * @param prefixPath
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	private static Set<ValueMeta> compareProperty(String packagePattern,
			Object source, Object target, String prefixPath,
			Set<Integer> idHashs) {
		Set<ValueMeta> diffMetas = Lang.newSet();
		if (source == null || target == null) {
			if (source == null && target == null) {
				return diffMetas;
			}
			ValueMeta propertyValueDiffMeta = new ValueMeta();
			propertyValueDiffMeta.path = prefixPath;
			propertyValueDiffMeta.sourceValue = source;
			propertyValueDiffMeta.targetValue = target;
			diffMetas.add(propertyValueDiffMeta);
			return diffMetas;
		}
		if (Lang.isBaseType(source.getClass())
				|| Lang.isBaseType(target.getClass())) {
			if (!Lang.equals(source, target)) {
				ValueMeta propertyValueDiffMeta = new ValueMeta();
				propertyValueDiffMeta.path = prefixPath;
				propertyValueDiffMeta.sourceValue = source;
				propertyValueDiffMeta.targetValue = target;
				diffMetas.add(propertyValueDiffMeta);
			}
			return diffMetas;
		}

		// 判断是否循环比较过了
		if (idHashs.contains(System.identityHashCode(source))
				|| idHashs.contains(System.identityHashCode(target))) {
			return diffMetas;
		}
		idHashs.add(System.identityHashCode(source));
		idHashs.add(System.identityHashCode(target));

		Class type = null;
		if (source.getClass().isAssignableFrom(target.getClass())) {
			type = (Class) source.getClass();
		} else if (target.getClass().isAssignableFrom(source.getClass())) {
			type = (Class) target.getClass();
		}
		if (type == null) {
			ValueMeta propertyValueDiffMeta = new ValueMeta();
			propertyValueDiffMeta.path = prefixPath;
			propertyValueDiffMeta.sourceValue = source;
			propertyValueDiffMeta.targetValue = target;
			diffMetas.add(propertyValueDiffMeta);
			return diffMetas;
		}

		if (Collection.class.isAssignableFrom(type)) {
			source = ((Collection) source).toArray();
			target = ((Collection) target).toArray();
			type = source.getClass();
		}

		if (type.isArray()) {
			int length = Math.max(Array.getLength(source),
					Array.getLength(target));
			for (int i = 0; i < length; i++) {
				String path = prefixPath.concat("[").concat(String.valueOf(i))
						.concat("]");
				Object sourceValue = Array.getLength(source) >= length ? Array
						.get(source, i) : null;
				Object targetValue = Array.getLength(target) >= length ? Array
						.get(target, i) : null;
				diffMetas.addAll(compareProperty(packagePattern, sourceValue,
						targetValue, path, idHashs));
			}
			return diffMetas;
		}

		if (Map.class.isAssignableFrom(type)) {
			ValueMeta propertyValueDiffMeta = new ValueMeta();
			propertyValueDiffMeta.path = prefixPath;
			propertyValueDiffMeta.sourceValue = source;
			propertyValueDiffMeta.targetValue = target;
			diffMetas.add(propertyValueDiffMeta);
			return diffMetas;
		}

		if (!ANT_PATH_MATCHER.match(packagePattern.replace('.', '/'), type
				.getPackage().getName().replace('.', '/'))) {
			return diffMetas;
		}
		try {
			PropertyDescriptor[] propertyDescriptors = Mirrors
					.getPropertys(type);
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				Method readMethod = propertyDescriptor.getReadMethod();
				if (readMethod != null) {
					if (!readMethod.isAccessible()) {
						readMethod.setAccessible(true);
					}
					String path = prefixPath.concat(".").concat(
							propertyDescriptor.getName());
					try {
						Object sourceValue = readMethod.invoke(source);
						Object targetValue = readMethod.invoke(target);
						diffMetas.addAll(compareProperty(packagePattern,
								sourceValue, targetValue, path, idHashs));
					} catch (Exception e) {
						log.warn(String.format("对比对象路径%s时发生异常", path), e);
					}
				}
			}
			return diffMetas;
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 比较对象 *
	 * 
	 * @param packagePattern
	 *            包路径Ant匹配式
	 * @param source
	 * @param target
	 * @return
	 */
	public static Set<ValueMeta> compareField(String packagePattern,
			Object source, Object target) {
		return compareField(packagePattern, source, target, "$",
				new HashSet<Integer>());
	}

	/**
	 * 比较对象 *
	 * 
	 * @param packagePattern
	 *            包路径Ant匹配式
	 * @param source
	 * @param target
	 * @param prefixPath
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	private static Set<ValueMeta> compareField(String packagePattern,
			Object source, Object target, String prefixPath,
			Set<Integer> idHashs) {
		Set<ValueMeta> diffMetas = Lang.newSet();
		if (source == null || target == null) {
			if (source == null && target == null) {
				return diffMetas;
			}
			ValueMeta propertyValueDiffMeta = new ValueMeta();
			propertyValueDiffMeta.path = prefixPath;
			propertyValueDiffMeta.sourceValue = source;
			propertyValueDiffMeta.targetValue = target;
			diffMetas.add(propertyValueDiffMeta);
			return diffMetas;
		}
		if (Lang.isBaseType(source.getClass())
				|| Lang.isBaseType(target.getClass())) {
			if (!Lang.equals(source, target)) {
				ValueMeta propertyValueDiffMeta = new ValueMeta();
				propertyValueDiffMeta.path = prefixPath;
				propertyValueDiffMeta.sourceValue = source;
				propertyValueDiffMeta.targetValue = target;
				diffMetas.add(propertyValueDiffMeta);
			}
			return diffMetas;
		}

		// 判断是否循环比较过了
		if (idHashs.contains(System.identityHashCode(source))
				|| idHashs.contains(System.identityHashCode(target))) {
			return diffMetas;
		}
		idHashs.add(System.identityHashCode(source));
		idHashs.add(System.identityHashCode(target));

		Class<?> type = null;
		if (source.getClass().isAssignableFrom(target.getClass())) {
			type = (Class) source.getClass();
		} else if (target.getClass().isAssignableFrom(source.getClass())) {
			type = (Class) target.getClass();
		}
		if (type == null) {
			ValueMeta propertyValueDiffMeta = new ValueMeta();
			propertyValueDiffMeta.path = prefixPath;
			propertyValueDiffMeta.sourceValue = source;
			propertyValueDiffMeta.targetValue = target;
			diffMetas.add(propertyValueDiffMeta);
			return diffMetas;
		}

		if (Collection.class.isAssignableFrom(type)) {
			source = ((Collection) source).toArray();
			target = ((Collection) target).toArray();
			type = source.getClass();
		}

		if (type.isArray()) {
			int length = Math.max(Array.getLength(source),
					Array.getLength(target));
			for (int i = 0; i < length; i++) {
				String path = prefixPath.concat("[").concat(String.valueOf(i))
						.concat("]");
				Object sourceValue = Array.getLength(source) >= length ? Array
						.get(source, i) : null;
				Object targetValue = Array.getLength(target) >= length ? Array
						.get(target, i) : null;
				diffMetas.addAll(compareField(packagePattern, sourceValue,
						targetValue, path, idHashs));
			}
			return diffMetas;
		}

		if (Map.class.isAssignableFrom(type)) {
			ValueMeta propertyValueDiffMeta = new ValueMeta();
			propertyValueDiffMeta.path = prefixPath;
			propertyValueDiffMeta.sourceValue = source;
			propertyValueDiffMeta.targetValue = target;
			diffMetas.add(propertyValueDiffMeta);
			return diffMetas;
		}

		if (!ANT_PATH_MATCHER.match(packagePattern.replace('.', '/'), type
				.getPackage().getName().replace('.', '/'))) {
			return diffMetas;
		}
		try {
			Field[] fields = Mirrors.getFields(type);
			for (Field field : fields) {
				String path = prefixPath.concat(".").concat(field.getName());
				try {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					Object sourceValue = field.get(source);
					Object targetValue = field.get(target);
					diffMetas.addAll(compareField(packagePattern, sourceValue,
							targetValue, path, idHashs));
				} catch (Exception e) {
					log.warn(String.format("对比对象路径%s时发生异常", path), e);
				}
			}
			return diffMetas;
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}
}