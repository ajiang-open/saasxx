package com.saasxx.framework.lang;

import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import com.saasxx.framework.io.UnsafeByteArrayInputStream;
import com.saasxx.framework.io.UnsafeByteArrayOutputStream;

/**
 * Gzip压缩工具
 * 
 * @author lujijiang
 * 
 */
public class Gzips {

	/**
	 * 将字节流经过gzip压缩后返回
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] gzip(byte[] in) throws IOException {
		UnsafeByteArrayOutputStream byteArrayOutputStream = new UnsafeByteArrayOutputStream();
		GZIPOutputStream gzipOutputStream = new GZIPOutputStream(
				byteArrayOutputStream);
		try {
			gzipOutputStream.write(in);
		} finally {
			gzipOutputStream.close();
		}
		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * 将字节流经过gzip解压缩后返回
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] ungzip(byte[] in) throws IOException {
		UnsafeByteArrayInputStream byteArrayInputStream = new UnsafeByteArrayInputStream(
				in);
		GZIPInputStream gzipInputStream = new GZIPInputStream(
				byteArrayInputStream);
		UnsafeByteArrayOutputStream byteArrayOutputStream = new UnsafeByteArrayOutputStream();
		try {
			IOUtils.copy(gzipInputStream, byteArrayOutputStream);
		} finally {
			gzipInputStream.close();
		}
		return byteArrayOutputStream.toByteArray();
	}
}
