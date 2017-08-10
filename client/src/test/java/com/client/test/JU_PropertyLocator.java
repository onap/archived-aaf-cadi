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
package com.client.test;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.att.cadi.Locator.Item;
import com.att.cadi.locator.PropertyLocator;

public class JU_PropertyLocator {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws Exception {
		PropertyLocator pl = new PropertyLocator("https://localhost:2345,https://fred.wilma.com:26444,https://tom.jerry.com:534");
		
		Item i;
		int count;
		boolean print = false;
		for(int j=0;j<900000;++j) {
			count = 0;
			for(i = pl.first();i!=null;i=pl.next(i)) {
				URI loc = pl.get(i);
				if(print)System.out.println(loc.toString());
				++count;
			}
			assertEquals(3,count);
			assertTrue(pl.hasItems());
			if(print)System.out.println("---");
			pl.invalidate(pl.best());
			
			count = 0;
			for(i = pl.first();i!=null;i=pl.next(i)) {
				URI loc = pl.get(i);
				if(print)System.out.println(loc.toString());
				++count;
			}
	
			assertEquals(2,count);
			assertTrue(pl.hasItems());
			if(print)System.out.println("---");
			pl.invalidate(pl.best());
			
			count = 0;
			for(i = pl.first();i!=null;i=pl.next(i)) {
				URI loc = pl.get(i);
				if(print)System.out.println(loc.toString());
				++count;
			}
	
			assertEquals(1,count);
			assertTrue(pl.hasItems());
			if(print)System.out.println("---");
			pl.invalidate(pl.best());
			
			count = 0;
			for(i = pl.first();i!=null;i=pl.next(i)) {
				URI loc = pl.get(i);
				if(print)System.out.println(loc.toString());
				++count;
			}
	
			assertEquals(0,count);
			assertFalse(pl.hasItems());
			
			pl.refresh();
		}
	}

}
