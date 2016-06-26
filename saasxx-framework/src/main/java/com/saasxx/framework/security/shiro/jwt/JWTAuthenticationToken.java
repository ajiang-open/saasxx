package com.saasxx.framework.security.shiro.jwt;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 获取认证Token
 * 
 * @author lujijiang
 *
 */
public class JWTAuthenticationToken implements AuthenticationToken {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3146622082049689260L;

	private String username;
	private String token;

	public JWTAuthenticationToken(String username, String token) {
		this.username = username;
		this.token = token;
	}

	@Override
	public Object getPrincipal() {
		return getUsername();
	}

	@Override
	public Object getCredentials() {
		return getToken();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
