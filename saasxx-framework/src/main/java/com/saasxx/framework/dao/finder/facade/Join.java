package com.saasxx.framework.dao.finder.facade;

import java.util.Collection;

public interface Join {

	public <T> JoinPath<T> get(T[] array);

	<T> JoinPath<T> get(Collection<T> list);

	<T> JoinPath<T> get(T obj);

}
