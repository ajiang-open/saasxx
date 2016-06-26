package com.saasxx.framework.security.shiro;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Shiro用户信息
 * 
 * @author lujijiang
 *
 */
public abstract class ShiroUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1744357610516928058L;
	/**
	 * 用户名
	 */
	String username;
	/**
	 * 用户属性
	 */
	Map<String, Object> attributes = new HashMap<String, Object>();

	public ShiroUser() {

	}

	public ShiroUser(String username, Map<String, Object> attributes) {
		this.username = username;
		this.attributes = attributes;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public abstract boolean isSuperAdmin();

	@Override
	public String toString() {
		return "ShiroUser [username=" + username + ", attributes=" + attributes + "]";
	}

}
