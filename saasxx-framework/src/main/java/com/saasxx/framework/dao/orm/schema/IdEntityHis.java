package com.saasxx.framework.dao.orm.schema;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import com.saasxx.framework.dao.orm.annotation.Comment;

/**
 * 抽象主键实体
 * 
 * @author lujijiang
 * 
 */
@MappedSuperclass
@IdClass(IdEntityHisPK.class)
public abstract class IdEntityHis extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5772642277715627903L;
	/**
	 * 主键
	 */
	@Id
	@NotNull
	@Size(min = 24, max = 24)
	@Column(name = "id_")
	@Comment("历史主键")
	protected String id;

	/**
	 * 创建时间
	 */
	@Id
	@NotNull
	@Column(name = "his_created_")
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Comment("历史创建时间")
	protected Date hisCreated;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getHisCreated() {
		return hisCreated;
	}

	public void setHisCreated(Date hisCreated) {
		this.hisCreated = hisCreated;
	}

	@PrePersist
	public void prePersist() {
		this.hisCreated = new Date();
	}

	public Object copy() throws CloneNotSupportedException {
		return super.copy();
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hisCreated == null) ? 0 : hisCreated.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdEntityHis other = (IdEntityHis) obj;
		if (hisCreated == null) {
			if (other.hisCreated != null)
				return false;
		} else if (!hisCreated.equals(other.hisCreated))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
