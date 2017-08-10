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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import com.att.cadi.CmdLine;
import com.att.cadi.Symm;

import junit.framework.Assert;

public class JU_Passcode {
	@Test
	public void test() throws Exception {
		int iterations = 0;
		long start = System.nanoTime();
		int gens= 800, en_de=2000;
		for(int j=0;j<gens;++j) {
			CmdLine.main(new String[] {"keygen","tempkey"});
			
			Symm symm;
			File fi = new File("tempkey");
			Assert.assertEquals(2074, fi.length());
			FileInputStream fis = new FileInputStream(fi);
			
			try {
				symm = Symm.obtain(fis);
			} finally {
				fis.close();
			}
			String samples[] = {"activevos","ThisIsATestPassword","I have spaces","I have 's, /s and &s and _s"};
			ByteArrayOutputStream baos;
			for(int i=0;i<en_de;++i) {
				String password = samples[i%samples.length];
				baos = new ByteArrayOutputStream();
				symm.enpass(password, baos);
				String pass = baos.toString();
				byte[] array = baos.toByteArray();
				for(int k=0;k<array.length;++k) {
					byte ch = array[k];
//				for(int k=0;k<pass.length();++k) {
//					char ch = pass.charAt(k);
					if(!(Character.isLetter(ch) || Character.isDigit(ch) || ch=='-' || ch=='_' || ch=='=')) {
						throw new Exception("Yikes, have a bad character..." + ch + '(' + (int)ch + ')');
					}
				}
				baos = new ByteArrayOutputStream();
				symm.depass(pass, baos);
				Assert.assertEquals(password,baos.toString());
				Assert.assertEquals(password,symm.depass(pass));
				++iterations;
			}
			symm.enpass("activevos", System.out);
			System.out.println();
		}
		float ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Ran " + iterations + " Encrypt/Decrypt cycles + " + gens + " keygens");

		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/iterations + "ms");
		System.out.println("Avg Gen + " + en_de + " Encrypt/Decrypt cycles:  " + ms/gens + "ms");


	}

}
