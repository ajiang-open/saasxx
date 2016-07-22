package com.saasxx.core.module.account.schema;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.hibernate.validator.constraints.Email;
import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.saasxx.core.module.account.constant.UserGender;
import com.saasxx.core.module.account.constant.UserStatus;
import com.saasxx.core.module.common.schema.PFile;
import com.saasxx.framework.dao.orm.annotation.Comment;
import com.saasxx.framework.dao.orm.schema.IdEntity;

@Entity
@Table(indexes = { @Index(columnList = "tel"), @Index(columnList = "email") })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
@Comment("用户信息表")
@SelectBeforeUpdate(true)
@DynamicUpdate
@DynamicInsert
public class PUser extends IdEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4324349702886054345L;

	/**
	 * 登录手机号码
	 */
	@Pattern(regexp = "((\\d{11})|^((\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1})|(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1}))$)", message = "电话号码无效")
	@Column(unique = true, length = 32)
	@NotNull
	@Comment("手机号码")
	String tel;
	/**
	 * 登录手机号码
	 */
	@Email
	@Column(unique = true, length = 256)
	@NotNull
	@Comment("电子邮箱")
	String email;

	@Comment("照片列表")
	@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JSONField(serialize = false)
	List<PFile> avatars;
	/**
	 * 真实姓名
	 */
	@Column(length = 128)
	@NotNull
	@Comment("真实姓名")
	String realName;
	/**
	 * 密码密文
	 */
	@Column(length = 128)
	@Comment("密码密文")
	String password;
	/**
	 * 密码盐
	 */
	@Column(length = 64)
	@Comment("密码盐")
	String salt;
	/**
	 * 性别
	 */
	@Comment("性别")
	@Enumerated(EnumType.STRING)
	@Column(length = 16)
	UserGender gender;
	/**
	 * 生日
	 */
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Comment("生日")
	Date birthday;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Comment("用户状态")
	@Column(length = 16)
	UserStatus status;

	@Comment("验证码")
	@Column(length = 6)
	String validateCode;

	@Temporal(TemporalType.TIMESTAMP)
	@Comment("验证码创建时间")
	Date validateCodeCreatedTime;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
	@ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinTable(inverseJoinColumns = @JoinColumn(name = "role"), // 被维护端外键
			joinColumns = @JoinColumn(name = "user"))
	// 维护端外键
	List<PRole> roles;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, include = "non-lazy")
	@ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinTable(inverseJoinColumns = @JoinColumn(name = "hobby"), // 被维护端外键
			joinColumns = @JoinColumn(name = "user"))

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

	public List<PFile> getAvatars() {
		return avatars;
	}

	public void setAvatars(List<PFile> avatars) {
		this.avatars = avatars;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public UserGender getGender() {
		return gender;
	}

	public void setGender(UserGender gender) {
		this.gender = gender;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public String getValidateCode() {
		return validateCode;
	}

	public void setValidateCode(String validateCode) {
		this.validateCode = validateCode;
	}

	public Date getValidateCodeCreatedTime() {
		return validateCodeCreatedTime;
	}

	public void setValidateCodeCreatedTime(Date validateCodeCreatedTime) {
		this.validateCodeCreatedTime = validateCodeCreatedTime;
	}

	public List<PRole> getRoles() {
		return roles;
	}

	public void setRoles(List<PRole> roles) {
		this.roles = roles;
	}

}