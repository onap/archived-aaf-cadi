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

import static org.junit.Assert.*;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.CachedPrincipal.Resp;
import org.onap.aaf.cadi.filter.MapPermConverter;
import org.onap.aaf.cadi.lur.EpiLur;
import org.onap.aaf.cadi.taf.TafResp;

public class JU_CadiWrapTest {
	
	@Mock
	private HttpServletRequest request;
	
	@Mock
	private TafResp tafResp;
	
	@Mock
	private Principal principle;

	@Mock
	private Lur lur;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testInstantiate() throws CadiException {
		Access a = new PropAccess();
		when(tafResp.getAccess()).thenReturn(a);
		
		lur.fishAll(isA(Principal.class), isA(List.class));
		
		EpiLur lur1 = new EpiLur(lur);
		
		CadiWrap wrap = new CadiWrap(request, tafResp, lur1);
		
		assertNull(wrap.getUserPrincipal());
		assertNull(wrap.getRemoteUser());
		assertNull(wrap.getUser());
		assertEquals(wrap.getPermissions(principle).size(), 0);
		
		byte[] arr = {'1','2'};
		wrap.setCred(arr);
		
		assertEquals(arr, wrap.getCred());
		
		wrap.setUser("User1");
		assertEquals("User1", wrap.getUser());
		
		wrap.invalidate("1");

		assertFalse(wrap.isUserInRole(null));
		
		wrap.set(tafResp, lur);
		
		wrap.invalidate("2");
		
		wrap.isUserInRole("User1");
	}

	@Test
	public void testInstantiateWithPermConverter() throws CadiException {
		Access a = new PropAccess();
		when(tafResp.getAccess()).thenReturn(a);
		when(tafResp.getPrincipal()).thenReturn(principle);
		
		
		CachingLur<Permission> lur1 = new CachingLur<Permission>() {

			@Override
			public Permission createPerm(String p) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean fish(Principal bait, Permission pond) {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public void fishAll(Principal bait, List<Permission> permissions) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void destroy() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean handlesExclusively(Permission pond) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean supports(String userName) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void remove(String user) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Resp reload(User<Permission> user) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setDebug(String commaDelimIDsOrNull) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void clear(Principal p, StringBuilder sb) {
				// TODO Auto-generated method stub
				
			}
		};
		
		MapPermConverter pc = new MapPermConverter();
		
		CadiWrap wrap = new CadiWrap(request, tafResp, lur1, pc);
		
		assertNotNull(wrap.getUserPrincipal());
		assertNull(wrap.getRemoteUser());
		assertNull(wrap.getUser());
		
		byte[] arr = {'1','2'};
		wrap.setCred(arr);
		
		assertEquals(arr, wrap.getCred());
		
		wrap.setUser("User1");
		assertEquals("User1", wrap.getUser());
		
		wrap.invalidate("1");
		wrap.setPermConverter(new MapPermConverter());
		
		assertTrue(wrap.getLur() instanceof CachingLur);
		assertTrue(wrap.isUserInRole("User1"));
		
		wrap.set(tafResp, lur);
		assertFalse(wrap.isUserInRole("Perm1"));
	}
}
