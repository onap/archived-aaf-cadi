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
package org.onap.aaf.cadi.aaf.client;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.GregorianCalendar;

import aaf.v2_0.Approval;
import aaf.v2_0.Approvals;
import aaf.v2_0.CredRequest;
import aaf.v2_0.Keys;
import aaf.v2_0.NsRequest;
import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;
import aaf.v2_0.Perm;
import aaf.v2_0.PermKey;
import aaf.v2_0.PermRequest;
import aaf.v2_0.Perms;
import aaf.v2_0.Pkey;
import aaf.v2_0.Request;
import aaf.v2_0.Role;
import aaf.v2_0.RoleKey;
import aaf.v2_0.RolePermRequest;
import aaf.v2_0.RoleRequest;
import aaf.v2_0.Roles;
import aaf.v2_0.UserRole;
import aaf.v2_0.UserRoleRequest;
import aaf.v2_0.UserRoles;
import aaf.v2_0.Users;
import aaf.v2_0.Users.User;

import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Data;
import org.onap.aaf.inno.env.Data.TYPE;
import org.onap.aaf.inno.env.util.Chrono;
import org.onap.aaf.rosetta.env.RosettaDF;
import org.onap.aaf.rosetta.env.RosettaEnv;

public class Examples {
	public static <C> String print(RosettaEnv env, String nameOrContentType, boolean optional) throws APIException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// Discover ClassName
		String className = null;
		String version = null;
		TYPE type = TYPE.JSON; // default
		if(nameOrContentType.startsWith("application/")) {
			for(String ct : nameOrContentType.split("\\s*,\\s*")) {
				for(String elem : ct.split("\\s*;\\s*")) {
					if(elem.endsWith("+json")) {
						type = TYPE.JSON;
						className = elem.substring(elem.indexOf('/')+1, elem.length()-5);
					} else if(elem.endsWith("+xml")) {
						type = TYPE.XML;
						className = elem.substring(elem.indexOf('/')+1, elem.length()-4);
					} else if(elem.startsWith("version=")) {
						version = elem.substring(8);
					}
				}
				if(className!=null && version!=null)break;
			}
			if(className==null) {
				throw new APIException(nameOrContentType + " does not contain Class Information");
			}
		} else {
			className = nameOrContentType;
		}
		
		// No Void.class in aaf.v2_0 package causing errors when trying to use a newVoidv2_0
		// method similar to others in this class. This makes it work, but is it right?
		if ("Void".equals(className)) return "";
				
		if("1.1".equals(version)) {
			version = "v1_0";
		} else if(version!=null) {
			version = "v" + version.replace('.', '_');
		} else {
			version = "v2_0";
		}
		
		Class<?> cls;
		try {
			cls = Examples.class.getClassLoader().loadClass("aaf."+version+'.'+className);
		} catch (ClassNotFoundException e) {
			throw new APIException(e);
		}
		
		Method meth;
		try {
			meth = Examples.class.getDeclaredMethod("new"+cls.getSimpleName()+version,boolean.class);
		} catch (Exception e) {
			throw new APIException("ERROR: " + cls.getName() + " does not have an Example in Code.  Request from AAF Developers");
		}
		
		RosettaDF<C> df = env.newDataFactory(cls);
		df.option(Data.PRETTY);
		
		Object data = meth.invoke(null,optional);
		
		@SuppressWarnings("unchecked")
		String rv = df.newData().load((C)data).out(type).asString();
//		Object obj = df.newData().in(type).load(rv).asObject();
		return rv;
	}
	
	/*
	 *  Set Base Class Request (easier than coding over and over)
	 */
	private static void setOptional(Request req) {
		GregorianCalendar gc = new GregorianCalendar();
		req.setStart(Chrono.timeStamp(gc));
		gc.add(GregorianCalendar.MONTH, 6);
		req.setEnd(Chrono.timeStamp(gc));
//		req.setForce("false");
		
	}
	
	@SuppressWarnings("unused")
	private static Request newRequestv2_0(boolean optional) {
		Request r = new Request();
		setOptional(r);
		return r;
	}
	@SuppressWarnings("unused")
	private static RolePermRequest newRolePermRequestv2_0(boolean optional) {
		RolePermRequest rpr = new RolePermRequest();
		Pkey pkey = new Pkey();
		pkey.setType("com.att.myns.mytype");
		pkey.setInstance("myInstance");
		pkey.setAction("myAction");
		rpr.setPerm(pkey);
		rpr.setRole("com.att.myns.myrole");
		if(optional)setOptional(rpr);
		return rpr;
	}
	
	@SuppressWarnings("unused")
	private static Roles newRolesv2_0(boolean optional) {
		Role r;
		Pkey p;
		Roles rs = new Roles();
		rs.getRole().add(r = new Role());
		r.setName("com.att.myns.myRole");
		r.getPerms().add(p = new Pkey());
		p.setType("com.att.myns.myType");
		p.setInstance("myInstance");
		p.setAction("myAction");
		
		r.getPerms().add(p = new Pkey());
		p.setType("com.att.myns.myType");
		p.setInstance("myInstance");
		p.setAction("myOtherAction");
		
		rs.getRole().add(r = new Role());
		r.setName("com.att.myns.myOtherRole");
		r.getPerms().add(p = new Pkey());
		p.setType("com.att.myns.myOtherType");
		p.setInstance("myInstance");
		p.setAction("myAction");
		
		r.getPerms().add(p = new Pkey());
		p.setType("com.att.myns.myOthertype");
		p.setInstance("myInstance");
		p.setAction("myOtherAction");

		return rs;
	}
	
	
	@SuppressWarnings("unused")
	private static PermRequest newPermRequestv2_0(boolean optional) {
		PermRequest pr = new PermRequest();
		pr.setType("com.att.myns.myType");
		pr.setInstance("myInstance");
		pr.setAction("myAction");
		if(optional) {
			pr.setDescription("Short and meaningful verbiage about the Permission");
			
			setOptional(pr);
		}
		return pr;
	}
	
	@SuppressWarnings("unused")
	private static Perm newPermv2_0(boolean optional) {
		Perm pr = new Perm();
		pr.setType("com.att.myns.myType");
		pr.setInstance("myInstance");
		pr.setAction("myAction");
		pr.getRoles().add("com.att.myns.myRole");
		pr.getRoles().add("com.att.myns.myRole2");
		pr.setDescription("This is my description, and I'm sticking with it");
		if(optional) {
			pr.setDescription("Short and meaningful verbiage about the Permission");
		}
		return pr;
	}


	@SuppressWarnings("unused")
	private static PermKey newPermKeyv2_0(boolean optional) {
		PermKey pr = new PermKey();
		pr.setType("com.att.myns.myType");
		pr.setInstance("myInstance");
		pr.setAction("myAction");
		return pr;
	}
	
	@SuppressWarnings("unused")
	private static Perms newPermsv2_0(boolean optional) {
		Perms perms = new Perms();
		Perm p;
		perms.getPerm().add(p=new Perm());
		p.setType("com.att.myns.myType");
		p.setInstance("myInstance");
		p.setAction("myAction");
		p.getRoles().add("com.att.myns.myRole");
		p.getRoles().add("com.att.myns.myRole2");
		

		perms.getPerm().add(p=new Perm());
		p.setType("com.att.myns.myOtherType");
		p.setInstance("myInstance");
		p.setAction("myOtherAction");
		p.getRoles().add("com.att.myns.myRole");
		p.getRoles().add("com.att.myns.myRole2");

		return perms;
		
	}
	
	@SuppressWarnings("unused")
	private static UserRoleRequest newUserRoleRequestv2_0(boolean optional) {
		UserRoleRequest urr = new UserRoleRequest();
		urr.setRole("com.att.myns.myRole");
		urr.setUser("ab1234@csp.att.com");
		if(optional) setOptional(urr);
		return urr;
	}
	
	@SuppressWarnings("unused")
	private static NsRequest newNsRequestv2_0(boolean optional) {
		NsRequest nr = new NsRequest();
		nr.setName("com.att.myns");
		nr.getResponsible().add("ab1234@csp.att.com");
		nr.getResponsible().add("cd5678@csp.att.com");
		nr.getAdmin().add("zy9876@csp.att.com");
		nr.getAdmin().add("xw5432@csp.att.com");		
		if(optional) {
			nr.setDescription("This is my Namespace to set up");
			nr.setType("APP");
			setOptional(nr);
		}
		return nr;
	}
	
	
	@SuppressWarnings("unused")
	private static Nss newNssv2_0(boolean optional) {
		Ns ns;
		
		Nss nss = new Nss();
		nss.getNs().add(ns = new Nss.Ns());
		ns.setName("com.att.myns");
		ns.getResponsible().add("ab1234@csp.att.com");
		ns.getResponsible().add("cd5678@csp.att.com");
		ns.getAdmin().add("zy9876@csp.att.com");
		ns.getAdmin().add("xw5432@csp.att.com");
		ns.setDescription("This is my Namespace to set up");
		
		nss.getNs().add(ns = new Nss.Ns());
		ns.setName("com.att.myOtherNs");
		ns.getResponsible().add("ab1234@csp.att.com");
		ns.getResponsible().add("cd5678@csp.att.com");
		ns.getAdmin().add("zy9876@csp.att.com");
		ns.getAdmin().add("xw5432@csp.att.com");		
			
		return nss;
	}
	@SuppressWarnings("unused")
	private static RoleRequest newRoleRequestv2_0(boolean optional) {
		RoleRequest rr = new RoleRequest();
		rr.setName("com.att.myns.myRole");
		if(optional) {
			rr.setDescription("This is my Role");
			setOptional(rr);
		}
		return rr;
	}

	@SuppressWarnings("unused")
	private static CredRequest newCredRequestv2_0(boolean optional) {
		CredRequest cr = new CredRequest();
		cr.setId("myID@fully.qualified.domain");
		if(optional) {
			cr.setType(2);
			cr.setEntry("0x125AB256344CE");
		} else {
			cr.setPassword("This is my provisioned password");
		}

		return cr;
	}
	
	@SuppressWarnings("unused")
	private static Users newUsersv2_0(boolean optional) {
		User user;
	
		Users users = new Users();
		users.getUser().add(user = new Users.User());
		user.setId("ab1234@csp.att.com");	
		GregorianCalendar gc = new GregorianCalendar();
		user.setExpires(Chrono.timeStamp(gc));
		
		users.getUser().add(user = new Users.User());
		user.setId("zy9876@csp.att.com");	
		user.setExpires(Chrono.timeStamp(gc));	
			
		return users;
	}

	@SuppressWarnings("unused")
	private static Role newRolev2_0(boolean optional) {
		Role r = new Role();
		Pkey p;
		r.setName("com.att.myns.myRole");
		r.getPerms().add(p = new Pkey());
		p.setType("com.att.myns.myType");
		p.setInstance("myInstance");
		p.setAction("myAction");

        return r;
    }

	@SuppressWarnings("unused")
	private static RoleKey newRoleKeyv2_0(boolean optional) {
		RoleKey r = new RoleKey();
		Pkey p;
		r.setName("com.att.myns.myRole");
        return r;
    }

	@SuppressWarnings("unused")
	private static Keys newKeysv2_0(boolean optional) {
		Keys ks = new Keys();
		ks.getKey().add("Reponse 1");
		ks.getKey().add("Response 2");
        return ks;
    }

	@SuppressWarnings("unused")
	private static UserRoles newUserRolesv2_0(boolean optional) {
		UserRoles urs = new UserRoles();
		UserRole ur = new UserRole();
		ur.setUser("xy1234");
		ur.setRole("com.test.myapp.myRole");
		ur.setExpires(Chrono.timeStamp());
		urs.getUserRole().add(ur);
		
		ur = new UserRole();
		ur.setUser("yx4321");
		ur.setRole("com.test.yourapp.yourRole");
		ur.setExpires(Chrono.timeStamp());
		urs.getUserRole().add(ur);
        return urs;
    }


	@SuppressWarnings("unused")
	private static Approvals newApprovalsv2_0(boolean optional) {
		Approvals as = new Approvals();
		Approval a = new Approval();
		a.setApprover("MyApprover");
		a.setId("MyID");
		a.setMemo("My memo (and then some)");
		a.setOperation("MyOperation");
		a.setStatus("MyStatus");
		a.setTicket("MyTicket");
		a.setType("MyType");
		a.setUpdated(Chrono.timeStamp());
		a.setUser("MyUser");
		as.getApprovals().add(a);
		a = new Approval();
		a.setApprover("MyApprover2");
		a.setId("MyID2");
		a.setMemo("My memo (and then some)2");
		a.setOperation("MyOperation2");
		a.setStatus("MyStatus2");
		a.setTicket("MyTicket2");
		a.setType("MyType2");
		a.setUpdated(Chrono.timeStamp());
		a.setUser("MyUser2");
		as.getApprovals().add(a);
        return as;
    }

	@SuppressWarnings("unused")
	private static Approval newApprovalv2_0(boolean optional) {
		Approval a = new Approval();
		a.setApprover("MyApprover");
		a.setId("MyID");
		a.setMemo("My memo (and then some)");
		a.setOperation("MyOperation");
		a.setStatus("MyStatus");
		a.setTicket("MyTicket");
		a.setType("MyType");
		a.setUpdated(Chrono.timeStamp());
		a.setUser("MyUser");
        return a;
    }

	

	@SuppressWarnings("unused")
	private static aaf.v2_0.Error newErrorv2_0(boolean optional) {
		aaf.v2_0.Error err = new aaf.v2_0.Error();
		err.setMessageId("SVC1403");
		err.setText("MyText %s, %s: The last three digits are usually the HTTP Code");
		err.getVariables().add("Variable 1");
		err.getVariables().add("Variable 2");
		return err;
	}

}
