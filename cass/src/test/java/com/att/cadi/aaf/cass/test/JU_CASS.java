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
package com.att.cadi.aaf.cass.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.IResource;
import org.apache.cassandra.auth.Permission;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.att.cadi.aaf.cass.AAFAuthenticator;
import com.att.cadi.aaf.cass.AAFAuthorizer;

public class JU_CASS {

	private static AAFAuthenticator aa;
	private static AAFAuthorizer an;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("cadi_prop_files", "etc/cadi.properties");
		
		aa = new AAFAuthenticator();
		an = new AAFAuthorizer();

		aa.setup();
		an.setup(); // does nothing after aa.
		
		aa.validateConfiguration();
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws Exception {
			Map<String,String> creds = new HashMap<String,String>();
			creds.put("username", "XXX@NS");
			creds.put("password", "enc:???");
			AuthenticatedUser aaf = aa.authenticate(creds);

			// Test out "aaf_default_domain
			creds.put("username", "XX");
			aaf = aa.authenticate(creds);
			
			IResource resource = new IResource() {
				public String getName() {
					return "data/authz";
				}

				public IResource getParent() {
					return null;
				}

				public boolean hasParent() {
					return false;
				}

				public boolean exists() {
					return true;
				}
				
			};
			
			Set<Permission> perms = an.authorize(aaf, resource);
			
			// Test out "AAF" access
			creds.put("username", "XXX@NS");
			creds.put("password", "enc:???");
			aaf = aa.authenticate(creds);
			perms = an.authorize(aaf, resource);
			Assert.assertFalse(perms.isEmpty());

			perms = an.authorize(aaf, resource);
			Assert.assertFalse(perms.isEmpty());
			
	}

}
