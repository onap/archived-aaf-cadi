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
package org.onap.aaf.cadi.locator;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.routing.GreatCircle;

import org.onap.aaf.inno.env.util.Split;

/**
 * This Locator is to handle Hot Peer load protection, when the Servers are 
 * 	1) Static
 * 	2) Well known client URL
 * 
 * The intention is to change traffic over to the Hot Peer, if a server goes down, and reinstate
 * when it is back up.
 * 
 * Example of this kind of Service is a MS Certificate Server
 * 
 *
 *
 * @param <CLIENT>
 */
public abstract class HotPeerLocator<CLIENT> implements Locator<CLIENT> {
		private final String[] urlstrs;
		private final CLIENT[] clients;
		private final long[] failures;
		private final double[] distances;
		private int preferred;
		private long invalidateTime;
		private Thread refreshThread;
		protected Access access;

		/**
		 * Construct:  Expect one or more Strings in the form:
		 * 		192.555.112.223:39/38.88087/-77.30122
		 *    separated by commas
		 * 
		 * @param trans
		 * @param urlstr
		 * @param invalidateTime
		 * @param localLatitude
		 * @param localLongitude
		 * @throws LocatorException
		 */
		@SuppressWarnings("unchecked")
		protected HotPeerLocator(Access access, final String urlstr, final long invalidateTime, final String localLatitude, final String localLongitude) throws LocatorException {
			this.access = access;
	 		urlstrs = Split.split(',', urlstr);
	 		clients = (CLIENT[])new Object[urlstrs.length];
	 		failures = new long[urlstrs.length];
	 		distances= new double[urlstrs.length];
	 		this.invalidateTime = invalidateTime;
	 		
	 		double distance = Double.MAX_VALUE;
	 		for(int i=0;i<urlstrs.length;++i) {
	 			String[] info = Split.split('/', urlstrs[i]);
	 			if(info.length<3) {
	 				throw new LocatorException("Configuration needs LAT and LONG, i.e. ip:port/lat/long");
	 			}
	 			try {
	 				clients[i] = _newClient(urlstrs[i]);
	 				failures[i] = 0L;
	 			} catch(LocatorException le) {
	 				failures[i] = System.currentTimeMillis()+invalidateTime;
	 			}
	 			
	 			double d = GreatCircle.calc(info[1],info[2],localLatitude,localLongitude);
	 			distances[i]=d;
	 			
	 			// find preferred server
	 			if(d<distance) {
	 				preferred = i;
	 				distance=d;
	 			}
	 		}
	 		
	 		access.printf(Level.INIT,"Preferred Client is %s",urlstrs[preferred]);
	 		for(int i=0;i<urlstrs.length;++i) {
	 			if(i!=preferred) {
	 				access.printf(Level.INIT,"Alternate Client is %s",urlstrs[i]);
	 			}
	 		}
		}
		
		protected abstract CLIENT _newClient(String hostInfo) throws LocatorException;
		/**
		 * If client can reconnect, then return.  Otherwise, destroy and return null;
		 * @param client
		 * @return
		 * @throws LocatorException
		 */
		protected abstract CLIENT _invalidate(CLIENT client);
		
		protected abstract void _destroy(CLIENT client);
		
		@Override
		public Item best() throws LocatorException {
			if(failures[preferred]==0L) {
				return new HPItem(preferred);
			} else {
				long now = System.currentTimeMillis();
				double d = Double.MAX_VALUE;
				int best = -1;
				boolean tickle = false;
				// try for best existing client
				for(int i=0;i<urlstrs.length;++i) {
					if(failures[i]<now && distances[i]<d) {
						if(clients[i]!=null) {
							best = i;
							break;
						} else {
							tickle = true; // There's some failed clients which can be restored
						}
					}
				}
				if(best<0 && tickle) {
					tickle=false;
					if(refresh()) {
						// try again
						for(int i=0;i<urlstrs.length;++i) {
							if(failures[i]==0L && distances[i]<d) {
								if(clients[i]!=null) {
									best = i;
									break;
								}
							}
						}
					}
				}
			
				/*
				 * If a valid client is available, but there are some that can refresh, return the client immediately
				 * but start a Thread to do the background Client setup.
				 */
				if(tickle) {
					synchronized(clients) {
						if(refreshThread==null) {
							refreshThread = new Thread(new Runnable(){
								@Override
								public void run() {
									refresh();
									refreshThread = null;
								}
							});
							refreshThread.setDaemon(true);
							refreshThread.start();
						}
					}
				}
				
				if(best<0) {
					throw new LocatorException("No Clients available");
				}

				
				return new HPItem(best);
			}
		}
		

		@Override
		public CLIENT get(Item item) throws LocatorException {
			HPItem hpi = (HPItem)item;
			CLIENT c = clients[hpi.idx];
			if(c==null) {
				if(failures[hpi.idx]>System.currentTimeMillis()) {
					throw new LocatorException("Client requested is invalid");	
				} else {
					synchronized(clients) {
						c = _newClient(urlstrs[hpi.idx]);
						failures[hpi.idx]=0L;
					}
				}
			} else if(failures[hpi.idx]>0){
				throw new LocatorException("Client requested is invalid");
			}
			return c;
		}
		
		public String info(Item item) {
			HPItem hpi = (HPItem)item;
			if(hpi!=null && hpi.idx<urlstrs.length) {
				return urlstrs[hpi.idx];
			} else {
				return "Invalid Item";
			}
		}

		@Override
		public boolean hasItems() {
			for(int i=0;i<clients.length;++i) {
				if(clients[i]!=null && failures[i]==0L) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public synchronized void invalidate(Item item) throws LocatorException {
			HPItem hpi = (HPItem)item;
			failures[hpi.idx] = System.currentTimeMillis() + invalidateTime;
			CLIENT c = clients[hpi.idx];
			clients[hpi.idx] = _invalidate(c);
		}
		
		@Override
		public Item first() throws LocatorException {
			return new HPItem(0);
		}
		
		@Override
		public Item next(Item item) throws LocatorException {
			HPItem hpi = (HPItem)item;
			if(++hpi.idx>=clients.length) {
				return null;
			}
			return hpi;
		}
		
		@Override
		public boolean refresh() {
			boolean force = !hasItems(); // If no Items at all, reset
			boolean rv = true;
			long now = System.currentTimeMillis();
			for(int i=0;i<clients.length;++i) {
				if(failures[i]>0L && (failures[i]<now || force)) { // retry
					try {
						synchronized(clients) {
							if(clients[i]==null) {
								clients[i]=_newClient(urlstrs[i]);
							}
							failures[i]=0L;
						}
					} catch (LocatorException e) {
						failures[i]=now+invalidateTime;
						rv = false;
					}
				}
			}
			return rv;
		}
		
		@Override
		public void destroy() {
			for(int i=0;i<clients.length;++i) {
				if(clients[i]!=null) {
					_destroy(clients[i]);
					clients[i] = null;
				}
			}
		}

		private static class HPItem implements Item {
			private int idx;

			public HPItem(int i) {
				idx = i;
			}
		}
		

		/*
		 * Convenience Functions
		 */
		public CLIENT bestClient() throws LocatorException {
			return get(best());
		}

		public boolean invalidate(CLIENT client) throws LocatorException {
			for(int i=0;i<clients.length;++i) {
				if(clients[i]==client) { // yes, "==" is appropriate here.. Comparing Java Object Reference
					invalidate(new HPItem(i));
					return true;
				}
			}
			return false;
		}

	}
