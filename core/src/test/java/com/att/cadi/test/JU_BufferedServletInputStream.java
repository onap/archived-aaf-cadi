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
package com.att.cadi.test;

import static junit.framework.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

import com.att.cadi.BufferedServletInputStream;

public class JU_BufferedServletInputStream {

	@Test
	public void testByteRead() throws Exception {
		FileInputStream fis = new FileInputStream("test/CBUSevent.xml");
		BufferedServletInputStream bsis = new BufferedServletInputStream(fis);
			try {
			bsis.mark(0);
			int c;
			byte aa[] = new byte[260];
			for(int i=0;i<aa.length;++i) {
				c = bsis.read();
				if(c>=0) {
					aa[i]=(byte)c;
				}
			}
			System.out.println(new String(aa));
			
			bsis.reset();

			byte bb[] = new byte[400];
			for(int i=0;i<bb.length;++i) {
				c = bsis.read();
				if(c>=0) {
					bb[i]=(byte)c;
				}
			}
			System.out.println(new String(bb));

		} finally {
			bsis.close();
			fis.close();
		}
	}
	
	@Test
	public void testByteArray() throws Exception {
		FileInputStream fis = new FileInputStream("test/CBUSevent.xml");
		BufferedServletInputStream bsis = new BufferedServletInputStream(fis);
		try {
			bsis.mark(0);
			byte aa[] = new byte[260];
			bsis.read(aa);
			System.out.println(new String(aa));
			
			bsis.reset();

			byte bb[] = new byte[400];
			bsis.read(bb);
			System.out.println(new String(bb));

		} finally {
			bsis.close();
			fis.close();
		}
	}

	@Test
	public void testDoubleRead() throws Exception {
		FileInputStream fis = new FileInputStream("test/CBUSevent.xml");
		BufferedServletInputStream bsis = new BufferedServletInputStream(fis);
		try {
			bsis.mark(0);
			byte aa[] = new byte[260];
			bsis.read(aa);
			System.out.println(new String(aa));
			
			bsis.reset();

			byte bb[] = new byte[400];
			bsis.read(bb);
			System.out.println(new String(bb));

		} finally {
			bsis.close();
			fis.close();
		}
	}

	@Test
	public void testByteArray2() throws Exception {
		FileInputStream fis = new FileInputStream("test/CBUSevent.xml");
		try {
			BufferedServletInputStream bsis = new BufferedServletInputStream(fis);
			byte[] content = null;
			byte aa[] = new byte[500];
			for(int i=0;i<2000;++i) {
				bsis.mark(0);
				bsis.read(aa,0,260);
				if(i==0)System.out.println(new String(aa));
				
				bsis.reset();
	
				bsis.read(aa,0,aa.length);
				if(i==0) {
					System.out.println(new String(aa));
					content = aa;
					aa = new byte[400];
				}
				bsis = new BufferedServletInputStream(new ByteArrayInputStream(content));
				
			}
			
			System.out.println(new String(aa));

		} finally {
			fis.close();
		}
	}

	// "Bug" 4/22/2013 
	// Some XML code expects Buffered InputStream can never return 0...  This isn't actually true, but we'll accommodate as far
	// as we can. 
	// Here, we make sure we set and read the Buffered data, making sure the buffer is empty on the last test...
	@Test
	public void issue04_22_2013() throws IOException {
		String testString = "We want to read in and get out with a Buffered Stream seamlessly.";
		ByteArrayInputStream bais = new ByteArrayInputStream(testString.getBytes());
		BufferedServletInputStream bsis = new BufferedServletInputStream(bais);
			try {
			bsis.mark(0);
			byte aa[] = new byte[testString.length()];  // 65 count... important for our test (divisible by 5);

			int read;
			for(int i=0;i<aa.length;i+=5) {
				read = bsis.read(aa, i, 5);
				assertEquals(5,read);
			}
			System.out.println(new String(aa));
			
			bsis.reset();

			byte bb[] = new byte[aa.length];
			read = 0;
			for(int i=0;read>=0;i+=read) {
				read = bsis.read(bb,i,5);
				switch(i) {
					case 65:
						assertEquals(read,-1);
						break;
					default:
						assertEquals(read,5);
				}
			}
			System.out.println(new String(bb));
			assertEquals(testString,new String(aa));
			assertEquals(testString,new String(bb));

		} finally {
			bsis.close();
			bais.close();
		}
		
	}
	

}
