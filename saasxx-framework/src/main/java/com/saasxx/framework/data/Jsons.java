package com.saasxx.framework.data;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasxx.framework.Lang;
import com.saasxx.framework.io.UnsafeStringReader;
import com.saasxx.framework.io.UnsafeStringWriter;

/**
 * 用于json转换，使用jackson实现
 * 
 * @author lujijiang
 * 
 */
public class Jsons {

	private static ObjectMapper mapper = new ObjectMapper();
	private static ObjectMapper mapperSerialized = new ObjectMapper();
	static {
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapperSerialized.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	}

	private Jsons() {
	}

	/**
	 * 将一个对象转换为JSON字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		UnsafeStringWriter stringWriter = new UnsafeStringWriter();
		try {
			mapper.writeValue(stringWriter, obj);
		} catch (JsonGenerationException e) {
			throw Lang.unchecked(e);
		} catch (JsonMappingException e) {
			throw Lang.unchecked(e);
		} catch (IOException e) {
			throw Lang.unchecked(e);
		}
		return stringWriter.toString();
	}

	/**
	 * 将一个对象转换为JSON字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String toJsonSerialized(Object obj) {
		UnsafeStringWriter stringWriter = new UnsafeStringWriter();
		try {
			mapperSerialized.writeValue(stringWriter, obj);
		} catch (JsonGenerationException e) {
			throw Lang.unchecked(e);
		} catch (JsonMappingException e) {
			throw Lang.unchecked(e);
		} catch (IOException e) {
			throw Lang.unchecked(e);
		}
		return stringWriter.toString();
	}

	/**
	 * 将JSON字符串转换为指定类型的对象
	 * 
	 * @param json
	 * @param type
	 * @return
	 */
	public static <T> T fromJson(String json, Class<T> type) {
		UnsafeStringReader stringReader = new UnsafeStringReader(json);
		try {
			return mapper.readValue(stringReader, type);
		} catch (JsonParseException e) {
			throw Lang.unchecked(e);
		} catch (JsonMappingException e) {
			throw Lang.unchecked(e);
		} catch (IOException e) {
			throw Lang.unchecked(e);
		} finally {
			stringReader.close();
		}
	}

	/**
	 * 将JSON字符串转换为指定类型的对象
	 * 
	 * @param json
	 * @param type
	 * @return
	 */
	public static <T> T fromJsonSerialized(String json, Class<T> type) {
		UnsafeStringReader stringReader = new UnsafeStringReader(json);
		try {
			return mapperSerialized.readValue(stringReader, type);
		} catch (JsonParseException e) {
			throw Lang.unchecked(e);
		} catch (JsonMappingException e) {
			throw Lang.unchecked(e);
		} catch (IOException e) {
			throw Lang.unchecked(e);
		} finally {
			stringReader.close();
		}
	}
}
