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
package com.att.cadi.taf.localhost;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.CachedPrincipal;
import com.att.cadi.CachedPrincipal.Resp;
import com.att.cadi.Taf;
import com.att.cadi.taf.HttpTaf;
import com.att.cadi.taf.TafResp;
import com.att.cadi.taf.TafResp.RESP;

/**
 * Implement the ability to utilize LocalHost as a TAF.
 * 
 * Configure with two properties, 
 * 	localhost.deny
 *  localhost.accept
 *  
 * 1) If localhost.deny==true, then no localhost requests are allowed
 * 2) If localhost.deny==false, but accept==false, return "Try Another TAF" (i.e. allow further checking of the
 *   chain, but don't treat localhost as an acceptable credential)
 * 3) If localhost.deny=false and accept=true, then the processes coming from the same machine, given logins are needed, 
 * to run, are treated as validated.  This is primarily for Developer purposes.
 *   
 * 
 *
 */
public class LocalhostTaf implements HttpTaf {
	private TafResp isLocalHost,isNotLocalHost;
	private static final TreeSet<String> addrSet;
	
	static {
		addrSet = new TreeSet<String>();
		try {
			for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();en.hasMoreElements();) {
				NetworkInterface ni = en.nextElement();
				for(Enumeration<InetAddress> eia = ni.getInetAddresses();eia.hasMoreElements();) {
					InetAddress ia = eia.nextElement();
					addrSet.add(ia.getHostAddress());
				}
			}
		} catch (SocketException e) {
		}
		
	}

	public LocalhostTaf(Access access, boolean accept, boolean isDenied) {
		String hostname = access.getProperty("hostname",null);
		if(hostname !=null) {
			try {
				addrSet.add(InetAddress.getByName(hostname).getHostAddress());
			} catch (UnknownHostException e) {
				access.log(e,"Unknown Host");
			}
		}
		
		if(isDenied) {
			access.log(Level.INFO,"LocalhostTaf will deny all localhost traffic");
		} else {
			access.log(Level.INFO,"LocalhostTaf will not deny localhost requests, ",
					(accept?"and will treat them as authenticated":"but will require other authentication"));
		}
		// Set the appropriate behavior for when ID coming in is from localhost
		isLocalHost = isDenied?	
			new LocalhostTafResp(access, RESP.NO_FURTHER_PROCESSING,"Localhost is denied"):
			accept?
				new LocalhostTafResp(access, RESP.IS_AUTHENTICATED,"Localhost is allowed"):
				new LocalhostTafResp(access, RESP.TRY_ANOTHER_TAF,"Localhost is allowed");
		isNotLocalHost = new LocalhostTafResp(access, RESP.TRY_ANOTHER_TAF,"Address is not Localhost");
	}

//	@Override
	public TafResp validate(Taf.LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
		String remote = req.getRemoteAddr();
		return addrSet.contains(remote)
			?isLocalHost
			:isNotLocalHost;
	}

	/** 
	 * This function used for other TAFs (i.e. CSP, which can't work on localhost address)
	 * 
	 * @param address
	 * @return
	 */
	public static boolean isLocalAddress(String address) {
		return addrSet.contains(address);
	}
	
	public String toString() {
		return "Localhost TAF activated: " + isLocalHost.desc();
	}

	public Resp revalidate(CachedPrincipal prin) {
		// shouldn't get here, since there's no need to Cache, but if so, LocalHost is always valid...
		return Resp.REVALIDATED;
	}
}
