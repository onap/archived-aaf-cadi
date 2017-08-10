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
package com.att.cadi.locator;

import java.net.URI;
import java.net.URISyntaxException;

import com.att.cadi.Access;
import com.att.cadi.LocatorException;
import com.att.cadi.http.HClient;
import com.att.cadi.http.HX509SS;

public class HClientHotPeerLocator extends HotPeerLocator<HClient> {
	private final HX509SS ss;

	public HClientHotPeerLocator(Access access, String urlstr, long invalidateTime, String localLatitude,
			String localLongitude, HX509SS ss) throws LocatorException {
		super(access, urlstr, invalidateTime, localLatitude, localLongitude);
		
		this.ss = ss;
	}

	@Override
	protected HClient _newClient(String clientInfo) throws LocatorException {
		try {
			int idx = clientInfo.indexOf('/');
			return new HClient(ss,new URI("https://"+(idx<0?clientInfo:clientInfo.substring(0, idx))),3000);
		} catch (URISyntaxException e) {
			throw new LocatorException(e);
		}
	}

	@Override
	protected HClient _invalidate(HClient client) {
		return null;
	}

	@Override
	protected void _destroy(HClient client) {
	}
}
