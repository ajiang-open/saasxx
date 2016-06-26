package com.saasxx.framework.io;

import java.io.File;
import java.net.URISyntaxException;

import com.saasxx.framework.Lang;

/**
 * 文件池
 * 
 * @author lujijiang
 *
 */
public class FilePool {

	/**
	 * 获取实际路径，其中~表示用户目录
	 * 
	 * @param path
	 * @return
	 */
	public static String getPath(String path) {
		return path.replace("~", System.getProperty("user.home"));
	}

	/**
	 * 根目录
	 */
	private File rootDir;

	/**
	 * 构造函数
	 * 
	 * @param rootDir
	 */
	public FilePool(File rootDir) {
		rootDir.mkdirs();
		if (!rootDir.isDirectory()) {
			throw Lang.newException("%s 不是一个目录", rootDir);
		}
		this.rootDir = rootDir;
	}

	/**
	 * 构造函数
	 * 
	 * @param rootDirPath
	 */
	public FilePool(String rootDirPath) {
		this(new File(getPath(rootDirPath)));
	}

	/**
	 * 取得指定路径的文件
	 * 
	 * @param path
	 * @return
	 */
	public File file(String path) {
		return new File(rootDir, path);
	}

	public static void main(String[] args) throws URISyntaxException {
		System.out.println(new File("~/filepool/hjw".replace("~", System.getProperty("user.home"))).getAbsolutePath());
	}
}
