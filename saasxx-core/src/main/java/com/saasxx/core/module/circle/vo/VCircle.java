package com.saasxx.core.module.circle.vo;

import java.util.List;

import com.saasxx.core.module.account.vo.VUser;
import com.saasxx.core.module.common.vo.VAddress;

/**
 * 圈子值对象
 * 
 * @author lujijiang
 *
 */
public class VCircle {
	/**
	 * 圈子ID
	 */
	String id;
	/**
	 * 圈子名称
	 */
	String name;
	/**
	 * 圈子热词
	 */
	String keywords;

	/**
	 * 圈子介绍
	 */
	String description;
	/**
	 * 照片列表
	 */
	List<String> avatars;
	/**
	 * 喜好列表
	 */
	List<VHobby> hobbies;
	/**
	 * 总部地址
	 */
	VAddress headquarter;
	/**
	 * 用户列表
	 */
	List<VUser> users;

	/**
	 * 创建日期
	 */
	String birthday;

	/**
	 * 自己的圈子
	 */
	boolean own;

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

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getAvatars() {
		return avatars;
	}

	public void setAvatars(List<String> avatars) {
		this.avatars = avatars;
	}

	public List<VHobby> getHobbies() {
		return hobbies;
	}

	public void setHobbies(List<VHobby> hobbies) {
		this.hobbies = hobbies;
	}

	public VAddress getHeadquarter() {
		return headquarter;
	}

	public void setHeadquarter(VAddress headquarter) {
		this.headquarter = headquarter;
	}

	public List<VUser> getUsers() {
		return users;
	}

	public void setUsers(List<VUser> users) {
		this.users = users;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public boolean isOwn() {
		return own;
	}

	public void setOwn(boolean own) {
		this.own = own;
	}

	@Override
	public String toString() {
		return "VCircle [id=" + id + ", name=" + name + ", keywords=" + keywords + ", description=" + description
				+ ", avatars=" + avatars + ", hobbies=" + hobbies + ", headquarter=" + headquarter + ", users=" + users
				+ ", birthday=" + birthday + "]";
	}

}
