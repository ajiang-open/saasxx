package com.saasxx.framework.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;

/**
 * 非对称加密RSA工具类
 * 
 * @author lujijiang
 *
 */
public class RSAs {

	/**
	 * 将钥转换为字节数组
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public static byte[] keyToBytes(Key key) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
			objectOutputStream.writeObject(key);
		}
		return outputStream.toByteArray();
	}

	/**
	 * 将钥转换为字符串
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public static String keyToString(Key key) throws IOException {
		byte[] data = keyToBytes(key);
		return Base64.encodeBase64String(data);
	}

	/**
	 * 将字节数组转换为钥
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Key bytesToKey(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
			return (Key) objectInputStream.readObject();
		}
	}

	/**
	 * 将字符串转换为钥
	 * 
	 * @param string
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Key stringToKey(String string) throws ClassNotFoundException, IOException {
		byte[] data = Base64.decodeBase64(string);
		return bytesToKey(data);
	}

	/**
	 * 生成公私钥对
	 * 
	 * @return 公私钥对
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IOException
	 */
	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024, new SecureRandom());
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		return keyPair;
	}

	/**
	 * 使用公钥加密
	 * 
	 * @param source
	 *            原始数据
	 * @param publicKey
	 *            公钥
	 * @return 加密后数据
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] encrypt(byte[] source, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(source);
	}

	/**
	 * 使用私钥解密
	 * 
	 * @param target
	 *            加密后数据
	 * @param privateKey
	 *            私钥
	 * @return 原始数据
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] decrypt(byte[] target, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(target);
	}

}
