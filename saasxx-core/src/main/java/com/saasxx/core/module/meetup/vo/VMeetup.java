package com.saasxx.core.module.meetup.vo;

import java.util.List;

import com.saasxx.core.module.account.vo.VUser;
import com.saasxx.core.module.circle.vo.VCircle;
import com.saasxx.core.module.circle.vo.VHobby;
import com.saasxx.core.module.common.vo.VAddress;

public class VMeetup {
	/**
	 * 圈子ID
	 */
	String id;

	/**
	 * 圈子信息
	 */
	VCircle circle;
	/**
	 * 圈子名称
	 */
	String title;
	/**
	 * 圈子热词
	 */
	String keywords;

	/**
	 * 活动时间
	 */
	String time;

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
	VAddress address;
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

	public VCircle getCircle() {
		return circle;
	}

	public void setCircle(VCircle circle) {
		this.circle = circle;
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

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
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

	public VAddress getAddress() {
		return address;
	}

	public void setAddress(VAddress address) {
		this.address = address;
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
		return "VMeetup [id=" + id + ", circle=" + circle + ", title=" + title + ", keywords=" + keywords
				+ ", description=" + description + ", avatars=" + avatars + ", hobbies=" + hobbies + ", address="
				+ address + ", users=" + users + ", birthday=" + birthday + ", own=" + own + "]";
	}

}
