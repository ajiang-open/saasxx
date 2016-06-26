package com.saasxx.framework.convert.converters.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.saasxx.framework.Lang;
import com.saasxx.framework.convert.Converter;
import com.saasxx.framework.convert.Converters;

public class IteratorToArrayConverter implements Converter {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object convert(Object from, Class toType, Object... args) {
		Iterator<?> iterator = (Iterator<?>) from;
		try {
			List<Object> list = new ArrayList<Object>();
			while (iterator.hasNext()) {
				Object object = iterator.next();
				list.add(Converters.BASE.convert(object,
						toType.getComponentType()));
			}
			return Arrays.copyOf(list.toArray(), list.size(), toType);
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

}
