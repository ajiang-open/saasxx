package com.saasxx.framework.testcase.cache.spring;

import java.util.Date;
import java.util.Map;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.saasxx.framework.Lang;
import com.saasxx.framework.cache.spring.RedisCacheManager;
import com.saasxx.framework.cache.spring.RedisCacheManager.RedisCache;

/**
 * 用于测试RedisSpringCacheManager
 * 
 * @author lujijiang
 *
 */
public class RedisCacheManagerTestCase {

	public static void main(String[] args) {
		CacheManager cacheManager = getCacheManager();
		Cache cache = cacheManager.getCache("testSpringCache");
		cache.clear();
		Map<Object, Object> map = ((RedisCache) cache).toMap();
		map.put("aaaa", "111111");
		map.put(222222, "bbbbbb");
		Date date = new Date();
		map.put(date, "Date(1111111)");
		System.out.println(map.get(222222));
		System.out.println(map.get(date));
		System.out.println(map.get(new Date(date.getTime())));
		System.out.println(((RedisCache) cache).keySet());
		System.out.println(map);
	}

	private static RedisCacheManager getCacheManager() {
		try {
			RedisCacheManager cacheManager = new RedisCacheManager();
			cacheManager.setRedisUri("http://localhost:6379");
			cacheManager.afterPropertiesSet();
			System.out.println(cacheManager.getCacheNames());
			return cacheManager;
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}
}
