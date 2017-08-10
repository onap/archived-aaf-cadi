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
package com.att.cadi.client;

import java.security.Principal;

import com.att.cadi.SecuritySetter;
import com.att.cadi.config.SecurityInfoC;
import com.att.cadi.principal.BasicPrincipal;
import com.att.cadi.principal.TGuardPrincipal;
import com.att.cadi.principal.TrustPrincipal;

public abstract class AbsTransferSS<CLIENT> implements SecuritySetter<CLIENT> {
	protected String value;
	protected SecurityInfoC<CLIENT> securityInfo;
	protected SecuritySetter<CLIENT> defSS;
	private Principal principal;

	//Format:<ID>:<APP>:<protocol>[:AS][,<ID>:<APP>:<protocol>]*
	public AbsTransferSS(Principal principal, String app) {
		init(principal, app);
	}

	public AbsTransferSS(Principal principal, String app, SecurityInfoC<CLIENT> si) {
		init(principal,app);
		securityInfo = si;
		this.defSS = si.defSS;
	}

	private void init(Principal principal, String app)  {
		this.principal=principal;
		if(principal==null) {
			return;
		} else if(principal instanceof BasicPrincipal) {
			value = principal.getName() + ':' + app + ":BasicAuth:AS";
		} else if(principal instanceof TrustPrincipal) {
			TrustPrincipal tp = (TrustPrincipal)principal;
			// recursive
			init(tp.original(),app);
			value += principal.getName() + ':' + app + ":Trust:AS" + ',' + tp.userChain();
		} else if(principal instanceof TGuardPrincipal) {
			value = principal.getName() + ':' + app + ":TGUARD:AS";
		}
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.SecuritySetter#getID()
	 */
	@Override
	public String getID() {
		return principal==null?"":principal.getName();
	}
}
