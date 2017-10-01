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
package org.onap.aaf.cadi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import org.junit.Test;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.config.Config;


public class JU_Base64 {
	private static final String encoding = "Man is distinguished, not only by his reason, but by this singular " +
			"passion from other animals, which is a lust of the mind, that by a " + 
			"perseverance of delight in the continued and indefatigable generation of " + 
			"knowledge, exceeds the short vehemence of any carnal pleasure.";
		 
	private static final String expected = 
			"TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz\n" + 
			"IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg\n" + 
			"dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu\n" +
			"dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo\n" +
			"ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";


	@Test
	public void test() throws Exception {
		// Test with different Padding
		encode("leas",    "bGVhcw==");
		encode("leasu",   "bGVhc3U=");
		encode("leasur",  "bGVhc3Vy");
		encode("leasure", "bGVhc3VyZQ==");
		encode("leasure.","bGVhc3VyZS4=");

		// Test with line ends
		encode(encoding, expected);
		
		int ITER = 2000;
		System.out.println("Priming by Encoding Base64 " + ITER + " times");
		long start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.encode(encoding);
		}
		Float ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
		
		System.out.println("Priming by Decoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.decode(expected);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");

		
		ITER=30000;
		System.out.println("Encoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.encode(encoding);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
		
		System.out.println("Decoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.decode(expected);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
	}
	
	@Test
	public void symmetric() throws IOException {
		System.out.println("Validating Generated Key mechanisms works");
		String symmetric = new String(Symm.base64.keygen());
		System.out.println(symmetric);
		Symm bsym = Symm.obtain(symmetric);
		String result = bsym.encode(encoding);
		System.out.println("\nResult:");
		System.out.println(result);
		assertEquals(encoding, bsym.decode(result));
		
		int ITER = 20000;
		System.out.println("Generating keys " + ITER + " times");
		long start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.keygen();
		}
		Float ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");

		char[] manipulate = symmetric.toCharArray();
		int spot = new SecureRandom().nextInt(manipulate.length);
		manipulate[spot]|=0xFF;
		String newsymmetric = new String(manipulate);
		assertNotSame(newsymmetric, symmetric);
		try {
			bsym = Symm.obtain(newsymmetric);
			result = bsym.decode(result);
			assertEquals(encoding, result);
		} catch (IOException e) {
			// this is what we want to see if key wrong
			System.out.println(e.getMessage() + " (as expected)");
		}
	}

	private void encode(String toEncode, String expected) throws IOException {
		System.out.println("-------------------------------------------------");
		System.out.println(toEncode);
		System.out.println();
		System.out.println(expected);
		System.out.println();
		String result = Symm.base64.encode(toEncode);
		System.out.println(result);
		assertEquals(expected,result);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Symm.base64.decode(new ByteArrayInputStream(result.getBytes()), baos);
		result = baos.toString(Config.UTF_8);
		System.out.println(result);
		assertEquals(toEncode,result);
		
	}
	
	@Test
	public void test1() throws Exception {
		// Test with different Padding
		encode("leas",    "bGVhcw==");
		encode("leasu",   "bGVhc3U=");
		encode("leasur",  "bGVhc3Vy");
		encode("leasure", "bGVhc3VyZQ==");
		encode("leasure.","bGVhc3VyZS4=");

		// Test with line ends
		encode(encoding, expected);
		
		int ITER = 2000;
		System.out.println("Priming by Encoding Base64 " + ITER + " times");
		long start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.encode(encoding);
		}
		Float ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
		
		System.out.println("Priming by Decoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.decode(expected);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");

		
		ITER=30000;
		System.out.println("Encoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.encode(encoding);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
		
		System.out.println("Decoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.decode(expected);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
	}
	
	@Test
	public void symmetric1() throws IOException {
		System.out.println("Validating Generated Key mechanisms works");
		String symmetric = new String(Symm.base64.keygen());
		System.out.println(symmetric);
		Symm bsym = Symm.obtain(symmetric);
		String result = bsym.encode(encoding);
		System.out.println("\nResult:");
		System.out.println(result);
		assertEquals(encoding, bsym.decode(result));
		
		int ITER = 20000;
		System.out.println("Generating keys " + ITER + " times");
		long start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.keygen();
		}
		Float ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");

		char[] manipulate = symmetric.toCharArray();
		int spot = new SecureRandom().nextInt(manipulate.length);
		manipulate[spot]|=0xFF;
		String newsymmetric = new String(manipulate);
		assertNotSame(newsymmetric, symmetric);
		try {
			bsym = Symm.obtain(newsymmetric);
			result = bsym.decode(result);
			assertEquals(encoding, result);
		} catch (IOException e) {
			// this is what we want to see if key wrong
			System.out.println(e.getMessage() + " (as expected)");
		}
	}

	private void encode1(String toEncode, String expected) throws IOException {
		System.out.println("-------------------------------------------------");
		System.out.println(toEncode);
		System.out.println();
		System.out.println(expected);
		System.out.println();
		String result = Symm.base64.encode(toEncode);
		System.out.println(result);
		assertEquals(expected,result);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Symm.base64.decode(new ByteArrayInputStream(result.getBytes()), baos);
		result = baos.toString(Config.UTF_8);
		System.out.println(result);
		assertEquals(toEncode,result);
		
	}
	
	@Test
	public void test2() throws Exception {
		// Test with different Padding
		encode("leas",    "bGVhcw==");
		encode("leasu",   "bGVhc3U=");
		encode("leasur",  "bGVhc3Vy");
		encode("leasure", "bGVhc3VyZQ==");
		encode("leasure.","bGVhc3VyZS4=");

		// Test with line ends
		encode(encoding, expected);
		
		int ITER = 2000;
		System.out.println("Priming by Encoding Base64 " + ITER + " times");
		long start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.encode(encoding);
		}
		Float ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
		
		System.out.println("Priming by Decoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.decode(expected);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");

		
		ITER=30000;
		System.out.println("Encoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.encode(encoding);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
		
		System.out.println("Decoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.decode(expected);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
	}
	
	@Test
	public void symmetric2() throws IOException {
		System.out.println("Validating Generated Key mechanisms works");
		String symmetric = new String(Symm.base64.keygen());
		System.out.println(symmetric);
		Symm bsym = Symm.obtain(symmetric);
		String result = bsym.encode(encoding);
		System.out.println("\nResult:");
		System.out.println(result);
		assertEquals(encoding, bsym.decode(result));
		
		int ITER = 20000;
		System.out.println("Generating keys " + ITER + " times");
		long start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.keygen();
		}
		Float ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");

		char[] manipulate = symmetric.toCharArray();
		int spot = new SecureRandom().nextInt(manipulate.length);
		manipulate[spot]|=0xFF;
		String newsymmetric = new String(manipulate);
		assertNotSame(newsymmetric, symmetric);
		try {
			bsym = Symm.obtain(newsymmetric);
			result = bsym.decode(result);
			assertEquals(encoding, result);
		} catch (IOException e) {
			// this is what we want to see if key wrong
			System.out.println(e.getMessage() + " (as expected)");
		}
	}

	private void encode2(String toEncode, String expected) throws IOException {
		System.out.println("-------------------------------------------------");
		System.out.println(toEncode);
		System.out.println();
		System.out.println(expected);
		System.out.println();
		String result = Symm.base64.encode(toEncode);
		System.out.println(result);
		assertEquals(expected,result);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Symm.base64.decode(new ByteArrayInputStream(result.getBytes()), baos);
		result = baos.toString(Config.UTF_8);
		System.out.println(result);
		assertEquals(toEncode,result);
		
	}
}
