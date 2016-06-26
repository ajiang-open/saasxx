package com.saasxx.framework.dao.finder.impl;

import java.util.List;

import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.finder.facade.Having;
import com.saasxx.framework.dao.finder.facade.HavingPath;
import com.saasxx.framework.dao.finder.facade.QueryAppender;
import com.saasxx.framework.dao.finder.vo.QueryContent;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

public class HavingImpl implements Having {

	static Log log = Logs.getLog();

	FinderHandler finderHandler;
	FinderImpl finderImpl;

	/**
	 * 路径
	 */
	List<Object> paths = Lang.newList();

	public FinderHandler getFinderHandler() {
		return finderHandler;
	}

	public FinderImpl getFinderImpl() {
		return finderImpl;
	}

	public List<Object> getPaths() {
		return paths;
	}

	public HavingImpl(FinderHandler finderHandler, FinderImpl finderImpl) {
		super();
		this.finderHandler = finderHandler;
		this.finderImpl = finderImpl;
	}

	public QueryContent toQueryContent() {
		return finderImpl.finderRender.toHaving(finderImpl, this);
	}

	public <T> HavingPath<T> get(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		HavingPath<T> havingPath = new HavingPathImpl<T>(finderHandler,
				finderImpl, this, arg);
		paths.add(havingPath);
		return havingPath;
	}

	public QueryAppender append(String queryString, Object... args) {
		QueryAppenderImpl queryAppenderImpl = new QueryAppenderImpl(finderImpl,
				queryString, args);
		paths.add(queryAppenderImpl);
		return queryAppenderImpl;
	}

}
