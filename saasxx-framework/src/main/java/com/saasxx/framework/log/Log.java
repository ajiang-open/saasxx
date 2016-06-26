package com.saasxx.framework.log;

import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import com.saasxx.framework.Lang;

/**
 * 包装日志器
 * 
 * @author lujijiang
 * 
 */
public class Log {

	/**
	 * 定位常量
	 */
	private static final String FQCN = Log.class.getCanonicalName();

	static ILoggerFactory iLoggerFactory;

	/**
	 * 日志接口
	 */
	LocationAwareLogger logger;

	/**
	 * 调用者
	 */
	String name;

	Log(String name) {
		this.name = name;
		try {
			logger = (LocationAwareLogger) LoggerFactory.getLogger(name);
		} catch (Exception e) {
			if (iLoggerFactory == null) {
				iLoggerFactory = new Log4jLoggerFactory();
			}
			logger = (LocationAwareLogger) iLoggerFactory.getLogger(name);
		}
	}

	public String toString() {
		return "Log [name=" + name + "]";
	}

	/**
	 * 获得日志名
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 是否是trace级别
	 * 
	 * @return
	 */
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	/**
	 * 打印Trace级别信息
	 * 
	 * @param e
	 *            异常对象
	 * @param msg
	 *            消息体
	 * @param args
	 *            消息参数，结合消息体中的{}占位符
	 */
	public void trace(Exception e, Object message, Object... args) {
		if (isTraceEnabled()) {
			String msg = message == null ? "null" : message.toString();
			logger.log(null, FQCN, LocationAwareLogger.TRACE_INT, msg, args, e);
		}
	}

	/**
	 * 打印Trace级别信息
	 * 
	 * @param message
	 *            消息体
	 * @param args
	 *            消息参数，结合消息体中的{}占位符
	 */
	public void trace(Object message, Object... args) {
		trace(null, message, args);
	}

	/**
	 * 打印Trace级别信息
	 * 
	 * @param message
	 *            消息体
	 */
	public void trace(Object message) {
		trace(message, Lang.EMPTY_ARRAY);
	}

	/**
	 * 是否是debug级别
	 * 
	 * @return
	 */
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	/**
	 * 打印Debug级别信息
	 * 
	 * @param e
	 *            异常对象
	 * @param msg
	 *            消息体
	 * @param args
	 *            消息参数，结合消息体中的{}占位符
	 */
	public void debug(Exception e, Object message, Object... args) {
		if (isDebugEnabled()) {
			String msg = message == null ? "null" : message.toString();
			logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, args, e);
		}
	}

	/**
	 * 打印Debug级别信息
	 * 
	 * @param message
	 *            消息体
	 * @param args
	 *            消息参数，结合消息体中的{}占位符
	 */
	public void debug(Object message, Object... args) {
		debug(null, message, args);
	}

	/**
	 * 打印Debug级别信息
	 * 
	 * @param message
	 *            消息体
	 */
	public void debug(Object message) {
		debug(message, Lang.EMPTY_ARRAY);
	}

	/**
	 * 是否是info级别
	 * 
	 * @return
	 */
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	/**
	 * 打印Info级别信息
	 * 
	 * @param e
	 *            异常对象
	 * @param msg
	 *            消息体
	 * @param args
	 *            消息参数，结合消息体中的{}占位符
	 */
	public void info(Exception e, Object message, Object... args) {
		if (isInfoEnabled()) {
			String msg = message == null ? "null" : message.toString();
			logger.log(null, FQCN, LocationAwareLogger.INFO_INT, msg, args, e);
		}
	}

	/**
	 * 打印Info级别信息
	 * 
	 * @param message
	 *            消息体
	 * @param args
	 *            消息参数，结合消息体中的{}占位符
	 */
	public void info(Object message, Object... args) {
		info(null, message, args);
	}

	/**
	 * 打印Info级别信息
	 * 
	 * @param message
	 *            消息体
	 */
	public void info(Object message) {
		info(message, Lang.EMPTY_ARRAY);
	}

	/**
	 * 是否是warn级别
	 * 
	 * @return
	 */
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	/**
	 * 打印Warn级别信息
	 * 
	 * @param e
	 *            异常对象
	 * @param msg
	 *            消息体
	 * @param args
	 *            消息参数，结合消息体中的{}占位符
	 */
	public void warn(Exception e, Object message, Object... args) {
		if (isWarnEnabled()) {
			String msg = message == null ? "null" : message.toString();
			logger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, args, e);
		}
	}

	/**
	 * 打印Warn级别信息
	 * 
	 * @param message
	 *            消息体
	 * @param args
	 *            消息参数，结合消息体中的{}占位符
	 */
	public void warn(Object message, Object... args) {
		warn(null, message, args);
	}

	/**
	 * 打印Warn级别信息
	 * 
	 * @param message
	 *            消息体
	 */
	public void warn(Object message) {
		warn(message, Lang.EMPTY_ARRAY);
	}

	/**
	 * 是否是error级别
	 * 
	 * @return
	 */
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	/**
	 * 打印Error级别信息
	 * 
	 * @param e
	 *            异常对象
	 * @param msg
	 *            消息体
	 * @param args
	 *            消息参数，结合消息体中的{}占位符
	 */
	public void error(Exception e, Object message, Object... args) {
		if (isErrorEnabled()) {
			String msg = message == null ? "null" : message.toString();
			logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, msg, args, e);
		}
	}

	/**
	 * 打印Error级别信息
	 * 
	 * @param message
	 *            消息体
	 * @param args
	 *            消息参数，结合消息体中的{}占位符
	 */
	public void error(Object message, Object... args) {
		error(null, message, args);
	}

	/**
	 * 打印Error级别信息
	 * 
	 * @param message
	 *            消息体
	 */
	public void error(Object message) {
		error(message, Lang.EMPTY_ARRAY);
	}

}
