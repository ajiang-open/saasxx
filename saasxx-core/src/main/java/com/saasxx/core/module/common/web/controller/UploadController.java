package com.saasxx.core.module.common.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Jsons;
import com.saasxx.framework.io.FilePool;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;

@Controller
@RequestMapping("/common")
public class UploadController {

	/**
	 * 文件上传的存储目录
	 */
	private static final String UPLOADED_PATH = "files/";

	private static Log log = Logs.getLog();

	@Autowired
	private FilePool filePool;

	@RequestMapping("/upload")
	@ResponseBody
	public Object upload(@RequestParam("file") MultipartFile[] files,
			@RequestParam(name = "data", required = false) String jsonData, @RequestParam String path,
			HttpServletRequest request) throws IllegalStateException, IOException {
		// 处理上传的文件
		List<String> filePaths = Lang.newList();
		if (files != null) {
			for (MultipartFile file : files) {
				log.info("已上传文件：{}", file.getOriginalFilename());
				String filePath = generateFile(path, file);
				File realFile = filePool.file(filePath);
				realFile.getParentFile().mkdirs();
				file.transferTo(realFile);
				log.info("文件：{}的大小是：{}", realFile.getAbsolutePath(), realFile.length());
				filePaths.add(filePath);
			}
		}
		// 处理额外的参数
		if (jsonData != null) {
			Map<String, Object> data = Jsons.fromJson(jsonData, HashMap.class);
			log.info("额外参数是：{}", data);
		}
		return Lang.newMap("paths", filePaths);
	}

	/**
	 * 生成文件
	 * 
	 * @param path
	 * @param file
	 * @return
	 */
	private String generateFile(String path, MultipartFile file) {
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(UPLOADED_PATH);
		pathBuilder.append("/");
		pathBuilder.append(DateTime.now().toString("yyyy-MM-dd"));
		pathBuilder.append("/");
		pathBuilder.append(path);
		pathBuilder.append("/");

		String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		String finalPath = baseName.concat("-").concat(DateTime.now().toString("yyyyMMddHHmmssSSS"))
				.concat(extension.length() == 0 ? "" : ".".concat(extension));

		pathBuilder.append(finalPath);
		return pathBuilder.toString().replaceAll("/+", "/");
	}
}