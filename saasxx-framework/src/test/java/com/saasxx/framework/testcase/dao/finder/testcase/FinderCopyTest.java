package com.saasxx.framework.testcase.dao.finder.testcase;

import java.util.Date;

import org.junit.Test;

import com.saasxx.framework.dao.finder.Finders;
import com.saasxx.framework.dao.finder.facade.Finder;

public class FinderCopyTest {
	@Test
	public void testWhere() {
		Finder finder = Finders.newFinder();
		FPojo modelFPojo = finder.from(FPojo.class);
		finder.where(modelFPojo.getAge()).equal(33);
		finder.where(modelFPojo.getPojoEnum()).equal(FPojoEnum.aaa);
		Finder newFinder = finder.copy();
		System.out.println("orgin:" + finder.toQueryContent());
		System.out.println();
		System.out.println("copier:" + newFinder.toQueryContent());
		System.out.println("----------------------------------");
		finder.where(modelFPojo.getEmail()).equal("aaa@aaa.com");
		System.out.println("orgin:" + finder.toQueryContent());
		System.out.println();
		System.out.println("copier:" + newFinder.toQueryContent());
		System.out.println("----------------------------------");
		newFinder.where(modelFPojo.getBirthday()).equal(new Date());
		System.out.println("orgin:" + finder.toQueryContent());
		System.out.println();
		System.out.println("copier:" + newFinder.toQueryContent());
	}

	// @Test
	public void testOrder() {
		Finder finder = Finders.newFinder();
		FPojo modelFPojo = finder.from(FPojo.class);
		finder.where(modelFPojo.getAge()).equal(33);
		Finder newFinder = finder.copy();
		System.out.println("orgin:" + finder.toQueryContent());
		System.out.println();
		System.out.println("copier:" + newFinder.toQueryContent());
		System.out.println("----------------------------------");
		finder.order(modelFPojo.getAge()).asc();
		System.out.println("orgin:" + finder.toQueryContent());
		System.out.println();
		System.out.println("copier:" + newFinder.toQueryContent());
		System.out.println("----------------------------------");
		newFinder.order(modelFPojo.getAge()).desc();
		System.out.println("orgin:" + finder.toQueryContent());
		System.out.println();
		System.out.println("copier:" + newFinder.toQueryContent());
	}
}
