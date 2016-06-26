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

/**
 * 允许请求跨域访问
 * 
 * @author lujijiang
 *
 */
public class CrossDomainFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

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
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
		response.addHeader("Access-Control-Allow-Headers", "Content-Type,Authorization");
		response.addHeader("Access-Control-Expose-Headers", "WWW-Authenticate");
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {

	}

}
