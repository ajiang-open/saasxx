package com.saasxx.framework.web;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Encodes;
import com.saasxx.framework.lang.Mirrors;
import com.saasxx.framework.web.filter.WebsFilter;
import com.saasxx.framework.web.filter.WebsFilter.WebInfo;

/**
 * Web工具类
 * 
 * @author lujijiang
 * 
 */
public class Webs {
	/**
	 * JSONP请求函数参数名
	 */
	public static final String JSONP_CALLBACK_NAME = "jsonpcallback";

	public static final Field WEB_INFO_THREAD_LOCAL_FIELD;
	static {
		try {
			WEB_INFO_THREAD_LOCAL_FIELD = WebsFilter.class
					.getDeclaredField("WEB_INFO_THREAD_LOCAL");
			WEB_INFO_THREAD_LOCAL_FIELD.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw Lang.unchecked(e);
		} catch (SecurityException e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 根据传入的request获取当前有效的cookieMap
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, Cookie> cookieMap(HttpServletRequest request) {
		Map<String, Cookie> cookieMap = Lang.newMap();
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				cookieMap.put(cookie.getName(), cookie);
			}
		}
		return cookieMap;
	}

	public static boolean isAjaxRequest(HttpServletRequest request) {
		return "XMLHttpRequest".equalsIgnoreCase(request
				.getHeader("X-Requested-With"));
	}

	public static String getIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (Lang.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (Lang.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (Lang.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		// 多个路由时，取第一个非unknown的ip
		String[] ss = ip.split(",");
		for (String s : ss) {
			if (!"unknown".equalsIgnoreCase(s)) {
				ip = s;
				break;
			}
		}
		return ip;
	}

	/**
	 * 从response对象获取字符流写入器，无论response是否被调用过getOutputStream
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	public static PrintWriter getWriter(HttpServletResponse response)
			throws IOException {
		try {
			return response.getWriter();
		} catch (Exception e) {
			return new PrintWriter(response.getOutputStream());
		}
	}

	/**
	 * 对URL增加参数，并且添加默认的时间戳
	 * 
	 * @param url
	 * @param args
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String addParamsWithTimestamp(String url, Object... args)
			throws UnsupportedEncodingException {
		url += (url.indexOf("?") == -1 ? "?" : "&").concat(Long.toString(
				System.currentTimeMillis(), 36));
		return addParams(url, args);
	}

	/**
	 * 对URL增加参数
	 * 
	 * @param url
	 * @param args
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static String addParams(String url, Object... args)
			throws UnsupportedEncodingException {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(url);
		if (args != null) {
			int n = 0;
			for (int i = 0; i < args.length; i += 2) {
				if (args.length > i + 1) {
					Object value = args[i + 1];
					if (value != null) {
						if (n == 0) {
							if (url.indexOf("?") != -1) {
								urlBuilder.append("&");
							} else {
								urlBuilder.append("?");
							}
						} else {
							urlBuilder.append("&");
						}
						Object name = args[i];
						urlBuilder.append(name);
						urlBuilder.append("=");
						urlBuilder.append(URLEncoder.encode(value.toString(),
								"UTF-8"));
						n++;
					}
				}
			}
		}
		return urlBuilder.toString();
	}

	/**
	 * 获取绝对地址
	 * 
	 * @param req
	 * @return
	 */
	public static String getAbsUrl(HttpServletRequest req) {
		String encoding = req.getCharacterEncoding();
		encoding = encoding != null ? encoding : "UTF-8";
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(getAbsUri(req));
		Map<String, String[]> paramMap = req.getParameterMap();
		if (!paramMap.isEmpty()) {
			urlBuilder.append("?");
			for (String name : paramMap.keySet()) {
				String[] values = paramMap.get(name);
				for (String value : values) {
					if (urlBuilder.charAt(urlBuilder.length() - 1) != '?') {
						urlBuilder.append('&');
					}
					urlBuilder.append(name);
					urlBuilder.append('=');
					try {
						urlBuilder.append(URLEncoder.encode(value, encoding));
					} catch (UnsupportedEncodingException e) {
						throw Lang.unchecked(e);
					}
				}
			}
		}
		return urlBuilder.toString();
	}

	/**
	 * 获取绝对URI
	 * 
	 * @param req
	 * @return
	 */
	public static String getAbsUri(HttpServletRequest req) {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(getAppUrl(req));
		urlBuilder.append(req.getRequestURI().substring(
				req.getContextPath().length()));
		return urlBuilder.toString();
	}

	/**
	 * 获取应用根地址
	 * 
	 * @param req
	 * @return
	 */
	public static String getAppUrl(HttpServletRequest req) {
		String host = req.getHeader("Host");
		if (host != null) {
			String scheme = req.getHeader("X-Forwarded-Proto");
			scheme = scheme != null ? scheme : req.getScheme();
			StringBuilder appUrlBuilder = new StringBuilder();
			appUrlBuilder.append(scheme);
			appUrlBuilder.append("://");
			appUrlBuilder.append(host);
			appUrlBuilder.append(req.getContextPath());
			return appUrlBuilder.toString();
		}
		String requestUrl = req.getRequestURL().toString();
		int pathLength = req.getRequestURI().length()
				- req.getContextPath().length();
		return requestUrl.substring(0, requestUrl.length() - pathLength);
	}

	/**
	 * 通过名字寻找Cookie
	 * 
	 * @param req
	 * @param name
	 * @return
	 */
	public static Cookie getCookie(HttpServletRequest req, String name) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie;
				}
			}
		}
		return null;
	}

	/**
	 * 根据给定的URL和参数生成一个自动提交的Form表单字符串
	 * 
	 * @param url
	 * @param paramMap
	 * @return
	 */
	public static String formRequest(String url, String method,
			Map<String, Object> paramMap, String charset) {
		StringBuilder htmlBuilder = new StringBuilder();
		String id = "id_" + UUID.randomUUID().toString().replace("-", "");
		htmlBuilder.append("<form id=\"");
		htmlBuilder.append(id);
		htmlBuilder.append("\" method=\"");
		htmlBuilder.append(method);
		htmlBuilder.append("\" action=\"");
		htmlBuilder.append(url);
		htmlBuilder.append("\"");
		if (charset != null) {
			htmlBuilder.append(" _input_charset=\"");
			htmlBuilder.append(charset);
			htmlBuilder.append("\"");
		}
		htmlBuilder.append(">");
		for (String name : paramMap.keySet()) {
			Object value = paramMap.get(name);
			if (!Lang.isEmpty(value)) {
				htmlBuilder.append("<input type=\"hidden\" name=\"");
				htmlBuilder.append(name);
				htmlBuilder.append("\" value=\"");
				htmlBuilder.append(Encodes.escapeHtml(value.toString()));
				htmlBuilder.append("\">");
			}
		}
		// 注入参数结束
		htmlBuilder.append("</form><script>document.getElementById('");
		htmlBuilder.append(id);
		htmlBuilder.append("').submit();</script>");
		return htmlBuilder.toString();
	}

	/**
	 * 根据给定的URL和参数生成一个自动提交的Form表单字符串
	 * 
	 * @param url
	 * @param paramMap
	 * @return
	 */
	public static String formPost(String url, Map<String, Object> paramMap,
			String charset) {
		return formRequest(url, "post", paramMap, charset);
	}

	/**
	 * 根据给定的URL和参数生成一个自动提交的Form表单字符串
	 * 
	 * @param url
	 * @param paramMap
	 * @return
	 */
	public static String formGet(String url, Map<String, Object> paramMap,
			String charset) {
		return formRequest(url, "get", paramMap, charset);
	}

	/**
	 * 将QueryString转换为对象
	 * 
	 * @param type
	 * @param queryString
	 * @param charset
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws UnsupportedEncodingException
	 */
	public static <T> T getParameterObject(Class<T> type, String queryString,
			String charset) {
		try {
			T obj = type.newInstance();
			Map<String, PropertyDescriptor> propertyDescriptors = Mirrors
					.getPropertyMap(type);
			String[] params = queryString.split("&");
			for (String param : params) {
				int p = param.indexOf("=");
				String key;
				String value;
				if (p == -1) {
					key = URLDecoder.decode(param, charset);
				} else {
					key = URLDecoder.decode(param.substring(0, p), charset);
				}
				if (propertyDescriptors.containsKey(key)) {
					PropertyDescriptor propertyDescriptor = propertyDescriptors
							.get(key);
					Method writeMethod = propertyDescriptor.getWriteMethod();
					if (writeMethod != null) {
						if (p == -1) {
							value = null;
						} else {
							value = param.length() > p + 1 ? URLDecoder.decode(
									param.substring(p + 1), charset) : "";
						}
						writeMethod.invoke(
								obj,
								Lang.convert(value,
										propertyDescriptor.getPropertyType()));
					}
				}
			}
			return obj;
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 将查询参数转换为Map
	 * 
	 * @param queryString
	 * @param charset
	 * @return
	 */
	public static Map<String, Object> getParameterMap(String queryString,
			String charset) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		try {
			String[] params = queryString.split("&");
			for (String param : params) {
				int p = param.indexOf("=");
				String key;
				String value;
				if (p == -1) {
					key = URLDecoder.decode(param, charset);
					value = null;
				} else {
					key = URLDecoder.decode(param.substring(0, p), charset);
					value = param.length() > p + 1 ? URLDecoder.decode(
							param.substring(p + 1), charset) : "";
				}
				if (map.containsKey(key)) {
					Object oldValue = map.get(key);
					if (oldValue != null && oldValue.getClass().isArray()) {
						String[] array = (String[]) oldValue;
						String[] newArray = new String[array.length + 1];
						System.arraycopy(array, 0, newArray, 0, array.length);
						newArray[array.length] = value;
						map.put(key, newArray);
					} else {
						map.put(key, new String[] { (String) oldValue, value });
					}
				} else {
					map.put(key, value);
				}
			}
		} catch (Exception e) {
			throw Lang.unchecked(e);
		}
		return map;
	}

	/**
	 * 将请求参数字符串转换为Map，可制定特定编码避免乱码
	 * 
	 * @param request
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Object> getParameterMap(
			HttpServletRequest request, String charset) throws IOException {
		String parameterString = getParameterString(request, charset);
		return getParameterMap(parameterString, charset);
	}

	/**
	 * 将参数转换为制定类型的对象，可制定特定的编码来避免乱码
	 * 
	 * @param type
	 * @param request
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static <T> T getParameterObject(Class<T> type,
			HttpServletRequest request, String charset) throws IOException {
		String parameterString = getParameterString(request, charset);
		return getParameterObject(type, parameterString, charset);
	}

	/**
	 * 获取参数字符串，包括queryString和post过来的那部分数据（Post时会关闭数据流，不可用于上传文件的请求的解析）
	 * 
	 * @param request
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static String getParameterString(HttpServletRequest request,
			String charset) throws IOException {
		StringBuilder queryBuilder = new StringBuilder();
		String queryString = request.getQueryString();
		if (queryString != null) {
			queryBuilder.append(queryString);
		}
		if ("post".equalsIgnoreCase(request.getMethod())) {
			if (request.getContentType() != null
					&& request.getContentType().contains("multipart/form-data")) {
				throw new IllegalStateException(String.format(
						"Can't handle the content type which contains %s",
						"multipart/form-data"));
			}
			InputStream is = request.getInputStream();
			try {
				String postString = IOUtils.toString(is, charset);
				if (postString.length() > 0) {
					if (queryBuilder.length() > 0) {
						queryBuilder.append('&');
					}
					queryBuilder.append(postString);
				}
			} finally {
				is.close();
			}
		}
		String parameterString = queryBuilder.toString();
		return parameterString;
	}

	/**
	 * 获取当前线程绑定的Request对象，需要配置过滤器WebsFilter，且在最前
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static HttpServletRequest getRequest() {
		try {
			ThreadLocal<WebInfo> threadLocal = (ThreadLocal<WebInfo>) WEB_INFO_THREAD_LOCAL_FIELD
					.get(WebsFilter.class);
			WebInfo webInfo = threadLocal.get();
			return webInfo == null ? null : webInfo.getRequest();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw Lang.unchecked(e);
		}
	}

	/**
	 * 获取当前线程绑定的Request对象，需要配置过滤器WebsFilter，且在最前
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static HttpServletResponse getResponse() {
		try {
			ThreadLocal<WebInfo> threadLocal = (ThreadLocal<WebInfo>) WEB_INFO_THREAD_LOCAL_FIELD
					.get(WebsFilter.class);
			WebInfo webInfo = threadLocal.get();
			return webInfo == null ? null : webInfo.getResponse();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw Lang.unchecked(e);
		}
	}

}
