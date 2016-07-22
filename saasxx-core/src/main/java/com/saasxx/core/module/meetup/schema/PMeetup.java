package com.saasxx.core.module.meetup.schema;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.saasxx.core.module.account.schema.PUser;
import com.saasxx.core.module.circle.schema.PCircle;
import com.saasxx.core.module.common.schema.PAddress;
import com.saasxx.core.module.common.schema.PFile;
import com.saasxx.core.module.common.schema.PText;
import com.saasxx.core.module.meetup.constant.MeetupStatus;
import com.saasxx.framework.dao.orm.annotation.Comment;
import com.saasxx.framework.dao.orm.schema.IdEntity;

@Entity
@Table(indexes = {})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
@Comment("活动信息表")
@SelectBeforeUpdate(true)
@DynamicUpdate
@DynamicInsert
public class PMeetup extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2170086091812202674L;

	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	@Comment("所属圈子")
	PCircle circle;

	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	@Comment("主办方")
	PUser organizer;

	@Comment("标题")
	@Column(length = 128)
	String title;

	@Column(length = 128)
	String keywords;

	@NotNull
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Comment("活动时间")
	protected Date time;

	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	@Comment("活动总部")
	PAddress address;

	@Comment("照片列表")
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JSONField(serialize = false)
	List<PFile> avatars;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Comment("活动状态")
	@Column(length = 16)
	MeetupStatus status;

	@Comment("内容")
	@ManyToOne(cascade = { CascadeType.REFRESH }, optional = false, fetch = FetchType.LAZY)
	PText description;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
	@ManyToMany(cascade = CascadeType.REFRESH, mappedBy = "meetups", // 通过维护端的属性关联
			fetch = FetchType.LAZY)
	List<PUser> users;

	@Comment("活动评论")
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "meetup")
	@JSONField(serialize = false)
	List<PComment> comments;

	public PCircle getCircle() {
		return circle;
	}

	public void setCircle(PCircle circle) {
		this.circle = circle;
	}

	public PUser getOrganizer() {
		return organizer;
	}

	public void setOrganizer(PUser organizer) {
		this.organizer = organizer;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public PAddress getAddress() {
		return address;
	}

	public void setAddress(PAddress address) {
		this.address = address;
	}

	public List<PFile> getAvatars() {
		return avatars;
	}

	public void setAvatars(List<PFile> avatars) {
		this.avatars = avatars;
	}

	public MeetupStatus getStatus() {
		return status;
	}

	public void setStatus(MeetupStatus status) {
		this.status = status;
	}

	public PText getDescription() {
		return description;
	}

	public void setDescription(PText description) {
		this.description = description;
	}

	public List<PUser> getUsers() {
		return users;
	}

	public void setUsers(List<PUser> users) {
		this.users = users;
	}

	public List<PComment> getComments() {
		return comments;
	}

	public void setComments(List<PComment> comments) {
		this.comments = comments;
	}

}
