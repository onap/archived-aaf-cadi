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

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.Hash;

public class AAFAuthenticationInfo implements AuthenticationInfo {
	private static final long serialVersionUID = -1502704556864321020L;
	
	// We assume that Shiro is doing Memory Only, and this salt is not needed cross process
	private final static int salt = new SecureRandom().nextInt(); 

	private final AAFPrincipalCollection apc;
	private final byte[] hash;
	private Access access;

	public AAFAuthenticationInfo(Access access, String username, String password) {
		this.access = access;
		apc = new AAFPrincipalCollection(username);
		hash = getSaltedCred(password);
	}
	
	@Override
	public byte[] getCredentials() {	
		access.log(Level.DEBUG, "AAFAuthenticationInfo.getCredentials");
		return hash;
	}

	@Override
	public PrincipalCollection getPrincipals() {
		access.log(Level.DEBUG, "AAFAuthenticationInfo.getPrincipals");
		return apc;
	}

	public boolean matches(AuthenticationToken atoken) {
		if(atoken instanceof UsernamePasswordToken) {
			UsernamePasswordToken upt = (UsernamePasswordToken)atoken;
			if(apc.getPrimaryPrincipal().getName().equals(upt.getPrincipal())) {
				byte[] newhash = getSaltedCred(new String(upt.getPassword()));
				if(newhash.length==hash.length) {
					for(int i=0;i<hash.length;++i) {
						if(hash[i]!=newhash[i]) {
							return false;
						}
					}
					access.printf(Level.DEBUG,"UserPassword Matches for %s",upt.getPrincipal());
					return true;
				}
			}
		} else {
			access.printf(Level.ERROR, "AAFAuthenticationInfo received non-AAF token %s (%s)",atoken.getPrincipal(),atoken.getClass().getName());
		}
		access.log(Level.DEBUG,"UserPassword does NOT match");
		return false;
	}
	
	private byte[] getSaltedCred(String password) {
		byte[] pbytes = password.getBytes();
		ByteBuffer bb = ByteBuffer.allocate(pbytes.length+Integer.SIZE/8);
		bb.asIntBuffer().put(salt);
		bb.put(password.getBytes());
		try {
			return Hash.hashSHA256(bb.array());
		} catch (NoSuchAlgorithmException e) {
			return new byte[0]; // should never get here
		}
	}
}
