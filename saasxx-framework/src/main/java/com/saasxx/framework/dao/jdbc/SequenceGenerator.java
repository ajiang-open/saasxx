package com.saasxx.framework.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.saasxx.framework.Lang;

/**
 * 序列发生器（可用于高并发、可回收等场景）
 * 
 * @author lujijiang
 * 
 */
final public class SequenceGenerator implements InitializingBean {

	interface Atom<T> {
		T atom(Connection connection) throws Exception;
	}

	public static <T> T atom(Connection connection, Atom<T> atom)
			throws Exception {
		connection.setAutoCommit(false);
		connection
				.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		try {
			T obj = atom.atom(connection);
			connection.commit();
			return obj;
		} catch (Exception e) {
			connection.rollback();
			throw e;
		}
	}

	static Map<String, String> createSqlMap = new ConcurrentHashMap<String, String>();
	static {
		createSqlMap
				.put("h2",
						"CREATE TABLE %s (v$key BIGINT NOT NULL,v$key$ varchar(256),v$seq BIGINT NOT NULL,v$state CHAR(1) NOT NULL, PRIMARY KEY (v$key,v$seq))");
		createSqlMap
				.put("mysql",
						"CREATE TABLE %s (v$key BIGINT NOT NULL,v$key$ varchar(256),v$seq BIGINT NOT NULL,v$state CHAR(1) NOT NULL, PRIMARY KEY (v$key,v$seq))");
		createSqlMap
				.put("informix",
						"CREATE TABLE %s (v$key DECIMAL(16,0) NOT NULL,v$key$ varchar(256),v$seq DECIMAL(16,0) NOT NULL,v$state CHAR(1) NOT NULL, PRIMARY KEY (v$key,v$seq))");
		createSqlMap
				.put("oracle",
						"CREATE TABLE %s (v$key NUMBER(16,0) NOT NULL,v$key$ varchar2(256),v$seq NUMBER(16,0) NOT NULL,v$state CHAR(1) NOT NULL, PRIMARY KEY (v$key,v$seq))");
	}

	static Map<String, String> insertSqlMap = new ConcurrentHashMap<String, String>();
	static {
		insertSqlMap
				.put("h2",
						"insert into %s(v$key,v$key$,v$seq,v$state) values(%d,'%s',(select ifnull(max(t.v$seq),%d)+%d from %s t where t.v$key=%d),'1')");
		insertSqlMap
				.put("mysql",
						"insert into %s(v$key,v$key$,v$seq,v$state) values(%d,'%s',(select ifnull(max(t.v$seq),%d)+%d from %s t where t.v$key=%d),'1')");
		insertSqlMap
				.put("informix",
						"insert into %s(v$key,v$key$,v$seq,v$state) values(%d,'%s',(select nvl(max(t.v$seq),%d)+%d from %s t where t.v$key=%d),'1')");
		insertSqlMap
				.put("oracle",
						"insert into %s(v$key,v$key$,v$seq,v$state) values(%d,'%s',(select nvl(max(t.v$seq),%d)+%d from %s t where t.v$key=%d),'1')");
	}
	/**
	 * 事务模版
	 */
	TransactionTemplate transactionTemplate;
	/**
	 * 是否使用新事务
	 */
	boolean newTransaction = true;
	/**
	 * JDBC模版
	 */
	JdbcTemplate jdbcTemplate;

	/**
	 * 尝试次数（默认10次）
	 */
	int tryTime = 10;

	/**
	 * 数据库类型
	 */
	String databaseType;
	/**
	 * 表名
	 */
	private String tableName;

	/**
	 * 容量
	 */
	private int capacity = 25;

	/**
	 * 步进
	 */
	private int step = 1;
	/**
	 * 是否可回收序列号
	 */
	protected boolean recyclable;

	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		if (transactionManager != null) {
			transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate
					.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
			transactionTemplate
					.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRED);
		}
	}

	public boolean isNewTransaction() {
		return newTransaction;
	}

	public void setNewTransaction(boolean newTransaction) {
		this.newTransaction = newTransaction;
	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setDataSource(DataSource dataSource) {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(dataSource);
		}
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	public void setTryTime(int tryTime) {
		this.tryTime = tryTime;
	}

	public void setDatabaseType(String databaseType) {
		if (databaseType != null) {
			databaseType = databaseType.trim().toLowerCase();
			this.databaseType = databaseType;
		}
	}

	public void setTableName(String tableName) {
		this.tableName = tableName == null ? null : tableName.trim();
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public void setStep(int step) {
		this.step = step < 1 ? 1 : step;
	}

	public void setRecyclable(boolean recyclable) {
		this.recyclable = recyclable;
	}

	/**
	 * 创建序列
	 * 
	 * @param key
	 * @param length
	 * @param start
	 * @return
	 */
	public String generateSequence(String name, int length, long start) {
		final long key = generateKey(name);
		try {
			return generateSequence(key, length, start, name);
		} catch (Exception t) {
			createSequenceTable();
			int time = tryTime >= 0 ? tryTime : 10000;
			while (time-- > 0) {
				try {
					return generateSequence(key, length, start, name);
				} catch (Exception e) {

				}
			}
			throw new RuntimeException("Generate sequence failure", t);
		}
	}

	private long generateKey(String name) {
		return 10000000000L + name.trim().hashCode();
	}

	/**
	 * 创建序列
	 * 
	 * @param key
	 * @param length
	 * @return
	 */
	public String generateSequence(String key, int length) {
		return generateSequence(key, length, 0);
	}

	/**
	 * 创建序列
	 * 
	 * @param key
	 * @return
	 */
	public String generateSequence(String key) {
		return generateSequence(key, 6);
	}

	private synchronized void createSequenceTable() {
		try {
			jdbcTemplate.execute(new ConnectionCallback<Integer>() {

				public Integer doInConnection(Connection connection)
						throws SQLException, DataAccessException {
					ResultSet rs = connection.getMetaData().getTables(null,
							null, tableName, null);
					try {
						while (rs.next()) {
							return 1;
						}
					} finally {
						rs.close();
					}
					String createSql = String.format(
							createSqlMap.get(databaseType), tableName);
					Statement statement = connection.createStatement();
					try {
						statement.execute(createSql);
					} finally {
						statement.close();
					}
					return 0;
				}
			});
		} catch (Exception t) {
			throw new RuntimeException(t);
		}
	}

	private String generateSequence(final long key, final int length,
			final long start, final String name) {
		if (newTransaction) {
			try {
				Connection connection = jdbcTemplate.getDataSource()
						.getConnection();
				try {
					return atom(connection, new Atom<String>() {

						@Override
						public String atom(Connection connection)
								throws Exception {
							if (recyclable) {
								String no = getRecoveredSequence(connection,
										key, length);
								if (no != null) {
									return no;
								}
							}
							Statement statement = connection.createStatement();
							try {
								String insertSql = String.format(insertSqlMap
										.get(databaseType), tableName, key,
										name.replace("'", "''"), start < 0 ? 0
												: start, step, tableName, key);
								execute(connection, insertSql);
								String querySql = String
										.format("select max(v$seq) from %s  where v$key=%d and v$state='1'",
												tableName, key);
								ResultSet rs = statement.executeQuery(querySql);
								try {
									while (rs.next()) {
										Long max = rs.getLong(1);
										if (max < start) {// 重新初始化
											String deleteSql = String
													.format("delete from %s where v$key=%d and v$state='1'",
															tableName, key);
											execute(connection, deleteSql);
											insertSql = String.format(
													insertSqlMap
															.get(databaseType),
													tableName, key,
													name.replace("'", "''"),
													start < 0 ? 0 : start,
													step, tableName, key);
											execute(connection, insertSql);
											return fillLeftZero(start, length);
										}
										if (capacity > 0 && max % capacity == 0) {
											String deleteSql = String
													.format("delete from %s where v$key=%d and v$seq<>%d and v$state='1'",
															tableName, key, max);
											execute(connection, deleteSql);
										}
										return fillLeftZero(max, length);
									}
								} finally {
									rs.close();
								}
							} finally {
								statement.close();
							}
							throw new IllegalStateException(
									SequenceGenerator.this.getClass()
											.getSimpleName()
											+ "无法获取单号，不可能发生的异常");
						}
					});
				} finally {
					connection.close();
				}
			} catch (Exception e) {
				throw Lang.unchecked(e);
			}
		}
		return getTransactionTemplate().execute(
				new TransactionCallback<String>() {
					public String doInTransaction(TransactionStatus ts) {
						try {
							if (recyclable) {
								String no = jdbcTemplate
										.execute(new ConnectionCallback<String>() {
											public String doInConnection(
													Connection connection)
													throws SQLException,
													DataAccessException {
												return getRecoveredSequence(
														connection, key, length);
											}
										});
								if (no != null) {
									return no;
								}
							}
							String insertSql = String.format(insertSqlMap
									.get(databaseType), tableName, key, name
									.replace("'", "''"), start < 0 ? 0 : start,
									step, tableName, key);
							execute(insertSql);
							String querySql = String
									.format("select max(v$seq) from %s  where v$key=%d and v$state='1'",
											tableName, key);
							Long max = jdbcTemplate.queryForObject(querySql,
									Long.class);
							if (max < start) {// 重新初始化
								String deleteSql = String
										.format("delete from %s where v$key=%d and v$state='1'",
												tableName, key);
								execute(deleteSql);
								insertSql = String.format(insertSqlMap
										.get(databaseType), tableName, key,
										name.replace("'", "''"), start < 0 ? 0
												: start, step, tableName, key);
								execute(insertSql);
								return fillLeftZero(start, length);
							}
							if (capacity > 0 && max % capacity == 0) {
								String deleteSql = String
										.format("delete from %s where v$key=%d and v$seq<>%d and v$state='1'",
												tableName, key, max);
								execute(deleteSql);
							}
							return fillLeftZero(max, length);
						} catch (Exception t) {
							ts.isRollbackOnly();
							throw new RuntimeException(t);
						}
					}
				});
	}

	/**
	 * 参数化执行SQL
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	protected int execute(String sql, final Object... args) {
		return jdbcTemplate.execute(sql,
				new PreparedStatementCallback<Integer>() {
					public Integer doInPreparedStatement(PreparedStatement ps)
							throws SQLException, DataAccessException {
						if (args != null) {
							for (int i = 0; i < args.length; i++) {
								ps.setObject(i + 1, args[i]);
							}
						}
						return ps.executeUpdate();
					}
				});
	}

	/**
	 * 参数化执行SQL
	 * 
	 * @param sql
	 * @param args
	 * @return
	 * @throws SQLException
	 */
	protected int execute(Connection conn, String sql, final Object... args)
			throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		try {
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					ps.setObject(i + 1, args[i]);
				}
			}
			return ps.executeUpdate();
		} finally {
			ps.close();
		}
	}

	protected String fillLeftZero(Long max, int length) {
		StringBuilder sb = new StringBuilder();
		sb.append(max);
		if (sb.length() >= length) {
			return sb.substring(sb.length() - length);
		} else {
			for (int i = sb.length(); i < length; i++) {
				sb.insert(0, '0');
			}
		}
		return sb.toString();
	}

	/**
	 * 获取已回收的序列
	 * 
	 * @param connection
	 * @param key
	 * @throws SQLException
	 */
	protected String getRecoveredSequence(Connection connection, long key,
			int length) throws SQLException {
		Statement statement = connection.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		try {
			String querySql = String
					.format("select v$key,v$seq,v$state from %s where v$key = %d and v$state<>'1' for update",
							tableName, key);
			ResultSet updateResultSet = statement.executeQuery(querySql);
			try {
				querySql = String
						.format("select v$key,v$seq,v$state from %s where v$key = %d and v$state<>'1' order by v$seq asc",
								tableName, key);
				ResultSet rs = statement.executeQuery(querySql);
				try {
					while (rs.next()) {
						try {
							Long min = rs.getLong(2);
							rs.updateString(3, "1");
							rs.updateRow();
							return fillLeftZero(min, length);
						} catch (Exception ex) {
						}
					}
				} finally {
					rs.close();
				}
			} finally {
				updateResultSet.close();
			}
		} finally {
			statement.close();
		}
		return null;
	}

	/**
	 * 回收序列号
	 * 
	 * @param name
	 * @param sequence
	 */
	public void recoveredSequence(final String name, final String sequence) {
		final long key = generateKey(name);
		final long seq = Long.valueOf(sequence);
		if (newTransaction) {
			try {
				Connection connection = jdbcTemplate.getDataSource()
						.getConnection();
				try {
					atom(connection, new Atom<Object>() {
						public Object atom(Connection connection)
								throws Exception {
							String updateSql = String
									.format("update %s set v$state='0' where v$key=%d and v$seq=%d",
											tableName, key, seq);
							int count = execute(connection, updateSql);
							if (count == 0) {
								String insertSql = String
										.format("insert into %s(v$key,v$key$,v$seq,v$state) values(%d,'%s',%d,'0')",
												tableName, key,
												name.replace("'", "''"), seq);
								execute(connection, insertSql);
							}
							return null;
						}
					});
					connection.commit();
				} finally {
					connection.close();
				}
			} catch (Exception e) {
				throw Lang.unchecked(e);
			}
		} else {
			transactionTemplate.execute(new TransactionCallback<Integer>() {
				public Integer doInTransaction(TransactionStatus status) {
					try {
						String updateSql = String
								.format("update %s set v$state='0' where v$key=%d and v$seq=%d",
										tableName, key, seq);
						int count = execute(updateSql);
						if (count == 0) {
							String insertSql = String
									.format("insert into %s(v$key,v$key$,v$seq,v$state) values(%d,'%s',%d,'0')",
											tableName, key,
											name.replace("'", "''"), seq);
							execute(insertSql);
						}
						return null;
					} catch (Exception t) {
						status.isRollbackOnly();
						throw new RuntimeException(t);
					}
				}
			});
		}
	}

	public void afterPropertiesSet() throws Exception {
		if (this.databaseType == null) {
			throw new IllegalArgumentException(String.format(
					"Property '%s' is required", "databaseType"));
		}
		if (databaseType != null) {
			if (!createSqlMap.containsKey(databaseType)) {
				throw new IllegalStateException(String.format(
						"Unsupported database type:%s", databaseType));
			}
		}
		if (this.jdbcTemplate == null) {
			throw new IllegalArgumentException(String.format(
					"Property '%s' or '%s' is required", "dataSource",
					"jdbcTemplate"));
		}
		if (this.tableName == null) {
			throw new IllegalArgumentException(String.format(
					"Property '%s' is required", "tableName"));
		}
		if (!tableName.matches("[a-zA-Z_][a-zA-Z0-9_]*")
				|| tableName.length() > 16) {
			throw new IllegalStateException(String.format(
					"Illegal tableName:%s", tableName));
		}
		if (!this.newTransaction) {
			if (this.transactionTemplate == null) {
				throw new IllegalArgumentException(String.format(
						"Property '%s' is required", "transactionManager"));
			}
		}
	}
}