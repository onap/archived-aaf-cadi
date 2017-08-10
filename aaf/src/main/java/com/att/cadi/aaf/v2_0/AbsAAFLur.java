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

import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.att.aft.dme2.api.DME2Exception;
import com.att.cadi.AbsUserCache;
import com.att.cadi.Access.Level;
import com.att.cadi.CachingLur;
import com.att.cadi.Permission;
import com.att.cadi.StrLur;
import com.att.cadi.Transmutate;
import com.att.cadi.User;
import com.att.cadi.config.Config;
import com.att.cadi.aaf.AAFPermission;
import com.att.cadi.aaf.AAFTransmutate;
import com.att.inno.env.APIException;
import com.att.inno.env.util.Split;

public abstract class AbsAAFLur<PERM extends Permission> extends AbsUserCache<PERM> implements StrLur, CachingLur<PERM> {
	protected static final byte[] BLANK_PASSWORD = new byte[0];
	protected static final Transmutate<Principal> transmutate = new AAFTransmutate();
	private String[] debug = null;
	public AAFCon<?> aaf;
	private String[] supports;

	public AbsAAFLur(AAFCon<?> con) throws DME2Exception, URISyntaxException, APIException {
		super(con.access, con.cleanInterval, con.highCount, con.usageRefreshTriggerCount);
		aaf = con;
		setLur(this);
		supports = con.access.getProperty(Config.AAF_DOMAIN_SUPPORT, Config.AAF_DOMAIN_SUPPORT_DEF).split("\\s*:\\s*");
	}

	public AbsAAFLur(AAFCon<?> con, AbsUserCache<PERM> auc) throws DME2Exception, URISyntaxException, APIException {
		super(auc);
		aaf = con;
		setLur(this);
		supports = con.access.getProperty(Config.AAF_DOMAIN_SUPPORT, Config.AAF_DOMAIN_SUPPORT_DEF).split("\\s*:\\s*");
	}

	@Override
	public void setDebug(String ids) {
		this.debug = ids==null?null:Split.split(',', ids);
	}
	
	protected abstract User<PERM> loadUser(Principal bait);
	protected abstract User<PERM> loadUser(String name);
	public final boolean supports(String userName) {
		if(userName!=null) {
			for(String s : supports) {
				if(userName.endsWith(s))
					return true;
			}
		}
		return false;
	}
	
	protected abstract boolean isCorrectPermType(Permission pond);
	
	// This is where you build AAF CLient Code.  Answer the question "Is principal "bait" in the "pond"
	public boolean fish(Principal bait, Permission pond) {
		return fish(bait.getName(), pond);
	}

	public void fishAll(Principal bait, List<Permission> perms) {
		fishAll(bait.getName(),perms);
	}

	// This is where you build AAF CLient Code.  Answer the question "Is principal "bait" in the "pond"
	public boolean fish(String bait, Permission pond) {
		if(isDebug(bait)) {
			boolean rv = false;
			StringBuilder sb = new StringBuilder("Log for ");
			sb.append(bait);
			if(supports(bait)) {
				User<PERM> user = getUser(bait);
				if(user==null) {
					sb.append("\n\tUser is not in Cache");
				} else {
					if(user.noPerms())sb.append("\n\tUser has no Perms");
					if(user.permExpired()) {
						sb.append("\n\tUser's perm expired [");
						sb.append(new Date(user.permExpires()));
						sb.append(']');
					} else {
						sb.append("\n\tUser's perm expires [");
						sb.append(new Date(user.permExpires()));
						sb.append(']');
					}
				}
				if(user==null || (user.noPerms() && user.permExpired())) {
					user = loadUser(bait);
					sb.append("\n\tloadUser called");
				}
				if(user==null) {
					sb.append("\n\tUser was not Loaded");
				} else if(user.contains(pond)) {
					sb.append("\n\tUser contains ");
					sb.append(pond.getKey());
					rv = true;
				} else {
					sb.append("\n\tUser does not contain ");
					sb.append(pond.getKey());
					List<Permission> perms = new ArrayList<Permission>();
					user.copyPermsTo(perms);
					for(Permission p : perms) {
						sb.append("\n\t\t");
						sb.append(p.getKey());
					}
				}
			} else {
				sb.append("AAF Lur does not support [");
				sb.append(bait);
				sb.append("]");
			}
			aaf.access.log(Level.INFO, sb);
			return rv;
		} else {
			if(supports(bait)) {
				User<PERM> user = getUser(bait);
				if(user==null || (user.noPerms() && user.permExpired())) {
					user = loadUser(bait);
				}
				return user==null?false:user.contains(pond);
			}
			return false;
		}
	}

	public void fishAll(String bait, List<Permission> perms) {
		if(isDebug(bait)) {
			StringBuilder sb = new StringBuilder("Log for ");
			sb.append(bait);
			if(supports(bait)) {
				User<PERM> user = getUser(bait);
				if(user==null) {
					sb.append("\n\tUser is not in Cache");
				} else {
					if(user.noPerms())sb.append("\n\tUser has no Perms");
					if(user.permExpired()) {
						sb.append("\n\tUser's perm expired [");
						sb.append(new Date(user.permExpires()));
						sb.append(']');
					} else {
						sb.append("\n\tUser's perm expires [");
						sb.append(new Date(user.permExpires()));
						sb.append(']');
					}
				}
				if(user==null || (user.noPerms() && user.permExpired())) {
					user = loadUser(bait);
					sb.append("\n\tloadUser called");
				}
				if(user==null) {
					sb.append("\n\tUser was not Loaded");
				} else {
					sb.append("\n\tCopying Perms ");
					user.copyPermsTo(perms);
					for(Permission p : perms) {
						sb.append("\n\t\t");
						sb.append(p.getKey());
					}
				}
			} else {
				sb.append("AAF Lur does not support [");
				sb.append(bait);
				sb.append("]");
			}
			aaf.access.log(Level.INFO, sb);
		} else {
			if(supports(bait)) {
				User<PERM> user = getUser(bait);
				if(user==null || (user.noPerms() && user.permExpired())) user = loadUser(bait);
				if(user!=null) {
					user.copyPermsTo(perms);
				}
			}
		}
	}
	
	@Override
	public void remove(String user) {
		super.remove(user);
	}

	private boolean isDebug(String bait) {
		if(debug!=null) {
			if(debug.length==1 && "all".equals(debug[0]))return true;
			for(String s : debug) {
				if(s.equals(bait))return true;
			}
		}
		return false;
	}
	/**
	 * This special case minimizes loops, avoids multiple Set hits, and calls all the appropriate Actions found.
	 * 
	 * @param bait
	 * @param obj
	 * @param type
	 * @param instance
	 * @param actions
	 */
	public<A> void fishOneOf(String bait, A obj, String type, String instance, List<Action<A>> actions) {
		User<PERM> user = getUser(bait);
		if(user==null || (user.noPerms() && user.permExpired()))user = loadUser(bait);
//		return user==null?false:user.contains(pond);
		if(user!=null) {
			ReuseAAFPermission perm = new ReuseAAFPermission(type,instance);
			for(Action<A> action : actions) {
				perm.setAction(action.getName());
				if(user.contains(perm)) {
					if(action.exec(obj))return;
				}
			}
		}
	}
	
	public static interface Action<A> {
		public String getName();
		/**
		 *  Return false to continue, True to end now
		 * @return
		 */
		public boolean exec(A a);
	}
	
	private class ReuseAAFPermission extends AAFPermission {
		public ReuseAAFPermission(String type, String instance) {
			super(type,instance,null);
		}

		public void setAction(String s) {
			action = s;
		}
		
		/**
		 * This function understands that AAF Keys are hierarchical, :A:B:C, 
		 *  Cassandra follows a similar method, so we'll short circuit and do it more efficiently when there isn't a first hit
		 * @return
		 */
	}
}
