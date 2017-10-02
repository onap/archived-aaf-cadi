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



import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.lur.LocalPermission;

public class JU_AAFPermissionTest {

	private static final String INSTANCE = "*";
	private static final String ACTION = "*";
	private static final String TYPE = "Auth";
	private static final String KEY =TYPE + '|' + INSTANCE + '|' + ACTION;
	
	private String STRINGVALUE =
	"AAFPermission:\n\tType: " + TYPE + 
	"\n\tInstance: " + INSTANCE +
	"\n\tAction: " + ACTION +
	"\n\tKey: " + KEY;

	@Mock
	private LocalPermission localPermission;
	
	@Mock
	private LocalPermission localPermission2;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(localPermission.getKey()).thenReturn(TYPE);
		when(localPermission2.getKey()).thenReturn(TYPE+" Default");
	}
	
	@Test
	public void test() {
		AAFPermission permission = new AAFPermission(TYPE, INSTANCE, ACTION);
		
		assertTrue("This should Match",permission.match(permission));
		
		assertTrue("This should Match", permission.match(localPermission));
		
		assertFalse("This should Not Match", permission.match(localPermission2));
		
		assertThat(permission.getKey(), is(KEY));
		
		assertThat(permission.permType(), is("AAF"));
		
		assertThat(permission.toString(), is(STRINGVALUE));
		
	}
	
	@Test
	public void test4() {
		AAFPermission permission = new AAFPermission(TYPE, INSTANCE, ACTION);
		
		assertTrue("This should Match",permission.match(permission));
		
		assertTrue("This should Match", permission.match(localPermission));
		
		assertFalse("This should Not Match", permission.match(localPermission2));
		
		assertThat(permission.getKey(), is(KEY));
		
		assertThat(permission.permType(), is("AAF"));
		
		assertThat(permission.toString(), is(STRINGVALUE));
		
	}
	
	@Test
	public void test1() {
		AAFPermission permission = new AAFPermission(TYPE, INSTANCE, ACTION);
		
		assertTrue("This should Match",permission.match(permission));
		
		assertTrue("This should Match", permission.match(localPermission));
		
		assertFalse("This should Not Match", permission.match(localPermission2));
		
		assertThat(permission.getKey(), is(KEY));
		
		assertThat(permission.permType(), is("AAF"));
		
		assertThat(permission.toString(), is(STRINGVALUE));
		
	}
	
	@Test
	public void test2() {
		AAFPermission permission = new AAFPermission(TYPE, INSTANCE, ACTION);
		
		assertTrue("This should Match",permission.match(permission));
		
		assertTrue("This should Match", permission.match(localPermission));
		
		assertFalse("This should Not Match", permission.match(localPermission2));
		
		assertThat(permission.getKey(), is(KEY));
		
		assertThat(permission.permType(), is("AAF"));
		
		assertThat(permission.toString(), is(STRINGVALUE));
		
	}

	
	@Test
	public void test3() {
		AAFPermission permission = new AAFPermission(TYPE, INSTANCE, ACTION);
		
		assertTrue("This should Match",permission.match(permission));
		
		assertTrue("This should Match", permission.match(localPermission));
		
		assertFalse("This should Not Match", permission.match(localPermission2));
		
		assertThat(permission.getKey(), is(KEY));
		
		assertThat(permission.permType(), is("AAF"));
		
		assertThat(permission.toString(), is(STRINGVALUE));
		
	}
	
	@Test
	public void test5() {
		AAFPermission permission = new AAFPermission(TYPE, INSTANCE, ACTION);
		
		assertTrue("This should Match",permission.match(permission));
		
		assertTrue("This should Match", permission.match(localPermission));
		
		assertFalse("This should Not Match", permission.match(localPermission2));
		
		assertThat(permission.getKey(), is(KEY));
		
		assertThat(permission.permType(), is("AAF"));
		
		assertThat(permission.toString(), is(STRINGVALUE));
		
	}
	
	@Test
	public void test6() {
		AAFPermission permission = new AAFPermission(TYPE, INSTANCE, ACTION);
		
		assertTrue("This should Match",permission.match(permission));
		
		assertTrue("This should Match", permission.match(localPermission));
		
		assertFalse("This should Not Match", permission.match(localPermission2));
		
		assertThat(permission.getKey(), is(KEY));
		
		assertThat(permission.permType(), is("AAF"));
		
		assertThat(permission.toString(), is(STRINGVALUE));
		
	}
	
	@Test
	public void test7() {
		AAFPermission permission = new AAFPermission(TYPE, INSTANCE, ACTION);
		
		assertTrue("This should Match",permission.match(permission));
		
		assertTrue("This should Match", permission.match(localPermission));
		
		assertFalse("This should Not Match", permission.match(localPermission2));
		
		assertThat(permission.getKey(), is(KEY));
		
		assertThat(permission.permType(), is("AAF"));
		
		assertThat(permission.toString(), is(STRINGVALUE));
		
	}
}
