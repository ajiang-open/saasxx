package com.saasxx.framework.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.saasxx.framework.Lang;
import com.saasxx.framework.io.UnsafeByteArrayInputStream;
import com.saasxx.framework.io.UnsafeStringReader;

/**
 * Xml解析工具类（依赖JDK6及以上版本）
 * 
 * @author lujijiang
 *
 */
public class Xmls {
	/**
	 * XML处理器
	 * 
	 * @author lujijiang
	 *
	 */
	public static interface XpathHandler {
		/**
		 * 处理XML内容
		 * 
		 * @param source
		 * @param xpath
		 * @throws Exception
		 */
		void handle(Document document, XPath xpath) throws Exception;
	}

	/**
	 * 根据阅读器处理Xml
	 * 
	 * @param xmlHandler
	 * @param reader
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void handle(XpathHandler xmlHandler, Reader reader) {
		try {
			InputSource source = new InputSource(reader);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(source);
			XPath xpath = XPathFactory.newInstance().newXPath();
			xmlHandler.handle(document, xpath);
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 根据XML字符串处理Xml
	 * 
	 * @param xmlHandler
	 * @param xml
	 */
	public static void handle(XpathHandler xmlHandler, String xml) {
		handle(xmlHandler, new UnsafeStringReader(xml));
	}

	/**
	 * 根据XML输入流处理Xml
	 * 
	 * @param xmlHandler
	 * @param is
	 * @param charset
	 * @throws IOException
	 */
	public static void handle(XpathHandler xmlHandler, InputStream is,
			String charset) throws IOException {
		Reader reader = new InputStreamReader(is, charset);
		try {
			handle(xmlHandler, reader);
		} finally {
			reader.close();
		}
	}

	/**
	 * 根据XML文件处理Xml
	 * 
	 * @param xmlHandler
	 * @param file
	 * @param charset
	 * @throws IOException
	 */
	public static void handle(XpathHandler xmlHandler, File file, String charset)
			throws IOException {
		InputStream inputStream = new FileInputStream(file);
		try {
			handle(xmlHandler, inputStream, charset);
		} catch (IOException e) {
			throw e;
		} finally {
			inputStream.close();
		}
	}

	/**
	 * 根据字节数组处理Xml
	 * 
	 * @param xmlHandler
	 * @param bytes
	 * @param charset
	 */
	public static void handle(XpathHandler xmlHandler, byte[] bytes,
			String charset) {
		InputStream inputStream = new UnsafeByteArrayInputStream(bytes);
		try {
			handle(xmlHandler, inputStream, charset);
		} catch (IOException e) {
			throw Lang.unchecked(e);
		}
	}
}
