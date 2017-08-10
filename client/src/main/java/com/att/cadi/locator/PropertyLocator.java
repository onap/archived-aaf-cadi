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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;

import com.att.cadi.Locator;
import com.att.cadi.LocatorException;
import com.att.inno.env.util.Split;

public class PropertyLocator implements Locator<URI> {
	private final URI [] orig;
	private PLItem[] current;
	private int end;
	private final SecureRandom random;
	private URI[] resolved;
	private long lastRefreshed=0L;
	private long minRefresh;
	private long backgroundRefresh;

	public PropertyLocator(String locList) throws LocatorException {
		this(locList,10000L, 1000*60*20); // defaults, do not refresh more than once in 10 seconds, Refresh Locator every 20 mins.
	}
	/**
	 * comma delimited root url list
	 * 
	 * @param locList
	 * @throws LocatorException
	 */
	public PropertyLocator(String locList, long minRefreshMillis, long backgroundRefreshMillis) throws LocatorException {
		minRefresh = minRefreshMillis;
		backgroundRefresh = backgroundRefreshMillis;
		if(locList==null) {
			throw new LocatorException("No Location List given for PropertyLocator");
		}
		String[] locarray = Split.split(',',locList);
		List<URI> uriList = new ArrayList<URI>();
		
		random = new SecureRandom();
		
		for(int i=0;i<locarray.length;++i) {
			try {
				int range = locarray[i].indexOf(":[");
				if(range<0) {
					uriList.add(new URI(locarray[i]));
				} else {
					int dash = locarray[i].indexOf('-',range+2);
					int brac = locarray[i].indexOf(']',dash+1);
					int start = Integer.parseInt(locarray[i].substring(range+2, dash));
					int end = Integer.parseInt(locarray[i].substring(dash+1, brac));
					for(int port=start;port<=end;++port) {
						uriList.add(new URI(locarray[i].substring(0, range+1)+port));
					}
				}
			} catch (NumberFormatException nf) {
				throw new LocatorException("Invalid URI format: " + locarray[i]);
			} catch (URISyntaxException e) {
				throw new LocatorException(e);
			}
		}
		orig = new URI[uriList.size()];
		uriList.toArray(orig);

		refresh();
		new Timer("PropertyLocator Refresh Timer",true).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				refresh();
			}
		}, backgroundRefresh,backgroundRefresh);

	}

	@Override
	public URI get(Item item) throws LocatorException {
		synchronized(orig) {
			if(item==null) {
				return null;
			} else {
				return resolved[((PLItem)item).idx];
			}
		}
	}

	@Override
	public Item first() throws LocatorException {
		return end>0?current[0]:null;
	}

	@Override
	public boolean hasItems() {
		return end>0;
	}

	@Override
	public Item next(Item item) throws LocatorException {
		if(item==null) {
			return null;
		} else {
			int spot;
			if((spot=(((PLItem)item).order+1))>=end)return null;
			return current[spot];
		}
	}

	@Override
	public synchronized void invalidate(Item item) throws LocatorException {
		if(--end<=0) {
			refresh();
			return;
		}
		PLItem pli = (PLItem)item;
		int i,order;
		for(i=0;i<end;++i) {
			if(pli==current[i])break;
		}
		order = current[i].order;
		for(;i<end;++i) {
			current[i]=current[i+1];
			current[i].order=order++;
		}
		current[end]=pli;
	}

	@Override
	public Item best() throws LocatorException {
		if(current.length==0) {
			refresh();
		}
		switch(current.length) {
			case 0:
				return null;
			case 1:
				return current[0];
			default:
				return current[Math.abs(random.nextInt())%current.length];
		}
	}

	@Override
	public synchronized boolean refresh() {
		if(System.currentTimeMillis()>lastRefreshed) {
			// Build up list
			List<URI> resolve = new ArrayList<URI>();
			String realname;
			for(int i = 0; i < orig.length ; ++i) {
				try {
					InetAddress ia[] = InetAddress.getAllByName(orig[i].getHost());

					URI o,n;
					for(int j=0;j<ia.length;++j) {
						o = orig[i];
						Socket socket = new Socket();
						try {
							realname=ia[j].getCanonicalHostName();
							socket.connect(new InetSocketAddress(realname,o.getPort()),3000);
							if(socket.isConnected()) {
								n = new URI(
										o.getScheme(),
										o.getUserInfo(),
										realname,
										o.getPort(),
										o.getPath(),
										o.getQuery(),
										o.getFragment()
										);
								resolve.add(n);
							}
						} catch (IOException e) {
						} finally {
							if(!socket.isClosed()) {
								try {
									socket.close();
								} catch (IOException e) {
									// nothing to do.
								}
							}
						}
					}
				} catch (UnknownHostException | URISyntaxException e) {
					// Note: Orig Name already known as valid, based on constructor
				}
			}
			end=resolve.size();
			PLItem[] newCurrent;
			if(current==null || current.length!=end) {
				newCurrent = new PLItem[end];
			} else {
				newCurrent = current;
			}
	
			for(int i=0; i< end; ++i) {
				if(newCurrent[i]==null){
					newCurrent[i]=new PLItem(i);
				} else {
					newCurrent[i].idx=newCurrent[i].order=i;
				}
			}
			synchronized(orig) {
				resolved = new URI[end];
				resolve.toArray(resolved);
				current = newCurrent;
			}
			lastRefreshed = System.currentTimeMillis()+minRefresh;
			return !resolve.isEmpty();
		} else {
			return false;
		}
	}
	
	private class PLItem implements Item {
		public int idx,order;
		
		public PLItem(int i) {
			idx = order =i;
		}
		
		public String toString() {
			return "Item: " + idx + " order: " + order;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(URI uri : orig) {
			boolean isResolved=false;
			if(uri!=null) {
				if(first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(uri.toString());
				sb.append(" [");
				for(URI u2 : resolved) {
					if(uri.equals(u2)) {
						isResolved = true;
						break;
					}
				}
				sb.append(isResolved?"X]\n":" ]");
			}
		}
		return sb.toString();
	}
	
	public void destroy() {
	}
}
