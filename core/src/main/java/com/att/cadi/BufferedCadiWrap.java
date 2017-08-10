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
package com.att.cadi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;

/**
 * BufferedCadiWrap exists to additionally wrap the InputStream with a BufferedInputStream to engage the
 * "mark()/release()" API of InputStream.
 * 
 * This is a requirement for Re-Reading Content for brain-dead Middleware such as SOAP WS-Security.
 * 
 * Framework needs to set the TafResp and Lur (typically) later in the 
 * 
 *
 */
public class BufferedCadiWrap extends CadiWrap {
	private BufferedServletInputStream sis;
	
	public BufferedCadiWrap(HttpServletRequest request) {
		super(request, null, null); // will need to set TafResp and Lur separately
		sis = null;
	}


	// @Override
	public BufferedServletInputStream getInputStream() throws IOException {
//		System.out.println("getInputStream() from Buffered CadiWrap... sis = " + sis);
//		try {
//			throw new Exception("OK, here's where we are...");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		if(sis==null) {
			sis = new BufferedServletInputStream(super.getInputStream());
//		} else {
//			try {
//			System.out.println("sis has " + sis.buffered() + " buffered bytes, and reports " + sis.available() + " available");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
		return sis;
	}

	// @Override
	public BufferedReader getReader() throws IOException {
//		System.out.println("getReader() from Buffered CadiWrap... sis = " + sis);
//		try {
//			throw new Exception("OK, here's where we are...");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}
}
