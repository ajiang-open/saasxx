package com.saasxx.framework.dao.finder.facade;

public interface HavingPath<T> {
	HavingPath<T> distinct();

	HavingFunctionPath<Long> count();

	HavingFunctionPath<T> avg();

	HavingFunctionPath<T> sum();

	HavingFunctionPath<T> min();

	HavingFunctionPath<T> max();
}
