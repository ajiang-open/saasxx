package com.saasxx.framework.dao.finder.impl;

import com.saasxx.framework.dao.finder.facade.GroupPath;

public class GroupPathImpl implements GroupPath {

	FinderHandler finderHandler;
	FinderImpl finderImpl;
	GroupImpl groupImpl;
	Object arg;

	public FinderHandler getFinderHandler() {
		return finderHandler;
	}

	public FinderImpl getFinderImpl() {
		return finderImpl;
	}

	public GroupImpl getGroupImpl() {
		return groupImpl;
	}

	public Object getArg() {
		return arg;
	}

	public GroupPathImpl(FinderHandler finderHandler, FinderImpl finderImpl,
			GroupImpl groupImpl, Object arg) {
		super();
		this.finderHandler = finderHandler;
		this.finderImpl = finderImpl;
		this.groupImpl = groupImpl;
		this.arg = arg;
	}

}
