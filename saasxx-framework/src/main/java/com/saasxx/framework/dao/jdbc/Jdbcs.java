package com.saasxx.framework.dao.jdbc;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.Assert;

import com.saasxx.framework.Lang;

/**
 * JDBC工具类
 * 
 * @author lujijiang
 *
 */
public class Jdbcs {

	public static final int procedureColumnReturn = DatabaseMetaData.procedureColumnReturn;
	public static final int procedureColumnInOut = DatabaseMetaData.procedureColumnInOut;
	public static final int procedureColumnOut = DatabaseMetaData.procedureColumnOut;
	public static final int procedureColumnIn = DatabaseMetaData.procedureColumnIn;

	/**
	 * 用于清除SQL注释的正则表达式
	 */
	private final static Pattern SQL_COMMENT_PATTERN = Pattern
			.compile("(?ms)('(([^'])*('')?([^'])*)*')|--.*?$|/\\*.*?\\*/");

	private final static Pattern SQL_DML_TABLE_PATTERN = Pattern
			.compile("(?i)((?<=^insert\\sinto\\s)|(?<=^update\\s)|(?<=^delete\\sfrom\\s))(`?)[a-z0-9_$]+(`?)");

	/**
	 * 分隔SQL文本为有效的SQL数组
	 * 
	 * @param sql
	 * @return
	 */
	public static String[] splitSQLText(String sqlText) {
		String magic = "```" + UUID.randomUUID().toString() + "```";
		Matcher matcher = SQL_COMMENT_PATTERN.matcher(sqlText);
		StringBuffer sqlBuilder = new StringBuffer();
		while (matcher.find()) {
			String replacement = matcher.group(1);
			replacement = replacement == null ? "" : replacement.replace(";", magic);
			matcher.appendReplacement(sqlBuilder, replacement);
		}
		matcher.appendTail(sqlBuilder);
		String[] sqls = sqlBuilder.toString().trim().split(";");
		List<String> sqlList = new ArrayList<>();
		for (int i = 0; i < sqls.length; i++) {
			sqls[i] = sqls[i].trim().replace(magic, ";");
			if (sqls[i].length() > 0) {
				sqlList.add(sqls[i]);
			}
		}
		return sqlList.toArray(new String[0]);
	}

	/**
	 * 根据输入的SQL文本按表归纳相关的DML语句
	 * 
	 * @param sqlText
	 * @return
	 */
	public static LinkedHashMap<String, Set<String>> generateTableDmlSqls(String sqlText) {
		LinkedHashMap<String, Set<String>> tableDmlSqls = new LinkedHashMap<>();
		String[] sqls = splitSQLText(sqlText);
		for (String sql : sqls) {
			Matcher matcher = SQL_DML_TABLE_PATTERN.matcher(sql.trim().replaceAll("\\s+", " "));
			while (matcher.find()) {
				String tableName = matcher.group();
				if (tableName.trim().length() == 0) {
					continue;
				}
				Set<String> dmlSqls = tableDmlSqls.get(tableName);
				if (dmlSqls == null) {
					dmlSqls = new LinkedHashSet<>();
					tableDmlSqls.put(tableName, dmlSqls);
				}
				dmlSqls.add(sql);
			}
		}
		return tableDmlSqls;
	}

	/**
	 * 执行器抽象类
	 * 
	 * @author lujijiang
	 *
	 */
	public static abstract class Executor {
		/**
		 * 可关闭对象集合
		 */
		private Set<AutoCloseable> closeables = Lang.newSet();
		/**
		 * 执行器连接
		 */
		private Connection connection;

		/**
		 * 带结果的查询
		 * 
		 * @param type
		 *            返回类型
		 * @param sql
		 *            查询SQL
		 * @param args
		 *            查询参数
		 * @return
		 * @throws SQLException
		 */
		@SuppressWarnings("unchecked")
		public <T> List<T> executeQuery(Class<T> type, String sql, Object... args) throws SQLException {
			ResultSet resultSet = executeQuery(sql, args);
			try {
				if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
					throw new IllegalArgumentException(
							String.format("The type should not be abstract type,but got %s", type.getCanonicalName()));
				}
				if (Lang.isBaseType(type)) {
					throw new IllegalArgumentException(
							String.format("The type should not be base type,but got %s", type.getCanonicalName()));
				}
				if (Collection.class.isAssignableFrom(type)) {
					throw new IllegalArgumentException(String
							.format("The type should not be collection type,but got %s", type.getCanonicalName()));
				}
				if (Map.class.isAssignableFrom(type)) {
					List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
					while (resultSet.next()) {
						Map<String, Object> map = (Map<String, Object>) type.newInstance();
						for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
							String label = resultSet.getMetaData().getColumnLabel(i + 1);
							map.put(label, resultSet.getObject(label));
						}
						list.add(map);
					}
					return (List<T>) list;
				}
				PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(type).getPropertyDescriptors();
				List<T> list = new LinkedList<T>();
				while (resultSet.next()) {
					T obj = type.newInstance();
					for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
						Method writeMethod = propertyDescriptor.getWriteMethod();
						if (writeMethod != null) {
							Object value = resultSet.getObject(propertyDescriptor.getName().toUpperCase(),
									propertyDescriptor.getPropertyType());
							writeMethod.invoke(obj, value);
						}
					}
					list.add(obj);
				}
				return list;
			} catch (Exception e) {
				throw Lang.unchecked(e);
			} finally {
				resultSet.close();
			}
		}

		/**
		 * 带结果的查询
		 * 
		 * @param sql
		 *            查询SQL
		 * @param args
		 *            查询参数
		 * @return
		 * @throws SQLException
		 */
		public ResultSet executeQuery(String sql, Object... args) throws SQLException {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			try {
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						preparedStatement.setObject(i + 1, args[i]);
					}
				}
				return preparedStatement.executeQuery();
			} finally {
				closeables.add(preparedStatement);
			}
		}

		/**
		 * 创建可编辑结果的查询
		 * 
		 * @param sql
		 *            查询SQL
		 * @param args
		 *            查询参数
		 * @return ResultSet对象，可编辑
		 * @throws SQLException
		 */
		public ResultSet executeUpdatableQuery(String sql, Object... args) throws SQLException {
			PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			try {
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						preparedStatement.setObject(i + 1, args[i]);
					}
				}
				return preparedStatement.executeQuery();
			} finally {
				closeables.add(preparedStatement);
			}
		}

		/**
		 * 执行更新SQL
		 * 
		 * @param sql
		 *            查询SQL
		 * @param args
		 *            查询参数
		 * @return
		 * @throws SQLException
		 */
		public int executeUpdate(String sql, Object... args) throws SQLException {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			try {
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						preparedStatement.setObject(i + 1, args[i]);
					}
				}
				return preparedStatement.executeUpdate();
			} finally {
				preparedStatement.close();
			}
		}

		/**
		 * 执行SQL文本，兼容SQL注释
		 * 
		 * @param sqlText
		 *            SQL文本
		 * @return 返回执行的SQL语句数及影响的行数
		 * @throws SQLException
		 */
		public int[] executeText(String sqlText) throws SQLException {
			Statement statement = connection.createStatement();
			try {
				String[] sqls = splitSQLText(sqlText);
				int[] results = new int[sqls.length];
				for (int i = 0; i < sqls.length; i++) {
					statement.execute(sqls[i]);
					results[i] = statement.getUpdateCount();
				}
				return results;
			} finally {
				statement.close();
			}
		}

		/**
		 * 智能执行SQL文本，兼容SQL注释，能够自动的保证表依赖间的SQL执行
		 * 
		 * @param sqlText
		 *            SQL文本
		 * @param tryTime
		 *            尝试执行的次数
		 * @return 返回执行的SQL语句数及影响的行数
		 * @throws SQLException
		 */
		public Map<String, Integer> executeTextSmart(String sqlText) throws SQLException {
			Map<String, Integer> executeResult = new LinkedHashMap<>();
			LinkedHashMap<String, Set<String>> tableDmlSqls = generateTableDmlSqls(sqlText);
			int tryTime = 0;
			Exception e = null;
			while (tryTime != tableDmlSqls.size()) {
				tryTime = tableDmlSqls.size();
				for (int i = 0; i < tryTime; i++) {
					e = executeTextSmartSqls(tableDmlSqls, executeResult);
					if (e == null) {
						break;
					}
				}
			}
			if (tableDmlSqls.isEmpty()) {
				return executeResult;
			} else {
				throw Lang.unchecked(e, "执行SQL脚本失败，未成功执行SQL的相关表如下：\r\n%s", tableDmlSqls.keySet());
			}
		}

		private Exception executeTextSmartSqls(LinkedHashMap<String, Set<String>> tableDmlSqls,
				Map<String, Integer> executeResult) throws SQLException {
			Iterator<String> tableNameIterator = tableDmlSqls.keySet().iterator();
			if (tableNameIterator.hasNext()) {
				String tableName = tableNameIterator.next();
				Set<String> sqls = tableDmlSqls.remove(tableName);
				try (Statement statement = connection.createStatement()) {
					Map<String, Integer> tempExecuteResult = new LinkedHashMap<>();
					for (String sql : sqls) {
						statement.execute(sql);
						tempExecuteResult.put(sql, statement.getUpdateCount());
					}
					executeResult.putAll(tempExecuteResult);
				} catch (Exception e) {
					tableDmlSqls.put(tableName, sqls);
					return e;
				}
			}
			return null;
		}

		public abstract Object execute() throws Exception;
	}

	/**
	 * 执行JDBC
	 * 
	 * @param dataSource
	 * @param executor
	 * @return
	 */
	public static Object execute(DataSource dataSource, Executor executor) {
		Assert.notNull(executor, "Executor object must not be null");
		Connection connection = DataSourceUtils.getConnection(dataSource);
		try {
			return execute(connection, executor);
		} catch (Exception e) {
			throw Lang.unchecked(e);
		} finally {
			DataSourceUtils.releaseConnection(connection, dataSource);
		}
	}

	/**
	 * 执行JDBC
	 * 
	 * @param dataSource
	 * @param executor
	 * @return
	 */
	public static Object execute(Connection connection, Executor executor) {
		Assert.notNull(executor, "Executor object must not be null");
		try {
			executor.connection = createConnectionProxy(connection);
			try {
				return executor.execute();
			} finally {
				for (AutoCloseable autoCloseable : executor.closeables) {
					try {
						autoCloseable.close();
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 创建一个禁止关闭的连接代理对象
	 * 
	 * @param connection
	 * @return
	 */
	private static Connection createConnectionProxy(final Connection connection) {
		return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(),
				new Class<?>[] { ConnectionProxy.class }, new InvocationHandler() {
					@SuppressWarnings("rawtypes")
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						// Invocation on ConnectionProxy interface coming in...
						if (method.getName().equals("equals")) {
							// Only consider equal when proxies are identical.
							return (proxy == args[0]);
						} else if (method.getName().equals("hashCode")) {
							// Use hashCode of PersistenceManager proxy.
							return System.identityHashCode(proxy);
						} else if (method.getName().equals("unwrap")) {
							if (((Class) args[0]).isInstance(proxy)) {
								return proxy;
							}
						} else if (method.getName().equals("isWrapperFor")) {
							if (((Class) args[0]).isInstance(proxy)) {
								return true;
							}
						} else if (method.getName().equals("close")) {
							// Handle close method: suppress, not valid.
							return null;
						} else if (method.getName().equals("isClosed")) {
							return false;
						} else if (method.getName().equals("getTargetConnection")) {
							// Handle getTargetConnection method: return
							// underlying Connection.
							return connection;
						}
						// Invoke method on target Connection.
						try {
							Object retVal = method.invoke(connection, args);
							return retVal;
						} catch (InvocationTargetException ex) {
							throw ex.getTargetException();
						}
					}
				});
	}

	/**
	 * 存储过程
	 *
	 * @author lujijiang
	 *
	 */
	public static class Procedure {

		public class Param {

			int type;
			int dataType;
			String dataTypeName;
			Object value;

			private Param() {
			}

			public int getType() {
				return type;
			}

			public void setType(int type) {
				this.type = type;
			}

			public int getDataType() {
				return dataType;
			}

			public void setDataType(int dataType) {
				this.dataType = dataType;
			}

			public String getDataTypeName() {
				return dataTypeName;
			}

			public void setDataTypeName(String dataTypeName) {
				this.dataTypeName = dataTypeName;
			}

			public Object getValue() {
				return value;
			}

			public void setValue(Object value) {
				this.value = value;
			}

		}

		/**
		 * 存储过程名
		 */
		String name;

		/**
		 * 返回参数
		 */
		Param returnParam;

		/**
		 * 参数列表
		 */
		Map<Integer, Param> params = new TreeMap<Integer, Param>();

		private Procedure(String name) {
			this.name = name;
		}

		/**
		 * 设置返回类型
		 *
		 * @param dataType
		 *            参数的数据库类型，参考java.sql.Types。
		 * @return
		 */
		public Procedure setReturn(int dataType) {
			returnParam = new Param();
			returnParam.type = procedureColumnReturn;
			returnParam.dataType = dataType;
			return this;
		}

		/**
		 * 设置返回类型
		 *
		 * @param dataTypeName
		 *            参数的数据库类型名，用于自定义参数类型。
		 * @return
		 */
		public Procedure setReturn(String dataTypeName) {
			returnParam = new Param();
			returnParam.type = procedureColumnReturn;
			returnParam.dataType = Types.OTHER;
			returnParam.dataTypeName = dataTypeName;
			return this;
		}

		/**
		 * 设置返回类型
		 *
		 * @param dataType
		 *            参数的数据库类型，参考java.sql.Types。
		 * @param dataTypeName
		 *            参数的数据库类型名，用于自定义参数类型。
		 * @return
		 */
		public Procedure setReturn(int dataType, String dataTypeName) {
			returnParam = new Param();
			returnParam.type = procedureColumnReturn;
			returnParam.dataType = dataType;
			returnParam.dataTypeName = dataTypeName.toUpperCase();
			return this;
		}

		/**
		 * 设置参数
		 *
		 * @param index
		 *            参数序号
		 * @param type
		 *            参数类型，定义出入餐，参考Param的静态变量
		 * @param value
		 *            参数值
		 * @param dataType
		 *            参数的数据库类型，参考java.sql.Types。
		 * @return
		 */
		public Procedure setParam(int index, int type, Object value, int dataType) {
			Param param = new Param();
			param.type = type;
			param.dataType = dataType;
			param.value = value;
			params.put(index, param);
			return this;
		}

		/**
		 * 设置参数
		 *
		 * @param index
		 *            参数序号
		 * @param type
		 *            参数类型，定义出入餐，参考Param的静态变量
		 * @param value
		 *            参数值
		 * @param dataTypeName
		 *            参数的数据库类型名，用于自定义参数类型。
		 * @return
		 */
		public Procedure setParam(int index, int type, Object value, String dataTypeName) {
			Param param = new Param();
			param.type = type;
			param.dataType = Types.OTHER;
			param.dataTypeName = dataTypeName.toUpperCase();
			param.value = value;
			params.put(index, param);
			return this;
		}

		/**
		 * 设置参数
		 *
		 * @param index
		 *            参数序号
		 * @param type
		 *            参数类型，定义出入餐，参考Param的静态变量
		 * @param value
		 *            参数值
		 * @param dataType
		 *            参数的数据库类型，参考java.sql.Types。
		 * @param dataTypeName
		 *            参数的数据库类型名称
		 * @return
		 */
		public Procedure setParam(int index, int type, Object value, int dataType, String dataTypeName) {
			Param param = new Param();
			param.type = type;
			param.dataType = dataType;
			param.dataTypeName = dataTypeName.toUpperCase();
			param.value = value;
			params.put(index, param);
			return this;
		}

		/**
		 * 获取返回
		 *
		 * @return
		 */
		public Param getReturn() {
			return returnParam;
		}

		/**
		 * 获取参数
		 *
		 * @param index
		 *            参数序号，从0开始
		 * @return
		 */
		public Param getParam(int index) {
			return params.get(index);
		}

		/**
		 * 调用存储过程或者函数
		 *
		 * @param connection
		 *            数据库连接
		 * @return 返回存储过程影响的行数
		 */
		public int call(Connection connection) {
			Map<Integer, Param> paramMap = new TreeMap<Integer, Param>();
			StringBuilder sqlBuilder = new StringBuilder();
			{
				int index = 1;
				sqlBuilder.append("{");
				if (returnParam != null) {
					sqlBuilder.append("? = ");
					paramMap.put(index, returnParam);
					index++;
				}
				sqlBuilder.append("call ");
				sqlBuilder.append(name);
				sqlBuilder.append("(");
				int paramIndex = 0;
				for (Integer key : params.keySet()) {
					if (paramIndex > 0) {
						sqlBuilder.append(", ");
					}
					sqlBuilder.append("?");
					paramMap.put(index, params.get(key));
					index++;
					paramIndex++;
				}
				sqlBuilder.append(")");
				sqlBuilder.append("}");
			}

			try {
				CallableStatement cs = connection.prepareCall(sqlBuilder.toString());
				int i = 1;
				if (returnParam != null) {
					registerParam(cs, i, returnParam);
					i++;
				}
				for (Param param : params.values()) {
					registerParam(cs, i, param);
					i++;
				}
				int count = cs.executeUpdate();
				for (Integer index : paramMap.keySet()) {
					Param param = paramMap.get(index);
					if (param.type == procedureColumnOut || param.type == procedureColumnInOut
							|| param.type == procedureColumnReturn) {
						param.value = cs.getObject(index);
					}
				}
				return count;
			} catch (SQLException e) {
				throw Lang.unchecked(e);
			}
		}

		private void registerParam(CallableStatement cs, int i, Param param) throws SQLException {
			if (param.type == procedureColumnOut || param.type == procedureColumnInOut
					|| param.type == procedureColumnReturn) {
				if (param.dataTypeName != null) {
					cs.registerOutParameter(i, param.dataType, param.dataTypeName);
				} else {
					cs.registerOutParameter(i, param.dataType);
				}
			}
			if (param.type == procedureColumnIn || param.type == procedureColumnInOut) {
				cs.setObject(i, param.value);
			}
		}
	}

	/**
	 * 创建存储过程
	 *
	 * @param name
	 * @return
	 */
	public static Procedure createProcedure(String name) {
		return new Procedure(name);
	}

}
