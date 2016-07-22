package com.saasxx.core.module.circle.vo;

/**
 * Created by lujijiang on 16/6/12.
 */
public class VHobby {
	String id;
	String name;
	boolean value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "VHobby [id=" + id + ", name=" + name + ", value=" + value + "]";
	}

}
