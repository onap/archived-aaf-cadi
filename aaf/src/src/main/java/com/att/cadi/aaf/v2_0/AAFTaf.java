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
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.cadi.AbsUserCache;
import com.att.cadi.Access.Level;
import com.att.cadi.CachedPrincipal;
import com.att.cadi.CachedPrincipal.Resp;
import com.att.cadi.GetCred;
import com.att.cadi.Hash;
import com.att.cadi.Taf.LifeForm;
import com.att.cadi.User;
import com.att.cadi.aaf.AAFPermission;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.principal.BasicPrincipal;
import com.att.cadi.principal.CachedBasicPrincipal;
import com.att.cadi.taf.HttpTaf;
import com.att.cadi.taf.TafResp;
import com.att.cadi.taf.TafResp.RESP;
import com.att.cadi.taf.basic.BasicHttpTafResp;

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
		String auth = req.getHeader("Authorization");
		
		System.out.println("value of auth  ------1------- ++++++++++++++++++++++++++++++++++++++++++" +auth);
		
		if(auth == null) {
			return new BasicHttpTafResp(aaf.access,null,"Requesting HTTP Basic Authorization",RESP.TRY_AUTHENTICATING,resp,aaf.getRealm(),false);
		} else  {
			if(warn&&!req.isSecure())aaf.access.log(Level.WARN,"WARNING! BasicAuth has been used over an insecure channel");
			
			try {
				CachedBasicPrincipal bp = new CachedBasicPrincipal(this,auth,aaf.getRealm(),aaf.cleanInterval);
				System.out.println(" value of aaf.getRealm  --------2--------- +++++++++++++++++++++++++++++++++++++++++++++" +aaf.getRealm() );
				//System.out.println(" value of bp +++++++++++++++++++++++++++++++++++++++++++" +bp.toString());
				System.out.println(" value of bp.getName() -------3----- +++++++++++++++++++++++++++++++++++++++++++" +bp.getName().toString());
				System.out.println(" value of bp.getCred() -------4----- +++++++++++++++++++++++++++++++++++++++++++" +bp.getCred().toString());
				
				// First try Cache
				User<AAFPermission> usr = getUser(bp);
				
			//	System.out.println(" value of usr -------5-------++++++++++++++++++++++++++++++++++++++++++" +usr.toString());
				
				if(usr != null && usr.principal != null) {
					if(usr.principal instanceof GetCred) {
						if(Hash.isEqual(bp.getCred(),((GetCred)usr.principal).getCred())) {
							
							return new BasicHttpTafResp(aaf.access,bp,bp.getName()+" authenticated by cached AAF password",RESP.IS_AUTHENTICATED,resp,aaf.getRealm(),false);
						}
					}
				}
				
				Miss miss = missed(bp.getName());
				 System.out.println(" value of miss before if loop  ---------6----- +++++++++++++++++++++++++++++++++++++" +miss );
				if(miss!=null && !miss.mayContinue(bp.getCred())) {
					
					System.out.println(" In if(miss!=null && !miss.mayContinue(bp.getCred())) -------7--------+++++++++++++++++++++++++++++++++++++++++++++");
					
					return new BasicHttpTafResp(aaf.access,null,buildMsg(bp,req,
							"User/Pass Retry limit exceeded"), 
							RESP.FAIL,resp,aaf.getRealm(),true);
				}
				
				Rcli<CLIENT> userAAF = aaf.client(AAFCon.AAF_VERSION).forUser(aaf.basicAuthSS(bp));
				
				//System.out.println("value of userAAF ------8---- +++++++++++++++++++++++" +userAAF);
				//System.out.println("value of userAAF +++++++++++++++++++++++" +userAAF.);
				Future<String> fp = userAAF.read("/authn/basicAuth", "text/plain");
				
				//System.out.println("value of fp --------9------ +++++++++++++++++++++++" +fp.toString());
				
				if(fp.get(aaf.timeout)) {
					System.out.println("In fp.get check -----10----- +++++++++++++");
					if(usr!=null)usr.principal = bp;

					else addUser(new User<AAFPermission>(bp,aaf.cleanInterval));
					return new BasicHttpTafResp(aaf.access,bp,bp.getName()+" authenticated by AAF password",RESP.IS_AUTHENTICATED,resp,aaf.getRealm(),false);
				} else {
					// Note: AddMiss checks for miss==null, and is part of logic
					
					System.out.println(" In the else part --------11--------++++++++++++++ ");
					
					boolean rv= addMiss(bp.getName(),bp.getCred());
					System.out.println(" value of bp.getName() and bp.getCred() before if check  ----12--- ++++++++++++!!!!!!!!!!!++++++++++" +bp.getName() +"and " +bp.getCred());

					if(rv) {
						System.out.println("In if(rv) check -----13----- +++++++++++++");
						return new BasicHttpTafResp(aaf.access,null,buildMsg(bp,req,
								"User/Pass combo invalid via AAF"), 
								RESP.TRY_AUTHENTICATING,resp,aaf.getRealm(),true);
					} else {
						System.out.println("In if(rv) else check -----14----- +++++++++++++");
						return new BasicHttpTafResp(aaf.access,null,buildMsg(bp,req,
								"User/Pass combo invalid via AAF - Retry limit exceeded"), 
								RESP.FAIL,resp,aaf.getRealm(),true);
					}
				}
			} catch (IOException e) {
				String msg = buildMsg(null,req,"Invalid Auth Token");
				System.out.println("In IOException catch block -----15----- +++++++++++++");
				e.getStackTrace();
				e.printStackTrace();
				aaf.access.log(Level.INFO,msg,'(', e.getMessage(), ')');
				return new BasicHttpTafResp(aaf.access,null,msg, RESP.TRY_AUTHENTICATING, resp, aaf.getRealm(),true);
			} catch (Exception e) {
				String msg = buildMsg(null,req,"Authenticating Service unavailable");
				System.out.println("In Exception catch block  -----16----- +++++++++++++");
				e.getStackTrace();
				e.printStackTrace();
				aaf.access.log(Level.INFO,msg,'(', e.getMessage(), ')');
				return new BasicHttpTafResp(aaf.access,null,msg, RESP.FAIL, resp, aaf.getRealm(),false);
			}
		}
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
				Rcli<CLIENT> userAAF = aaf.client(AAFCon.AAF_VERSION).forUser(aaf.transferSS(prin));
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
