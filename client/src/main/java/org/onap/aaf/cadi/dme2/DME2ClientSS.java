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

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.Access.Level;

import com.att.aft.dme2.api.DME2Client;

public class DME2ClientSS implements SecuritySetter<DME2Client> {
	private Access access;
	private String user,crd;
	
	public DME2ClientSS(Access access, String user, String pass) throws IOException {
		this.access = access;
		this.user = user;
		this.crd = pass;
	}
	
	@Override
	public void setSecurity(DME2Client client) {
		try {
			client.setCredentials(user, access.decrypt(crd, false));
		} catch (IOException e) {
			access.log(Level.ERROR,e,"Error decrypting DME2 Password");
		}
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.SecuritySetter#getID()
	 */
	@Override
	public String getID() {
		return user;
	}

	@Override
	public int setLastResponse(int respCode) {
		// TODO Auto-generated method stub
		return 0;
	}
}
