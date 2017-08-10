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
package com.att.cadi.lur.aaf.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import com.att.cadi.aaf.PermEval;

public class JU_PermEval {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		assertTrue(PermEval.evalInstance(":com.att.temp:role:write",":!com.att.*:role:write"));
		
		// TRUE
		assertTrue(PermEval.evalAction("fred","fred"));
		assertTrue(PermEval.evalAction("fred,wilma","fred"));
		assertTrue(PermEval.evalAction("barney,betty,fred,wilma","fred"));
		assertTrue(PermEval.evalAction("*","fred"));
		
		assertTrue(PermEval.evalInstance("fred","fred"));
		assertTrue(PermEval.evalInstance("fred,wilma","fred"));
		assertTrue(PermEval.evalInstance("barney,betty,fred,wilma","fred"));
		assertTrue(PermEval.evalInstance("*","fred"));
		
		assertTrue(PermEval.evalInstance(":fred:fred",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:fred,wilma",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:barney,betty,fred,wilma",":fred:fred"));
		assertTrue(PermEval.evalInstance("*","fred"));
		assertTrue(PermEval.evalInstance(":*:fred",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:*",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:fred",":!f.*:fred"));
		assertTrue(PermEval.evalInstance(":fred:fred",":fred:!f.*"));
		
		/// FALSE
		assertFalse(PermEval.evalInstance("fred","wilma"));
		assertFalse(PermEval.evalInstance("fred,barney,betty","wilma"));
		assertFalse(PermEval.evalInstance(":fred:fred",":fred:wilma"));
		assertFalse(PermEval.evalInstance(":fred:fred",":wilma:fred"));
		assertFalse(PermEval.evalInstance(":fred:fred",":wilma:!f.*"));
		assertFalse(PermEval.evalInstance(":fred:fred",":!f.*:wilma"));
		assertFalse(PermEval.evalInstance(":fred:fred",":!w.*:!f.*"));
		assertFalse(PermEval.evalInstance(":fred:fred",":!f.*:!w.*"));

		assertFalse(PermEval.evalInstance(":fred:fred",":fred:!x.*"));

		// MSO Tests 12/3/2015
		assertFalse(PermEval.evalInstance("/v1/services/features/*","/v1/services/features"));
		assertFalse(PermEval.evalInstance(":v1:services:features:*",":v1:services:features"));
		assertTrue(PermEval.evalInstance("/v1/services/features/*","/v1/services/features/api1"));
		assertTrue(PermEval.evalInstance(":v1:services:features:*",":v1:services:features:api2"));
		// MSO - Xue Gao
		assertTrue(PermEval.evalInstance(":v1:requests:*",":v1:requests:test0-service"));   


		
		// Same tests, with Slashes
		assertTrue(PermEval.evalInstance("/fred/fred","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/fred,wilma","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/barney,betty,fred,wilma","/fred/fred"));
		assertTrue(PermEval.evalInstance("*","fred"));
		assertTrue(PermEval.evalInstance("/*/fred","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/*","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/fred","/!f.*/fred"));
		assertTrue(PermEval.evalInstance("/fred/fred","/fred/!f.*"));
		
		/// FALSE
		assertFalse(PermEval.evalInstance("fred","wilma"));
		assertFalse(PermEval.evalInstance("fred,barney,betty","wilma"));
		assertFalse(PermEval.evalInstance("/fred/fred","/fred/wilma"));
		assertFalse(PermEval.evalInstance("/fred/fred","/wilma/fred"));
		assertFalse(PermEval.evalInstance("/fred/fred","/wilma/!f.*"));
		assertFalse(PermEval.evalInstance("/fred/fred","/!f.*/wilma"));
		assertFalse(PermEval.evalInstance("/fred/fred","/!w.*/!f.*"));
		assertFalse(PermEval.evalInstance("/fred/fred","/!f.*/!w.*"));

		assertFalse(PermEval.evalInstance("/fred/fred","/fred/!x.*"));
		
	}

}
