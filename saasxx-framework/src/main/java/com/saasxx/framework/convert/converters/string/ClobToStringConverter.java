package com.saasxx.framework.convert.converters.string;

import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;

import com.saasxx.framework.Lang;
import com.saasxx.framework.convert.Converter;

public class ClobToStringConverter implements Converter {

	public Object convert(Object from, Class<?> toType, Object... args) {
		Clob clob = (Clob) from;
		try {
			return IOUtils.toString(clob.getCharacterStream());
		} catch (IOException e) {
			throw Lang.unchecked(e);
		} catch (SQLException e) {
			throw Lang.unchecked(e);
		}
	}

}
