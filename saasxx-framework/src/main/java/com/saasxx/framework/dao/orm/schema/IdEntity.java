package com.saasxx.framework.dao.orm.schema;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.search.annotations.DocumentId;

import com.saasxx.framework.Lang;
import com.saasxx.framework.dao.orm.annotation.Comment;

/**
 * 抽象主键实体
 * 
 * @author lujijiang
 * 
 */
@MappedSuperclass
public abstract class IdEntity extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8833080406291300295L;

	/**
	 * 主键
	 */
	@Id
	@NotNull
	@Size(min = 24, max = 24)
	@Column(name = "id_")
	@DocumentId
	@Comment("主键")
	protected String id;

	public IdEntity() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (!(this.getClass().isAssignableFrom(object.getClass()))) {
			return false;
		}
		IdEntity other = (IdEntity) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@PrePersist
	public void prePersist() {
		if (id == null) {
			this.id = Lang.id();
		}
		super.prePersist();
	}

	public Object copy() throws CloneNotSupportedException {
		return super.copy();
	}
}
