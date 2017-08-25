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

import org.onap.aaf.cadi.AbsUserCache;
import org.onap.aaf.cadi.CachedPrincipal;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.GetCred;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.User;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.lur.ConfigPrincipal;

import com.att.aft.dme2.api.DME2Exception;
import org.onap.aaf.inno.env.APIException;

public class AAFAuthn<CLIENT> extends AbsUserCache<AAFPermission> {
	private AAFCon<CLIENT> con;
	private String realm;
	
	/**
	 * Configure with Standard AAF properties, Stand alone
	 * @param con
	 * @throws Exception 
	 */
	// Package on purpose
	AAFAuthn(AAFCon<CLIENT> con) throws Exception {
		super(con.access,con.cleanInterval,con.highCount,con.usageRefreshTriggerCount);
		this.con = con;

		try {
			setRealm();
		} catch (APIException e) {
			if(e.getCause() instanceof DME2Exception) {
				// Can't contact AAF, assume default
				realm=con.access.getProperty(Config.AAF_DEFAULT_REALM, Config.getDefaultRealm());
			}
		}
		}

	/**
	 * Configure with Standard AAF properties, but share the Cache (with AAF Lur)
	 * @param con
	 * @throws Exception 
	 */
	// Package on purpose
	AAFAuthn(AAFCon<CLIENT> con, AbsUserCache<AAFPermission> cache) throws Exception {
		super(cache);
		this.con = con;
		try {
			setRealm();
		} catch (Exception e) {
			if(e.getCause() instanceof DME2Exception) {
				access.log(e);
				// Can't contact AAF, assume default		
				realm=con.access.getProperty(Config.AAF_DEFAULT_REALM, Config.getDefaultRealm());
			}
		}
	}

	private void setRealm() throws Exception {
		// Make a call without security set to get the 401 response, which
		// includes the Realm of the server
		// This also checks on Connectivity early on.
		Future<String> fp = con.client(AAFCon.AAF_LATEST_VERSION).read("/authn/basicAuth", "text/plain");
		if(fp.get(con.timeout)) {
			throw new Exception("Do not preset Basic Auth Information for AAFAuthn");
		} else {
			if(fp.code()==401) {
				realm = fp.header("WWW-Authenticate");
				if(realm!=null && realm.startsWith("Basic realm=\"")) {
					realm = realm.substring(13, realm.length()-1);
				} else {
					realm = "unknown.com";
				}
			}
		}
	}
	
	/**
	 * Return Native Realm of AAF Instance.
	 * 
	 * @return
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * Returns null if ok, or an Error String;
	 * 
	 * @param user
	 * @param password
	 * @return
	 * @throws IOException 
	 * @throws CadiException 
	 * @throws Exception
	 */
	public String validate(String user, String password) throws IOException, CadiException {
		User<AAFPermission> usr = getUser(user);
		if(password.startsWith("enc:???")) {
			password = access.decrypt(password, true);
		}

		byte[] bytes = password.getBytes();
		if(usr != null && usr.principal != null && usr.principal.getName().equals(user) 
				&& usr.principal instanceof GetCred) {
			
			if(Hash.isEqual(((GetCred)usr.principal).getCred(),bytes)) {
				return null;
			} else {
				remove(usr);
				usr = null;
			}
		}
		
		AAFCachedPrincipal cp = new AAFCachedPrincipal(this,con.app, user, bytes, con.cleanInterval);
		// Since I've relocated the Validation piece in the Principal, just revalidate, then do Switch
		// Statement
		switch(cp.revalidate()) {
			case REVALIDATED:
				if(usr!=null) {
					usr.principal = cp;
				} else {
					addUser(new User<AAFPermission>(cp,con.timeout));
				}
				return null;
			case INACCESSIBLE:
				return "AAF Inaccessible";
			case UNVALIDATED:
				return "User/Pass combo invalid for " + user;
			case DENIED:
				return "AAF denies API for " + user;
			default: 
				return "AAFAuthn doesn't handle Principal " + user;
		}
	}
	
	private class AAFCachedPrincipal extends ConfigPrincipal implements CachedPrincipal {
		private long expires,timeToLive;

		public AAFCachedPrincipal(AAFAuthn<?> aaf, String app, String name, byte[] pass, int timeToLive) {
			super(name,pass);
			this.timeToLive = timeToLive;
			expires = timeToLive + System.currentTimeMillis();
		}

		public Resp revalidate() {
			if(con.isDisabled()) {
				return Resp.DENIED;
			}
			try {
				Miss missed = missed(getName());
				if(missed==null || missed.mayContinue(getCred())) {
					Rcli<CLIENT> client = con.client(AAFCon.AAF_LATEST_VERSION).forUser(con.basicAuth(getName(), new String(getCred())));
					Future<String> fp = client.read(
							"/authn/basicAuth",
							"text/plain"
							);
					if(fp.get(con.timeout)) {
						expires = System.currentTimeMillis() + timeToLive;
						addUser(new User<AAFPermission>(this, expires));
						return Resp.REVALIDATED;
					} else {
						addMiss(getName(), getCred());
						return Resp.UNVALIDATED;
					}
				} else {
					return Resp.UNVALIDATED;
				}
			} catch (Exception e) {
				con.access.log(e);
				return Resp.INACCESSIBLE;
			}
		}

		public long expires() {
			return expires;
		}
	};

}
