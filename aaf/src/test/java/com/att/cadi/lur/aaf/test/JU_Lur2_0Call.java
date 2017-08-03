/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
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
package com.att.cadi.lur.aaf.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.junit.BeforeClass;
import org.junit.Test;

import com.att.cadi.CadiException;
import com.att.cadi.Lur;
import com.att.cadi.Permission;
import com.att.cadi.PropAccess;
import com.att.cadi.Symm;
import com.att.cadi.Taf.LifeForm;
import com.att.cadi.aaf.AAFPermission;
import com.att.cadi.aaf.v2_0.AAFConHttp;
import com.att.cadi.aaf.v2_0.AAFLurPerm;
import com.att.cadi.aaf.v2_0.AAFTaf;
import com.att.cadi.locator.DNSLocator;
import com.att.cadi.lur.ConfigPrincipal;
import com.att.cadi.lur.LocalPermission;
import com.att.cadi.taf.TafResp;

public class JU_Lur2_0Call {
	private static AAFConHttp aaf;
	private static PropAccess access;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		access = new PropAccess();
		aaf = new AAFConHttp(access,new DNSLocator(access,"https","localhost","8100"));
		aaf.basicAuth("testid", "whatever");
	}

	@Test 
	public void test() throws Exception {
	
		AAFLurPerm aafLur = aaf.newLur();

		Principal pri = new ConfigPrincipal("testid@aaf.att.com","whatever");
		for (int i = 0; i < 10; ++i) {
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|myInstance|write"),true);
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|kumquat|write"),false);
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|myInstance|read"),true);
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|kumquat|read"),true);
			
			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service","myInstance","write"),true);
			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service","kumquat","write"),false);
			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service","myInstance","read"),true);
			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service","kumquat","read"),true);

			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|!kum.*|read"),true);
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|myInstance|!wr*"),true);
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|myInstance"),true);

			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service","!kum.*","read"),true);
			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service","myInstance","!wr*"),true);

			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|!kum[Qq]uat|read"),true);
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|!my[iI]nstance|!wr*"),true);
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|!my[iI]nstance|!wr*"),true);
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|myInstance|!wr*"),true);

			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service","!kum[Qq]uat","read"),true);
			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service","!my[iI]nstance","!wr*"),true);
			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service","!my[iI]nstance","!wr*"),true);
			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service","myInstance","!wr*"),true);
			

			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|!my.nstance|!wr*"),true);
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|my.nstance|!wr*"),false);
			
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|my.nstance|!wr*"),false);
			
			//Maitrayee, aren't we going to have issues if we do RegExp with "."?
			//Is it too expensive to only do Reg Ex in presence of special characters, []{}*, etc? Not sure this helps for GRID.
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|kum.quat|read"),true);
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|!kum..uat|read"),true);
			
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|myInstance"),true); // ok if Stored Action is "*"
			
			// Key Evaluations
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|:myCluster:*:!my.*|write"),true); // ok if Stored Action is "*"
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|:myCluster:*|write"),false); // not ok if key lengths don't match "*"
			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|:myCluster:*:myCF|write"),true); // ok if Stored Action is "*"
			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service",":myCluster:*:!my.*","write"),true); // ok if Stored Action is "*"
			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service",":myCluster:*:myCF","write"),true); // ok if Stored Action is "*"
			print(aafLur, pri, new AAFPermission("com.test.JU_Lur2_0Call.service",":myCluster:*","write"),false); // not ok if key lengths don't match
			
		}

		print(aafLur, pri, new LocalPermission("bogus"),false);

//		try {
//			Thread.sleep(7000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		for (int i = 0; i < 10; ++i)
			print(aafLur, pri, new LocalPermission("supergroup"),false);

		System.out.println("All Done");
	}
	@Test
	public void testTaf() throws Exception {
		AAFTaf<?> aaft = new AAFTaf<HttpURLConnection>(aaf,true);
		
		TafResp resp;
		// No Header
		resp = aaft.validate(LifeForm.CBLF, new Req(), null);
		assertEquals(TafResp.RESP.TRY_AUTHENTICATING, resp.isAuthenticated());

		String auth = "Basic " + Symm.base64.encode("testid:whatever");
		resp = aaft.validate(LifeForm.CBLF, new Req("Authorization",auth), null);
		assertEquals(TafResp.RESP.IS_AUTHENTICATED, resp.isAuthenticated());
		
	}
//	@Test
//	public void testRole() throws CadiException {
//		TestAccess ta = new TestAccess();
//		AAFLurRole1_0 aafLur = new AAFLurRole1_0(
//				ta,
////				"http://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=1.0.0/envContext=UAT/routeOffer=BAU_SE",
//				"http://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=1.0.0/envContext=DEV/routeOffer=D1",
//				"m12345", "m12345pass", 50000, // dme Time
//				// 5*60000); // 5 minutes User Expiration
//				50000, // 5 seconds after Expiration
//				200); // High Count of items.. These do not take much memory
//
//		Principal pri = new ConfigPrincipal("xy1234","whatever);
//		for (int i = 0; i < 10; ++i) {
////			print(aafLur, pri, new LocalPermission("*|*|*|com.att.authz"));
//			print(aafLur, pri, new LocalPermission("service|myInstance|write"),false);
//			print(aafLur, pri, new LocalPermission("com.test.JU_Lur2_0Call.service|myInstance|write"),false);
//			print(aafLur, pri, new LocalPermission("com.att.cadi"),true);
//			print(aafLur, pri, new LocalPermission("global"),true);
//			print(aafLur, pri, new LocalPermission("kumquat"),false);
//		}
//
//		print(aafLur, pri, new LocalPermission("bogus"),false);
//
//		for (int i = 0; i < 10; ++i)
//			print(aafLur, pri, new LocalPermission("supergroup"),false);
//
//		System.out.println("All Done");
//	}


	private void print(Lur aafLur, Principal pri, Permission perm, boolean shouldBe)
			throws CadiException {
		long start = System.nanoTime();
	
		// The Call
		boolean ok = aafLur.fish(pri, perm);
	
		assertEquals(shouldBe,ok);
		float ms = (System.nanoTime() - start) / 1000000f;
		if (ok) {
			System.out.println("Yes, part of " + perm.getKey() + " (" + ms
					+ "ms)");
		} else {
			System.out.println("No, not part of " + perm.getKey() + " (" + ms
					+ "ms)");
		}
	}

	@SuppressWarnings("rawtypes")
	public class Req implements HttpServletRequest {
		private String[] headers;

		public Req(String ... headers) {
			this.headers = headers;
		}

		public Object getAttribute(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@SuppressWarnings("unchecked")
		public Enumeration getAttributeNames() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getCharacterEncoding() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setCharacterEncoding(String env)
				throws UnsupportedEncodingException {
			// TODO Auto-generated method stub
			
		}

		public int getContentLength() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getContentType() {
			// TODO Auto-generated method stub
			return null;
		}

		public ServletInputStream getInputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getParameter(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@SuppressWarnings("unchecked")
		public Enumeration getParameterNames() {
			// TODO Auto-generated method stub
			return null;
		}

		public String[] getParameterValues(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@SuppressWarnings("unchecked")
		public Map getParameterMap() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getProtocol() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getScheme() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getServerName() {
			// TODO Auto-generated method stub
			return null;
		}

		public int getServerPort() {
			// TODO Auto-generated method stub
			return 0;
		}

		public BufferedReader getReader() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRemoteAddr() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRemoteHost() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setAttribute(String name, Object o) {
			// TODO Auto-generated method stub
			
		}

		public void removeAttribute(String name) {
			// TODO Auto-generated method stub
			
		}

		public Locale getLocale() {
			// TODO Auto-generated method stub
			return null;
		}

		@SuppressWarnings("unchecked")
		public Enumeration getLocales() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isSecure() {
			// TODO Auto-generated method stub
			return false;
		}

		public RequestDispatcher getRequestDispatcher(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRealPath(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		public int getRemotePort() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getLocalName() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getLocalAddr() {
			// TODO Auto-generated method stub
			return null;
		}

		public int getLocalPort() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getAuthType() {
			// TODO Auto-generated method stub
			return null;
		}

		public Cookie[] getCookies() {
			// TODO Auto-generated method stub
			return null;
		}

		public long getDateHeader(String name) {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getHeader(String name) {
			for(int i=1;i<headers.length;i=i+2) {
				if(headers[i-1].equals(name)) return headers[i];
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		public Enumeration getHeaders(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@SuppressWarnings("unchecked")
		public Enumeration getHeaderNames() {
			// TODO Auto-generated method stub
			return null;
		}

		public int getIntHeader(String name) {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getMethod() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getPathInfo() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getPathTranslated() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getContextPath() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getQueryString() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRemoteUser() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isUserInRole(String role) {
			// TODO Auto-generated method stub
			return false;
		}

		public Principal getUserPrincipal() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRequestedSessionId() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRequestURI() {
			// TODO Auto-generated method stub
			return null;
		}

		public StringBuffer getRequestURL() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getServletPath() {
			// TODO Auto-generated method stub
			return null;
		}

		public HttpSession getSession(boolean create) {
			// TODO Auto-generated method stub
			return null;
		}

		public HttpSession getSession() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isRequestedSessionIdValid() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isRequestedSessionIdFromCookie() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isRequestedSessionIdFromURL() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isRequestedSessionIdFromUrl() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public ServletContext getServletContext() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AsyncContext startAsync() throws IllegalStateException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AsyncContext startAsync(ServletRequest servletRequest,
				ServletResponse servletResponse) throws IllegalStateException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isAsyncStarted() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isAsyncSupported() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public AsyncContext getAsyncContext() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DispatcherType getDispatcherType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean authenticate(HttpServletResponse response)
				throws IOException, ServletException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void login(String username, String password)
				throws ServletException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void logout() throws ServletException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Collection<Part> getParts() throws IOException, ServletException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Part getPart(String name) throws IOException, ServletException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
