package com.saasxx.framework.convert.converters.array;

import com.saasxx.framework.convert.Converter;
import com.saasxx.framework.convert.Converters;

public class IterableToArrayConverter implements Converter {

	public Object convert(Object from, Class<?> toType, Object... args) {
		Iterable<?> iterable = (Iterable<?>) from;
		return Converters.BASE.convert(iterable.iterator(), toType, args);
	}

}
