/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.cadi.shiro;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.aaf.AAFPermission;

/**
 * We treat "roles" and "permissions" in a similar way for first pass.
 * 
 * @author JonathanGathman
 *
 */
public class AAFAuthorizationInfo implements AuthorizationInfo {
	private static final long serialVersionUID = -4805388954462426018L;
	
	private Access access;
	private Principal bait;
	private List<org.onap.aaf.cadi.Permission> pond;
	// Use these to save conversions
	private List<org.onap.aaf.cadi.Permission> cPerms;
	private List<Permission> oPerms;
	private List<String> sPerms;

	public AAFAuthorizationInfo(Access access, Principal bait) {
		this.access = access;
		this.bait = bait;
		cPerms=null;
		oPerms=null;
		sPerms=null;
		pond=null;
	}

	public AAFAuthorizationInfo(Access access, Principal bait, List<org.onap.aaf.cadi.Permission> pond) {
		this.access = access;
		this.bait = bait;
		this.pond = pond;
		oPerms=null;
		sPerms=null;
		cPerms=null;
	}
	
	public Principal principal() {
		return bait;
	}
	
	@Override
	public Collection<Permission> getObjectPermissions() {
		access.log(Level.DEBUG, "AAFAuthorizationInfo.getObjectPermissions");
		synchronized(bait) {
			if(oPerms == null) {
				if (pond != null) {
					oPerms = new ArrayList<Permission>();
					for(final org.onap.aaf.cadi.Permission p : pond) {
						oPerms.add(new AAFShiroPermission(p));
					}
				} else {
					oPerms = new ArrayList<>();
					if (cPerms == null) {
						cPerms = new ArrayList<>();
						AAFRealm.singleton.authz.fishAll(bait, cPerms);
					}
					for (final org.onap.aaf.cadi.Permission p : cPerms) {
						oPerms.add(new AAFShiroPermission(p));
					}
				}
			}
		}
		return oPerms;
	}

	@Override
	public Collection<String> getRoles() {
		access.log(Level.DEBUG,"AAFAuthorizationInfo.getRoles");
		// Until we decide to make Roles available, tie into String based permissions.
		return getStringPermissions();
	}

	@Override
	public Collection<String> getStringPermissions() {
		access.log(Level.DEBUG,"AAFAuthorizationInfo.getStringPermissions");
		synchronized(bait) {
			if(sPerms == null) {
				if (pond != null) {
					sPerms = new ArrayList<String>();
					for(org.onap.aaf.cadi.Permission p : pond) {
						sPerms.add(p.getKey().replace("|", ":"));
						access.printf(Level.INFO, "%s has %s", bait.getName(), p.getKey());
					}
				} else {
					sPerms = new ArrayList<>();
					if (cPerms == null) {
						cPerms = new ArrayList<>();
						AAFRealm.singleton.authz.fishAll(bait, cPerms);
					}
					for (final org.onap.aaf.cadi.Permission p : cPerms) {
						sPerms.add(p.getKey());
					}
				}
			}
		}
		return sPerms;
	}

}
