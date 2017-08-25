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
package org.onap.aaf.cadi.config;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.onap.aaf.cadi.AbsUserCache;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CachingLur;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.CredVal;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.TrustChecker;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.lur.EpiLur;
import org.onap.aaf.cadi.lur.LocalLur;
import org.onap.aaf.cadi.lur.NullLur;
import org.onap.aaf.cadi.taf.HttpEpiTaf;
import org.onap.aaf.cadi.taf.HttpTaf;
import org.onap.aaf.cadi.taf.basic.BasicHttpTaf;
import org.onap.aaf.cadi.taf.cert.X509Taf;
import org.onap.aaf.cadi.taf.dos.DenialOfServiceTaf;

import java.util.Properties;
import java.util.TimerTask;

/**
 * Create a Consistent Configuration mechanism, even when configuration styles are as vastly different as
 * Properties vs JavaBeans vs FilterConfigs...
 * 
 *
 */
public class Config {

	private static final String HIDE_PASS = "***************";

	public static final String UTF_8 = "UTF-8";

	// Property Names associated with configurations.
	// As of 1.0.2, these have had the dots removed so as to be compatible with JavaBean style
	// configurations as well as property list style.
	public static final String HOSTNAME = "hostname";
	public static final String CADI_PROP_FILES = "cadi_prop_files"; // Additional Properties files (separate with ;)
	public static final String CADI_LOGLEVEL = "cadi_loglevel";
	public static final String CADI_LOGNAME = "cadi_logname";
	public static final String CADI_KEYFILE = "cadi_keyfile";
	public static final String CADI_KEYSTORE = "cadi_keystore";
	public static final String CADI_KEYSTORE_PASSWORD = "cadi_keystore_password";
	public static final String CADI_ALIAS = "cadi_alias";
	public static final String CADI_LOGINPAGE_URL = "cadi_loginpage_url";

	public static final String CADI_KEY_PASSWORD = "cadi_key_password";
	public static final String CADI_TRUSTSTORE = "cadi_truststore";
	public static final String CADI_TRUSTSTORE_PASSWORD = "cadi_truststore_password";
	public static final String CADI_X509_ISSUERS = "cadi_x509_issuers";
	public static final String CADI_TRUST_MASKS="cadi_trust_masks";
	public static final String CADI_TRUST_PERM="cadi_trust_perm"; //  IDs with this perm can utilize the "AS " user concept
	public static final String CADI_PROTOCOLS = "cadi_protocols";
	public static final String CADI_NOAUTHN = "cadi_noauthn";
	public static final String CADI_LOC_LIST = "cadi_loc_list";
	
	public static final String CADI_USER_CHAIN_TAG = "cadi_user_chain";
	public static final String CADI_USER_CHAIN = "USER_CHAIN";

	
	
	public static final String CSP_DOMAIN = "csp_domain";
	public static final String CSP_HOSTNAME = "csp_hostname";
	public static final String CSP_DEVL_LOCALHOST = "csp_devl_localhost";
	public static final String CSP_USER_HEADER = "CSP_USER";
	public static final String CSP_SYSTEMS_CONF = "CSPSystems.conf";
    public static final String CSP_SYSTEMS_CONF_FILE = "csp_systems_conf_file";


	public static final String TGUARD_ENV="tguard_env";
	public static final String TGUARD_DOMAIN = "tguard_domain";
	public static final String TGUARD_TIMEOUT = "tguard_timeout";
	public static final String TGUARD_TIMEOUT_DEF = "5000";
	public static final String TGUARD_CERTS = "tguard_certs"; // comma delimited SHA-256 finger prints
//	public static final String TGUARD_DEVL_LOCALHOST = "tguard_devl_localhost";
//	public static final String TGUARD_USER_HEADER = "TGUARD_USER";

	public static final String LOCALHOST_ALLOW = "localhost_allow";
	public static final String LOCALHOST_DENY = "localhost_deny";
	
	public static final String BASIC_REALM = "basic_realm";  // what is sent to the client 
	public static final String BASIC_WARN = "basic_warn";  // Warning of insecure channel 
	public static final String USERS = "local_users";
	public static final String GROUPS = "local_groups";
	public static final String WRITE_TO = "local_writeto"; // dump RBAC to local file in Tomcat Style (some apps use)
	
	public static final String AAF_ENV = "aaf_env";
	public static final String AAF_ROOT_NS = "aaf_root_ns";
	public static final String AAF_ROOT_COMPANY = "aaf_root_company";
	public static final String AAF_URL = "aaf_url"; //URL for AAF... Use to trigger AAF configuration
	public static final String AAF_MECHID = "aaf_id";
	public static final String AAF_MECHPASS = "aaf_password";
	public static final String AAF_LUR_CLASS = "aaf_lur_class";
	public static final String AAF_TAF_CLASS = "aaf_taf_class";
	public static final String AAF_CONNECTOR_CLASS = "aaf_connector_class";
	public static final String AAF_LOCATOR_CLASS = "aaf_locator_class";
	public static final String AAF_CONN_TIMEOUT = "aaf_conn_timeout";
	public static final String AAF_CONN_TIMEOUT_DEF = "3000";
	public static final String AAF_READ_TIMEOUT = "aaf_timeout";
	public static final String AAF_READ_TIMEOUT_DEF = "5000";
	public static final String AAF_USER_EXPIRES = "aaf_user_expires";
	public static final String AAF_USER_EXPIRES_DEF = "600000"; // Default is 10 mins
	public static final String AAF_CLEAN_INTERVAL = "aaf_clean_interval";
	public static final String AAF_CLEAN_INTERVAL_DEF = "30000"; // Default is 30 seconds
	public static final String AAF_REFRESH_TRIGGER_COUNT = "aaf_refresh_trigger_count";
	public static final String AAF_REFRESH_TRIGGER_COUNT_DEF = "3"; // Default is 10 mins
	
	public static final String AAF_HIGH_COUNT = "aaf_high_count";
	public static final String AAF_HIGH_COUNT_DEF = "1000"; // Default is 1000 entries
	public static final String AAF_PERM_MAP = "aaf_perm_map";
	public static final String AAF_DEPLOYED_VERSION = "DEPLOYED_VERSION";
	public static final String AAF_CERT_IDS = "aaf_cert_ids";
	public static final String AAF_DEBUG_IDS = "aaf_debug_ids"; // comma delimited
	
	public static final String GW_URL = "gw_url";
	public static final String CM_URL = "cm_url";
	public static final String CM_TRUSTED_CAS = "cm_trusted_cas";

	public static final String PATHFILTER_URLPATTERN = "pathfilter_urlpattern";
	public static final String PATHFILTER_STACK = "pathfilter_stack";
	public static final String PATHFILTER_NS = "pathfilter_ns";
	public static final String PATHFILTER_NOT_AUTHORIZED_MSG = "pathfilter_not_authorized_msg";

	public static final String AFT_DME2_TRUSTSTORE_PASSWORD = "AFT_DME2_TRUSTSTORE_PASSWORD";
	public static final String AFT_DME2_TRUSTSTORE = "AFT_DME2_TRUSTSTORE";
	public static final String AFT_DME2_KEYSTORE_PASSWORD = "AFT_DME2_KEYSTORE_PASSWORD";
	public static final String AFT_DME2_KEY_PASSWORD = "AFT_DME2_KEY_PASSWORD";
	public static final String AFT_DME2_KEYSTORE = "AFT_DME2_KEYSTORE";
	public static final String AFT_DME2_SSL_TRUST_ALL = "AFT_DME2_SSL_TRUST_ALL";
	public static final String AFT_DME2_SSL_INCLUDE_PROTOCOLS = "AFT_DME2_SSL_INCLUDE_PROTOCOLS";


	// DME2 Client.  First property must be set to "false", and the others set in order to use SSL Client
	public static final String AFT_DME2_CLIENT_IGNORE_SSL_CONFIG="AFT_DME2_CLIENT_IGNORE_SSL_CONFIG";
	public static final String AFT_DME2_CLIENT_KEYSTORE = "AFT_DME2_CLIENT_KEYSTORE";
	public static final String AFT_DME2_CLIENT_KEYSTORE_PASSWORD = "AFT_DME2_CLIENT_KEYSTORE_PASSWORD";
	public static final String AFT_DME2_CLIENT_TRUSTSTORE = "AFT_DME2_CLIENT_TRUSTSTORE";
	public static final String AFT_DME2_CLIENT_TRUSTSTORE_PASSWORD = "AFT_DME2_CLIENT_TRUSTSTORE_PASSWORD";
	public static final String AFT_DME2_CLIENT_SSL_CERT_ALIAS = "AFT_DME2_CLIENT_SSL_CERT_ALIAS"; 
	public static final String AFT_DME2_CLIENT_SSL_INCLUDE_PROTOCOLS = "AFT_DME2_CLIENT_SSL_INCLUDE_PROTOCOLS";

	
	// This one should go unpublic
	public static final String AAF_DEFAULT_REALM = "aaf_default_realm";
	private static String defaultRealm="none";

	public static final String AAF_DOMAIN_SUPPORT = "aaf_domain_support";
	//public static final String AAF_DOMAIN_SUPPORT_DEF = ".com";
	public static final String AAF_DOMAIN_SUPPORT_DEF = ".org";


	public static void setDefaultRealm(Access access) throws CadiException {
		try {
			boolean hasCSP;
			try {
				Class.forName("com.att.cadi.taf.csp.CSPTaf");
				hasCSP=true;
			} catch(ClassNotFoundException e) {
				hasCSP = logProp(access,Config.CSP_DOMAIN, null)!=null;
			}
			defaultRealm = logProp(access,Config.AAF_DEFAULT_REALM,
					hasCSP?"csp.att.com":
					logProp(access,Config.BASIC_REALM,
						logProp(access,HOSTNAME,InetAddress.getLocalHost().getHostName())
						)
					);
		} catch (UnknownHostException e) {
			//defaultRealm="none";
		}
	}
	

	public static HttpTaf configHttpTaf(Access access, TrustChecker tc, CredVal up, Lur lur, Object ... additionalTafLurs) throws CadiException {
		/////////////////////////////////////////////////////
		// Setup AAFCon for any following
		/////////////////////////////////////////////////////
		Object aafcon = null;
		if(lur != null) {
			Field f = null;
			try {
				f = lur.getClass().getField("aaf");
				aafcon = f.get(lur);
			} catch (Exception nsfe) {
			}
		}
		// IMPORTANT!  Don't attempt to load AAF Connector if there is no AAF URL
		String aafURL = access.getProperty(AAF_URL,null);
		if(aafcon==null && aafURL!=null) {
			aafcon = loadAAFConnector(access, aafURL);	
		}
		
		HttpTaf taf;
		// Setup Host, in case Network reports an unusable Hostname (i.e. VTiers, VPNs, etc)
		String hostname = logProp(access, HOSTNAME,null);
		if(hostname==null) {
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e1) {
				throw new CadiException("Unable to determine Hostname",e1);
			}
		}
		
		access.log(Level.INIT, "Hostname set to",hostname);
		// Get appropriate TAFs
		ArrayList<HttpTaf> htlist = new ArrayList<HttpTaf>();

		/////////////////////////////////////////////////////
		// Add a Denial of Service TAF
		// Note: how IPs and IDs are added are up to service type.
		// They call "DenialOfServiceTaf.denyIP(String) or denyID(String)
		/////////////////////////////////////////////////////
		htlist.add(new DenialOfServiceTaf(access));

		/////////////////////////////////////////////////////
		// Configure LocalHost 
		/////////////////////////////////////////////////////
		
		String truststore = logProp(access, CADI_TRUSTSTORE, access.getProperty("AFT_DME2_TRUSTSTORE", null));
		if(truststore!=null) {
			String truststore_pwd = access.getProperty(CADI_TRUSTSTORE_PASSWORD, access.getProperty("AFT_DME2_TRUSTSTORE_PASSWORD",null));
			if(truststore_pwd!=null) {
				if(truststore_pwd.startsWith(Symm.ENC)) {
					try {
						truststore_pwd = access.decrypt(truststore_pwd,false);
					} catch (IOException e) {
						throw new CadiException(CADI_TRUSTSTORE_PASSWORD + " cannot be decrypted",e);
					}
				}
				try {
					htlist.add(new X509Taf(access,lur));
					access.log(Level.INIT,"Certificate Authorization enabled");
				} catch (SecurityException e) {
					access.log(Level.INIT,"AAFListedCertIdentity cannot be instantiated. Certificate Authorization is now disabled",e);
				} catch (IllegalArgumentException e) {
					access.log(Level.INIT,"AAFListedCertIdentity cannot be instantiated. Certificate Authorization is now disabled",e);
				} catch (CertificateException e) {
					access.log(Level.INIT,"Certificate Authorization failed, it is disabled",e);
				} catch (NoSuchAlgorithmException e) {
					access.log(Level.INIT,"Certificate Authorization failed, wrong Security Algorithm",e);
				}
			}
		} else {
			access.log(Level.INIT,"Certificate Authorization not enabled");
		}
		
		/////////////////////////////////////////////////////
		// Configure Basic Auth (local content)
		/////////////////////////////////////////////////////
		String basic_realm = logProp(access, BASIC_REALM,null);
		boolean basic_warn = "TRUE".equals(access.getProperty(BASIC_WARN,"FALSE"));
		if(basic_realm!=null && up!=null) {
			access.log(Level.INIT,"Basic Authorization is enabled using realm",basic_realm);
			// Allow warning about insecure channel to be turned off
			if(!basic_warn)access.log(Level.INIT,"WARNING! The basic_warn property has been set to false.",
					" There will be no additional warning if Basic Auth is used on an insecure channel"
					);
			String aafCleanup = logProp(access, AAF_USER_EXPIRES,AAF_USER_EXPIRES_DEF); // Default is 10 mins
			long userExp = Long.parseLong(aafCleanup);

			htlist.add(new BasicHttpTaf(access, up, basic_realm, userExp, basic_warn));
		} else {
			access.log(Level.INIT,"Local Basic Authorization is disabled.  Enable by setting basic_realm=<appropriate realm, i.e. my.att.com>");
		}
		
		/////////////////////////////////////////////////////
		// Configure AAF Driven Basic Auth
		/////////////////////////////////////////////////////
		boolean getRemoteAAF = true;
		if(additionalTafLurs!=null) {
			for(Object o : additionalTafLurs) {
				if(o.getClass().getSimpleName().equals("DirectAAFLur")) {
					getRemoteAAF = false;
					break;
				}
			}
		}
		HttpTaf aaftaf=null;
		if(getRemoteAAF) {
			if(aafcon==null) {
				access.log(Level.INIT,"AAF Connection (AAFcon) is null.  Cannot create an AAF TAF");
			} else if(aafURL==null) {
				access.log(Level.INIT,"No AAF URL in properties, Cannot create an AAF TAF");
			} else {// There's an AAF_URL... try to configure an AAF 
				String defName = aafURL.contains("version=2.0")?"com.att.cadi.aaf.v2_0.AAFTaf":"";
				String aafTafClassName = logProp(access, AAF_TAF_CLASS,defName);
				// Only 2.0 available at this time
				if("com.att.cadi.aaf.v2_0.AAFTaf".equals(aafTafClassName)) { 
					try {
						Class<?> aafTafClass = loadClass(access,aafTafClassName);
						Class<?> aafConClass = loadClass(access,"com.att.cadi.aaf.v2_0.AAFCon");
	
						Constructor<?> cstr = aafTafClass.getConstructor(aafConClass,boolean.class,AbsUserCache.class);
						if(cstr!=null) {
							aaftaf = (HttpTaf)cstr.newInstance(aafcon,basic_warn,lur);
							if(aaftaf==null) {
								access.log(Level.INIT,"ERROR! AAF TAF Failed construction.  NOT Configured");
							} else {
								access.log(Level.INIT,"AAF TAF Configured to ",aafURL);
								// Note: will add later, after all others configured
							}
						}
					} catch(Exception e) {
						access.log(Level.INIT,"ERROR! AAF TAF Failed construction.  NOT Configured");
					}
				}
			}
		}
		
		
		String alias = logProp(access, CADI_ALIAS,null);

		/////////////////////////////////////////////////////
		// Configure tGuard... (AT&T Client Repo)
		/////////////////////////////////////////////////////
		// TGUARD Environment, translated to any other remote Environment validation mechanism...
		String tGuard_domain = logProp(access, TGUARD_DOMAIN,null);
		String tGuard_env = logProp(access, TGUARD_ENV, null);

		if(!("PROD".equals(tGuard_env) || "STAGE".equals(tGuard_env))) {
			access.log(Level.INIT, "tGuard Authorization is disabled.  Enable by setting", TGUARD_ENV, "to \"PROD\" or \"STAGE\"");
		} else if(tGuard_domain==null) {
			access.log(Level.INIT,TGUARD_DOMAIN + " must be set:  tGuard Authorization is disabled.");
		} else if(alias == null) {
			access.log(Level.INIT,CADI_ALIAS + " must be set:  tGuard Authorization is disabled.");
		} else {
			try {
				Class<?> tGuardClass = loadClass(access,"com.att.cadi.tguard.TGuardHttpTaf");
				if(aaftaf!=null) {
					Constructor<?> tGuardCnst = tGuardClass.getConstructor(new Class[]{Access.class, AbsUserCache.class});
					htlist.add((HttpTaf)tGuardCnst.newInstance(new Object[] {access,aaftaf}));
					access.log(Level.INIT,"tGuard Authorization is enabled on",tGuard_env,"on the",tGuard_domain," tGuard Domain");
				} else {
					Constructor<?> tGuardCnst = tGuardClass.getConstructor(new Class[]{Access.class, int.class, int.class, int.class});
					htlist.add((HttpTaf)tGuardCnst.newInstance(new Object[] {
							access,
							Integer.parseInt(logProp(access, AAF_CLEAN_INTERVAL,AAF_CLEAN_INTERVAL_DEF)),
							Integer.parseInt(logProp(access, AAF_HIGH_COUNT, AAF_HIGH_COUNT_DEF)),
							Integer.parseInt(logProp(access, AAF_REFRESH_TRIGGER_COUNT, AAF_REFRESH_TRIGGER_COUNT_DEF))
							}));
					access.log(Level.INIT,"tGuard Authorization is enabled on",tGuard_env,"on the",tGuard_domain," tGuard Domain");
				}
			} catch(Exception e) {
				access.log(e, Level.INIT,"tGuard Class cannot be loaded:  tGuard Authorization is disabled.");
			}
		}
		
		/////////////////////////////////////////////////////
		// Adding BasicAuth (AAF) last, after other primary Cookie Based
		// Needs to be before Cert... see below
		/////////////////////////////////////////////////////
		if(aaftaf!=null) {
			htlist.add(aaftaf);
		}


		/////////////////////////////////////////////////////
		// Any Additional Lurs passed in Constructor
		/////////////////////////////////////////////////////
		if(additionalTafLurs!=null) {
			for(Object additional : additionalTafLurs) {
				if(additional instanceof HttpTaf) {
					htlist.add((HttpTaf)additional);
					access.log(Level.INIT,additional);
				}
			}
		}

		/////////////////////////////////////////////////////
		// Create EpiTaf from configured TAFs
		/////////////////////////////////////////////////////
		if(htlist.size()==1) {
			// just return the one
			taf = htlist.get(0);
		} else {
			HttpTaf[] htarray = new HttpTaf[htlist.size()];
			htlist.toArray(htarray);
			Locator<URI> locator = loadLocator(access, logProp(access, CADI_LOGINPAGE_URL, null));
			
			taf = new HttpEpiTaf(access,locator, tc, htarray); // ok to pass locator == null
			String level = logProp(access, CADI_LOGLEVEL, null);
			if(level!=null) {
				access.setLogLevel(Level.valueOf(level));
			}
		}
		
		return taf;
	}
	
	public static String logProp(Access access,String tag, String def) {
		String rv = access.getProperty(tag, def);
		if(rv == null) {
			access.log(Level.INIT,tag,"is not set");
		} else {
			access.log(Level.INIT,tag,"is set to",rv);
		}
		return rv;
	}
	
	public static Lur configLur(Access access, Object ... additionalTafLurs) throws CadiException {
		List<Lur> lurs = new ArrayList<Lur>();
		
		/////////////////////////////////////////////////////
		// Configure a Local Property Based RBAC/LUR
		/////////////////////////////////////////////////////
		try {
			String users = access.getProperty(USERS,null);
			String groups = access.getProperty(GROUPS,null);

			if(groups!=null || users!=null) {
				LocalLur ll;
				lurs.add(ll = new LocalLur(access, users, groups)); // note b64==null is ok.. just means no encryption.
				
				String writeto = access.getProperty(WRITE_TO,null);
				if(writeto!=null) {
					String msg = UsersDump.updateUsers(writeto, ll);
					if(msg!=null) access.log(Level.INIT,"ERROR! Error Updating ",writeto,"with roles and users:",msg);
				}
			}
		} catch (IOException e) {
			throw new CadiException(e);
		}
		
		/////////////////////////////////////////////////////
		// Configure the AAF Lur (if any)
		/////////////////////////////////////////////////////
		String aafURL = logProp(access,AAF_URL,null); // Trigger Property
		String aaf_env = access.getProperty(AAF_ENV,null);
		if(aaf_env == null && aafURL!=null && access instanceof PropAccess) { // set AAF_ENV from AAF_URL
			int ec = aafURL.indexOf("envContext=");
			if(ec>0) {
				ec += 11; // length of envContext=
				int slash = aafURL.indexOf('/', ec);
				if(slash>0) {
					aaf_env = aafURL.substring(ec, slash);
					((PropAccess)access).setProperty(AAF_ENV, aaf_env);
					access.printf(Level.INIT, "Setting aaf_env to %s from aaf_url value",aaf_env);
				}
			}
		}
			
		if(aafURL==null) {
			access.log(Level.INIT,"No AAF LUR properties, AAF will not be loaded");
		} else {// There's an AAF_URL... try to configure an AAF
			String aafLurClassStr = logProp(access,AAF_LUR_CLASS,"com.att.cadi.aaf.v2_0.AAFLurPerm");
			////////////AAF Lur 2.0 /////////////
			if(aafLurClassStr.startsWith("com.att.cadi.aaf.v2_0")) { 
				try {
					Object aafcon = loadAAFConnector(access, aafURL);
					if(aafcon==null) {
						access.log(Level.INIT,"AAF LUR class,",aafLurClassStr,"cannot be constructed without valid AAFCon object.");
					} else {
						Class<?> aafAbsAAFCon = loadClass(access, "com.att.cadi.aaf.v2_0.AAFCon");
						Method mNewLur = aafAbsAAFCon.getMethod("newLur");
						Object aaflur = mNewLur.invoke(aafcon);
	
						if(aaflur==null) {
							access.log(Level.INIT,"ERROR! AAF LUR Failed construction.  NOT Configured");
						} else {
							access.log(Level.INIT,"AAF LUR Configured to ",aafURL);
							lurs.add((Lur)aaflur);
							String debugIDs = logProp(access,Config.AAF_DEBUG_IDS, null);
							if(debugIDs !=null && aaflur instanceof CachingLur) {
								((CachingLur<?>)aaflur).setDebug(debugIDs);
							}
						}
					}
				} catch (Exception e) {
					access.log(e,"AAF LUR class,",aafLurClassStr,"could not be constructed with given Constructors.");
				}
			} 
		} 

		/////////////////////////////////////////////////////
		// Any Additional passed in Constructor
		/////////////////////////////////////////////////////
		if(additionalTafLurs!=null) {
			for(Object additional : additionalTafLurs) {
				if(additional instanceof Lur) {
					lurs.add((Lur)additional);
					access.log(Level.INIT, additional);
				}
			}
		}

		/////////////////////////////////////////////////////
		// Return a Lur based on how many there are... 
		/////////////////////////////////////////////////////
		switch(lurs.size()) {
			case 0: 
				access.log(Level.INIT,"WARNING! No CADI LURs configured");
				// Return a NULL Lur that does nothing.
				return new NullLur();
			case 1:
				return lurs.get(0); // Only one, just return it, save processing
			default:
				// Multiple Lurs, use EpiLUR to handle
				Lur[] la = new Lur[lurs.size()];
				lurs.toArray(la);
				return new EpiLur(la);
		}
	}
	
	private static final String COM_ATT_CADI_AAF_V2_0_AAF_CON_DME2 = "com.att.cadi.aaf.v2_0.AAFConDME2";
	private static final String COM_ATT_CADI_AAF_V2_0_AAF_CON_HTTP = "com.att.cadi.aaf.v2_0.AAFConHttp";
	public static Object loadAAFConnector(Access access, String aafURL) {
		Object aafcon = null;
		Class<?> aafConClass = null;

		try {
			if(aafURL!=null) {
				String aafConnector = access.getProperty(AAF_CONNECTOR_CLASS, COM_ATT_CADI_AAF_V2_0_AAF_CON_HTTP);
				if(COM_ATT_CADI_AAF_V2_0_AAF_CON_DME2.equals(aafConnector) || aafURL.contains("/service=")) {
					aafConClass = loadClass(access, COM_ATT_CADI_AAF_V2_0_AAF_CON_DME2);
					if(aafConClass!=null) {
						Constructor<?> cons = aafConClass.getConstructor(PropAccess.class);
						aafcon = cons.newInstance(access);
					} else {
						access.log(Level.ERROR, "URL contains '/service=', which requires DME2");
					}
				} else if(COM_ATT_CADI_AAF_V2_0_AAF_CON_HTTP.equals(aafConnector)) {
					aafConClass = loadClass(access, COM_ATT_CADI_AAF_V2_0_AAF_CON_HTTP);
					for(Constructor<?> c : aafConClass.getConstructors()) {
						List<Object> lo = new ArrayList<Object>();
						for(Class<?> pc : c.getParameterTypes()) {
							if(pc.equals(PropAccess.class)) {
								lo.add(access);
							} else if(pc.equals(Locator.class)) {
								lo.add(loadLocator(access, aafURL));
							} else {
								continue;
							}
						}
						if(c.getParameterTypes().length!=lo.size()) {
							continue; // back to another Constructor
						} else {
							aafcon = c.newInstance(lo.toArray());
						}
						break;
					}
				}
				if(aafcon!=null) {
					String mechid = logProp(access,Config.AAF_MECHID, null);
					String pass = access.getProperty(Config.AAF_MECHPASS, null);
					if(mechid!=null && pass!=null) {
						try {
							Method basicAuth = aafConClass.getMethod("basicAuth", String.class, String.class);
							basicAuth.invoke(aafcon, mechid,pass);
						} catch (NoSuchMethodException nsme) {
							// it's ok, don't use
						}
					}
				}
			}
		} catch (Exception e) {
			access.log(e,"AAF Connector could not be constructed with given Constructors.");
		}
		
		return aafcon;
	}

	public static Class<?> loadClass(Access access, String className) {
		Class<?> cls=null;
		try {
			cls = access.classLoader().loadClass(className);
		} catch (ClassNotFoundException cnfe) {
			try {
				cls = access.getClass().getClassLoader().loadClass(className);
			} catch (ClassNotFoundException cnfe2) {
				// just return null
			}
		}
		return cls;
	}

	@SuppressWarnings("unchecked")
	public static Locator<URI> loadLocator(Access access, String url) {
		Locator<URI> locator = null;
		if(url==null) {
			access.log(Level.INIT,"No URL for AAF Login Page. Disabled");
		} else {
			if(url.contains("DME2RESOLVE")) {
				try {
					Class<?> lcls = loadClass(access,"com.att.cadi.locator.DME2Locator");
					Class<?> dmcls = loadClass(access,"com.att.aft.dme2.api.DME2Manager");
					Constructor<?> cnst = lcls.getConstructor(new Class[] {Access.class,dmcls,String.class});
					locator = (Locator<URI>)cnst.newInstance(new Object[] {access,null,url});
					access.log(Level.INFO, "DME2Locator enabled with " + url);
				} catch (Exception e) {
					access.log(Level.INIT,"AAF Login Page accessed by " + url + " requires DME2. It is now disabled",e);
				}
			} else {
				try {
					Class<?> cls = loadClass(access,"com.att.cadi.locator.PropertyLocator");
					Constructor<?> cnst = cls.getConstructor(new Class[] {String.class});
					locator = (Locator<URI>)cnst.newInstance(new Object[] {url});
					access.log(Level.INFO, "PropertyLocator enabled with " + url);
				} catch (Exception e) {
					access.log(Level.INIT,"AAF Login Page accessed by " + url + " requires PropertyLocator. It is now disabled",e);
				}
			}
		}
		return locator;
	}

	/*
	 * DME2 can only read Passwords as clear text properties.  Leaving in "System Properties" un-encrypted exposes these passwords
	 */
	public static class PasswordRemoval extends TimerTask {
		private Access access;
		
		private final List<String> pws;

		public PasswordRemoval(Access access) {
			this.access = access;
			pws = new ArrayList<String>();
		}
		
		@Override
		public void run() {
			for(String key:pws) {
				access.log(Level.INIT, "Scrubbing " + key);
				System.clearProperty(key);
			}
		}		
		public void add(String key) {
			pws.add(key);
		}
	}

	private static final String Y = "Y";

	private static String[][] CONVERTER_STRINGS=new String[][] {
			{AFT_DME2_KEYSTORE,CADI_KEYSTORE,null},
			{AFT_DME2_KEYSTORE_PASSWORD,CADI_KEYSTORE_PASSWORD,null},
			{AFT_DME2_KEY_PASSWORD,CADI_KEY_PASSWORD,null},
			{AFT_DME2_TRUSTSTORE,CADI_TRUSTSTORE,null},
			{AFT_DME2_TRUSTSTORE_PASSWORD,CADI_TRUSTSTORE_PASSWORD,null},
			{AFT_DME2_CLIENT_KEYSTORE,CADI_KEYSTORE,null},
			{AFT_DME2_CLIENT_KEYSTORE_PASSWORD,CADI_KEYSTORE_PASSWORD,null},
			{AFT_DME2_CLIENT_TRUSTSTORE,CADI_TRUSTSTORE,null},
			{AFT_DME2_CLIENT_TRUSTSTORE_PASSWORD,CADI_TRUSTSTORE_PASSWORD,null},
			{AFT_DME2_CLIENT_SSL_CERT_ALIAS,CADI_ALIAS,null},
			{AFT_DME2_CLIENT_SSL_INCLUDE_PROTOCOLS,CADI_PROTOCOLS,null},
			{"AFT_DME2_HOSTNAME",HOSTNAME,null},
			{"AFT_LATITUDE",null,Y},
			{"AFT_LONGITUDE",null,Y},
			{"AFT_ENVIRONMENT",null,Y},
			{"SCLD_PLATFORM",null,Y},
			{"DME2_EP_REGISTRY_CLASS",null,Y},// for Developer local access
			{"AFT_DME2_EP_REGISTRY_FS_DIR",null,Y},
			{"DME2.DEBUG",null,null},
			{"AFT_DME2_HTTP_EXCHANGE_TRACE_ON",null,null},
			{"AFT_DME2_SSL_ENABLE",null,null},
			{"AFT_DME2_SSL_WANT_CLIENT_AUTH",null,null},
			{AFT_DME2_SSL_INCLUDE_PROTOCOLS,CADI_PROTOCOLS,null},
			{"AFT_DME2_SSL_VALIDATE_CERTS",null,null},
			{AFT_DME2_CLIENT_IGNORE_SSL_CONFIG,null,null},
			{"https.protocols",CADI_PROTOCOLS,Y},
			};



	public static Properties getDME2Props(PropAccess access) {
		Properties dprops = new Properties();
		String value = null;
		boolean reqClientConfig = false;
		for(String[] row : CONVERTER_STRINGS) {
			value = access.getProperty(row[0],null);
			if(value==null) {
				value = System.getProperty(row[0]);
				if(value==null && row[1]!=null) {
					value = access.getProperty(row[1],null);
					if(value == null) {
						value = System.getProperty(row[1]);
					}
				}
			}
			if(value!=null) {
				if(row[0].contains("_SSL_")) {
					reqClientConfig = true;
				}
				if(row[0].startsWith("AFT") || row[0].startsWith("SCLD") || row[0].contains("DME2")) {
					if(value.startsWith("enc:")) {
						try {
							value = access.decrypt(value, true);
						} catch (IOException e) {
							access.log(Level.ERROR, e);
						}
						System.setProperty(row[0], value);
					} else if(Y.equals(row[2])) {
						System.setProperty(row[0], value);
						dprops.setProperty(row[0], value);
					} else if(row[0].contains("PASSWORD") || row[0].contains("STORE")) {
						System.setProperty(row[0], value);
					} else {
						dprops.setProperty(row[0], value);
					}
				}
				
			}
			
		}
		
		Properties sprops = System.getProperties();
		if(reqClientConfig && sprops.getProperty(AFT_DME2_CLIENT_IGNORE_SSL_CONFIG)==null) {
			sprops.put(AFT_DME2_CLIENT_IGNORE_SSL_CONFIG, "false");
			replaceKeyWithTrust(sprops,AFT_DME2_KEYSTORE,AFT_DME2_TRUSTSTORE);
			replaceKeyWithTrust(sprops,AFT_DME2_KEYSTORE_PASSWORD,AFT_DME2_TRUSTSTORE_PASSWORD);
			replaceKeyWithTrust(sprops,AFT_DME2_CLIENT_KEYSTORE,AFT_DME2_CLIENT_TRUSTSTORE);
			replaceKeyWithTrust(sprops,AFT_DME2_CLIENT_KEYSTORE_PASSWORD,AFT_DME2_CLIENT_TRUSTSTORE_PASSWORD);
		}
		
		if(sprops.getProperty(AFT_DME2_CLIENT_SSL_INCLUDE_PROTOCOLS)==null) {
			sprops.setProperty(AFT_DME2_CLIENT_SSL_INCLUDE_PROTOCOLS, access.getProperty(CADI_PROTOCOLS,SecurityInfo.HTTPS_PROTOCOLS_DEFAULT));
		}

		if(sprops.getProperty(AFT_DME2_SSL_INCLUDE_PROTOCOLS)==null) {
			sprops.setProperty(AFT_DME2_SSL_INCLUDE_PROTOCOLS, access.getProperty(CADI_PROTOCOLS,SecurityInfo.HTTPS_PROTOCOLS_DEFAULT));
		}
		
		if(access.willLog(Level.DEBUG)) {
			if(access instanceof PropAccess) {
				access.log(Level.DEBUG,"Access Properties");
				for(Entry<Object, Object> es : ((PropAccess)access).getProperties().entrySet()) {
					access.printf(Level.DEBUG,"    %s=%s",es.getKey().toString(),es.getValue().toString());
				}
			}
			access.log(Level.DEBUG,"DME2 Properties()");
			for(Entry<Object, Object> es : dprops.entrySet()) {
				value = es.getValue().toString();
				if(es.getKey().toString().contains("PASS")) {
					if(value==null || !value.contains("enc:")) {
						value = HIDE_PASS;
					}
				}
				access.printf(Level.DEBUG,"    %s=%s",es.getKey().toString(),value);
			}
			
			access.log(Level.DEBUG,"System (AFT) Properties");
			for(Entry<Object, Object> es : System.getProperties().entrySet()) {
				if(es.getKey().toString().startsWith("AFT")) {
					value = es.getValue().toString();
					if(es.getKey().toString().contains("PASS")) {
						if(value==null || !value.contains("enc:")) {
							value = HIDE_PASS;
						}
					}
					access.printf(Level.DEBUG,"    %s=%s",es.getKey().toString(),value);
				}
			}
		}
		// Cover any not specific AFT props
		String key;
		for(Entry<Object, Object> es : access.getProperties().entrySet()) {
			if((key=es.getKey().toString()).startsWith("AFT_") && 
					!key.contains("PASSWORD") &&
					dprops.get(key)==null) {
				dprops.put(key, es.getValue());
			}
		}
		return dprops;
	}
	
	private static void replaceKeyWithTrust(Properties props, String ks, String ts) {
		String value;
		if(props.get(ks)==null && (value=props.getProperty(ts))!=null) {
			props.put(ks,value);
			props.remove(ts);
		}
	}
	// Set by CSP, or is hostname.
	public static String getDefaultRealm() {
		return defaultRealm;
	}

}
