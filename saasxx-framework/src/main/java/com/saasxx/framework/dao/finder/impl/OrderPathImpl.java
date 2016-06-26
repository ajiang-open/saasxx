package com.saasxx.framework.dao.finder.impl;

import com.saasxx.framework.dao.finder.facade.Order;
import com.saasxx.framework.dao.finder.facade.OrderPath;

public class OrderPathImpl implements OrderPath {

	FinderHandler finderHandler;
	FinderImpl finderImpl;
	OrderImpl orderImpl;
	Object arg;
	OrderPathType orderPathType = OrderPathType.asc;

	public FinderHandler getFinderHandler() {
		return finderHandler;
	}

	public FinderImpl getFinderImpl() {
		return finderImpl;
	}

	public OrderImpl getOrderImpl() {
		return orderImpl;
	}

	public Object getArg() {
		return arg;
	}

	public OrderPathType getOrderPathType() {
		return orderPathType;
	}

	public OrderPathImpl(FinderHandler finderHandler, FinderImpl finderImpl,
			OrderImpl orderImpl, Object arg) {
		super();
		this.finderHandler = finderHandler;
		this.finderImpl = finderImpl;
		this.orderImpl = orderImpl;
		this.arg = arg;
	}

	public Order asc() {
		orderPathType = OrderPathType.asc;
		return orderImpl;
	}

	public Order desc() {
		orderPathType = OrderPathType.desc;
		return orderImpl;
	}

}
