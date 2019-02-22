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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.shiro.subject.PrincipalCollection;

public class AAFPrincipalCollection implements PrincipalCollection {
	
	private static final long serialVersionUID = 558246013419818831L;
	private static final Set<String> realmSet;
	private final Principal principal;
	private List<Principal> list=null;
	private Set<Principal> set=null;

	static {
		realmSet = new HashSet<String>();
		realmSet.add(AAFRealm.AAF_REALM);
	}
	
	public AAFPrincipalCollection(Principal p) {
		principal = p;

	}

	public AAFPrincipalCollection(final String principalName) {
		principal = 	new Principal() {
			private final String name = principalName;
			@Override
			public String getName() {
				return name;
			}
		};
	}

	@Override
	public Iterator<Principal> iterator() {
		return null;
	}

	@Override
	public List<Principal> asList() {
		if(list==null) {
			list = new ArrayList<Principal>();
		}
		list.add(principal);
		return list;
	}

	@Override
	public Set<Principal> asSet() {
		if(set==null) {
			set = new HashSet<Principal>();
		}
		set.add(principal);
		return set;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<T> byType(Class<T> cls) {
		Collection<T> coll = new ArrayList<T>();
		if(cls.isAssignableFrom(Principal.class)) {
			coll.add((T)principal);
		}
		return coll;
	}

	@Override
	public Collection<Principal> fromRealm(String realm) {
		if(AAFRealm.AAF_REALM.equals(realm)) {
			return asList();
		} else {
			return new ArrayList<Principal>();
		}
	}

	@Override
	public Principal getPrimaryPrincipal() {
		return principal;
	}

	@Override
	public Set<String> getRealmNames() {
		return realmSet;
	}

	@Override
	public boolean isEmpty() {
		return principal==null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T oneByType(Class<T> cls) {
		if(cls.isAssignableFrom(Principal.class)) {
			return (T)principal;
		}
		return null;
	}

}
