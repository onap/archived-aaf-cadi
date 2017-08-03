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
package com.att.cadi.taf.cert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.TrustManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.CachedPrincipal;
import com.att.cadi.CachedPrincipal.Resp;
import com.att.cadi.CadiException;
import com.att.cadi.Lur;
import com.att.cadi.Symm;
import com.att.cadi.Taf.LifeForm;
import com.att.cadi.config.Config;
import com.att.cadi.config.SecurityInfoC;
import com.att.cadi.config.SecurityInfo;
import com.att.cadi.lur.LocalPermission;
import com.att.cadi.principal.TGuardPrincipal;
import com.att.cadi.principal.X509Principal;
import com.att.cadi.taf.HttpTaf;
import com.att.cadi.taf.TafResp;
import com.att.cadi.taf.TafResp.RESP;
import com.att.cadi.util.Split;

public class X509Taf implements HttpTaf {
	
	public static final CertificateFactory certFactory;
	public static final MessageDigest messageDigest;
	public static final TrustManagerFactory tmf;
	private Access access;
	private CertIdentity[] certIdents;
	private Lur lur;
	private ArrayList<String> cadiIssuers;
	private String env;
	private SecurityInfo si;

	static {
		try {
			certFactory = CertificateFactory.getInstance("X.509");
			messageDigest = MessageDigest.getInstance("SHA-256"); // use this to clone
			tmf = TrustManagerFactory.getInstance(SecurityInfoC.SslKeyManagerFactoryAlgorithm);
		} catch (Exception e) {
			throw new RuntimeException("X.509 and SHA-256 are required for X509Taf",e);
		}
	}
	
	public X509Taf(Access access, Lur lur, CertIdentity ... cis) throws CertificateException, NoSuchAlgorithmException, CadiException {
		this.access = access;
		env = access.getProperty(Config.AAF_ENV,null);
		if(env==null) {
			throw new CadiException("X509Taf requires Environment ("+Config.AAF_ENV+") to be set.");
		}
		this.lur = lur;
		this.cadiIssuers = new ArrayList<String>();
		for(String ci : access.getProperty(Config.CADI_X509_ISSUERS, "CN=ATT CADI Issuing CA 01, OU=CSO, O=ATT, C=US:CN=ATT CADI Issuing CA 02, OU=CSO, O=ATT, C=US").split(":")) {
			cadiIssuers.add(ci);
		}
		try {
			Class<?> dci = access.classLoader().loadClass("com.att.authz.cadi.DirectCertIdentity");
			CertIdentity temp[] = new CertIdentity[cis.length+1];
			System.arraycopy(cis, 0, temp, 1, cis.length);
			temp[0] = (CertIdentity) dci.newInstance();
			certIdents=temp;
		} catch (Exception e) {
			certIdents = cis;
		}
		
		try {
			si = new SecurityInfo(access);
		} catch (GeneralSecurityException | IOException e1) {
			throw new CadiException(e1);
		}
	}

	public static final X509Certificate getCert(byte[] certBytes) throws CertificateException {
		ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
		return (X509Certificate)certFactory.generateCertificate(bais);
	}

	public static final byte[] getFingerPrint(byte[] ba) {
		MessageDigest md;
		try {
			md = (MessageDigest)messageDigest.clone();
		} catch (CloneNotSupportedException e) {
			// should never get here
			return new byte[0];
		}
		md.update(ba);
		return md.digest();
	}

	public TafResp validate(LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
		// Check for Mutual SSL
		try {
			X509Certificate[] certarr = (X509Certificate[])req.getAttribute("javax.servlet.request.X509Certificate");
			if(certarr!=null && certarr.length>0) {
				si.checkClientTrusted(certarr);
				// Note: If the Issuer is not in the TrustStore, it's not added to the Cert list
				if(cadiIssuers.contains(certarr[0].getIssuerDN().toString())) {
					String x500 = certarr[0].getSubjectDN().getName();
					int ou=x500.indexOf("OU=");
					if(ou>0) {
						ou+=3;
						int comma = x500.indexOf(',',ou);
						if(comma>0) {
							String id= x500.substring(ou,comma);
							String idenv[] = id.split(":");
							if(idenv.length==1 || (idenv.length>1 && env.equals(idenv[1]))) {
								return new X509HttpTafResp(access, 
									new X509Principal(idenv[0], certarr[0],null), 
										id + " validated by CADI x509", RESP.IS_AUTHENTICATED);
							}
						}
					}
				}
			}

			byte[] array = null;
			byte[] certBytes = null;
			X509Certificate cert=null;
			String responseText=null;
			String authHeader = req.getHeader("Authorization");

			if(certarr!=null) {  // If cert !=null, Cert is Tested by Mutual Protocol.
				if(authHeader!=null) { // This is only intended to be a Secure Connection, not an Identity
					return new X509HttpTafResp(access, null, "Certificate verified, but another Identity is presented", RESP.TRY_ANOTHER_TAF);
				}
				cert = certarr[0];
				responseText = ", validated by Mutual SSL Protocol";
			} else {		 // If cert == null, Get Declared Cert (in header), but validate by having them sign something
				if(authHeader != null && authHeader.startsWith("x509 ")) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream(authHeader.length());
					try {
						array = authHeader.getBytes();
						ByteArrayInputStream bais = new ByteArrayInputStream(array);
						Symm.base64noSplit.decode(bais, baos, 5);
						certBytes = baos.toByteArray();
						cert = getCert(certBytes);
						
						/** 
						 * Identity from CERT if well know CA and specific encoded information
						 */
						// If found Identity doesn't work, try SignedStuff Protocol
//									cert.checkValidity();
//									cert.--- GET FINGERPRINT?
						String stuff = req.getHeader("Signature");
						if(stuff==null) 
							return new X509HttpTafResp(access, null, "Header entry 'Signature' required to validate One way X509 Certificate", RESP.TRY_ANOTHER_TAF);
						String data = req.getHeader("Data"); 
//									if(data==null) 
//										return new X509HttpTafResp(access, null, "No signed Data to validate with X509 Certificate", RESP.TRY_ANOTHER_TAF);

						// Note: Data Pos shows is "<signatureType> <data>"
//									int dataPos = (stuff.indexOf(' ')); // determine what is Algorithm
						// Get Signature 
						bais = new ByteArrayInputStream(stuff.getBytes());
						baos = new ByteArrayOutputStream(stuff.length());
						Symm.base64noSplit.decode(bais, baos);
						array = baos.toByteArray();
//									Signature sig = Signature.getInstance(stuff.substring(0, dataPos)); // get Algorithm from first part of Signature
						
						Signature sig = Signature.getInstance(cert.getSigAlgName()); 
						sig.initVerify(cert.getPublicKey());
						sig.update(data.getBytes());
						if(!sig.verify(array)) {
							access.log(Level.ERROR, "Signature doesn't Match");
							return new X509HttpTafResp(access, null, "Certificate NOT verified", RESP.TRY_ANOTHER_TAF);
						}
						responseText = ", validated by Signed Data";
					} catch (Exception e) {
						access.log(e, "Exception while validating Cert");
						return new X509HttpTafResp(access, null, "Certificate NOT verified", RESP.TRY_ANOTHER_TAF);
					}
					
				} else {
					return new X509HttpTafResp(access, null, "No Certificate Info on Transaction", RESP.TRY_ANOTHER_TAF);
				}
			}

			// A cert has been found, match Identify
			Principal prin=null;
			
			for(int i=0;prin==null && i<certIdents.length;++i) {
				if((prin=certIdents[i].identity(req, cert, certBytes))!=null) {
					responseText = prin.getName() + " matches Certificate " + cert.getSubjectX500Principal().getName() + responseText;
//					xresp = new X509HttpTafResp(
//								access,
//								prin,
//								prin.getName() + " matches Certificate " + cert.getSubjectX500Principal().getName() + responseText,
//								RESP.IS_AUTHENTICATED);
					
				}
			}

			// if Principal is found, check for "AS_USER" and whether this entity is trusted to declare
			if(prin!=null) {
				String as_user=req.getHeader(Config.CADI_USER_CHAIN);
				if(as_user!=null) {
					if(as_user.startsWith("TGUARD ") && lur.fish(prin, new LocalPermission("com.att.aaf.trust|"+prin.getName()+"|tguard"))) {
						prin = new TGuardPrincipal(as_user.substring(7));
						responseText=prin.getName() + " set via trust of " + responseText;
					}
				}
				return new X509HttpTafResp(
					access,
					prin,
					responseText,
					RESP.IS_AUTHENTICATED);
			}
		} catch(Exception e) {
			return new X509HttpTafResp(access, null, e.getMessage(), RESP.TRY_ANOTHER_TAF);	
		}
	
		return new X509HttpTafResp(access, null, "Certificate NOT verified", RESP.TRY_ANOTHER_TAF);
	}

	public Resp revalidate(CachedPrincipal prin) {
		return null;
	}

}
