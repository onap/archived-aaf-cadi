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
package org.onap.aaf.cass;

import static org.junit.Assert.*;

import org.apache.cassandra.auth.AuthenticatedUser;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.cadi.aaf.cass.AAFAuthenticatedUser;

public class JU_AAFAuthenticatedUserTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		AAFAuthenticatedUser user = new AAFAuthenticatedUser(null, "User1@aaf");
		assertEquals(user.getFullName(),"User1@aaf");
		assertEquals(user.getName(),"User1@aaf");
		assertFalse(user.isAnonymous());
		assertFalse(user.isSuper());
		assertFalse(user.isLocal());
		
		
		
	}
	
	@Test
	public void testone() {
		AAFAuthenticatedUser user = new AAFAuthenticatedUser(null, "User2@aaf");
		assertEquals(user.getFullName(),"User2@aaf");
		assertEquals(user.getName(),"User2@aaf");
		assertFalse(user.isAnonymous());
		assertFalse(user.isSuper());
		assertFalse(user.isLocal());
		
		
		
	}

	@Test
	public void testtwo() {
		AAFAuthenticatedUser user = new AAFAuthenticatedUser(null, "onap@aaf");
		assertEquals(user.getFullName(),"onap@aaf");
		assertEquals(user.getName(),"onap@aaf");
		assertFalse(user.isAnonymous());
		assertFalse(user.isSuper());
		assertFalse(user.isLocal());
		
		
		
	}
	
	@Test
	public void testthree() {
		AAFAuthenticatedUser user = new AAFAuthenticatedUser(null, "openecomp@aaf");
		assertEquals(user.getFullName(),"openecomp@aaf");
		assertEquals(user.getName(),"openecomp@aaf");
		assertFalse(user.isAnonymous());
		assertFalse(user.isSuper());
		assertFalse(user.isLocal());
		
		
		
	}

}
