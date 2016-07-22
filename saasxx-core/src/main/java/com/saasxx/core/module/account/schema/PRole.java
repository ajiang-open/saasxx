package com.saasxx.core.module.account.schema;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

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
@Comment("角色表")
@SelectBeforeUpdate(true)
@DynamicUpdate
@DynamicInsert
public class PRole extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6016142750537903140L;

	@Comment("角色标签")
	@Column(length = 256, unique = true)
	@NotNull
	private String label;

	@Comment("角色名")
	@Column(length = 128, unique = true)
	@NotNull
	private String name;
	/**
	 * 角色所包含的用户信息
	 */
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
	@ManyToMany(cascade = CascadeType.REFRESH, mappedBy = "roles", // 通过维护端的属性关联
			fetch = FetchType.LAZY)
	List<PUser> users;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
	@ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinTable(inverseJoinColumns = @JoinColumn(name = "permission"), // 被维护端外键
			joinColumns = @JoinColumn(name = "role"))
	// 维护端外键
	List<PPermission> permissions;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<PUser> getUsers() {
		return users;
	}

	public void setUsers(List<PUser> users) {
		this.users = users;
	}

	public List<PPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<PPermission> permissions) {
		this.permissions = permissions;
	}

}
