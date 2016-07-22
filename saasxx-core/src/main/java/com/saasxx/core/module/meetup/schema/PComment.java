package com.saasxx.core.module.meetup.schema;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

import com.alibaba.fastjson.annotation.JSONField;
import com.saasxx.core.module.account.schema.PUser;
import com.saasxx.core.module.common.schema.PText;
import com.saasxx.framework.dao.orm.annotation.Comment;
import com.saasxx.framework.dao.orm.schema.IdEntity;

@Entity
@Table(indexes = {})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
@Comment("话题信息表")
@SelectBeforeUpdate(true)
@DynamicUpdate
@DynamicInsert
public class PComment extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3337626498409230109L;

	@Comment("所属活动")
	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	@NotNull
	PMeetup meetup;

	@Comment("话题作者")
	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	@NotNull
	PUser anthor;

	@Comment("内容")
	@NotNull
	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	PText content;

	@Comment("父评论")
	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	PComment parent;

	@Comment("子评论")
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "parent")
	@JSONField(serialize = false)
	List<PComment> children;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
	@ManyToMany(cascade = CascadeType.REFRESH, mappedBy = "comments", // 通过维护端的属性关联
			fetch = FetchType.LAZY)
	List<PUser> fans;

}
