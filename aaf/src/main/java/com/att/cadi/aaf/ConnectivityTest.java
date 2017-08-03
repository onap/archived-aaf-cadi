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
package com.att.cadi.aaf;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Date;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.api.DME2Manager;
import com.att.cadi.CadiException;
import com.att.cadi.Locator;
import com.att.cadi.Locator.Item;
import com.att.cadi.LocatorException;
import com.att.cadi.Lur;
import com.att.cadi.PropAccess;
import com.att.cadi.SecuritySetter;
import com.att.cadi.TrustChecker;
import com.att.cadi.aaf.v2_0.AAFCon;
import com.att.cadi.aaf.v2_0.AAFConDME2;
import com.att.cadi.client.Future;
import com.att.cadi.config.Config;
import com.att.cadi.config.SecurityInfoC;
import com.att.cadi.http.HBasicAuthSS;
import com.att.cadi.http.HClient;
import com.att.cadi.http.HX509SS;
import com.att.cadi.locator.DME2Locator;
import com.att.cadi.locator.PropertyLocator;
import com.att.inno.env.APIException;
import com.att.rosetta.env.RosettaDF;
import com.att.rosetta.env.RosettaEnv;

import aaf.v2_0.Perms;

public class ConnectivityTest {
	private static final String PROD = "PROD";
	private static final String SRV_RESOLVE = "https://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=2.0/envContext=%s/routeOffer=%s";
	private static final String GW_RESOLVE = "https://DME2RESOLVE/service=com.att.authz.authz-gw/version=2.0/envContext=%s/routeOffer=%s";
	
	public static void main(String[] args) {
		if(args.length<2) {
			System.out.println("Usage: ConnectivityTester <TEST|IST|PROD> <cadi_prop_files>");
		} else {
			print(true,"START OF CONNECTIVITY TESTS",new Date().toString(),System.getProperty("user.name"),
					"Note: All API Calls are /authz/perms/user/<MechID/Alias of the caller>");

			final String aaf_env = args[0];
			args[1]=Config.CADI_PROP_FILES+'='+args[1];
			
			PropAccess pa = new PropAccess(args);
			String user = pa.getProperty(Config.AAF_MECHID);
			String pass = pa.getProperty(Config.AAF_MECHPASS);
			String alias = pa.getProperty(Config.CADI_ALIAS);
			if(user==null) {
				user=alias;
			}
			RosettaEnv env = new RosettaEnv(pa.getProperties());
			
			try {
				RosettaDF<Perms> permsDF = env.newDataFactory(Perms.class);
				SecurityInfoC<HttpURLConnection> si = new SecurityInfoC<HttpURLConnection>(pa);
				HBasicAuthSS hbass = new HBasicAuthSS(pa,si);
				if(hbass.getID()==null) {
					hbass=null; // not configured with ID.
				}
				HX509SS hxss=null;
				AAFCon<?> aafcon;
				
				try {
					hxss = new HX509SS(user,si);
				} catch(Exception e) {
					e.printStackTrace();
					print(false,"Continuing");
				}
				String aafurl;
				if(user==null || (pass==null && alias==null)) {
					System.out.printf("ERROR: DME2 Client cannot be tested with out %s and %s properties"
							, Config.AAF_MECHID, Config.AAF_MECHPASS );
				} else {
					if("TEST".equals(aaf_env) || "IST".equals(aaf_env) || "PROD".equals(aaf_env)) {
						DME2Manager dm = null;
						print(false,"Attempt DME2Manager Load");
						if(Class.forName("com.att.aft.dme2.api.DME2Manager")==null) {
							print(true,"DME2 jar is not available:  Skipping DME2 Tests");
						} else { // DME2 Client Tests
							pass=pa.decrypt(pass,false);
							// Out of the box DME2
							aafurl = String.format(SRV_RESOLVE, aaf_env, PROD.equals(aaf_env)?"DEFAULT":"BAU_SE");
							print(true,"TEST CADI Config",aafurl);
							aafcon = testConfig(pa,aafurl);
							test(aafcon,permsDF,user);
							
							print(true,"Find and TEST Connections with DME2Locator",aafurl);
							DME2Locator dl = new DME2Locator(pa,dm,aafurl);
							connectTest(dl);
	
							dm =  new DME2Manager("DME2Manager",pa.getProperties());
	
							dme2RawTest(dm, aafurl,user,pass);
							
							// URL specific Variant
							if((aafurl = specificDME2URL(dl, aafurl))!=null) {
								print(true,"TEST Specific DME2 CADI Config",aafurl);
								aafcon = testConfig(pa,aafurl);
								test(aafcon,permsDF,user);
	
								dme2RawTest(dm,aafurl,user,pass);
							}
							
							print(true,"CADI Direct AAFConDME2 Object Usage",aafurl);
							try {
								pa.setProperty(Config.AAF_URL,aafurl);
								aafcon = new AAFConDME2(pa);
								test(aafcon,permsDF,user);
							} catch(Throwable t) {
								t.printStackTrace();
							}
							
							// find a direct client to code a Direct HTTP with
	//						
							if(hbass!=null) {
								print(true,"CADI Http DME2Locator Client Coding Methodology BasicAuth",aafurl);
								hClientTest(dl,hbass,user);
							}
							if(hxss!=null) {
								print(true,"CADI Http DME2Locator Client Coding Methodology X509",aafurl);
								hClientTest(dl,hxss,user);
							}
							
							// ##### PROXY CHECKS
							aafurl = String.format(GW_RESOLVE, aaf_env, PROD.equals(aaf_env)?"DEFAULT":"BAU_SE");
							print(true,"TEST PROXY DME2 CADI Config",aafurl);
							aafcon = testConfig(pa,aafurl);
							test(aafcon,permsDF,user);
	
	
							dme2RawTest(dm, aafurl,user,pass);
							
							// URL specific Variant
							dl = new DME2Locator(pa,dm,aafurl);
							if((aafurl = specificDME2URL(dl, aafurl))!=null) {
								print(true,"TEST PROXY Specific DME2 CADI Config",aafurl);
								aafcon = testConfig(pa,aafurl);
								test(aafcon,permsDF,user);
	
								dme2RawTest(dm,aafurl,user,pass);
							}
						}
					}

					// Prop Locator
					PropertyLocator pl = servicePropLocator(aaf_env);
					connectTest(pl);
					URI uri = pl.get(pl.best());
					if(uri!=null) {
						aafurl = uri.toString();
						print(true,"TEST Service PropertyLocator based Config",aafurl);
						aafcon = testConfig(pa,aafurl);
						test(aafcon,permsDF,user);
	
						if(hbass!=null) {
							print(true,"CADI Service Http PropLocator Client Coding Methodology Basic Auth",aafurl);
							hClientTest(pl,hbass, user);
							print(true,"CADI Service Http PropLocator Client Coding Methodology /authn/basicAuth",aafurl);
							basicAuthTest(pl,hbass);
						}
						if(hxss!=null) {
							print(true,"CADI Service Http PropLocator Client Coding Methodology X509",aafurl);
							hClientTest(pl,hxss, user);
						}
					}
					pl = proxyPropLocator(aaf_env);
					connectTest(pl);
					uri = pl.get(pl.best());
					if(uri!=null) {
						aafurl = uri.toString();
						print(true,"TEST PROXY PropertyLocator based Config",aafurl);
						aafcon = testConfig(pa,aafurl);
						test(aafcon,permsDF,user);
	
						if(hbass!=null) {
							print(true,"CADI PROXY Http PropLocator Client Coding Methodology Basic Auth",aafurl);
							hClientTest(pl,hbass, user);
							print(true,"CADI PROXY Http PropLocator Client Coding Methodology /proxy/authn/basicAuth",aafurl);
							basicAuthTest(pl,hbass);
						}
						if(hxss!=null) {
							print(true,"CADI PROXY Http PropLocator Client Coding Methodology X509",aafurl);
							hClientTest(pl,hxss, user);
						}
					}
				}
				
			} catch(Exception e) {
				e.printStackTrace(System.err);
			} finally {
				print(true,"END OF TESTS");
			}
		}
	}
	
	private static void print(Boolean strong, String ... args) {
		PrintStream out = System.out;
		out.println();
		if(strong) {
			for(int i=0;i<70;++i) {
				out.print('=');
			}
			out.println();
		}
		for(String s : args) {
			out.print(strong?"==  ":"------ ");
			out.print(s);
			if(!strong) {
				out.print("  ------");
			}
			out.println();
		}
		if(strong) {
			for(int i=0;i<70;++i) {
				out.print('=');
			}
		}
		out.println();
	}

	private static void test(AAFCon<?> aafcon,RosettaDF<Perms> permsDF,String user) {
		if(aafcon==null) {
			print(false,"AAFCon is null");
		} else {
			try {
				print(false,"Calling with AAFCon");
				Future<Perms> fp = aafcon.client("2.0").read("/authz/perms/user/"+user, Perms.class, permsDF);
				if(fp.get(4000)) {
					System.out.printf("Found %d Permission(s)\n",fp.value.getPerm().size());
				} else {
					System.out.printf("Error: %d %s\n",fp.code(),fp.body());
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	
	private static AAFCon<?> testConfig(PropAccess pa, String aafurl) {
		try {
			pa.setProperty(Config.AAF_URL, aafurl);
			Lur lur = Config.configLur(pa);
			Config.configHttpTaf(pa, TrustChecker.NOTRUST, null, lur);
			if(lur != null) {
				Field f = null;
				try {
					f = lur.getClass().getField("aaf");
					return (AAFCon<?>)f.get(lur);
				} catch (Exception nsfe) {
				}
			}

		} catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	private static String specificDME2URL(Locator<URI> loc, String aafurl) throws LocatorException {
		Item item = loc.best();
		if(item!=null) {
			URI uri = loc.get(item);
			return aafurl.replace("DME2RESOLVE", String.format("%s:%d",uri.getHost(),uri.getPort()));
		}
		return null;
	}

	private static void connectTest(Locator<URI> dl) throws LocatorException {
		URI uri;
		Socket socket;
		print(false,"TCP/IP Connect test to all Located Services");
		for(Item li = dl.first();li!=null;li=dl.next(li)) {
			if((uri = dl.get(li)) == null) {
				System.out.println("Locator Item empty");
			} else {
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress(uri.getHost(),  uri.getPort()),3000);
					System.out.printf("Can Connect a Socket to %s %d\n",uri.getHost(),uri.getPort());
					try {
						socket.close();
					} catch (IOException e1) {
						System.out.printf("Could not close Socket Connection: %s\n",e1.getMessage());
					}
				} catch (IOException e) {
					System.out.printf("Cannot Connect a Socket to  %s %d: %s\n",uri.getHost(),uri.getPort(),e.getMessage());
				}
			}
		}
	}

	private static PropertyLocator servicePropLocator(String env) throws LocatorException {
		String purls;
		switch(env) {
			case "LOCAL":
				try {
					purls="https://"+InetAddress.getLocalHost().getHostName()+":8100";
				} catch (UnknownHostException e) {
					throw new LocatorException(e);
				}
				break;
			case "DEV":
				purls="https://aaf.dev.att.com:8100,https://aaf.dev.att.com:8101";
				break;
			case "TEST":
				purls="https://aaftest.test.att.com:8100,https://aaftest.test.att.com:8101";
				break;
			case "IST":
				purls="https://aafist.test.att.com:8100,https://aafist.test.att.com:8101";
				break;
			case PROD:
				purls="https://aaf.it.att.com:8100,https://aaf.it.att.com:8101";
				break;
			default:
				if(env.contains(".")) {
					purls="https://"+env+":8100";
				} else {
					throw new LocatorException(ConnectivityTest.class.getSimpleName() + ": unknown Env");
				}
		}
		System.out.printf("Creating a PropertyLocator for %s\n",purls);
		return new PropertyLocator(purls);
	}
	
	private static PropertyLocator proxyPropLocator(String env) throws LocatorException {
		String purls;
		switch(env) {
			case "LOCAL":
				try {
					purls="https://"+InetAddress.getLocalHost().getHostAddress()+":8100";
				} catch (UnknownHostException e) {
					throw new LocatorException(e);
				}
				break;
			case "DEV":
				purls="https://aaf.dev.att.com:8095/proxy";
				break;
			case "TEST":
				purls="https://aaftest.test.att.com:8095/proxy";
				break;
			case "IST":
				purls="https://aafist.test.att.com:8095/proxy";
				break;
			case PROD:
				purls="https://aaf.it.att.com:8095/proxy";
				break;
			default:
				if(env.contains(".")) {
					purls="https://"+env+":8095/proxy";
				} else {
					throw new LocatorException(ConnectivityTest.class.getSimpleName() + ": unknown Env");
				}

		}
		System.out.printf("Creating a PropertyLocator for %s\n",purls);
		return new PropertyLocator(purls);
	}
		
	


	private static void hClientTest(Locator<URI> dl, SecuritySetter<HttpURLConnection> ss, String user)  {
		try {
			URI uri = dl.get(dl.best());
			System.out.println("Resolved to: " + uri);
			HClient client = new HClient(ss, uri, 3000);
			client.setMethod("GET");
			client.setPathInfo("/authz/perms/user/"+user);
			client.send();
			Future<String> future = client.futureReadString();
			if(future.get(7000)) {
				System.out.println(future.body());	
			} else {
				System.out.println(future.code() + ":" + future.body());
			}
		} catch (CadiException | LocatorException | APIException e) {
			e.printStackTrace();
		}
	}


	private static void basicAuthTest(PropertyLocator dl, SecuritySetter<HttpURLConnection> ss) {
		try {
			URI uri = dl.get(dl.best());
			System.out.println("Resolved to: " + uri);
			HClient client = new HClient(ss, uri, 3000);
			client.setMethod("GET");
			client.setPathInfo("/authn/basicAuth");
			client.addHeader("Accept", "text/plain");
			client.send();
	
		
			Future<String> future = client.futureReadString();
			if(future.get(7000)) {
				System.out.println("BasicAuth Validated");	
			} else {
				System.out.println("Failure " + future.code() + ":" + future.body());
			}
		} catch (CadiException | LocatorException | APIException e) {
			e.printStackTrace();
		}
	}

	// Regular DME2Client Coding Style
	private static void dme2RawTest(DME2Manager dm, String aafurl, String user, String pass) {
		try { 
			if(dm==null) {
				return;
			}
			URI uri = new URI(aafurl);
			print(true,"DME2 Direct Client Coding Methodology",uri.toString());
			DME2Client client = dm.newClient( uri, 3000);
			client.setMethod("GET"); // FYI, DME2 defaults to "POST"
			client.setContext("/authz/perms/user/"+user); // DME2 direct requires separate setting of Context from URI
			if(pass!=null) { // rely on Cert if no pass
				client.setCredentials(user, pass);
			}
			client.setPayload(""); // DME2  will not send without something
			String resp = client.sendAndWait(7000);
			System.out.println(resp);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
}
