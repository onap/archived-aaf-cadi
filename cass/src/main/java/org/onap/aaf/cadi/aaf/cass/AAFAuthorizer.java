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
package org.onap.aaf.cadi.aaf.cass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.IAuthorizer;
import org.apache.cassandra.auth.IResource;
import org.apache.cassandra.auth.Permission;
import org.apache.cassandra.auth.PermissionDetails;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.aaf.v2_0.AbsAAFLur;
import org.onap.aaf.cadi.lur.LocalPermission;

public class AAFAuthorizer extends AAFBase implements IAuthorizer {
	// Returns every permission on the resource granted to the user.
    public Set<Permission> authorize(AuthenticatedUser user, IResource resource) {
    	String uname, rname;
    	access.log(Level.DEBUG,"Authorizing",uname=user.getName(),"for",rname=resource.getName());

    	Set<Permission> permissions;

    	if(user instanceof AAFAuthenticatedUser) {
	        AAFAuthenticatedUser aafUser = (AAFAuthenticatedUser) user;
			aafUser.setAnonymous(false);
			
			if(aafUser.isLocal()) {
				permissions = checkPermissions(aafUser, new LocalPermission(
					rname.replaceFirst("data", cluster_name)
				));
			} else {
		   		permissions = checkPermissions(
		   				aafUser,
		   				perm_type,
		   				':'+rname.replaceFirst("data", cluster_name).replace('/', ':'));
			}
    	} else {
    		permissions = Permission.NONE;
    	}
    	
    	access.log(Level.INFO,"Permissions on",rname,"for",uname,':', permissions);

        return permissions;
    }
    
    /**
     * Check only for Localized IDs (see cadi.properties)
     * @param aau
     * @param perm
     * @return
     */
    private Set<Permission> checkPermissions(AAFAuthenticatedUser aau, LocalPermission perm) {
    	if(localLur.fish(aau.getFullName(), perm)) {
//        	aau.setSuper(true);
        	return Permission.ALL;
    	} else {
    		return Permission.NONE;
    	}
    }
    
    /**
     * Check remoted AAF Permissions
     * @param aau
     * @param type
     * @param instance
     * @return
     */
    private Set<Permission> checkPermissions(AAFAuthenticatedUser aau, String type, String instance) {
		// Can perform ALL actions
        String fullName = aau.getFullName();
        PermHolder ph = new PermHolder(aau);
        aafLur.fishOneOf(fullName, ph,type,instance,actions);
        return ph.permissions;
    }   

    private class PermHolder {
    	private AAFAuthenticatedUser aau;
		public PermHolder(AAFAuthenticatedUser aau) {
    		this.aau = aau;
    	}
    	public Set<Permission> permissions = Permission.NONE;
		public void mutable() {
			if(permissions==Permission.NONE) {
				permissions = new HashSet<Permission>();
			}
		}
    };
 
   /**
    * This specialty List avoid extra Object Creation, and allows the Lur to do a Vistor on all appropriate Perms
    */
   private static final ArrayList<AbsAAFLur.Action<PermHolder>> actions = new ArrayList<AbsAAFLur.Action<PermHolder>>();
   static {
	   actions.add(new AbsAAFLur.Action<PermHolder>() {
		public String getName() {
			return "*";
		}
		
		public boolean exec(PermHolder a) {
        	a.aau.setSuper(true);
        	a.permissions = Permission.ALL;
			return true;
		}
	   });
	   
	   actions.add(new AbsAAFLur.Action<PermHolder>() {
		public String getName() {
			return "SELECT";
		}
		
		public boolean exec(PermHolder ph) {
			ph.mutable();
        	ph.permissions.add(Permission.SELECT);
			return false;
		}
	   });
	   actions.add(new AbsAAFLur.Action<PermHolder>() {
		public String getName() {
			return "MODIFY";
		}
		
		public boolean exec(PermHolder ph) {
			ph.mutable();
        	ph.permissions.add(Permission.MODIFY);
			return false;
		}
	   });
	   actions.add(new AbsAAFLur.Action<PermHolder>() {
		public String getName() {
			return "CREATE";
		}
		
		public boolean exec(PermHolder ph) {
			ph.mutable();
        	ph.permissions.add(Permission.CREATE);
			return false;
		}
	   });

	   actions.add(new AbsAAFLur.Action<PermHolder>() {
		public String getName() {
			return "ALTER";
		}
		
		public boolean exec(PermHolder ph) {
			ph.mutable();
        	ph.permissions.add(Permission.ALTER);
			return false;
		}
	   });
	   actions.add(new AbsAAFLur.Action<PermHolder>() {
		public String getName() {
			return "DROP";
		}
		
		public boolean exec(PermHolder ph) {
			ph.mutable();
        	ph.permissions.add(Permission.DROP);
			return false;
		}
	   });
	   actions.add(new AbsAAFLur.Action<PermHolder>() {
		public String getName() {
			return "AUTHORIZE";
		}
		
		public boolean exec(PermHolder ph) {
			ph.mutable();
        	ph.permissions.add(Permission.AUTHORIZE);
			return false;
		}
	   });


   }; 
   
   
    public void grant(AuthenticatedUser performer, Set<Permission> permissions, IResource resource, String to) throws RequestExecutionException {
    	access.log(Level.INFO, "Use AAF CLI to grant permission(s) to user/role");
    }

    public void revoke(AuthenticatedUser performer, Set<Permission> permissions, IResource resource, String from) throws RequestExecutionException {
    	access.log(Level.INFO,"Use AAF CLI to revoke permission(s) for user/role");
    }

    public Set<PermissionDetails> list(AuthenticatedUser performer, Set<Permission> permissions, IResource resource, String of) throws RequestValidationException, RequestExecutionException {
    	access.log(Level.INFO,"Use AAF CLI to find the list of permissions");
    	return null;
    }

    // Called prior to deleting the user with DROP USER query. Internal hook, so no permission checks are needed here.
    public void revokeAll(String droppedUser) {
    	access.log(Level.INFO,"Use AAF CLI to revoke permission(s) for user/role");
    }

    // Called after a resource is removed (DROP KEYSPACE, DROP TABLE, etc.).
    public void revokeAll(IResource droppedResource) {
    	access.log(Level.INFO,"Use AAF CLI to delete the unused permission", droppedResource.getName());
    }

}
