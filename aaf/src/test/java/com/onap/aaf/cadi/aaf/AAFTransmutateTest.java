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
package com.onap.aaf.cadi.aaf;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.aaf.AAFTransmutate;
import org.onap.aaf.cadi.lur.ConfigPrincipal;
import org.onap.aaf.cadi.principal.BasicPrincipal;

public class AAFTransmutateTest {
	
	@Mock
	Principal principal ;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(principal.getName()).thenReturn("Value");
	}

	@Test
	public void testMutate() throws IOException {
		BasicPrincipal p = new BasicPrincipal("content", "domain");
		AAFTransmutate transmutate = new AAFTransmutate();
//		assertNotNull(transmutate.mutate(p));
		
		ConfigPrincipal cp = new ConfigPrincipal("content", "cred");
//		assertNotNull(transmutate.mutate(cp));
		
		assertNull(transmutate.mutate(principal));
	}

	
	@Test
	public void testMutate4() throws IOException {
		BasicPrincipal p = new BasicPrincipal("content", "domain");
		AAFTransmutate transmutate = new AAFTransmutate();
//		assertNotNull(transmutate.mutate(p));
		
		ConfigPrincipal cp = new ConfigPrincipal("content", "cred");
//		assertNotNull(transmutate.mutate(cp));
		
		assertNull(transmutate.mutate(principal));
	}

	
	@Test
	public void testMutate1() throws IOException {
		BasicPrincipal p = new BasicPrincipal("content", "domain");
		AAFTransmutate transmutate = new AAFTransmutate();
//		assertNotNull(transmutate.mutate(p));
		
		ConfigPrincipal cp = new ConfigPrincipal("content", "cred");
//		assertNotNull(transmutate.mutate(cp));
		
		assertNull(transmutate.mutate(principal));
	}

	
	@Test
	public void testMutate2() throws IOException {
		BasicPrincipal p = new BasicPrincipal("content", "domain");
		AAFTransmutate transmutate = new AAFTransmutate();
//		assertNotNull(transmutate.mutate(p));
		
		ConfigPrincipal cp = new ConfigPrincipal("content", "cred");
//		assertNotNull(transmutate.mutate(cp));
		
		assertNull(transmutate.mutate(principal));
	}

	
	@Test
	public void testMutate3() throws IOException {
		BasicPrincipal p = new BasicPrincipal("content", "domain");
		AAFTransmutate transmutate = new AAFTransmutate();
//		assertNotNull(transmutate.mutate(p));
		
		ConfigPrincipal cp = new ConfigPrincipal("content", "cred");
//		assertNotNull(transmutate.mutate(cp));
		
		assertNull(transmutate.mutate(principal));
	}

}
