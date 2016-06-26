package com.saasxx.framework.dao.orm.schema;

import java.io.Serializable;
import java.util.Date;

public class IdEntityHisPK implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3029432671204677287L;

	protected String id;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hisCreated == null) ? 0 : hisCreated.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdEntityHisPK other = (IdEntityHisPK) obj;
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
