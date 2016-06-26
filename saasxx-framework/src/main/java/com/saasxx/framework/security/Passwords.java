package com.saasxx.framework.security;

import java.io.UnsupportedEncodingException;

import org.springframework.util.Assert;

import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Encodes;

/**
 * 密码加密器，采用SHA256算法
 * 
 * @author lujijiang
 *
 */
public class Passwords {
	/**
	 * 密码散列算法
	 */
	private static final String HASH_ALGORITHM = Digests.SHA256;
	/**
	 * 密码循环散列次数
	 */
	private static final int HASH_INTERATIONS = 1024;
	/**
	 * 密码盐大小
	 */
	public static final int SALT_SIZE = 8;

	/**
	 * 加密密码
	 * 
	 * @param userVo
	 * @param salt
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encryptPassword(String password, String salt) {
		Assert.notNull(password, "密码不能为空");
		try {
			byte[] hashPassword = Digests.digest(password.getBytes("UTF-8"), HASH_ALGORITHM,
					salt == null ? null : Encodes.decodeHex(salt), HASH_INTERATIONS);
			password = Encodes.encodeHex(hashPassword);
			return password;
		} catch (UnsupportedEncodingException e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 生成密码盐
	 * 
	 * @return
	 */
	public static String generateSalt() {
		return Encodes.encodeHex(Digests.generateSalt(SALT_SIZE));
	}

}
