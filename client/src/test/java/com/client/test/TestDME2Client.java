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
package com.client.test;

import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.api.DME2Manager;
import com.att.cadi.Symm;

public class TestDME2Client {
	public static void main(String[] args) {
		try {
			Properties props = System.getProperties();
			props.put("AFT_LATITUDE","32.780140");
			props.put("AFT_LONGITUDE","-96.800451");
			props.put("AFT_ENVIRONMENT","AFTUAT");
			
			//props.put("keyStore","/Volumes/Data/src/authz/common/aaf.att.jks");
			props.put("AFT_DME2_KEYSTORE","/Volumes/Data/src/authz/common/aaf.att.jks");
			props.put("AFT_DME2_KEYSTORE_PASSWORD","enc:???");
			props.put("AFT_DME2_TRUSTSTORE","/Volumes/Data/src/authz/common/truststore.jks");
			props.put("AFT_DME2_TRUSTSTORE_PASSWORD","enc:???");
			
			// Local Testing on dynamic IP PC ***ONLY***
//			props.put("DME2_EP_REGISTRY_CLASS","DME2FS");
//			props.put("AFT_DME2_EP_REGISTRY_FS_DIR","/Volumes/Data/src/authz/dme2reg");
//			props.put("AFT_DME2_SSL_TRUST_ALL", "true");

			Symm symm;
			FileInputStream keyfile=new FileInputStream("/Volumes/Data/src/authz/common/keyfile");
			try {
				symm=Symm.obtain(keyfile);
			} finally {
				keyfile.close();
			}

			DME2Manager dm = new DME2Manager("DME2Manager TestHClient",props);
					  // Standard RESOLVE format
			String prefix;
			URI uri = 
//					new URI(
//					  "https://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=2.0/envContext=DEV/routeOffer=BAU_SE"
//					);
//				prefix = "";
//				   Direct Format
//				   new URI("https://mithrilcsp.sbc.com:8100/service=com.att.authz.AuthorizationService/version=2.0/envContext=DEV/routeOffer=BAU_SE");
//				   prefix = "";
//				   Go through PROXY
//			   	   new URI("https://mithrilcsp.sbc.com:8095");
//				   prefix = "/proxy";
					
//					new URI("https://mithrilcsp.sbc.com:8095");
					new URI("https://DME2RESOLVE/service=com.att.authz.authz-gw/version=2.0/envContext=UAT/routeOffer=BAU_SE");
//				   prefix = "";
				   prefix = "/proxy";
			DME2Client client = new DME2Client(dm,uri,3000);

			client.setCredentials("XX@NS", symm.depass("enc:???"));
			
			client.addHeader("Accept", "text/plain");
			client.setMethod("GET");
			client.setContext(prefix+"/authn/basicAuth");
			client.setPayload("");// Note: Even on "GET", you need a String in DME2
			
			String o = client.sendAndWait(5000); // There are other Asynchronous call options, see DME2 Docs
			
			System.out.println('[' + o + ']' + " (blank is good)");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
