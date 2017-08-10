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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.att.cadi.Permission;
import com.att.cadi.PropAccess;
import com.att.cadi.aaf.v2_0.AAFAuthn;
import com.att.cadi.aaf.v2_0.AAFConHttp;
import com.att.cadi.aaf.v2_0.AAFLurPerm;
import com.att.cadi.aaf.v2_0.AAFTaf;
import com.att.cadi.config.Config;
import com.att.cadi.locator.DNSLocator;
import com.att.cadi.principal.CachedBasicPrincipal;

import junit.framework.Assert;

public class JU_JMeter {
	private static AAFConHttp aaf;
	private static AAFAuthn<HttpURLConnection> aafAuthn;
	private static AAFLurPerm aafLur;
	private static ArrayList<Principal> perfIDs;
	
	private static AAFTaf<HttpURLConnection> aafTaf;
	private static PropAccess access;

	@BeforeClass
	public static void before() throws Exception {
		if(aafLur==null) {
			Properties props = System.getProperties();
			props.setProperty("AFT_LATITUDE", "32.780140");
			props.setProperty("AFT_LONGITUDE", "-96.800451");
			props.setProperty("DME2_EP_REGISTRY_CLASS","DME2FS");
			props.setProperty("AFT_DME2_EP_REGISTRY_FS_DIR","/Volumes/Data/src/authz/dme2reg");
			props.setProperty("AFT_ENVIRONMENT", "AFTUAT");
			props.setProperty("SCLD_PLATFORM", "NON-PROD");
			props.setProperty(Config.AAF_URL,"https://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=2.0/envContext=DEV/routeOffer=BAU_SE");
			props.setProperty(Config.AAF_READ_TIMEOUT, "2000");
			int timeToLive = 3000;
			props.setProperty(Config.AAF_CLEAN_INTERVAL, Integer.toString(timeToLive));
			props.setProperty(Config.AAF_HIGH_COUNT, "4");

			String aafPerfIDs = props.getProperty("AAF_PERF_IDS");
			perfIDs = new ArrayList<Principal>();
			File perfFile = null;
			if(aafPerfIDs!=null) {
				perfFile = new File(aafPerfIDs);
			}

			access = new PropAccess();
			aaf = new AAFConHttp(access, new DNSLocator(access,"https","localhost","8100"));
			aafTaf = new AAFTaf<HttpURLConnection>(aaf,false);
			aafLur = aaf.newLur(aafTaf);
			aafAuthn = aaf.newAuthn(aafTaf);
			aaf.basicAuth("testid@aaf.att.com", "whatever");

			if(perfFile==null||!perfFile.exists()) {
				perfIDs.add(new CachedBasicPrincipal(aafTaf, 
						"Basic dGVzdGlkOndoYXRldmVy", 
						"aaf.att.com",timeToLive));
				perfIDs.add(new Princ("ab1234@aaf.att.com")); // Example of Local ID, which isn't looked up
			} else {
				BufferedReader ir = new BufferedReader(new FileReader(perfFile));
				try {
					String line;
					while((line = ir.readLine())!=null) {
						if((line=line.trim()).length()>0)
							perfIDs.add(new Princ(line));
					}
				} finally {
					ir.close();
				}
			}
			Assert.assertNotNull(aafLur);
		}
	}

	private static class Princ implements Principal {
		private String name;
		public Princ(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		
	};
	
	private static int index = -1;
	
	private synchronized Principal getIndex() {
		if(perfIDs.size()<=++index)index=0;
		return perfIDs.get(index);
	}
	@Test
	public void test() {
		try {
				aafAuthn.validate("testid@aaf.att.com", "whatever");
				List<Permission> perms = new ArrayList<Permission>();
				aafLur.fishAll(getIndex(), perms);
//				Assert.assertFalse(perms.isEmpty());
//				for(Permission p : perms) {
//					//access.log(Access.Level.AUDIT, p.permType());
//				}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			Assert.assertFalse(sw.toString(),true);
		}
	}

}
