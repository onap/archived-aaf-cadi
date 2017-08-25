/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package org.onap.aaf.cadi;
import java.util.Enumeration;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.onap.aaf.cadi.config.Config;

public class ServletContextAccess extends PropAccess {

	private ServletContext context;

	public ServletContextAccess(FilterConfig filterConfig) {
		super(filterConfig); // protected contstructor... does not have "init" called.
		context = filterConfig.getServletContext();

		for(Enumeration<?> en = filterConfig.getInitParameterNames();en.hasMoreElements();) {
			String name = (String)en.nextElement();
			setProperty(name, filterConfig.getInitParameter(name));
		}
		init(getProperties());
		Config.getDME2Props(this);
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.PropAccess#log(com.att.cadi.Access.Level, java.lang.Object[])
	 */
	@Override
	public void log(Level level, Object... elements) {
		if(willLog(level)) {
			StringBuilder sb = buildMsg(level, elements);
			context.log(sb.toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.PropAccess#log(java.lang.Exception, java.lang.Object[])
	 */
	@Override
	public void log(Exception e, Object... elements) {
		StringBuilder sb = buildMsg(Level.ERROR, elements);
		context.log(sb.toString(),e);
	}

	public ServletContext context() {
		return context;
	}
}
