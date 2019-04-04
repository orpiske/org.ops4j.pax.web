/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.web.jsp.filter.test;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestFilter implements Filter {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(TestFilter.class);

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
						 FilterChain chain) throws IOException, ServletException {
		LOG.debug("TestFilter - doFilter");
		chain.doFilter(request, response);
		LOG.debug("TestFilter - return from doFilter");
		response.getWriter().write("Filtered");
		LOG.debug("response altered!");
	}

	@Override
	public void init(FilterConfig arg0) {
		LOG.debug("TestFilter - init: {}", arg0);
	}

}
