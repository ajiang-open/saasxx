package com.saasxx.framework.testcase.dao.jdbc.testcase;

import com.saasxx.framework.dao.jdbc.Sql;

public class SqlTest {

	public static void main(String[] args) {
		Sql sql = new Sql() {
			{
				select("name,age");
				from("user");
				where("name = :name", "Pitt");
				and("age>333");
			}
		};
		System.out.println(sql);

	}

}
