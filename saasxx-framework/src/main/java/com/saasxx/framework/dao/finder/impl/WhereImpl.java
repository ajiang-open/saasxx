package com.saasxx.framework.dao.finder.impl;

import java.util.List;
import java.util.Map;

import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.finder.facade.And;
import com.saasxx.framework.dao.finder.facade.Finder;
import com.saasxx.framework.dao.finder.facade.Or;
import com.saasxx.framework.dao.finder.facade.QueryAppender;
import com.saasxx.framework.dao.finder.facade.Where;
import com.saasxx.framework.dao.finder.facade.WherePath;
import com.saasxx.framework.dao.finder.facade.SubFinder.SubFinderType;
import com.saasxx.framework.dao.finder.vo.EntityInfo;
import com.saasxx.framework.dao.finder.vo.QueryContent;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

/**
 * Where实现
 * 
 * @author lujijiang
 * 
 */
public class WhereImpl implements Where, Or, And {

	static Log log = Logs.getLog();

	/**
	 * 连接类型
	 */
	WhereType type;

	/**
	 * finder实现类
	 */
	FinderImpl finderImpl;

	/**
	 * finder处理器
	 */
	FinderHandler finderHandler;

	/**
	 * where路径
	 */
	List<Object> wherePaths = Lang.newList();

	/**
	 * 个性化映射
	 */
	Map<Long, EntityInfo<?>> entityInfoMap;

	public WhereType getType() {
		return type;
	}

	public FinderImpl getFinderImpl() {
		return finderImpl;
	}

	public List<Object> getWherePaths() {
		return wherePaths;
	}

	public FinderHandler getFinderHandler() {
		return finderHandler;
	}

	public Map<Long, EntityInfo<?>> getEntityInfoMap() {
		return entityInfoMap;
	}

	public WhereImpl(FinderHandler finderHandler, FinderImpl finderImpl,
			WhereType type, Map<Long, EntityInfo<?>> entityInfoMap) {
		this.finderImpl = finderImpl;
		this.finderHandler = finderHandler;
		this.type = type;
		this.entityInfoMap = entityInfoMap;
	}

	public QueryContent toQueryContent() {
		return finderImpl.finderRender.toWhere(finderImpl, this);
	}

	public <T> WherePath<T> get(T obj) {
		Object arg = finderHandler.getPathInfo();
		arg = arg == null ? obj : arg;
		WherePathImpl<T> wherePath = new WherePathImpl<T>(finderHandler,
				finderImpl, this, arg);
		wherePaths.add(wherePath);
		return wherePath;
	}

	public And and() {
		WhereImpl subWhereImpl = new WhereImpl(finderHandler, finderImpl,
				Where.WhereType.and, entityInfoMap);
		wherePaths.add(subWhereImpl);
		return subWhereImpl;
	}

	public Or or() {
		WhereImpl subWhereImpl = new WhereImpl(finderHandler, finderImpl,
				Where.WhereType.or, entityInfoMap);
		wherePaths.add(subWhereImpl);
		return subWhereImpl;
	}

	public Where exists(Finder subFinder) {
		SubFinderImpl subFinderImpl = new SubFinderImpl((FinderImpl) subFinder,
				SubFinderType.exists);
		wherePaths.add(subFinderImpl);
		return this;
	}

	public Where notExists(Finder subFinder) {
		SubFinderImpl subFinderImpl = new SubFinderImpl((FinderImpl) subFinder,
				SubFinderType.notExists);
		wherePaths.add(subFinderImpl);
		return this;
	}

	public QueryAppender append(String queryString, Object... args) {
		QueryAppenderImpl queryAppenderImpl = new QueryAppenderImpl(finderImpl,
				queryString, args);
		wherePaths.add(queryAppenderImpl);
		return queryAppenderImpl;
	}

}
