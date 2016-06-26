package com.saasxx.framework.dao.finder.impl;

import com.saasxx.framework.dao.finder.facade.SubFinder;

public class SubFinderImpl implements SubFinder {

	FinderImpl finderImpl;

	SubFinderType subFinderType;

	public SubFinderImpl(FinderImpl finderImpl, SubFinderType subFinderType) {
		this.finderImpl = finderImpl;
		this.subFinderType = subFinderType;
	}

	public FinderImpl getFinderImpl() {
		return finderImpl;
	}

	public SubFinderType getSubFinderType() {
		return subFinderType;
	}

}
