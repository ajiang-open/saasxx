package com.saasxx.framework.web.springmvc.handler;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.saasxx.framework.Lang;
import com.saasxx.framework.log.Log;
import com.saasxx.framework.log.Logs;
import com.saasxx.framework.web.Webs;

/**
 * 用于处理JSON请求时的异常
 * 
 * @author lujijiang
 *
 */
public class JsonExceptionResolver extends SimpleMappingExceptionResolver {

	private static Log log = Logs.getLog();
	/**
	 * 用于获取JSONP调用时的回调函数名的请求参数名
	 */
	private String jsonpCallbackParameterName = Webs.JSONP_CALLBACK_NAME;

	private SerializerFeature[] features = new SerializerFeature[0];

	public SerializerFeature[] getFeatures() {
		return features;
	}

	public void setFeatures(SerializerFeature... features) {
		this.features = features;
	}

	public String getJsonpCallbackParameterName() {
		return jsonpCallbackParameterName;
	}

	public void setJsonpCallbackParameterName(String jsonpCallbackParameterName) {
		this.jsonpCallbackParameterName = jsonpCallbackParameterName;
	}

	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		if (!response.isCommitted()) {
			if (handler instanceof HandlerMethod) {
				HandlerMethod handlerMethod = (HandlerMethod) handler;
				if (handlerMethod.getMethodAnnotation(ResponseBody.class) != null
						|| handlerMethod.getBeanType().getAnnotation(ResponseBody.class) != null
						|| handlerMethod.getBeanType().getAnnotation(RestController.class) != null
						|| request.getParameter(jsonpCallbackParameterName) != null) {
					return handleExceptionMessage(request, response, ex);
				}
			} else {
				// 根据请求信息判断是否需要进行JSON序列化
				// if (Webs.isAjaxRequest(request) ||
				// request.getParameter(jsonpCallbackParameterName) != null) {
				// return handleExceptionMessage(request, response, ex);
				// }
			}
		} else {
			log.error("不能将异常信息处理为JSON格式，因为输出流已提交，原始异常信息如下：{}", Lang.getMessageCause(ex));
		}
		return super.resolveException(request, response, handler, ex);
	}

	private ModelAndView handleExceptionMessage(HttpServletRequest request, HttpServletResponse response,
			Exception ex) {
		response.resetBuffer();
		final String callbackName = request.getParameter(jsonpCallbackParameterName);
		return new ModelAndView(new View() {
			@Override
			public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
					throws Exception {
				PrintWriter out = Webs.getWriter(response);
				try {
					Throwable throwable = Lang.getMessageCause(ex);
					Map<String, Object> data = Lang.newMap(
							"status", false, "error", throwable.getMessage() == null
									? throwable.getClass().getCanonicalName() : throwable.getMessage(),
							"type", throwable.getClass().getCanonicalName());
					String json = JSON.toJSONString(data, features);
					if (callbackName != null) {
						out.print(callbackName);
						out.print("(");
						out.print(json);
						out.print(")");
					} else {
						out.print(json);
					}
				} finally {
					out.close();
				}
			}

			@Override
			public String getContentType() {
				return callbackName == null ? "plain/json;charset=utf-8" : "application/javascript;charset=utf-8";
			}
		});
	}

}
