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

import org.apache.cassandra.exceptions.AuthenticationException;
import org.junit.Test;
import org.onap.aaf.cadi.aaf.cass.AAFAuthenticator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class JU_AAFAuthenticatorTest
{
	

	@Before
	public void setUp()
	{
		
	}

	@After
	public void tearDown()
	{
		
	}

	/*
	 * Testing Conditon(s): Default
	 */
	@Test
	public void test_method_requireAuthentication_0_branch_0()
	{
		System.out.println("Now Testing Method:requireAuthentication Branch:0");
		
		//Constructor
		AAFAuthenticator instance = new AAFAuthenticator();
		
		//Get expected result and result
		Object expResult = null;
		Object result = instance.requireAuthentication();
		
		//Check Return value
		assertEquals(expResult, result);
		
		//Check Test Verification Points
		assertEquals(null, instance.requireAuthentication());
		
	}

	/*
	 * Testing Conditon(s): if: (username == null)
	 */
	@Test
	public void test_method_authenticate_1_branch_0()
	{
		System.out.println("Now Testing Method:authenticate Branch:0");
		
	
		
	}

	/*
	 * Testing Conditon(s): else: Not (username == null)
	 */
	@Test
	public void test_method_authenticate_1_branch_1() throws AuthenticationException
	{
		System.out.println("Now Testing Method:authenticate Branch:1");
		
		//Constructor
		AAFAuthenticator instance = new AAFAuthenticator();
		
		//Get expected result and result
		Object expResult = null;
		Object result = instance.authenticate(null);
		
		//Check Return value
		assertEquals(expResult, result);
		
	
		
	}

	/*
	 * Testing Conditon(s): if: (password == null)
	 */
	@Test
	public void test_method_authenticate_1_branch_2()
	{
		System.out.println("Now Testing Method:authenticate Branch:2");
		
		
		
	}

	/*
	 * Testing Conditon(s): else: Not (password == null), if: (password.startsWith("bsf:"))
	 */
	@Test
	public void test_method_authenticate_1_branch_3()
	{
		System.out.println("Now Testing Method:authenticate Branch:3");
		
		
		
	}

	/*
	 * Testing Conditon(s): else: Not (password == null), else: Not (password.startsWith("bsf:")), if: (password.startsWith("enc:???"))
	 */
	@Test
	public void test_method_authenticate_1_branch_4()
	{
		System.out.println("Now Testing Method:authenticate Branch:4");
	
	}

	/*
	 * Testing Conditon(s): else: Not (password == null), else: Not (password.startsWith("bsf:")), else: Not (password.startsWith("enc:???"))
	 */
	@Test
	public void test_method_authenticate_1_branch_5()
	{
		System.out.println("Now Testing Method:authenticate Branch:5");
		
		
		
	}

	/*
	 * Testing Conditon(s): if: (localLur!=null), if: (localLur.validate(fullName, Type.PASSWORD, password.getBytes()))
	 */
	@Test
	public void test_method_authenticate_1_branch_6()
	{
		System.out.println("Now Testing Method:authenticate Branch:6");
		
		
		
	}

	/*
	 * Testing Conditon(s): if: (localLur!=null), else: Not (localLur.validate(fullName, Type.PASSWORD, password.getBytes()))
	 */
	@Test
	public void test_method_authenticate_1_branch_7()
	{
		System.out.println("Now Testing Method:authenticate Branch:7");
		
		
	}

	/*
	 * Testing Conditon(s): else: Not (localLur!=null)
	 */
	@Test
	public void test_method_authenticate_1_branch_8()
	{
		System.out.println("Now Testing Method:authenticate Branch:8");
		
		
		
	}

	/*
	 * Testing Conditon(s): if: (aafResponse != null)
	 */
	@Test
	public void test_method_authenticate_1_branch_9()
	{
		System.out.println("Now Testing Method:authenticate Branch:9");
		
	
	}

	/*
	 * Testing Conditon(s): else: Not (aafResponse != null)
	 */
	@Test
	public void test_method_authenticate_1_branch_10()
	{
		System.out.println("Now Testing Method:authenticate Branch:10");
		
		
		
	}

	/*
	 * Testing Conditon(s): Default
	 */
	@Test
	public void test_method_create_2_branch_0()
	{
		System.out.println("Now Testing Method:create Branch:0");
		
		
		
	}

	/*
	 * Testing Conditon(s): Default
	 */
	@Test
	public void test_method_alter_3_branch_0()
	{
		System.out.println("Now Testing Method:alter Branch:0");
		
		
	}

	/*
	 * Testing Conditon(s): Default
	 */
	@Test
	public void test_method_drop_4_branch_0()
	{
		System.out.println("Now Testing Method:drop Branch:0");
		
		
	}

	/*
	 * Testing Conditon(s): Default
	 */
	@Test
	public void test_method_evaluateResponse_5_branch_0()
	{
		System.out.println("Now Testing Method:evaluateResponse Branch:0");
		
		
	}

	/*
	 * Testing Conditon(s): Default
	 */
	@Test
	public void test_method_isComplete_6_branch_0()
	{
		System.out.println("Now Testing Method:isComplete Branch:0");
		
	
	}

	/*
	 * Testing Conditon(s): Default
	 */
	@Test
	public void test_method_getAuthenticatedUser_7_branch_0()
	{
		System.out.println("Now Testing Method:getAuthenticatedUser Branch:0");
		
		
		
	}

	/*
	 * Testing Conditon(s): Default
	 */
	@Test
	public void test_method_newAuthenticator_8_branch_0()
	{
		System.out.println("Now Testing Method:newAuthenticator Branch:0");
		
		
		
	}

}
