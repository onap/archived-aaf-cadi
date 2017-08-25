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
import java.security.cert.CertificateEncodingException;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;

import com.att.aft.dme2.api.DME2Client;
import org.onap.aaf.inno.env.APIException;


public class DME2x509SS implements SecuritySetter<DME2Client> {
	private String alias;

	public DME2x509SS(final String sendAlias, SecurityInfoC<DME2Client> si) throws APIException, IOException, CertificateEncodingException {
		if((alias=sendAlias) == null) {
			if(si.default_alias == null) {
				throw new APIException("JKS Alias is required to use X509SS Security.  Use " + Config.CADI_ALIAS +" to set default alias");
			} else {
				alias = si.default_alias;
			}
		}
	}

	@Override
	public void setSecurity(DME2Client dme2) throws CadiException {
		// DME2Client has to have properties set before creation to work.
	}
	
	/* (non-Javadoc)
	 * @see com.att.cadi.SecuritySetter#getID()
	 */
	@Override
	public String getID() {
		return alias;
	}

	@Override
	public int setLastResponse(int respCode) {
		return 0;
	}

}
