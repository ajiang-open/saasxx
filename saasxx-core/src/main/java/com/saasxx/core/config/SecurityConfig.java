package com.saasxx.core.config;

import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.saasxx.framework.Lang;
import com.saasxx.framework.security.shiro.ShiroRealm;
import com.saasxx.framework.security.shiro.ShiroService;
import com.saasxx.framework.security.shiro.SupportedSuccessfulStrategy;
import com.saasxx.framework.security.shiro.jwt.JWTAuthenticationFilter;
import com.saasxx.framework.security.shiro.jwt.JWTShiroRealm;

@Configuration
public class SecurityConfig {

	@Autowired
	ApplicationContext ac;

	@Bean
	public JWTAuthenticationFilter jwtAuthenticationFilter() {
		JWTAuthenticationFilter jwtAuthenticationFilter = new JWTAuthenticationFilter();
		return jwtAuthenticationFilter;
	}

	@Bean(name = { "shiroFilter" })
	public ShiroFilterFactoryBean shiroFilterFactoryBean() {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
		shiroFilterFactoryBean.setSecurityManager(securityManager());
		shiroFilterFactoryBean.getFilters().put("jwt", jwtAuthenticationFilter());
		shiroFilterFactoryBean.setFilterChainDefinitionMap(Lang.newMap("/web-rpc", "noSessionCreation,jwt",
				"/security/**", "noSessionCreation,jwt", "*.validate", "noSessionCreation,jwt"));
		return shiroFilterFactoryBean;
	}

	@Bean
	public DefaultWebSecurityManager securityManager() {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealms(Lang.newList(realm(), jwtRealm()));
		if (securityManager.getAuthenticator() instanceof ModularRealmAuthenticator) {
			ModularRealmAuthenticator modularRealmAuthenticator = (ModularRealmAuthenticator) securityManager
					.getAuthenticator();
			modularRealmAuthenticator.setAuthenticationStrategy(new SupportedSuccessfulStrategy());
		}
		return securityManager;
	}

	@Bean
	public Realm realm() {
		ShiroRealm shiroRealm = new ShiroRealm();
		shiroRealm.setShiroService(ac.getBean(ShiroService.class));
		return shiroRealm;
	}

	@Bean
	public Realm jwtRealm() {
		JWTShiroRealm shiroRealm = new JWTShiroRealm();
		shiroRealm.setAppName("saasxx");
		shiroRealm.setShiroService(ac.getBean(ShiroService.class));
		return shiroRealm;
	}

}
