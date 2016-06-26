package com.saasxx.framework.dao.finder.vo;

import java.util.List;

import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.finder.impl.JoinPathImpl;

/**
 * From信息
 * 
 * @author lujijiang
 * 
 */
public class FromInfo {
	/**
	 * 实体信息
	 */
	EntityInfo<?> entityInfo;

	/**
	 * Join信息
	 */
	List<JoinPathImpl<?>> joinPaths = Lang.newList();

	public EntityInfo<?> getEntityInfo() {
		return entityInfo;
	}

	public List<JoinPathImpl<?>> getJoinPaths() {
		return joinPaths;
	}

	public FromInfo(EntityInfo<?> entityInfo) {
		super();
		this.entityInfo = entityInfo;
	}

}
