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
package org.onap.aaf.client.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Properties;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.dme2.DME2Locator;

import com.att.aft.dme2.api.DME2Manager;

public class JU_PaulUzee {
	public static void main(String[] args) {
		try {
			// You'll want to put this on Command line "-D" probably
			Properties props = System.getProperties();
			props.put("AFT_LATITUDE","32.780140");
			props.put("AFT_LONGITUDE","-96.800451");
			props.put("AFT_ENVIRONMENT","AFTPRD");

			//
			// Use an "Access" class to hook up logging, properties, etc.
			// Make one that ties into your code's logging, property mechanism, etc.
			//
			Access access = new PaulAccess();
			
			DME2Manager dm = new DME2Manager("Paul Uzee's Test",props);
			Locator loc = new DME2Locator(access ,dm,"com.att.authz.AuthorizationService","2.0","PROD","DEFAULT");

			
			for(Item item = loc.first(); item!=null; item=loc.next(item)) {
				URI location = (URI) loc.get(item);
				access.log(Level.INFO,location);
				access.log(Level.INFO,location.getScheme());
				access.log(Level.INFO,location.getHost());
				access.log(Level.INFO, location.getPort());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	}

	private static class PaulAccess implements Access {
		private Level willWrite = Level.INFO;

		@Override
		public ClassLoader classLoader() {
			return getClass().getClassLoader();
		}

		@Override
		public String decrypt(String data, boolean def) throws IOException {
			return data;
		}

		@Override
		public String getProperty(String tag, String def) {
			return System.getProperty(tag, def);
		}

		@Override
		public void load(InputStream is) throws IOException {
			System.getProperties().load(is);
		}

		@Override
		public void log(Level level, Object... obj) {
			if(level.compareTo(willWrite)<0) return;
			PrintStream ps;
			switch(level) {
				case DEBUG:
				case AUDIT:
				case ERROR:
				case WARN:
					ps = System.err;
					break;
				case INFO:
				case INIT:
				default:
					ps = System.out;
			}
			boolean first = true;
			for(Object o : obj) {
				if(first)first=false;
				else ps.print(' ');
				ps.print(o.toString());
			}
			ps.println();
		}

		@Override
		public void log(Exception e, Object... obj) {
			Object[] objs = new Object[obj.length+1];
			objs[0]=e.getMessage();
			System.arraycopy(objs, 1, obj, 0, obj.length);
			log(Level.ERROR,e,objs);
		}

		@Override
		public void setLogLevel(Level l) {
			willWrite = l;
		}

		/* (non-Javadoc)
		 * @see com.att.cadi.Access#willLog(com.att.cadi.Access.Level)
		 */
		@Override
		public boolean willLog(Level level) {
			return true;
		}

		@Override
		public void printf(Level level, String fmt, Object... elements) {
			// TODO Auto-generated method stub
			
		}
	};
}
