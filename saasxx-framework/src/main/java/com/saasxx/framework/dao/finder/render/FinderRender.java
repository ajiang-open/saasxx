package com.saasxx.framework.dao.finder.render;

import com.saasxx.framework.dao.finder.impl.FinderImpl;
import com.saasxx.framework.dao.finder.impl.GroupImpl;
import com.saasxx.framework.dao.finder.impl.HavingImpl;
import com.saasxx.framework.dao.finder.impl.OrderImpl;
import com.saasxx.framework.dao.finder.impl.SelectImpl;
import com.saasxx.framework.dao.finder.impl.WhereImpl;
import com.saasxx.framework.dao.finder.vo.QueryContent;

public interface FinderRender {

	QueryContent toFrom(FinderImpl finderImpl);

	QueryContent toWhere(FinderImpl finderImpl, WhereImpl whereImpl);

	QueryContent toSelect(FinderImpl finderImpl, SelectImpl selectImpl);

	QueryContent toOrder(FinderImpl finderImpl, OrderImpl orderImpl);

	QueryContent toGroup(FinderImpl finderImpl, GroupImpl groupImpl);

	QueryContent toHaving(FinderImpl finderImpl, HavingImpl havingImpl);

	QueryContent toSelectCount(FinderImpl finderImpl, SelectImpl selectImpl);

}
