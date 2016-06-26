package com.saasxx.framework.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;

import com.saasxx.framework.Lang;

/**
 * SQL生成类
 * 
 * @author lujijiang
 *
 */
public class Sql {

	public static class NonUniqueResultException extends RuntimeException {

		private static final long serialVersionUID = 6194460514862456035L;

		public NonUniqueResultException(int resultCount) {
			super("query did not return a unique result: " + resultCount);
		}

	}

	private final static Pattern PATTERN_SINGLE_QUOTE = Pattern.compile("(')");

	private final static Pattern PATTERN_SELECT_COMMA_LAST = Pattern.compile("(,\\s*$)", Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_SELECT_BODY = Pattern.compile("(^\\s*select\\s+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern PATTERN_CONTACT = Pattern.compile("(^\\s*(,|and|or))", Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_SELECT = Pattern
			.compile(
					PATTERN_CONTACT.pattern() + "|" + PATTERN_SELECT_BODY.pattern() + "|"
							+ PATTERN_SINGLE_QUOTE.pattern() + "|" + PATTERN_SELECT_COMMA_LAST.pattern(),
					Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_FROM_BODY = Pattern.compile("(^\\s*from\\s+)", Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_FROM = Pattern.compile(
			PATTERN_CONTACT.pattern() + "|" + PATTERN_FROM_BODY.pattern() + "|" + PATTERN_SINGLE_QUOTE.pattern() + "|"
					+ PATTERN_SELECT_COMMA_LAST.pattern(),
			Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_JOIN_BODY = Pattern.compile("(^\\s*((left|right|inner|full)\\s+)?join\\s+)",
			Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_JOIN = Pattern.compile(
			PATTERN_CONTACT.pattern() + "|" + PATTERN_JOIN_BODY.pattern() + "|" + PATTERN_SINGLE_QUOTE.pattern() + "|"
					+ PATTERN_SELECT_COMMA_LAST.pattern(),
			Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_WHERE_BODY = Pattern.compile("(^\\s*where\\s+)", Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_WHERE = Pattern
			.compile(
					PATTERN_CONTACT.pattern() + "|" + PATTERN_WHERE_BODY.pattern() + "|"
							+ PATTERN_SINGLE_QUOTE.pattern() + "|" + PATTERN_SELECT_COMMA_LAST.pattern(),
					Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_ORDER_BY_BODY = Pattern.compile("(^\\s*order\\s+by\\s+)",
			Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_ORDER_BY = Pattern
			.compile(
					PATTERN_CONTACT.pattern() + "|" + PATTERN_ORDER_BY_BODY.pattern() + "|"
							+ PATTERN_SINGLE_QUOTE.pattern() + "|" + PATTERN_SELECT_COMMA_LAST.pattern(),
					Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_GROUP_BY_BODY = Pattern.compile("(^\\s*group\\s+by\\s+)",
			Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_GROUP_BY = Pattern
			.compile(
					PATTERN_CONTACT.pattern() + "|" + PATTERN_GROUP_BY_BODY.pattern() + "|"
							+ PATTERN_SINGLE_QUOTE.pattern() + "|" + PATTERN_SELECT_COMMA_LAST.pattern(),
					Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_HAVING_BODY = Pattern.compile("(^\\s*having\\s+)", Pattern.CASE_INSENSITIVE);

	private final static Pattern PATTERN_HAVING = Pattern
			.compile(
					PATTERN_CONTACT.pattern() + "|" + PATTERN_HAVING_BODY.pattern() + "|"
							+ PATTERN_SINGLE_QUOTE.pattern() + "|" + PATTERN_SELECT_COMMA_LAST.pattern(),
					Pattern.CASE_INSENSITIVE);

	private StringBuilder selectBuilder = new StringBuilder();
	private StringBuilder fromBuilder = new StringBuilder();
	private StringBuilder joinBuilder = new StringBuilder();
	private StringBuilder leftJoinBuilder = new StringBuilder();
	private StringBuilder rightJoinBuilder = new StringBuilder();
	private StringBuilder whereBuilder = new StringBuilder();
	private StringBuilder orderByBuilder = new StringBuilder();
	private StringBuilder groupByBuilder = new StringBuilder();
	private StringBuilder havingBuilder = new StringBuilder();

	private List<Object> argList = new ArrayList<Object>();

	private void generateStatement(StringBuilder sqlBuilder, Pattern pattern, String contact, String sql,
			Object... args) {
		Matcher matcher = pattern.matcher(sql);
		StringBuffer sqlBuffer = new StringBuffer();
		boolean hasContact = false;
		while (matcher.find()) {
			String group = matcher.group();
			if (PATTERN_SINGLE_QUOTE.matcher(group).matches()) {
				throw new IllegalStateException(
						String.format("SQL fragments not allowed a single quotation mark,but got:\r\n%s", sql));
			} else if (PATTERN_CONTACT.matcher(group).matches() && matcher.start() == 0) {
				matcher.appendReplacement(sqlBuffer, contact);
				hasContact = true;
			} else {
				matcher.appendReplacement(sqlBuffer, "");
			}
		}
		if (!hasContact && sqlBuilder.length() > 0) {
			sqlBuilder.append(contact);
		}
		matcher.appendTail(sqlBuffer);
		sqlBuilder.append(sqlBuffer);
		argList.addAll(Arrays.asList(args));
	}

	protected Sql select(String sql, Object... args) {
		generateStatement(selectBuilder, PATTERN_SELECT, ",", sql, args);
		return this;
	}

	protected Sql select(String sql) {
		return select(sql, Lang.EMPTY_ARRAY);
	}

	protected Sql from(String sql, Object... args) {
		generateStatement(fromBuilder, PATTERN_FROM, ",", sql, args);
		return this;
	}

	protected Sql from(String sql) {
		return from(sql, Lang.EMPTY_ARRAY);
	}

	protected Sql join(String sql, Object... args) {
		generateStatement(joinBuilder, PATTERN_JOIN, " join ", sql, args);
		return this;
	}

	protected Sql join(String sql) {
		return join(sql, Lang.EMPTY_ARRAY);
	}

	protected Sql leftjoin(String sql, Object... args) {
		generateStatement(leftJoinBuilder, PATTERN_JOIN, " left join ", sql, args);
		return this;
	}

	protected Sql leftjoin(String sql) {
		return leftjoin(sql, Lang.EMPTY_ARRAY);
	}

	protected Sql rightjoin(String sql, Object... args) {
		generateStatement(rightJoinBuilder, PATTERN_JOIN, " right join ", sql, args);
		return this;
	}

	protected Sql rightjoin(String sql) {
		return rightjoin(sql, Lang.EMPTY_ARRAY);
	}

	protected Sql where(String sql, Object... args) {
		generateStatement(whereBuilder, PATTERN_WHERE, " and ", sql, args);
		return this;
	}

	protected Sql where(String sql) {
		return where(sql, Lang.EMPTY_ARRAY);
	}

	protected Sql and(String sql, Object... args) {
		generateStatement(whereBuilder, PATTERN_WHERE, " and ", sql, args);
		return this;
	}

	protected Sql and(String sql) {
		return and(sql, Lang.EMPTY_ARRAY);
	}

	protected Sql or(String sql, Object... args) {
		generateStatement(whereBuilder, PATTERN_WHERE, " or ", sql, args);
		return this;
	}

	protected Sql or(String sql) {
		return or(sql, Lang.EMPTY_ARRAY);
	}

	protected Sql orderBy(String sql, Object... args) {
		generateStatement(orderByBuilder, PATTERN_ORDER_BY, ",", sql, args);
		return this;
	}

	protected Sql orderBy(String sql) {
		return orderBy(sql, Lang.EMPTY_ARRAY);
	}

	protected Sql groupBy(String sql, Object... args) {
		generateStatement(groupByBuilder, PATTERN_GROUP_BY, ",", sql, args);
		return this;
	}

	protected Sql groupBy(String sql) {
		return groupBy(sql, Lang.EMPTY_ARRAY);
	}

	protected Sql having(String sql, Object... args) {
		generateStatement(havingBuilder, PATTERN_HAVING, " and ", sql, args);
		return this;
	}

	protected Sql having(String sql) {
		return having(sql, Lang.EMPTY_ARRAY);
	}

	public String toString() {
		StringBuilder sqlBuilder = new StringBuilder();
		if (selectBuilder.length() > 0) {
			sqlBuilder.append("select ");
			sqlBuilder.append(selectBuilder);
		}

		if (fromBuilder.length() > 0) {
			sqlBuilder.append(" from ");
			sqlBuilder.append(fromBuilder);
		}

		if (joinBuilder.length() > 0) {
			sqlBuilder.append(" join ");
			sqlBuilder.append(joinBuilder);
		}

		if (leftJoinBuilder.length() > 0) {
			sqlBuilder.append(" left join ");
			sqlBuilder.append(leftJoinBuilder);
		}

		if (rightJoinBuilder.length() > 0) {
			sqlBuilder.append(" right join ");
			sqlBuilder.append(rightJoinBuilder);
		}

		if (whereBuilder.length() > 0) {
			sqlBuilder.append(" where ");
			sqlBuilder.append(whereBuilder);
		}

		if (orderByBuilder.length() > 0) {
			sqlBuilder.append(" order by ");
			sqlBuilder.append(orderByBuilder);
		}

		if (groupByBuilder.length() > 0) {
			sqlBuilder.append(" group by ");
			sqlBuilder.append(groupByBuilder);
		}

		if (havingBuilder.length() > 0) {
			sqlBuilder.append(" having ");
			sqlBuilder.append(havingBuilder);
		}

		return sqlBuilder.toString();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> list(Connection connection, final Class<T> type) {
		final String sql = this.toString();
		final Object[] args = argList.toArray();
		return (java.util.List<T>) Jdbcs.execute(connection, new Jdbcs.Executor() {
			public Object execute() throws SQLException {
				return executeQuery(type, sql, args);
			}
		});
	}

	public <T> List<T> list(EntityManager entityManager, final Class<T> type) {
		entityManager.flush();
		return list(entityManager.unwrap(Connection.class), type);
	}

	public <T> List<T> list(DataSource dataSource, final Class<T> type) {
		Connection connection = DataSourceUtils.getConnection(dataSource);
		try {
			return list(connection, type);
		} catch (Exception e) {
			throw Lang.unchecked(e);
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
		}
	}

	private static <T> T unique(List<T> list) throws NonUniqueResultException {
		int size = list.size();
		if (size == 0)
			return null;
		T first = list.get(0);
		for (int i = 1; i < size; i++) {
			if (list.get(i) != first) {
				throw new NonUniqueResultException(list.size());
			}
		}
		return first;
	}

	public <T> T unique(Connection connection, final Class<T> type) {
		return unique(list(connection, type));
	}

	public <T> T unique(EntityManager entityManager, final Class<T> type) {
		entityManager.flush();
		return unique(entityManager.unwrap(Connection.class), type);
	}

	public <T> T unique(DataSource dataSource, final Class<T> type) {
		Connection connection = DataSourceUtils.getConnection(dataSource);
		try {
			return unique(connection, type);
		} catch (Exception e) {
			throw Lang.unchecked(e);
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
		}
	}

	public int update(Connection connection) {
		final String sql = this.toString();
		final Object[] args = argList.toArray();
		return (Integer) Jdbcs.execute(connection, new Jdbcs.Executor() {
			public Object execute() throws SQLException {
				return executeUpdate(sql, args);
			}
		});
	}

	public int update(EntityManager entityManager) {
		entityManager.flush();
		return update(entityManager.unwrap(Connection.class));
	}

	public int update(DataSource dataSource) {
		Connection connection = DataSourceUtils.getConnection(dataSource);
		try {
			return update(connection);
		} catch (Exception e) {
			throw Lang.unchecked(e);
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
		}
	}

}
