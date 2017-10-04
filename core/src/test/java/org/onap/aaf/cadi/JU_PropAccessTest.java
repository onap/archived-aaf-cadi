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
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.config.Config;

public class JU_PropAccessTest {

	@Test
	public void test() {
		PropAccess prop = new PropAccess(new Object());
		
		prop = new PropAccess("cadi_name=user","cadi_loglevel=DEBUG","cadi_prop_files=conf/cadi.properties");
		
		assertTrue(prop.getProperties().keySet().contains("cadi_name"));
		assertTrue(prop.getProperty("cadi_name").equals("user"));
		
		prop.setProperty("cadi_keyfile", "file");
		prop.setLogLevel(Level.DEBUG);
		assertEquals(prop.getProperty("cadi_keyfile"),"file");
		assertEquals(prop.getDME2Properties().size(),3);
		prop.log(Level.DEBUG);
	}
	
	@Test
	public void testWithProperties() {
		Properties p = new Properties();
		p.put("cadi_name", "user");
		p.put("cadi_loglevel", "DEBUG");
		
		PropAccess prop = new PropAccess(p);
		
		assertTrue(prop.getProperties().keySet().contains("cadi_name"));
		assertTrue(prop.getProperty("cadi_name").equals("user"));
		
		prop.setProperty("cadi_keyfile", "file");
		prop.setLogLevel(Level.DEBUG);
		assertEquals(prop.getProperty("cadi_keyfile"),"file");
		assertEquals(prop.getDME2Properties().size(),3);
		prop.log(Level.DEBUG);
	}
}
