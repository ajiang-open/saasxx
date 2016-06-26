package com.saasxx.framework.dao.finder.impl;

import java.util.List;

import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.finder.facade.Group;
import com.saasxx.framework.dao.finder.facade.GroupPath;
import com.saasxx.framework.dao.finder.facade.QueryAppender;
import com.saasxx.framework.dao.finder.vo.QueryContent;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

public class GroupImpl implements Group {

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

	public GroupImpl(FinderHandler finderHandler, FinderImpl finderImpl) {
		super();
		this.finderHandler = finderHandler;
		this.finderImpl = finderImpl;
	}

	public QueryContent toQueryContent() {
		return finderImpl.finderRender.toGroup(finderImpl, this);
	}

	public GroupPath get(Object obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		GroupPath path = new GroupPathImpl(finderHandler, finderImpl, this, arg);
		paths.add(path);
		return path;
	}

	public QueryAppender append(String queryString, Object... args) {
		QueryAppenderImpl queryAppenderImpl = new QueryAppenderImpl(finderImpl,
				queryString, args);
		paths.add(queryAppenderImpl);
		return queryAppenderImpl;
	}

}
