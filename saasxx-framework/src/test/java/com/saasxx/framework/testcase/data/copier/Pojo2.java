package com.saasxx.framework.testcase.data.copier;

import java.util.Date;

public class Pojo2 {
	String naMe;
	String password;
	Date date;
	int interger;
	boolean bool;

	public String getNaMe() {
		return naMe;
	}

	public void setNaMe(String naMe) {
		this.naMe = naMe;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getInterger() {
		return interger;
	}

	public void setInterger(int interger) {
		this.interger = interger;
	}

	public boolean isBool() {
		return bool;
	}

	public void setBool(boolean bool) {
		this.bool = bool;
	}

	@Override
	public String toString() {
		return "Pojo2 [naMe=" + naMe + ", password=" + password + ", date="
				+ date + ", interger=" + interger + ", bool=" + bool + "]";
	}

}
