package com.saasxx.framework.dao.finder.facade;

import com.saasxx.framework.dao.finder.vo.QueryContent;

/**
 * 查询信息渲染接口
 * 
 * @author lujijiang
 * 
 */
public interface QueryRender {
	/**
	 * 获得查询信息
	 * 
	 * @return
	 */
	public QueryContent toQueryContent();
}
