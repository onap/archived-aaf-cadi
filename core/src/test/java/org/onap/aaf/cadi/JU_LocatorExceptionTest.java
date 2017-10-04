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


import org.junit.Before;


public class JU_LocatorExceptionTest {

	@Test
	public void testLocatorExceptionString() {
		LocatorException exception = new LocatorException("New Exception");
		assertNotNull(exception);
		assertThat(exception.getMessage(), is("New Exception"));
	}

	@Test
	public void testLocatorExceptionThrowable() {
		LocatorException exception = new LocatorException(new Throwable("New Exception"));
		assertNotNull(exception);
		assertThat(exception.getMessage(), is("java.lang.Throwable: New Exception"));
	}

	@Test
	public void testLocatorExceptionStringThrowable() {
		LocatorException exception = new LocatorException("New Exception",new Throwable("New Exception"));
		assertNotNull(exception);
		assertThat(exception.getMessage(), is("New Exception"));
	}

	@Test
	public void testLocatorExceptionCharSequence() {
		CharSequence s ="New Exception";
		LocatorException exception = new LocatorException(s);
		assertNotNull(exception);
		assertThat(exception.getMessage(), is("New Exception"));
	}
	
	@Test
	public void testLocatorExceptionCharSequence1() {
		CharSequence s ="New Exception 1";
		LocatorException exception = new LocatorException(s);
		assertNotNull(exception);
		assertThat(exception.getMessage(), is("New Exception 1"));
	}
	
	@Test
	public void testLocatorExceptionCharSequence2() {
		CharSequence s ="New Exception 2";
		LocatorException exception = new LocatorException(s);
		assertNotNull(exception);
		assertThat(exception.getMessage(), is("New Exception 2"));
	}
}
