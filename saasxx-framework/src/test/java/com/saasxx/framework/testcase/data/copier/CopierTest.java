package com.saasxx.framework.testcase.data.copier;

import java.io.IOException;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Beans;

public class CopierTest {

	@Test
	public void testCopy() throws IOException {
		final Pojo1 pojo1 = new Pojo1();
		pojo1.setName("tester");
		pojo1.setPassword("123456");
		pojo1.setDate(DateTime.parse("2010-03-02").toDate());

		final Pojo2 pojo2 = new Pojo2();
		pojo2.setDate(new Date());
		pojo2.setInterger(333);
		pojo2.setBool(true);

		System.out.println(pojo2);
		System.out.println(Lang.timing(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 10000000; i++) {
					Beans.from(pojo1)
					// .excludePrimitiveZero()
					// .excludePrimitiveFalse()
					// .excludes("password")
							.caseInsensitive().to(pojo2);
				}
			}
		}));

		System.out.println(pojo2);
	}
}
