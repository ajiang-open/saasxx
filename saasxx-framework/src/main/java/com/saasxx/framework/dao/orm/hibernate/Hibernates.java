package com.saasxx.framework.dao.orm.hibernate;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.SQLServer2008Dialect;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.persister.entity.AbstractEntityPersister;

import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.orm.annotation.Comment;
import com.saasxx.framework.dao.orm.hibernate.dialect.InformixDialect;

/**
 * Hibernate工具类
 * 
 * @author lujijiang
 * 
 */
public class Hibernates {
	@SuppressWarnings("rawtypes")
	private static Class hibernateSessionClass = null;
	static {
		try {
			hibernateSessionClass = Class.forName("org.hibernate.Session");
		} catch (ClassNotFoundException e) {

		}
	}

	/**
	 * Initialize the lazy property value.
	 * 
	 * e.g. Hibernates.initLazyProperty(user.getGroups());
	 */
	public static void initLazyProperty(Object proxyedPropertyValue) {
		Hibernate.initialize(proxyedPropertyValue);
	}

	/**
	 * 从DataSoure中取出connection, 根据connection的metadata中的jdbcUrl判断Dialect类型.
	 * 仅支持Oracle, H2, MySql, PostgreSql, SQLServer，如需更多数据库类型，请仿照此类自行编写。
	 */
	public static String getDialect(DataSource dataSource) {
		String jdbcUrl = getJdbcUrlFromDataSource(dataSource);

		// 根据jdbc url判断dialect
		if (StringUtils.contains(jdbcUrl, ":h2:")) {
			return H2Dialect.class.getName();
		} else if (StringUtils.contains(jdbcUrl, ":mysql:")) {
			return MySQL5InnoDBDialect.class.getName();
		} else if (StringUtils.contains(jdbcUrl, ":oracle:")) {
			return Oracle10gDialect.class.getName();
		} else if (StringUtils.contains(jdbcUrl, ":postgresql:")) {
			return PostgreSQL82Dialect.class.getName();
		} else if (StringUtils.contains(jdbcUrl, ":sqlserver:")) {
			return SQLServer2008Dialect.class.getName();
		} else if (StringUtils.contains(jdbcUrl, ":informix-sqli:")) {
			return InformixDialect.class.getName();
		} else {
			throw new IllegalArgumentException("Unknown Database of " + jdbcUrl);
		}
	}

	private static String getJdbcUrlFromDataSource(DataSource dataSource) {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			if (connection == null) {
				throw new IllegalStateException(
						"Connection returned by DataSource [" + dataSource
								+ "] was null");
			}
			return connection.getMetaData().getURL();
		} catch (SQLException e) {
			throw new RuntimeException("Could not get database url", e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * 判断是否是Hibernate实现
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean isHibernate(EntityManager em) {
		if (em.getDelegate() != null && hibernateSessionClass != null) {
			if (hibernateSessionClass.isAssignableFrom(em.getDelegate()
					.getClass())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 写入注释（仅支持Oracle, H2, MySql, PostgreSql, SQLServer，如需更多数据库类型，请仿照此类自行编写。）
	 * 数据源需要设置如下属性支持
	 * ：remarksReporting＝true（当是oracle的时候）或者useInformationSchema＝true
	 * （当是mysql的时候）
	 * 
	 * @param sessionFactory
	 */
	public static void writeComments(SessionFactory sessionFactory) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			for (String name : sessionFactory.getAllClassMetadata().keySet()) {
				AbstractEntityPersister classMetadata = (AbstractEntityPersister) sessionFactory
						.getAllClassMetadata().get(name);
				Class<?> entityType = classMetadata.getMappedClass();
				final String tableName = classMetadata.getTableName();
				String tableComment = session
						.doReturningWork(new ReturningWork<String>() {
							public String execute(Connection connection)
									throws SQLException {
								ResultSet rs = connection.getMetaData()
										.getTables(null, getScheme(connection),
												tableName, null);
								try {
									while (rs.next()) {
										String remark = rs.getString("REMARKS");
										if (!Lang.isEmpty(remark)) {
											return remark;
										}
									}
								} finally {
									rs.close();
								}
								return null;
							}
						});
				if (!Lang.isEmpty(tableComment)) {
					continue;
				}
				{
					Comment comment = entityType.getAnnotation(Comment.class);
					if (comment != null) {
						writeTableComment(session, sessionFactory, tableName,
								comment);
					}
				}

				try {
					// 解析实体类中的属性
					PropertyDescriptor[] propertyDescriptors = Introspector
							.getBeanInfo(entityType).getPropertyDescriptors();
					for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
						Method method = propertyDescriptor.getReadMethod();
						if (method != null) {
							Comment comment = method
									.getAnnotation(Comment.class);
							try {
								String[] columnNames = classMetadata
										.getPropertyColumnNames(propertyDescriptor
												.getName());
								if (comment != null && columnNames != null) {
									for (String columnName : columnNames) {
										writeColumnComment(session,
												sessionFactory, tableName,
												columnName, comment);
									}
								}
							} catch (MappingException e) {
							}

						}
					}
					// 解析实体类中的字段
					while (entityType != null) {
						Field[] fields = entityType.getDeclaredFields();
						entityType = entityType.getSuperclass();
						for (Field field : fields) {
							Comment comment = field
									.getAnnotation(Comment.class);
							try {
								String[] columnNames = classMetadata
										.getPropertyColumnNames(field.getName());
								if (comment != null && columnNames != null) {
									for (String columnName : columnNames) {
										writeColumnComment(session,
												sessionFactory, tableName,
												columnName, comment);
									}
								}
							} catch (MappingException e) {
							}
						}
					}
				} catch (Exception e) {
					throw Lang.unchecked(e);
				}
			}
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			throw Lang.unchecked(e);
		} finally {
			session.close();
		}
	}

	/**
	 * 生成修改列注释的SQL
	 * 
	 * @param connection
	 * @param tableName
	 * @param columnName
	 * @param comment
	 * @return
	 * @throws SQLException
	 */
	private static String generateAlertTableColumnCommentSql(
			Connection connection, final String tableName,
			final String columnName, final Comment comment) throws SQLException {
		String jdbcUrl = connection.getMetaData().getURL();
		String schemeName = getScheme(connection);
		if (StringUtils.contains(jdbcUrl, ":mysql:")) {
			String columnMeta = null;
			Statement statement = connection.createStatement();
			try {
				String sql = "SELECT CONCAT_WS(' ',COLUMNS.COLUMN_TYPE,IF(COLUMNS.IS_NULLABLE = 'NO','NOT NULL',''),COLUMNS.COLUMN_DEFAULT) FROM information_schema.COLUMNS "
						+ "where COLUMNS.TABLE_SCHEMA like '%s' "
						+ "and COLUMNS.TABLE_NAME = '%s' "
						+ "and COLUMNS.COLUMN_NAME='%s'";

				schemeName = schemeName == null ? "%" : schemeName;
				sql = String.format(sql, schemeName, tableName, columnName);
				ResultSet resultSet = statement.executeQuery(sql);
				if (resultSet.next()) {
					columnMeta = resultSet.getString(1);
				}
				resultSet.close();
			} finally {
				statement.close();
			}

			if (columnMeta == null) {
				throw new IllegalStateException(String.format(
						"无法查询到%s.%s列的元数据，请检查information_schema.columns表",
						tableName, columnName));
			}

			String sql = "alter table %s.%s modify column %s %s comment '%s'";
			sql = String.format(sql, schemeName, tableName, columnName,
					columnMeta, commentToString(comment));
			return sql;
		} else {
			String sql = "COMMENT ON COLUMN %s.%s IS '%s'";
			sql = String.format(sql, tableName, columnName,
					commentToString(comment));
			return sql;
		}

	}

	private static String commentToString(final Comment comment) {
		String commentString = comment.value();
		if (!Lang.isEmpty(comment.description())) {
			commentString = commentString.concat(",").concat(
					comment.description());
		}
		return escapeSql(commentString);
	}

	private static String escapeSql(final String sql) {
		return sql.replace("'", "''");
	}

	/**
	 * 写如列注释
	 * 
	 * @param sessionFactory
	 * @param tableName
	 * @param columnName
	 * @param comment
	 */
	private static void writeColumnComment(Session session,
			SessionFactory sessionFactory, final String tableName,
			final String columnName, final Comment comment) {
		session.doWork(new Work() {
			public void execute(Connection connection) throws SQLException {
				Statement statement = connection.createStatement();
				try {
					String sql = generateAlertTableColumnCommentSql(connection,
							tableName, columnName, comment);
					if (sql != null) {
						statement.execute(sql);
					}
				} catch (SQLException e) {

				} finally {
					statement.close();
				}
			}

		});
	}

	/**
	 * 生成修改表注释的SQL
	 * 
	 * @param connection
	 * @param tableName
	 * @param comment
	 * @return
	 * @throws SQLException
	 */
	private static String generateAlertTableCommentSql(Connection connection,
			final String tableName, final Comment comment) throws SQLException {
		String jdbcUrl = connection.getMetaData().getURL();
		String schemeName = getScheme(connection);
		if (StringUtils.contains(jdbcUrl, ":mysql:")) {
			String sql = "alter table %s.%s comment '%s'";
			sql = String.format(sql, schemeName, tableName,
					commentToString(comment));
			return sql;
		} else {
			String sql = "COMMENT ON TABLE %s IS '%s'";
			sql = String.format(sql, tableName, commentToString(comment));
			return sql;
		}

	}

	private static String getScheme(final Connection connection)
			throws SQLException {
		return connection.getCatalog();
	}

	/**
	 * 写如表注释
	 * 
	 * @param sessionFactory
	 * @param tableName
	 * @param comment
	 */
	private static void writeTableComment(Session session,
			SessionFactory sessionFactory, final String tableName,
			final Comment comment) {
		session.doWork(new Work() {
			public void execute(Connection connection) throws SQLException {
				Statement statement = connection.createStatement();
				try {
					String sql = generateAlertTableCommentSql(connection,
							tableName, comment);
					if (sql != null) {
						statement.execute(sql);
					}
				} catch (SQLException e) {

				} finally {
					statement.close();
				}
			}

		});
	}
}