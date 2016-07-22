package com.saasxx.core.module.account.vo;

public class VPreUser {
	String email;
	String tel;
	String realName;
	String advice;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getAdvice() {
		return advice;
	}

	public void setAdvice(String advice) {
		this.advice = advice;
	}

	@Override
	public String toString() {
		return "VPreUser [email=" + email + ", tel=" + tel + ", realName=" + realName + ", advice=" + advice + "]";
	}

}
