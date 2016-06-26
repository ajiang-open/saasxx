package com.saasxx.framework.dao.data;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.AbstractList;

import org.springframework.data.domain.Page;

import com.saasxx.framework.lang.Proxys;

/**
 * 页面处理工具
 * 
 * @author lujijiang
 *
 */
public class Pages {
	/**
	 * 页面转换器
	 * 
	 * @author lujijiang
	 *
	 * @param <S>
	 * @param <T>
	 */
	public static interface PageConverter<S, T> {
		/**
		 * 将源Page对象转换为另一种类型的Page对象
		 * 
		 * @param source
		 * @return
		 */
		T convert(S source);
	}

	@SuppressWarnings("unchecked")
	public static <T, S> Page<T> convert(final Page<S> sourcePage,
			final PageConverter<S, T> pageConverter) {

		return Proxys.newProxyInstance(new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Exception {
				if ("getContent".equals(method.getName())) {
					return new AbstractList<T>() {
						public T get(int index) {
							return pageConverter.convert(sourcePage
									.getContent().get(index));
						}

						public int size() {
							return sourcePage.getContent().size();
						}
					};
				}
				return method.invoke(sourcePage, args);
			}
		}, Page.class);

	}
}
