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
package org.onap.aaf.cadi.test;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A Class to run on command line to determine suitability of environment for certain TAFs.
 * 
 * For instance, CSP supports services only in certain domains, and while dynamic host
 * lookups on the machine work in most cases, sometimes, names and IPs are unexpected (and
 * invalid) for CSP because of multiple NetworkInterfaces, etc
 * 
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("CADI/TAF test");
			
			String hostName = InetAddress.getLocalHost().getCanonicalHostName();
			
			System.out.println("  Your automatic hostname is reported as \"" + hostName + "\"\n");
			String[] two;

			for(String str : args) {
				two = str.split("=");
				if(two.length==2) {
					if("hostname".equals(two[0])) {
						hostName = two[1];
						System.out.println("  You have overlaid the automatic hostname with \"" + hostName + "\"\n");
					}
				}
			}
			if(hostName.endsWith("vpn.cingular.net"))
				System.out.println("  This service appears to be an AT&T VPN address. These VPNs are typically\n" +
						"    (and appropriately) firewalled against incoming traffic, and likely cannot be accessed.\n" +
						"    For best results, choose a machine that is not firewalled on the ports you choose.\n");
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

}
