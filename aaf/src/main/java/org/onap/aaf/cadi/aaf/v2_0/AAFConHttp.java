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
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.Principal;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.client.AbsTransferSS;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HBasicAuthSS;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.cadi.http.HRcli;
import org.onap.aaf.cadi.http.HTransferSS;
import org.onap.aaf.cadi.http.HX509SS;
import org.onap.aaf.cadi.principal.BasicPrincipal;

import org.onap.aaf.inno.env.APIException;

public class AAFConHttp extends AAFCon<HttpURLConnection> {
	private final HMangr hman;

	public AAFConHttp(PropAccess access) throws CadiException, GeneralSecurityException, IOException {
		super(access,Config.AAF_URL,new SecurityInfoC<HttpURLConnection>(access));
		hman = new HMangr(access,Config.loadLocator(access, access.getProperty(Config.AAF_URL,null)));
	}

	public AAFConHttp(PropAccess access, String tag) throws CadiException, GeneralSecurityException, IOException {
		super(access,tag,new SecurityInfoC<HttpURLConnection>(access));
		hman = new HMangr(access,Config.loadLocator(access, access.getProperty(tag,null)));
	}

	public AAFConHttp(PropAccess access, String urlTag, SecurityInfoC<HttpURLConnection> si) throws CadiException {
		super(access,urlTag,si);
		hman = new HMangr(access,Config.loadLocator(access, access.getProperty(urlTag,null)));
	}

	public AAFConHttp(PropAccess access, Locator<URI> locator) throws CadiException, GeneralSecurityException, IOException {
		super(access,Config.AAF_URL,new SecurityInfoC<HttpURLConnection>(access));
		hman = new HMangr(access,locator);
	}

	public AAFConHttp(PropAccess access, Locator<URI> locator, SecurityInfoC<HttpURLConnection> si) throws CadiException {
		super(access,Config.AAF_URL,si);
		hman = new HMangr(access,locator);
	}

	public AAFConHttp(PropAccess access, Locator<URI> locator, SecurityInfoC<HttpURLConnection> si, String tag) throws CadiException {
		super(access,tag,si);
		hman = new HMangr(access, locator);
	}
	
	private AAFConHttp(AAFCon<HttpURLConnection> aafcon, String url) {
		super(aafcon);
		hman = new HMangr(aafcon.access,Config.loadLocator(access, url));
	}

	@Override
	public AAFCon<HttpURLConnection> clone(String url) {
		return new AAFConHttp(this,url);
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.aaf.v2_0.AAFCon#basicAuth(java.lang.String, java.lang.String)
	 */
	@Override
	public SecuritySetter<HttpURLConnection> basicAuth(String user, String password) throws CadiException {
		if(password.startsWith("enc:???")) {
			try {
				password = access.decrypt(password, true);
			} catch (IOException e) {
				throw new CadiException("Error decrypting password",e);
			}
		}
		try {
			return new HBasicAuthSS(user,password,si);
		} catch (IOException e) {
			throw new CadiException("Error creating HBasicAuthSS",e);
		}
	}

	public SecuritySetter<HttpURLConnection> x509Alias(String alias) throws APIException, CadiException {
		try {
			return set(new HX509SS(alias,si));
		} catch (Exception e) {
			throw new CadiException("Error creating X509SS",e);
		}
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.aaf.v2_0.AAFCon#rclient(java.net.URI, com.att.cadi.SecuritySetter)
	 */
	@Override
	protected Rcli<HttpURLConnection> rclient(URI ignoredURI, SecuritySetter<HttpURLConnection> ss) throws CadiException {
		if(hman.loc==null) {
			throw new CadiException("No Locator set in AAFConHttp"); 
		}
		try {
			return new HRcli(hman, hman.loc.best() ,ss);
		} catch (Exception e) {
			throw new CadiException(e);
		}
	}

	@Override
	public AbsTransferSS<HttpURLConnection> transferSS(Principal principal) throws CadiException {
		return new HTransferSS(principal, app,si);
	}
	
	/* (non-Javadoc)
	 * @see com.att.cadi.aaf.v2_0.AAFCon#basicAuthSS(java.security.Principal)
	 */
	@Override
	public SecuritySetter<HttpURLConnection> basicAuthSS(BasicPrincipal principal) throws CadiException {
		try {
			return new HBasicAuthSS(principal,si);
		} catch (IOException e) {
			throw new CadiException("Error creating HBasicAuthSS",e);
		}
	}

	public HMangr hman() {
		return hman;
	}

	@Override
	public <RET> RET best(Retryable<RET> retryable) throws LocatorException, CadiException, APIException {
		return hman.best(ss, (Retryable<RET>)retryable);
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.aaf.v2_0.AAFCon#initURI()
	 */
	@Override
	protected URI initURI() {
		try {
			Item item = hman.loc.best();
			if(item!=null) {
				return hman.loc.get(item);
			}
		} catch (LocatorException e) {
			access.log(e, "Error in AAFConHttp obtaining initial URI");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.aaf.v2_0.AAFCon#setInitURI(java.lang.String)
	 */
	@Override
	protected void setInitURI(String uriString) throws CadiException {
		// TODO Auto-generated method stub
		
	}
	
}
