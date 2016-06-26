package com.saasxx.framework.lang;

/**
 * 回调接口
 * 
 * @author lujijiang
 * 
 * @param <T>
 */
public interface Callback<T> {
	T execute() throws Exception;
}
