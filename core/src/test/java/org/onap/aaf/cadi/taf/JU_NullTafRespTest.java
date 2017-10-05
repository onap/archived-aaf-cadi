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
package org.onap.aaf.cadi.taf;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.taf.TafResp.RESP;

public class JU_NullTafRespTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws IOException {
		TafResp singleton = NullTafResp.singleton();
		TafResp singleton1 = NullTafResp.singleton();
		
		assertEquals(singleton, singleton1);
		
		assertFalse(singleton.isValid());
		
		assertEquals(singleton.isAuthenticated(), RESP.NO_FURTHER_PROCESSING);
		
		assertEquals(singleton.desc(), "All Authentication denied");
		
		assertEquals(singleton.authenticate(), RESP.NO_FURTHER_PROCESSING);
		
		assertNull(singleton.getPrincipal());
		
		assertEquals(singleton.getAccess(), Access.NULL);
		
		assertEquals(singleton.isFailedAttempt(), true);
	}

}
