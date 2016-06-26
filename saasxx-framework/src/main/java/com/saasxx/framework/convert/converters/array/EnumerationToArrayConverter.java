package com.saasxx.framework.convert.converters.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import com.saasxx.framework.Lang;
import com.saasxx.framework.convert.Converter;
import com.saasxx.framework.convert.Converters;

public class EnumerationToArrayConverter implements Converter {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object convert(Object from, Class toType, Object... args) {
		Enumeration<?> enumeration = (Enumeration<?>) from;
		try {
			List<Object> list = new ArrayList<Object>();
			while (enumeration.hasMoreElements()) {
				Object object = enumeration.nextElement();
				list.add(Converters.BASE.convert(object,
						toType.getComponentType()));
			}
			return Arrays.copyOf(list.toArray(), list.size(), toType);
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

}
