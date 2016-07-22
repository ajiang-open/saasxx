package com.saasxx.core.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.hibernate.cache.ehcache.EhCacheRegionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.pagehelper.PageHelper;
import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.jdbc.JpaProxyDataSource;
import com.saasxx.framework.dao.orm.jpa.ImplicitNamingStrategyCompliantImpl;
import com.saasxx.framework.dao.orm.jpa.JpaCommentListener;
import com.saasxx.framework.io.FilePool;
import com.saasxx.framework.lang.Springs;
import com.saasxx.framework.startup.StartupListener;

import net.sf.log4jdbc.sql.jdbcapi.DataSourceSpy;

@EnableTransactionManagement(proxyTargetClass = true)
@EnableJpaRepositories(value = Constants.BACKAGE_DAO)
@MapperScan(Constants.BACKAGE_DAO)
@Configuration
@EnableAspectJAutoProxy
@ComponentScan({ Constants.BACKAGE_DAO, Constants.BACKAGE_SERVICE, Constants.BACKAGE_STARTUP })
public class MainConfig {

	private Environment env = Springs.getEnvironment("classpath:${spring.profiles.active}/config.properties");

	@Autowired
	ApplicationContext ac;

	@Bean
	public DataSource dataSource() {

		String jndiName = env.getProperty("db.jndi_name");
		if (!Lang.isEmpty(jndiName)) {

			JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
			try {
				dsLookup.setResourceRef(false);
				return dsLookup.getDataSource(jndiName);
			} catch (Exception e) {
				dsLookup.setResourceRef(true);
				return dsLookup.getDataSource(jndiName);
			}
		}
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setUrl(env.getProperty("db.url"));
		dataSource.setUsername(env.getProperty("db.username"));
		dataSource.setPassword(env.getProperty("db.password"));
		dataSource.setTestWhileIdle(false);
		return new DataSourceSpy(dataSource);
	}

	@Bean
	public DataSource jpaProxyDataSource() {
		return new JpaProxyDataSource();
	}

	@Bean
	@Autowired
	public StartupListener startupListenerDev() {
		StartupListener startupListener = new StartupListener();
		startupListener.setBasePackages(Constants.BACKAGE_STARTUP);
		return startupListener;
	}

	@Bean
	@Autowired
	public JpaCommentListener jpaCommentListener() {
		JpaCommentListener jpaCommentListener = new JpaCommentListener();
		jpaCommentListener.setEntityManagerFactory(ac.getBean(EntityManagerFactory.class));
		jpaCommentListener.setActiveProfiles("dev");
		return jpaCommentListener;
	}

	@Bean
	@Autowired
	public PlatformTransactionManager transactionManager() throws ClassNotFoundException {
		return new JpaTransactionManager(entityManagerFactory().getObject());
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws ClassNotFoundException {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

		entityManagerFactoryBean.setDataSource(dataSource());
		entityManagerFactoryBean.setPackagesToScan(Constants.BACKAGE_SCHEMA);
		entityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);

		Properties jpaProperties = new Properties();

		// SQL generation / debugging

		jpaProperties.put(AvailableSettings.FORMAT_SQL, false);
		jpaProperties.put(AvailableSettings.SHOW_SQL, false);
		jpaProperties.put(AvailableSettings.HBM2DDL_AUTO, env.getProperty(AvailableSettings.HBM2DDL_AUTO));
		jpaProperties.put(AvailableSettings.GENERATE_STATISTICS, true);

		// Second level caching
		jpaProperties.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, true);
		jpaProperties.put(AvailableSettings.USE_QUERY_CACHE, env.getProperty(AvailableSettings.USE_QUERY_CACHE));
		jpaProperties.put(AvailableSettings.USE_STRUCTURED_CACHE, true);

		// for redis cache implement
		// jpaProperties.put(AvailableSettings.CACHE_REGION_FACTORY,
		// SingletonRedisRegionFactory.class.getName());
		// jpaProperties.put(AvailableSettings.CACHE_REGION_PREFIX,
		// "hibernate-redis");
		// jpaProperties.put(AvailableSettings.USE_STRUCTURED_CACHE, true);
		// jpaProperties.put(AvailableSettings.CACHE_PROVIDER_CONFIG,
		// env.getActiveProfiles()[0] + "/cache/hibernate-redis.properties");

		// for ehcache implement
		jpaProperties.put(AvailableSettings.CACHE_REGION_FACTORY, EhCacheRegionFactory.class.getName());
		jpaProperties.put(AvailableSettings.CACHE_REGION_PREFIX, "hibernate-ehcache");

		jpaProperties.put("net.sf.ehcache.configurationResourceName",
				env.getActiveProfiles()[0] + "/cache/hibernate-ehcache.xml");
		//
		// We
		// only
		// have
		// one
		// active
		// profile
		jpaProperties.put(AvailableSettings.DIALECT, env.getProperty(AvailableSettings.DIALECT));
		jpaProperties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY,
				ImplicitNamingStrategyCompliantImpl.class.getCanonicalName());
		entityManagerFactoryBean.setJpaProperties(jpaProperties);
		return entityManagerFactoryBean;
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		String basePackage = Constants.BACKAGE_DAO;
		PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + basePackage.replace('.', '/')
				+ "/" + "**/*.xml";
		sessionFactory.setMapperLocations(pathMatchingResourcePatternResolver.getResources(packageSearchPath));
		// 配置分页信息
		PageHelper pageHelper = new PageHelper();
		Properties pageHelperProperties = new Properties();
		pageHelperProperties.put("dialect", "mysql");
		pageHelperProperties.put("offsetAsPageNum", true);
		pageHelperProperties.put("rowBoundsWithCount", true);
		pageHelperProperties.put("pageSizeZero", true);
		pageHelperProperties.put("reasonable", true);
		pageHelper.setProperties(pageHelperProperties);
		sessionFactory.setPlugins(new Interceptor[] { pageHelper });
		sessionFactory.setDataSource(jpaProxyDataSource());
		return sessionFactory.getObject();
	}

	@Bean
	public FilePool filePool() {
		return new FilePool(env.getProperty("filepool.path"));
	}
}
