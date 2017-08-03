/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
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
package com.att.cadi;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4JAccess extends PropAccess {
	private static final Logger slf4j = LoggerFactory.getLogger("AAF");
	
	public SLF4JAccess(final Properties initial) throws CadiException {
		super(initial);
	}

	public void log(Level level, Object... elements) {
		switch(level) {
			case AUDIT:
				slf4j.info(msg(elements).toString());
				break;
			case DEBUG:
				slf4j.debug(msg(elements).toString());
				break;
			case ERROR:
				slf4j.error(msg(elements).toString());
				break;
			case INFO:
				slf4j.info(msg(elements).toString());
				break;
			case INIT:
				slf4j.info(msg(elements).toString());
				break;
			case WARN:
				slf4j.warn(msg(elements).toString());
				break;
			default:
				slf4j.info(msg(elements).toString());
				break;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.att.cadi.Access#willLog(com.att.cadi.Access.Level)
	 */
	@Override
	public boolean willLog(Level level) {
		switch(level) {
			case DEBUG:
				return slf4j.isDebugEnabled();
			case ERROR:
				return slf4j.isErrorEnabled();
			case WARN:
				return slf4j.isWarnEnabled();
//			case INFO:
//			case INIT:
//			case AUDIT:
			default:
				return slf4j.isInfoEnabled();
		}
	}

	private StringBuilder msg(Object ... elements) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Object o : elements) {
			if(first) first = false;
			else {
				sb.append(' ');
			}
			sb.append(o.toString());
		}
		return sb;
	}

	public void log(Exception e, Object... elements) {
		slf4j.error(msg(elements).toString(),e);
	}

}
