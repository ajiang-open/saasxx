package com.saasxx.framework.dao.data;

import java.lang.reflect.Field;

import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.PageRequest;

/**
 * 用来替代PageRequest以从WebRpc中直接传参
 * 
 * @author lujijiang
 *
 */
public class PageVo extends PageRequest {

	static Field pageField;
	static Field sizeField;

	static {
		try {
			pageField = AbstractPageRequest.class.getDeclaredField("page");
			pageField.setAccessible(true);
			sizeField = AbstractPageRequest.class.getDeclaredField("size");
			sizeField.setAccessible(true);
		} catch (NoSuchFieldException e) {
		} catch (SecurityException e) {
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8660968296435812194L;

	public PageVo() {
		super(0, 25);
	}

	public void setPage(int page) {
		try {
			pageField.set(this, page);
		} catch (IllegalArgumentException | IllegalAccessException e) {

		}
	}

	public void setSize(int size) {
		try {
			sizeField.set(this, size);
		} catch (IllegalArgumentException | IllegalAccessException e) {

		}
	}
}
