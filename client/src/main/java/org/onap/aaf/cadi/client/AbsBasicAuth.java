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
package org.onap.aaf.cadi.client;

import java.io.IOException;

import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.config.SecurityInfoC;

public abstract class AbsBasicAuth<CLIENT> implements SecuritySetter<CLIENT> {
		protected static final String REPEAT_OFFENDER="This call is aborted because of repeated usage of invalid Passwords";
		private static final int MAX_TEMP_COUNT = 10;
		private static final int MAX_SPAM_COUNT = 10000;
		private static final long WAIT_TIME = 1000*60*4;
		
		protected final String headValue;
		protected SecurityInfoC<CLIENT> securityInfo;
		protected String user;
		private long lastMiss;
		private int count;

		public AbsBasicAuth(String user, String pass, SecurityInfoC<CLIENT> si) throws IOException {
			this.user = user;
			headValue = "Basic " + Symm.base64.encode(user + ':' + pass);
			securityInfo = si;
			lastMiss=0L;
			count=0;
		}

		/* (non-Javadoc)
		 * @see com.att.cadi.SecuritySetter#getID()
		 */
		@Override
		public String getID() {
			return user;
		}
		
		public boolean isDenied() {
			if(lastMiss>0 && lastMiss>System.currentTimeMillis()) {
				return true;
			} else {
				lastMiss=0L;
				return false;
			}
		}
		
		public synchronized int setLastResponse(int httpcode) {
			if(httpcode == 401) {
				++count;
				if(lastMiss==0L && count>MAX_TEMP_COUNT) {
					lastMiss=System.currentTimeMillis()+WAIT_TIME;
				}
//				if(count>MAX_SPAM_COUNT) {
//					System.err.printf("Your service has %d consecutive bad service logins to AAF. \nIt will now exit\n",
//							count);
//					System.exit(401);
//				}
				if(count%1000==0) {
					System.err.printf("Your service has %d consecutive bad service logins to AAF. AAF Access will be disabled after %d\n",
						count,MAX_SPAM_COUNT);
				}

			} else {
				lastMiss=0;
			}
			return count;
		}
		
		public int count() {
			return count;
		}
}
