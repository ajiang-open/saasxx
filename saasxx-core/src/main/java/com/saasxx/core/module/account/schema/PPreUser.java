package com.saasxx.core.module.account.schema;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

import com.saasxx.framework.dao.orm.annotation.Comment;
import com.saasxx.framework.dao.orm.schema.IdEntity;

@Entity
@Table(indexes = {})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
@Comment("预备用户表")
@SelectBeforeUpdate(true)
@DynamicUpdate
@DynamicInsert
public class PPreUser extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8630224832669631995L;

	@Comment("对应用户")
	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	PUser user;

	@Comment("咨询建议")
	@Column(length = 4096)
	String advice;

	public PUser getUser() {
		return user;
	}

	public void setUser(PUser user) {
		this.user = user;
	}

	public String getAdvice() {
		return advice;
	}

	public void setAdvice(String advice) {
		this.advice = advice;
	}

}
