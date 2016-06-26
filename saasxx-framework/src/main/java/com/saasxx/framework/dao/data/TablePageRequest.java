package com.saasxx.framework.dao.data;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * VSSQ专用页对象
 * 
 * @author lujijiang
 * 
 */
public class TablePageRequest extends PageRequest implements Pageable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2762528006991689398L;
	/**
	 * 统计开关
	 */
	private Boolean countSwitch;

	/**
	 * 搜索Map
	 */
	private Map<String, Object> searchMap;
	/**
	 * 搜索Map
	 */
	private Map<String, Object> globalSearchMap;

	public Boolean getCountSwitch() {
		return countSwitch;
	}

	public Map<String, Object> getSearchMap() {
		return searchMap;
	}

	public Map<String, Object> getGlobalSearchMap() {
		return globalSearchMap;
	}

	public TablePageRequest(Pageable pageable, Boolean countSwitch,
			Map<String, Object> searchMap, Map<String, Object> globalSearchMap) {
		super(pageable.getPageNumber(), pageable.getPageSize(), pageable
				.getSort());
		this.countSwitch = countSwitch;
		this.searchMap = searchMap;
		this.globalSearchMap = globalSearchMap;
	}
}
