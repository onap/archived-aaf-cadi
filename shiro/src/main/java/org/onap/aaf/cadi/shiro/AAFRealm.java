/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.cadi.shiro;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.onap.aaf.cadi.aaf.v2_0.AAFCon;
import org.onap.aaf.cadi.aaf.v2_0.AAFLurPerm;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.filter.MapBathConverter;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAFRealm extends AuthorizingRealm {
	public static final String AAF_REALM = "AAFRealm";
	private static final Logger logger =  LoggerFactory.getLogger(AAFRealm.class);
	private static Singleton singleton = Singleton.singleton();
	
	private static class Singleton {
		private AAFCon<?> acon;
		private AAFAuthn<?> authn;
//		private Set<Class<? extends AuthenticationToken>> supports;
		private AAFLurPerm authz;
		private MapBathConverter mbc;
		private Map<String,String> idMap;
		private Singleton() {
			mbc = null;
			idMap = null;
			String cadi_prop_files = access.getProperty(Config.CADI_PROP_FILES);
			if(cadi_prop_files==null) {
				String msg = Config.CADI_PROP_FILES + " in VM Args is required to initialize AAFRealm.";
				access.log(Level.INFO,msg);
				throw new RuntimeException(msg);
			} else {
				try {
					acon = AAFCon.newInstance(access);
					authn = acon.newAuthn();
					authz = acon.newLur(authn);
					
					final String csv = access.getProperty(Config.CADI_BATH_CONVERT);
					if(csv!=null) {
						try {
							mbc = new MapBathConverter(access, new CSV(access,csv));
							access.log(Level.INFO, "MapBathConversion enabled with file ",csv);
							idMap = Collections.synchronizedMap(new TreeMap<String,String>());
							// Load 
							for(Entry<String, String> es : mbc.map().entrySet()) {
								String oldID = es.getKey();
								if(oldID.startsWith("Basic ")) {
									oldID = Symm.base64noSplit.decode(oldID.substring(6));
									int idx = oldID.indexOf(':');
									if(idx>=0) {
										oldID = oldID.substring(0, idx);
									}
								}
								String newID = es.getValue();
								if(newID.startsWith("Basic ")) {
									newID = Symm.base64noSplit.decode(newID.substring(6));
									int idx = newID.indexOf(':');
									if(idx>=0) {
										newID = newID.substring(0, idx);
									}
								}
								idMap.put(oldID,newID);
							}
						} catch (IOException e) {
							access.log(e);
						}
					}
				} catch (APIException | CadiException | LocatorException e) {
					String msg = "Cannot initiate AAFRealm";
					access.log(Level.ERROR,e,msg);
					throw new RuntimeException(msg,e);
				}
			}
			
			// There is only one of these.  If there are more, put back 
//			supports = Collections.synchronizedSet(new HashSet<>());
//			supports.add(UsernamePasswordToken.class);
		}
		
		public static synchronized Singleton singleton() {
			if(singleton==null) {
				singleton = new Singleton();
			}
			return singleton;
		}

		// pick up cadi_prop_files from VM_Args
		private final PropAccess access = new PropAccess() {
			@Override
			public void log(Exception e, Object... elements) {
				logger.error(buildMsg(Level.ERROR, elements).toString(),e);
			}
		
			@Override
			public void log(Level level, Object... elements) {
				if(willLog(level)) {
					String str = buildMsg(level, elements).toString();
					switch(level) {
						case WARN:
						case AUDIT:
							logger.warn(str);
							break;
						case DEBUG:
							logger.debug(str);
							break;
						case ERROR:
							logger.error(str);
							break;
						case INFO:
						case INIT:
							logger.info(str);
							break;
						case NONE:
							break;
						case TRACE:
							logger.trace(str);
							break;
					}
				}
			}
		
			@Override
			public void printf(Level level, String fmt, Object... elements) {
				if(willLog(level)) {
					String str = String.format(fmt, elements);
					switch(level) {
						case WARN:
						case AUDIT:
							logger.warn(str);
							break;
						case DEBUG:
							logger.debug(str);
							break;
						case ERROR:
							logger.error(str);
							break;
						case INFO:
						case INIT:
							logger.info(str);
							break;
						case NONE:
							break;
						case TRACE:
							logger.trace(str);
							break;
					}
				}
			}
		
			@Override
			public boolean willLog(Level level) {
				if(super.willLog(level)) {
					switch(level) {
						case WARN:
						case AUDIT:
							return logger.isWarnEnabled();
						case DEBUG:
							return logger.isDebugEnabled();
						case ERROR:
							return logger.isErrorEnabled();
						case INFO:
						case INIT:
							return logger.isInfoEnabled();
						case NONE:
							return false;
						case TRACE:
							return logger.isTraceEnabled();
					}
				}
				return false;
			}
		};
	}		
	
	/**
	 * 
	 * There appears to be no configuration objects or references available for CADI to start with.
	 *  
	 */

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		final UsernamePasswordToken upt = (UsernamePasswordToken)token;
		final String user = upt.getUsername();
		String authUser = user; 
		final String password=new String(upt.getPassword());
		String authPassword = password;
		if(singleton.mbc!=null) {
			try {
				final String oldBath = "Basic " + Symm.base64noSplit.encode(user+':'+password);
				String bath = singleton.mbc.convert(singleton.access, oldBath);
				if(bath!=oldBath) {
					bath = Symm.base64noSplit.decode(bath.substring(6));
					int colon = bath.indexOf(':');
					if(colon>=0) {
						authUser = bath.substring(0, colon);
						authPassword = bath.substring(colon+1);	
					}
				}
			} catch (IOException e) {
				singleton.access.log(e);
			} 
		}
		String err;
		try {
			err = singleton.authn.validate(authUser,authPassword);
			if(err != null) {
				singleton.access.log(Level.INFO, err);
				throw new AuthenticationException(err);
			}

		} catch (IOException e) {
			singleton.access.log(e,"Credential cannot be validated");
		}
		
	    return new AAFAuthenticationInfo(
	    		singleton.access,
	    		user,
	    		password
	    );
	}

	@Override
	protected void assertCredentialsMatch(AuthenticationToken atoken, AuthenticationInfo ai)throws AuthenticationException {
		if(ai instanceof AAFAuthenticationInfo) {
			if(!((AAFAuthenticationInfo)ai).matches(atoken)) {
				throw new AuthenticationException("Credentials do not match");
			}
		} else {
			throw new AuthenticationException("AuthenticationInfo is not an AAFAuthenticationInfo");
		}
	}

	@Override
	protected AAFAuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Principal bait = (Principal)principals.getPrimaryPrincipal();
		Principal newBait = bait;
		if(singleton.idMap!=null) {
			final String newID = singleton.idMap.get(bait.getName());
			if(newID!=null) {
				singleton.access.printf(Level.INFO,"Successful authentication Translation %s to %s",bait.getName(), newID); 
				newBait = new Principal() {
					@Override
					public String getName() {
						return newID;
					}
				};
			}
		}
		List<Permission> pond = new ArrayList<>();
		singleton.authz.fishAll(newBait,pond);
		return new AAFAuthorizationInfo(singleton.access,bait,pond);
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		// Only one was being loaded.  If more are needed uncomment the multi-class mode
		return UsernamePasswordToken.class.equals(token);
//		return singleton.supports.contains(token.getClass());
	}

	@Override
	public String getName() {
		return AAF_REALM;
	}

}
