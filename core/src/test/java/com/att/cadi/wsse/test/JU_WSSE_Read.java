/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
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
package com.att.cadi.wsse.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.att.cadi.BasicCred;
import com.att.cadi.BufferedServletInputStream;
import com.att.cadi.wsse.WSSEParser;

public class JU_WSSE_Read {

	@Test
	public void test() {
		try {
			final BasicCred bc = new BasicCred() {

				private String user;
				private byte[] password;

				public void setUser(String user) {
					this.user = user;
				}

				public void setCred(byte[] passwd) {
					this.password = passwd;
				}

				public String getUser() {
					return user;
				}

				public byte[] getCred() {
					return password;
				}
			};

			WSSEParser wp = new WSSEParser();

			FileInputStream fis;
			fis = new FileInputStream("test/example.xml");
			BufferedServletInputStream is = new BufferedServletInputStream(fis);
			try {
				is.mark(1536);
				try {
					assertNull(wp.parse(bc, is));
				} finally {
					is.reset();
					assertEquals(814,is.buffered());
				}
				String password = new String(bc.getCred());
				System.out.println("CadiWrap credentials are: " + bc.getUser() + ", " + password);
				assertEquals("some_user", bc.getUser());
				assertEquals("some_password", password);
				
			} finally {
				fis.close();
			}

			// CBUS (larger)
			fis = new FileInputStream("test/CBUSevent.xml");
			is = new BufferedServletInputStream(fis);
			try {
				is.mark(1536);
				try {
					assertNull(wp.parse(bc, is));
				} finally {
					is.reset();
					assertEquals(667,is.buffered());
				}
				String password = new String(bc.getCred());
				System.out.println("CadiWrap credentials are: " + bc.getUser() + ", " + password);
				assertEquals("none", bc.getUser());
				assertEquals("none", password);
				
			} finally {
				fis.close();
			}

			// Closed Stream
			fis = new FileInputStream("test/example.xml");
			fis.close();
			bc.setCred(null);
			bc.setUser(null);
			XMLStreamException ex = wp.parse(bc, fis);
			assertNotNull(ex);
			assertNull(bc.getUser());
			assertNull(bc.getCred());


			fis = new FileInputStream("test/exampleNoSecurity.xml");
			try {
				bc.setCred(null);
				bc.setUser(null);
				assertNull(wp.parse(bc, fis));
				assertNull(bc.getUser());
				assertNull(bc.getCred());
			} finally {
				fis.close();
			}

			fis = new FileInputStream("test/exampleBad1.xml");
			try {
				bc.setCred(null);
				bc.setUser(null);
				assertNull(wp.parse(bc, fis));
				assertNull(bc.getUser());
				assertNull(bc.getCred());
			} finally {
				fis.close();
			}

			XMLStreamException e = wp.parse(bc, new ByteArrayInputStream("Not XML".getBytes())); // empty
			assertNotNull(e);

			e = wp.parse(bc, new ByteArrayInputStream("".getBytes())); // empty
			assertNotNull(e);
			
			
			long start, count = 0L;
			int iter = 30000;
			File f = new File("test/CBUSevent.xml");
			fis = new FileInputStream(f);
			is = new BufferedServletInputStream(fis);
			is.mark(0);
			try {
				while(is.read()>=0);
			} finally {
				fis.close();
			}

			for(int i=0;i<iter;++i) {
				start = System.nanoTime();
				is.reset();
				try {
					assertNull(wp.parse(bc, is));
				} finally {
					count += System.nanoTime()-start;
				}
			}
			float ms = count/1000000f;
			System.out.println("Executed " + iter + " WSSE reads from Memory Stream in " + ms + "ms.  " + ms/iter + "ms per trans");
			
			// SPECIFIC ISSUES
			
			fis = new FileInputStream("test/error2013_04_23.xml");
			try {
				bc.setCred(null);
				bc.setUser(null);
				assertNull(wp.parse(bc, fis));
				assertNull(bc.getUser());
				assertNull(bc.getCred());
			} finally {
				fis.close();
			}
		} catch(Exception e) {
			e.printStackTrace(System.err);
		}
	}

}
