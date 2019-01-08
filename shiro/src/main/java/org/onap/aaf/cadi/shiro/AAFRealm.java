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
import java.io.PrintStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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
public class AAFRealm extends AuthorizingRealm {
	
	final static Logger logger = Logger.getLogger(AAFRealm.class);
	
	public static final String AAF_REALM = "AAFRealm";
	
	private PropAccess access;
	private AAFCon<?> acon;
	private AAFAuthn<?> authn;
	private HashSet<Class<? extends AuthenticationToken>> supports;
	private AAFLurPerm authz;
	private MapBathConverter mbc;
	private Map<String,String> idMap;
	

	/**
	 * 
	 * There appears to be no configuration objects or references available for CADI to start with.
	 *  
	 */
	public AAFRealm () {
		access = new PropAccess(); // pick up cadi_prop_files from VM_Args
		mbc = null;
		idMap = null;
		String cadi_prop_files = access.getProperty(Config.CADI_PROP_FILES);
		if(cadi_prop_files==null) {
			String msg = Config.CADI_PROP_FILES + " in VM Args is required to initialize AAFRealm.";
			access.log(Level.INIT,msg);
			throw new RuntimeException(msg);
		} else {
			try {
				String log4jConfigFile = "./etc/org.onap.cadi.logging.cfg";
		        PropertyConfigurator.configure(log4jConfigFile);
		        System.setOut(createLoggingProxy(System.out));
		        System.setErr(createLoggingProxy(System.err));
			} catch(Exception e) {
				e.printStackTrace();
			}
			//System.out.println("Configuration done");
			try {
				acon = AAFCon.newInstance(access);
				authn = acon.newAuthn();
				authz = acon.newLur(authn);
				
				final String csv = access.getProperty(Config.CADI_BATH_CONVERT);
				if(csv!=null) {
					try {
						mbc = new MapBathConverter(access, new CSV(csv));
						logger.info("MapBathConversion enabled with file "+csv);
						idMap = new TreeMap<String,String>();
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
						logger.error(e.getMessage(), e);
					}
				}
			} catch (APIException | CadiException | LocatorException e) {
				String msg = "Cannot initiate AAFRealm";
				logger.info(msg + " "+ e.getMessage(), e);
				throw new RuntimeException(msg,e);
			}
		}
		supports = new HashSet<Class<? extends AuthenticationToken>>();
		supports.add(UsernamePasswordToken.class);
	}
	public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            public void print(final String string) {
                realPrintStream.print(string);
                logger.info(string);
            }
        };
    }

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		logger.debug("AAFRealm.doGetAuthenticationInfo :"+token);
		
		final UsernamePasswordToken upt = (UsernamePasswordToken)token;
		final String user = upt.getUsername();
		String authUser = user; 
		final String password=new String(upt.getPassword());
		String authPassword = password;
		if(mbc!=null) {
			try {
				final String oldBath = "Basic " + Symm.base64noSplit.encode(user+':'+password);
				String bath = mbc.convert(access, oldBath);
				if(bath!=oldBath) {
					bath = Symm.base64noSplit.decode(bath.substring(6));
					int colon = bath.indexOf(':');
					if(colon>=0) {
						authUser = bath.substring(0, colon);
						authPassword = bath.substring(colon+1);
					}
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} 
		}
		String err;
		try {
			err = authn.validate(authUser,authPassword);
		} catch (IOException e) {
			err = "Credential cannot be validated";
			logger.error(err, e);
		}
		
		if(err != null) {
			logger.debug(err);
			throw new AuthenticationException(err);
		}

	    return new AAFAuthenticationInfo(
	    		access,
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
		logger.debug("AAFRealm.doGetAuthenthorizationInfo");
		Principal bait = (Principal)principals.getPrimaryPrincipal();
		Principal newBait = bait;
		if(idMap!=null) {
			final String newID = idMap.get(bait.getName());
			if(newID!=null) {
				newBait = new Principal() {
					@Override
					public String getName() {
						return newID;
					}
				};
			}
		}
		List<Permission> pond = new ArrayList<>();
		authz.fishAll(newBait,pond);
		
		return new AAFAuthorizationInfo(access,bait,pond);
       
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		return supports.contains(token.getClass());
	}

	@Override
	public String getName() {
		return AAF_REALM;
	}

}
