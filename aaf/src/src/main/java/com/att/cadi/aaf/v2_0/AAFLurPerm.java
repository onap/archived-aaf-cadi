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
package com.att.cadi.aaf.v2_0;

import java.net.ConnectException;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Map;

import com.att.aft.dme2.api.DME2Exception;
import com.att.cadi.AbsUserCache;
import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.CachedPrincipal.Resp;
import com.att.cadi.CadiException;
import com.att.cadi.Permission;
import com.att.cadi.User;
import com.att.cadi.aaf.AAFPermission;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.inno.env.APIException;

import aaf.v2_0.Perm;
import aaf.v2_0.Perms;

/**
 * Use AAF Service as Permission Service.
 * 
 * This Lur goes after AAF Permissions, which are elements of Roles, not the Roles themselves.
 * 
 * If you want a simple Role Lur, use AAFRoleLur
 * 
 *
 */
public class AAFLurPerm extends AbsAAFLur<AAFPermission> {
	/**
	 *  Need to be able to transmutate a Principal into either ATTUID or MechID, which are the only ones accepted at this
	 *  point by AAF.  There is no "domain", aka, no "@att.com" in "ab1234@att.com".  
	 *  
	 *  The only thing that matters here for AAF is that we don't waste calls with IDs that obviously aren't valid.
	 *  Thus, we validate that the ID portion follows the rules before we waste time accessing AAF remotely
	 * @throws APIException 
	 * @throws URISyntaxException 
	 * @throws DME2Exception 
	 */
	// Package on purpose
	AAFLurPerm(AAFCon<?> con) throws CadiException, DME2Exception, URISyntaxException, APIException {
		super(con);
	}

	// Package on purpose
	AAFLurPerm(AAFCon<?> con, AbsUserCache<AAFPermission> auc) throws DME2Exception, URISyntaxException, APIException {
		super(con,auc);
	}

	protected User<AAFPermission> loadUser(Principal p)  {
		// Note: The rules for AAF is that it only stores permissions for ATTUID and MechIDs, which don't 
		// have domains.  We are going to make the Transitive Class (see this.transmutative) to convert
		Principal principal = transmutate.mutate(p);
		if(principal==null)return null; // if not a valid Transmutated credential, don't bother calling...
		return loadUser(p, p.getName());
	}
	
	protected User<AAFPermission> loadUser(String name) {
		return loadUser((Principal)null, name);
	}
	
	private User<AAFPermission> loadUser(final Principal prin, final String name) {
		
		//TODO Create a dynamic way to declare domains supported.
		final long start = System.nanoTime();
		final boolean[] success = new boolean[]{false};
		
//		new Exception("loadUser").printStackTrace();
		try {
			return aaf.best(new Retryable<User<AAFPermission>>() {
				@Override
				public User<AAFPermission> code(Rcli<?> client) throws CadiException, ConnectException, APIException {
					Future<Perms> fp = client.read("/authz/perms/user/"+name,aaf.permsDF);
					
					// In the meantime, lookup User, create if necessary
					User<AAFPermission> user = getUser(name);
					Principal p;
					if(prin == null) {
						p = new Principal() {// Create a holder for lookups
							private String n = name;
							public String getName() {
								return n;
							}
						};
					} else {
						p = prin;
					}
					
					if(user==null) {
						addUser(user = new User<AAFPermission>(p,aaf.userExpires)); // no password
					}
					
					// OK, done all we can, now get content
					if(fp.get(aaf.timeout)) {
						success[0]=true;
						Map<String, Permission> newMap = user.newMap();
						for(Perm perm : fp.value.getPerm()) {
							user.add(newMap,new AAFPermission(perm.getType(),perm.getInstance(),perm.getAction()));
							aaf.access.log(Level.DEBUG, name,"has '",perm.getType(),'|',perm.getInstance(),'|',perm.getAction(),'\'');
						}
						user.setMap(newMap);
						user.renewPerm();
					} else {
						int code;
						switch(code=fp.code()) {
							case 401:
								aaf.access.log(Access.Level.ERROR, code, "Unauthorized to make AAF calls");
								break;
							default:
								aaf.access.log(Access.Level.ERROR, code, fp.body());
						}
					}

					return user;
				}
			});
		} catch (Exception e) {
			aaf.access.log(e,"Calling","/authz/perms/user/"+name);
			return null;
		} finally {
			float time = (System.nanoTime()-start)/1000000f;
			aaf.access.log(Level.AUDIT, success[0]?"Loaded":"Load Failure",name,"from AAF in",time,"ms");
		}
	}

	public Resp reload(User<AAFPermission> user) {
		final String name = user.principal.getName();
		long start = System.nanoTime();
		boolean success = false;
		try {
			Future<Perms> fp = aaf.client(AAFCon.AAF_VERSION).read(
					"/authz/perms/user/"+name,
					aaf.permsDF
					);
			
			// OK, done all we can, now get content
			if(fp.get(aaf.timeout)) {
				success = true;
				Map<String,Permission> newMap = user.newMap(); 
				for(Perm perm : fp.value.getPerm()) {
					user.add(newMap, new AAFPermission(perm.getType(),perm.getInstance(),perm.getAction()));
					aaf.access.log(Level.DEBUG, name,"has",perm.getType(),perm.getInstance(),perm.getAction());
				}
				user.renewPerm();
				return Resp.REVALIDATED;
			} else {
				int code;
				switch(code=fp.code()) {
					case 401:
						aaf.access.log(Access.Level.ERROR, code, "Unauthorized to make AAF calls");
						break;
					default:
						aaf.access.log(Access.Level.ERROR, code, fp.body());
				}
				return Resp.UNVALIDATED;
			}
		} catch (Exception e) {
			aaf.access.log(e,"Calling","/authz/perms/user/"+name);
			return Resp.INACCESSIBLE;
		} finally {
			float time = (System.nanoTime()-start)/1000000f;
			aaf.access.log(Level.AUDIT, success?"Reloaded":"Reload Failure",name,"from AAF in",time,"ms");
		}
	}

	@Override
	protected boolean isCorrectPermType(Permission pond) {
		return pond instanceof AAFPermission;
	}

}
