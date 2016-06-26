package com.saasxx.framework.dao.finder.facade;

public interface HavingFunctionPath<T> extends HavingPath<T> {
	Having equal(T obj);

	Having notEqual(T obj);

	Having greatThan(T obj);

	Having greatEqual(T obj);

	Having lessThan(T obj);

	Having lessEqual(T obj);

	Having equal(Finder subFinder);

	Having notEqual(Finder subFinder);

	Having greatThan(Finder subFinder);

	Having greatEqual(Finder subFinder);

	Having lessThan(Finder subFinder);

	Having lessEqual(Finder subFinder);

	Having equalIfExist(T obj);

	Having notEqualIfExist(T obj);

	Having greatThanIfExist(T obj);

	Having greatEqualIfExist(T obj);

	Having lessThanIfExist(T obj);

	Having lessEqualIfExist(T obj);
}
