package com.saasxx.framework.web.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Mimes;
import com.saasxx.framework.io.FilePool;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

/**
 * 静态资源过滤器，支持文件池
 * 
 * @author lujijiang
 *
 */
public class StaticFilter implements Filter {

	private static final String BOOT_TIMESTAMP = Long.toString(System.currentTimeMillis(), 36);

	private static final String CONFIG_CHARSET = "charset";

	private static final String CONFIG_URL_CHARSET = "url-charset";

	private static final String CONFIG_MAX_AGE = "maxAge";

	private static final String FILE_POOL_PATH = "filePoolPath";

	private static final Pattern CSS_URL_PATTERN = Pattern.compile("url\\(.+?\\)", Pattern.CASE_INSENSITIVE);

	private static final Log log = Logs.getLog();

	private ServletContext servletContext;

	private FilePool filePool;

	/**
	 * 设置资源编码，默认编码是UTF-8
	 */
	private Charset charset = Charset.forName("UTF-8");
	/**
	 * URL编码规则
	 */
	private String urlCharset = "UTF-8";
	/**
	 * 缓存时间，单位为秒，默认不设置缓存，由浏览器控制
	 */
	private int maxAge = -1;

	public FilePool getFilePool() {
		return filePool;
	}

	public void setFilePool(FilePool filePool) {
		this.filePool = filePool;
	}

	public void setFilePool(String filePoolPath) {
		this.filePool = new FilePool(filePoolPath);
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public String getUrlCharset() {
		return urlCharset;
	}

	public void setUrlCharset(String urlCharset) {
		this.urlCharset = urlCharset;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		servletContext = filterConfig.getServletContext();
		String filePoolPath = filterConfig.getInitParameter(FILE_POOL_PATH);
		if (filePoolPath != null) {
			setFilePool(filePoolPath);
		}
		if (filePool == null) {
			throw Lang.newException("文件池必须设置");
		}

		String charset = filterConfig.getInitParameter(CONFIG_CHARSET);
		if (charset != null) {
			this.charset = Charset.forName(charset);
		}

		String urlCharset = filterConfig.getInitParameter(CONFIG_URL_CHARSET);
		if (urlCharset != null) {
			this.urlCharset = urlCharset;
		}

		String maxAge = filterConfig.getInitParameter(CONFIG_MAX_AGE);
		if (maxAge != null) {
			this.maxAge = Integer.valueOf(maxAge);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			handle((HttpServletRequest) request, (HttpServletResponse) response, chain);
			return;
		}
		chain.doFilter(request, response);
	}

	private void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		if ((path.equalsIgnoreCase("/-.js") || path.equalsIgnoreCase("/-.css")) && request.getQueryString() != null) {

			String suffix = path.substring(path.lastIndexOf("."));

			String queryString = request.getQueryString();
			int end = queryString.indexOf("=");
			if (end == -1) {
				end = queryString.length();
			}
			queryString = queryString.substring(0, end);
			String[] paths = queryString.split(";");

			mergeResource(request, response, chain, paths, suffix);
			return;
		}
		uniqueResource(request, response, chain, path);
	}

	private void uniqueResource(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			String path) throws UnsupportedEncodingException, IOException, ServletException, FileNotFoundException {
		path = URLDecoder.decode(path, urlCharset);
		File file = getFile(path);
		if (!file.isFile()) {
			chain.doFilter(request, response);
			return;
		}
		long lastModified = file.lastModified();
		long length = file.length();

		setContentType(response, path);
		// response.setContentLengthLong(file.length());
		String etag = new StringBuffer().append(BOOT_TIMESTAMP).append("-").append(Long.toString(lastModified, 36))
				.append("-").append(Long.toString(length, 36)).toString();
		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		String ifNoneMatch = request.getHeader("If-None-Match");
		if (ifModifiedSince == lastModified && etag.equals(ifNoneMatch)) {
			response.setStatus(304);
			return;
		}
		response.addDateHeader("Last-Modified", lastModified);
		response.setHeader("Etag", etag);
		response.setContentLength((int) length);
		if (maxAge > -1) {
			response.addHeader("Cache-Control", "max-age=" + maxAge);
		}
		try (OutputStream os = response.getOutputStream(); InputStream is = new FileInputStream(file)) {
			IOUtils.copy(is, os);
		}
	}

	private void setContentType(HttpServletResponse response, String path) {
		String contentType = Mimes.guessContentType(FilenameUtils.getExtension(path));
		contentType = contentType == null ? "application/octet-stream" : contentType;
		if (charset != null) {
			contentType = contentType.concat(";charset=").concat(charset.name());
		}
		response.setContentType(contentType);
	}

	/**
	 * 处理资源合并请求
	 * 
	 * @param request
	 * @param response
	 * @param chain
	 * @param paths
	 * @throws IOException
	 */
	private void mergeResource(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			String[] paths, String suffix) throws IOException {
		long lastModified = 0;
		long length = 0;
		Map<String, File> fileMap = new HashMap<>();
		Map<String, String> commentMap = new HashMap<>();
		for (int i = 0; i < paths.length; i++) {
			paths[i] = URLDecoder.decode(paths[i], urlCharset).trim();
			File file = getFile(paths[i]);
			if (!file.isFile()) {
				throw new FileNotFoundException(String.format("%s is not exists.", file.getAbsolutePath()));
			}
			lastModified += file.lastModified();
			length += file.length();
			fileMap.put(paths[i], file);
			String comment = "\r\n\r\n/**************************************".concat(paths[i])
					.concat("**************************************/\r\n\r\n");
			length += comment.getBytes(charset).length;
			commentMap.put(paths[i], comment);
		}
		lastModified = lastModified / fileMap.size();
		setContentType(response, suffix);
		response.setContentLengthLong(length);
		String etag = new StringBuffer().append(BOOT_TIMESTAMP).append("-").append(Long.toString(lastModified, 36))
				.append("-").append(Long.toString(length, 36)).toString();
		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		String ifNoneMatch = request.getHeader("If-None-Match");
		if (ifModifiedSince == lastModified && etag.equals(ifNoneMatch)) {
			response.setStatus(304);
			return;
		}
		response.addDateHeader("Last-Modified", lastModified);
		response.setHeader("Etag", etag);
		response.setContentLength((int) length);
		if (maxAge > -1) {
			response.addHeader("Cache-Control", "max-age=" + maxAge);
		}
		PrintWriter writer = response.getWriter();
		for (String path : paths) {
			File file = fileMap.get(path);
			String text = FileUtils.readFileToString(file, charset);
			if (path.endsWith(".css")) {
				text = fixCssPath(text, path);
			}
			writer.append(commentMap.get(path));
			writer.append(text);
		}
		writer.close();
	}

	private String fixCssPath(String text, final String path) {
		StringBuffer sb = new StringBuffer();
		Matcher matcher = CSS_URL_PATTERN.matcher(text);
		while (matcher.find()) {
			String _path = path;
			String url = matcher.group();
			url = url.replaceAll("[\"']", "");
			if (!url.matches("url\\(((data:)|(#)).+")) {
				int p;
				url = url.replaceAll("(url\\()|(\\))", "");
				while ((p = url.indexOf("../")) != -1) {
					url = url.replaceFirst("\\.\\./", "");
					_path = _path.substring(0, _path.lastIndexOf("/"));
				}
				int i = _path.lastIndexOf("/");
				url = _path.substring(0, i) + "/" + url;
				url = "url('" + url + "')";
			}
			matcher.appendReplacement(sb, url);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 获取文件
	 * 
	 * @param path
	 * @return
	 */
	private File getFile(String path) {
		File file = new File(servletContext.getRealPath(path));
		if (!file.isFile()) {
			file = filePool.file(path);
		}
		return file;
	}

	@Override
	public void destroy() {
	}

}
