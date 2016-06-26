package com.saasxx.framework.lang;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.saasxx.framework.Lang;

/**
 * 类型工具类（需要Spring支持）
 * 
 * @author lujijiang
 * 
 */
public class Classes {

	public interface ClassFilter {
		boolean accept(Class<Object> type);
	}

	/**
	 * jar包中类路径的标记字符串
	 */
	private static final String PATH_DELIMITER_JAR = "!/";

	/**
	 * classes下面类路径的标记字符串
	 */
	private static final String PATH_DELIMITER_CLASSES = "classes/";
	/**
	 * 找到所有的类资源
	 */
	private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	private Classes() {
	}

	private static PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();

	/**
	 * 用于扫描包下面的类
	 * 
	 * @param basePackage
	 *            根包
	 * @param classFilter
	 *            类过滤器
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Set<Class> scanPackage(String basePackage,
			final ClassFilter classFilter) {
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
				+ basePackage.replace('.', '/')
				+ "/"
				+ DEFAULT_RESOURCE_PATTERN;
		try {
			Resource[] resources = pathMatchingResourcePatternResolver
					.getResources(packageSearchPath);
			Set<Class> classes = new LinkedHashSet<Class>();
			for (final Resource resource : resources) {
				String path = resource.getURL().getPath();
				int p;
				if ((p = path.lastIndexOf(PATH_DELIMITER_CLASSES)) != -1) {
					p += PATH_DELIMITER_CLASSES.length();
				} else if ((p = path.lastIndexOf(PATH_DELIMITER_JAR)) != -1) {
					p += PATH_DELIMITER_JAR.length();
				}
				if (p != -1) {
					String name = path.substring(p, path.lastIndexOf("."))
							.replace('/', '.');
					try {
						Class type = Class.forName(name, false,
								Classes.class.getClassLoader());
						if (type != null && classFilter.accept(type)) {
							classes.add(type);
						}
					} catch (Exception e) {
					}
				}
			}
			return classes;
		} catch (Exception e) {
			throw Lang.unchecked(e, "scan package path %s fail ", basePackage);
		}
	}

	/**
	 * 根据类型名实例化类型对象
	 * 
	 * @param className
	 * @return
	 */
	public static Class<?> forName(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			if (Byte.TYPE.getName().equals(className)) {
				return Byte.TYPE;
			}
			if (Short.TYPE.getName().equals(className)) {
				return Short.TYPE;
			}
			if (Integer.TYPE.getName().equals(className)) {
				return Integer.TYPE;
			}
			if (Long.TYPE.getName().equals(className)) {
				return Long.TYPE;
			}
			if (Float.TYPE.getName().equals(className)) {
				return Float.TYPE;
			}
			if (Double.TYPE.getName().equals(className)) {
				return Double.TYPE;
			}
			if (Boolean.TYPE.getName().equals(className)) {
				return Boolean.TYPE;
			}
			if (Character.TYPE.getName().equals(className)) {
				return Character.TYPE;
			}
			// 处理数组
			int p = className.lastIndexOf("[]");
			if (p != -1) {
				return Array.newInstance(forName(className.substring(0, p)), 0)
						.getClass();
			}
			throw Lang.unchecked(e);
		}
	}
}
