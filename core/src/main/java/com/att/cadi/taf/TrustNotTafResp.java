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

public class TrustNotTafResp implements TafResp {
	private final TafResp delegate;
	private final String desc;
	
	public TrustNotTafResp(final TafResp delegate, final String desc) {
		this.delegate = delegate;
		this.desc = desc;
	}
	
	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public String desc() {
		return desc;
	}

	@Override
	public RESP isAuthenticated() {
		return RESP.NO_FURTHER_PROCESSING;
	}

	@Override
	public RESP authenticate() throws IOException {
		return RESP.NO_FURTHER_PROCESSING;
	}

	@Override
	public Principal getPrincipal() {
		return delegate.getPrincipal();
	}

	@Override
	public Access getAccess() {
		return delegate.getAccess();
	}

	@Override
	public boolean isFailedAttempt() {
		return true;
	}
	
	public String toString() {
		return desc();
	}
}
