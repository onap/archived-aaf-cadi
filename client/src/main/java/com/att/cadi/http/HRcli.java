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
package com.att.cadi.http;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import com.att.aft.dme2.api.DME2Exception;
import com.att.cadi.CadiException;
import com.att.cadi.LocatorException;
import com.att.cadi.SecuritySetter;
import com.att.cadi.Locator.Item;
import com.att.cadi.client.EClient;
import com.att.cadi.client.Rcli;
import com.att.inno.env.APIException;
import com.att.inno.env.Data.TYPE;

/**
 * DME2 Rosetta Client
 * 
 * JAXB defined JSON or XML over DME2 middleware
 * 
 *
 * @param <T>
 */
public class HRcli extends Rcli<HttpURLConnection> {
	private HMangr hman;
	private Item item;
	private SecuritySetter<HttpURLConnection> ss;

	public HRcli(HMangr hman, Item locItem, SecuritySetter<HttpURLConnection> secSet) throws URISyntaxException, LocatorException {
		item=locItem;
		uri=hman.loc.get(locItem);
		this.hman = hman;
		ss=secSet;
		type = TYPE.JSON;
		apiVersion = hman.apiVersion();
	}

	public HRcli(HMangr hman, URI uri, Item locItem, SecuritySetter<HttpURLConnection> secSet) {
		locItem=item;
		this.uri = uri;
		this.hman = hman;
		ss=secSet;
		type = TYPE.JSON;
		apiVersion = hman.apiVersion();
	}

	@Override
	protected HRcli clone(URI uri, SecuritySetter<HttpURLConnection> ss) {
		return new HRcli(hman,uri,item,ss);
	}



	/**
	 * Note from Thaniga on 11/5.  DME2Client is not expected to be reused... need a fresh one
	 * on each transaction, which is expected to cover the Async aspects.
	 * 
	 * @return
	 * @throws APIException 
	 * @throws DME2Exception 
	 */
	protected EClient<HttpURLConnection> client() throws CadiException {
		try {
			if(uri==null) {
				Item item = hman.loc.best();
				if(item==null) {
					throw new CadiException("No service available for " + hman.loc.toString());
				}
				uri = hman.loc.get(item);
			}
			return new HClient(ss,uri,connectionTimeout);
		} catch (Exception e) {
			throw new CadiException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.att.cadi.client.Rcli#setSecuritySetter(com.att.cadi.SecuritySetter)
	 */
	@Override
	public void setSecuritySetter(SecuritySetter<HttpURLConnection> ss) {
		this.ss = ss;
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.client.Rcli#getSecuritySetter()
	 */
	@Override
	public SecuritySetter<HttpURLConnection> getSecuritySetter() {
		return ss;
	}

	public void invalidate() throws CadiException {
		try {
			hman.loc.invalidate(item);
		} catch (Exception e) {
			throw new CadiException(e);
		}
	}
	
	public HRcli setManager(HMangr hman) {
		this.hman = hman;
		return this;
	}

	public String toString() {
		return uri.toString();
	}
	
}
