package com.saasxx.framework.web.springmvc.processor;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import com.saasxx.framework.Lang;
import com.saasxx.framework.web.springmvc.annotation.RequestForm;

/**
 * 用来处理带前缀的Form表单请求参数的情况，基于注解@RequestForm
 * 
 * @author lujijiang
 *
 */
public class PrefixFormServletModelAttributeMethodProcessor implements
		HandlerMethodArgumentResolver {

	class ServletModelAttributeMethodProcessor_ extends
			ServletModelAttributeMethodProcessor {

		public ServletModelAttributeMethodProcessor_(
				boolean annotationNotRequired) {
			super(annotationNotRequired);
		}

		public Object createAttribute_(String name, MethodParameter parameter,
				WebDataBinderFactory binderFactory, NativeWebRequest webRequest)
				throws Exception {
			return super.createAttribute(name, parameter, binderFactory,
					webRequest);
		}

		public void bindRequestParameters_(WebDataBinder binder,
				NativeWebRequest webRequest) {
			super.bindRequestParameters(binder, webRequest);
		}

		public void validateIfApplicable_(WebDataBinder binder,
				MethodParameter parameter) {
			super.validateIfApplicable(binder, parameter);
		}

		public boolean isBindExceptionRequired_(WebDataBinder binder,
				MethodParameter parameter) {
			return super.isBindExceptionRequired(binder, parameter);
		}

	}

	ServletModelAttributeMethodProcessor_ servletModelAttributeMethodProcessor = new ServletModelAttributeMethodProcessor_(
			true) {

	};

	public final Object resolveArgument(MethodParameter parameter,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) throws Exception {

		String name = ModelFactory.getNameForParameter(parameter);
		Object attribute = (mavContainer.containsAttribute(name) ? mavContainer
				.getModel().get(name) : servletModelAttributeMethodProcessor
				.createAttribute_(name, parameter, binderFactory, webRequest));

		WebDataBinder binder = binderFactory.createBinder(webRequest,
				attribute, name);
		String prefix = getPrefix(parameter);
		if (!Lang.isEmpty(prefix)) {
			prefix += ".";
			binder.setFieldDefaultPrefix(prefix);
		}
		if (binder.getTarget() != null) {
			servletModelAttributeMethodProcessor.bindRequestParameters_(binder,
					webRequest);
			servletModelAttributeMethodProcessor.validateIfApplicable_(binder,
					parameter);
			if (binder.getBindingResult().hasErrors()
					&& servletModelAttributeMethodProcessor
							.isBindExceptionRequired_(binder, parameter)) {
				throw new BindException(binder.getBindingResult());
			}
		}

		// Add resolved attribute and BindingResult at the end of the model
		Map<String, Object> bindingResultModel = binder.getBindingResult()
				.getModel();
		mavContainer.removeAttributes(bindingResultModel);
		mavContainer.addAllAttributes(bindingResultModel);

		return binder.convertIfNecessary(binder.getTarget(),
				parameter.getParameterType(), parameter);
	}

	/**
	 * 用来获取表单参数前缀
	 * 
	 * @param parameter
	 * @return
	 */
	private String getPrefix(MethodParameter parameter) {
		// 首先通过注解RequestForm获取前缀
		RequestForm requestForm = parameter
				.getParameterAnnotation(RequestForm.class);
		if (requestForm == null) {
			// 不可能的情况
			return null;
		}
		String prefix = requestForm.value();
		// 其次通过类型名缩写
		if (prefix.trim().equals("")) {
			prefix = StringUtils.uncapitalize(parameter.getParameterType()
					.getSimpleName());
		}
		return prefix;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestForm.class);
	}

}
