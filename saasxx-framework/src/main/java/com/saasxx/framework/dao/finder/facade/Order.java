package com.saasxx.framework.dao.finder.facade;

public interface Order extends QueryRender {

	OrderPath get(Object obj);

	QueryAppender append(String queryString, Object... args);

}
