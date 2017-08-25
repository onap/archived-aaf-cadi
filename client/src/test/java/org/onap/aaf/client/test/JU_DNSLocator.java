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
package org.onap.aaf.client.test;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.junit.AfterClass;
import org.junit.Test;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.locator.DNSLocator;

public class JU_DNSLocator {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		
		DNSLocator dl = new DNSLocator(new PropAccess(), "https", "aaf.it.att.com","8150-8152");
		try {
			Item item = dl.best();
			URI uri = dl.get(item);
			URL url = uri.toURL();
			URLConnection conn = url.openConnection();
			conn.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
