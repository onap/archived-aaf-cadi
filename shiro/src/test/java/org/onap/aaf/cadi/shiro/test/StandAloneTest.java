	/*
	 * Licensed to the Apache Software Foundation (ASF) under one
	 * or more contributor license agreements.  See the NOTICE file
	 * distributed with this work for additional information
	 * regarding copyright ownership.  The ASF licenses this file
	 * to you under the Apache License, Version 2.0 (the
	 * "License"); you may not use this file except in compliance
	 * with the License.  You may obtain a copy of the License at
	 *
	 *     http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing,
	 * software distributed under the License is distributed on an
	 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
	 * KIND, either express or implied.  See the License for the
	 * specific language governing permissions and limitations
	 * under the License.
	 */
package org.onap.aaf.cadi.shiro.test;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.onap.aaf.cadi.shiro.AAFRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandAloneTest {

	/**
	 * Simple Quickstart application, from Shiro, showing how to use Shiro's API.
	 *
	 * @since 0.9 RC2
	 */
	    private static final transient Logger log = LoggerFactory.getLogger(StandAloneTest.class);

	    public static void main(String[] args) {
	    	if(args.length<3) {
	    		System.out.println("Usage: java StandAloneTest fqi ns passwd");
	    	} else {
	    		
		        String user = args[0];
		        String ns = args[1];
		        String pass = args[2];
		    	
		        // The easiest way to create a Shiro SecurityManager with configured
		        // realms, users, roles and permissions is to use the simple INI config.
		        // We'll do that by using a factory that can ingest a .ini file and
		        // return a SecurityManager instance:
	
		    	Ini ini = new Ini();
		    	Section section = ini.addSection("main");
		    	section.put("aafRealm", "org.onap.aaf.cadi.shiro.AAFRealm");
		    	section.put("securityManager.realms","$aafRealm");
		    	/*
		    	 * Equivalent to shiro.ini
		    	 * 
		    	 *   [main]
		    	 *   aafRealm=org.onap.aaf.cadi.shiro.AAFRealm
		    	 *   securityManager.realms=$aafRealm
		    	 */
		    	Factory<SecurityManager> factory = new IniSecurityManagerFactory(ini);
		    	
		        // Alternative: Use the shiro.ini file at the root of the classpath
		        // (file: and url: prefixes load from files and urls respectively):
		        // Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
		        SecurityManager securityManager = factory.getInstance();
	
		        // for this simple example quickstart, make the SecurityManager
		        // accessible as a JVM singleton.  Most applications wouldn't do this
		        // and instead rely on their container configuration or web.xml for
		        // webapps.  That is outside the scope of this simple quickstart, so
		        // we'll just do the bare minimum so you can continue to get a feel
		        // for things.
		        SecurityUtils.setSecurityManager(securityManager);
	
		        // Now that a simple Shiro environment is set up, let's see what you can do:
	
		        // get the currently executing user:
		        Subject currentUser = SecurityUtils.getSubject();
	
		        // Do some stuff with a Session (no need for a web or EJB container!!!)
		        Session session = currentUser.getSession();
		        session.setAttribute("someKey", "aValue");
		        String value = (String) session.getAttribute("someKey");
		        if (value.equals("aValue")) {
		            log.info("Retrieved the correct value! [" + value + "]");
		        }
		
		        for(int i=0;i<3;++i) {
			        // let's login the current user so we can check against roles and permissions:
			        if (!currentUser.isAuthenticated()) {
			            UsernamePasswordToken token = new UsernamePasswordToken(user,pass);
	//		            UsernamePasswordToken token = new UsernamePasswordToken("lonestarr", "vespa");
			            token.setRememberMe(true);
			            try {
			                currentUser.login(token);
			            } catch (UnknownAccountException uae) {
			                log.info("There is no user with username of " + token.getPrincipal());
			            } catch (IncorrectCredentialsException ice) {
			                log.info("Password for account " + token.getPrincipal() + " was incorrect!");
			            } catch (LockedAccountException lae) {
			                log.info("The account for username " + token.getPrincipal() + " is locked.  " +
			                        "Please contact your administrator to unlock it.");
			            }
			            // ... catch more exceptions here (maybe custom ones specific to your application?
			            catch (AuthenticationException ae) {
			                //unexpected condition?  error?
			            	// AT&T doesn't allow specifics
			            	log.info(ae.getMessage());
			            }
			        }
			        
			        // Uncomment following to test calls after Cache is Cleared
			        // AAFRealm.Singleton.singleton().authz.clearAll();
		
			        //say who they are:
			        //print their identifying principal (in this case, a username):
			        log.info("User [" + currentUser.getPrincipal() + "] logged in successfully.");
		
			        //test NS Write Access
			        String msg = String.format("You are %s in role %s.admin",
			        		currentUser.hasRole(ns+".admin")?"":"not",
			        		ns);
			        log.info(msg);
		
			        //test a typed permission (not instance-level)
			        msg = String.format("You %s have write access into NS %s",
			        		currentUser.isPermitted(ns+".access|*|*")?"":"do not",
			        		ns);
			        log.info(msg);
		        }	
		        //all done - log out!
		        currentUser.logout();
	    	}
	    }
}
