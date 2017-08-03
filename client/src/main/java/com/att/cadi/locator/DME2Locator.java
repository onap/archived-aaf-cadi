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
package com.att.cadi.locator;


import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.Random;
import java.security.SecureRandom;

//import com.att.aft.dme2.api.DME2Endpoint;
import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.api.DME2Manager;
import com.att.aft.dme2.api.DME2Server;
import com.att.aft.dme2.manager.registry.DME2Endpoint;
import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.Locator;
import com.att.cadi.LocatorException;
import com.att.cadi.PropAccess;

public class DME2Locator implements Locator<URI> {
	private DME2Manager dm;
	private DME2Endpoint[] endpoints;
	private Access access;
	private String service;
	private String version;
	private String routeOffer;
	private String envContext;
	private String thisMachine;
	private String pathInfo;
	private int thisPort;
	private boolean removeSelf;
	private final static SecureRandom random = new SecureRandom();

	// Default is to not bother trying to remove self
	public DME2Locator(Access access, DME2Manager dm, String service, String version, String envContext, String routeOffer) throws DME2Exception, UnknownHostException, LocatorException {
		this(access,dm,service,version,envContext,routeOffer,false);
	}
	
	public DME2Locator(Access access, DME2Manager dm, String service, String version, String envContext, String routeOffer, boolean removeSelf) throws DME2Exception, UnknownHostException, LocatorException {
		this.access = access;
		if(dm==null) {
			this.dm = new DME2Manager("DME2Locator created DME2Manager",System.getProperties());
		} else {
			this.dm = dm;
		}
		this.service = service;
		this.version = version;
		this.envContext = envContext;
		this.routeOffer = routeOffer;
		refresh();
		if(thisMachine==null) {
			// Can't get from dm... 
			thisMachine = InetAddress.getLocalHost().getHostName();
			thisPort = 0;
		} else {
			thisPort = dm.getPort();
		}

		this.removeSelf = removeSelf;
	}

	// Default is to not bother trying to remove self
	public DME2Locator(Access access, DME2Manager dm, String aafurl) throws DME2Exception, UnknownHostException, LocatorException {
		this(access,dm,aafurl,false);
	}
	
	public DME2Locator(Access access, DME2Manager dm, String aafurl, boolean removeSelf) throws DME2Exception, UnknownHostException, LocatorException {
		if(aafurl==null) {
			throw new LocatorException("URL is null");
		}
		this.access = access;
		if(dm==null) {
			Properties dprops;
			if(access instanceof PropAccess) {
				dprops = ((PropAccess)access).getDME2Properties();
			} else {
				dprops = System.getProperties();
			}
			dm = this.dm = new DME2Manager("DME2Locator created DME2Manager",dprops);
		} else {
			this.dm = dm;
		}
		String[] split = aafurl.split("/");
		StringBuilder sb = new StringBuilder();
		boolean dme2Entered = false;
		for(String s : split) {
			if(s.startsWith("service=")) {
				this.service = s.substring(8);
			} else if(s.startsWith("version=")) {
				this.version = s.substring(8);
			} else if(s.startsWith("envContext=")) {
				this.envContext = s.substring(11);
			} else if(s.startsWith("routeOffer=")) {
				this.routeOffer = s.substring(11);
				dme2Entered = true;
			} else if(dme2Entered) {
				sb.append('/');
				sb.append(s);
			}
		}
		pathInfo = sb.toString();
		thisMachine = dm.getHostname();
		if(thisMachine==null) {
			// Can't get from dm... 
			thisMachine = InetAddress.getLocalHost().getHostName();
			thisPort = 0;
		} else {
			thisPort = dm.getPort();
		}
		this.removeSelf=removeSelf;
		refresh();
	}
	
	@Override
	public boolean refresh() {
		try {
			dm.refresh();
			//endpoints = dm.findEndpoints(service, version, envContext, routeOffer, true);
			if(removeSelf) {
//				for(int i=0;i<endpoints.length;++i) {
//					if(endpoints[i].getPort()==thisPort && endpoints[i].getHost().equals(thisMachine))
//						endpoints[i]=null;
				}
			//}
			//return endpoints.length!=0;
		} catch (Exception e) {
			access.log(Level.ERROR, e.getMessage());
		}
		return false;
	}

	private String noEndpointsString() {
		StringBuilder sb = new StringBuilder("No DME2 Endpoints found for ");
		sb.append(service);
		sb.append('/');
		sb.append(version);
		sb.append('/');
		sb.append(envContext);
		sb.append('/');
		sb.append(routeOffer);
		return sb.toString();
	}

	@Override
	public URI get(Locator.Item item) throws LocatorException {
		if(!hasItems()) 
			throw new LocatorException(noEndpointsString());
		if(item == null) 
			return null;

		DME2Item li = ((DME2Item)item);
		// if URI has been created, use it
		if(li.uri!=null)return li.uri;
	
		// URI not created, create it
//		if(li.idx<endpoints.length) {
//			DME2Endpoint de = endpoints[li.idx];
//			if(de!=null) {
//				try {
//					return li.uri=new URI(de.getProtocol().toLowerCase(),null,de.getHost(),de.getPort(),pathInfo,null,null);
//				} catch (URISyntaxException e) {
//					throw new LocatorException(e);
//				}
//			}
//		}
		return null;
	}
	
	@Override
	public boolean hasItems() {
		//return endpoints!=null && endpoints.length>0;
		return true;
	}

	@Override
	public void invalidate(Locator.Item item) throws LocatorException {
		if(item instanceof DME2Item) {
			int idx = ((DME2Item)item).idx;
//			if(idx<endpoints.length) {
//				DME2Endpoint uhoh = endpoints[idx]; // Sometimes, DME2Endpoint, at least on File system, returns bogus entries.
//				endpoints[idx]=null;
//				boolean noneLeft=true;
//				for(int i=0;i<endpoints.length && noneLeft;++i) {
//					noneLeft = endpoints[i]==null;
//				}
//				if(noneLeft && refresh()) { // make sure DME2 isn't giving us the same invalidated entry...
//					for(int i=0;i<endpoints.length && noneLeft;++i) {
//						DME2Endpoint ep = endpoints[i];
//						if(ep != null && 
//						   ep.getHost().equals(uhoh.getHost()) &&
//						   ep.getPort()==uhoh.getPort()) {
//							 endpoints[i]=null;
//						}
//					}
//				}
//				
//			}
		}
	}

	public class DME2Item implements Locator.Item {
		private final int idx;
		private URI uri;
		private DME2Item(int i) {
			idx = i;
			uri = null;
		}
	}

	@Override
	public DME2Item best() throws LocatorException {
		if(!hasItems()) // checks endpoints
			if(!refresh()) throw new LocatorException("No DME2 Endpoints Available");
		
		// Some endpoints in Array are null.  Need sub array of usable endpoints
		//int usable[] = new int[endpoints.length];
		int count=0;
int[] usable = null;
		//		for(int i=0;i<endpoints.length;++i) {
//			if(endpoints[i]!=null) {
//				usable[count++] = i;
//			}
//		}
		switch(count) {
			case 0: refresh(); return null;
			case 1: return new DME2Item(usable[0]);
			default:
				int samemach[] = new int[count];
				int samecount = 0,closecount=0;
				// has to be sortable
				Integer closemach[] = new Integer[count];
				
				// Analyze for Same Machine or Remote machines
//				for(int i=0;i<count;++i) {
//					DME2Endpoint ep = endpoints[usable[i]];
//					String host = ep.getHost();
//					if(thisMachine.equalsIgnoreCase(host)) {
//						samemach[samecount++] = usable[i];
//					} else {
//						closemach[closecount++] = usable[i];
//					}
//				}
				
				switch(samecount) {
					case 0: break;
					case 1: return new DME2Item(samemach[0]);
					default: // return randomized is multiple Endpoints on local machine.
						int i = random.nextInt();
						return new DME2Item(usable[Math.abs(i%samecount)]);
				}
				
				// Analyze for closest remote
				switch(closecount) {
					case 0:	return null;
					case 1: return new DME2Item(closemach[0]);
					default: // return closest machine
						DoubIndex remote[] = new DoubIndex[closecount];
						int remotecount = 0;
						for(int i=0;i<closecount;++i) {
							//DME2Endpoint de = endpoints[usable[i]];
						//	remote[remotecount++] = new DoubIndex(de.getDistance(),i);
						}
						Arrays.sort(remote,new Comparator<DoubIndex> () {
							@Override
							public int compare(DoubIndex a, DoubIndex b) {
								if(a.d<b.d) return -1;
								if(a.d>b.d) return 1;
								return (random.nextInt()%1)==0?1:0;// randomize if the same
							}
							
						});
						return new DME2Item(remote[0].idx);
				}
		}
	}
	
	private static class DoubIndex {
		public final double d;
		public final int idx;
		
		public DoubIndex(double doub, int i) {
			d = doub;
			idx = i;
		}
	}
	@Override
	public DME2Item first() {
//		if(endpoints==null)return null;
//		for(int i=0;i<endpoints.length;++i) {
//			if(endpoints[i]!=null)
//				return new DME2Item(i); 
//		}
		return null;
	}

	@Override
	public DME2Item next(Locator.Item item) throws LocatorException {
		//if(endpoints==null || endpoints.length==0 || !(item instanceof DME2Item))return null;
		int idx = ((DME2Item)item).idx +1;
//		for(int i=idx;i<endpoints.length;++i) {
//			if(endpoints[i]!=null)
//				return new DME2Item(i); 
//		}
// This is a mistake..  will start infinite loops
//		// Did not have any at end... try beginning
//		for(int i=0;i<idx-1;++i) {
//			if(endpoints[i]!=null)
//				return new Item(i); 
//		}
//		// If still nothing, refresh
//		refresh();
		return null;
	}
	
	@Override
	public void destroy() {
	}
}
