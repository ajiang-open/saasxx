package com.saasxx.framework.dao.finder.vo;

import org.apache.commons.lang3.StringUtils;

import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.finder.impl.FinderHandler;

/**
 * 实体信息
 * 
 * @author lujijiang
 * 
 */
public class EntityInfo<T> {
	/**
	 * 实体类型
	 */
	Class<T> type;
	/**
	 * 实体别名
	 */
	String alias;
	/**
	 * 实体模型
	 */
	T proxy;
	/**
	 * 实体key
	 */
	Long key;

	public EntityInfo(FinderHandler finderHandler, Class<T> type, T proxy) {
		this.type = type;
		this.proxy = proxy;
		key = Lang.identityHashCode(proxy);
		alias = StringUtils.uncapitalize(type.getSimpleName());
		int index = finderHandler.generateEntityIndex(type);
		if (index > 0) {
			alias = alias.concat("_").concat(String.valueOf(index));
		}

	}

	public Class<T> getType() {
		return type;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public T getProxy() {
		return proxy;
	}

	public Long getKey() {
		return key;
	}

}
