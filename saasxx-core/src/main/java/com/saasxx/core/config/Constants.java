package com.saasxx.core.config;

public class Constants {
	/** 以下是包扫描路径常量 **/
	final static String BACKAGE_SCHEMA = "com.saasxx.**.schema";
	final static String BACKAGE_DAO = "com.saasxx.**.dao";
	final static String BACKAGE_SERVICE = "com.saasxx.**.service";
	final static String BACKAGE_STARTUP = "com.saasxx.**.startup";
	final static String BACKAGE_WEB = "com.saasxx.**.web";
	/*
	 * 验证码
	 */
	public static final long VERIFICATION_CODE_EXPIRES = 30 * 60 * 1000; // 验证码超时时间(30m)
	public static final long SEND_VERIFICATION_CODE_INTERVAL = 60 * 1000; // 发送验证码时间间隔(60s)
}
