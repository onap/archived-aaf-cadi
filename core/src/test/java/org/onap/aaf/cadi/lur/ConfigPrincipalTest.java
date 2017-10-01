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
package org.onap.aaf.cadi.lur;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class ConfigPrincipalTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testConfigPrincipalStringString() {
		ConfigPrincipal p =  new ConfigPrincipal("User", "pass");
		
		assertEquals(p.getName(), "User");
		assertEquals(p.toString(), "User");
		
	}

	@Test
	public void testConfigPrincipalStringByteArray() throws IOException {
		byte[] bytes = "pass".getBytes();
		ConfigPrincipal p =  new ConfigPrincipal("User", bytes);
		
		assertEquals(p.getName(), "User");
		assertEquals(p.getCred(), bytes);
		assertEquals(p.toString(), "User");
		assertTrue(p.getAsBasicAuthHeader().startsWith("Basic"));
	}
	
	@Test
	public void testConfigPrincipalStringString1() {
		ConfigPrincipal p =  new ConfigPrincipal("Demo", "password");
		
		assertEquals(p.getName(), "Demo");
		assertEquals(p.toString(), "Demo");
		
	}

	@Test
	public void testConfigPrincipalStringByteArray1() throws IOException {
		byte[] bytes = "password".getBytes();
		ConfigPrincipal p =  new ConfigPrincipal("Demo", bytes);
		
		assertEquals(p.getName(), "Demo");
		assertEquals(p.getCred(), bytes);
		assertEquals(p.toString(), "Demo");
		assertTrue(p.getAsBasicAuthHeader().startsWith("Basic"));
	}

}
