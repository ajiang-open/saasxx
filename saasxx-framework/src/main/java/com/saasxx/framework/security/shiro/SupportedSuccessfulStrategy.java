package com.saasxx.framework.security.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.pam.AbstractAuthenticationStrategy;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 所有支持的Realm都需要返回正确的对象
 * 
 * @author lujijiang
 *
 */
public class SupportedSuccessfulStrategy extends AbstractAuthenticationStrategy {

	private static final Logger log = LoggerFactory.getLogger(SupportedSuccessfulStrategy.class);

	public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo info,
			AuthenticationInfo aggregate, Throwable t) throws AuthenticationException {
		if (t != null) {
			if (t instanceof AuthenticationException) {
				// propagate:
				throw ((AuthenticationException) t);
			} else {
				String msg = "Unable to acquire account data from realm [" + realm + "].  The [" + getClass().getName()
						+ " implementation requires supported configured realm(s) to operate successfully "
						+ "for a successful authentication.";
				throw new AuthenticationException(msg, t);
			}
		}
		if (info == null) {
			String msg = "Realm [" + realm + "] could not find any associated account data for the submitted "
					+ "AuthenticationToken [" + token + "].  The [" + getClass().getName()
					+ "] implementation requires "
					+ "supported configured realm(s) to acquire valid account data for a submitted token during the "
					+ "log-in process.";
			throw new UnknownAccountException(msg);
		}

		log.debug("Account successfully authenticated using realm [{}]", realm);

		// If non-null account is returned, then the realm was able to
		// authenticate the
		// user - so merge the account with any accumulated before:
		merge(info, aggregate);

		return aggregate;
	}
}
