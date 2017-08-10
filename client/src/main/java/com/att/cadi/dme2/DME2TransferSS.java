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
package com.att.cadi.dme2;

import java.io.IOException;
import java.security.Principal;

import com.att.aft.dme2.api.DME2Client;
import com.att.cadi.CadiException;
import com.att.cadi.client.AbsTransferSS;
import com.att.cadi.config.Config;
import com.att.cadi.config.SecurityInfoC;

public class DME2TransferSS extends AbsTransferSS<DME2Client> {

	public DME2TransferSS(Principal principal, String app, SecurityInfoC<DME2Client> si) throws IOException {
		super(principal, app, si);
	}

	@Override
	public void setSecurity(DME2Client client) throws CadiException {
		if(value!=null) {
			if(defSS==null) {
				throw new CadiException("Need App Credentials to send message");
			}
			defSS.setSecurity(client);
			client.addHeader(Config.CADI_USER_CHAIN, value);
		}
	}

	@Override
	public int setLastResponse(int respCode) {
		return 0;
	}
}
