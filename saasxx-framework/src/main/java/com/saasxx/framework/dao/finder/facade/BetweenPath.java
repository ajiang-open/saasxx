package com.saasxx.framework.dao.finder.facade;

/**
 * between类型路径
 * 
 * @author lujijiang
 * 
 * @param <T>
 */
public interface BetweenPath<T> {

	Where and(T obj);

	Where and(Finder subFinder);

}
