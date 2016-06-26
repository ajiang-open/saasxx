package com.saasxx.framework.lang;

import java.math.BigDecimal;

/**
 * 数学工具类
 * 
 * @author lujijiang
 *
 */
public class Maths {
	/**
	 * 将数字准确转换为BigDecimal
	 * 
	 * @param number
	 * @return
	 */
	public static BigDecimal toBigDecimal(Object number) {
		if (number == null) {
			return null;
		}
		if (number instanceof Double) {
			return BigDecimal.valueOf((Double) number);
		}
		if (number instanceof Long) {
			return BigDecimal.valueOf((Long) number);
		}
		return new BigDecimal(number.toString());
	}

	public static void main(String[] args) {
		System.out.println(4.015 * 100);
	}
}
