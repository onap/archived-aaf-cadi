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

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.cadi.cm.CertException;

public class JU_CertExceptionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		CertException ex = new CertException();
		assertNotNull(ex);
		
		ex = new CertException("Exception Message");
		assertNotNull(ex);
		assertEquals(ex.getMessage(),"Exception Message");
		
		ex = new CertException(new Throwable());
		assertNotNull(ex);
		assertNotNull(ex.getCause());
		
		ex = new CertException("Exception Message1", new Throwable());
		assertNotNull(ex.getCause());
		assertEquals(ex.getMessage(),"Exception Message1");
	}

	@Test
	public void test3() {
		CertException ex = new CertException();
		assertNotNull(ex);
		
		ex = new CertException("Exception Message");
		assertNotNull(ex);
		assertEquals(ex.getMessage(),"Exception Message");
		
		ex = new CertException(new Throwable());
		assertNotNull(ex);
		assertNotNull(ex.getCause());
		
		ex = new CertException("Exception Message1", new Throwable());
		assertNotNull(ex.getCause());
		assertEquals(ex.getMessage(),"Exception Message1");
	}

	
	@Test
	public void test1() {
		CertException ex = new CertException();
		assertNotNull(ex);
		
		ex = new CertException("Exception Message");
		assertNotNull(ex);
		assertEquals(ex.getMessage(),"Exception Message");
		
		ex = new CertException(new Throwable());
		assertNotNull(ex);
		assertNotNull(ex.getCause());
		
		ex = new CertException("Exception Message1", new Throwable());
		assertNotNull(ex.getCause());
		assertEquals(ex.getMessage(),"Exception Message1");
	}

	
	@Test
	public void test2() {
		CertException ex = new CertException();
		assertNotNull(ex);
		
		ex = new CertException("Exception Message");
		assertNotNull(ex);
		assertEquals(ex.getMessage(),"Exception Message");
		
		ex = new CertException(new Throwable());
		assertNotNull(ex);
		assertNotNull(ex.getCause());
		
		ex = new CertException("Exception Message1", new Throwable());
		assertNotNull(ex.getCause());
		assertEquals(ex.getMessage(),"Exception Message1");
	}
	@Test
	public void test4() {
		CertException ex = new CertException();
		assertNotNull(ex);
		
		ex = new CertException("Exception Message");
		assertNotNull(ex);
		assertEquals(ex.getMessage(),"Exception Message");
		
		ex = new CertException(new Throwable());
		assertNotNull(ex);
		assertNotNull(ex.getCause());
		
		ex = new CertException("Exception Message1", new Throwable());
		assertNotNull(ex.getCause());
		assertEquals(ex.getMessage(),"Exception Message1");
	}

	@Test
	public void test5() {
		CertException ex = new CertException();
		assertNotNull(ex);
		
		ex = new CertException("Exception Message");
		assertNotNull(ex);
		assertEquals(ex.getMessage(),"Exception Message");
		
		ex = new CertException(new Throwable());
		assertNotNull(ex);
		assertNotNull(ex.getCause());
		
		ex = new CertException("Exception Message1", new Throwable());
		assertNotNull(ex.getCause());
		assertEquals(ex.getMessage(),"Exception Message1");
	}

	@Test
	public void test6() {
		CertException ex = new CertException();
		assertNotNull(ex);
		
		ex = new CertException("Exception Message");
		assertNotNull(ex);
		assertEquals(ex.getMessage(),"Exception Message");
		
		ex = new CertException(new Throwable());
		assertNotNull(ex);
		assertNotNull(ex.getCause());
		
		ex = new CertException("Exception Message1", new Throwable());
		assertNotNull(ex.getCause());
		assertEquals(ex.getMessage(),"Exception Message1");
	}

	@Test
	public void test7() {
		CertException ex = new CertException();
		assertNotNull(ex);
		
		ex = new CertException("Exception Message");
		assertNotNull(ex);
		assertEquals(ex.getMessage(),"Exception Message");
		
		ex = new CertException(new Throwable());
		assertNotNull(ex);
		assertNotNull(ex.getCause());
		
		ex = new CertException("Exception Message1", new Throwable());
		assertNotNull(ex.getCause());
		assertEquals(ex.getMessage(),"Exception Message1");
	}


	@Test
	public void test8() {
		CertException ex = new CertException();
		assertNotNull(ex);
		
		ex = new CertException("Exception Message");
		assertNotNull(ex);
		assertEquals(ex.getMessage(),"Exception Message");
		
		ex = new CertException(new Throwable());
		assertNotNull(ex);
		assertNotNull(ex.getCause());
		
		ex = new CertException("Exception Message1", new Throwable());
		assertNotNull(ex.getCause());
		assertEquals(ex.getMessage(),"Exception Message1");
	}

	@Test
	public void test9() {
		CertException ex = new CertException();
		assertNotNull(ex);
		
		ex = new CertException("Exception Message");
		assertNotNull(ex);
		assertEquals(ex.getMessage(),"Exception Message");
		
		ex = new CertException(new Throwable());
		assertNotNull(ex);
		assertNotNull(ex.getCause());
		
		ex = new CertException("Exception Message1", new Throwable());
		assertNotNull(ex.getCause());
		assertEquals(ex.getMessage(),"Exception Message1");
	}

	@Test
	public void test10() {
		CertException ex = new CertException();
		assertNotNull(ex);
		
		ex = new CertException("Exception Message");
		assertNotNull(ex);
		assertEquals(ex.getMessage(),"Exception Message");
		
		ex = new CertException(new Throwable());
		assertNotNull(ex);
		assertNotNull(ex.getCause());
		
		ex = new CertException("Exception Message1", new Throwable());
		assertNotNull(ex.getCause());
		assertEquals(ex.getMessage(),"Exception Message1");
	}

}