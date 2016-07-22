package com.saasxx.core.config;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.springframework.web.util.IntrospectorCleanupListener;

import com.saasxx.framework.lang.Springs;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;
import com.saasxx.framework.web.filter.CrossDomainFilter;
import com.saasxx.framework.web.filter.ImageScaleFilter;
import com.saasxx.framework.web.filter.MultiFilter;
import com.saasxx.framework.web.filter.StaticFilter;
import com.saasxx.framework.web.filter.WebsFilter;

/**
 * 程序初始化配置类，作用相当于web.xml
 * 
 * @author lujijiang
 *
 */
public class ApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer
		implements WebApplicationInitializer {

	private final static Log log = Logs.getLog();

	private Environment env = Springs.getEnvironment("classpath:${spring.profiles.active}/config.properties");

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		WebApplicationContext context = (WebApplicationContext) super.createRootApplicationContext();
		String activeProfile = System.getProperty("spring.profiles.active");
		if (activeProfile == null) {
			activeProfile = "dev"; // Development Profile "default"
			System.setProperty("spring.profiles.active", activeProfile);
		}
		log.info("Activating >>>>>> {} <<<<<< spring profile", activeProfile.toUpperCase());

		((ConfigurableEnvironment) context.getEnvironment()).setActiveProfiles(activeProfile);
		return context;
	}

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] { MainConfig.class,
				// MailConfig.class,
				SecurityConfig.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] { MvcConfig.class };
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	public void onStartup(ServletContext servletContext) throws ServletException {
		// add listener by lujijiang
		servletContext.addListener(IntrospectorCleanupListener.class);
		servletContext.addListener(RequestContextListener.class);
		super.onStartup(servletContext);
	}

	@Override
	protected Filter[] getServletFilters() {
		WebsFilter websFilter = new WebsFilter();

		StaticFilter staticFilter = new StaticFilter();
		staticFilter.setFilePool(env.getProperty("filepool.path"));

		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		// characterEncodingFilter.setForceEncoding(true);
		characterEncodingFilter.setEncoding("utf-8");

		DelegatingFilterProxy shiroFilter = new DelegatingFilterProxy();
		shiroFilter.setTargetFilterLifecycle(true);

		ImageScaleFilter imageScaleFilter = new ImageScaleFilter();
		imageScaleFilter.setFilePool(env.getProperty("filepool.path"));

		MultiFilter multiFilter = MultiFilter.create();
		multiFilter.addFilter(new CrossDomainFilter());
		multiFilter.addFilter(imageScaleFilter);
		multiFilter.addFilter(staticFilter);
		multiFilter.addFilter(websFilter);
		multiFilter.addFilter(characterEncodingFilter);
		multiFilter.addFilter(new OpenEntityManagerInViewFilter());
		multiFilter.addFilter(shiroFilter, "shiroFilter");
		return new Filter[] { multiFilter };
	}
}