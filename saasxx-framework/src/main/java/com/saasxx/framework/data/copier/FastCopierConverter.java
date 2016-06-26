package com.saasxx.framework.data.copier;

public interface FastCopierConverter {
	Object convert(Object source, Object target, Class sourceClass,
			Class targetClass, Object context);
}
