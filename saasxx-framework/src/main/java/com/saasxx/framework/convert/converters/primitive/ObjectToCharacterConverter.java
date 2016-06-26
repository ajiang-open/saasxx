package com.saasxx.framework.convert.converters.primitive;

import com.saasxx.framework.convert.Converter;
import com.saasxx.framework.convert.Converters;

public class ObjectToCharacterConverter implements Converter {

	public Object convert(Object from, Class<?> toType, Object... args) {
		String string = Converters.BASE.convert(from, String.class);
		return string.length() == 1 ? string.charAt(0) : (char) 0;
	}

}
