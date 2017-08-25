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
package org.onap.aaf.cadi.lur;

import java.io.IOException;
import java.security.Principal;

import org.onap.aaf.cadi.GetCred;
import org.onap.aaf.cadi.Symm;

public class ConfigPrincipal implements Principal, GetCred {
	private String name;
	private byte[] cred;
	private String content;

	public ConfigPrincipal(String name, String passwd) {
		this.name = name;
		this.cred = passwd.getBytes();
		content = null;
	}

	public ConfigPrincipal(String name, byte[] cred) {
		this.name = name;
		this.cred = cred;
		content = null;
	}

	public String getName() {
		return name;
	}
	
	public byte[] getCred() {
		return cred;
	}

	public String toString() {
		return name;
	}
	
	public String getAsBasicAuthHeader() throws IOException {
		if(content ==null) {
			String s = name + ':' + new String(cred);
			content = "Basic " + Symm.base64.encode(s);  
		} else if(!content.startsWith("Basic ")) { // content is the saved password from construction
			String s = name + ':' + content;
			content = "Basic " + Symm.base64.encode(s);  
		}
		return content;
	}
}
