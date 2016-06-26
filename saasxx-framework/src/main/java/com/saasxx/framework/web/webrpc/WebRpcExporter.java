package com.saasxx.framework.web.webrpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestHandler;

import com.saasxx.framework.Lang;
import com.saasxx.framework.data.Jsons;
import com.saasxx.framework.web.webrpc.annotation.WebRpc;
import com.saasxx.framework.web.webrpc.exception.WebRpcException;
import com.saasxx.framework.web.webrpc.vo.WebRpcRequest;
import com.saasxx.framework.web.webrpc.vo.WebRpcResponse;

/**
 * WebRpc暴露器，要实用WebRpc特性，需要初始化这个工具
 * 
 * @author lujijiang
 *
 */
public class WebRpcExporter implements HttpRequestHandler,
		ApplicationListener<ContextRefreshedEvent> {

	private static Logger logger = LoggerFactory
			.getLogger(WebRpcExporter.class);

	class WebRpcMeta {
		String name;
		Object bean;
		Method method;

		@Override
		public String toString() {
			return "WebRpcMeta{" + "name='" + name + '\'' + ", bean=" + bean
					+ ", method=" + method + '}';
		}
	}

	private Map<String, WebRpcMeta> webRpcMetaMap = new ConcurrentHashMap<>();

	private boolean initialized;

	private String encoding = "UTF-8";

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/javascript;charset=utf-8");
		try (InputStream is = request.getInputStream();
				PrintWriter out = response.getWriter()) {
			handle(IOUtils.toString(is, encoding), out);
		}
	}

	private void handle(String json, PrintWriter out) throws IOException {
		WebRpcRequest[] list = Jsons.fromJson(json, WebRpcRequest[].class);
		List<WebRpcResponse> webRpcResponses = new ArrayList<>();
		for (WebRpcRequest webRpcRequest : list) {
			WebRpcResponse webRpcResponse = new WebRpcResponse();
			webRpcResponse.setId(webRpcRequest.getId());
			try {
				String name = webRpcRequest.getName();
				WebRpcMeta webRpcMeta = webRpcMetaMap.get(name);
				if (webRpcMeta == null) {
					throw new WebRpcException("Can not find a WebRpc named %s",
							name);
				}
				Assert.notNull(webRpcMeta,
						String.format("Can not find a WebRpc named %s", name));
				Class<?>[] parameterTypes = webRpcMeta.method
						.getParameterTypes();
				if (parameterTypes.length != webRpcRequest.getArgs().length) {
					throw new WebRpcException("Need %d parameters,but got %d",
							parameterTypes.length,
							webRpcRequest.getArgs().length);
				}
				for (int i = 0; i < webRpcRequest.getArgs().length; i++) {
					webRpcRequest.getArgs()[i] = Jsons.fromJson(
							String.valueOf(webRpcRequest.getArgs()[i]),
							parameterTypes[i]);
				}
				Object result = webRpcMeta.method.invoke(webRpcMeta.bean,
						webRpcRequest.getArgs());
				webRpcResponse.setResult(result);
			} catch (Exception e) {
				Throwable cause = Lang.getMessageCause(e);
				webRpcResponse
						.setErrorType(cause.getClass().getCanonicalName());
				String error = cause.getMessage() == null ? cause.getClass()
						.getCanonicalName() : cause.getMessage();
				webRpcResponse.setError(error);
				logger.error("WebRpc execute error:", cause);
			}
			webRpcResponses.add(webRpcResponse);
		}
		String responseJSON = Jsons.toJson(webRpcResponses);
		out.print(responseJSON);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (!initialized) {
			initialized = true;
			initializeWebRpc(event);
		}
	}

	/**
	 * 初始化动作，找到所有的WebRpc并进行注册
	 * 
	 * @param event
	 *            Spring启动时的监听事件
	 */
	private void initializeWebRpc(ContextRefreshedEvent event) {
		for (String beanName : event.getApplicationContext()
				.getBeanDefinitionNames()) {
			Object bean = event.getApplicationContext().getBean(beanName);
			Class<?> beanClass = event.getApplicationContext()
					.getType(beanName);
			while (beanClass.getName().contains("$$")) {
				beanClass = beanClass.getSuperclass();
			}
			Method[] methods = beanClass.getMethods();
			for (Method method : methods) {
				WebRpc webRpc = method.getAnnotation(WebRpc.class);
				if (webRpc != null) {
					String name = webRpc.value().equals("") ? beanName.concat(
							".").concat(method.getName()) : webRpc.value();
					WebRpcMeta webRpcMeta = new WebRpcMeta();
					webRpcMeta.method = method;
					webRpcMeta.bean = bean;
					webRpcMeta.name = name;
					WebRpcMeta existWebRpcMeta = webRpcMetaMap.get(name);
					if (existWebRpcMeta != null) {
						throw new IllegalStateException(String.format(
								"Duplicate WebRpc name %s with %s and %s",
								name, existWebRpcMeta, webRpcMeta));
					}
					webRpcMetaMap.put(name, webRpcMeta);
					logger.info("WebRpc service({}) has been registered.",
							webRpcMeta);
				}
			}
		}
	}
}
