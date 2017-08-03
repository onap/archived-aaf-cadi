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
package com.att.cadi.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URI;

import com.att.aft.dme2.api.DME2Client;
import com.att.cadi.Symm;
import com.att.cadi.config.Config;

public abstract class RawClient {
	protected static String aafid, aafpass, aafurl;
	protected static Symm symm;
	
	protected static boolean init(PrintStream out) {
		try {
			String propfile = System.getProperty(Config.CADI_PROP_FILES);
			if(propfile==null) {
				propfile = "raw.props";
			}
			File pfile = new File(propfile);
			if(!pfile.exists())  {
				if(propfile.equals("raw.props")) {
					out.println("Creating 'raw.props'.  Edit for proper values, then run again.  Alternatively, set "
					+ Config.CADI_PROP_FILES+" to a cadi properties file");
					FileOutputStream fos = new FileOutputStream(pfile);
					PrintStream ps = new PrintStream(fos);
					try {
						ps.println("# Use http://www.bing.com/maps to figure out LAT/LONG of an Address");
						ps.println("AFT_LATITUDE=38.432930");
						ps.println("AFT_LONGITUDE=-90.432480");
						ps.println("AFT_ENVIRONMENT=AFTUAT");
						ps.print(Config.AAF_URL);
						ps.println("=aaf_url=https://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=2.0/envContext=DEV/routeOffer=BAU_SE");
						ps.print(Config.CADI_KEYFILE);
						ps.println("=<keyfile.  use java -jar cadi-core*.jar in lib dir>");
						ps.println(Config.AAF_MECHID);
						ps.print("=<your id>");
						ps.println(Config.AAF_MECHPASS);
						ps.print("=<your encrypted password.  use java -jar cadi-core*.jar in lib dir>");
					} finally {
						ps.close();
					}
				}
			} else {
				FileInputStream fis = new FileInputStream(propfile);
				try {
					System.getProperties().load(fis);
				} finally {
					fis.close();
				}
				
				String cadiKeyFile = System.getProperty(Config.CADI_KEYFILE);
				aafid = System.getProperty(Config.AAF_MECHID);
				aafpass = System.getProperty(Config.AAF_MECHPASS);
				aafurl = System.getProperty(Config.AAF_URL);
				out.println("Contacting: " + aafurl);

				if(cadiKeyFile==null || aafid==null || aafpass==null || aafurl==null ) {
					out.print(Config.CADI_KEYFILE);
					out.print(", ");
					out.print(Config.CADI_KEYFILE);
					out.print(", ");
					out.print(Config.CADI_KEYFILE);
					out.print(", ");
					out.print(Config.CADI_KEYFILE);
					out.print(" need to be set in ");
					out.println(propfile);
				} else {
					fis = new FileInputStream(cadiKeyFile);
					try {
						symm = Symm.obtain(fis);
					} finally {
						fis.close();
					}
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace(out);
		}
		return false;

	}
	
	public abstract String call(final PrintStream out, final String meth, final String path) throws Exception;
	
	public static void main(String[] args) {
		// Sonar idiocy
		PrintStream out = System.out;

		try {
			if(init(out)) {
				if(args.length<2) {
					System.out.println("Parameters: <Method> <path>");
				} else {
					RawClient client = new DME2();
					out.println(client.call(out,args[0],args[1]));
				}
			}		
		} catch (Exception e) {
			e.printStackTrace(out);
		}
	}
	
	protected static class DME2 extends RawClient {

		public String call(final PrintStream out, final String meth, final String path) {
			try {
				DME2Client client = new DME2Client(new URI(aafurl),10000);
				client.setCredentials(aafid, symm.depass(aafpass));
				client.setMethod(meth);
				client.setContext(path);
				
				if("GET".equalsIgnoreCase(meth) ||
				   "DELETE".equalsIgnoreCase(meth)) {
					client.setPayload("");
				} else if("POST".equalsIgnoreCase(meth) ||
						  "PUT".equalsIgnoreCase(meth)) {
					int c;
					StringBuilder sb = new StringBuilder();
					while((c=System.in.read()) >=0) {
						sb.append((char)c);
					}
					client.setPayload(sb.toString());
				}
				return client.sendAndWait(10000);
			} catch (Exception e) {
				e.printStackTrace(out);
				return "";
			}
		}
	}
}
