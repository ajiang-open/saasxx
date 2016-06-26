package com.saasxx.framework.convert.converters.string;

import com.saasxx.framework.convert.Converter;

public class ObjectToStringConverter implements Converter {

	public Object convert(Object from, Class<?> toType, Object... args) {
		return String.valueOf(from);
	}

}
