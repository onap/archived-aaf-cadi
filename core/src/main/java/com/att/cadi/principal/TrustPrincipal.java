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
package com.att.cadi.principal;

import java.security.Principal;

import com.att.cadi.UserChain;

public class TrustPrincipal extends BearerPrincipal implements UserChain {
	private final String name;
	private final Principal original;
	private String userChain;
	
	public TrustPrincipal(final Principal actual, final String asName) {
		this.original = actual;
		name = asName.trim();
		if(actual instanceof UserChain) {
			UserChain uc = (UserChain)actual;
			userChain = uc.userChain();
		} else if(actual instanceof X509Principal) {
			userChain="x509";
		} else if(actual instanceof BasicPrincipal) {
			userChain="BAth";
		} else {
			userChain = actual.getClass().getSimpleName();
		}
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public String getOrigName() {
		return original.getName() + '[' + userChain + ']';
	}

	@Override
	public String userChain() {
		return userChain;
	}
	
	public Principal original() {
		return original;
	}
	
}
