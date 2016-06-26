package com.saasxx.framework.web.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.saasxx.framework.Lang;

/**
 * 过滤器代理器，一次性代理多个过滤器
 * 
 * @author lujijiang
 *
 */
public class MultiFilter implements Filter {

	class FilterMeta {
		Filter filter;
		FilterConfig filterConfig;
	}

	ServletContext servletContext;

	private List<FilterMeta> filterMetas = Lang.newList();

	private MultiFilter() {
	}

	public static MultiFilter create() {
		return new MultiFilter();
	}

	/**
	 * 添加过滤器
	 * 
	 * @param filter
	 * @param filterConfig
	 * @return
	 */
	public MultiFilter addFilter(Filter filter, FilterConfig filterConfig) {
		FilterMeta filterMeta = new FilterMeta();
		filterMeta.filter = filter;
		filterMeta.filterConfig = filterConfig;
		filterMetas.add(filterMeta);
		return this;
	}

	/**
	 * 添加过滤器
	 * 
	 * @param filter
	 * @param name
	 * @param initParameters
	 * @return
	 */
	public MultiFilter addFilter(Filter filter, final String name,
			final Map<String, String> initParameters) {
		return addFilter(filter, new FilterConfig() {
			public ServletContext getServletContext() {
				return servletContext;
			}

			public Enumeration<String> getInitParameterNames() {
				return new Enumeration<String>() {

					Iterator<String> iterator = initParameters.keySet()
							.iterator();

					public boolean hasMoreElements() {
						return iterator.hasNext();
					}

					public String nextElement() {
						return iterator.next();
					}
				};
			}

			public String getInitParameter(String name) {
				return initParameters.get(name);
			}

			public String getFilterName() {
				return name;
			}
		});
	}

	/**
	 * 添加过滤器
	 * 
	 * @param filter
	 * @param name
	 * @return
	 */
	public MultiFilter addFilter(Filter filter, final String name) {
		return addFilter(filter, name, Lang.newMap());
	}

	/**
	 * 添加过滤器
	 * 
	 * @param filter
	 * @return
	 */
	public MultiFilter addFilter(Filter filter) {
		return addFilter(filter,
				StringUtils.uncapitalize(filter.getClass().getSimpleName()));
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		servletContext = filterConfig.getServletContext();
		for (FilterMeta filterMeta : filterMetas) {
			filterMeta.filter.init(filterMeta.filterConfig);
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			final FilterChain chain) throws IOException, ServletException {
		FilterChain proxyChain = new FilterChain() {

			Iterator<FilterMeta> filterIterator = filterMetas.iterator();

			public void doFilter(ServletRequest request,
					ServletResponse response) throws IOException,
					ServletException {
				if (filterIterator.hasNext()) {
					FilterMeta filterMeta = filterIterator.next();
					filterMeta.filter.doFilter(request, response, this);
				} else {
					chain.doFilter(request, response);
				}
			}
		};

		proxyChain.doFilter(request, response);
	}

	public void destroy() {
		for (FilterMeta filterMeta : filterMetas) {
			filterMeta.filter.destroy();
		}
	}

}
