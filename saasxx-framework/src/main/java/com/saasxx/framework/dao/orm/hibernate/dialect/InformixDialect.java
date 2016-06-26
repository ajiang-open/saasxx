package com.saasxx.framework.dao.orm.hibernate.dialect;

import java.sql.Types;

import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.type.StandardBasicTypes;

/**
 * Informix方言（支持informix10及以上版本）
 */
public class InformixDialect extends org.hibernate.dialect.InformixDialect {
	public InformixDialect() {
		super();
		// 注册时间函数
		registerFunction("current_date", new NoArgSQLFunction("current",
				StandardBasicTypes.DATE, false));
		registerFunction("current_time", new NoArgSQLFunction("current",
				StandardBasicTypes.TIME, false));
		registerFunction("current_timestamp", new NoArgSQLFunction("current",
				StandardBasicTypes.TIMESTAMP, false));
		// 注册CLOB和长字段类型为text类型
		registerColumnType(Types.CLOB, "text");
		registerColumnType(Types.LONGVARCHAR, "text");
	}

	/*
	 * 使用字符t表示true，f表示false
	 */
	public String toBooleanValueString(boolean bool) {
		return bool ? "'t'" : "'f'";
	}

	/**
	 * 重写分页代码，支持skip语句
	 */
	public String getLimitString(String querySelect, int offset, int limit) {
		StringBuilder buf = new StringBuilder();
		buf.append(" skip ").append(offset).append(" first ").append(limit);
		StringBuilder result = new StringBuilder(querySelect.length()
				+ buf.length());
		result.append(querySelect).insert(
				querySelect.toLowerCase().indexOf("select") + 6, buf);
		return result.toString();
	}

	public boolean useMaxForLimit() {
		return false;
	}

	public boolean supportsLimitOffset() {
		return true;
	}

}
