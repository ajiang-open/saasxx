package com.saasxx.framework.web.filter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;
import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Mimes;
import com.saasxx.framework.io.FilePool;
import com.saasxx.framework.io.Images;
import com.saasxx.framework.web.Webs;

/**
 * 用于对图片进行裁剪
 * 
 * @author lujijiang
 *
 */
public class ImageScaleFilter implements Filter {

	private static final String URL_CHARSET = "urlCharset";

	private static final String FILE_POOL_PATH = "filePoolPath";

	private static final String TEMP_ROOT_PATH = "/temp";

	public static class ImageScaleMeta {
		int width;
		int height;
		float quality;

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public float getQuality() {
			return quality;
		}

		public void setQuality(float quality) {
			this.quality = quality;
		}

	}

	private FilePool filePool;

	private String urlCharset;

	public FilePool getFilePool() {
		return filePool;
	}

	public void setFilePool(FilePool filePool) {
		this.filePool = filePool;
	}

	public void setFilePool(String filePoolPath) {
		this.filePool = new FilePool(filePoolPath);
	}

	public String getUrlCharset() {
		return urlCharset;
	}

	public void setUrlCharset(String urlCharset) {
		this.urlCharset = urlCharset;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String filePoolPath = filterConfig.getInitParameter(FILE_POOL_PATH);
		if (filePoolPath != null) {
			setFilePool(filePoolPath);
		}
		if (filePool == null) {
			throw Lang.newException("文件池必须设置");
		}
		if (urlCharset == null) {
			urlCharset = filterConfig.getInitParameter(URL_CHARSET);
		}
		urlCharset = urlCharset == null ? "UTF-8" : urlCharset;
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
		String queryString = request.getQueryString();
		if (queryString == null) {
			chain.doFilter(request, response);
			return;
		}
		Map<String, Object> queryParameterMap = Webs.getParameterMap(queryString, "UTF-8");
		String imgConfig = null;
		for (String name : queryParameterMap.keySet()) {
			if (name.startsWith("img:{")) {
				imgConfig = name;
				break;
			}
		}
		if (imgConfig == null) {
			chain.doFilter(request, response);
			return;
		}
		int index = imgConfig.indexOf(":");
		if (imgConfig.length() <= index + 2) {
			chain.doFilter(request, response);
			return;
		}
		imgConfig = imgConfig.substring(index + 1);
		final ImageScaleMeta imageScaleMeta = JSON.parseObject(imgConfig, ImageScaleMeta.class);
		String path = request.getRequestURI().substring(request.getContextPath().length());
		path = URLDecoder.decode(path, urlCharset);
		if (path.startsWith(TEMP_ROOT_PATH.concat("/"))) {
			chain.doFilter(request, response);
			return;
		}
		File file = filePool.file(path);
		if (!file.isFile()) {
			chain.doFilter(request, response);
			return;
		}
		File source = file;
		file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())
				.concat("." + Lang.hashCode(file.getName()) + ".").concat(FilenameUtils.getExtension(file.getName())));
		if (!file.isFile()) {
			try {
				// 尝试自动正位图片方向
				Images.autoRotate(source, file);
				file.setLastModified(source.lastModified());
			} catch (Throwable e) {
				file.deleteOnExit();
				file = source;
			}
		}
		String matchFilePath = TEMP_ROOT_PATH.concat(path).concat(".").concat(String.valueOf(Lang.hashCode(imgConfig)))
				.concat(".").concat(FilenameUtils.getExtension(file.getName()));
		File matchFile = filePool.file(matchFilePath);
		if (!matchFile.isFile()) {
			matchFile.getParentFile().mkdirs();
			try (FileOutputStream fileOutputStream = new FileOutputStream(matchFile)) {
				BufferedImage image = ImageIO.read(file);
				ImageIO.write(
						Images.smartCut(image, imageScaleMeta.width, imageScaleMeta.height, imageScaleMeta.quality),
						FilenameUtils.getExtension(file.getAbsolutePath()), fileOutputStream);
				matchFile.setLastModified(file.lastModified());
			} catch (Exception e) {
				matchFile.deleteOnExit();
				throw Lang.unchecked(e);
			}
		}
		response.setContentType(Mimes.guessContentType(FilenameUtils.getExtension(matchFilePath)));
		response.setContentLengthLong(matchFile.length());
		try (InputStream inputStream = new FileInputStream(matchFile);
				OutputStream outputStream = response.getOutputStream()) {
			IOUtils.copy(inputStream, outputStream);
		}
		return;
	}

	@Override
	public void destroy() {

	}

}
