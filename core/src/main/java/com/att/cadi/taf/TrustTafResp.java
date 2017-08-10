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

public class TrustTafResp implements TafResp {
	private final TafResp delegate;
	private final Principal principal;
	private final String desc;
	
	public TrustTafResp(final TafResp delegate, final Principal principal, final String desc) {
		this.delegate = delegate;
		this.principal = principal;
		this.desc = desc + ' ' + delegate.desc();
	}
	
	@Override
	public boolean isValid() {
		return delegate.isValid();
	}

	@Override
	public String desc() {
		return desc;
	}

	@Override
	public RESP isAuthenticated() {
		return delegate.isAuthenticated();
	}

	@Override
	public RESP authenticate() throws IOException {
		return delegate.authenticate();
	}

	@Override
	public Principal getPrincipal() {
		return principal;
	}

	@Override
	public Access getAccess() {
		return delegate.getAccess();
	}

	@Override
	public boolean isFailedAttempt() {
		return delegate.isFailedAttempt();
	}
	
	public String toString() {
		return principal.getName() + " by trust of " + desc();
	}
}
