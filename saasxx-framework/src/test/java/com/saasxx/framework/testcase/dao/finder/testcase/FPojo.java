package com.saasxx.framework.testcase.dao.finder.testcase;

import java.util.Date;

public class FPojo {
	String name;
	String email;
	String password;
	int age;
	Date birthday;
	FPojoEnum pojoEnum;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public FPojoEnum getPojoEnum() {
		return pojoEnum;
	}

	public void setPojoEnum(FPojoEnum pojoEnum) {
		this.pojoEnum = pojoEnum;
	}

}
