package com.saasxx.framework.dao.orm.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.Assert;

import com.saasxx.framework.dao.orm.hibernate.Hibernates;

/**
 * 用来处理Jpa的注释的
 * 
 * @author lujijiang
 * 
 */
@SuppressWarnings("rawtypes")
public class JpaCommentListener implements ApplicationListener,
		InitializingBean {

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * JPA工厂
	 */
	private EntityManagerFactory entityManagerFactory;

	/**
	 * 哪些环境要应用该监听器
	 */
	private String activeProfiles;

	public void setEntityManagerFactory(
			EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	public void setActiveProfiles(String activeProfiles) {
		this.activeProfiles = activeProfiles;
	}

	/**
	 * 是否已初始化过
	 */
	private boolean isinitialized;

	public void onApplicationEvent(ApplicationEvent event) {
		if (activeProfiles != null) {
			if (!applicationContext.getEnvironment().acceptsProfiles(
					activeProfiles.split("[,，\\s]+"))) {
				return;
			}
		}
		if (event instanceof ContextRefreshedEvent && !isinitialized) {
			isinitialized = true;
			initializeStartup();
		}
	}

	/**
	 * 初始化处理方法
	 */
	private void initializeStartup() {
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();
		try {
			if (Hibernates.isHibernate(entityManager)) {
				Hibernates.writeComments(entityManagerFactory
						.unwrap(SessionFactory.class));
			}
		} finally {
			entityManager.close();
		}
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(entityManagerFactory,
				"The entityManagerFactory should be null");
	}

}
