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
package org.onap.aaf.cadi.http;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class JU_HBasicAuthSSTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHBasicAuthSSStringStringSecurityInfoCOfHttpURLConnection() throws IOException {
		HBasicAuthSS basicAuth = new HBasicAuthSS("user", "pass", null); 
		
		assertEquals(basicAuth.getID(), "user");
		assertFalse(basicAuth.isDenied());
		assertEquals(basicAuth.count(), 0);
		assertEquals(basicAuth.setLastResponse(401), 1);
	}
	
	@Test
	public void testHBasicAuthSSStringStringSecurityInfoCOfHttpURLConnection_one() throws IOException {
		HBasicAuthSS basicAuth = new HBasicAuthSS("demo", "demopass", null); 
		
		assertEquals(basicAuth.getID(), "demo");
		assertFalse(basicAuth.isDenied());
		assertEquals(basicAuth.count(), 0);
		assertEquals(basicAuth.setLastResponse(401), 1);
	}

	@Test
	public void testHBasicAuthSSStringStringSecurityInfoCOfHttpURLConnectionTwo() throws IOException {
		HBasicAuthSS basicAuth = new HBasicAuthSS("user 1", "user 2", null); 
		
		assertEquals(basicAuth.getID(), "user 1");
		assertFalse(basicAuth.isDenied());
		assertEquals(basicAuth.count(), 0);
		assertEquals(basicAuth.setLastResponse(401), 1);
	}
	
	@Test
	public void testHBasicAuthSSStringStringSecurityInfoCOfHttpURLConnectionThree() throws IOException {
		HBasicAuthSS basicAuth = new HBasicAuthSS("onap", "onap", null); 
		
		assertEquals(basicAuth.getID(), "onap");
		assertFalse(basicAuth.isDenied());
		assertEquals(basicAuth.count(), 0);
		assertEquals(basicAuth.setLastResponse(401), 1);
	}
}
