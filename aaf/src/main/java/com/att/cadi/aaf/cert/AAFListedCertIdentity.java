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
package com.att.cadi.aaf.cert;


import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.XMLGregorianCalendar;

import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.Hash;
import com.att.cadi.aaf.v2_0.AAFCon;
import com.att.cadi.client.Future;
import com.att.cadi.config.Config;
import com.att.cadi.principal.X509Principal;
import com.att.cadi.taf.cert.CertIdentity;
import com.att.cadi.taf.cert.X509Taf;
import com.att.inno.env.APIException;
import com.att.inno.env.util.Chrono;
import com.att.inno.env.util.Split;

import aaf.v2_0.Certs;
import aaf.v2_0.Certs.Cert;
import aaf.v2_0.Users;
import aaf.v2_0.Users.User;

public class AAFListedCertIdentity implements CertIdentity {
	//TODO should 8 hours be configurable? 
	private static final long EIGHT_HOURS = 1000*60*60*8; 
	private static final String AAF_VERSION = "2.0";
			
	private static Map<ByteArrayHolder,String> certs = null;
	
	// Did this to add other Trust Mechanisms
	// Trust mechanism set by Property: 
	private static final String[] authMechanisms = new String[] {"tguard","basicAuth","csp"};
	private static String[] certIDs;
	
	private static Map<String,Set<String>> trusted =null;

	public AAFListedCertIdentity(Access access, AAFCon<?> aafcon) throws APIException {
		synchronized(AAFListedCertIdentity.class) {
			if(certIDs==null) {
				String cip = access.getProperty(Config.AAF_CERT_IDS, null);
				if(cip!=null) {
					certIDs = Split.split(',',cip);
				}
			}
			if(certIDs!=null && certs==null) {
				TimerTask cu = new CertUpdate(aafcon);
				cu.run(); // want this to run in this thread first...
				new Timer("AAF Identity Refresh Timer",true).scheduleAtFixedRate(cu, EIGHT_HOURS,EIGHT_HOURS);
			}
		}
	}

	public static Set<String> trusted(String authMech) {
		return trusted.get(authMech);
	}
	
	public Principal identity(HttpServletRequest req, X509Certificate cert,	byte[] certBytes) throws CertificateException {
		if(cert==null && certBytes==null)return null;
		if(certBytes==null)certBytes = cert.getEncoded();
		byte[] fingerprint = X509Taf.getFingerPrint(certBytes);
		String id = certs.get(new ByteArrayHolder(fingerprint));
		if(id!=null) { // Caller is Validated
			return new X509Principal(id,cert,certBytes);
		}
		return null;
	}

	private static class ByteArrayHolder implements Comparable<ByteArrayHolder> {
		private byte[] ba;
		public ByteArrayHolder(byte[] ba) {
			this.ba = ba;
		}
		public int compareTo(ByteArrayHolder b) {
			return Hash.compareTo(ba, b.ba);
		}
	}
	
	private class CertUpdate extends TimerTask {

		private AAFCon<?> aafcon;
		public CertUpdate(AAFCon<?> con) {
			aafcon = con;
		}
		
		@Override
		public void run() {
			try {
				TreeMap<ByteArrayHolder, String> newCertsMap = new TreeMap<ByteArrayHolder,String>();
				Map<String,Set<String>> newTrustMap = new TreeMap<String,Set<String>>();
				Set<String> userLookup = new HashSet<String>();
				for(String s : certIDs) {
					userLookup.add(s);
				}
				for(String authMech : authMechanisms) {
					Future<Users> fusr = aafcon.client(AAF_VERSION).read("/authz/users/perm/com.att.aaf.trust/"+authMech+"/authenticate", Users.class, aafcon.usersDF);
					if(fusr.get(5000)) {
						List<User> users = fusr.value.getUser();
						if(users.isEmpty()) {
							aafcon.access.log(Level.WARN, "AAF Lookup-No IDs in Role com.att.aaf.trustForID <> "+authMech);
						} else {
							aafcon.access.log(Level.INFO,"Loading Trust Authentication Info for",authMech);
							Set<String> hsUser = new HashSet<String>();
							for(User u : users) {
								userLookup.add(u.getId());
								hsUser.add(u.getId());
							}
							newTrustMap.put(authMech,hsUser);
						}
					} else {
						aafcon.access.log(Level.WARN, "Could not get Users in Perm com.att.trust|tguard|authenticate",fusr.code(),fusr.body());
					}
					
				}
				
				for(String u : userLookup) {
					Future<Certs> fc = aafcon.client(AAF_VERSION).read("/authn/cert/id/"+u, Certs.class, aafcon.certsDF);
					XMLGregorianCalendar now = Chrono.timeStamp();
					if(fc.get(5000)) {
						List<Cert> certs = fc.value.getCert();
						if(certs.isEmpty()) {
							aafcon.access.log(Level.WARN, "No Cert Associations for",u);
						} else {
							for(Cert c : fc.value.getCert()) {
								XMLGregorianCalendar then =c.getExpires();
								if(then !=null && then.compare(now)>0) {
									newCertsMap.put(new ByteArrayHolder(c.getFingerprint()), c.getId());
									aafcon.access.log(Level.INIT,"Associating "+ c.getId() + " expiring " + Chrono.dateOnlyStamp(c.getExpires()) + " with " + c.getX500());
								}
							}
						}
					} else {
						aafcon.access.log(Level.WARN, "Could not get Certificates for",u);
					}
				}

				certs = newCertsMap;
				trusted = newTrustMap;
			} catch(Exception e) {
				aafcon.access.log(e, "Failure to update Certificate Identities from AAF");
			}
		}
	}
}
