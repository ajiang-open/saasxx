package com.saasxx.framework.dao.orm.jpa;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.spi.MetadataBuildingContext;

/**
 * 全局命名策略器，驼峰式单词分解为下划线隔开
 * 
 * @author lujijiang
 *
 */
public class ImplicitNamingStrategyCompliantImpl extends ImplicitNamingStrategyJpaCompliantImpl {

	private static final long serialVersionUID = -3799304339334003382L;

	private String fixName(String name) {
		StringBuilder nameBuilder = new StringBuilder();
		char[] cs = name.toCharArray();
		for (int i = 0; i < cs.length; i++) {
			char c = cs[i];
			if (i > 0 && Character.isUpperCase(c) && Character.isLowerCase(cs[i - 1])) {
				nameBuilder.append("_");
				nameBuilder.append(Character.toLowerCase(c));
			} else {
				nameBuilder.append(Character.toLowerCase(c));
			}
		}
		name = nameBuilder.toString();
		return name;
	}

	@Override
	protected Identifier toIdentifier(String stringForm, MetadataBuildingContext buildingContext) {
		return super.toIdentifier(fixName(stringForm), buildingContext);
	}

}
