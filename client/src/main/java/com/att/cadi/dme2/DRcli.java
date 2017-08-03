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
package com.att.cadi.dme2;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.api.DME2Manager;
import com.att.aft.dme2.manager.registry.DME2Endpoint;
import com.att.aft.dme2.request.DmeUniformResource;
import com.att.cadi.CadiException;
import com.att.cadi.SecuritySetter;
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
public class DRcli extends Rcli<DME2Client> {
	// Can be more efficient if tied to manager, apparently.  Can pass in null.
	DME2Manager manager=null;
	private SecuritySetter<DME2Client> ss;
	private boolean isProxy;
	
	public DRcli(URI uri, SecuritySetter<DME2Client> secSet) {
		this.uri = uri;
		type = TYPE.JSON;
		apiVersion = null;
		ss=secSet;
	}
	
	@Override
	protected DRcli clone(URI uri, SecuritySetter<DME2Client> ss) {
		return new DRcli(uri,ss);
	}



	/**
	 * Note from Thaniga on 11/5.  DME2Client is not expected to be reused... need a fresh one
	 * on each transaction, which is expected to cover the Async aspects.
	 * 
	 * @return
	 * @throws APIException 
	 * @throws DME2Exception 
	 */
	protected EClient<DME2Client> client() throws CadiException {
		try {
			DEClient dc = new DEClient(manager,getSecuritySetter(),uri,readTimeout);
			dc.setProxy(isProxy);
			return dc;
		} catch (DME2Exception e) {
			throw new CadiException(e);
		}
	}

	public DRcli setManager(DME2Manager dme2Manager) {
		manager = dme2Manager;
		return this;
	}

	public List<DRcli> all() throws DME2Exception, APIException {
		ArrayList<DRcli> al = new ArrayList<DRcli>();
		
		if(manager == null) {
			manager = DME2Manager.getDefaultInstance();
		}
		try {
			DME2Endpoint[] endp = manager.getEndpoints(new DmeUniformResource(manager.getConfig(),uri));
			// Convert Searchable Endpoints to Direct Endpoints
			for(DME2Endpoint de : endp) {
				al.add(new DRcli(
						new URI(uri.getScheme(),null,de.getHost(),de.getPort(),null,null,null),ss)
//						new URI(uri.getScheme(),null,de.getHost(),de.getPort(),uri.getPath(),null,null),ss)
				.setManager(manager)
				);
			}
		} catch (MalformedURLException e) {
			throw new APIException("Invalid URL",e);
		} catch (URISyntaxException e) {
			throw new APIException("Invalid URI",e);
		}
		return al;
	}

	@Override
	public void invalidate() throws CadiException {
		try {
			manager.refresh();
		} catch (Exception e) {
			throw new CadiException(e);
		}
	}

	@Override
	public void setSecuritySetter(SecuritySetter<DME2Client> ss) {
		this.ss = ss;
	}

	@Override
	public SecuritySetter<DME2Client> getSecuritySetter() {
		return ss;
	}

	public void setProxy(boolean isProxy) {
		this.isProxy = isProxy;
	}

}
