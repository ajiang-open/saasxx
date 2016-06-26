package com.saasxx.framework.web.http.converter;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.saasxx.framework.Lang;
import com.saasxx.framework.web.Webs;

public class JsonHttpMessageConverter extends FastJsonHttpMessageConverter {

	/**
	 * 用于获取JSONP调用时的回调函数名的请求参数名
	 */
	private String jsonpCallbackParameterName = Webs.JSONP_CALLBACK_NAME;

	public String getJsonpCallbackParameterName() {
		return jsonpCallbackParameterName;
	}

	public void setJsonpCallbackParameterName(String jsonpCallbackParameterName) {
		this.jsonpCallbackParameterName = jsonpCallbackParameterName;
	}

	@Override
	protected void writeInternal(Object obj, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
		OutputStream os = outputMessage.getBody();
		StringBuilder out = new StringBuilder();
		String json = JSON.toJSONString(
				Lang.newMap("status", true, "data", obj), getFeatures());
		String callbackName = request.getParameter(jsonpCallbackParameterName);
		if (callbackName != null) {
			out.append(callbackName);
			out.append("(");
			out.append(json);
			out.append(")");
		} else {
			out.append(json);
		}
		String text = out.toString();
		byte[] bytes = text.getBytes(getCharset());
		os.write(bytes);
	}

}
