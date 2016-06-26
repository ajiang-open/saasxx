package com.saasxx.framework.convert.converters.primitive;

import com.saasxx.framework.convert.Converter;
import com.saasxx.framework.convert.Converters;

public class ObjectToBooleanConverter implements Converter {

	public Object convert(Object from, Class<?> toType, Object... args) {
		String string = Converters.BASE.convert(from, String.class);
		return Boolean.valueOf(string);
	}

}
