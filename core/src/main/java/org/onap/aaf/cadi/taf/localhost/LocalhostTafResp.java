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
package org.onap.aaf.cadi.taf.localhost;

import java.security.Principal;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.taf.TafResp;

public class LocalhostTafResp implements TafResp {
	private RESP action;
	private String description;
	private final static Principal principal = new Principal() {
		private String name = System.getProperty("user.name")+"@localhost";
//		@Override
		public String getName() {
			return name;
		}
	};

	private Access access;
	
	public LocalhostTafResp(Access access, RESP state, String desc) {
		action = state;
		description = desc;
		this.access = access;
	}
	
//	@Override
	public boolean isValid() {
		return action == RESP.IS_AUTHENTICATED;
	}

//	@Override
	public String desc() {
		return description;
	}

//	@Override
	public RESP authenticate() {
		return action;
	}
	
	public RESP isAuthenticated() {
		return action;
	}

//	@Override
	public Principal getPrincipal() {
		return principal;
	}

	public Access getAccess() {
		return access;
	}

	public boolean isFailedAttempt() {
		return false;
	}

}
