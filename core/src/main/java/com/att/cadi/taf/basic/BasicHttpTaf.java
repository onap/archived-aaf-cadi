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
package com.att.cadi.taf.basic;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.BasicCred;
import com.att.cadi.CachedPrincipal;
import com.att.cadi.CachedPrincipal.Resp;
import com.att.cadi.CredVal;
import com.att.cadi.CredVal.Type;
import com.att.cadi.Taf;
import com.att.cadi.principal.BasicPrincipal;
import com.att.cadi.principal.CachedBasicPrincipal;
import com.att.cadi.taf.HttpTaf;
import com.att.cadi.taf.TafResp;
import com.att.cadi.taf.TafResp.RESP;
import com.att.cadi.taf.dos.DenialOfServiceTaf;

/**
 * BasicHttpTaf
 * 
 * This TAF implements the "Basic Auth" protocol.  
 * 
 * WARNING! It is true for any implementation of "Basic Auth" that the password is passed unencrypted.  
 * This is because the expectation, when designed years ago, was that it would only be used in 
 * conjunction with SSL (https).  It is common, however, for users to ignore this on the assumption that
 * their internal network is secure, or just ignorance.  Therefore, a WARNING will be printed
 * when the HTTP Channel is not encrypted (unless explicitly turned off).
 * 
 *
 */
public class BasicHttpTaf implements HttpTaf {
	private Access access;
	private String realm;
	private CredVal rbac;
	private boolean warn;
	private long timeToLive;
	
	public BasicHttpTaf(Access access, CredVal rbac, String realm, long timeToLive, boolean turnOnWarning) {
		this.access = access;
		this.realm = realm;
		this.rbac = rbac;
		this.warn = turnOnWarning;
		this.timeToLive = timeToLive;
	}

	/**
	 * Note: BasicHttp works for either Carbon Based (Humans) or Silicon Based (machine) Lifeforms.  
	 * @see Taf
	 */
	public TafResp validate(Taf.LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
		// See if Request implements BasicCred (aka CadiWrap or other), and if User/Pass has already been set separately
		if(req instanceof BasicCred) {
			BasicCred bc = (BasicCred)req;
			if(bc.getUser()!=null) { // CadiWrap, if set, makes sure User & Password are both valid, or both null
				if(DenialOfServiceTaf.isDeniedID(bc.getUser())!=null) {
					return DenialOfServiceTaf.respDenyID(access,bc.getUser());
				}
				CachedBasicPrincipal bp = new CachedBasicPrincipal(this,bc,realm,timeToLive);
				// ONLY FOR Last Ditch DEBUGGING... 
				// access.log(Level.WARN,bp.getName() + ":" + new String(bp.getCred()));
				if(rbac.validate(bp.getName(),Type.PASSWORD,bp.getCred())) {
					return new BasicHttpTafResp(access,bp,bp.getName()+" authenticated by password",RESP.IS_AUTHENTICATED,resp,realm,false);
				} else {
					//TODO may need timed retries in a given time period
					return new BasicHttpTafResp(access,null,buildMsg(bp,req,"User/Pass combo invalid for ",bc.getUser()), 
							RESP.TRY_AUTHENTICATING,resp,realm,true);
				}
			}
		}
		// Get User/Password from Authorization Header value
		String authz = req.getHeader("Authorization");
		if(authz != null && authz.startsWith("Basic ")) {
			if(warn&&!req.isSecure()) {
				access.log(Level.WARN,"WARNING! BasicAuth has been used over an insecure channel");
			}
			try {
				CachedBasicPrincipal ba = new CachedBasicPrincipal(this,authz,realm,timeToLive);
				if(DenialOfServiceTaf.isDeniedID(ba.getName())!=null) {
					return DenialOfServiceTaf.respDenyID(access,ba.getName());
				}

				// ONLY FOR Last Ditch DEBUGGING... 
				// access.log(Level.WARN,ba.getName() + ":" + new String(ba.getCred()));
				if(rbac.validate(ba.getName(), Type.PASSWORD, ba.getCred())) {
					return new BasicHttpTafResp(access,ba, ba.getName()+" authenticated by BasicAuth password",RESP.IS_AUTHENTICATED,resp,realm,false);
				} else {
					//TODO may need timed retries in a given time period
					return new BasicHttpTafResp(access,null,buildMsg(ba,req,"User/Pass combo invalid"), 
							RESP.TRY_AUTHENTICATING,resp,realm,true);
				}
			} catch (IOException e) {
				String msg = buildMsg(null,req,"Failed HTTP Basic Authorization (", e.getMessage(), ')');
				access.log(Level.INFO,msg);
				return new BasicHttpTafResp(access,null,msg, RESP.TRY_AUTHENTICATING, resp, realm,true);
			}
		}
		return new BasicHttpTafResp(access,null,"Requesting HTTP Basic Authorization",RESP.TRY_AUTHENTICATING,resp,realm,false);
	}
	
	protected String buildMsg(Principal pr, HttpServletRequest req, Object ... msg) {
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

	@Override
	public Resp revalidate(CachedPrincipal prin) {
		if(prin instanceof BasicPrincipal) {
			BasicPrincipal ba = (BasicPrincipal)prin;
			if(DenialOfServiceTaf.isDeniedID(ba.getName())!=null) {
				return Resp.UNVALIDATED;
			}
			return rbac.validate(ba.getName(), Type.PASSWORD, ba.getCred())?Resp.REVALIDATED:Resp.UNVALIDATED;
		}
		return Resp.NOT_MINE;
	}
	
	public String toString() {
		return "Basic Auth enabled on realm: " + realm;
	}
}
