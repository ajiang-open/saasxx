package com.saasxx.framework.lang;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.util.Assert;

import com.saasxx.framework.Lang;

/**
 * Spring工具类
 * 
 * @author lujijiang
 * 
 */
public class Springs implements ApplicationListener<ContextRefreshedEvent> {

	private static ApplicationContext context;

	public void onApplicationEvent(ContextRefreshedEvent event) {
		context = event.getApplicationContext();
	}

	/**
	 * 获取bean实例对象
	 * 
	 * <pre>
	 * CodeService codeService = Springs.getBean(CodeService.class, &quot;codeService&quot;);
	 * </pre>
	 * 
	 * @param beanClass
	 *            实现类的Class
	 * @param beanName
	 *            bean名称
	 * @return bean实例对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(Class<? extends T> beanClass, String beanName) {
		Assert.notNull(context,
				"The spring applicationContext was not initialized");
		return (T) context.getBean(beanName);
	}

	/**
	 * 获取bean实例对象
	 * 
	 * <pre>
	 * CodeService codeService = (CodeService) Springs.getBean(&quot;codeService&quot;);
	 * </pre>
	 * 
	 * @param beanName
	 *            bean名称
	 * @return bean实例对象
	 */
	public static Object getBean(String beanName) {
		Assert.notNull(context,
				"The spring applicationContext was not initialized");
		return context.getBean(beanName);
	}

	/**
	 * 获取bean实例对象
	 * 
	 * <pre>
	 * CodeService codeService = Springs.getBean(CodeServiceSpringImpl.class);
	 * </pre>
	 * 
	 * @param beanClass
	 *            实现类的Class
	 * @return bean实例对象
	 */
	public static <T> T getBean(Class<? extends T> beanClass) {
		Assert.notNull(context,
				"The spring applicationContext was not initialized");
		return context.getBean(beanClass);
	}

	private static ResourceLoader resourceLoader = new DefaultResourceLoader();

	private static Environment parent = new StandardEnvironment() {
	};

	/**
	 * 根据属性文件生成环境
	 * 
	 * @param propertiesFiles
	 * @return
	 */
	public static Environment getEnvironment(final String... propertiesFiles) {

		if (propertiesFiles == null) {
			throw new IllegalStateException(
					"propertiesFiles should not be null");
		}

		return new StandardEnvironment() {

			public void customizePropertySources(
					MutablePropertySources propertySources) {
				for (int i = 0; i < propertiesFiles.length; i++) {
					String propertiesFile = propertiesFiles[i];
					Assert.notNull(propertiesFile,
							"propertiesFiles should not be contain a null value");
					propertiesFile = parent
							.resolveRequiredPlaceholders(propertiesFile);
					Resource resource = resourceLoader
							.getResource(propertiesFile);
					if (!resource.exists()) {
						throw new IllegalArgumentException(String.format(
								"The resource %s is not exist", propertiesFile));
					}
					try {
						propertySources.addLast(new ResourcePropertySource(
								resource.getFilename(), resource));
					} catch (IOException e) {
						throw Lang.unchecked(e);
					}
				}
				super.customizePropertySources(propertySources);
			}
		};
	}

}
