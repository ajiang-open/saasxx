package com.saasxx.framework.dao.finder.facade;

public interface Having extends QueryRender {

	<T> HavingPath<T> get(T obj);

	QueryAppender append(String queryString, Object... args);

}
