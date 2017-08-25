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
package org.onap.aaf.cadi.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// Substandard, because System.in doesn't do Passwords..
public class SubStandardConsole implements MyConsole {
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	@Override
	public String readLine(String fmt, Object... args) {
		String rv;
		try {
			System.out.printf(fmt,args);
			rv = br.readLine();
			if(args.length==1 && rv.length()==0) {
				rv = args[0].toString();
			}
		} catch (IOException e) {
			System.err.println("uh oh...");
			rv = "";
		}
		return rv;
	}

	@Override
	public char[] readPassword(String fmt, Object... args) {
		try {
			System.out.printf(fmt,args);
			return br.readLine().toCharArray();
		} catch (IOException e) {
			System.err.println("uh oh...");
			return new char[0];
		}
	}

	@Override
	public void printf(String fmt, Object... args) {
		System.out.printf(fmt, args);
	}
}
