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
package org.onap.aaf.cadi.aaf.v2_0;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Properties;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.dme2.DME2BasicAuth;
import org.onap.aaf.cadi.dme2.DME2TransferSS;
import org.onap.aaf.cadi.dme2.DME2x509SS;
import org.onap.aaf.cadi.dme2.DRcli;
import org.onap.aaf.cadi.principal.BasicPrincipal;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.api.DME2Manager;
import org.onap.aaf.inno.env.APIException;

public class AAFConDME2 extends AAFCon<DME2Client>{
	private DME2Manager manager;
	private boolean isProxy;
	private URI initURI;

	public AAFConDME2(PropAccess access) throws CadiException, GeneralSecurityException, IOException{
		super(access,Config.AAF_URL,new SecurityInfoC<DME2Client> (access));
		manager = newManager(access);
		setIsProxy();
	}
	
	public AAFConDME2(PropAccess access, String url) throws CadiException, GeneralSecurityException, IOException{
		super(access,url,new SecurityInfoC<DME2Client> (access));
		manager = newManager(access);
		setIsProxy();
	}

	public AAFConDME2(PropAccess access, SecurityInfoC<DME2Client> si) throws CadiException {
		super(access,Config.AAF_URL,si);
		manager = newManager(access);
		setIsProxy();
	}

	public AAFConDME2(PropAccess access, String url, SecurityInfoC<DME2Client> si) throws CadiException {
		super(access,url,si);
		manager = newManager(access);
		setIsProxy();
	}

	/**
	*  Construct a Connector based on the AAF one.  This is for remote access to OTHER than AAF,
	*  but using Credentials, etc
	*/ 
	private AAFConDME2(AAFCon<DME2Client> aafcon, String url) throws CadiException {
		super(aafcon);
		try {
			initURI = new URI(url);
		} catch (URISyntaxException e) {
			throw new CadiException(e);
		}
		manager = newManager(access);
	}
	
	/**
	*  Create a Connector based on the AAF one.  This is for remote access to OTHER than AAF,
	*  but using Credentials, etc
	*/ 
	public AAFCon<DME2Client> clone(String url) throws CadiException {
		return new AAFConDME2(this,url);
	}
	
	private void setIsProxy() {
		String str;
		if((str=access.getProperty(Config.AAF_URL, null))!=null) {
			isProxy = str.contains("service=com.att.authz.authz-gw/version=");
		}
	}

	private DME2Manager newManager(PropAccess access) throws CadiException {
		Properties props = access.getDME2Properties();
		// Critical that TLS Settings not ignored
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
		dc.setProxy(isProxy);
		dc.setManager(manager);
		return dc;
	}

	@Override
	public SecuritySetter<DME2Client> transferSS(Principal principal) throws CadiException {
		try {
			return principal==null?ss:new DME2TransferSS(principal, app, si);
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
			presetProps(access, alias);
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
	
	public static void presetProps(PropAccess access, String alias) throws IOException {
		System.setProperty(Config.AFT_DME2_CLIENT_SSL_CERT_ALIAS, alias);
		if(System.getProperty(Config.AFT_DME2_CLIENT_IGNORE_SSL_CONFIG)==null) {
			access.getDME2Properties();
		}

	}

	/* (non-Javadoc)
	 * @see com.att.cadi.aaf.v2_0.AAFCon#initURI()
	 */
	@Override
	protected URI initURI() {
		return initURI;
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.aaf.v2_0.AAFCon#setInitURI(java.lang.String)
	 */
	@Override
	protected void setInitURI(String uriString) throws CadiException {
		try {
			initURI = new URI(uriString);
		} catch (URISyntaxException e) {
			throw new CadiException(e);
		}
	}
}
