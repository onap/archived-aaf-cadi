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
package org.onap.aaf.cadi.filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.PropAccess;

public class JU_FCGetTest {

	@Mock
	private ServletContext context;
	
	@Mock
	private FilterConfig config;

	@Mock
	private PropAccess access = new PropAccess();
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGetStringFromDef() {
		PropAccess access = new PropAccess();
		
		FCGet fcGet = new FCGet(access, context, config);

		String user = fcGet.get("user", "DefaultUser", true);
		
		assertEquals(user, "DefaultUser");
	}

	@Test
	public void testGetStringFromContext() {
		PropAccess access = new PropAccess();
		when(context.getInitParameter("user")).thenReturn("ContextUser");
		
		FCGet fcGet = new FCGet(access, context, null);

		String user = fcGet.get("user", "DefaultUser", true);
		
		assertEquals(user,"ContextUser");
	}
	
	@Test
	public void testGetStringFromFilter() {
		PropAccess access = new PropAccess();
		when(config.getInitParameter("user")).thenReturn("FilterUser");
		
		FCGet fcGet = new FCGet(access, null, config);

		String user = fcGet.get("user", "DefaultUser", true);
		
		assertEquals(user,"FilterUser");
	}
	
	@Test
	public void testGetStringWithNullContextFilter() {
		
		when(access.getProperty("user", "DefaultUser")).thenReturn(null);
		
		FCGet fcGet = new FCGet(access, null, null);

		String user = fcGet.get("user", "DefaultUser", true);
		
		assertEquals(user,"DefaultUser");
	}
}
