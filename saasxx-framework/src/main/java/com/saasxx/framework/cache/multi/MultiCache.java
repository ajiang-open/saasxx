package com.saasxx.framework.cache.multi;

import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多key缓存
 * 
 * @author lujijiang
 *
 * @param <V>
 */
public abstract class MultiCache<V> {
	/**
	 * 创建一个线程安全的、Hash的多key缓存
	 * 
	 * @return
	 */
	public static <V> MultiCache<V> create() {
		return concurrentHashMultiCache();
	}

	/**
	 * 创建一个线程安全的、Hash的多key缓存
	 * 
	 * @return
	 */
	public static <V> MultiCache<V> concurrentHashMultiCache() {
		return new MultiCache<V>() {
			public Map<Object, Object> createMap() {
				return new ConcurrentHashMap<Object, Object>();
			}
		};
	}

	/**
	 * 创建一个Hash的多key缓存
	 * 
	 * @param sync
	 *            是否同步
	 * @return
	 */
	public static <V> MultiCache<V> hashMultiCache(final boolean sync) {
		if (sync) {
			return concurrentHashMultiCache();
		}
		return new MultiCache<V>() {
			public Map<Object, Object> createMap() {
				Map<Object, Object> map = new HashMap<Object, Object>();
				return map;
			}
		};
	}

	/**
	 * 创建一个软引用的、Hash的多key缓存，可随时由JVM回收掉其中的键值对
	 * 
	 * @param sync
	 *            是否同步
	 * @return
	 */
	public static <V> MultiCache<V> weakHashMultiCache(final boolean sync) {

		return new MultiCache<V>() {
			public Map<Object, Object> createMap() {
				if(sync){
					return new ConcurrentReferenceHashMap<Object, Object>();
				}
				return new WeakHashMap<Object, Object>();
			}
		};
	}

	/**
	 * 创建一个Hash的、保持顺序的多key缓存
	 * 
	 * @param sync
	 *            是否同步
	 * @return
	 */
	public static <V> MultiCache<V> linkedHashMultiCache(final boolean sync) {
		return new MultiCache<V>() {
			public Map<Object, Object> createMap() {
				Map<Object, Object> map = new LinkedHashMap<Object, Object>();
				if (sync) {
					map = Collections.synchronizedMap(map);
				}
				return map;
			}
		};
	}

	/**
	 * 创建一个自动排序的多key缓存
	 * 
	 * @param sync
	 *            是否同步
	 * @return
	 */
	public static <V> MultiCache<V> treeMultiCache(final boolean sync) {
		return new MultiCache<V>() {
			public Map<Object, Object> createMap() {
				Map<Object, Object> map = new TreeMap<Object, Object>();
				if (sync) {
					map = Collections.synchronizedMap(map);
				}
				return map;
			}
		};
	}

	public abstract Map<Object, Object> createMap();

	private Map<Object, Object> multiMaps = createMap();

	@SuppressWarnings("unchecked")
	private Map<Object, V> valueMap(Object[] keys, int offset) {
		int multiMapKey = keys.length + offset;
		Map<Object, Object> multiMap = (Map<Object, Object>) multiMaps
				.get(multiMapKey);
		if (multiMap == null) {
			multiMap = createMap();
			multiMaps.put(multiMapKey, multiMap);
		}
		int length = keys.length + offset;
		for (int i = 0; i < length; i++) {
			Object key = keys[i];
			Map<Object, Object> subMultiMap = (Map<Object, Object>) multiMap
					.get(key);
			if (subMultiMap == null) {
				subMultiMap = createMap();
				multiMap.put(key, subMultiMap);
			}
			multiMap = subMultiMap;
		}
		return (Map<Object, V>) multiMap;
	}

	private void checkKeys(Object[] keys, int minNumber) {
		if (keys == null) {
			throw new NullPointerException("The keys should not be null");
		}
		if (keys.length < minNumber) {
			throw new IllegalArgumentException(String.format(
					"The keys's number should at least %d", minNumber));
		}
	}

	public boolean containsKey(Object... keys) {
		checkKeys(keys, 1);
		return valueMap(keys, -1).containsKey(keys[keys.length - 1]);
	}

	public V get(Object... keys) {
		checkKeys(keys, 1);
		return valueMap(keys, -1).get(keys[keys.length - 1]);
	}

	public V put(V value, Object... keys) {
		checkKeys(keys, 1);
		return valueMap(keys, -1).put(keys[keys.length - 1], value);
	}

	public V remove(Object... keys) {
		checkKeys(keys, 1);
		return valueMap(keys, -1).remove(keys[keys.length - 1]);
	}

	public Map<Object, V> map(Object... keys) {
		checkKeys(keys, 0);
		return valueMap(keys, 0);
	}

	public int size(Object... keys) {
		return map(keys).size();
	}

	public boolean isEmpty(Object... keys) {
		return map(keys).isEmpty();
	}

	public boolean containsValue(Object value, Object... keys) {
		return map(keys).containsValue(value);
	}

	public void putAll(Map<? extends Object, ? extends V> m, Object... keys) {
		map(keys).putAll(m);
	}

	public Set<Object> keySet(Object... keys) {
		return map(keys).keySet();
	}

	public Collection<V> values(Object... keys) {
		return map(keys).values();
	}

	public Set<Map.Entry<Object, V>> entrySet(Object... keys) {
		return map(keys).entrySet();
	}

	public void clear(Object... keys) {
		map(keys).clear();
	}

}
