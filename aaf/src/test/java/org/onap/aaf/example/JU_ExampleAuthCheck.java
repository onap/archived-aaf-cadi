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
package org.onap.aaf.example;

import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.onap.aaf.cadi.aaf.v2_0.AAFConHttp;
import org.onap.aaf.cadi.locator.DNSLocator;

public class JU_ExampleAuthCheck {
	public static void main(String args[]) {
		// Link or reuse to your Logging mechanism
		PropAccess myAccess = new PropAccess(); // 
		
		try {
			AAFConHttp acon = new AAFConHttp(myAccess, new DNSLocator(
					myAccess,"https","localhost","8100"));
			AAFAuthn<?> authn = acon.newAuthn();
			long start; 
			for (int i=0;i<10;++i) {
				start = System.nanoTime();
				String err = authn.validate("", "gritty");
				if(err!=null) System.err.println(err);
				else System.out.println("I'm ok");
				
				err = authn.validate("bogus", "gritty");
				if(err!=null) System.err.println(err + " (correct error)");
				else System.out.println("I'm ok");

				System.out.println((System.nanoTime()-start)/1000000f + " ms");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
