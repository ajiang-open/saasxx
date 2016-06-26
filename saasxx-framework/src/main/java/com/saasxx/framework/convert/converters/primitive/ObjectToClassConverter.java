package com.saasxx.framework.convert.converters.primitive;

import com.saasxx.framework.convert.Converter;
import com.saasxx.framework.lang.Classes;

public class ObjectToClassConverter implements Converter {

	public Object convert(Object from, Class<?> toType, Object... args) {
		return Classes.forName(from.toString());
	}

}
