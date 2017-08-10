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
package com.att.cadi.taf;

import java.io.IOException;
import java.security.Principal;

import com.att.cadi.Access;

/**
 * A Null Pattern for setting responses to "Deny" before configuration is setup.
 *
 */
class NullTafResp implements TafResp {
	private NullTafResp(){}
	
	private static TafResp singleton = new NullTafResp();
	
	public static TafResp singleton() {
		return singleton;
	}
	
	public boolean isValid() {
		return false;
	}
	
	public RESP isAuthenticated() {
		return RESP.NO_FURTHER_PROCESSING;
	}
	
	public String desc() {
		return "All Authentication denied";
	}
	
	public RESP authenticate() throws IOException {
		return RESP.NO_FURTHER_PROCESSING;
	}

	public Principal getPrincipal() {
		return null;
	}

	public Access getAccess() {
		return Access.NULL;
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.taf.TafResp#isFailedAttempt()
	 */
	public boolean isFailedAttempt() {
		return true;
	}
}
