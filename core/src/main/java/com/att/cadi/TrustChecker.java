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
package com.att.cadi;


import javax.servlet.http.HttpServletRequest;

import com.att.cadi.taf.TafResp;

/**
 * Change to another Principal based on Trust of caller and User Chain (if desired)
 * 
 *
 */
public interface TrustChecker {
	public TafResp mayTrust(TafResp tresp, HttpServletRequest req);
	
	/**
	 * A class that trusts no-one else, so just return same TResp
	 */
	public static TrustChecker NOTRUST = new TrustChecker() {
		@Override
		public TafResp mayTrust(TafResp tresp, HttpServletRequest req) {
			return tresp;
		}

		@Override
		public void setLur(Lur lur) {
		}
	};

	public void setLur(Lur lur);
}
