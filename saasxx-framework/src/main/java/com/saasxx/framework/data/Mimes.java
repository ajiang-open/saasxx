package com.saasxx.framework.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.saasxx.framework.Lang;

/**
 * 元数据工具类，用于猜测文件的ContentType
 * 
 * @author lujijiang
 *
 */
public class Mimes {

	final static String MIME_FILE_PATH = "mimes.properties";

	final static Map<String, String> mimeMap = getMimeMap();

	private static Map<String, String> getMimeMap() {
		Map<String, String> mimeMap = Lang.newMap();
		try (InputStream is = Mimes.class.getResourceAsStream(MIME_FILE_PATH)) {
			String txt = IOUtils.toString(is, "UTF-8");
			String[] lines = txt.split("\n+");
			for (String line : lines) {
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				int i = line.indexOf("=");
				if (i == -1) {
					continue;
				}
				String suffix = line.substring(0, i).trim().toLowerCase();
				String type = line.substring(i + 1).trim().toLowerCase();
				mimeMap.put(suffix, type);
			}
			mimeMap.put("", mimeMap.get("*"));
		} catch (IOException e) {
			throw Lang.unchecked(e);
		}
		return mimeMap;
	}

	/**
	 * 根据文件后缀猜测文件类型，如果猜不到，则返回null
	 * 
	 * @param suffix
	 *            文件后缀名
	 * @return
	 */
	public static String guessContentType(String suffix) {
		String type = mimeMap.get(suffix);
		return type;
	}
}
