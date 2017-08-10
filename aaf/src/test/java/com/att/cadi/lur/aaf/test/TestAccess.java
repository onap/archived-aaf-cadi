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
package com.att.cadi.lur.aaf.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import com.att.cadi.Access;
import com.att.cadi.Symm;
import com.att.cadi.config.Config;

public class TestAccess implements Access {
	private Symm symm;
	private PrintStream out;

	public TestAccess(PrintStream out) {
		this.out = out;
		InputStream is = ClassLoader.getSystemResourceAsStream("cadi.properties");
		try {
			System.getProperties().load(is);
		} catch (IOException e) {
			e.printStackTrace(out);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace(out);
			}
		}
		
		String keyfile = System.getProperty(Config.CADI_KEYFILE);
		if(keyfile==null) {
			System.err.println("No " + Config.CADI_KEYFILE + " in Classpath");
		} else {
			try {
				is = new FileInputStream(keyfile);
				try {
					symm = Symm.obtain(is);
				} finally {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace(out);
			}
		}
		


	}
	
	public void log(Level level, Object... elements) {
		boolean first = true;
		for(int i=0;i<elements.length;++i) {
			if(first)first = false;
			else out.print(' ');
			out.print(elements[i].toString());
		}
		out.println();
	}

	public void log(Exception e, Object... elements) {
		e.printStackTrace(out);
		log(Level.ERROR,elements);
	}

	public void setLogLevel(Level level) {
		
	}

	@Override
	public boolean willLog(Level level) {
		return true;
	}

	public ClassLoader classLoader() {
		return ClassLoader.getSystemClassLoader();
	}

	public String getProperty(String string, String def) {
		String rv = System.getProperty(string);
		return rv==null?def:rv;
	}

	public void load(InputStream is) throws IOException {
		
	}

	public String decrypt(String encrypted, boolean anytext) throws IOException {
		return (encrypted!=null && (anytext==true || encrypted.startsWith(Symm.ENC)))
			? symm.depass(encrypted)
			: encrypted;
	}

	@Override
	public void printf(Level level, String fmt, Object... elements) {
		// TODO Auto-generated method stub
		
	}

}
