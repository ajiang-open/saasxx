package com.saasxx.core.module.circle.schema;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
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
import com.saasxx.core.module.circle.constant.CircleStatus;
import com.saasxx.core.module.common.schema.PAddress;
import com.saasxx.core.module.common.schema.PFile;
import com.saasxx.core.module.common.schema.PText;
import com.saasxx.framework.dao.orm.annotation.Comment;
import com.saasxx.framework.dao.orm.schema.IdEntity;

@Entity
@Table(indexes = {})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
@Comment("圈子信息表")
@SelectBeforeUpdate(true)
@DynamicUpdate
@DynamicInsert
public class PCircle extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1725645607123083000L;

	@ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinTable(inverseJoinColumns = @JoinColumn(name = "hobby"), // 被维护端外键
			joinColumns = @JoinColumn(name = "circle"))
	List<PHobby> hobbies;

	@Column(length = 256, unique = true)
	String name;

	@Column(length = 128)
	String keywords;

	@Comment("描述")
	@NotNull
	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	PText description;

	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	@Comment("圈子总部")
	PAddress headquarter;

	@Comment("照片列表")
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JSONField(serialize = false)
	List<PFile> avatars;

	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	@Comment("创建人")
	PUser creator;

	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	@Comment("管理员")
	PUser admin;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Comment("圈子状态")
	@Column(length = 16)
	CircleStatus status;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
	@ManyToMany(cascade = CascadeType.REFRESH, mappedBy = "circles", // 通过维护端的属性关联
			fetch = FetchType.LAZY)
	List<PUser> users;

	@Comment("话题列表")
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "circle")
	@JSONField(serialize = false)
	List<PTopic> topics;

	public List<PHobby> getHobbies() {
		return hobbies;
	}

	public void setHobbies(List<PHobby> hobbies) {
		this.hobbies = hobbies;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public PText getDescription() {
		return description;
	}

	public void setDescription(PText description) {
		this.description = description;
	}

	public PAddress getHeadquarter() {
		return headquarter;
	}

	public void setHeadquarter(PAddress headquarter) {
		this.headquarter = headquarter;
	}

	public List<PFile> getAvatars() {
		return avatars;
	}

	public void setAvatars(List<PFile> avatars) {
		this.avatars = avatars;
	}

	public PUser getCreator() {
		return creator;
	}

	public void setCreator(PUser creator) {
		this.creator = creator;
	}

	public PUser getAdmin() {
		return admin;
	}

	public void setAdmin(PUser admin) {
		this.admin = admin;
	}

	public CircleStatus getStatus() {
		return status;
	}

	public void setStatus(CircleStatus status) {
		this.status = status;
	}

	public List<PUser> getUsers() {
		return users;
	}

	public void setUsers(List<PUser> users) {
		this.users = users;
	}

	public List<PTopic> getTopics() {
		return topics;
	}

	public void setTopics(List<PTopic> topics) {
		this.topics = topics;
	}

}
