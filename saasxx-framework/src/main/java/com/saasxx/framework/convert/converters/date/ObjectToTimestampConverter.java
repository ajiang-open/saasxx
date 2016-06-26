package com.saasxx.framework.convert.converters.date;

import java.sql.Timestamp;
import java.util.Date;

import com.saasxx.framework.convert.Converter;
import com.saasxx.framework.convert.Converters;

public class ObjectToTimestampConverter implements Converter {

	public Object convert(Object from, Class<?> toType, Object... args) {
		return new Timestamp(Converters.BASE.convert(from, Date.class, args)
				.getTime());
	}

}
