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
package com.att.aaf.example;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import com.att.cadi.Permission;
import com.att.cadi.PropAccess;
import com.att.cadi.aaf.AAFPermission;
import com.att.cadi.aaf.v2_0.AAFAuthn;
import com.att.cadi.aaf.v2_0.AAFConHttp;
import com.att.cadi.aaf.v2_0.AAFLurPerm;
import com.att.cadi.locator.DNSLocator;

public class ExamplePerm2_0_DME2 {
	public static void main(String args[]) {
		// Link or reuse to your Logging mechanism
		PropAccess myAccess = new PropAccess();  
		
		// 
		try {
			AAFConHttp acon = new AAFConHttp(myAccess, new DNSLocator(
					myAccess,"https","localhost","8100"));
			
			// AAFLur has pool of DME clients as needed, and Caches Client lookups
			AAFLurPerm aafLur = acon.newLur();
			
			// Note: If you need both Authn and Authz construct the following:
			AAFAuthn<?> aafAuthn = acon.newAuthn(aafLur);

			// Do not set Mech ID until after you construct AAFAuthn,
			// because we initiate  "401" info to determine the Realm of 
			// of the service we're after.
			acon.basicAuth("mc0897@aaf.att.com", "XXXXXX");

			try {
				
				// Normally, you obtain Principal from Authentication System.
				// For J2EE, you can ask the HttpServletRequest for getUserPrincipal()
				// If you use CADI as Authenticator, it will get you these Principals from
				// CSP or BasicAuth mechanisms.
				String id = "mc0897@aaf.att.com"; //"cluster_admin@gridcore.att.com";

				// If Validate succeeds, you will get a Null, otherwise, you will a String for the reason.
				String ok = aafAuthn.validate(id, "XXXXXX");
				if(ok!=null)System.out.println(ok);
				
				ok = aafAuthn.validate(id, "wrongPass");
				if(ok!=null)System.out.println(ok);


				// AAF Style permissions are in the form
				// Type, Instance, Action 
				AAFPermission perm = new AAFPermission("com.att.grid.core.coh",":dev_cluster", "WRITE");
				
				// Now you can ask the LUR (Local Representative of the User Repository about Authorization
				// With CADI, in J2EE, you can call isUserInRole("com.att.mygroup|mytype|write") on the Request Object 
				// instead of creating your own LUR
				System.out.println("Does " + id + " have " + perm);
				if(aafLur.fish(id, perm)) {
					System.out.println("Yes, you have permission");
				} else {
					System.out.println("No, you don't have permission");
				}

				System.out.println("Does Bogus have " + perm);
				if(aafLur.fish("Bogus", perm)) {
					System.out.println("Yes, you have permission");
				} else {
					System.out.println("No, you don't have permission");
				}

				// Or you can all for all the Permissions available
				List<Permission> perms = new ArrayList<Permission>();
				
				aafLur.fishAll(id,perms);
				for(Permission prm : perms) {
					System.out.println(prm.getKey());
				}
				
				// It might be helpful in some cases to clear the User's identity from the Cache
				aafLur.remove(id);
			} finally {
				aafLur.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
