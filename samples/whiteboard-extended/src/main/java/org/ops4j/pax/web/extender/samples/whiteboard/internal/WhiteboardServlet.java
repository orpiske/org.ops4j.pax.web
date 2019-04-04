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
package org.ops4j.pax.web.extender.samples.whiteboard.internal;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhiteboardServlet extends HttpServlet {
	private static final Logger LOG = LoggerFactory
			.getLogger(WhiteboardServlet.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 2468029128065282904L;
	private String servletAlias;

	public WhiteboardServlet(final String alias) {
		servletAlias = alias;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext context = config.getServletContext();
		LOG.debug(
				"Servlet Context info - ContextName = [{}], ContextPath = [{}]",
				context.getServletContextName(), context.getContextPath());
	}

	protected void doGet(HttpServletRequest request,
						 HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("<h1>Hello Whiteboard Extender</h1>");
		response.getWriter().println("request alias: " + servletAlias);
	}

}
