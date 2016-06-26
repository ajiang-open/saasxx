package com.saasxx.framework.dao.finder.impl;

import java.util.List;
import java.util.Map;

import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.finder.facade.QueryAppender;
import com.saasxx.framework.dao.finder.facade.Select;
import com.saasxx.framework.dao.finder.facade.SelectPath;
import com.saasxx.framework.dao.finder.vo.EntityInfo;
import com.saasxx.framework.dao.finder.vo.QueryContent;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

/**
 * Select实现
 * 
 * @author lujijiang
 * 
 */
public class SelectImpl implements Select {

	static Log log = Logs.getLog();

	/**
	 * finder实现类
	 */
	FinderImpl finderImpl;

	/**
	 * finder处理器
	 */
	FinderHandler finderHandler;

	/**
	 * select路径
	 */
	List<Object> selectPaths = Lang.newList();
	/**
	 * 个性化映射
	 */
	Map<Long, EntityInfo<?>> entityInfoMap;

	public FinderImpl getFinderImpl() {
		return finderImpl;
	}

	public FinderHandler getFinderHandler() {
		return finderHandler;
	}

	public List<Object> getSelectPaths() {
		return selectPaths;
	}

	public Map<Long, EntityInfo<?>> getEntityInfoMap() {
		return entityInfoMap;
	}

	public SelectImpl(FinderHandler finderHandler, FinderImpl finderImpl,
			Map<Long, EntityInfo<?>> entityInfoMap) {
		super();
		this.finderImpl = finderImpl;
		this.finderHandler = finderHandler;
		this.entityInfoMap = entityInfoMap;
	}

	public QueryContent toQueryContent() {
		return finderImpl.finderRender.toSelect(finderImpl, this);
	}

	public <T> SelectPath<T> get(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		SelectPath<T> selectPath = new SelectPathImpl<T>(finderHandler,
				finderImpl, this, arg);
		selectPaths.add(selectPath);
		return selectPath;
	}

	public QueryAppender append(String queryString, Object... args) {
		QueryAppenderImpl queryAppenderImpl = new QueryAppenderImpl(finderImpl,
				queryString, args);
		selectPaths.add(queryAppenderImpl);
		return queryAppenderImpl;
	}

}
