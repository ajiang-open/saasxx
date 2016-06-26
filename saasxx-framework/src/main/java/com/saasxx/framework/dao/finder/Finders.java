package com.saasxx.framework.dao.finder;

import com.saasxx.framework.dao.finder.facade.Finder;
import com.saasxx.framework.dao.finder.impl.FinderHandler;
import com.saasxx.framework.dao.finder.impl.FinderImpl;
import com.saasxx.framework.dao.finder.render.impl.JpaFinderRender;

/**
 * Finder工具类
 * 
 * @author lujijiang
 * 
 */
public class Finders {

	public static Finder newFinder() {
		return new FinderImpl(new FinderHandler(), new JpaFinderRender());
	}

}
