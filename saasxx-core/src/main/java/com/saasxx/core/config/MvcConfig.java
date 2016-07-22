package com.saasxx.core.config;

import java.util.List;

import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AopAllianceAnnotationsAuthorizingMethodInterceptor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.saasxx.framework.lang.Springs;
import com.saasxx.framework.security.shiro.ShiroUser;
import com.saasxx.framework.security.shiro.Shiros;
import com.saasxx.framework.web.http.converter.JsonHttpMessageConverter;
import com.saasxx.framework.web.springmvc.handler.JsonExceptionResolver;
import com.saasxx.framework.web.springmvc.processor.PrefixFormServletModelAttributeMethodProcessor;
import com.saasxx.framework.web.webrpc.WebRpcExporter;

@EnableWebMvc

@Configuration
@ComponentScan({ Constants.BACKAGE_WEB })
class MvcConfig extends WebMvcConfigurerAdapter {

	private Environment env = Springs.getEnvironment("classpath:${spring.profiles.active}/config.properties");

	@Autowired
	private SecurityManager securityManager;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		JsonHttpMessageConverter jsonHttpMessageConverter = new JsonHttpMessageConverter();
		jsonHttpMessageConverter.setFeatures(SerializerFeature.BrowserCompatible);
		converters.add(jsonHttpMessageConverter);
	}

	@Override
	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		JsonExceptionResolver jsonSimpleMappingExceptionResolver = new JsonExceptionResolver();
		jsonSimpleMappingExceptionResolver.setFeatures(SerializerFeature.BrowserCompatible);
		exceptionResolvers.add(jsonSimpleMappingExceptionResolver);

	}

	/*
	 * 用于处理分页的情况
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new PageableHandlerMethodArgumentResolver());
		argumentResolvers.add(new PrefixFormServletModelAttributeMethodProcessor());
	}

	/**
	 * 用来处理文件上传的解析，使用时声明参数为CommonsMultipartFile类型即可
	 * 
	 * @return 返回文件上传解析器
	 */
	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver commonsMultipartResolver() {
		CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
		// 设置最大上传文件大小，这里是100MB
		Long maxUpladSize = env.getProperty("web.maxUpladSize", Long.class, 1024 * 1024 * 100L);
		commonsMultipartResolver.setMaxUploadSize(maxUpladSize);
		// 设置最大上传缓存大小，这里是1MB
		Integer maxInMemorySize = env.getProperty("web.maxInMemorySize", Integer.class, 1024 * 1024);
		commonsMultipartResolver.setMaxInMemorySize(maxInMemorySize);
		return commonsMultipartResolver;
	}

	/*
	 * 设置首页
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "/index.html");
	}

	/*
	 * 设置静态资源默认处理器
	 */
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/views/", ".jsp");
	}

	@Bean(name = "lifecycleBeanPostProcessor")
	public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	@Bean
	@DependsOn("lifecycleBeanPostProcessor")
	public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
		return new DefaultAdvisorAutoProxyCreator();
	}

	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		authorizationAttributeSourceAdvisor.setAdvice(new AopAllianceAnnotationsAuthorizingMethodInterceptor() {
			protected void assertAuthorized(MethodInvocation methodInvocation) throws AuthorizationException {
				ShiroUser shiroUser = Shiros.currentUser();
				if (shiroUser != null && shiroUser.isSuperAdmin()) {
					// 如果是超级管理员，则不作AOP方法校验
					return;
				}
				super.assertAuthorized(methodInvocation);
			}
		});
		return authorizationAttributeSourceAdvisor;
	}

	@Bean(name = "/web-rpc")
	public WebRpcExporter webRpcExporter() {
		WebRpcExporter webRpcExporter = new WebRpcExporter();
		return webRpcExporter;
	}

}