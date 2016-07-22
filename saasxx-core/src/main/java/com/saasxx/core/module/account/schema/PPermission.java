package com.saasxx.core.module.account.schema;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

import com.saasxx.core.module.account.constant.PermissionType;
import com.saasxx.framework.dao.orm.annotation.Comment;
import com.saasxx.framework.dao.orm.schema.IdEntity;

@Entity
@Table(indexes = {})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
@Comment("权限表")
@SelectBeforeUpdate(true)
@DynamicUpdate
@DynamicInsert
public class PPermission extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8322991992189674593L;

	@NotNull
	@Column(length = 128)
	@Comment("标签，用于显示含义标题")
	String label;

	@NotNull
	@Column(length = 128, unique = true)
	@Comment("权限值")
	String value;

	@Comment("权限类型")
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 16)
	PermissionType type;

	@NotNull
	@Column(length = 1024)
	@Comment("权限描述")
	String description;

	/**
	 * 权限所属角色
	 */
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
	@ManyToMany(cascade = CascadeType.REFRESH, mappedBy = "permissions", // 通过维护端的属性关联
			fetch = FetchType.LAZY)
	List<PRole> roles;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public PermissionType getType() {
		return type;
	}

	public void setType(PermissionType type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<PRole> getRoles() {
		return roles;
	}

	public void setRoles(List<PRole> roles) {
		this.roles = roles;
	}

}
