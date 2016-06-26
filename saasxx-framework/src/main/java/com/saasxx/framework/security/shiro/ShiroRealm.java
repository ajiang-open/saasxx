package com.saasxx.framework.security.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.saasxx.framework.Lang;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

/**
 * Shiro处理对象
 * 
 * @author lujijiang
 *
 */
public class ShiroRealm extends AuthorizingRealm implements InitializingBean {

	private static Log log = Logs.getLog();

	protected ShiroService shiroService;

	public void setShiroService(ShiroService shiroService) {
		this.shiroService = shiroService;
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof UsernamePasswordToken;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		try {
			ShiroUser shiroUser = shiroService.findUser(token);
			if (shiroUser != null) {
				return new SimpleAuthenticationInfo(shiroUser, token.getCredentials(), shiroUser.getUsername());
			} else {
				throw new UnknownAccountException("Could not authenticate with given credentials");
			}
		} catch (Exception e) {
			log.error(e, "Could not authenticate with given credentials");
			throw new UnknownAccountException(Lang.getCause(e).getMessage());
		}

	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		info.addStringPermissions(shiroService.findPermissions(shiroUser));
		info.addRoles(shiroService.findRoles(shiroUser));
		return info;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(shiroService, "The shiroService should not be null");
	}

}
