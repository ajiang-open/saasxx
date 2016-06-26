package com.saasxx.framework.dao.finder.impl;

import com.saasxx.framework.dao.finder.facade.BetweenPath;
import com.saasxx.framework.dao.finder.facade.Finder;
import com.saasxx.framework.dao.finder.facade.Where;
import com.saasxx.framework.dao.finder.facade.WherePath.WherePathType;

public class BetweenPathImpl<T> implements BetweenPath<T> {

	FinderHandler finderHandler;
	FinderImpl finderImpl;
	WhereImpl whereImpl;
	WherePathImpl<T> wherePathImpl;
	WherePathType wherePathType;
	boolean ifExist;
	Object arg;

	public BetweenPathImpl(FinderHandler finderHandler, FinderImpl finderImpl,
			WhereImpl whereImpl, WherePathImpl<T> wherePathImpl,
			WherePathType wherePathType, boolean ifExist, Object arg) {
		this.finderHandler = finderHandler;
		this.finderImpl = finderImpl;
		this.whereImpl = whereImpl;
		this.wherePathImpl = wherePathImpl;
		this.wherePathType = wherePathType;
		this.ifExist = ifExist;
		this.arg = arg;
	}

	public Where and(T obj) {
		Object secondArg = finderHandler.getPathInfo();
		secondArg = secondArg == null ? obj : secondArg;
		wherePathImpl.fillPath(wherePathType, ifExist, arg, secondArg);
		return whereImpl;
	}

	public Where and(Finder subFinder) {
		wherePathImpl.fillPath(wherePathType, ifExist, arg, subFinder);
		return whereImpl;
	}

}
