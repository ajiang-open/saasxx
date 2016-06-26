package com.saasxx.framework.security.shiro;

import org.apache.shiro.SecurityUtils;

import com.saasxx.framework.Lang;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

/**
 * Shiro工具类
 * 
 * @author lujijiang
 *
 */
public class Shiros {

	static Log log = Logs.getLog();

	/**
	 * 获取的当前用户
	 * 
	 * @return
	 */
	public static ShiroUser currentUser() {
		try {
			ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal();
			return shiroUser;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 检查是否拥有某些角色
	 * 
	 * @param roles
	 * @return
	 */
	public static boolean hasRoles(String... roles) {
		try {
			return SecurityUtils.getSubject().hasAllRoles(Lang.newSet(roles));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 检查是否拥有某些权限
	 * 
	 * @param permissions
	 * @return
	 */
	public static boolean hasPermissions(String... permissions) {
		try {
			return SecurityUtils.getSubject().isPermittedAll(permissions);
		} catch (Exception e) {
			return false;
		}
	}
}
