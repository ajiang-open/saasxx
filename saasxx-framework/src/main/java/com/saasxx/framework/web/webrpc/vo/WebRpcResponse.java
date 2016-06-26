package com.saasxx.framework.web.webrpc.vo;

import java.io.Serializable;

/**
 * WebRpc响应对象
 * 
 * @author lujijiang
 *
 */
public class WebRpcResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8983511258225932890L;
	/**
	 * 回调ID
	 */
	String id;
	/**
	 * 执行值
	 */
	Object result;
	/**
	 * 异常类型
	 */
	String errorType;
	/**
	 * 执行异常
	 */
	String error;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", result=" + result
				+ ", errorType=" + errorType + ", error=" + error + "]";
	}

}
