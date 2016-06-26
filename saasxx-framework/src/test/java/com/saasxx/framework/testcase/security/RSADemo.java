package com.saasxx.framework.testcase.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;

import com.saasxx.framework.security.RSAs;

/**
 * RSA非对称加密算法测试
 * 
 * @author lujijiang
 *
 */
public class RSADemo {

	/**
	 * 私钥，自己留存，用于加密，不可外泄
	 */
	private static String privateKey;

	/**
	 * 公钥，分发给合作方，用于解密
	 */
	private static String publicKey;

	static {
		try {
			// 生成公私钥对
			KeyPair keyPair = RSAs.generateKeyPair();
			privateKey = RSAs.keyToString(keyPair.getPrivate());
			System.out.println("私钥：" + privateKey);
			publicKey = RSAs.keyToString(keyPair.getPublic());
			System.out.println("公钥：" + publicKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String encryptData(String source)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException, ClassNotFoundException, IOException {
		// 使用公钥加密原始字符串
		byte[] data = RSAs.encrypt(source.getBytes("UTF-8"), RSAs.stringToKey(privateKey));
		System.out.println("加密后数据：" + new String(data, "UTF-8"));
		return Base64.encodeBase64String(data);
	}

	private static void decryptData(String target)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, ClassNotFoundException, IOException, UnsupportedEncodingException {
		// 使用私钥解密被加密的数据
		byte[] data = Base64.decodeBase64(target);
		byte[] result = RSAs.decrypt(data, RSAs.stringToKey(publicKey));
		System.out.println("解密后数据：" + new String(result, "UTF-8"));
	}

	public static void main(String[] args)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException, ClassNotFoundException, IOException {
		// 定义原始数据字符串
		String source = "我是原始数据~~~~~~~~";
		System.out.println("原始数据：" + source);
		// 加密数据
		String target = encryptData(source);
		System.out.println("加密后数据再经过Base64编码，方便传输：" + target);
		// 解密数据
		decryptData(target);
	}

}
