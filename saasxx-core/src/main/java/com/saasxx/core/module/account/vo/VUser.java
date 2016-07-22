package com.saasxx.core.module.account.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.saasxx.core.module.account.constant.UserGender;
import com.saasxx.core.module.circle.vo.VHobby;

/**
 * 用户信息值对象
 * 
 * @author lujijiang
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VUser {
	/**
	 * 实体ID
	 */
	String id;
	/**
	 * 电话号码
	 */
	String tel;
	/**
	 * 电子邮箱
	 */
	String email;
	/**
	 * 用户密码
	 */
	String password;
	/**
	 * 真实姓名
	 */
	String realName;
	/**
	 * 性别
	 */
	UserGender gender;
	/**
	 * 手机验证码
	 */
	String validateCode;

	/**
	 * 照片列表
	 */
	List<String> avatars;
	/**
	 * 喜好列表
	 */
	List<VHobby> hobbies;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public UserGender getGender() {
		return gender;
	}

	public void setGender(UserGender gender) {
		this.gender = gender;
	}

	public String getValidateCode() {
		return validateCode;
	}

	public void setValidateCode(String validateCode) {
		this.validateCode = validateCode;
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

	@Override
	public String toString() {
		return "VUser [id=" + id + ", tel=" + tel + ", email=" + email + ", password=" + password + ", realName="
				+ realName + ", gender=" + gender + ", validateCode=" + validateCode + ", avatars=" + avatars
				+ ", hobbies=" + hobbies + "]";
	}

}
