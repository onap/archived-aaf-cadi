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
package org.onap.aaf.cadi.aaf;

import org.onap.aaf.cadi.Permission;

/**
 * A Class that understands the AAF format of Permission (name/type/action)
 *  or String "name|type|action"
 * 
 *
 */
public class AAFPermission implements Permission {
	protected String type,instance,action,key;

	protected AAFPermission() {}

	public AAFPermission(String type, String instance, String action) {
		this.type = type;
		this.instance = instance;
		this.action = action;
		key = type + '|' + instance + '|' + action;
	}
	
	/**
	 * Match a Permission
	 * if Permission is Fielded type "Permission", we use the fields
	 * otherwise, we split the Permission with '|'
	 * 
	 * when the type or action starts with REGEX indicator character ( ! ),
	 * then it is evaluated as a regular expression.
	 * 
	 * If you want a simple field comparison, it is faster without REGEX
	 */
	public boolean match(Permission p) {
		if(p instanceof AAFPermission) {
			AAFPermission ap = (AAFPermission)p;
			// Note: In AAF > 1.0, Accepting "*" from name would violate multi-tenancy
			// Current solution is only allow direct match on Type.
			// 8/28/2014 - added REGEX ability
			if(type.equals(ap.getName()))  
				if(PermEval.evalInstance(instance,ap.getInstance()))
					if(PermEval.evalAction(action,ap.getAction()))
						return true;
		} else {
			// Permission is concatenated together: separated by |
			String[] aaf = p.getKey().split("[\\s]*\\|[\\s]*",3);
			if(aaf.length>0 && type.equals(aaf[0]))
				if(PermEval.evalInstance(instance,aaf.length>1?aaf[1]:"*"))
					if(PermEval.evalAction(action,aaf.length>2?aaf[2]:"*"))
						return true;
		}				
		return false;
	}

	public String getName() {
		return type;
	}
	
	public String getInstance() {
		return instance;
	}
	
	public String getAction() {
		return action;
	}
	
	public String getKey() {
		return key;
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.Permission#permType()
	 */
	public String permType() {
		return "AAF";
	}

	public String toString() {
		return "AAFPermission:\n\tType: " + type + 
				"\n\tInstance: " + instance +
				"\n\tAction: " + action +
				"\n\tKey: " + key;
	}
}
