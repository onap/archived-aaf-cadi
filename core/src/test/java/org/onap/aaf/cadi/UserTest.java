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


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.lur.LocalPermission;

public class UserTest {

	@Mock
	private Principal principal;
	
	@Mock
	private LocalPermission permission;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		when(principal.getName()).thenReturn("Principal");
		
		when(permission.getKey()).thenReturn("NewKey");
		when(permission.match(permission)).thenReturn(true);
	}
	
	@Test
	public void testCountCheck() {
		User<Permission> user = new User<Permission>(principal);
		user.resetCount();
		assertThat(user.count, is(0));
		user.incCount();
		assertThat(user.count, is(1));
	}
	
	@Test
	public void testCountCheck1() {
		User<Permission> user = new User<Permission>(principal);
		user.resetCount();
		assertThat(user.count, is(0));
		user.incCount();
		assertThat(user.count, is(1));
	}

	@Test
	public void testPerm() throws InterruptedException {
		User<Permission> user = new User<Permission>(principal);
		assertThat(user.permExpires(), is(Long.MAX_VALUE));
		user.renewPerm();
		Thread.sleep(1);
		assertThat(user.permExpired(), is(true));
		user = new User<Permission>(principal,100);
		assertTrue(user.noPerms());
		user.add(permission);
		assertFalse(user.noPerms());
		user.setNoPerms();
		assertThat(user.permExpired(), is(false));
		assertFalse(user.permsUnloaded());
		user.perms = null;
		assertTrue(user.permsUnloaded());
		assertTrue(user.noPerms());
	}
	
	@Test
	public void testPerm1() throws InterruptedException {
		User<Permission> user = new User<Permission>(principal);
		assertThat(user.permExpires(), is(Long.MAX_VALUE));
		user.renewPerm();
		Thread.sleep(1);
		assertThat(user.permExpired(), is(true));
		user = new User<Permission>(principal,100);
		assertTrue(user.noPerms());
		user.add(permission);
		assertFalse(user.noPerms());
		user.setNoPerms();
		assertThat(user.permExpired(), is(false));
		assertFalse(user.permsUnloaded());
		user.perms = null;
		assertTrue(user.permsUnloaded());
		assertTrue(user.noPerms());
	}
	
	@Test
	public void testAddValuesToNewMap() {
		User<Permission> user = new User<Permission>(principal);
		Map<String, Permission> newMap = new HashMap<String,Permission>();
		
		assertFalse(user.contains(permission));
		
		user.add(newMap, permission);
		user.setMap(newMap);
		
		assertTrue(user.contains(permission));
		
		List<Permission> sink = new ArrayList<Permission>();
		user.copyPermsTo(sink);
		
		assertThat(sink.size(), is(1));
		assertTrue(sink.contains(permission));
		
		assertThat(user.toString(), is("Principal|:NewKey"));
	}
	
	@Test
	public void testAddValuesToNewMap1() {
		User<Permission> user = new User<Permission>(principal);
		Map<String, Permission> newMap = new HashMap<String,Permission>();
		
		assertFalse(user.contains(permission));
		
		user.add(newMap, permission);
		user.setMap(newMap);
		
		assertTrue(user.contains(permission));
		
		List<Permission> sink = new ArrayList<Permission>();
		user.copyPermsTo(sink);
		
		assertThat(sink.size(), is(1));
		assertTrue(sink.contains(permission));
		
		assertThat(user.toString(), is("Principal|:NewKey"));
	}
}
