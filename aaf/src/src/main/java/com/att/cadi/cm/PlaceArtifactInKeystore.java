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
package com.att.cadi.cm;

import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;

import com.att.cadi.CadiException;
import com.att.cadi.Symm;
import com.att.cadi.config.Config;
import com.att.cadi.util.Chmod;
import com.att.inno.env.Trans;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

public class PlaceArtifactInKeystore extends ArtifactDir {
	private String kst;
	//TODO get ROOT DNs or Trusted DNs from Certificate Manager.
	private static String[] rootDNs = new String[]{			
			"CN=ATT CADI Root CA - Test, O=ATT, OU=CSO, C=US",	
			"CN=ATT AAF CADI CA, OU=CSO, O=ATT, C=US"
	};

	public PlaceArtifactInKeystore(String kst) {
		this.kst = kst;
	}

	@Override
	public boolean _place(Trans trans, CertInfo certInfo, Artifact arti) throws CadiException {
		File fks = new File(dir,arti.getAppName()+'.'+kst);
		try {
			KeyStore jks = KeyStore.getInstance(kst);
			if(fks.exists()) {
				fks.delete();
			}	

			// Get the Cert(s)... Might include Trust store
			Collection<? extends Certificate> certColl = Factory.toX509Certificate(trans, certInfo.getCerts());
			Certificate[] certs = new Certificate[certColl.size()];
			certColl.toArray(certs);
			
			boolean first = true;
			StringBuilder issuers = new StringBuilder();
			for(Certificate c : certs) {
				if(c instanceof X509Certificate) {
					X509Certificate xc = (X509Certificate)c;
					String issuer = xc.getIssuerDN().toString();
					for(String root : rootDNs) {
						if(root.equals(issuer)) {
							if(first) {
								first=false;
							} else {
								issuers.append(":");
							}
							if(xc.getSubjectDN().toString().contains("Issuing CA")) {
								issuers.append(xc.getSubjectDN());
							}
						}
					}
				}
			}
			addProperty(Config.CADI_X509_ISSUERS,issuers.toString());

			// Add CADI Keyfile Entry to Properties
			addProperty(Config.CADI_KEYFILE,arti.getDir()+'/'+arti.getAppName() + ".keyfile");
			// Set Keystore Password
			addProperty(Config.CADI_KEYSTORE,fks.getCanonicalPath());
			String keystorePass = Symm.randomGen(CmAgent.PASS_SIZE);
			addEncProperty(Config.CADI_KEYSTORE_PASSWORD,keystorePass);
			char[] keystorePassArray = keystorePass.toCharArray();
			jks.load(null,keystorePassArray); // load in
			
			// Add Private Key/Cert Entry for App
			// Note: Java SSL security classes, while having a separate key from keystore,
			// is documented to not actually work. 
			// java.security.UnrecoverableKeyException: Cannot recover key
			// You can create a custom Key Manager to make it work, but Practicality  
			// dictates that you live with the default, meaning, they are the same
			String keyPass = keystorePass; //Symm.randomGen(CmAgent.PASS_SIZE);
			PrivateKey pk = Factory.toPrivateKey(trans, certInfo.getPrivatekey());
			addEncProperty(Config.CADI_KEY_PASSWORD, keyPass);
			addProperty(Config.CADI_ALIAS, arti.getMechid());
//			Set<Attribute> attribs = new HashSet<Attribute>();
//			if(kst.equals("pkcs12")) {
//				// Friendly Name
//				attribs.add(new PKCS12Attribute("1.2.840.113549.1.9.20", arti.getAppName()));
//			} 
//			
			KeyStore.ProtectionParameter protParam = 
					new KeyStore.PasswordProtection(keyPass.toCharArray());
			
			KeyStore.PrivateKeyEntry pkEntry = 
				new KeyStore.PrivateKeyEntry(pk, new Certificate[] {certs[0]});
			jks.setEntry(arti.getMechid(), 
					pkEntry, protParam);
		
			// Write out
			write(fks,Chmod.to400,jks,keystorePassArray);
			
			// Change out to TrustStore
			fks = new File(dir,arti.getAppName()+".trust."+kst);
			jks = KeyStore.getInstance(kst);
			
			// Set Truststore Password
			addProperty(Config.CADI_TRUSTSTORE,fks.getCanonicalPath());
			String trustStorePass = Symm.randomGen(CmAgent.PASS_SIZE);
			addEncProperty(Config.CADI_TRUSTSTORE_PASSWORD,trustStorePass);
			char[] truststorePassArray = trustStorePass.toCharArray();
			jks.load(null,truststorePassArray); // load in
			
			// Add Trusted Certificates
			for(int i=1; i<certs.length;++i) {
				jks.setCertificateEntry("cadi_" + arti.getCa() + '_' + i, certs[i]);
			}
			// Write out
			write(fks,Chmod.to400,jks,truststorePassArray);

		} catch (Exception e) {
			throw new CadiException(e);
		}
		return false;
	}

}
