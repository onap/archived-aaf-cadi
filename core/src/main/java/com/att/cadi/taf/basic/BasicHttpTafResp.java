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
package com.att.cadi.taf.basic;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.taf.AbsTafResp;
import com.att.cadi.taf.TafResp;

public class BasicHttpTafResp extends AbsTafResp implements TafResp {
	private HttpServletResponse httpResp;
	private String realm;
	private RESP status;
	private final boolean wasFailed;
	
	public BasicHttpTafResp(Access access, Principal principal, String description, RESP status, HttpServletResponse resp, String realm, boolean wasFailed) {
		super(access,principal, description);
		httpResp = resp;
		this.realm = realm;
		this.status = status;
		this.wasFailed = wasFailed;
	}

	public RESP authenticate() throws IOException {
		httpResp.setStatus(401); // Unauthorized	
		httpResp.setHeader("WWW-Authenticate", "Basic realm=\""+realm+'"');
		return RESP.HTTP_REDIRECT_INVOKED;
	}

	public RESP isAuthenticated() {
		return status;
	}

	public boolean isFailedAttempt() {
		return wasFailed;
	}


}
