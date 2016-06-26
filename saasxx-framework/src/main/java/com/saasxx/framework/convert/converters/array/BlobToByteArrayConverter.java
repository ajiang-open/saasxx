package com.saasxx.framework.convert.converters.array;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;

import com.saasxx.framework.Lang;
import com.saasxx.framework.convert.Converter;

public class BlobToByteArrayConverter implements Converter {

	public Object convert(Object from, Class<?> toType, Object... args) {
		Blob blob = (Blob) from;
		try {
			return IOUtils.toByteArray(blob.getBinaryStream());
		} catch (IOException e) {
			throw Lang.unchecked(e);
		} catch (SQLException e) {
			throw Lang.unchecked(e);
		}
	}

}
