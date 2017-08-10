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
package com.att.cadi.dnsloc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.Locator;
import com.att.cadi.LocatorException;

public class DNSLocator implements Locator {
	private static enum Status {UNTRIED, OK, INVALID, SLOW};
	private static final int CHECK_TIME = 3000;
	
	private String host, protocol;
	private Access access;
	private Host[] hosts;
	private int startPort, endPort;
	private String suffix;
	
	public DNSLocator(Access access, String protocol, String host, String range) {
		this.host = host;
		this.protocol = protocol;
		this.access = access;
		int dash = range.indexOf('-');
		if(dash<0) {
			startPort = endPort = Integer.parseInt(range);
		} else {
			startPort = Integer.parseInt(range.substring(0,dash));
			endPort = Integer.parseInt(range.substring(dash + 1));
		}
		refresh();
	}

	@Override
	public URI get(Item item) throws LocatorException {
		return hosts[((DLItem)item).cnt].uri;
	}

	@Override
	public boolean hasItems() {
		for(Host h : hosts) {
			if(h.status==Status.OK) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void invalidate(Item item) {
		DLItem di = (DLItem)item;
		hosts[di.cnt].status = Status.INVALID;
	}

	@Override
	public Item best() throws LocatorException {
		// not a good "best"
		for(int i=0;i<hosts.length;++i) {
			switch(hosts[i].status) {
				case OK:
					return new DLItem(i);
				case INVALID:
					break;
				case SLOW:
					break;
				case UNTRIED:
					try {
						if(hosts[i].ia.isReachable(CHECK_TIME)) {
							hosts[i].status = Status.OK;
							return new DLItem(i);
						}
					} catch (IOException e) {
						throw new LocatorException(e);
					}
					break;
				default:
					break;
			}
		}
		throw new LocatorException("No Available URIs for " + host);
	}

	@Override
	public Item first() throws LocatorException {
		return new DLItem(0);
	}

	@Override
	public Item next(Item item) throws LocatorException {
		DLItem di = (DLItem)item;
		if(++di.cnt<hosts.length) {
			return di;
		} else {
			return null;
		}
	}

	@Override
	public boolean refresh() {
		try {
			InetAddress[] ias = InetAddress.getAllByName(host);
			Host[] temp = new Host[ias.length * (1 + endPort - startPort)];
			int cnt = -1;
			for(int j=startPort; j<=endPort; ++j) {
				for(int i=0;i<ias.length;++i) {
					temp[++cnt] = new Host(ias[i], j, suffix);
				}
			}
			hosts = temp;
			return true;
		} catch (Exception e) {
			access.log(Level.ERROR, e);
		}
		return false;
	}

	private class Host {
		private URI uri;
		private InetAddress ia;
		private Status status;
		
		public Host(InetAddress inetAddress, int port, String suffix) throws URISyntaxException {
			ia = inetAddress;
			uri = new URI(protocol,null,inetAddress.getHostAddress(),port,suffix,null,null);
			status = Status.UNTRIED;
		}
	}
	
	private class DLItem implements Item {
		public DLItem(int i) {
			cnt = i;
		}

		private int cnt;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
}
