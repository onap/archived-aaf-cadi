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
package org.onap.aaf.cadi;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.config.Config;

public class JU_PropAccessTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPropAccess() throws IOException {
		PropAccess p = new PropAccess(new Object());
		
		assertNotNull(p);
		assertNotNull(p.getProperties());
		assertNull(p.getProperty("anything"));
		
		p.setProperty("prop", "value");
		assertEquals(p.getProperty("prop"), "value");
		
		p.setProperty(Config.CADI_KEYFILE, "value");
		assertEquals(p.getProperty("prop"), "value");
		
		p.setLogLevel(Level.INFO);
		assertTrue(p.willLog(Level.INFO));
		p.log(Level.DEBUG, new Object());
		String[] args = {"key=value","wow=wow"};
		p = new PropAccess(args);
	}
	
	@Test
	public void testPropAccessone() throws IOException {
		PropAccess p = new PropAccess(new Object());
		
		assertNotNull(p);
		assertNotNull(p.getProperties());
		assertNull(p.getProperty("everything"));
		
		p.setProperty("prop1", "value1");
		assertEquals(p.getProperty("prop1"), "value1");
		
		p.setProperty(Config.CADI_KEYFILE, "value1");
		assertEquals(p.getProperty("prop1"), "value1");
		
		p.setLogLevel(Level.INFO);
		assertTrue(p.willLog(Level.INFO));
		p.log(Level.DEBUG, new Object());
		String[] args = {"key=value1","wow=wow1"};
		p = new PropAccess(args);
	}

}

