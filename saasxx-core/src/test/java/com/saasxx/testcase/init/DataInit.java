package com.saasxx.testcase.init;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import com.saasxx.core.config.MainConfig;
import com.saasxx.core.config.SecurityConfig;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;
import com.saasxx.framework.test.JUnitInitializer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { MainConfig.class,
		SecurityConfig.class }, initializers = JUnitInitializer.class)
@ActiveProfiles("dev")
@Transactional
public class DataInit {

	static Log log = Logs.getLog();

	@PersistenceContext
	EntityManager em;

	@Test
	@Rollback(false)
	public void initUser() {

	}
}
