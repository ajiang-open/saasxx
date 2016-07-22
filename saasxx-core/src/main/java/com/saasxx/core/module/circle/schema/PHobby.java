package com.saasxx.core.module.circle.schema;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

import com.alibaba.fastjson.annotation.JSONField;
import com.saasxx.core.module.account.schema.PUser;
import com.saasxx.framework.dao.orm.annotation.Comment;
import com.saasxx.framework.dao.orm.schema.IdEntity;

@Entity
@Table(indexes = {})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
@Comment("圈子分类表")
@SelectBeforeUpdate(true)
@DynamicUpdate
@DynamicInsert
public class PHobby extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2792604554236436007L;

	@Column(length = 256, unique = true)
	String name;

	@Comment("父分类")
	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	PHobby parent;

	@Comment("子分类")
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "parent")
	@JSONField(serialize = false)
	List<PHobby> children;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
	@ManyToMany(cascade = CascadeType.REFRESH, mappedBy = "hobbies", // 通过维护端的属性关联
			fetch = FetchType.LAZY)
	List<PCircle> circles;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
	@ManyToMany(cascade = CascadeType.REFRESH, mappedBy = "hobbies", // 通过维护端的属性关联
			fetch = FetchType.LAZY)
	List<PUser> users;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PHobby getParent() {
		return parent;
	}

	public void setParent(PHobby parent) {
		this.parent = parent;
	}

	public List<PHobby> getChildren() {
		return children;
	}

	public void setChildren(List<PHobby> children) {
		this.children = children;
	}

	public List<PCircle> getCircles() {
		return circles;
	}

	public void setCircles(List<PCircle> circles) {
		this.circles = circles;
	}

	public List<PUser> getUsers() {
		return users;
	}

	public void setUsers(List<PUser> users) {
		this.users = users;
	}

}
