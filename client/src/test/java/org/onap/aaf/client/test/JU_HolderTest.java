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

import static org.junit.Assert.*;

import org.junit.Test;
import org.onap.aaf.cadi.client.Holder;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class JU_HolderTest {

	@Test
	public void testSet() {
		Holder<String> holder = new Holder<String>("StringHolder");
		assertEquals(holder.get(), "StringHolder");
		
		holder.set("New String");
		assertEquals(holder.get(), "New String");
	}

	@Test
	public void testSet4() {
		Holder<String> holder = new Holder<String>("StringHolder");
		assertEquals(holder.get(), "StringHolder");
		
		holder.set("New String1");
		assertEquals(holder.get(), "New String1");
	}
	@Test
	public void testSet1() {
		Holder<String> holder = new Holder<String>("StringHolder");
		assertEquals(holder.get(), "StringHolder");
		
		holder.set("New String2");
		assertEquals(holder.get(), "New String2");
	}
	
	@Test
	public void testSet2() {
		Holder<String> holder = new Holder<String>("StringHolder");
		assertEquals(holder.get(), "StringHolder");
		
		holder.set("New String3");
		assertEquals(holder.get(), "New String3");
	}
	
	@Test
	public void testSet3() {
		Holder<String> holder = new Holder<String>("StringHolder");
		assertEquals(holder.get(), "StringHolder");
		
		holder.set("New String4");
		assertEquals(holder.get(), "New String4");
	}
}
