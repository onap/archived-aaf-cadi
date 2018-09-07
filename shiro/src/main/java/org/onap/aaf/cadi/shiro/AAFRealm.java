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

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.onap.aaf.cadi.aaf.v2_0.AAFCon;
import org.onap.aaf.cadi.aaf.v2_0.AAFLurPerm;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;

public class AAFRealm extends AuthorizingRealm {
	public static final String AAF_REALM = "AAFRealm";
	
	private PropAccess access;
	private AAFCon<?> acon;
	private AAFAuthn<?> authn;
	private HashSet<Class<? extends AuthenticationToken>> supports;
	private AAFLurPerm authz;
	

	/**
	 * 
	 * There appears to be no configuration objects or references available for CADI to start with.
	 *  
	 */
	public AAFRealm () {
		access = new PropAccess(); // pick up cadi_prop_files from VM_Args
		String cadi_prop_files = access.getProperty(Config.CADI_PROP_FILES);
		if(cadi_prop_files==null) {
			String msg = Config.CADI_PROP_FILES + " in VM Args is required to initialize AAFRealm.";
			access.log(Level.INIT,msg);
			throw new RuntimeException(msg);
		} else {
			try {
				acon = AAFCon.newInstance(access);
				authn = acon.newAuthn();
				authz = acon.newLur(authn);
			} catch (APIException | CadiException | LocatorException e) {
				String msg = "Cannot initiate AAFRealm";
				access.log(Level.INIT,msg,e.getMessage());
				throw new RuntimeException(msg,e);
			}
		}
		supports = new HashSet<Class<? extends AuthenticationToken>>();
		supports.add(UsernamePasswordToken.class);
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		access.log(Level.DEBUG, "AAFRealm.doGetAuthenticationInfo",token);
		
		final UsernamePasswordToken upt = (UsernamePasswordToken)token;
		String password=new String(upt.getPassword());
		String err;
		try {
			err = authn.validate(upt.getUsername(),password);
		} catch (CadiException | IOException e) {
			err = "Credential cannot be validated";
			access.log(e, err);
		}
		
		if(err != null) {
			access.log(Level.DEBUG, err);
			throw new AuthenticationException(err);
		}

	    return new AAFAuthenticationInfo(
	    		access,
	    		upt.getUsername(),
	    		password
	    );
	}

	@Override
	protected void assertCredentialsMatch(AuthenticationToken atoken, AuthenticationInfo ai)throws AuthenticationException {
		if(ai instanceof AAFAuthenticationInfo) {
			if(!((AAFAuthenticationInfo)ai).matches(atoken)) {
				throw new AuthenticationException("Credentials do not match");
			}
		} else {
			throw new AuthenticationException("AuthenticationInfo is not an AAFAuthenticationInfo");
		}
	}


	@Override
	protected AAFAuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		access.log(Level.DEBUG, "AAFRealm.doGetAuthenthorizationInfo");
		Principal bait = (Principal)principals.getPrimaryPrincipal();
		List<Permission> pond = new ArrayList<>();
		authz.fishAll(bait,pond);
		
		return new AAFAuthorizationInfo(access,bait,pond);
       
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		return supports.contains(token.getClass());
	}

	@Override
	public String getName() {
		return AAF_REALM;
	}

}
