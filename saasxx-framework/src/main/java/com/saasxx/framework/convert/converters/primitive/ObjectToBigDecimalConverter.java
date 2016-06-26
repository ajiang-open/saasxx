package com.saasxx.framework.convert.converters.primitive;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import com.saasxx.framework.Lang;
import com.saasxx.framework.convert.Converter;
import com.saasxx.framework.convert.Converters;

public class ObjectToBigDecimalConverter implements Converter {

	public Object convert(Object from, Class<?> toType, Object... args) {
		String string = Converters.BASE.convert(from, String.class);
		if (args != null && args.length != 0 && args[0] != null) {
			NumberFormat format;
			if (args[0] instanceof NumberFormat) {
				format = (NumberFormat) args[0];
			} else {
				format = new DecimalFormat(args[0].toString());
			}
			try {
				string = format.parseObject(string).toString();
			} catch (ParseException e) {
				throw Lang.unchecked(e);
			}
		}
		return new BigDecimal(string);
	}

}
