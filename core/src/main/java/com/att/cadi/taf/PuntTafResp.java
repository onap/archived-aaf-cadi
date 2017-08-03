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
package com.att.cadi.taf;

import java.io.IOException;
import java.security.Principal;

import com.att.cadi.Access;

/**
 * A Punt Resp to make it fast and easy for a Taf to respond that it cannot handle a particular kind of
 * request.  It is always the same object, so there is no cost for memory, etc.
 *
 */
public class PuntTafResp implements TafResp {
	private PuntTafResp(){}
	
	private static TafResp singleton = new PuntTafResp();
	
	public static TafResp singleton() {
		return singleton;
	}
	
	public boolean isValid() {
		return false;
	}
	
	public RESP isAuthenticated() {
		return RESP.TRY_ANOTHER_TAF;
	}
	
	public String desc() {
		return "This Taf can or will not handle this authentication";
	}
	
	public RESP authenticate() throws IOException {
		return RESP.TRY_ANOTHER_TAF;
	}

	public Principal getPrincipal() {
		return null;
	}

	public Access getAccess() {
		return NullTafResp.singleton().getAccess();
	}

	public boolean isFailedAttempt() {
		return false;
	}
}
