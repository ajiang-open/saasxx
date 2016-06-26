package com.saasxx.framework.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebsFilter implements Filter {

	private final static ThreadLocal<WebInfo> WEB_INFO_THREAD_LOCAL = new ThreadLocal<WebInfo>();

	public static class WebInfo {
		HttpServletRequest httpServletRequest;
		HttpServletResponse httpServletResponse;

		public HttpServletRequest getRequest() {
			return httpServletRequest;
		}

		public HttpServletResponse getResponse() {
			return httpServletResponse;
		}

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(final ServletRequest request,
			final ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try {
			if (request instanceof HttpServletRequest) {
				WebInfo webInfo = new WebInfo() {
					{
						this.httpServletRequest = (HttpServletRequest) request;
						this.httpServletResponse = (HttpServletResponse) response;
					}
				};
				WEB_INFO_THREAD_LOCAL.set(webInfo);
			}
			chain.doFilter(request, response);
		} finally {
			WEB_INFO_THREAD_LOCAL.remove();
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
