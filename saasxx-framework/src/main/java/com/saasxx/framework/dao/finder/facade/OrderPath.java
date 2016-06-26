package com.saasxx.framework.dao.finder.facade;

public interface OrderPath {

	enum OrderPathType {
		asc, desc
	}

	Order asc();

	Order desc();

}
