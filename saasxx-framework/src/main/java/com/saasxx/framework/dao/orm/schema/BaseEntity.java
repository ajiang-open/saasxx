package com.saasxx.framework.dao.orm.schema;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import com.saasxx.framework.dao.orm.annotation.Comment;

/**
 * 抽象实体类型，用于联合主键表
 * 
 * @author lujijiang
 * 
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8833080406291300295L;

	/**
	 * 是否无效（默认是有效）
	 */
	@NotNull
	@Column(name = "disabled_", columnDefinition = "decimal(1,0)")
	@Comment("无效状态位，默认有效")
	protected Boolean disabled;
	/**
	 * 最后更新时间
	 */
	@NotNull
	@Column(name = "last_updated_")
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Comment("最后更新时间")
	protected Date lastUpdated;
	/**
	 * 创建时间
	 */
	@NotNull
	@Column(name = "date_created_")
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Comment("创建时间")
	protected Date dateCreated;

	/**
	 * 数据库版本号（用于乐观锁）
	 */
	@NotNull
	@Column(name = "version_")
	@Version
	@Comment("乐观锁版本号")
	protected Integer version;

	public BaseEntity() {

	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@PrePersist
	public void prePersist() {
		if (dateCreated == null)
			this.dateCreated = new Date();
		if (disabled == null)
			this.disabled = false;
		if (lastUpdated == null)
			this.lastUpdated = this.dateCreated;
		if (version == null) {
			version = 0;
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.lastUpdated = new Date();
		if (disabled == null)
			this.disabled = false;
	}

	public Object copy() throws CloneNotSupportedException {
		return super.clone();
	}

}
