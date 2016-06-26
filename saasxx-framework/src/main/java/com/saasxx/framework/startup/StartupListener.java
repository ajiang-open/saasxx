package com.saasxx.framework.startup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import com.saasxx.framework.Lang;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

/**
 * 启动器
 * 
 * @author lujijiang
 * 
 */
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

	private static Log log = Logs.getLog();

	/**
	 * 文件后缀名
	 */
	private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	/**
	 * 分割字符串并返回迭代器，其中分割后空白字符串将被忽略
	 * 
	 * @param str
	 *            字符串
	 * @param delimiters
	 *            分隔字符串
	 * @return
	 */
	private static Iterator<String> splitIgnoreBlank(final String str, final String delimiters) {
		if (null == str)
			return null;
		return new Iterator<String>() {

			int fromIndex = 0;

			String subString;

			public boolean hasNext() {
				if (subString == null || subString.length() == 0) {
					if (fromIndex + delimiters.length() > str.length()) {
						return false;
					}
					int index = str.indexOf(delimiters, fromIndex);
					if (index == -1) {
						index = str.length();
					}
					subString = str.substring(fromIndex, index).trim();
					fromIndex = index + delimiters.length();
					return hasNext();
				}
				return true;
			}

			public String next() {
				if (subString == null) {
					throw new IllegalStateException("Should be call hasNext first");
				}
				String s = subString;
				subString = null;
				return s;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	private boolean isinitialized;

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * 要扫描的包
	 */
	private String basePackages;

	public String getBasePackages() {
		return basePackages;
	}

	public void setBasePackages(String basePackages) {
		this.basePackages = basePackages;
	}

	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event instanceof ContextRefreshedEvent && !isinitialized) {
			isinitialized = true;
			initializeStartup();
		}
	}

	private void initializeStartup() {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
		List<MethodMetadata> methodMetadatas = new ArrayList<MethodMetadata>();
		Iterator<String> scanPackageIterator = splitIgnoreBlank(basePackages, ",");
		while (scanPackageIterator.hasNext()) {
			String basePackage = scanPackageIterator.next();
			basePackage = '/' + basePackage.trim().replace('.', '/');
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + basePackage + "/"
					+ DEFAULT_RESOURCE_PATTERN;
			try {
				Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
				for (Resource resource : resources) {
					MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
					Set<MethodMetadata> methodMetadataSet = metadataReader.getAnnotationMetadata()
							.getAnnotatedMethods(Startup.class.getCanonicalName());
					methodMetadatas.addAll(methodMetadataSet);
				}
			} catch (Exception e) {
				throw Lang.unchecked(e);
			}
		}

		Collections.sort(methodMetadatas, new Comparator<MethodMetadata>() {
			public int compare(MethodMetadata o1, MethodMetadata o2) {
				int p1 = (Integer) o1.getAnnotationAttributes(Startup.class.getCanonicalName()).get("priority");
				int p2 = (Integer) o2.getAnnotationAttributes(Startup.class.getCanonicalName()).get("priority");
				return p2 - p1;
			}
		});

		for (MethodMetadata methodMetadata : methodMetadatas) {
			try {
				final Class<?> cls = Class.forName(methodMetadata.getDeclaringClassName());
				final Method method = cls.getMethod(methodMetadata.getMethodName());
				String[] activeProfiles = ((String[]) methodMetadata
						.getAnnotationAttributes(Startup.class.getCanonicalName()).get("activeProfiles"));
				if (Lang.isEmpty(activeProfiles)
						|| applicationContext.getEnvironment().acceptsProfiles(activeProfiles)) {
					try {
						if (!method.isAccessible()) {
							method.setAccessible(false);
						}
						boolean daemon = (Boolean) methodMetadata
								.getAnnotationAttributes(Startup.class.getCanonicalName()).get("daemon");
						if (methodMetadata.isStatic()) {
							if (method.getParameterTypes().length == 0) {
								if (daemon) {
									new Thread() {
										public void run() {
											try {
												method.invoke(cls);
											} catch (Exception e) {
												throw Lang.unchecked(e);
											}
										}
									}.start();
								} else {
									method.invoke(cls);
								}
							} else {
								throw new IllegalStateException(
										"The execute method parameters should be empty parameters");
							}
						} else {
							if (method.getParameterTypes().length == 0) {
								final Object bean = applicationContext.getBean(cls);
								if (daemon) {
									new Thread() {
										public void run() {
											try {
												method.invoke(bean);
											} catch (Exception e) {
												throw Lang.unchecked(e);
											}
										}
									}.start();
								} else {
									method.invoke(bean);
								}
							} else {
								throw new IllegalStateException(
										"The execute method parameters should be empty parameters");
							}
						}
					} catch (Exception e) {
						boolean ignoreError = (Boolean) methodMetadata
								.getAnnotationAttributes(Startup.class.getCanonicalName()).get("ignoreError");
						if (!ignoreError) {
							throw Lang.unchecked(e);
						} else {
							log.error(e, "The startup {} invoke error", methodMetadata);
						}
					}
				}
			} catch (Exception e) {
				throw Lang.unchecked(e);
			}
		}
	}

}
