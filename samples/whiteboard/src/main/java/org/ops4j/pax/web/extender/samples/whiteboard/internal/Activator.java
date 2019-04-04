/*
 * Copyright 2007 Alin Dreghiciu.
 *
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

import java.util.Dictionary;
import java.util.EventListener;
import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.ops4j.pax.web.extender.whiteboard.ExtenderConstants;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultErrorPageMapping;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultJspMapping;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultResourceMapping;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultWelcomeFileMapping;
import org.ops4j.pax.web.service.whiteboard.ErrorPageMapping;
import org.ops4j.pax.web.service.whiteboard.JspMapping;
import org.ops4j.pax.web.service.whiteboard.ResourceMapping;
import org.ops4j.pax.web.service.whiteboard.WelcomeFileMapping;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

	private ServiceRegistration rootServletReg;
	private ServiceRegistration<Servlet> servletReg;
	private ServiceRegistration<Servlet> servletFilteredReg;
	private ServiceRegistration<ResourceMapping> resourcesReg;
	private ServiceRegistration<Filter> filterReg;
	private ServiceRegistration<EventListener> listenerReg;
	private ServiceRegistration<HttpContext> httpContextReg;
	private ServiceRegistration<Servlet> forbiddenServletReg;
	private ServiceRegistration<HttpServlet> exceptionServletRegistration;
	private ServiceRegistration<WelcomeFileMapping> welcomeFileRegistration;
	private ServiceRegistration<ErrorPageMapping> errorpage404Registration;
	private ServiceRegistration<ErrorPageMapping> uncaughtExceptionRegistration;
	private ServiceRegistration<ResourceMapping> rootResourceMappingRegistration;

	private ServiceRegistration<JspMapping> jspMappingRegistration;

	public void start(final BundleContext bundleContext) {
		Dictionary<String, String> props;

		// register a custom http context that forbids access
		props = new Hashtable<>();
		props.put(ExtenderConstants.PROPERTY_HTTP_CONTEXT_ID, "forbidden");
		httpContextReg = bundleContext.registerService(HttpContext.class,
				new WhiteboardContext(), props);
		// and an servlet that cannot be accessed due to the above context
		props = new Hashtable<>();
		props.put(ExtenderConstants.PROPERTY_ALIAS, "/forbidden");
		props.put(ExtenderConstants.PROPERTY_HTTP_CONTEXT_ID, "forbidden");
		forbiddenServletReg = bundleContext.registerService(Servlet.class,
				new WhiteboardServlet("/forbidden"), props);

		//first make sure all mappings are registered, servlets aren't notified of updates since jetty 9.3.5 upgrade

		// register welcome page - interesting how it will work with the root
		// servlet, i.e. will it showdow it
		DefaultWelcomeFileMapping welcomeFileMapping = new DefaultWelcomeFileMapping();
		welcomeFileMapping.setRedirect(true);
		welcomeFileMapping.setWelcomeFiles(new String[]{"index.html",
				"welcome.html"});
		welcomeFileRegistration = bundleContext.registerService(
				WelcomeFileMapping.class, welcomeFileMapping, null);

		// register error pages for 404 and java.lang.Exception
		DefaultErrorPageMapping errorpageMapping = new DefaultErrorPageMapping();
		errorpageMapping.setError("404");
		errorpageMapping.setLocation("/404.html");

		errorpage404Registration = bundleContext.registerService(
				ErrorPageMapping.class, errorpageMapping, null);

		// java.lang.Exception
		DefaultErrorPageMapping exceptionErrorMapping = new DefaultErrorPageMapping();
		exceptionErrorMapping.setError(java.lang.Exception.class.getName());
		exceptionErrorMapping.setLocation("/uncaughtException.html");
		uncaughtExceptionRegistration = bundleContext.registerService(
				ErrorPageMapping.class, exceptionErrorMapping, null);

		// Properties for the service
		props = new Hashtable<>();
		props.put("alias", "/whiteboard");
		// registering the servlet as service
		servletReg = bundleContext.registerService(Servlet.class,
				new WhiteboardServlet("/whiteboard"), props);

		props = new Hashtable<>();
		props.put("alias", "/root");
		rootServletReg = bundleContext.registerService(
				new String[]{HttpServlet.class.getName(), Servlet.class.getName()},
				new WhiteboardServlet("/root"), props);

		// Registering resource mappings as service
		DefaultResourceMapping resourceMapping = new DefaultResourceMapping();
		resourceMapping.setAlias("/whiteboardresources");
		resourceMapping.setPath("/images");
		resourcesReg = bundleContext.registerService(ResourceMapping.class,
				resourceMapping, null);

		try {
			props = new Hashtable<>();
			props.put("alias", "/filtered");
			servletFilteredReg = bundleContext.registerService(Servlet.class,
					new WhiteboardServlet("/filtered"), props);

			// register a filter
			props = new Hashtable<>();
			props.put(ExtenderConstants.PROPERTY_URL_PATTERNS, "/filtered/*");
			filterReg = bundleContext.registerService(Filter.class,
					new WhiteboardFilter(), props);
		} catch (NoClassDefFoundError ignore) {
			// in this case most probably that we do not have a servlet version
			// >= 2.3
			// required by our filter
			LOG.warn("Cannot start filter example (javax.servlet version?): "
					+ ignore.getMessage());
		}

		//registering servlet and two filters on one URL
		try {
			props = new Hashtable<>();
			props.put("alias", "/second");
			servletFilteredReg = bundleContext.registerService(Servlet.class,
					new WhiteboardServlet("/second"), props);

			// register a filter
			props = new Hashtable<>();
			props.put(ExtenderConstants.PROPERTY_URL_PATTERNS, "/second/*");
			filterReg = bundleContext.registerService(Filter.class,
					new WhiteboardFilter(), props);

			// register second filter
			props = new Hashtable<>();
			props.put(ExtenderConstants.PROPERTY_URL_PATTERNS, "/second/*");
			filterReg = bundleContext.registerService(Filter.class,
					new SecondWhiteboardFilter(), props);

		} catch (NoClassDefFoundError ignore) {
			// in this case most probably that we do not have a servlet version
			// >= 2.3
			// required by our filter
			LOG.warn("Cannot start filter example (javax.servlet version?): "
					+ ignore.getMessage());
		}

		try {
			// register a servlet request listener
			listenerReg = bundleContext.registerService(EventListener.class,
					new WhiteboardListener(), null);
		} catch (NoClassDefFoundError ignore) {
			// in this case most probably that we do not have a servlet version
			// >= 2.4
			// required by our request listener
			LOG.warn("Cannot start filter example (javax.servlet version?): "
					+ ignore.getMessage());
		}

		// servlet to test exceptions and error pages
		props = new Hashtable<>();
		props.put("alias", "/exception");
		exceptionServletRegistration = bundleContext.registerService(
				HttpServlet.class, new ExceptionServlet(), props);

		// register resource at root of bundle
		DefaultResourceMapping rootResourceMapping = new DefaultResourceMapping();
		rootResourceMapping.setAlias("/");
		rootResourceMapping.setPath("");
		rootResourceMappingRegistration = bundleContext.registerService(
				ResourceMapping.class, rootResourceMapping, null);

		// register hello jsp
		DefaultJspMapping jspMapping = new DefaultJspMapping();
		jspMapping.setUrlPatterns("/jsp");
		jspMappingRegistration = bundleContext.registerService(
				JspMapping.class, jspMapping, null);
	}

	public void stop(BundleContext bundleContext) {
		if (rootResourceMappingRegistration != null) {
			rootResourceMappingRegistration.unregister();
			rootResourceMappingRegistration = null;
		}
		if (uncaughtExceptionRegistration != null) {
			uncaughtExceptionRegistration.unregister();
			uncaughtExceptionRegistration = null;
		}
		if (errorpage404Registration != null) {
			errorpage404Registration.unregister();
			errorpage404Registration = null;
		}
		if (welcomeFileRegistration != null) {
			welcomeFileRegistration.unregister();
			welcomeFileRegistration = null;
		}
		if (rootServletReg != null) {
			rootServletReg.unregister();
			rootServletReg = null;
		}
		if (servletReg != null) {
			servletReg.unregister();
			servletReg = null;
		}
		if (servletFilteredReg != null) {
			servletFilteredReg.unregister();
			servletFilteredReg = null;
		}
		if (exceptionServletRegistration != null) {
			exceptionServletRegistration.unregister();
			exceptionServletRegistration = null;
		}
		if (resourcesReg != null) {
			resourcesReg.unregister();
			resourcesReg = null;
		}
		if (filterReg != null) {
			filterReg.unregister();
			filterReg = null;
		}
		if (listenerReg != null) {
			listenerReg.unregister();
			listenerReg = null;
		}
		if (httpContextReg != null) {
			httpContextReg.unregister();
			httpContextReg = null;
		}
		if (forbiddenServletReg != null) {
			forbiddenServletReg.unregister();
			forbiddenServletReg = null;
		}
		if (jspMappingRegistration != null) {
			jspMappingRegistration.unregister();
			jspMappingRegistration = null;
		}
	}
}
