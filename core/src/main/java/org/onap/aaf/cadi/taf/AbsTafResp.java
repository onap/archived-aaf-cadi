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
package org.onap.aaf.cadi.taf;

import java.security.Principal;

import org.onap.aaf.cadi.Access;

/**
 * AbsTafResp
 * 
 * Base class for TafResp (TAF Response Objects)
 *
 */
public abstract class AbsTafResp implements TafResp {

	protected final String desc;
	protected final Principal principal;
	protected final Access access;

	/**
	 * AbsTafResp
	 * 
	 * Set and hold
	 * Description (for logging)
	 * Principal (as created by derived class)
	 * Access (for access to underlying container, i.e. for Logging, auditing, ClassLoaders, etc)
	 *  
	 * @param access
	 * @param principal
	 * @param description
	 */
	public AbsTafResp(Access access, Principal principal, String description) {
		this.access = access;
		this.principal = principal;
		this.desc = description;
	}

	/**
	 * isValid()
	 * 
	 * Respond in the affirmative if the TAF was able to Authenticate
	 */
	public boolean isValid() {
		return principal!=null;
	}

	/**
	 * desc()
	 * 
	 * Respond with description of response as given by the TAF  
	 */
	public String desc() {
		return desc;
	}

	/**
	 * isAuthenticated()
	 * 
	 * Respond with the TAF's code of whether Authenticated, or suggested next steps
	 * default is either IS_AUTHENTICATED, or TRY_ANOTHER_TAF.  The TAF can overload
	 * and suggest others, such as "NO_FURTHER_PROCESSING", if it can detect that this
	 * is some sort of security breach (i.e. Denial of Service)  
	 */
	public RESP isAuthenticated() {
		return principal==null?RESP.TRY_ANOTHER_TAF:RESP.IS_AUTHENTICATED;
	}

	/**
	 * getPrincipal()
	 * 
	 * Return the principal created by the TAF based on Authentication. 
	 * 
	 * Returns "null" if Authentication failed (no principal)
	 */
	public Principal getPrincipal() {
		return principal;
	}

	/**
	 * getAccess()
	 * 
	 * Get the Access object from the TAF, so that appropriate Logging, etc can be coordinated.
	 */
	public Access getAccess() {
		return access;
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.taf.TafResp#isFailedAttempt()
	 */
	public boolean isFailedAttempt() {
		return false;
	}

}
