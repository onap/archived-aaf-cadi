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
package org.onap.aaf.cadi.client;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Data;
import org.onap.aaf.rosetta.env.RosettaDF;


public interface EClient<CT> {
	public void setMethod(String meth);
	public void setPathInfo(String pathinfo);
	public void setPayload(Transfer transfer);
	public void addHeader(String tag, String value);
	public void setQueryParams(String q);
	public void setFragment(String f);
	public void send() throws APIException;
	public<T> Future<T> futureCreate(Class<T> t);
	public Future<String> futureReadString();
	public<T> Future<T> futureRead(RosettaDF<T> df,Data.TYPE type);
	public<T> Future<T> future(T t);
	public Future<Void> future(HttpServletResponse resp, int expected) throws APIException;
	
	public interface Transfer {
		public void transfer(OutputStream os) throws IOException, APIException;
	}
}
