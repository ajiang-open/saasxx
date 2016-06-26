package com.saasxx.framework.convert;

public interface Converter {
	public Object convert(Object from, Class<?> toType, Object... args);
}
