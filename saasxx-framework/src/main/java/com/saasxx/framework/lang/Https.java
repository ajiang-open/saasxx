package com.saasxx.framework.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.saasxx.framework.io.UnsafeByteArrayInputStream;

/**
 * HTTP工具类
 * 
 * @author lujijiang
 * 
 */
public class Https {

	private static Log log = LogFactory.getLog(Https.class);

	public static final String PROTOCOL_HTTPS = "https";
	public static final String PROTOCOL_HTTP = "http";

	public static final String BOUNDARY = Long.toHexString(System
			.currentTimeMillis());
	public static final String CRLF = "\r\n";

	public enum HttpMethod {
		GET, POST, HEAD, OPTIONS, PUT, PATCH, DELETE, TRACE
	}

	static class HttpRequestUpload {
		String name;
		File file;
	}

	/**
	 * Http请求器
	 * 
	 * @author lujijiang
	 * 
	 */
	public static class HttpRequest {
		public static final String HEADER_AUTHORIZATION = "Authorization";
		public static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";

		public static final String HEADER_ACCEPT = "Accept";
		public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
		public static final String HEADER_ACCEPT_LANGUAGE = "Accept-age";

		public static final String HEADER_CONTENT_TYPE = "Content-Type";
		public static final String HEADER_CACHE_CONTROL = "Cache-Control";
		public static final String HEADER_CONNECTION = "Connection";

		public static final String HEADER_COOKIE = "Cookie";
		public static final String HEADER_USER_AGENT = "User-Agent";
		public static final String HEADER_HOST = "Host";

		/**
		 * URL地址
		 */
		private String url;

		/**
		 * 代理主机名或者域名
		 */
		private String proxyHostname;

		/**
		 * 代理端口
		 */
		private int proxyPort;

		/**
		 * 代理类型
		 */
		private Proxy.Type proxyType;

		/**
		 * 代理认证用户名
		 */
		private String proxyUsername;
		/**
		 * 代理认证密码
		 */
		private String proxyPassword;
		/**
		 * 代理的Basic认证编码
		 */
		private String proxyBasicAuthorizationEncoding = "UTF-8";
		/**
		 * URL编码
		 */
		private String encoding = "UTF-8";
		/**
		 * 连接握手时间（默认一分钟）
		 */
		private int connectTimeout = 60000;
		/**
		 * 数据读取时间（默认两分钟）
		 */
		private int readTimeout = 120000;

		/**
		 * 用户名，用于Basic认证
		 */
		private String username;

		/**
		 * 密码，用于Basic认证
		 */
		private String password;
		/**
		 * 字符编码，用于Basic认证
		 */
		private String basicAuthorizationEncoding = "UTF-8";

		/**
		 * SSL证书文件流
		 */
		private InputStream certStream;
		/**
		 * SSL证书文件密码
		 */
		private String certPassword;

		/**
		 * 内容字节数组
		 */
		private byte[] content;

		/**
		 * 参数映射
		 */
		private Map<String, List<String>> parameterMap = newMap();
		/**
		 * 文件数据参数映射
		 */
		private List<HttpRequestUpload> uploads = newList();
		/**
		 * header属性
		 */
		private Map<String, String> headerPropertyMap = newMap();

		/**
		 * 要上传的cookie
		 */
		private List<HttpCookie> cookies = newList();

		private HttpRequest(String url) {
			this.url = url;
		}

		/**
		 * @param proxyHostname
		 *            the proxyHostname to set
		 */
		public HttpRequest setProxyHostname(String proxyHostname) {
			this.proxyHostname = proxyHostname == null ? null : proxyHostname
					.trim();
			return this;
		}

		/**
		 * @param proxyPort
		 *            the proxyPort to set
		 */
		public HttpRequest setProxyPort(int proxyPort) {
			this.proxyPort = proxyPort;
			return this;
		}

		/**
		 * @param proxyType
		 *            the proxyType to set
		 */
		public HttpRequest setProxyType(Proxy.Type proxyType) {
			this.proxyType = proxyType;
			return this;
		}

		/**
		 * @param proxyUsername
		 *            the proxyUsername to set
		 */
		public HttpRequest setProxyUsername(String proxyUsername) {
			this.proxyUsername = proxyUsername == null ? null : proxyUsername
					.trim();
			return this;
		}

		/**
		 * @param proxyPassword
		 *            the proxyPassword to set
		 */
		public HttpRequest setProxyPassword(String proxyPassword) {
			this.proxyPassword = proxyPassword;
			return this;
		}

		/**
		 * @param proxyBasicAuthorizationEncoding
		 *            the proxyBasicAuthorizationEncoding to set
		 */
		public HttpRequest setProxyBasicAuthorizationEncoding(
				String proxyBasicAuthorizationEncoding) {
			this.proxyBasicAuthorizationEncoding = proxyBasicAuthorizationEncoding;
			return this;
		}

		public HttpRequest setEncoding(String encoding) {
			this.encoding = encoding;
			return this;
		}

		public HttpRequest setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
			return this;
		}

		public HttpRequest setReadTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
			return this;
		}

		public HttpRequest setUsername(String username) {
			this.username = username;
			return this;
		}

		public HttpRequest setPassword(String password) {
			this.password = password;
			return this;
		}

		public HttpRequest setBasicAuthorizationEncoding(
				String basicAuthorizationEncoding) {
			this.basicAuthorizationEncoding = basicAuthorizationEncoding;
			return this;
		}

		public HttpRequest setCertStream(InputStream certStream) {
			this.certStream = certStream;
			return this;
		}

		public HttpRequest setCertPassword(String certPassword) {
			this.certPassword = certPassword;
			return this;
		}

		public HttpRequest setHeaderContentType(String headerContentType) {
			return setHeader(HEADER_CONTENT_TYPE, headerContentType);
		}

		public HttpRequest setHeaderAccept(String headerAccept) {
			return setHeader(HEADER_ACCEPT, headerAccept);
		}

		public HttpRequest setHeaderAcceptEncoding(String headerAcceptEncoding) {
			return setHeader(HEADER_ACCEPT_ENCODING, headerAcceptEncoding);
		}

		public HttpRequest setHeaderAcceptage(String headerAcceptage) {
			return setHeader(HEADER_ACCEPT_LANGUAGE, headerAcceptage);
		}

		public HttpRequest setHeaderCacheControl(String headerCacheControl) {
			return setHeader(HEADER_CACHE_CONTROL, headerCacheControl);
		}

		public HttpRequest setHeaderConnection(String headerConnection) {
			return setHeader(HEADER_CONNECTION, headerConnection);
		}

		public HttpRequest setHeaderUserAgent(String headerUserAgent) {
			return setHeader(HEADER_USER_AGENT, headerUserAgent);
		}

		public HttpRequest setHeaderHost(String headerHost) {
			return setHeader(HEADER_HOST, headerHost);
		}

		public HttpRequest setHeader(String name, String value) {
			if (HEADER_COOKIE.equalsIgnoreCase(name)) {
				throw new UnsupportedOperationException(
						"Use addCookies method set cookie");
			}
			if (HEADER_AUTHORIZATION.equalsIgnoreCase(name)) {
				throw new UnsupportedOperationException(
						"Use setUsername and setPassword method for Basic Authorization");
			}
			this.headerPropertyMap.put(name, value);
			return this;
		}

		/**
		 * 设置内容
		 * 
		 * @param content
		 * @return
		 */
		public HttpRequest setContent(byte[] content) {
			this.content = content;
			return this;
		}

		/**
		 * 设置内容
		 * 
		 * @param content
		 * @return
		 * @throws IOException
		 */
		public HttpRequest setContent(InputStream content) throws IOException {
			return setContent(IOUtils.toByteArray(content));
		}

		/**
		 * 设置内容
		 * 
		 * @param content
		 * @return
		 */
		public HttpRequest setContent(String content) {
			return setContent(content.getBytes());
		}

		/**
		 * 设置内容
		 * 
		 * @param content
		 * @param encoding
		 * @return
		 * @throws UnsupportedEncodingException
		 */
		public HttpRequest setContent(String content, String encoding)
				throws UnsupportedEncodingException {
			return setContent(content.getBytes(encoding));
		}

		/**
		 * 添加参数
		 * 
		 * @param name
		 * @param values
		 * @return
		 */
		public HttpRequest addParameters(String name, String... values) {
			if (name != null && values != null) {
				name = name.trim();
				List<String> valueList = parameterMap.get(name);
				if (valueList == null) {
					valueList = newList(values);
					parameterMap.put(name, valueList);
				} else {
					valueList.addAll(newList(values));
				}
			}
			return this;
		}

		/**
		 * 添加上传数据
		 * 
		 * @param name
		 * @param values
		 * @return
		 */
		public HttpRequest addUploads(String name, String filename, byte[] data) {
			try {
				File file = new File(System.getProperty("java.io.tmpdir")
						.concat("/lang/Https/uploads"), filename);
				file.getParentFile().mkdirs();
				FileUtils.writeByteArrayToFile(file, data);
				addUploads(name, file);
			} catch (IOException e) {
				throw unchecked(e);
			}
			return this;
		}

		/**
		 * 添加上传文件
		 * 
		 * @param name
		 * @param files
		 * @return
		 * @throws IOException
		 */
		public HttpRequest addUploads(String name, File file)
				throws IOException {
			HttpRequestUpload upload = new HttpRequestUpload();
			upload.name = name;
			upload.file = file;
			uploads.add(upload);
			return this;
		}

		/**
		 * 添加cookie
		 * 
		 * @param cookies
		 * @return
		 */
		public HttpRequest addCookies(HttpCookie... cookies) {
			if (cookies != null) {
				for (HttpCookie cookie : cookies) {
					if (cookie != null) {
						this.cookies.add(cookie);
					}
				}
			}
			return this;
		}

		/**
		 * 发起请求
		 * 
		 * @param method
		 *            请求类型
		 * @return
		 */
		public HttpResponse request(HttpMethod method) {
			try {
				method = method == null ? HttpMethod.GET : method;
				if (!uploads.isEmpty()) {
					method = HttpMethod.POST;
				}
				StringBuilder address = new StringBuilder(url.trim());
				if (!HttpMethod.POST.equals(method) || content != null) {
					handleGet(address);
				}
				if (log.isDebugEnabled())
					log.debug(String.format("The address is:%s", address));
				URL url = new URL(address.toString());

				HttpURLConnection hc;
				if (proxyPort != 0) {
					hc = handleProxy(url);
				} else {
					hc = (HttpURLConnection) url.openConnection();
				}
				handleConfig(method, hc);
				// 添加Headers
				handleHeaders(method, hc);

				// 添加cookies，将覆盖headerCookie的添加
				if (cookies != null) {
					handleCookies(url);
				}

				// 添加Basic认证
				if (username != null && username.length() != 0) {
					handleBasic(hc);
				}
				// 添加SSL证书
				if (certStream != null) {
					handleSSL(url, hc);
				}

				try {
					// 追加POST参数
					if (HttpMethod.POST.equals(method)) {
						handlePost(hc);
					}
					hc.connect();
					return handleResponse(method, hc);
				} finally {
					hc.disconnect();
				}
			} catch (Exception e) {
				throw unchecked(e);
			}

		}

		private HttpResponse handleResponse(HttpMethod method,
				HttpURLConnection hc) {
			HttpResponse response = new HttpResponse(hc);
			response.url = this.url;
			response.method = method;
			return response;
		}

		private void handlePost(HttpURLConnection hc) throws IOException,
				UnsupportedEncodingException {
			OutputStream os = hc.getOutputStream();
			try {
				if (!uploads.isEmpty()) {
					// 需要上传文件
					handleUploads(os);
				} else if (content != null) {
					os.write(content);
				} else {
					int parameterCount = 0;
					for (String name : parameterMap.keySet()) {
						List<String> values = parameterMap.get(name);
						for (String value : values) {
							if (parameterCount > 0) {
								IOUtils.write("&", os);
							}
							if (value == null) {
								continue;
							}
							parameterCount++;
							IOUtils.write(URLEncoder.encode(name, encoding), os);
							IOUtils.write("=", os);
							IOUtils.write(URLEncoder.encode(value, encoding),
									os);
						}
					}
				}
			} finally {
				os.close();
			}
		}

		private void handleConfig(HttpMethod method, HttpURLConnection hc)
				throws ProtocolException {
			hc.setConnectTimeout(connectTimeout);
			hc.setReadTimeout(readTimeout);
			hc.setRequestMethod(method.name());
			hc.setDoOutput(true);
			hc.setDoInput(true);
			hc.setUseCaches(false);
		}

		private void handleGet(StringBuilder address)
				throws UnsupportedEncodingException {
			int parameterCount = 0;
			int questionMarkIndex = address.lastIndexOf("?");
			for (String name : parameterMap.keySet()) {
				List<String> values = parameterMap.get(name);
				for (String value : values) {
					if (parameterCount > 0) {
						address.append('&');
					} else {
						if (questionMarkIndex != -1) {
							if (address.lastIndexOf("&") != address.length() - 1) {
								address.append('&');
							}
						} else if (questionMarkIndex != address.length() - 1) {
							address.append('?');
						}
					}
					parameterCount++;
					address.append(URLEncoder.encode(name, encoding));
					address.append('=');
					address.append(URLEncoder.encode(value, encoding));
				}
			}
		}

		private HttpURLConnection handleProxy(URL url) throws IOException,
				UnsupportedEncodingException {
			HttpURLConnection hc;
			Proxy proxy;
			Proxy.Type proxyType = this.proxyType == null ? Proxy.Type.HTTP
					: this.proxyType;
			if (proxyHostname != null) {
				proxy = new Proxy(proxyType, new InetSocketAddress(
						proxyHostname, proxyPort));
			} else {
				proxy = new Proxy(proxyType, new InetSocketAddress(proxyPort));
			}
			if (log.isDebugEnabled())
				log.debug(String.format("The connection by proxy::%s", proxy));
			hc = (HttpURLConnection) url.openConnection(proxy);
			// 添加Basic认证
			if (proxyUsername != null && proxyUsername.length() != 0) {
				handleProxyBasic(hc);
			}
			return hc;
		}

		private void handleProxyBasic(HttpURLConnection hc)
				throws UnsupportedEncodingException {
			// 添加Proxy Basic认证
			proxyPassword = proxyPassword == null ? "" : proxyPassword;
			byte[] token = new StringBuilder().append(proxyUsername)
					.append(':').append(proxyPassword).toString()
					.getBytes(proxyBasicAuthorizationEncoding);
			String authorization = "Basic ".concat(new String(Base64
					.encodeBase64(token), proxyBasicAuthorizationEncoding));
			hc.setRequestProperty(HEADER_PROXY_AUTHORIZATION, authorization);
		}

		private void handleHeaders(HttpMethod method, HttpURLConnection hc) {
			if (method.equals(HttpMethod.POST)) {
				if (uploads.isEmpty()) {
					if (!headerPropertyMap.containsKey(HEADER_CONTENT_TYPE)) {
						if (encoding != null) {
							headerPropertyMap.put(HEADER_CONTENT_TYPE,
									"application/x-www-form-urlencoded;charset="
											.concat(encoding));
						} else {
							headerPropertyMap.put(HEADER_CONTENT_TYPE,
									"application/x-www-form-urlencoded");
						}
					}
				} else {
					headerPropertyMap.put(HEADER_CONTENT_TYPE,
							"multipart/form-data; boundary=".concat(BOUNDARY));
				}
			}
			for (String name : headerPropertyMap.keySet()) {
				hc.setRequestProperty(name, headerPropertyMap.get(name));
			}
		}

		private void handleCookies(URL url) throws UnsupportedEncodingException {
			StringBuilder cookieBuilder = new StringBuilder();
			for (HttpCookie cookie : cookies) {
				if (cookie.getName() == null || cookie.getValue() == null) {
					continue;
				}
				if (cookie.getSecure()) {
					if (!url.getProtocol().equals(PROTOCOL_HTTPS)) {
						continue;
					}
				}
				if (cookie.hasExpired()) {
					continue;
				}
				if (cookie.getPath() != null
						&& !cookie.getPath().equals("/")
						&& !url.getPath().regionMatches(0, cookie.getPath(), 0,
								cookie.getPath().length())) {
					continue;
				}

				if (cookieBuilder.length() != 0) {
					cookieBuilder.append(";");
				}
				cookieBuilder.append(URLEncoder.encode(cookie.getName(),
						encoding));
				cookieBuilder.append("=");
				cookieBuilder.append(URLEncoder.encode(cookie.getValue(),
						encoding));
			}
		}

		private void handleBasic(HttpURLConnection hc)
				throws UnsupportedEncodingException {
			// 添加Basic认证
			password = password == null ? "" : password;
			byte[] token = new StringBuilder().append(username).append(':')
					.append(password).toString()
					.getBytes(basicAuthorizationEncoding);
			String authorization = "Basic ".concat(new String(Base64
					.encodeBase64(token), basicAuthorizationEncoding));
			hc.setRequestProperty(HEADER_AUTHORIZATION, authorization);
		}

		private void handleSSL(URL url, HttpURLConnection hc)
				throws KeyStoreException, IOException,
				NoSuchAlgorithmException, CertificateException,
				UnrecoverableKeyException, KeyManagementException {
			if (!url.getProtocol().equals(PROTOCOL_HTTPS)) {
				throw new IllegalStateException(
						String.format(
								"You can not add a certificate of non-SSL connection:%s",
								url));
			}
			certPassword = certPassword == null ? "" : certPassword;

			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(certStream, certPassword.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, certPassword.toCharArray());

			// 忽略证书链验证
			TrustManager[] tm = { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}

				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
				}
			} };

			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(kmf.getKeyManagers(), tm, null);

			SSLSocketFactory factory = sslContext.getSocketFactory();
			((HttpsURLConnection) hc).setSSLSocketFactory(factory);
		}

		private void handleUploads(OutputStream os) throws IOException {
			final Charset charset = encoding == null ? Charset.forName("UTF-8")
					: Charset.forName(encoding);
			// 写入普通参数
			StringBuilder writer = new StringBuilder();
			for (String name : parameterMap.keySet()) {
				List<String> values = parameterMap.get(name);
				for (String value : values) {
					writer.append("--").append(BOUNDARY).append(CRLF)
							.append("Content-Disposition: form-data; name=\"")
							.append(name.replace("\"", "%22")).append("\"")
							.append(CRLF).append(CRLF).append(value)
							.append(CRLF);
				}
			}
			os.write(writer.toString().getBytes(charset));
			// 写入文件
			for (HttpRequestUpload upload : uploads) {
				String contentType;
				InputStream is = new FileInputStream(upload.file);
				try {
					contentType = URLConnection.guessContentTypeFromStream(is);
					if (contentType == null) {
						contentType = URLConnection
								.guessContentTypeFromName(upload.file.getName());
					}
					contentType = contentType != null ? contentType.concat(
							";charset=").concat(charset.name())
							: "application/octet-stream;charset="
									.concat(charset.name());
				} finally {
					is.close();
				}
				is = new FileInputStream(upload.file);
				try {
					String header = new StringBuilder().append("--")
							.append(BOUNDARY).append(CRLF)
							.append("Content-Disposition: form-data; name=\"")
							.append(upload.name.replace("\"", "%22"))
							.append("\"; filename=\"")
							.append(upload.file.getName().replace("\"", "%22"))
							.append("\"").append(CRLF).append("Content-Type: ")
							.append(contentType).append(CRLF)
							.append("Content-Transfer-Encoding: binary")
							.append(CRLF).append(CRLF).toString();
					os.write(header.getBytes(charset));
					IOUtils.copyLarge(is, os);
					os.write(CRLF.getBytes(charset));
				} finally {
					is.close();
				}
			}
			os.write(new StringBuilder().append("--").append(BOUNDARY)
					.append("--").append(CRLF).toString().getBytes(charset));
		}

		/**
		 * 发起GET请求
		 * 
		 * @return
		 */
		public HttpResponse get() {
			return request(HttpMethod.GET);
		}

		/**
		 * 发起POST请求
		 * 
		 * @return
		 */
		public HttpResponse post() {
			return request(HttpMethod.POST);
		}

		/**
		 * 发起PUT请求
		 * 
		 * @return
		 */
		public HttpResponse put() {
			return request(HttpMethod.PUT);
		}

		/**
		 * 发起DELETE请求
		 * 
		 * @return
		 */
		public HttpResponse delete() {
			return request(HttpMethod.DELETE);
		}
	}

	/**
	 * HTTP应答信息对象
	 * 
	 * @author lujijiang
	 * 
	 */
	public static class HttpResponse {

		public static final String HEADER_SET_COOKIE = "Set-Cookie";
		/**
		 * URL地址
		 */
		private String url;
		/**
		 * 请求类型
		 */
		private HttpMethod method;

		/**
		 * 响应代码
		 */
		private int responseCode;
		/**
		 * 响应消息
		 */
		private String responseMessage;
		/**
		 * header属性
		 */
		private Map<String, List<String>> headerMap;
		/**
		 * 返回的cookie
		 */
		private List<HttpCookie> cookies;
		/**
		 * 内容字节流
		 */
		private byte[] content;

		public HttpResponse(HttpURLConnection hc) {
			try {
				headerMap = Collections.unmodifiableMap(hc.getHeaderFields());
				String cookieHeader = hc.getHeaderField(HEADER_SET_COOKIE);
				if (cookieHeader != null) {
					cookies = Collections.unmodifiableList(HttpCookie
							.parse(cookieHeader));
				} else {
					cookies = newList();
				}
				responseCode = hc.getResponseCode();
				responseMessage = hc.getResponseMessage();
				InputStream is;
				if (responseCode >= 400) {
					is = hc.getErrorStream();
				} else {
					is = hc.getInputStream();
				}
				try {
					if (is != null) {
						content = IOUtils.toByteArray(is);
					}
				} finally {
					IOUtils.closeQuietly(is);
				}
			} catch (Exception e) {
				throw unchecked(e);
			}
		}

		/**
		 * 获取响应地址
		 * 
		 * @return
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * 获取请求类型
		 * 
		 * @return
		 */
		public HttpMethod getMethod() {
			return method;
		}

		/**
		 * 获取响应代码
		 * 
		 * @return
		 */
		public int getResponseCode() {
			return responseCode;
		}

		/**
		 * 获取响应消息
		 * 
		 * @return
		 */
		public String getResponseMessage() {
			return responseMessage;
		}

		/**
		 * 获取响应头部信息
		 * 
		 * @return
		 */
		public Map<String, List<String>> getHeaderMap() {
			return headerMap;
		}

		public List<HttpCookie> getCookies() {
			return cookies;
		}

		public Map<String, HttpCookie> getCookieMap() {
			Map<String, HttpCookie> cookieMap = newMap();
			for (HttpCookie cookie : getCookies()) {
				cookieMap.put(cookie.getName(), cookie);
			}
			return cookieMap;
		}

		/**
		 * 获取内容字节数组
		 * 
		 * @return
		 */
		public byte[] getContent() {
			if (content == null) {
				return new byte[0];
			}
			return content;
		}

		public InputStream getContentAsInputStream() {
			return new UnsafeByteArrayInputStream(getContent());
		}

		public String getContentAsString() {
			return new String(getContent());
		}

		public String getContentAsString(String encoding)
				throws UnsupportedEncodingException {
			return new String(getContent(), encoding);
		}

		@Override
		public String toString() {
			return "HttpResponse [url=" + url + ", method=" + method
					+ ", responseCode=" + responseCode + ", responseMessage="
					+ responseMessage + ", headerMap=" + headerMap
					+ ", cookies=" + cookies + ", content=bytes[length="
					+ content.length + "]]";
		}

	}

	public static HttpRequest create(String url) {
		return new HttpRequest(url);
	}

	/**
	 * 将CheckedException转换为RuntimeException.
	 */
	static RuntimeException unchecked(Exception e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}

	/**
	 * 新建一个Map，必须是偶数个参数
	 * 
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static <K, V> Map<K, V> newMap(Object... args) {
		Map<K, V> map = new ConcurrentHashMap<K, V>();
		if (args != null) {
			if (args.length % 2 != 0) {
				throw new IllegalArgumentException(
						"The number of arguments must be an even number");
			}
			for (int i = 0; i < args.length; i += 2) {
				map.put((K) args[i], (V) args[i + 1]);
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	static <T> List<T> newList(T... args) {
		int length = args == null ? 1 : args.length;
		List<T> list = new ArrayList<T>(length);
		if (args == null) {
			list.add(null);
		} else {
			for (int i = 0; i < args.length; i++) {
				list.add(args[i]);
			}
		}
		return list;
	}
}
