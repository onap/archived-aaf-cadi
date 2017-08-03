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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

import com.att.cadi.AbsUserCache;
import com.att.cadi.Access;
import com.att.cadi.CadiException;
import com.att.cadi.CadiWrap;
import com.att.cadi.Connector;
import com.att.cadi.LocatorException;
import com.att.cadi.Lur;
import com.att.cadi.SecuritySetter;
import com.att.cadi.aaf.AAFPermission;
import com.att.cadi.aaf.marshal.CertsMarshal;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.cadi.config.Config;
import com.att.cadi.config.SecurityInfo;
import com.att.cadi.lur.EpiLur;
import com.att.cadi.principal.BasicPrincipal;
import com.att.inno.env.APIException;
import com.att.inno.env.util.Split;
import com.att.rosetta.env.RosettaDF;
import com.att.rosetta.env.RosettaEnv;

import aaf.v2_0.Certs;
import aaf.v2_0.Perms;
import aaf.v2_0.Users;

public abstract class AAFCon<CLIENT> implements Connector {
	public static final String AAF_VERSION = "2.0";

	final public Access access;
	// Package access
	final public int timeout, cleanInterval, connTimeout;
	final public int highCount, userExpires, usageRefreshTriggerCount;
	private Rcli<CLIENT> client = null;
	final public RosettaDF<Perms> permsDF;
	final public RosettaDF<Certs> certsDF;
	final public RosettaDF<Users> usersDF;
	private String realm;
	public final String app;
	protected SecuritySetter<CLIENT> ss;
	protected SecurityInfo<CLIENT> si;
	protected final URI initURI;

	public Rcli<CLIENT> client(String apiVersion) throws CadiException {
		if(client==null) {
			client = rclient(initURI,ss);
			client.apiVersion(apiVersion)
				  .readTimeout(connTimeout);
		}
		return client;
	}
	
	protected AAFCon(Access access, String tag, SecurityInfo<CLIENT> si) throws CadiException{
		try {
			this.access = access;
			this.si = si;
			this.ss = si.defSS;
			if(ss==null) {
				String mechid = access.getProperty(Config.AAF_MECHID, null);
				String encpass = access.getProperty(Config.AAF_MECHPASS, null);
				if(encpass==null) {
					String alias = access.getProperty(Config.CADI_ALIAS, mechid);
					if(alias==null) {
						throw new CadiException(Config.CADI_ALIAS + " or " + Config.AAF_MECHID + " required.");
					}
					si.defSS=ss = x509Alias(alias);
				} else {
					if(mechid!=null && encpass !=null) {
						si.defSS=ss=basicAuth(mechid, encpass);
					} else {
						si.defSS=ss=new SecuritySetter<CLIENT>() {
							
							@Override
							public String getID() {
								return "";
							}
			
							@Override
							public void setSecurity(CLIENT client) throws CadiException {
								throw new CadiException("AAFCon has not been initialized with Credentials (SecuritySetter)");
							}
						};
					}
				}
			}
			
			timeout = Integer.parseInt(access.getProperty(Config.AAF_READ_TIMEOUT, Config.AAF_READ_TIMEOUT_DEF));
			cleanInterval = Integer.parseInt(access.getProperty(Config.AAF_CLEAN_INTERVAL, Config.AAF_CLEAN_INTERVAL_DEF));
			highCount = Integer.parseInt(access.getProperty(Config.AAF_HIGH_COUNT, Config.AAF_HIGH_COUNT_DEF).trim());
			connTimeout = Integer.parseInt(access.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF).trim());
			userExpires = Integer.parseInt(access.getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF).trim());
			usageRefreshTriggerCount = Integer.parseInt(access.getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF).trim())-1; // zero based
	
			
			initURI = new URI(access.getProperty(tag,null));
			if(initURI==null) {
				throw new CadiException(tag + " property is required.");
			}
	
			app=reverseDomain(ss.getID());
			realm="openecomp.org";
	
			RosettaEnv env = new RosettaEnv();
			permsDF = env.newDataFactory(Perms.class);
			usersDF = env.newDataFactory(Users.class);
			certsDF = env.newDataFactory(Certs.class);
			certsDF.rootMarshal(new CertsMarshal()); // Speedier Marshaling
		} catch (APIException|URISyntaxException e) {
			throw new CadiException("AAFCon cannot be configured",e);
		}
	}
	
	/**
	 * Return the backing AAFCon, if there is a Lur Setup that is AAF.
	 * 
	 * If there is no AAFLur setup, it will return "null"
	 * @param servletRequest
	 * @return
	 */
	public static final AAFCon<?> obtain(Object servletRequest) {
		if(servletRequest instanceof CadiWrap) {
			Lur lur = ((CadiWrap)servletRequest).getLur();
			if(lur != null) {
				if(lur instanceof EpiLur) {
					AbsAAFLur<?> aal = (AbsAAFLur<?>) ((EpiLur)lur).subLur(AbsAAFLur.class);
					if(aal!=null) {
						return aal.aaf;
					}
				} else {
					if(lur instanceof AbsAAFLur) {
						return ((AbsAAFLur<?>)lur).aaf;
					}
				}
			}
		}
		return null;
	}
	
	public AAFAuthn<CLIENT> newAuthn() throws APIException {
		try {
			return new AAFAuthn<CLIENT>(this);
		} catch (APIException e) {
			throw e;
		} catch (Exception e) {
			throw new APIException(e);
		}
	}

	public AAFAuthn<CLIENT> newAuthn(AbsUserCache<AAFPermission> c) throws APIException {
		try {
			return new AAFAuthn<CLIENT>(this,c);
		} catch (APIException e) {
			throw e;
		} catch (Exception e) {
			throw new APIException(e);
		}
	}

	public AAFLurPerm newLur() throws CadiException {
		try {
			return new AAFLurPerm(this);
		} catch (CadiException e) {
			throw e;
		} catch (Exception e) {
			throw new CadiException(e);
		}
	}
	
	public AAFLurPerm newLur(AbsUserCache<AAFPermission> c) throws APIException {
		try {
			return new AAFLurPerm(this,c);
		} catch (APIException e) {
			throw e;
		} catch (Exception e) {
			throw new APIException(e);
		}
	}

	/**
	 * Take a Fully Qualified User, and get a Namespace from it.
	 * @param user
	 * @return
	 */
	public static String reverseDomain(String user) {
		StringBuilder sb = null;
		String[] split = Split.split('.',user);
		int at;
		for(int i=split.length-1;i>=0;--i) {
			if(sb == null) {
				sb = new StringBuilder();
			} else {
				sb.append('.');
			}

			if((at = split[i].indexOf('@'))>0) {
				sb.append(split[i].subSequence(at+1, split[i].length()));
			} else {
				sb.append(split[i]);
			}
		}
		
		return sb==null?"":sb.toString();
	}

	protected abstract Rcli<CLIENT> rclient(URI uri, SecuritySetter<CLIENT> ss) throws CadiException;
	
	public abstract<RET> RET best(Retryable<RET> retryable) throws LocatorException, CadiException, APIException;


	public abstract SecuritySetter<CLIENT> basicAuth(String user, String password) throws CadiException;
	
	public abstract SecuritySetter<CLIENT> transferSS(Principal principal) throws CadiException;
	
	public abstract SecuritySetter<CLIENT> basicAuthSS(BasicPrincipal principal) throws CadiException;
	
	public abstract SecuritySetter<CLIENT> x509Alias(String alias) throws APIException, CadiException;


	public String getRealm() {
		return realm;

	}

	public SecuritySetter<CLIENT> set(SecuritySetter<CLIENT> ss) {
		this.ss = ss;
		if(client!=null) {
			client.setSecuritySetter(ss);
		}
		return ss;
	}
	
	public SecurityInfo<CLIENT> securityInfo() {
		return si;
	}

	public String defID() {
		if(ss!=null) {
			return ss.getID();
		}
		return "unknown";
	}
	
	public void invalidate() throws CadiException {
		if(client!=null) {
			client.invalidate();
		}
		client = null;
	}


}
