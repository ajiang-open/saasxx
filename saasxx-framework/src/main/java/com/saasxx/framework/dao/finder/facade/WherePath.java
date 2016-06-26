package com.saasxx.framework.dao.finder.facade;

public interface WherePath<T> {
	public enum WherePathType {
		equal, notEqual, like, notLike, ilike, notIlike, greatThan, greatEqual, lessThan, lessEqual, between, notBetween, isNull, isNotNull, in, notIn
	}

	Where equal(T obj);

	Where notEqual(T obj);

	Where like(T obj);

	Where notLike(T obj);

	Where ilike(T obj);

	Where notIlike(T obj);

	Where greatThan(T obj);

	Where greatEqual(T obj);

	Where lessThan(T obj);

	Where lessEqual(T obj);

	BetweenPath<T> between(T obj);

	BetweenPath<T> notBetween(T obj);

	Where in(T... objs);

	Where notIn(T... objs);

	Where equal(Finder subFinder);

	Where notEqual(Finder subFinder);

	Where like(Finder subFinder);

	Where notLike(Finder subFinder);

	Where ilike(Finder subFinder);

	Where notIlike(Finder subFinder);

	Where greatThan(Finder subFinder);

	Where greatEqual(Finder subFinder);

	Where lessThan(Finder subFinder);

	Where lessEqual(Finder subFinder);

	BetweenPath<T> between(Finder subFinder);

	BetweenPath<T> notBetween(Finder subFinder);

	Where in(Finder subFinder);

	Where notIn(Finder subFinder);

	Where equalIfExist(T obj);

	Where notEqualIfExist(T obj);

	Where likeIfExist(T obj);

	Where notLikeIfExist(T obj);

	Where ilikeIfExist(T obj);

	Where notIlikeIfExist(T obj);

	Where greatThanIfExist(T obj);

	Where greatEqualIfExist(T obj);

	Where lessThanIfExist(T obj);

	Where lessEqualIfExist(T obj);

	BetweenPath<T> betweenIfExist(T obj);

	BetweenPath<T> notBetweenIfExist(T obj);

	Where isNull();

	Where isNotNull();

}