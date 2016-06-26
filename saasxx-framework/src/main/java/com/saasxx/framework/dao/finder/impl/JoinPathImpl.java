package com.saasxx.framework.dao.finder.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.saasxx.framework.dao.finder.facade.JoinPath;
import com.saasxx.framework.dao.finder.facade.Where.WhereType;
import com.saasxx.framework.dao.finder.vo.EntityInfo;
import com.saasxx.framework.dao.finder.vo.PathInfo;

public class JoinPathImpl<T> implements JoinPath<T> {

	FinderHandler finderHandler;
	FinderImpl finderImpl;
	JoinImpl joinImpl;
	EntityInfo<T> entityInfo;
	PathInfo pathInfo;
	JoinPathType joinPathType = JoinPathType.inner;
	WhereImpl whereImpl;

	public FinderHandler getFinderHandler() {
		return finderHandler;
	}

	public FinderImpl getFinderImpl() {
		return finderImpl;
	}

	public JoinImpl getJoinImpl() {
		return joinImpl;
	}

	public EntityInfo<T> getEntityInfo() {
		return entityInfo;
	}

	public PathInfo getPathInfo() {
		return pathInfo;
	}

	public JoinPathType getJoinPathType() {
		return joinPathType;
	}

	public WhereImpl getWhereImpl() {
		return whereImpl;
	}

	public JoinPathImpl(FinderHandler finderHandler, FinderImpl finderImpl,
			JoinImpl joinImpl, EntityInfo<T> entityInfo, PathInfo pathInfo) {
		super();
		this.finderHandler = finderHandler;
		this.finderImpl = finderImpl;
		this.joinImpl = joinImpl;
		this.entityInfo = entityInfo;
		this.pathInfo = pathInfo;
		this.whereImpl = new WhereImpl(finderHandler, finderImpl,
				WhereType.and, new ConcurrentHashMap<Long, EntityInfo<?>>());
	}

	public T inner() {
		this.joinPathType = JoinPathType.inner;
		return entityInfo.getProxy();
	}

	public T left() {
		this.joinPathType = JoinPathType.left;
		return entityInfo.getProxy();
	}

	public T right() {
		this.joinPathType = JoinPathType.right;
		return entityInfo.getProxy();
	}

	public T full() {
		this.joinPathType = JoinPathType.full;
		return entityInfo.getProxy();
	}

}
