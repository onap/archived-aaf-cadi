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
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.AbsUserCache;
import org.onap.aaf.cadi.CachedPrincipal;
import org.onap.aaf.cadi.GetCred;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.User;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CachedPrincipal.Resp;
import org.onap.aaf.cadi.Taf.LifeForm;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.principal.BasicPrincipal;
import org.onap.aaf.cadi.principal.CachedBasicPrincipal;
import org.onap.aaf.cadi.taf.HttpTaf;
import org.onap.aaf.cadi.taf.TafResp;
import org.onap.aaf.cadi.taf.TafResp.RESP;
import org.onap.aaf.cadi.taf.basic.BasicHttpTafResp;

public class AAFTaf<CLIENT> extends AbsUserCache<AAFPermission> implements HttpTaf {
//	private static final String INVALID_AUTH_TOKEN = "Invalid Auth Token";
//	private static final String AUTHENTICATING_SERVICE_UNAVAILABLE = "Authenticating Service unavailable";
	private AAFCon<CLIENT> aaf;
	private boolean warn;

	public AAFTaf(AAFCon<CLIENT> con, boolean turnOnWarning) {
		super(con.access,con.cleanInterval,con.highCount, con.usageRefreshTriggerCount);
		aaf = con;
		warn = turnOnWarning;
	}

	public AAFTaf(AAFCon<CLIENT> con, boolean turnOnWarning, AbsUserCache<AAFPermission> other) {
		super(other);
		aaf = con;
		warn = turnOnWarning;
	}

	public TafResp validate(LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
		//TODO Do we allow just anybody to validate?

		// Note: Either Carbon or Silicon based LifeForms ok
		String authz = req.getHeader("Authorization");
		if(authz != null && authz.startsWith("Basic ")) {
			if(warn&&!req.isSecure())aaf.access.log(Level.WARN,"WARNING! BasicAuth has been used over an insecure channel");
			try {
				CachedBasicPrincipal bp;
				if(req.getUserPrincipal() instanceof CachedBasicPrincipal) {
					bp = (CachedBasicPrincipal)req.getUserPrincipal();
				} else {
					bp = new CachedBasicPrincipal(this,authz,aaf.getRealm(),aaf.userExpires);
				}
				// First try Cache
				User<AAFPermission> usr = getUser(bp);
				if(usr != null && usr.principal != null) {
					if(usr.principal instanceof GetCred) {
						if(Hash.isEqual(bp.getCred(),((GetCred)usr.principal).getCred())) {
							return new BasicHttpTafResp(aaf.access,bp,bp.getName()+" authenticated by cached AAF password",RESP.IS_AUTHENTICATED,resp,aaf.getRealm(),false);
						}
					}
				}
				
				Miss miss = missed(bp.getName());
				if(miss!=null && !miss.mayContinue(bp.getCred())) {
					return new BasicHttpTafResp(aaf.access,null,buildMsg(bp,req,
							"User/Pass Retry limit exceeded"), 
							RESP.FAIL,resp,aaf.getRealm(),true);
				}
				
				Rcli<CLIENT> userAAF = aaf.client(AAFCon.AAF_LATEST_VERSION).forUser(aaf.basicAuthSS(bp));
				Future<String> fp = userAAF.read("/authn/basicAuth", "text/plain");
				if(fp.get(aaf.timeout)) {
					if(usr!=null) {
						usr.principal = bp;
					} else {
						addUser(new User<AAFPermission>(bp,aaf.userExpires));
					}
					return new BasicHttpTafResp(aaf.access,bp,bp.getName()+" authenticated by AAF password",RESP.IS_AUTHENTICATED,resp,aaf.getRealm(),false);
				} else {
					// Note: AddMiss checks for miss==null, and is part of logic
					boolean rv= addMiss(bp.getName(),bp.getCred());
					if(rv) {
						return new BasicHttpTafResp(aaf.access,null,buildMsg(bp,req,
								"User/Pass combo invalid via AAF"), 
								RESP.TRY_AUTHENTICATING,resp,aaf.getRealm(),true);
					} else {
						return new BasicHttpTafResp(aaf.access,null,buildMsg(bp,req,
								"User/Pass combo invalid via AAF - Retry limit exceeded"), 
								RESP.FAIL,resp,aaf.getRealm(),true);
					}
				}
			} catch (IOException e) {
				String msg = buildMsg(null,req,"Invalid Auth Token");
				aaf.access.log(Level.WARN,msg,'(', e.getMessage(), ')');
				return new BasicHttpTafResp(aaf.access,null,msg, RESP.TRY_AUTHENTICATING, resp, aaf.getRealm(),true);
			} catch (Exception e) {
				String msg = buildMsg(null,req,"Authenticating Service unavailable");
				aaf.access.log(Level.WARN,msg,'(', e.getMessage(), ')');
				return new BasicHttpTafResp(aaf.access,null,msg, RESP.FAIL, resp, aaf.getRealm(),false);
			}
		}
		return new BasicHttpTafResp(aaf.access,null,"Requesting HTTP Basic Authorization",RESP.TRY_AUTHENTICATING,resp,aaf.getRealm(),false);
	}
	
	private String buildMsg(Principal pr, HttpServletRequest req, Object ... msg) {
		StringBuilder sb = new StringBuilder();
		for(Object s : msg) {
			sb.append(s.toString());
		}
		if(pr!=null) {
			sb.append(" for ");
			sb.append(pr.getName());
		}
		sb.append(" from ");
		sb.append(req.getRemoteAddr());
		sb.append(':');
		sb.append(req.getRemotePort());
		return sb.toString();
	}


	
	public Resp revalidate(CachedPrincipal prin) {
		//  !!!! TEST THIS.. Things may not be revalidated, if not BasicPrincipal
		if(prin instanceof BasicPrincipal) {
			Future<String> fp;
			try {
				Rcli<CLIENT> userAAF = aaf.client(AAFCon.AAF_LATEST_VERSION).forUser(aaf.transferSS(prin));
				fp = userAAF.read("/authn/basicAuth", "text/plain");
				return fp.get(aaf.timeout)?Resp.REVALIDATED:Resp.UNVALIDATED;
			} catch (Exception e) {
				aaf.access.log(e, "Cannot Revalidate",prin.getName());
				return Resp.INACCESSIBLE;
			}
		}
		return Resp.NOT_MINE;
	}

}
