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
package org.onap.aaf.cadi.shiro.test;

import java.util.ArrayList;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.Test;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.shiro.AAFRealm;
import org.onap.aaf.cadi.shiro.AAFShiroPermission;

import junit.framework.Assert;

public class JU_AAFRealm {

	// TODO: Ian - fix this test
	// @Test
	// public void test() {
	// 	// NOTE This is a live test.  This JUnit needs to be built with "Mock"
	// 	try {
	// 		System.setProperty(Config.CADI_PROP_FILES, "/opt/app/osaaf/etc/org.osaaf.common.props");
	// 		TestAAFRealm ar = new TestAAFRealm();
			
	// 		UsernamePasswordToken upt = new UsernamePasswordToken("jonathan@people.osaaf.org", "new2You!");
	// 		AuthenticationInfo ani = ar.authn(upt);
			
	// 		AuthorizationInfo azi = ar.authz(ani.getPrincipals());
	// 		// Change this to something YOU have, Sai...
			
	// 		testAPerm(true,azi,"org.access","something","*");
	// 		testAPerm(false,azi,"org.accessX","something","*");
	// 	} catch (Throwable t) {
	// 		t.printStackTrace();
	// 		Assert.fail();
	// 	}
	// }

	private void testAPerm(boolean expect,AuthorizationInfo azi, String type, String instance, String action) {
		
		AAFShiroPermission testPerm = new AAFShiroPermission(new AAFPermission(type,instance,action,new ArrayList<String>()));

		boolean any = false;
		for(Permission p : azi.getObjectPermissions()) {
			if(p.implies(testPerm)) {
				any = true;
			}
		}
		if(expect) {
			Assert.assertTrue(any);
		} else {
			Assert.assertFalse(any);
		}

		
	}

	/**
	 * Note, have to create a derived class, because "doGet"... are protected
	 */
	private class TestAAFRealm extends AAFRealm {
		public AuthenticationInfo authn(UsernamePasswordToken upt) {
			return doGetAuthenticationInfo(upt);
		}
		public AuthorizationInfo authz(PrincipalCollection pc) {
			return doGetAuthorizationInfo(pc);
		}
		
	}
}
