package com.saasxx.framework.dao.finder.render.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.finder.facade.Finder;
import com.saasxx.framework.dao.finder.facade.SelectPath.SelectPathType;
import com.saasxx.framework.dao.finder.impl.FinderImpl;
import com.saasxx.framework.dao.finder.impl.GroupImpl;
import com.saasxx.framework.dao.finder.impl.GroupPathImpl;
import com.saasxx.framework.dao.finder.impl.HavingImpl;
import com.saasxx.framework.dao.finder.impl.HavingPathImpl;
import com.saasxx.framework.dao.finder.impl.JoinPathImpl;
import com.saasxx.framework.dao.finder.impl.OrderImpl;
import com.saasxx.framework.dao.finder.impl.OrderPathImpl;
import com.saasxx.framework.dao.finder.impl.QueryAppenderImpl;
import com.saasxx.framework.dao.finder.impl.SelectImpl;
import com.saasxx.framework.dao.finder.impl.SelectPathImpl;
import com.saasxx.framework.dao.finder.impl.SubFinderImpl;
import com.saasxx.framework.dao.finder.impl.WhereImpl;
import com.saasxx.framework.dao.finder.impl.WherePathImpl;
import com.saasxx.framework.dao.finder.render.FinderRender;
import com.saasxx.framework.dao.finder.vo.EntityInfo;
import com.saasxx.framework.dao.finder.vo.FromInfo;
import com.saasxx.framework.dao.finder.vo.PathInfo;
import com.saasxx.framework.dao.finder.vo.QueryContent;

/**
 * JPA查询渲染器，兼容Hibernate、Toplink等
 * 
 * @author lujijiang
 * 
 */
public class JpaFinderRender implements FinderRender {

	public JpaFinderRender() {
	}

	public QueryContent toFrom(FinderImpl finderImpl) {
		if (!finderImpl.getCurrentFromInfos().isEmpty()) {
			QueryContent queryContent = new QueryContent();
			int count = 0;
			for (FromInfo fromInfo : finderImpl.getCurrentFromInfos().values()) {
				if (count > 0) {
					queryContent.getQueryBuilder().append(',');
				}
				queryContent.getQueryBuilder().append(
						fromInfo.getEntityInfo().getType().getCanonicalName());
				queryContent.getQueryBuilder().append(" as ");
				queryContent.getQueryBuilder().append(
						fromInfo.getEntityInfo().getAlias());
				QueryContent joinQueryContent = toJoin(finderImpl,
						fromInfo.getJoinPaths());
				if (joinQueryContent != null) {
					queryContent.append(joinQueryContent);
				}
				count++;
			}
			return queryContent;
		}
		return null;
	}

	private QueryContent toJoin(FinderImpl finderImpl,
			List<JoinPathImpl<?>> joinPaths) {
		if (joinPaths == null || joinPaths.isEmpty()) {
			return null;
		}
		QueryContent queryContent = new QueryContent();
		for (JoinPathImpl<?> joinPathImpl : joinPaths) {
			QueryContent joinPathQueryContent = toJoinPath(finderImpl,
					joinPathImpl);
			if (joinPathQueryContent != null) {
				queryContent.append(" ");
				queryContent.append(joinPathQueryContent);
			}
		}
		return queryContent;
	}

	private QueryContent toJoinPath(FinderImpl finderImpl,
			JoinPathImpl<?> joinPathImpl) {
		QueryContent queryContent = new QueryContent();
		queryContent.append(joinPathImpl.getJoinPathType().name());
		queryContent.append(" join ");
		queryContent.append(toPathArg(finderImpl, null,
				joinPathImpl.getPathInfo()));
		queryContent.append(" as ");
		queryContent.append(joinPathImpl.getEntityInfo().getAlias());
		QueryContent whereQueryContent = toWhere(finderImpl,
				joinPathImpl.getWhereImpl());
		if (whereQueryContent != null) {
			queryContent.append(" on ");
			queryContent.append(whereQueryContent);
		}
		return queryContent;
	}

	public QueryContent toWhere(FinderImpl finderImpl, WhereImpl whereImpl) {
		QueryContent queryContent = new QueryContent();
		int count = 0;
		for (Object wherePath : whereImpl.getWherePaths()) {
			QueryContent pathQueryContent = null;

			if (wherePath instanceof WherePathImpl) {
				WherePathImpl<?> wherePathImpl = (WherePathImpl<?>) wherePath;
				pathQueryContent = toWherePath(finderImpl, wherePathImpl);
				if (pathQueryContent != null) {
					if (count > 0) {
						queryContent.getQueryBuilder().append(" ");
						queryContent.getQueryBuilder().append(
								whereImpl.getType().name());
						queryContent.getQueryBuilder().append(" ");
					}
					queryContent.append(pathQueryContent);
					count++;
				}
			} else if (wherePath instanceof WhereImpl) {
				WhereImpl subWhereImpl = (WhereImpl) wherePath;
				pathQueryContent = toWhere(finderImpl, subWhereImpl);
				if (pathQueryContent != null) {
					if (count > 0) {
						queryContent.getQueryBuilder().append(" ");
						queryContent.getQueryBuilder().append(
								whereImpl.getType().name());
						queryContent.getQueryBuilder().append(" ");
					}
					queryContent.append("(");
					queryContent.append(pathQueryContent);
					queryContent.append(")");
					count++;
				}
			} else if (wherePath instanceof QueryAppenderImpl) {
				QueryAppenderImpl queryAppenderImpl = (QueryAppenderImpl) wherePath;
				pathQueryContent = queryAppenderImpl.toQueryContent();
				if (pathQueryContent != null) {
					if (count > 0) {
						queryContent.getQueryBuilder().append(" ");
						queryContent.getQueryBuilder().append(
								whereImpl.getType().name());
						queryContent.getQueryBuilder().append(" ");
					}
					queryContent.append(pathQueryContent);
					count++;
				}
			} else if (wherePath instanceof SubFinderImpl) {
				SubFinderImpl subFinderImpl = (SubFinderImpl) wherePath;
				if (subFinderImpl.getFinderImpl().getSelectImpl()
						.getSelectPaths().isEmpty()) {
					throw new IllegalStateException(
							String.format(
									"The sub finder must be contains one and only one select path,this sub finder is:%s",
									subFinderImpl.getFinderImpl()
											.toQueryContent()));
				}
				pathQueryContent = subFinderImpl.getFinderImpl()
						.toQueryContent();
				if (pathQueryContent != null) {
					if (count > 0) {
						queryContent.getQueryBuilder().append(" ");
						queryContent.getQueryBuilder().append(
								whereImpl.getType().name());
						queryContent.getQueryBuilder().append(" ");
					}
					switch (subFinderImpl.getSubFinderType()) {
					case exists:
						queryContent.append("exists");
						break;
					case notExists:
						queryContent.append("not exists");
						break;
					case all:
						queryContent.append("all");
						break;
					case any:
						queryContent.append("any");
						break;
					case some:
						queryContent.append("some");
						break;
					default:
						continue;
					}
					queryContent.append("(");
					queryContent.append(pathQueryContent);
					queryContent.append(")");
					count++;
				}
			}

		}

		if (count > 0) {
			return queryContent;
		}
		return null;
	}

	private QueryContent toWherePath(FinderImpl finderImpl,
			WherePathImpl<?> wherePathImpl) {
		QueryContent queryContent = null;
		switch (wherePathImpl.getWherePathType()) {
		case equal: {
			queryContent = toWherePathOperate(finderImpl, wherePathImpl, "=");
			break;
		}
		case notEqual: {
			queryContent = toWherePathOperate(finderImpl, wherePathImpl, "<>");
			break;
		}
		case like: {
			queryContent = toWherePathOperate(finderImpl, wherePathImpl, "like");
			break;
		}
		case notLike: {
			queryContent = toWherePathOperate(finderImpl, wherePathImpl,
					"not like");
			break;
		}
		case ilike: {
			queryContent = toWherePathOperate(finderImpl, wherePathImpl,
					"like", true);
			break;
		}
		case notIlike: {
			queryContent = toWherePathOperate(finderImpl, wherePathImpl,
					"not like", true);
			break;
		}

		case greatEqual: {
			queryContent = toWherePathOperate(finderImpl, wherePathImpl, ">=");
			break;
		}
		case greatThan: {
			queryContent = toWherePathOperate(finderImpl, wherePathImpl, ">");
			break;
		}

		case lessEqual: {
			queryContent = toWherePathOperate(finderImpl, wherePathImpl, "<=");
			break;
		}
		case lessThan: {
			queryContent = toWherePathOperate(finderImpl, wherePathImpl, "<");
			break;
		}

		case between: {
			queryContent = toWherePathBetween(finderImpl, wherePathImpl, true);
			break;
		}

		case notBetween: {
			queryContent = toWherePathBetween(finderImpl, wherePathImpl, false);
			break;
		}

		case isNull: {
			queryContent = toWherePathNull(finderImpl, wherePathImpl, true);
			break;
		}

		case isNotNull: {
			queryContent = toWherePathNull(finderImpl, wherePathImpl, false);
			break;
		}

		case in: {
			queryContent = toWherePathIn(finderImpl, wherePathImpl, true);
			break;
		}

		case notIn: {
			queryContent = toWherePathIn(finderImpl, wherePathImpl, false);
			break;
		}

		default:
			;
		}
		return queryContent;
	}

	private QueryContent toWherePathIn(FinderImpl finderImpl,
			WherePathImpl<?> wherePathImpl, boolean flag) {
		List<Object> argList = toList(wherePathImpl.getArgs());
		if (argList.isEmpty()) {
			return null;
		}
		QueryContent queryContent = new QueryContent();
		QueryContent leftQueryContent = toPathArg(finderImpl, wherePathImpl
				.getWhereImpl().getEntityInfoMap(), wherePathImpl.getLeft());
		queryContent.append(leftQueryContent);
		if (!flag) {
			queryContent.append(" not");
		}
		queryContent.append(" in (");
		for (int i = 0; i < argList.size(); i++) {
			if (i > 0) {
				queryContent.append(",");
			}
			queryContent.append(toPathArg(finderImpl, wherePathImpl
					.getWhereImpl().getEntityInfoMap(), argList.get(i)));
		}
		queryContent.append(")");
		return queryContent;
	}

	private List<Object> toList(Object... a) {
		List<Object> list = new LinkedList<Object>();
		if (a == null) {
			list.add(a);
			return list;
		}
		for (Object o : a) {
			if (o == null) {
				list.add(o);
			} else if (o instanceof Collection) {
				list.addAll(toList(((Collection<?>) o).toArray()));
			} else if (o.getClass().isArray()) {
				for (int i = 0; i < Array.getLength(o); i++) {
					list.add(Array.get(o, i));
				}
			} else {
				list.add(o);
			}
		}
		return list;
	}

	private QueryContent toWherePathNull(FinderImpl finderImpl,
			WherePathImpl<?> wherePathImpl, boolean flag) {
		QueryContent queryContent = new QueryContent();
		QueryContent leftQueryContent = toPathArg(finderImpl, wherePathImpl
				.getWhereImpl().getEntityInfoMap(), wherePathImpl.getLeft());
		queryContent.append(leftQueryContent);
		queryContent.append(" is ");
		if (!flag) {
			queryContent.append("not ");
		}
		queryContent.append("null");
		return queryContent;
	}

	/**
	 * 将between类型的路径进行渲染
	 * 
	 * @param finderImpl
	 * @param wherePathImpl
	 * @param queryContent
	 * @param flag
	 */
	private QueryContent toWherePathBetween(FinderImpl finderImpl,
			WherePathImpl<?> wherePathImpl, boolean flag) {
		QueryContent queryContent = new QueryContent();
		QueryContent rightQueryContent1 = null;
		QueryContent rightQueryContent2 = null;
		Object right1 = wherePathImpl.getArgs()[0];
		Object right2 = wherePathImpl.getArgs()[1];
		if (Lang.isEmpty(right1) || Lang.isEmpty(right2)) {
			if (wherePathImpl.isIfExist()) {
				return null;
			}
		}
		rightQueryContent1 = right1 == null ? null : toPathArg(finderImpl,
				wherePathImpl.getWhereImpl().getEntityInfoMap(), right1);
		rightQueryContent2 = right2 == null ? null : toPathArg(finderImpl,
				wherePathImpl.getWhereImpl().getEntityInfoMap(), right2);
		QueryContent leftQueryContent = toPathArg(finderImpl, wherePathImpl
				.getWhereImpl().getEntityInfoMap(), wherePathImpl.getLeft());
		queryContent.append(leftQueryContent);
		if (rightQueryContent1 != null && rightQueryContent2 != null) {
			if (!flag) {
				queryContent.append(" not");
			}
			queryContent.append(" between ");
			queryContent.append(rightQueryContent1);
			queryContent.append(" and ");
			queryContent.append(rightQueryContent2);
		} else if (rightQueryContent1 == null && rightQueryContent2 == null) {
			queryContent.append(" is null");
		} else if (rightQueryContent1 == null) {
			if (!flag) {
				queryContent.append(" >= ");
			} else {
				queryContent.append(" <= ");
			}
			queryContent.append(rightQueryContent2);
		} else {
			if (!flag) {
				queryContent.append(" <= ");
			} else {
				queryContent.append(" >= ");
			}
			queryContent.append(rightQueryContent1);
		}
		return queryContent;
	}

	/**
	 * 将逻辑运算符路径进行渲染
	 * 
	 * @param finderImpl
	 *            finder 对象
	 * @param wherePathImpl
	 *            wherePath路径实现对象
	 * @param queryContent
	 *            父查询内容对象
	 * @param operate
	 *            操作符
	 */
	private QueryContent toWherePathOperate(FinderImpl finderImpl,
			WherePathImpl<?> wherePathImpl, String operate) {
		return toWherePathOperate(finderImpl, wherePathImpl, operate, false);
	}

	/**
	 * 将逻辑运算符路径进行渲染
	 * 
	 * @param finderImpl
	 *            finder 对象
	 * @param wherePathImpl
	 *            wherePath路径实现对象
	 * @param queryContent
	 *            父查询内容对象
	 * @param operate
	 *            操作符
	 * @param caseInsensitive
	 *            true则忽略大小写
	 */
	private QueryContent toWherePathOperate(FinderImpl finderImpl,
			WherePathImpl<?> wherePathImpl, String operate,
			boolean caseInsensitive) {
		QueryContent queryContent = new QueryContent();
		QueryContent rightQueryContent = null;
		Object right = wherePathImpl.getArgs()[0];
		if (Lang.isEmpty(right)) {
			if (wherePathImpl.isIfExist()) {
				return null;
			}
		} else {
			rightQueryContent = toPathArg(finderImpl, wherePathImpl
					.getWhereImpl().getEntityInfoMap(), right);
		}
		QueryContent leftQueryContent = toPathArg(finderImpl, wherePathImpl
				.getWhereImpl().getEntityInfoMap(), wherePathImpl.getLeft());
		if (caseInsensitive) {
			queryContent.append("upper(");
		}
		queryContent.append(leftQueryContent);
		if (caseInsensitive) {
			queryContent.append(")");
		}
		queryContent.append(" ");
		queryContent.append(operate);
		queryContent.append(" ");
		if (rightQueryContent != null) {
			if (caseInsensitive) {
				queryContent.append("upper(");
			}
			queryContent.append(rightQueryContent);
			if (caseInsensitive) {
				queryContent.append(")");
			}
		} else {
			queryContent.append("null");
		}
		return queryContent;
	}

	private QueryContent toPathArg(FinderImpl finderImpl,
			Map<Long, EntityInfo<?>> entityInfoMap, Object arg) {
		if (arg == null) {
			throw new NullPointerException(
					"The arg should be null for where path");
		}
		QueryContent queryContent = new QueryContent();
		if (arg instanceof PathInfo) {
			PathInfo pathInfo = (PathInfo) arg;
			FromInfo fromInfo = finderImpl.getCurrentFromInfos().get(
					pathInfo.getRootKey());
			if (fromInfo == null) {
				fromInfo = finderImpl.getParentFromInfos().get(
						pathInfo.getRootKey());
			}
			EntityInfo<?> entityInfo = null;
			if (fromInfo == null && entityInfoMap != null) {
				entityInfo = entityInfoMap.get(pathInfo.getRootKey());

			} else if (fromInfo != null) {
				entityInfo = fromInfo.getEntityInfo();
			}
			if (entityInfo == null) {
				throw new IllegalArgumentException(String.format(
						"The info path %s root proxy instance is not valid",
						pathInfo));
			}
			queryContent.append(entityInfo.getAlias());
			queryContent.append(".");
			queryContent.append(pathInfo.getPathBuilder().toString());
		} else if (arg instanceof FinderImpl) {
			if (((FinderImpl) arg).getSelectImpl().getSelectPaths().isEmpty()) {
				throw new IllegalStateException(
						String.format(
								"The arg finder must be contains one and only one select path,this arg finder is:%s",
								((Finder) arg).toQueryContent()));
			}
			queryContent.append("(");
			queryContent.append(((Finder) arg).toQueryContent());
			queryContent.append(")");
		} else {
			try {
				String alias = finderImpl.alias(arg);
				queryContent.append(alias);
			} catch (IllegalStateException e) {
				String name = finderImpl.getFinderHandler().generateParamName();
				Map<String, Object> argMap = new ConcurrentHashMap<String, Object>();
				argMap.put(name, arg);
				queryContent.append(":".concat(name), argMap);
			}
		}
		return queryContent;
	}

	public QueryContent toSelect(FinderImpl finderImpl, SelectImpl selectImpl) {
		QueryContent queryContent = new QueryContent();
		int count = 0;
		for (Object selectPath : selectImpl.getSelectPaths()) {
			QueryContent pathQueryContent = null;
			if (selectPath instanceof SelectPathImpl<?>) {
				SelectPathImpl<?> selectPathImpl = (SelectPathImpl<?>) selectPath;
				SelectPathImpl<?> topSelectPathImpl = selectPathImpl;
				while (topSelectPathImpl.getParentSelectPathImpl() != null) {
					topSelectPathImpl = topSelectPathImpl
							.getParentSelectPathImpl();
				}
				pathQueryContent = toSelectPath(finderImpl, topSelectPathImpl);
			} else if (selectPath instanceof QueryAppenderImpl) {
				QueryAppenderImpl queryAppenderImpl = (QueryAppenderImpl) selectPath;
				pathQueryContent = queryAppenderImpl.toQueryContent();
			}
			if (pathQueryContent != null) {
				if (count > 0) {
					if (pathQueryContent.getQueryString().startsWith(
							SelectPathType.distinct.name().concat("("))) {
						throw new IllegalStateException(String.format(
								"Distinct只能约束第一个Select选择列，这里是第%d个", count + 1));
					}
					queryContent.getQueryBuilder().append(",");
				}
				queryContent.append(pathQueryContent);
				count++;
			}
		}
		if (count == 0) {
			return null;
		}
		return queryContent;
	}

	public QueryContent toSelectCount(FinderImpl finderImpl,
			SelectImpl selectImpl) {
		for (Object selectPath : selectImpl.getSelectPaths()) {
			QueryContent pathQueryContent = null;
			if (selectPath instanceof SelectPathImpl<?>) {
				SelectPathImpl<?> selectPathImpl = (SelectPathImpl<?>) selectPath;
				SelectPathImpl<?> topSelectPathImpl = selectPathImpl;
				while (topSelectPathImpl.getParentSelectPathImpl() != null) {
					topSelectPathImpl = topSelectPathImpl
							.getParentSelectPathImpl();
				}
				pathQueryContent = toSelectPath(finderImpl, topSelectPathImpl);
			} else if (selectPath instanceof QueryAppenderImpl) {
				QueryAppenderImpl queryAppenderImpl = (QueryAppenderImpl) selectPath;
				pathQueryContent = queryAppenderImpl.toQueryContent();
			}
			if (pathQueryContent != null) {
				return pathQueryContent;
			}
		}
		return new QueryContent().append("*");
	}

	private QueryContent toSelectPath(FinderImpl finderImpl,
			SelectPathImpl<?> selectPathImpl) {
		QueryContent queryContent = new QueryContent();
		if (selectPathImpl.getSelectPathType() != null) {
			queryContent.append(selectPathImpl.getSelectPathType().name());
			queryContent.append("(");
		}
		Object arg = selectPathImpl.getArg();
		if (arg == null) {
			queryContent.append("null");
		} else if (arg instanceof SelectPathImpl) {
			queryContent.append(toSelectPath(finderImpl,
					(SelectPathImpl<?>) arg));
		} else {
			queryContent.append(toPathArg(finderImpl, selectPathImpl
					.getSelectImpl().getEntityInfoMap(), arg));
		}
		if (selectPathImpl.getSelectPathType() != null) {
			queryContent.append(")");
		}
		return queryContent;
	}

	public QueryContent toOrder(FinderImpl finderImpl, OrderImpl orderImpl) {
		QueryContent queryContent = new QueryContent();
		int count = 0;
		for (Object path : orderImpl.getPaths()) {
			QueryContent pathQueryContent;
			if (path instanceof OrderPathImpl) {
				OrderPathImpl orderPathImpl = (OrderPathImpl) path;
				pathQueryContent = toOrderPath(finderImpl, orderPathImpl);
				if (pathQueryContent != null) {
					if (count > 0) {
						queryContent.getQueryBuilder().append(",");
					}
					queryContent.append(pathQueryContent);
					count++;
				}
			} else if (path instanceof QueryAppenderImpl) {
				QueryAppenderImpl queryAppenderImpl = (QueryAppenderImpl) path;
				pathQueryContent = queryAppenderImpl.toQueryContent();
				if (pathQueryContent != null) {
					if (count > 0) {
						queryContent.getQueryBuilder().append(",");
					}
					queryContent.append(pathQueryContent);
					count++;
				}
			}
		}
		if (count == 0) {
			return null;
		}
		return queryContent;
	}

	private QueryContent toOrderPath(FinderImpl finderImpl,
			OrderPathImpl orderPathImpl) {
		QueryContent queryContent = new QueryContent();
		queryContent.append(toPathArg(finderImpl, finderImpl.getSelectImpl()
				.getEntityInfoMap(), orderPathImpl.getArg()));
		queryContent.append(" ");
		queryContent.append(orderPathImpl.getOrderPathType().name());
		return queryContent;
	}

	public QueryContent toGroup(FinderImpl finderImpl, GroupImpl groupImpl) {
		QueryContent queryContent = new QueryContent();
		int count = 0;
		for (Object path : groupImpl.getPaths()) {
			QueryContent pathQueryContent;
			if (path instanceof GroupPathImpl) {
				GroupPathImpl groupPathImpl = (GroupPathImpl) path;
				pathQueryContent = toGroupPath(finderImpl, groupPathImpl);
				if (pathQueryContent != null) {
					if (count > 0) {
						queryContent.getQueryBuilder().append(",");
					}
					queryContent.append(pathQueryContent);
					count++;
				}
			} else if (path instanceof QueryAppenderImpl) {
				QueryAppenderImpl queryAppenderImpl = (QueryAppenderImpl) path;
				pathQueryContent = queryAppenderImpl.toQueryContent();
				if (pathQueryContent != null) {
					if (count > 0) {
						queryContent.getQueryBuilder().append(",");
					}
					queryContent.append(pathQueryContent);
					count++;
				}
			}
		}
		if (count == 0) {
			return null;
		}
		return queryContent;
	}

	private QueryContent toGroupPath(FinderImpl finderImpl,
			GroupPathImpl groupPathImpl) {
		QueryContent queryContent = new QueryContent();
		queryContent.append(toPathArg(finderImpl, finderImpl.getSelectImpl()
				.getEntityInfoMap(), groupPathImpl.getArg()));
		return queryContent;
	}

	public QueryContent toHaving(FinderImpl finderImpl, HavingImpl havingImpl) {
		QueryContent queryContent = new QueryContent();
		int count = 0;
		for (Object path : havingImpl.getPaths()) {
			QueryContent pathQueryContent;
			if (path instanceof HavingPathImpl<?>) {
				HavingPathImpl<?> groupPathImpl = (HavingPathImpl<?>) path;
				HavingPathImpl<?> topHavingPathImpl = groupPathImpl;
				while (topHavingPathImpl.getParentHavingPathImpl() != null) {
					topHavingPathImpl = topHavingPathImpl
							.getParentHavingPathImpl();
				}
				pathQueryContent = toHavingPath(finderImpl, topHavingPathImpl);
				if (pathQueryContent != null) {
					if (count > 0) {
						queryContent.getQueryBuilder().append(" and ");
					}
					queryContent.append(pathQueryContent);
					count++;
				}
			} else if (path instanceof QueryAppenderImpl) {
				QueryAppenderImpl queryAppenderImpl = (QueryAppenderImpl) path;
				pathQueryContent = queryAppenderImpl.toQueryContent();
				if (pathQueryContent != null) {
					if (count > 0) {
						queryContent.getQueryBuilder().append(" and ");
					}
					queryContent.append(pathQueryContent);
					count++;
				}
			}
		}
		if (count == 0) {
			return null;
		}
		return queryContent;
	}

	private QueryContent toHavingPath(FinderImpl finderImpl,
			HavingPathImpl<?> havingPathImpl) {
		QueryContent queryContent = new QueryContent();
		QueryContent selectQueryContent = toHavingSelectPath(finderImpl,
				havingPathImpl);
		queryContent.append(selectQueryContent);
		QueryContent whereQueryContent = toHavingWherePath(finderImpl,
				havingPathImpl);
		if (whereQueryContent == null) {
			return null;
		}
		queryContent.append(whereQueryContent);
		return queryContent;
	}

	private QueryContent toHavingWherePath(FinderImpl finderImpl,
			HavingPathImpl<?> havingPathImpl) {
		QueryContent whereQueryContent = null;
		switch (havingPathImpl.getWherePathType()) {
		case equal:
			whereQueryContent = toHavingWhereOperatePath(finderImpl,
					havingPathImpl, "=");
			break;
		case notEqual:
			whereQueryContent = toHavingWhereOperatePath(finderImpl,
					havingPathImpl, "<>");
			break;
		case greatEqual:
			whereQueryContent = toHavingWhereOperatePath(finderImpl,
					havingPathImpl, ">=");
			break;
		case greatThan:
			whereQueryContent = toHavingWhereOperatePath(finderImpl,
					havingPathImpl, ">");
			break;
		case lessEqual:
			whereQueryContent = toHavingWhereOperatePath(finderImpl,
					havingPathImpl, "<=");
			break;
		case lessThan:
			whereQueryContent = toHavingWhereOperatePath(finderImpl,
					havingPathImpl, "<");
			break;
		default:
			break;
		}
		return whereQueryContent;
	}

	private QueryContent toHavingWhereOperatePath(FinderImpl finderImpl,
			HavingPathImpl<?> havingPathImpl, String operate) {
		QueryContent rightQueryContent = null;
		Object right = havingPathImpl.getArgs()[0];
		if (Lang.isEmpty(right)) {
			if (havingPathImpl.isIfExist()) {
				return null;
			}
		} else {
			rightQueryContent = toPathArg(finderImpl, null, right);
		}
		QueryContent queryContent = new QueryContent();
		queryContent.append(" ");
		queryContent.append(operate);
		queryContent.append(" ");
		queryContent.append(rightQueryContent);
		return queryContent;
	}

	private QueryContent toHavingSelectPath(FinderImpl finderImpl,
			HavingPathImpl<?> havingPathImpl) {
		QueryContent queryContent = new QueryContent();
		if (havingPathImpl.getSelectPathType() != null) {
			queryContent.append(havingPathImpl.getSelectPathType().name());
			queryContent.append("(");
		}
		Object arg = havingPathImpl.getLeft();
		if (arg == null) {
			queryContent.append("null");
		} else if (arg instanceof HavingPathImpl) {
			queryContent.append(toHavingSelectPath(finderImpl,
					(HavingPathImpl<?>) arg));
		} else {
			queryContent.append(toPathArg(finderImpl, finderImpl
					.getSelectImpl().getEntityInfoMap(), arg));
		}
		if (havingPathImpl.getSelectPathType() != null) {
			queryContent.append(")");
		}
		return queryContent;
	}

}
