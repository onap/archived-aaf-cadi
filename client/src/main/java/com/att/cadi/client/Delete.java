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
package com.att.cadi.client;

import com.att.cadi.CadiException;
import com.att.inno.env.APIException;
import com.att.rosetta.env.RosettaDF;

public class Delete<T> extends AAFClient.Call<T> {
	public Delete(AAFClient ac, RosettaDF<T> df) {
		super(ac,df);
	}

	@SuppressWarnings("unchecked")
	public Result<T> delete(final String pathInfo, final T t) throws Exception {
		if(t==null) {
			return (Result<T>)delete(pathInfo);
		}
		return client.hman.best(client.ss, 
			 new Retryable<Result<T>>() {
				@Override
				public Result<T> code(Rcli<?> client) throws APIException, CadiException {
					Future<T> ft = client.delete(pathInfo,df,t); 
					if(ft.get(client.readTimeout)) {
						return Result.ok(ft.code(),ft.value);
					} else {
						return Result.err(ft.code(),ft.body());
					}
				}
			});
	}

	public Result<Void> delete(final String pathInfo) throws Exception {
		return client.hman.best(client.ss, 
			 new Retryable<Result<Void>>() {
				@Override
				public Result<Void> code(Rcli<?> client) throws APIException, CadiException {
					Future<Void> ft = client.delete(pathInfo,VOID_CONTENT_TYPE); 
					if(ft.get(client.readTimeout)) {
						return Result.ok(ft.code(),ft.value);
					} else {
						return Result.err(ft.code(),ft.body());
					}
				}
			});
	}



}
