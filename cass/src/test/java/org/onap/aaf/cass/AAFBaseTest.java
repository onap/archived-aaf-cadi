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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.cadi.aaf.cass.AAFBase;

import static org.junit.Assert.*;

public class AAFBaseTest
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
	public void test_method_setAccess_0_branch_0()
	{
		System.out.println("Now Testing Method:setAccess Branch:0");
		
		//Call Method
		AAFBase.setAccess(null);
		
	}

	/*
	 * Testing Conditon(s): if: (!props_ok)
	 */
	@Test
	public void test_method_validateConfiguration_1_branch_0()
	{
		System.out.println("Now Testing Method:validateConfiguration Branch:0");
		
		
		
	}

	/*
	 * Testing Conditon(s): else: Not (!props_ok)
	 */
	@Test
	public void test_method_validateConfiguration_1_branch_1()
	{
		System.out.println("Now Testing Method:validateConfiguration Branch:1");
		
	
		
		//Call Method
	
		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: (access==null), if: (cadi_props == null), if: (cp.exists())
	 */
	@Test
	public void test_method_setup_2_branch_0()
	{
		System.out.println("Now Testing Method:setup Branch:0");
		

		
	
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: (access==null), if: (cadi_props == null), else: Not (cp.exists())
	 */
	@Test
	public void test_method_setup_2_branch_1()
	{
		System.out.println("Now Testing Method:setup Branch:1");
		

		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: (access==null), else: Not (cadi_props == null)
	 */
	@Test
	public void test_method_setup_2_branch_2()
	{
		System.out.println("Now Testing Method:setup Branch:2");
		

	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), else: Not (access==null)
	 */
	@Test
	public void test_method_setup_2_branch_3()
	{
		System.out.println("Now Testing Method:setup Branch:3");
		

	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: ((perm_type = Config.logProp(access, "cass_group_name",null))==null)
	 */
	@Test
	public void test_method_setup_2_branch_4()
	{
		System.out.println("Now Testing Method:setup Branch:4");
		

		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), else: Not ((perm_type = Config.logProp(access, "cass_group_name",null))==null)
	 */
	@Test
	public void test_method_setup_2_branch_5()
	{
		System.out.println("Now Testing Method:setup Branch:5");
		

	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: ((cluster_name = Config.logProp(access,"cass_cluster_name",null))==null), if: ((cluster_name = DatabaseDescriptor.getClusterName())==null)
	 */
	@Test
	public void test_method_setup_2_branch_6()
	{
		System.out.println("Now Testing Method:setup Branch:6");
		

		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: ((cluster_name = Config.logProp(access,"cass_cluster_name",null))==null), else: Not ((cluster_name = DatabaseDescriptor.getClusterName())==null)
	 */
	@Test
	public void test_method_setup_2_branch_7()
	{
		System.out.println("Now Testing Method:setup Branch:7");
		

		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), else: Not ((cluster_name = Config.logProp(access,"cass_cluster_name",null))==null)
	 */
	@Test
	public void test_method_setup_2_branch_8()
	{
		System.out.println("Now Testing Method:setup Branch:8");
		

		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: ((default_realm = Config.logProp(access, Config.AAF_DEFAULT_REALM, null))==null)
	 */
	@Test
	public void test_method_setup_2_branch_9()
	{
		System.out.println("Now Testing Method:setup Branch:9");
		

	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), else: Not ((default_realm = Config.logProp(access, Config.AAF_DEFAULT_REALM, null))==null)
	 */
	@Test
	public void test_method_setup_2_branch_10()
	{
		System.out.println("Now Testing Method:setup Branch:10");

		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: (props_ok==false)
	 */
	@Test
	public void test_method_setup_2_branch_11()
	{
		System.out.println("Now Testing Method:setup Branch:11");

		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), else: Not (props_ok==false)
	 */
	@Test
	public void test_method_setup_2_branch_12()
	{
		System.out.println("Now Testing Method:setup Branch:12");
		
		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: (lur instanceof EpiLur), for: (int i=0; (lur = elur.get(i))!=null;++i), if: (lur instanceof AbsAAFLur)
	 */
	@Test
	public void test_method_setup_2_branch_13()
	{
		System.out.println("Now Testing Method:setup Branch:13");
		

		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: (lur instanceof EpiLur), for: (int i=0; (lur = elur.get(i))!=null;++i), else: Not (lur instanceof AbsAAFLur), if: (lur instanceof LocalLur)
	 */
	@Test
	public void test_method_setup_2_branch_14()
	{
		System.out.println("Now Testing Method:setup Branch:14");
		

	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: (lur instanceof EpiLur), for: (int i=0; (lur = elur.get(i))!=null;++i), else: Not (lur instanceof AbsAAFLur), else: Not (lur instanceof LocalLur)
	 */
	@Test
	public void test_method_setup_2_branch_15()
	{
		System.out.println("Now Testing Method:setup Branch:15");
		
	
		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: (lur instanceof EpiLur), for: Not (int i=0; (lur = elur.get(i))!=null;++i)
	 */
	@Test
	public void test_method_setup_2_branch_16()
	{
		System.out.println("Now Testing Method:setup Branch:16");

		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), else: Not (lur instanceof EpiLur), if: (lur instanceof AbsAAFLur)
	 */
	@Test
	public void test_method_setup_2_branch_17()
	{
		System.out.println("Now Testing Method:setup Branch:17");
		
	
		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), else: Not (lur instanceof EpiLur), else: Not (lur instanceof AbsAAFLur)
	 */
	@Test
	public void test_method_setup_2_branch_18()
	{
		System.out.println("Now Testing Method:setup Branch:18");
		
	
		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: (aafAuthn==null)
	 */
	@Test
	public void test_method_setup_2_branch_19()
	{
		System.out.println("Now Testing Method:setup Branch:19");
		
	
		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), else: Not (aafAuthn==null)
	 */
	@Test
	public void test_method_setup_2_branch_20()
	{
		System.out.println("Now Testing Method:setup Branch:20");
		
	
		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), if: (access!=null)
	 */
	@Test
	public void test_method_setup_2_branch_21()
	{
		System.out.println("Now Testing Method:setup Branch:21");
		
		
		
	}

	/*
	 * Testing Conditon(s): if: (aafAuthn == null), else: Not (access!=null)
	 */
	@Test
	public void test_method_setup_2_branch_22()
	{
		System.out.println("Now Testing Method:setup Branch:22");
		
	
	}

	/*
	 * Testing Conditon(s): else: Not (aafAuthn == null)
	 */
	@Test
	public void test_method_setup_2_branch_23()
	{
		System.out.println("Now Testing Method:setup Branch:23");
	
	}

	/*
	 * Testing Conditon(s): Default
	 */
	@Test
	public void test_method_protectedResources_3_branch_0()
	{
		System.out.println("Now Testing Method:protectedResources Branch:0");
		
		
	}

	/*
	 * Testing Conditon(s): Default
	 */
	@Test
	public void test_method_supportedOptions_4_branch_0()
	{
		System.out.println("Now Testing Method:supportedOptions Branch:0");
		
	
	}

	/*
	 * Testing Conditon(s): Default
	 */
	@Test
	public void test_method_alterableOptions_5_branch_0()
	{
		System.out.println("Now Testing Method:alterableOptions Branch:0");
		
	
		
	}

}
