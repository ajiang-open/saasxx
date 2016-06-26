package com.saasxx.framework.dao.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为ORM映射的实体及其字段添加数据库注释
 * 
 * @author lujijiang
 * 
 */
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Comment {
	/**
	 * 字段名
	 * 
	 * @return
	 */
	String value();

	/**
	 * 字段详细描述
	 * 
	 * @return
	 */
	String description() default "";
}
