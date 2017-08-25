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
package org.onap.aaf.cadi.dme2;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.AbsBasicAuth;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.principal.BasicPrincipal;

import com.att.aft.dme2.api.DME2Client;

public class DME2BasicAuth extends AbsBasicAuth<DME2Client> {
	public DME2BasicAuth(String user, String pass, SecurityInfoC<DME2Client> si) throws IOException {
		super(user,pass,si);
	}

	public DME2BasicAuth(Access access, SecurityInfoC<DME2Client> si) throws IOException {
		super(access.getProperty(Config.AAF_MECHID, null),
				access.decrypt(access.getProperty(Config.AAF_MECHPASS, null), false),
				si);
	}

	public DME2BasicAuth(BasicPrincipal bp,SecurityInfoC<DME2Client> si) throws IOException {
		super(bp.getName(),new String(bp.getCred()),si);
	}

	public DME2BasicAuth(Access access) throws IOException, GeneralSecurityException {
		super(access.getProperty(Config.AAF_MECHID, null),
				access.decrypt(access.getProperty(Config.AAF_MECHPASS, null), false),
				new SecurityInfoC<DME2Client>(access));
	}

	public void setSecurity(DME2Client client) throws CadiException {
		if(isDenied()) {
			throw new CadiException(REPEAT_OFFENDER);
		}
		client.addHeader("Authorization", headValue);
	}
}
