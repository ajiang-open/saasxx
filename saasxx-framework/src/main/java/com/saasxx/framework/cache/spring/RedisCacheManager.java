package com.saasxx.framework.cache.spring;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;

import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Encodes;
import com.saasxx.framework.data.Kryos;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.SafeEncoder;

/**
 * 基于Redis的Spring缓存管理器
 * 
 * @author lujijiang
 *
 */
public class RedisCacheManager implements CacheManager, InitializingBean {

	/**
	 * 基于Redis的Spring缓存
	 * 
	 * @author lujijiang
	 *
	 */
	public static class RedisCache implements Cache {

		/**
		 * 值包装器
		 * 
		 * @author lujijiang
		 *
		 */
		public static class RedisValueWrapper implements ValueWrapper {

			Object value;

			public RedisValueWrapper(Object value) {
				this.value = value;
			}

			public Object getValue() {
				return value;
			}

			public void setValue(Object value) {
				this.value = value;
			}

			public Object get() {
				return value;
			}

			@Override
			public String toString() {
				return "RedisValueWrapper [value=" + value + "]";
			}

		}

		/**
		 * Redis连接池
		 */
		private JedisPool jedisPool;

		/**
		 * 缓存名
		 */
		private String name;

		/**
		 * 缓存过期时间
		 */
		private int expiredSecond;

		/**
		 * 全局前缀
		 */
		private String prefix;

		public RedisCache(JedisPool jedisPool, String prefix, String name,
				int expiredSecond) {
			this.jedisPool = jedisPool;
			this.prefix = prefix;
			this.name = name;
			this.expiredSecond = expiredSecond;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getExpiredSecond() {
			return expiredSecond;
		}

		public void setExpiredSecond(int expiredSecond) {
			this.expiredSecond = expiredSecond;
		}

		@Override
		public Object getNativeCache() {
			return this;
		}

		@Override
		public ValueWrapper get(Object key) {
			Jedis jedis = jedisPool.getResource();
			try {
				byte[] keyBytes = toKeyBytes(key);
				byte[] valueBytes = jedis.get(keyBytes);
				if (valueBytes == null) {
					return null;
				}
				if (valueBytes.length == 0) {
					return null;
				}
				ValueWrapper valueWrapper = Kryos.fromBytes(valueBytes,
						RedisValueWrapper.class);
				return valueWrapper;
			} finally {
				jedis.close();
			}
		}

		private byte[] toKeyBytes(Object key) {
			StringBuilder keyBuilder = new StringBuilder();
			keyBuilder.append(prefix);
			keyBuilder.append(":");
			keyBuilder.append(name.hashCode());
			keyBuilder.append(":");
			if (key == null || !(key instanceof CharSequence)) {
				keyBuilder.append("b64:");
				key = Encodes.encodeBase64(Kryos.toBytes(new RedisValueWrapper(
						key)));
			} else {
				keyBuilder.append("str:");
			}
			keyBuilder.append(key);
			return SafeEncoder.encode(keyBuilder.toString());
		}

		private byte[] toValueBytes(Object value) {
			return Kryos.toBytes(new RedisValueWrapper(value));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(Object key, Class<T> type) {
			ValueWrapper valueWrapper = get(key);
			if (valueWrapper == null)
				return null;
			else
				return (T) valueWrapper.get();
		}

		public void put(Object key, Object value, int expiredSecond) {
			Jedis jedis = jedisPool.getResource();
			try {
				byte[] keyBytes = toKeyBytes(key);
				byte[] valueBytes = toValueBytes(value);
				jedis.setex(keyBytes, expiredSecond, valueBytes);
			} finally {
				jedis.close();
			}
		}

		@Override
		public void put(Object key, Object value) {
			put(key, value, expiredSecond);
		}

		public ValueWrapper putIfAbsent(Object key, Object value,
				int expiredSecond) {
			final ValueWrapper valueWrapper = get(key);
			if (valueWrapper == null) {
				put(key, value, expiredSecond);
				return null;
			}
			return valueWrapper;
		}

		@Override
		public ValueWrapper putIfAbsent(Object key, Object value) {
			return putIfAbsent(key, value, expiredSecond);
		}

		@Override
		public void evict(Object key) {
			Jedis jedis = jedisPool.getResource();
			try {
				byte[] keyBytes = toKeyBytes(key);
				jedis.del(keyBytes);
			} finally {
				jedis.close();
			}
		}

		@Override
		public void clear() {
			StringBuilder keyBuilder = new StringBuilder();
			keyBuilder.append(prefix);
			keyBuilder.append(":");
			keyBuilder.append(name.hashCode());
			keyBuilder.append(":*");
			Jedis jedis = jedisPool.getResource();
			try {
				Iterator<String> iterator = jedis.keys(keyBuilder.toString())
						.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					jedis.del(key);
				}
			} finally {
				jedis.close();
			}
		}

		/**
		 * 获取所有的健的Set
		 * 
		 * @return
		 */
		public Set<Object> keySet() {
			StringBuilder keyBuilder = new StringBuilder();
			keyBuilder.append(prefix);
			keyBuilder.append(":");
			keyBuilder.append(name.hashCode());
			keyBuilder.append(":");
			String prefix = keyBuilder.toString();
			Jedis jedis = jedisPool.getResource();
			try {
				Set<String> originalKeySet = jedis.keys(prefix.concat("*"));
				Set<Object> keySet = new LinkedHashSet<Object>();
				for (String key : originalKeySet) {
					String type = key.substring(prefix.length(),
							prefix.length() + 3);
					key = key.substring(prefix.length() + 4);
					if ("b64".equals(type)) {
						RedisValueWrapper valueWrapper = Kryos.fromBytes(
								Encodes.decodeBase64(key),
								RedisValueWrapper.class);
						keySet.add(valueWrapper.get());
					} else {
						keySet.add(key);
					}
				}
				return Collections.unmodifiableSet(keySet);
			} finally {
				jedis.close();
			}
		}

		public Map<Object, Object> toMap() {

			return new AbstractMap<Object, Object>() {

				@Override
				public boolean containsKey(Object key) {
					return RedisCache.this.get(key) != null;
				}

				@Override
				public Object get(Object key) {
					ValueWrapper valueWrapper = RedisCache.this.get(key);
					return valueWrapper == null ? null : valueWrapper.get();
				}

				@Override
				public Object put(Object key, Object value) {
					ValueWrapper oldValue = RedisCache.this.get(key);
					RedisCache.this.put(key, value);
					return oldValue == null ? null : oldValue.get();
				}

				@Override
				public Object remove(Object key) {
					ValueWrapper oldValue = RedisCache.this.get(key);
					RedisCache.this.evict(key);
					return oldValue == null ? null : oldValue.get();
				}

				@Override
				public void clear() {
					RedisCache.this.clear();
				}

				public Set<java.util.Map.Entry<Object, Object>> entrySet() {
					return new AbstractSet<java.util.Map.Entry<Object, Object>>() {

						Set<Object> keySet = RedisCache.this.keySet();

						public Iterator<java.util.Map.Entry<Object, Object>> iterator() {

							Iterator<Object> keyIterator = keySet.iterator();

							return new Iterator<java.util.Map.Entry<Object, Object>>() {

								@Override
								public boolean hasNext() {
									return keyIterator.hasNext();
								}

								@Override
								public java.util.Map.Entry<Object, Object> next() {
									final Object key = keyIterator.next();
									return new java.util.Map.Entry<Object, Object>() {
										public Object getKey() {
											return key;
										}

										@Override
										public Object getValue() {
											ValueWrapper valueWrapper = RedisCache.this
													.get(key);
											return valueWrapper == null ? null
													: valueWrapper.get();
										}

										@Override
										public Object setValue(Object value) {
											return put(key, value);
										}

									};
								}
							};
						}

						public int size() {
							return keySet.size();
						}
					};
				}
			};
		}
	}

	/**
	 * jedis 连接池
	 */
	private JedisPool jedisPool;

	/**
	 * redisUri
	 */
	private URI redisUri;
	/**
	 * redis连接超时时间
	 */
	private int redisTimeout = 120;
	/**
	 * 缓存的全局前缀
	 */
	private String prefix = "spring";
	/**
	 * 过期秒数（默认120秒）
	 */
	private int expiredSecond = 120;

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public URI getRedisUri() {
		return redisUri;
	}

	public void setRedisUri(URI redisUri) {
		this.redisUri = redisUri;
	}

	public void setRedisUri(String redisUri) {
		try {
			setRedisUri(new URI(redisUri));
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

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public int getExpiredSecond() {
		return expiredSecond;
	}

	public void setExpiredSecond(int expiredSecond) {
		this.expiredSecond = expiredSecond;
	}

	@Override
	public Cache getCache(final String name) {
		Assert.notNull(name, "The cache name should not be null");
		Cache cache = new RedisCache(jedisPool, prefix, name, expiredSecond);
		Jedis jedis = jedisPool.getResource();
		try {
			StringBuilder keyBuilder = new StringBuilder();
			keyBuilder.append(prefix);
			keyBuilder.append("::");
			keyBuilder.append(name.hashCode());
			byte[] keyBytes = SafeEncoder.encode(keyBuilder.toString());
			Boolean isExist = jedis.exists(keyBytes);
			if (isExist == null || !isExist) {
				jedis.set(keyBytes, SafeEncoder.encode(name));
			}
		} finally {
			jedis.close();
		}
		return cache;
	}

	@Override
	public Collection<String> getCacheNames() {
		StringBuilder keyBuilder = new StringBuilder();
		keyBuilder.append(prefix);
		keyBuilder.append("::*");
		Jedis jedis = jedisPool.getResource();
		try {
			Set<String> cacheNameSet = new LinkedHashSet<String>();
			Iterator<String> iterator = jedis.keys(keyBuilder.toString())
					.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				cacheNameSet.add(jedis.get(key));
			}
			return cacheNameSet;
		} finally {
			jedis.close();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (jedisPool == null) {
			Assert.notNull(redisUri, "redisUri should not be null");
			jedisPool = new JedisPool(redisUri, redisTimeout);
		}
		Assert.notNull(prefix, "prefix should not be null");
	}

	/**
	 * 摧毁缓存管理器
	 */
	public void destroy() {
		StringBuilder keyBuilder = new StringBuilder();
		keyBuilder.append(prefix);
		keyBuilder.append(":*");
		Jedis jedis = jedisPool.getResource();
		try {
			Iterator<String> iterator = jedis.keys(keyBuilder.toString())
					.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				jedis.del(key);
			}
		} finally {
			jedis.close();
		}
		jedisPool.destroy();
	}

}
