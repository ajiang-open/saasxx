package com.saasxx.framework.web.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Kryos;
import com.saasxx.framework.lang.OID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.SafeEncoder;

/**
 * 基于Redis实现Session共享的过滤器
 * 
 * @author lujijiang
 *
 */
public class RedisSessionFilter implements Filter {

	private static final String CONFIG_REDIS_EXPIRE_SECOND = "redisExpireSecond";

	private static final String CONFIG_SESSION_CAPACITY = "sessionCapacity";

	private static final String CONFIG_REDIS_TIMEOUT = "redisTimeout";

	private static final String CONFIG_REDIS_URI = "redisUri";

	private static final String CONFIG_SESSION_ID_COOKIE_SECURE = "sessionIdCookieSecure";

	private static final String CONFIG_SESSION_ID_COOKIE_MAX_AGE = "sessionIdCookieMaxAge";

	private static final String CONFIG_SESSION_ID_COOKIE_PATH = "sessionIdCookiePath";

	private static final String CONFIG_SESSION_ID_COOKIE_NAME = "sessionIdCookieName";

	private static final String DEFAULT_REDIS_SESSION_ID_NAME = "RSESSIONID";

	/**
	 * 值元对象
	 * 
	 * @author lujijiang
	 *
	 */
	static class ValueMeta {
		Class<?> type;
		Object value;

		public Class<?> getType() {
			return type;
		}

		public void setType(Class<?> type) {
			this.type = type;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}

	/**
	 * 共享会话对象
	 * 
	 * @author lujijiang
	 *
	 */
	static class RedisSession implements HttpSession {

		JedisPool jedisPool;

		String id;

		ServletContext servletContext;

		private long creationTime = Long.MIN_VALUE;

		private int maxInactiveInterval = Integer.MIN_VALUE;

		private int redisExpireSecond;

		public RedisSession(JedisPool jedisPool, ServletContext servletContext,
				String id, int redisExpireSecond) {
			this.jedisPool = jedisPool;
			this.servletContext = servletContext;
			this.id = id;
			this.redisExpireSecond = redisExpireSecond;
		}

		void setRemoteValue(String name, Object value) {
			ValueMeta valueMeta = new ValueMeta();
			valueMeta.type = value == null ? null : value.getClass();
			valueMeta.value = value;
			Jedis jedis = jedisPool.getResource();
			try {
				jedis.setex(SafeEncoder.encode(name), redisExpireSecond,
						Kryos.toBytes(valueMeta));
			} finally {
				jedis.close();
			}
		}

		Object getRemoteValue(String name) {
			Jedis jedis = jedisPool.getResource();
			try {
				byte[] value = jedis.get(SafeEncoder.encode(name));
				if (value == null || value.length == 0) {
					return null;
				}
				ValueMeta valueMeta = Kryos.fromBytes(value, ValueMeta.class);
				return valueMeta.getValue();
			} finally {
				jedis.close();
			}

		}

		@Override
		public long getCreationTime() {
			if (creationTime == Long.MIN_VALUE) {
				Long value = (Long) getRemoteValue(getId().concat(
						":creationTime"));
				creationTime = value == null ? Long.MIN_VALUE : value;
			}
			return creationTime;
		}

		public void setCreationTime(long time) {
			setRemoteValue(getId().concat(":creationTime"), time);
			creationTime = time;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public long getLastAccessedTime() {
			Long value = (Long) getRemoteValue(getId().concat(
					":lastAccessedTime"));
			return value == null ? -1 : value;
		}

		public void setLastAccessedTime(long time) {
			setRemoteValue(getId().concat(":lastAccessedTime"), time);
		}

		@Override
		public ServletContext getServletContext() {
			return servletContext;
		}

		@Override
		public void setMaxInactiveInterval(int interval) {
			setRemoteValue(getId().concat(":maxInactiveInterval"), interval);
			maxInactiveInterval = interval;
		}

		@Override
		public int getMaxInactiveInterval() {
			if (maxInactiveInterval == Integer.MIN_VALUE) {
				Integer value = (Integer) getRemoteValue(getId().concat(
						":maxInactiveInterval"));
				maxInactiveInterval = value == null ? Integer.MIN_VALUE : value;
			}
			return maxInactiveInterval;
		}

		@SuppressWarnings("deprecation")
		@Override
		public HttpSessionContext getSessionContext() {
			return new HttpSessionContext() {

				@Override
				public HttpSession getSession(String sessionId) {
					return RedisSession.this;
				}

				@Override
				public Enumeration<String> getIds() {
					return new Enumeration<String>() {
						Iterator<String> iterator = Lang.newSet(
								RedisSession.this.getId()).iterator();

						public boolean hasMoreElements() {
							return iterator.hasNext();
						}

						public String nextElement() {
							return iterator.next();
						}
					};
				}
			};
		}

		/**
		 * 生成属性的key字节数组
		 * 
		 * @param name
		 * @return
		 */
		private String getAttributeKey(String name) {
			StringBuilder keyBuilder = new StringBuilder();
			keyBuilder.append(getId());
			keyBuilder.append(":attrs:");
			keyBuilder.append(name);
			return keyBuilder.toString();
		}

		@Override
		public Object getAttribute(String name) {
			long maxInactiveInterval = getMaxInactiveInterval();
			if (maxInactiveInterval != -1) {
				if ((System.currentTimeMillis() - getCreationTime()) / 1000 > getMaxInactiveInterval()) {
					invalidate();
					return null;
				}
			}
			return getRemoteValue(getAttributeKey(name));
		}

		@Override
		public Object getValue(String name) {
			return getAttribute(name);
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			StringBuilder keyBuilder = new StringBuilder();
			keyBuilder.append(getId());
			keyBuilder.append(":attrs:*");
			Jedis jedis = jedisPool.getResource();
			try {
				final Iterator<String> iterator = jedis.keys(
						keyBuilder.toString()).iterator();

				return new Enumeration<String>() {

					@Override
					public boolean hasMoreElements() {
						return iterator.hasNext();
					}

					@Override
					public String nextElement() {
						return iterator.next();
					}
				};
			} finally {
				jedis.close();
			}

		}

		@Override
		public String[] getValueNames() {
			Set<String> nameSet = new LinkedHashSet<String>();
			Enumeration<String> enumeration = getAttributeNames();
			while (enumeration.hasMoreElements()) {
				String name = enumeration.nextElement();
				nameSet.add(name);
			}
			return nameSet.toArray(new String[0]);
		}

		@Override
		public void setAttribute(String name, Object value) {
			setRemoteValue(getAttributeKey(name), value);
		}

		@Override
		public void putValue(String name, Object value) {
			setAttribute(name, value);
		}

		@Override
		public void removeAttribute(String name) {
			Jedis jedis = jedisPool.getResource();
			try {
				jedis.del(getAttributeKey(name));
			} finally {
				jedis.close();
			}

		}

		@Override
		public void removeValue(String name) {
			removeAttribute(name);
		}

		@Override
		public void invalidate() {
			Jedis jedis = jedisPool.getResource();
			try {
				Enumeration<String> enumeration = getAttributeNames();
				while (enumeration.hasMoreElements()) {
					String name = enumeration.nextElement();
					jedis.del(name);
				}
			} finally {
				jedis.close();
			}
			long time = System.currentTimeMillis();
			setCreationTime(time);
			setLastAccessedTime(time);
		}

		@Override
		public boolean isNew() {
			return getCreationTime() == getLastAccessedTime();
		}

	}

	private String sessionIdCookieName = DEFAULT_REDIS_SESSION_ID_NAME;

	private String sessionIdCookiePath;

	private int sessionIdCookieMaxAge = -1;

	private boolean sessionIdCookieSecure;

	private ServletContext servletContext;

	private URI redisUri;

	private int redisTimeout = 120;

	private int redisExpireSecond = 3600 * 24;

	private JedisPool jedisPool;

	private int sessionCapacity = 1024 * 1024;

	private Map<String, RedisSession> sessionMap;

	public String getSessionIdCookieName() {
		return sessionIdCookieName;
	}

	public void setSessionIdCookieName(String sessionIdCookieName) {
		this.sessionIdCookieName = sessionIdCookieName;
	}

	public String getSessionIdCookiePath() {
		return sessionIdCookiePath;
	}

	public void setSessionIdCookiePath(String sessionIdCookiePath) {
		this.sessionIdCookiePath = sessionIdCookiePath;
	}

	public int getSessionIdCookieMaxAge() {
		return sessionIdCookieMaxAge;
	}

	public void setSessionIdCookieMaxAge(int sessionIdCookieMaxAge) {
		this.sessionIdCookieMaxAge = sessionIdCookieMaxAge;
	}

	public boolean isSessionIdCookieSecure() {
		return sessionIdCookieSecure;
	}

	public void setSessionIdCookieSecure(boolean sessionIdCookieSecure) {
		this.sessionIdCookieSecure = sessionIdCookieSecure;
	}

	public URI getRedisUri() {
		return redisUri;
	}

	public void setRedisUri(URI redisUri) {
		this.redisUri = redisUri;
	}

	public void setRedisUri(String redisUri) {
		try {
			this.redisUri = new URI(redisUri);
		} catch (URISyntaxException e) {
			throw Lang.unchecked(e);
		}
	}

	public int getRedisTimeout() {
		return redisTimeout;
	}

	public void setRedisTimeout(int redisTimeout) {
		this.redisTimeout = redisTimeout;
	}

	public int getRedisExpireSecond() {
		return redisExpireSecond;
	}

	public void setRedisExpireSecond(int redisExpireSecond) {
		this.redisExpireSecond = redisExpireSecond;
	}

	public int getSessionCapacity() {
		return sessionCapacity;
	}

	public void setSessionCapacity(int sessionCapacity) {
		this.sessionCapacity = sessionCapacity;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.servletContext = filterConfig.getServletContext();
		try {
			String sessionIdCookieName = filterConfig
					.getInitParameter(CONFIG_SESSION_ID_COOKIE_NAME);
			if (!Lang.isEmpty(sessionIdCookieName)) {
				this.sessionIdCookieName = sessionIdCookieName;
			}
			String sessionIdCookiePath = filterConfig
					.getInitParameter(CONFIG_SESSION_ID_COOKIE_PATH);
			if (!Lang.isEmpty(sessionIdCookiePath)) {
				this.sessionIdCookiePath = sessionIdCookiePath;
			}
			String sessionIdCookieMaxAge = filterConfig
					.getInitParameter(CONFIG_SESSION_ID_COOKIE_MAX_AGE);
			if (!Lang.isEmpty(sessionIdCookieMaxAge)) {
				this.sessionIdCookieMaxAge = Integer
						.valueOf(sessionIdCookieMaxAge);
			}
			String sessionIdCookieSecure = filterConfig
					.getInitParameter(CONFIG_SESSION_ID_COOKIE_SECURE);
			if (!Lang.isEmpty(sessionIdCookieSecure)) {
				this.sessionIdCookieSecure = Boolean
						.valueOf(sessionIdCookieSecure);
			}
			String redisUri = filterConfig.getInitParameter(CONFIG_REDIS_URI);
			if (!Lang.isEmpty(redisUri)) {
				this.redisUri = new URI(redisUri);
			}
			if (this.redisUri == null) {
				throw Lang
						.newException("The redisUri should not be null or empty");
			}
			String redisTimeout = filterConfig
					.getInitParameter(CONFIG_REDIS_TIMEOUT);
			if (!Lang.isEmpty(redisTimeout)) {
				this.redisTimeout = Integer.valueOf(redisTimeout);
			}
			jedisPool = new JedisPool(getRedisUri(), getRedisTimeout());
			String redisExpireSecond = filterConfig
					.getInitParameter(CONFIG_REDIS_EXPIRE_SECOND);
			if (!Lang.isEmpty(redisExpireSecond)) {
				this.redisExpireSecond = Integer.valueOf(redisExpireSecond);
			}
			String sessionCapacity = filterConfig
					.getInitParameter(CONFIG_SESSION_CAPACITY);
			if (!Lang.isEmpty(sessionCapacity)) {
				this.sessionCapacity = Integer.valueOf(sessionCapacity);
			}
			sessionMap = Lang.newLRUMap(this.sessionCapacity);
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
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

	private void handle(final HttpServletRequest request,
			final HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest wrapRequest = new HttpServletRequestWrapper(request) {

			@Override
			public HttpSession getSession(boolean create) {
				String sessionId = getSessionId(request);
				if (sessionId == null) {
					sessionId = generateSessionId();
					setSessionId(sessionId, request, response);
					return createAndSetSession(sessionId);
				}
				RedisSession session = RedisSessionFilter.this
						.getSession(sessionId);
				if (session == null && create) {
					sessionId = generateSessionId();
					setSessionId(sessionId, request, response);
					return createAndSetSession(sessionId);
				}
				return session;
			}

			private String generateSessionId() {
				return new OID().toString();
			}

			@Override
			public HttpSession getSession() {
				return getSession(true);
			}

		};
		chain.doFilter(wrapRequest, response);
	}

	protected void setSessionId(String sessionId, HttpServletRequest request,
			HttpServletResponse response) {
		Cookie cookie = new Cookie(sessionIdCookieName, sessionId);
		cookie.setHttpOnly(true);
		cookie.setPath(Lang.isEmpty(sessionIdCookiePath) ? request
				.getContextPath() : sessionIdCookiePath);
		cookie.setMaxAge(sessionIdCookieMaxAge);
		cookie.setSecure(sessionIdCookieSecure);
		response.addCookie(cookie);
	}

	protected RedisSession createAndSetSession(String sessionId) {
		long time = System.currentTimeMillis();
		RedisSession redisSession = new RedisSession(jedisPool, servletContext,
				sessionId, redisExpireSecond);
		redisSession.setCreationTime(time);
		redisSession.setLastAccessedTime(time);
		redisSession.setMaxInactiveInterval(sessionIdCookieMaxAge);
		redisSession.setRemoteValue(sessionId, sessionId);
		sessionMap.put(sessionId, redisSession);
		return redisSession;
	}

	protected RedisSession getSession(String sessionId) {
		RedisSession redisSession = sessionMap.get(sessionId);
		if (redisSession == null) {
			redisSession = new RedisSession(jedisPool, servletContext,
					sessionId, redisExpireSecond);
			if (!Lang.equals(redisSession.getRemoteValue(sessionId), sessionId)) {
				return null;
			}
		}
		redisSession.setLastAccessedTime(System.currentTimeMillis());
		sessionMap.put(sessionId, redisSession);
		return redisSession;
	}

	protected String getSessionId(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(sessionIdCookieName)) {
					if (!sessionIdCookieSecure
							|| sessionIdCookieSecure == cookie.getSecure()) {
						return cookie.getValue();
					}
				}
			}
		}
		return null;
	}

	@Override
	public void destroy() {
		sessionMap.clear();
		jedisPool.destroy();
	}

}
