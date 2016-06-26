package com.saasxx.framework.startup;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启动器，需事先配置好StartUpListener
 * 
 * @author lujijiang
 * 
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Startup {
	/**
	 * 优先级大小，越大则越先执行。
	 * 
	 * @return
	 */
	int priority() default 0;

	/**
	 * 是否忽略异常，允许系统继续允许。如果为false，则当发生异常时，系统将停止执行
	 * 
	 * @return
	 */
	boolean ignoreError() default false;

	/**
	 * 是否作为守护线程执行，如果是守护线程，则执行成功与否无法干扰应用启动，且执行时不会阻断应用的启动
	 * 
	 * @return
	 */
	boolean daemon() default false;

	/**
	 * 所属的Spring环境
	 * 
	 * @return
	 */
	String[] activeProfiles() default {};
}