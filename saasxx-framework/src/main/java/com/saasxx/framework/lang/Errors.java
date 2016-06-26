package com.saasxx.framework.lang;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import com.saasxx.framework.Lang;

/**
 * 错误处理器，支持国际化
 * 
 * @author lujijiang
 *
 */
public class Errors {
	/**
	 * 资源绑定器缓存
	 */
	private final static Map<Locale, ResourceBundle> RESOURCEBUNDLE_MAP = new ConcurrentHashMap<Locale, ResourceBundle>();

	/**
	 * 当前语言区域设置
	 */
	private final static ThreadLocal<Locale> CURRENT_LOCALE_LOCAL = new ThreadLocal<Locale>();

	/**
	 * 设置当前线程中的默认语言区域
	 * 
	 * @return
	 */
	public static void setDefaultLocale(Locale locale) {
		CURRENT_LOCALE_LOCAL.set(locale);
	}
	/**
	 * 获取默认的语言区域
	 * 
	 * @return
	 */
	private static Locale getDefaultLocale() {
		Locale locale = CURRENT_LOCALE_LOCAL.get();
		if (locale != null) {
			return locale;
		}
		return Locale.getDefault();
	}
	/**
	 * 判断一个变量是否为真，如果为假，则抛出异常消息
	 * 
	 * @param flag
	 *            断言变量
	 * @param locale
	 *            当前语言环境
	 * @param message
	 *            消息体，如果消息是以:开头，则从errors.properties（或者其它语言版本）的资源文件中找到对应的消息体（可带{}
	 *            格式化）
	 * @param args
	 *            消息体格式化参数
	 */
	public static void isTrue(boolean flag, Locale locale, String message,
			Object... args) {
		if (!flag) {
			if (message.startsWith(":")) {
				ResourceBundle bundle = RESOURCEBUNDLE_MAP.get(locale);
				if (bundle == null) {
					bundle = ResourceBundle.getBundle("errors", locale);
					RESOURCEBUNDLE_MAP.put(locale, bundle);
				}
				message = bundle.getString(message);
			}
			throw new IllegalStateException(MessageFormat.format(message,
					args));
		}
	}

	/**
	 * 判断一个对象是否为null，不为null则抛出异常
	 * 
	 * @param object
	 *            判断对象
	 * @param locale
	 *            当前语言环境
	 * @param message
	 *            消息体，如果消息是以:开头，则从errors.properties（或者其它语言版本）的资源文件中找到对应的消息体（可带{}
	 *            格式化）
	 * @param args
	 *            消息体格式化参数
	 */
	public static void isNull(Object object, Locale locale, String message,
			Object... args) {
		isTrue(object == null, locale, message, args);
	}

	/**
	 * 判断一个对象是否不为null，为null则抛出异常
	 * 
	 * @param object
	 *            判断对象
	 * @param locale
	 *            当前语言环境
	 * @param message
	 *            消息体，如果消息是以:开头，则从errors.properties（或者其它语言版本）的资源文件中找到对应的消息体（可带{}
	 *            格式化）
	 * @param args
	 *            消息体格式化参数
	 */
	public static void notNull(Object object, Locale locale, String message,
			Object... args) {
		isTrue(object != null, locale, message, args);
	}

	/**
	 * 判断一个对象是否为空，不为空则抛出异常
	 * 
	 * @param object
	 *            判断对象
	 * @param locale
	 *            当前语言环境
	 * @param message
	 *            消息体，如果消息是以:开头，则从errors.properties（或者其它语言版本）的资源文件中找到对应的消息体（可带{}
	 *            格式化）
	 * @param args
	 *            消息体格式化参数
	 */
	public static void isEmpty(Object object, Locale locale, String message,
			Object... args) {
		isTrue(Lang.isEmpty(object), locale, message, args);
	}

	/**
	 * 判断一个对象是否不为空，为空则抛出异常
	 * 
	 * @param object
	 *            判断对象
	 * @param locale
	 *            当前语言环境
	 * @param message
	 *            消息体，如果消息是以:开头，则从errors.properties（或者其它语言版本）的资源文件中找到对应的消息体（可带{}
	 *            格式化）
	 * @param args
	 *            消息体格式化参数
	 */
	public static void notEmpty(Object object, Locale locale, String message,
			Object... args) {
		isTrue(!Lang.isEmpty(object), locale, message, args);
	}

	/**
	 * 判断一个变量是否为真，如果为假，则抛出异常消息
	 * 
	 * @param flag
	 *            断言变量
	 * @param message
	 *            消息体，如果消息是以:开头，则从errors.properties（或者其它语言版本）的资源文件中找到对应的消息体（可带{}
	 *            格式化）
	 * @param args
	 *            消息体格式化参数
	 */
	public static void isTrue(boolean flag, String message, Object... args) {
		isTrue(flag, getDefaultLocale(), message, args);
	}

	/**
	 * 判断一个对象是否为null，不为null则抛出异常
	 * 
	 * @param object
	 *            判断对象
	 * @param message
	 *            消息体，如果消息是以:开头，则从errors.properties（或者其它语言版本）的资源文件中找到对应的消息体（可带{}
	 *            格式化）
	 * @param args
	 *            消息体格式化参数
	 */
	public static void isNull(Object object, String message,
			Object... args) {
		isNull(object, getDefaultLocale(), message, args);
	}

	/**
	 * 判断一个对象是否不为null，为null则抛出异常
	 * 
	 * @param object
	 *            判断对象
	 * @param message
	 *            消息体，如果消息是以:开头，则从errors.properties（或者其它语言版本）的资源文件中找到对应的消息体（可带{}
	 *            格式化）
	 * @param args
	 *            消息体格式化参数
	 */
	public static void notNull(Object object, String message,
			Object... args) {
		notNull(object, getDefaultLocale(), message, args);
	}

	/**
	 * 判断一个对象是否为空，不为空则抛出异常
	 * 
	 * @param object
	 *            判断对象
	 * @param message
	 *            消息体，如果消息是以:开头，则从errors.properties（或者其它语言版本）的资源文件中找到对应的消息体（可带{}
	 *            格式化）
	 * @param args
	 *            消息体格式化参数
	 */
	public static void isEmpty(Object object, String message,
			Object... args) {
		isEmpty(object, getDefaultLocale(), message, args);
	}


	/**
	 * 判断一个对象是否不为空，为空则抛出异常
	 * 
	 * @param object
	 *            判断对象
	 * @param message
	 *            消息体，如果消息是以:开头，则从errors.properties（或者其它语言版本）的资源文件中找到对应的消息体（可带{}
	 *            格式化）
	 * @param args
	 *            消息体格式化参数
	 */
	public static void notEmpty(Object object, String message,
			Object... args) {
		notEmpty(object, getDefaultLocale(), message, args);
	}

}
