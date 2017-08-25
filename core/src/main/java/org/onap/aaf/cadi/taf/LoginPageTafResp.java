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
package org.onap.aaf.cadi.taf;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.Locator.Item;

public class LoginPageTafResp extends AbsTafResp {
	private final HttpServletResponse httpResp;
	private final String loginPageURL;

	private LoginPageTafResp(Access access, final HttpServletResponse resp, String loginPageURL) {
		super(access, null, "Multiple Possible HTTP Logins available.  Redirecting to Login Choice Page");
		httpResp = resp;
		this.loginPageURL = loginPageURL;
	}

	@Override
	public RESP authenticate() throws IOException {
		httpResp.sendRedirect(loginPageURL);
		return RESP.HTTP_REDIRECT_INVOKED;
	}
	
	@Override
	public RESP isAuthenticated() {
		return RESP.TRY_AUTHENTICATING;
	}
	
	public static TafResp create(Access access, Locator<URI> locator, final HttpServletResponse resp, List<Redirectable> redir) {
		if(locator!=null) {
			try {
				Item item = locator.best();
				URI uri = locator.get(item);
				if(uri!=null) {
					StringBuilder sb = new StringBuilder(uri.toString());
					String query = uri.getQuery();
					boolean first = query==null || query.length()==0;
					int count=0;
					for(Redirectable t : redir) {
						if(first) {
							sb.append('?');
							first=false;
						}
						else sb.append('&');
						sb.append(t.get());
						++count;
					}
					if(count>0)return new LoginPageTafResp(access, resp, sb.toString());
				}
			} catch (Exception e) {
				access.log(e, "Error deriving Login Page location");
			}
		} else if(!redir.isEmpty()) { 
			access.log(Level.DEBUG,"LoginPage Locator is not configured. Taking first Redirectable Taf");
			return redir.get(0);
		}
		return NullTafResp.singleton();
	}
}
