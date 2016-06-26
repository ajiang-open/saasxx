package com.saasxx.framework.convert.converters.map;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import com.saasxx.framework.Lang;
import com.saasxx.framework.convert.Converter;
import com.saasxx.framework.lang.Proxys;

import net.sf.cglib.beans.BeanMap;

public class ObjectToMapConverter implements Converter {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object convert(Object from, Class<?> toType, Object... args) {
		final Map original = from instanceof Map ? (Map) from : BeanMap
				.create(from);
		if (toType.isInterface() || Modifier.isAbstract(toType.getModifiers())) {
			return Proxys.newProxyInstance(new InvocationHandler() {
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Exception {
					return method.invoke(original, args);
				}
			}, toType);
		} else {
			try {
				Map map = (Map) toType.newInstance();
				map.putAll((Map) original);
				return map;
			} catch (InstantiationException e) {
				throw Lang.unchecked(e);
			} catch (IllegalAccessException e) {
				throw Lang.unchecked(e);
			}

		}
	}

}
