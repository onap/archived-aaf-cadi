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
package org.onap.aaf.client.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.cadi.client.Result;

public class ResultTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testOk() {
		Result<String> t = Result.ok(1, "Ok");
		assertNotNull(t);
		assertThat(t.code, is(1));
		assertTrue(t.isOK());
		assertThat(t.toString(), is("Code: 1"));
	}

	@Test
	public void testErr() {
		Result<String> t = Result.err(1, "Error Body");
		assertNotNull(t);
		assertThat(t.error, is("Error Body"));
		assertFalse(t.isOK());
		assertThat(t.toString(), is("Code: 1 = Error Body"));
	}
	
	@Test
	public void testOk1() {
		Result<String> t = Result.ok(1, "Ok");
		assertNotNull(t);
		assertThat(t.code, is(1));
		assertTrue(t.isOK());
		assertThat(t.toString(), is("Code: 1"));
	}

	@Test
	public void testErr1() {
		Result<String> t = Result.err(1, "Error Body");
		assertNotNull(t);
		assertThat(t.error, is("Error Body"));
		assertFalse(t.isOK());
		assertThat(t.toString(), is("Code: 1 = Error Body"));
	}
	
	@Test
	public void testOk2() {
		Result<String> t = Result.ok(1, "Ok");
		assertNotNull(t);
		assertThat(t.code, is(1));
		assertTrue(t.isOK());
		assertThat(t.toString(), is("Code: 1"));
	}

	@Test
	public void testErr2() {
		Result<String> t = Result.err(1, "Error Body");
		assertNotNull(t);
		assertThat(t.error, is("Error Body"));
		assertFalse(t.isOK());
		assertThat(t.toString(), is("Code: 1 = Error Body"));
	}
	
	@Test
	public void testOk3() {
		Result<String> t = Result.ok(1, "Ok");
		assertNotNull(t);
		assertThat(t.code, is(1));
		assertTrue(t.isOK());
		assertThat(t.toString(), is("Code: 1"));
	}

	@Test
	public void testErr3() {
		Result<String> t = Result.err(1, "Error Body");
		assertNotNull(t);
		assertThat(t.error, is("Error Body"));
		assertFalse(t.isOK());
		assertThat(t.toString(), is("Code: 1 = Error Body"));
	}
}
