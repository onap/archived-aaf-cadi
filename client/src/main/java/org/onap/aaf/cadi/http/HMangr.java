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
package org.onap.aaf.cadi.http;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLHandshakeException;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;

import org.onap.aaf.inno.env.APIException;

public class HMangr {
	private String apiVersion;
	private int readTimeout, connectionTimeout;
	public final Locator<URI> loc;
	private Access access;
	
	public HMangr(Access access, Locator<URI> loc) {
		readTimeout = 10000;
		connectionTimeout=3000;
		this.loc = loc;
		this.access = access;
	}

	/**
	 * Reuse the same service.  This is helpful for multiple calls that change service side cached data so that 
	 * there is not a speed issue.
	 * 
	 * If the service goes down, another service will be substituted, if available.
	 * 
	 * @param access
	 * @param loc
	 * @param ss
	 * @param item
	 * @param retryable
	 * @return
	 * @throws URISyntaxException 
	 * @throws Exception
	 */
	public<RET> RET same(SecuritySetter<HttpURLConnection> ss, Retryable<RET> retryable) throws APIException, CadiException, LocatorException {
		RET ret = null;
		boolean retry = true;
		int retries = 0;
		Rcli<HttpURLConnection> client = retryable.lastClient();
		try {
			do {
				// if no previous state, get the best
				if(retryable.item()==null) {
					retryable.item(loc.best());
					retryable.lastClient = null;
				}
				if(client==null) {
					Item item = retryable.item();
					URI uri=loc.get(item);
					if(uri==null) {
						loc.invalidate(retryable.item());
						if(loc.hasItems()) {
							retryable.item(loc.next(retryable.item()));
							continue;
						} else {
							throw new LocatorException("No clients available for " + loc.toString());
						}
					}
					client = new HRcli(this, uri,item,ss)
						.connectionTimeout(connectionTimeout)
						.readTimeout(readTimeout)
						.apiVersion(apiVersion);
				} else {
					client.setSecuritySetter(ss);
				}
				
				retry = false;
				try {
					ret = retryable.code(client);
				} catch (APIException | CadiException e) {
					Item item = retryable.item();
					loc.invalidate(item);
					retryable.item(loc.next(item));
					try {
						Throwable ec = e.getCause();
						if(ec instanceof java.net.ConnectException) {
							if(client!=null && ++retries<2) { 
								access.log(Level.WARN,"Connection refused, trying next available service");
								retry = true;
							} else {
								throw new CadiException("Connection refused, no more available connections to try");
							}
						} else if(ec instanceof SSLHandshakeException) {
							retryable.item(null);
							throw e;
						} else if(ec instanceof SocketException) {
							if("java.net.SocketException: Connection reset".equals(ec.getMessage())) {
								access.log(Level.ERROR, ec.getMessage(), " can mean Certificate Expiration or TLS Protocol issues");
							}
							retryable.item(null);
							throw e;
						} else {
							retryable.item(null);
							throw e;
						}
					} finally {
						client = null;
					}
				} catch (ConnectException e) {
					Item item = retryable.item();
					loc.invalidate(item);
					retryable.item(loc.next(item));
				}
			} while(retry);
		} finally {
			retryable.lastClient = client;
		}
		return ret;
	}
	
	
	public<RET> RET best(SecuritySetter<HttpURLConnection> ss, Retryable<RET> retryable) throws LocatorException, CadiException, APIException {
		if(loc==null) {
			throw new LocatorException("No Locator Configured");
		}
		retryable.item(loc.best());
		return same(ss,retryable);
	}
	public<RET> RET all(SecuritySetter<HttpURLConnection> ss, Retryable<RET> retryable) throws LocatorException, CadiException, APIException {
		return oneOf(ss,retryable,true,null);
	}

	public<RET> RET all(SecuritySetter<HttpURLConnection> ss, Retryable<RET> retryable,boolean notify) throws LocatorException, CadiException, APIException {
		return oneOf(ss,retryable,notify,null);
	}
	
	public<RET> RET oneOf(SecuritySetter<HttpURLConnection> ss, Retryable<RET> retryable,boolean notify,String host) throws LocatorException, CadiException, APIException {
		RET ret = null;
		// make sure we have all current references:
		loc.refresh();
		for(Item li=loc.first();li!=null;li=loc.next(li)) {
			URI uri=loc.get(li);
			if(host!=null && !host.equals(uri.getHost())) {
				break;
			}
			try {
				ret = retryable.code(new HRcli(this,uri,li,ss));
				access.log(Level.DEBUG,"Success calling",uri,"during call to all services");
			} catch (APIException | CadiException e) {
				Throwable t = e.getCause();
				if(t!=null && t instanceof ConnectException) {
					loc.invalidate(li);
					access.log(Level.ERROR,"Connection to",uri,"refused during call to all services");
				} else if(t instanceof SSLHandshakeException) {
					access.log(Level.ERROR,t.getMessage());
					loc.invalidate(li);
				} else if(t instanceof SocketException) {
					if("java.net.SocketException: Connection reset".equals(t.getMessage())) {
						access.log(Level.ERROR, t.getMessage(), " can mean Certificate Expiration or TLS Protocol issues");
					}
					retryable.item(null);
					throw e;
				} else {
					throw e;
				}
			} catch (ConnectException e) {
				loc.invalidate(li);
				access.log(Level.ERROR,"Connection to",uri,"refused during call to all services");
			}
		}
			
		if(ret == null && notify) 
			throw new LocatorException("No available clients to call");
		return ret;
	}
	

	public void close() {
		// TODO Anything here?
	}

	public HMangr readTimeout(int timeout) {
		this.readTimeout = timeout;
		return this;
	}

	public int readTimeout() {
		return readTimeout;
	}
	
	public void connectionTimeout(int t) {
		connectionTimeout = t;
	}

	public int connectionTimout() {
		return connectionTimeout;
	}

	public HMangr apiVersion(String version) {
		apiVersion = version;
		return this;
	}

	public String apiVersion() {
		return apiVersion;
	}

}
