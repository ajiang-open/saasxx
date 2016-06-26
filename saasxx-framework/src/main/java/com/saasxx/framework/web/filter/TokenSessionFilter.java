package com.saasxx.framework.web.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.saasxx.framework.Lang;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

/**
 * 基于Token的Session
 * 
 * @author lujijiang
 *
 */
public class TokenSessionFilter implements Filter {

	private static Log log = Logs.getLog();

	public class TokenSession implements HttpSession {

		Map<String, Object> attributeMap = new HashMap<String, Object>();

		long creationTime;

		String id;

		long lastAccessedTime;

		int maxInactiveInterval;

		boolean invalidate;

		boolean newSession;

		@Override
		public long getCreationTime() {
			return creationTime;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public long getLastAccessedTime() {
			return lastAccessedTime;
		}

		@Override
		public ServletContext getServletContext() {
			return null;
		}

		@Override
		public void setMaxInactiveInterval(int interval) {
			this.maxInactiveInterval = interval;
		}

		@Override
		public int getMaxInactiveInterval() {
			return maxInactiveInterval;
		}

		@Override
		public HttpSessionContext getSessionContext() {
			throw new UnsupportedOperationException();
		}

		private boolean isInvalid() {
			boolean invalid = invalidate;
			if (!invalid) {
				long maxInactiveInterval = getMaxInactiveInterval();
				if (maxInactiveInterval != -1) {
					if ((System.currentTimeMillis() - getCreationTime()) / 1000 > getMaxInactiveInterval()) {
						invalidate();
						invalid = true;
					}
				}
			}
			this.newSession = false;
			this.lastAccessedTime = System.currentTimeMillis();
			return invalid;
		}

		@Override
		public Object getAttribute(String name) {
			boolean invalid = isInvalid();
			if (invalid) {
				return null;
			}
			return attributeMap.get(name);
		}

		@Override
		public Object getValue(String name) {
			return getAttribute(name);
		}

		@Override
		public Enumeration<String> getAttributeNames() {

			boolean invalid = isInvalid();

			if (invalid) {
				return null;
			}

			final Set<String> nameSet = attributeMap.keySet();

			final Iterator<String> nameIterator = nameSet.iterator();

			return new Enumeration<String>() {

				@Override
				public boolean hasMoreElements() {
					return nameIterator.hasNext();
				}

				@Override
				public String nextElement() {
					return nameIterator.next();
				}
			};
		}

		@Override
		public String[] getValueNames() {

			boolean invalid = isInvalid();

			if (invalid) {
				return null;
			}

			return attributeMap.keySet().toArray(new String[0]);
		}

		@Override
		public void setAttribute(String name, Object value) {
			if (invalidate) {
				throw new IllegalStateException("This session is invalidated");
			}
			attributeMap.put(name, value);
		}

		@Override
		public void putValue(String name, Object value) {
			if (invalidate) {
				throw new IllegalStateException("This session is invalidated");
			}
			attributeMap.put(name, value);
		}

		@Override
		public void removeAttribute(String name) {
			if (invalidate) {
				throw new IllegalStateException("This session is invalidated");
			}
			attributeMap.remove(name);
		}

		@Override
		public void removeValue(String name) {
			if (invalidate) {
				throw new IllegalStateException("This session is invalidated");
			}
			attributeMap.remove(name);
		}

		@Override
		public void invalidate() {
			this.invalidate = true;
			this.newSession = false;
		}

		@Override
		public boolean isNew() {
			return newSession;
		}

	}

	private Map<String, TokenSession> tokenSessionMap = Lang
			.newLRUMap(1024 * 10);

	/**
	 * 会话超时时间（默认30分钟）
	 */
	private int maxInactiveInterval = 1800;

	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			handle((HttpServletRequest) request,
					(HttpServletResponse) response, chain);
			return;
		}
		chain.doFilter(request, response);
	}

	private void handle(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest wrapRequest = new HttpServletRequestWrapper(request) {
			@Override
			public HttpSession getSession(boolean create) {
				String authorization = request.getHeader("Authorization");
				if (authorization == null) {
					log.warn("The header Authorization is null");
					return null;
				}
				if (!authorization.startsWith("Bearer ")) {
					log.warn("The header Authorization is not a bearer token");
					return null;
				}
				String token = authorization.substring("Bearer ".length());
				TokenSession tokenSession = tokenSessionMap.get(token);
				if (tokenSession == null && create) {
					tokenSession = new TokenSession();
					tokenSession.maxInactiveInterval = maxInactiveInterval;
					tokenSession.creationTime = System.currentTimeMillis();
					tokenSession.id = token;
					tokenSession.lastAccessedTime = tokenSession.creationTime;
					tokenSession.newSession = true;
					tokenSessionMap.put(token, tokenSession);
					return tokenSession;
				}
				return tokenSession;
			}

			@Override
			public HttpSession getSession() {
				return getSession(true);
			}

		};
		chain.doFilter(wrapRequest, response);
	}

	@Override
	public void destroy() {

	}

}
