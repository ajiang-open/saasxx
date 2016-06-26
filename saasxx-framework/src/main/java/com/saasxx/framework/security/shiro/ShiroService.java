package com.saasxx.framework.security.shiro;

import java.util.Collection;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * Shiro服务，结合SSO等使用，需要实现其中的方法
 * 
 * @author lujijiang
 *
 */
public interface ShiroService {
	/**
	 * 根据令牌定位用户
	 * 
	 * @param token
	 * @return
	 */
	ShiroUser findUser(AuthenticationToken token);

	/**
	 * 根据用户信息获取用户
	 * 
	 * @param username
	 * @param attributes
	 * @return
	 */
	ShiroUser findUser(String username, Map<String, Object> attributes);

	/**
	 * 根据Principal取得包含的角色信息
	 * 
	 * @param principal
	 * @return
	 */
	Collection<String> findRoles(ShiroUser shiroUser);

	/**
	 * 根据Principal取得包含的权限信息
	 * 
	 * @param principal
	 * @return
	 */
	Collection<String> findPermissions(ShiroUser shiroUser);

}
