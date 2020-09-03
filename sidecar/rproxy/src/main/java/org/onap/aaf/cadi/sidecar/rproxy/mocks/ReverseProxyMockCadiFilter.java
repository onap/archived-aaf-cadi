/**
 * ﻿============LICENSE_START=======================================================
 * org.onap.aaf
 * ================================================================================
 * Copyright © 2018 European Software Marketing Ltd.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aaf.cadi.sidecar.rproxy.mocks;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiWrap;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.cadi.taf.TafResp;

public class ReverseProxyMockCadiFilter implements Filter {

	private FakeLur fakeLur = new FakeLur();

	static class FakeLur implements Lur {

		@Override
		public void fishAll(Principal bait, List<Permission> permissions) {

			final String WildcardPermissionType = "test.wildcard.access";
			final String MultiplePermissionType = "test.multiple.access";
			final String TestAuthAccessPermissionType = "test.auth.access";
			final String PermissionAction = "permission";

			String principalName = bait.getName();

			if (principalName != null && principalName.equals("UserWithInstanceActionWildcardPermissionGranted")) {
				permissions.add(new AAFPermission(null, WildcardPermissionType, "*", "*"));
			} else if (principalName != null && principalName.equals("UserWithInstanceWildcardPermissionGranted")) {
				permissions.add(new AAFPermission(null, WildcardPermissionType, "*", PermissionAction));
			} else if (principalName != null && principalName.equals("UserWithActionWildcardPermissionGranted")) {
				permissions.add(new AAFPermission(null, WildcardPermissionType, "first", "*"));
			} else {

				// For single permission test
				permissions.add(new AAFPermission(null, "test.single.access", "single", PermissionAction));

				// For multiple permission test
				permissions.add(new AAFPermission(null, MultiplePermissionType, "first", PermissionAction));
				permissions.add(new AAFPermission(null, MultiplePermissionType, "second", PermissionAction));
				permissions.add(new AAFPermission(null, MultiplePermissionType, "third", PermissionAction));

				// For transaction id test
				permissions.add(new AAFPermission(null, TestAuthAccessPermissionType, "rest", "write"));
				permissions.add(new AAFPermission(null, TestAuthAccessPermissionType, "rpc", "write"));
			}
		}

		@Override
		public Permission createPerm(String p) {
			return null;
		}

		@Override
		public boolean fish(Principal bait, Permission... pond) {
			return false;
		}

		@Override
		public void destroy() {
			// Mock implementation
		}

		@Override
		public boolean handlesExclusively(Permission... pond) {
			return false;
		}

		@Override
		public boolean handles(Principal principal) {
			return false;
		}

		@Override
		public void clear(Principal p, StringBuilder report) {
			// Mock implementation
		}

	}

	@Override
	public void destroy() {
		// Mock implementation
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		TafResp tafResponseMock = new TafResp() {

			@Override
			public void timing(long arg0) {
				// Mock implementation
			}

			@Override
			public float timing() {
				return 0;
			}

			@Override
			public String taf() {
				return null;
			}

			@Override
			public boolean isValid() {
				return false;
			}

			@Override
			public boolean isFailedAttempt() {
				return false;
			}

			@Override
			public RESP isAuthenticated() {
				return null;
			}

			@Override
			public TaggedPrincipal getPrincipal() {
				return new TaggedPrincipal() {

					@Override
					public String getName() {
						return ((HttpServletRequest) servletRequest).getHeader("PermissionsUser");
					}

					@Override
					public String tag() {
						return null;
					}
				};
			}

			@Override
			public Access getAccess() {
				return null;
			}

			@Override
			public String desc() {
				return null;
			}

			@Override
			public String getTarget() {
				return null;
			}

			@Override
			public RESP authenticate() throws IOException {
				return null;
			}
		};

		CadiWrap cadiWrap = new CadiWrap((HttpServletRequest) servletRequest, tafResponseMock, fakeLur);
		filterChain.doFilter(cadiWrap, servletResponse);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// Mock implementation
	}

}
