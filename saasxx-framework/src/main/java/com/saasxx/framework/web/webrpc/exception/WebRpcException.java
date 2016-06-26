package com.saasxx.framework.web.webrpc.exception;

public class WebRpcException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1704773559463924335L;

	public WebRpcException(String format, Object... args) {
		super(String.format(format, args));
	}

}
