package com.saasxx.framework.dao.finder.impl;

import com.saasxx.framework.dao.finder.facade.BetweenPath;
import com.saasxx.framework.dao.finder.facade.Finder;
import com.saasxx.framework.dao.finder.facade.Where;
import com.saasxx.framework.dao.finder.facade.WherePath;

/**
 * Where路径实现类
 * 
 * @author lujijiang
 * 
 * @param <T>
 */
public class WherePathImpl<T> implements WherePath<T> {
	/**
	 * finder处理器
	 */
	FinderHandler finderHandler;
	/**
	 * finder实现类
	 */
	FinderImpl finderImpl;
	/**
	 * 主where示例
	 */
	WhereImpl whereImpl;
	/**
	 * 路径信息
	 */
	Object left;

	/**
	 * 是否允许空参数
	 */
	boolean ifExist;
	/**
	 * 路径类型
	 */
	WherePathType wherePathType;
	/**
	 * 参数
	 */
	Object[] args;

	public FinderHandler getFinderHandler() {
		return finderHandler;
	}

	public FinderImpl getFinderImpl() {
		return finderImpl;
	}

	public WhereImpl getWhereImpl() {
		return whereImpl;
	}

	public Object getLeft() {
		return left;
	}

	public boolean isIfExist() {
		return ifExist;
	}

	public WherePathType getWherePathType() {
		return wherePathType;
	}

	public Object[] getArgs() {
		return args;
	}

	public WherePathImpl(FinderHandler finderHandler, FinderImpl finderImpl,
			WhereImpl whereImpl, Object left) {
		this.finderHandler = finderHandler;
		this.finderImpl = finderImpl;
		this.whereImpl = whereImpl;
		this.left = left;
	}

	/**
	 * 填充路径信息
	 * 
	 * @param obj
	 *            参数对象
	 * @param wherePathType
	 *            where类型
	 * @param ifExist
	 *            是否存在参数
	 */
	Where fillPath(WherePathType wherePathType, boolean ifExist, Object... args) {
		this.args = args;
		this.wherePathType = wherePathType;
		this.ifExist = ifExist;
		return whereImpl;
	}

	public Where equal(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.equal, false, arg);
	}

	public Where notEqual(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.notEqual, false, arg);
	}

	public Where like(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.like, false, arg);
	}

	public Where notLike(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.notLike, false, arg);
	}

	public Where ilike(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.ilike, false, arg);
	}

	public Where notIlike(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.notIlike, false, arg);
	}

	public Where greatThan(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.greatThan, false, arg);
	}

	public Where greatEqual(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.greatEqual, false, arg);
	}

	public Where lessThan(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.lessThan, false, arg);
	}

	public Where lessEqual(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.lessEqual, false, arg);
	}

	public BetweenPath<T> between(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		BetweenPath<T> betweenPath = new BetweenPathImpl<T>(finderHandler,
				finderImpl, whereImpl, this, WherePathType.between, false, arg);
		return betweenPath;
	}

	public BetweenPath<T> notBetween(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		BetweenPath<T> betweenPath = new BetweenPathImpl<T>(finderHandler,
				finderImpl, whereImpl, this, WherePathType.notBetween, false,
				arg);
		return betweenPath;
	}

	public Where in(T... objs) {
		Object arg = finderHandler.getPathInfo();
		return fillPath(WherePathType.in, false, arg == null ? objs : arg);
	}

	public Where notIn(T... objs) {
		Object arg = finderHandler.getPathInfo();
		return fillPath(WherePathType.notIn, false, arg == null ? objs : arg);
	}

	public Where equal(Finder subFinder) {
		return fillPath(WherePathType.equal, false, subFinder);
	}

	public Where notEqual(Finder subFinder) {
		return fillPath(WherePathType.notEqual, false, subFinder);
	}

	public Where like(Finder subFinder) {
		return fillPath(WherePathType.like, false, subFinder);
	}

	public Where notLike(Finder subFinder) {
		return fillPath(WherePathType.notIlike, false, subFinder);
	}

	public Where ilike(Finder subFinder) {
		return fillPath(WherePathType.ilike, false, subFinder);
	}

	public Where notIlike(Finder subFinder) {
		return fillPath(WherePathType.notIlike, false, subFinder);
	}

	public Where greatThan(Finder subFinder) {
		return fillPath(WherePathType.greatThan, false, subFinder);
	}

	public Where greatEqual(Finder subFinder) {
		return fillPath(WherePathType.greatEqual, false, subFinder);
	}

	public Where lessThan(Finder subFinder) {
		return fillPath(WherePathType.lessThan, false, subFinder);
	}

	public Where lessEqual(Finder subFinder) {
		return fillPath(WherePathType.lessEqual, false, subFinder);
	}

	public BetweenPath<T> between(Finder subFinder) {
		BetweenPath<T> betweenPath = new BetweenPathImpl<T>(finderHandler,
				finderImpl, whereImpl, this, WherePathType.between, false,
				subFinder);
		return betweenPath;
	}

	public BetweenPath<T> notBetween(Finder subFinder) {
		BetweenPath<T> betweenPath = new BetweenPathImpl<T>(finderHandler,
				finderImpl, whereImpl, this, WherePathType.notBetween, false,
				subFinder);
		return betweenPath;
	}

	public Where in(Finder subFinder) {
		return fillPath(WherePathType.in, false, subFinder);
	}

	public Where notIn(Finder subFinder) {
		return fillPath(WherePathType.notIn, false, subFinder);
	}

	public Where equalIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.equal, true, arg);
	}

	public Where notEqualIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.notEqual, true, arg);
	}

	public Where likeIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.like, true, arg);
	}

	public Where notLikeIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.notLike, true, arg);
	}

	public Where ilikeIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.ilike, true, arg);
	}

	public Where notIlikeIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.notIlike, true, arg);
	}

	public Where greatThanIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.greatThan, true, arg);
	}

	public Where greatEqualIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.greatEqual, true, arg);
	}

	public Where lessThanIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.lessThan, true, arg);
	}

	public Where lessEqualIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		return fillPath(WherePathType.lessEqual, true, arg);
	}

	public BetweenPath<T> betweenIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		BetweenPath<T> betweenPath = new BetweenPathImpl<T>(finderHandler,
				finderImpl, whereImpl, this, WherePathType.between, true, arg);
		return betweenPath;
	}

	public BetweenPath<T> notBetweenIfExist(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		BetweenPath<T> betweenPath = new BetweenPathImpl<T>(finderHandler,
				finderImpl, whereImpl, this, WherePathType.notBetween, true,
				arg);
		return betweenPath;
	}

	public Where isNull() {
		return fillPath(WherePathType.isNull, false);
	}

	public Where isNotNull() {
		return fillPath(WherePathType.isNotNull, false);
	}

	public boolean equals(Object obj) {
		throw new UnsupportedOperationException(
				"The equals method is not supported, maybe you should use equal method");
	}

}
