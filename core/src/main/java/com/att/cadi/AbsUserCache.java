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


import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.att.cadi.Access.Level;
import com.att.cadi.CachedPrincipal.Resp;

/**
 * Implement Fast lookup and Cache for Local User Info
 * 
 * Include ability to add and remove Users
 * 
 * Also includes a Timer Thread (when necessary) to invoke cleanup on expiring Credentials
 * 
 *
 */
public abstract class AbsUserCache<PERM extends Permission> {
	static final int MIN_INTERVAL = 15000;
	static final int MAX_INTERVAL = 1000*60*5; // 5 mins
	private static Timer timer;
	// Map of userName to User
	private final Map<String, User<PERM>> userMap;
	private final Map<String, Miss> missMap;
	private Clean clean;
	protected Access access;
//	private final static Permission teaser = new LocalPermission("***NoPERM****");
	
	protected AbsUserCache(Access access, long cleanInterval, int highCount, int usageCount) {
		this.access = access;
		userMap = new ConcurrentHashMap<String, User<PERM>>();
		missMap = new TreeMap<String,Miss>();
		if(cleanInterval>0) {
			cleanInterval = Math.max(MIN_INTERVAL, cleanInterval);
			synchronized(AbsUserCache.class) { // Lazy instantiate.. in case there is no cleanup needed
				if(timer==null) {
					timer = new Timer("CADI Cleanup Timer",true);
				}
				
				timer.schedule(clean = new Clean(access, cleanInterval, highCount, usageCount), cleanInterval, cleanInterval);
				access.log(Access.Level.INIT, "Cleaning Thread initialized with interval of",cleanInterval, "ms and max objects of", highCount);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public AbsUserCache(AbsUserCache<PERM> cache) {
		this.access = cache.access;
		userMap = cache.userMap;
		missMap = cache.missMap;
		synchronized(AbsUserCache.class) {
			if(cache.clean!=null && cache.clean.lur==null && this instanceof CachingLur) {
				cache.clean.lur=(CachingLur<PERM>)this;
			}
		}
	}

	protected void setLur(CachingLur<PERM> lur) {
		if(clean!=null)clean.lur = lur;
		
	}
	
	protected void addUser(User<PERM> user) {
		userMap.put(user.principal.getName(), user);
	}

	// Useful for looking up by WebToken, etc.
	protected void addUser(String key, User<PERM> user) {
		userMap.put(key, user);
	}
	
	/**
	 * Add miss to missMap.  If Miss exists, or too many tries, returns false.
	 * 
	 * otherwise, returns true to allow another attempt.
	 * 
	 * @param key
	 * @param bs
	 * @return
	 */
	protected boolean addMiss(String key, byte[] bs) {
		Miss miss = missMap.get(key);
		if(miss==null) {
			synchronized(missMap) {
				missMap.put(key, new Miss(bs,clean==null?MIN_INTERVAL:clean.timeInterval));
			}
			return true;
		}
		return miss.add(bs); 
	}

	protected Miss missed(String key) {
		return missMap.get(key);
	}

	protected User<PERM> getUser(String userName) {
		User<PERM> u = userMap.get(userName);
		if(u!=null) {
			u.incCount();
		}
		return u;
	}
	
	protected User<PERM> getUser(Principal principal) {
		return getUser(principal.getName()); 
	}
	
	/**
	 * Removes User from the Cache
	 * @param user
	 */
	protected void remove(User<PERM> user) {
		userMap.remove(user.principal.getName());
	}
	
	/**
	 * Removes user from the Cache
	 * 
	 * @param user
	 */
	public void remove(String user) {
		Object o = userMap.remove(user);
		if(o!=null) {
			access.log(Level.INFO, user,"removed from Client Cache by Request");
		}
	}
	
	/**
	 * Clear all users from the Client Cache
	 */
	public void clearAll() {
		userMap.clear();
	}
	
	public final List<DumpInfo> dumpInfo() {
		List<DumpInfo> rv = new ArrayList<DumpInfo>();
		for(User<PERM> user : userMap.values()) {
			rv.add(new DumpInfo(user));
		}
		return rv;
	}

	/**
	 * The default behavior of a LUR is to not handle something exclusively.
	 */
	public boolean handlesExclusively(Permission pond) {
		return false;
	}
	
	/**
	 * Container calls when cleaning up... 
	 * 
	 * If overloading in Derived class, be sure to call "super.destroy()"
	 */
	public void destroy() {
		if(timer!=null) {
			timer.purge();
			timer.cancel();
		}
	}
	
	

	// Simple map of Group name to a set of User Names
	//	private Map<String, Set<String>> groupMap = new HashMap<String, Set<String>>();

	/**
	 * Class to hold a small subset of the data, because we don't want to expose actual Permission or User Objects
	 */
	public final class DumpInfo {
		public String user;
		public List<String> perms;
		
		public DumpInfo(User<PERM> user) {
			this.user = user.principal.getName();
			perms = new ArrayList<String>(user.perms.keySet());
		}
	}
	
	/**
	 * Clean will examine resources, and remove those that have expired.
	 * 
	 * If "highs" have been exceeded, then we'll expire 10% more the next time.  This will adjust after each run
	 * without checking contents more than once, making a good average "high" in the minimum speed.
	 * 
	 *
	 */
	private final class Clean extends TimerTask {
		private final Access access;
		private CachingLur<PERM> lur;
		
		// The idea here is to not be too restrictive on a high, but to Expire more items by 
		// shortening the time to expire.  This is done by judiciously incrementing "advance"
		// when the "highs" are exceeded.  This effectively reduces numbers of cached items quickly.
		private final int high;
		private long advance;
		private final long timeInterval;
		private final int usageTriggerCount;
		
		public Clean(Access access, long cleanInterval, int highCount, int usageTriggerCount) {
			this.access = access;
			lur = null;
			high = highCount;
			timeInterval = cleanInterval;
			advance = 0;
			this.usageTriggerCount=usageTriggerCount;
		}
		public void run() {
			int renewed = 0;
			int count = 0;
			int total = 0;
			try {
				// look at now.  If we need to expire more by increasing "now" by "advance"
				ArrayList<User<PERM>> al = new ArrayList<User<PERM>>(userMap.values().size());
				al.addAll(0, userMap.values());
				long now = System.currentTimeMillis() + advance;
				for(User<PERM> user : al) {
					++total;
						if(user.count>usageTriggerCount) {
	//						access.log(Level.AUDIT, "Checking Thread", new Date(now));
							boolean touched = false, removed=false;
							if(user.principal instanceof CachedPrincipal) {
								CachedPrincipal cp = (CachedPrincipal)user.principal;
								if(cp.expires() < now) {
									switch(cp.revalidate()) {
										case INACCESSIBLE:
											access.log(Level.AUDIT, "AAF Inaccessible.  Keeping credentials");
											break;
										case REVALIDATED:
											user.resetCount();
			//								access.log(Level.AUDIT, "CACHE revalidated credentials");
											touched = true;
											break;
										default:
											user.resetCount();
											remove(user);
											++count;
											removed = true;
											break;
									}
								}
							}
						
	//						access.log(Level.AUDIT, "User Perm Expires", new Date(user.permExpires));
							if(!removed && lur!=null && user.permExpires<= now ) {
	//							access.log(Level.AUDIT, "Reloading");
								if(lur.reload(user).equals(Resp.REVALIDATED)) {
									user.renewPerm();
									access.log(Level.DEBUG, "Reloaded Perms for",user);
									touched = true;
								}
							}
							user.resetCount();
							if(touched) {
								++renewed;
							}
	
						} else {
							if(user.permExpired()) {
								remove(user);
								++count;
							}
						}
				}
				
				// Clean out Misses
				int missTotal = missMap.keySet().size();
				int miss = 0;
				if(missTotal>0) {
					ArrayList<String> keys = new ArrayList<String>(missTotal);
					keys.addAll(missMap.keySet());
					for(String key : keys) {
						Miss m = missMap.get(key);
						if(m!=null && m.timestamp<System.currentTimeMillis()) {
							synchronized(missMap) {
								missMap.remove(key);
							}
							access.log(Level.INFO, key, "has been removed from Missed Credential Map (" + m.tries + " invalid tries)");
							++miss;
						}
					}
				}
				
				if(count+renewed+miss>0) {
					access.log(Level.INFO, (lur==null?"Cache":lur.getClass().getSimpleName()), "removed",count,
						"and renewed",renewed,"expired Permissions out of", total,"and removed", miss, "password misses out of",missTotal);
				}
	
				// If High (total) is reached during this period, increase the number of expired services removed for next time.
				// There's no point doing it again here, as there should have been cleaned items.
				if(total>high) {
					// advance cleanup by 10%, without getting greater than timeInterval.
					advance = Math.min(timeInterval, advance+(timeInterval/10));
				} else {
					// reduce advance by 10%, without getting lower than 0.
					advance = Math.max(0, advance-(timeInterval/10));
				}
			} catch (Exception e) {
				access.log(Level.ERROR,e.getMessage());
			}
		}
	}
	
	public static class Miss {
		private static final int MAX_TRIES = 3;

		long timestamp;
		byte[][] array;

		private long timetolive;

		private int tries;
		
		public Miss(byte[] first, long timeInterval) {
			array = new byte[MAX_TRIES][];
			array[0]=first;
			timestamp = System.currentTimeMillis() + timeInterval;
			this.timetolive = timeInterval;
			tries = 1;
		}
		
		public boolean mayContinue(byte[] bs) {
			if(++tries > MAX_TRIES) return false;
			for(byte[] a : array) {
				if(a==null)return true;
				if(equals(a,bs)) {
					return false;
				}
			}
			return true;
		}

		public synchronized boolean add(byte[] bc) {
			if(++tries>MAX_TRIES)return false;
			timestamp = System.currentTimeMillis()+timetolive;
			for(int i=0;i<MAX_TRIES;++i) {
				if(array[i]==null) {
					array[i]=bc;
					return true; // add to array, and allow more tries
				} else if(equals(array[i],bc)) {
					return false;
				}
			}
			return false; // no more tries until cache cleared.
		}
		
		private boolean equals(byte[] src, byte[] target) {
			if(target.length==src.length) {
				for(int j=0;j<src.length;++j) {
					if(src[j]!=target[j]) return false;
				}
				return true; // same length and same chars
			}
			return false;
		}
	}
	
	/**
	 * Report on state
	 */
	public String toString() {
		return getClass().getSimpleName() + 
				" Cache:\n  Users Cached: " +
				userMap.size() +
				"\n  Misses Saved: " +
				missMap.size() +
				'\n';
				
	}

	public void clear(Principal p, StringBuilder sb) {
		sb.append(toString());
		userMap.clear();
		missMap.clear();
		access.log(Level.AUDIT, p.getName(),"has cleared User Cache in",getClass().getSimpleName());
		sb.append("Now cleared\n");
	}

}
