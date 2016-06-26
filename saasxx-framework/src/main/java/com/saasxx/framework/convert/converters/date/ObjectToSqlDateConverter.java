package com.saasxx.framework.convert.converters.date;

import java.util.Date;

import com.saasxx.framework.convert.Converter;
import com.saasxx.framework.convert.Converters;

public class ObjectToSqlDateConverter implements Converter {

	public Object convert(Object from, Class<?> toType, Object... args) {
		return new java.sql.Date(Converters.BASE
				.convert(from, Date.class, args).getTime());
	}

}
