package com.saasxx.framework.test;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class JUnitInitializer implements
		ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		System.setProperty("spring.profiles.active", StringUtils.join(
				applicationContext.getEnvironment().getActiveProfiles(), ","));
	}

}
