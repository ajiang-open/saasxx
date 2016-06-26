package com.saasxx.framework.security.shiro.jwt;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.saasxx.framework.Lang;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;
import com.saasxx.framework.security.shiro.ShiroUser;
import com.saasxx.framework.security.shiro.Shiros;

/**
 * 专用于JWT认证的Shiro过滤器
 * 
 * @author lujijiang
 *
 */
public class JWTAuthenticationFilter extends AuthenticatingFilter {

	private static Log log = Logs.getLog();

	private static final String AUTHORIZATION_BEARER = "Bearer ";

	private static final String AUTHORIZATION_HEADER = "Authorization";

	private static final String AUTHENTICATE_HEADER = "WWW-Authenticate";

	private static final String AUTHORIZATION_PARAMETER = "$Authorization$";

	@Autowired
	private JWTShiroRealm jwtShiroRealm;

	private String authzScheme = AUTHORIZATION_BEARER;

	private String authcScheme = AUTHORIZATION_BEARER;

	private String applicationName = "application";

	public String getAuthzScheme() {
		return authzScheme;
	}

	public void setAuthzScheme(String authzScheme) {
		this.authzScheme = authzScheme;
	}

	public String getAuthcScheme() {
		return authcScheme;
	}

	public void setAuthcScheme(String authcScheme) {
		this.authcScheme = authcScheme;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
		String authenticationToken = getAuthorizationToken(request);
		if (authenticationToken != null) {
			return createToken(authenticationToken);
		}
		return new JWTAuthenticationToken(null, null);
	}

	private String getAuthorizationToken(ServletRequest request) {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String authorization = httpServletRequest.getHeader(AUTHORIZATION_HEADER);
		if (authorization == null) {
			authorization = httpServletRequest.getParameter(AUTHORIZATION_PARAMETER);
		}
		if (authorization != null && authorization.startsWith(AUTHORIZATION_BEARER)) {
			return authorization.substring(AUTHORIZATION_BEARER.length());
		}
		return null;
	}

	private AuthenticationToken createToken(String jwt) {
		int point = jwt.indexOf(".");
		return new JWTAuthenticationToken(jwt.substring(0, point), jwt.substring(point + 1));
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
		if ("OPTIONS".equalsIgnoreCase(httpServletRequest.getMethod())) {
			return true;
		}
		if (isLoginAttempt(request, response)) {
			executeLogin(request, response);
			return true;
		}
		sendChallenge(request, response);
		return false;
	}

	@Override
	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request,
			ServletResponse response) throws Exception {
		HttpServletResponse httpResponse = WebUtils.toHttp(response);
		ShiroUser shiroUser = (ShiroUser) subject.getPrincipals().getPrimaryPrincipal();
		if (shiroUser != null) {
			String jwt = jwtShiroRealm.updateJWT(Shiros.currentUser().getUsername(),
					Shiros.currentUser().getAttributes());
			String authenticationToken = getAuthorizationToken(request);
			if (!Lang.equals(jwt, authenticationToken)) {
				httpResponse.setHeader(AUTHENTICATE_HEADER, jwt);
			}
		}
		return super.onLoginSuccess(token, subject, request, response);
	}

	protected boolean sendChallenge(ServletRequest request, ServletResponse response) {
		if (log.isDebugEnabled()) {
			log.debug("Authentication required: sending 401 Authentication challenge response.");
		}
		HttpServletResponse httpResponse = WebUtils.toHttp(response);
		httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		String authcHeader = getAuthcScheme() + " realm=\"" + getApplicationName() + "\"";
		httpResponse.setHeader(AUTHENTICATE_HEADER, authcHeader);
		return false;
	}

	protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
		String authzHeader = getAuthzHeader(request);
		return authzHeader != null && isLoginAttempt(authzHeader);
	}

	protected String getAuthzHeader(ServletRequest request) {
		HttpServletRequest httpRequest = WebUtils.toHttp(request);
		return httpRequest.getHeader(AUTHORIZATION_HEADER);
	}

	protected boolean isLoginAttempt(String authzHeader) {
		// SHIRO-415: use English Locale:
		String authzScheme = getAuthzScheme().toLowerCase(Locale.ENGLISH);
		return authzHeader.toLowerCase(Locale.ENGLISH).startsWith(authzScheme);
	}

}
