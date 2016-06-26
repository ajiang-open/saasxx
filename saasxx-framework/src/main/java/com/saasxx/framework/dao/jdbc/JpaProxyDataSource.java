package com.saasxx.framework.dao.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;

import com.saasxx.framework.Lang;
import com.saasxx.framework.lang.Proxys;

/**
 * 代理数据源
 * 
 * @author lujijiang
 *
 */
public class JpaProxyDataSource implements DataSource, InitializingBean {
	/**
	 * JPA数据源
	 */
	@PersistenceContext
	private EntityManager entityManager;

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return getDataSource().getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		getDataSource().setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		getDataSource().setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return getDataSource().getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return getDataSource().getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return getDataSource().unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return getDataSource().isWrapperFor(iface);
	}

	@Override
	public Connection getConnection() throws SQLException {
		entityManager.flush();
		class $ {
			Connection connection;
		}
		final $ $ = new $();
		entityManager.unwrap(Session.class).doWork(new Work() {
			public void execute(Connection connection) throws SQLException {
				$.connection = connection;
			}
		});
		return Proxys.newProxyInstance(new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if ("close|commit|rollback|setAutoCommit|setReadOnly".contains(method.getName())) {
					return null;
				}
				return method.invoke($.connection, args);
			}
		}, Connection.class);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return getConnection();
	}

	private DataSource getDataSource() {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		return info.getDataSource();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (entityManager == null) {
			throw Lang.newException("The entityManager should not be null");
		}
	}

}
