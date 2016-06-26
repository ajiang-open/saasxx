package com.saasxx.framework.log;

/**
 * 日志工具类
 * 
 * @author lujijiang
 * 
 */
public class Logs {

	/**
	 * 获取指定名字的日志
	 * 
	 * @param name
	 * @return
	 */
	public static Log getLog(String name) {
		return new Log(name);
	}

	/**
	 * 获取当前类型名的日志
	 * 
	 * @return
	 */
	public static Log getLog() {
		String name = Thread.currentThread().getStackTrace()[2].getClassName();
		return getLog(name);
	}
}
