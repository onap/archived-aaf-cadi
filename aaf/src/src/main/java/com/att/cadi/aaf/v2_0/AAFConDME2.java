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
package com.att.cadi.aaf.v2_0;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Properties;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.api.DME2Manager;
import com.att.cadi.Access;
import com.att.cadi.CadiException;
import com.att.cadi.LocatorException;
import com.att.cadi.SecuritySetter;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.cadi.config.Config;
import com.att.cadi.config.SecurityInfo;
import com.att.cadi.dme2.DME2BasicAuth;
import com.att.cadi.dme2.DME2TransferSS;
import com.att.cadi.dme2.DME2x509SS;
import com.att.cadi.dme2.DRcli;
import com.att.cadi.principal.BasicPrincipal;
import com.att.inno.env.APIException;

public class AAFConDME2 extends AAFCon<DME2Client>{
	private DME2Manager manager;

	public AAFConDME2(Access access) throws CadiException, GeneralSecurityException, IOException{
		super(access,Config.AAF_URL,new SecurityInfo<DME2Client> (access));
		manager = newManager(access);
	}
	
	public AAFConDME2(Access access, String url) throws CadiException, GeneralSecurityException, IOException{
		super(access,url,new SecurityInfo<DME2Client> (access));
		manager = newManager(access);
	}

	public AAFConDME2(Access access, SecurityInfo<DME2Client> si) throws CadiException {
		super(access,Config.AAF_URL,si);
		manager = newManager(access);
	}

	public AAFConDME2(Access access, String url, SecurityInfo<DME2Client> si) throws CadiException {
		super(access,url,si);
		manager = newManager(access);
	}

	private DME2Manager newManager(Access access) throws CadiException {
		Properties props = new Properties();
		Config.cadiToDME2(access, props);
		try {
			return new DME2Manager("AAFCon",props);
		} catch (DME2Exception e) {
			throw new CadiException(e);
		}
	}


	/* (non-Javadoc)
	 * @see com.att.cadi.aaf.v2_0.AAFCon#basicAuth(java.lang.String, java.lang.String)
	 */
	@Override
	public SecuritySetter<DME2Client> basicAuth(String user, String password) throws CadiException {
		if(password.startsWith("enc:???")) {
			try {
				password = access.decrypt(password, true);
			} catch (IOException e) {
				throw new CadiException("Error Decrypting Password",e);
			}
		}

		try {
			return set(new DME2BasicAuth(user,password,si));
		} catch (IOException e) {
			throw new CadiException("Error setting up DME2BasicAuth",e);
		}
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.aaf.v2_0.AAFCon#rclient(java.net.URI, com.att.cadi.SecuritySetter)
	 */
	@Override
	protected Rcli<DME2Client> rclient(URI uri, SecuritySetter<DME2Client> ss) {
		DRcli dc = new DRcli(uri, ss);
		dc.setManager(manager);
		return dc;
	}

	@Override
	public SecuritySetter<DME2Client> transferSS(Principal principal) throws CadiException {
		try {
			return principal==null?ss:new DME2TransferSS(principal, app);
		} catch (IOException e) {
			throw new CadiException("Error creating DME2TransferSS",e);
		}
	}

	@Override
	public SecuritySetter<DME2Client> basicAuthSS(BasicPrincipal principal) throws CadiException {
		try {
			return new DME2BasicAuth(principal,si);
		} catch (IOException e) {
			throw new CadiException("Error creating DME2BasicAuth",e);
		}

	}

	@Override
	public SecuritySetter<DME2Client> x509Alias(String alias) throws CadiException {
		try {
			return new DME2x509SS(alias,si);
		} catch (Exception e) {
			throw new CadiException("Error creating DME2x509SS",e);
		}
	}

	@Override
	public <RET> RET best(Retryable<RET> retryable) throws LocatorException, CadiException, APIException {
		// NOTE: DME2 had Retry Logic embedded lower.  
		try {
			return (retryable.code(rclient(initURI,ss)));
		} catch (ConnectException e) {
			// DME2 should catch
			try {
				manager.refresh();
			} catch (Exception e1) {
				throw new CadiException(e1);
			}
			throw new CadiException(e);
		}
	}
}
