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

import org.onap.aaf.cadi.CadiException;

import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.rosetta.env.RosettaDF;

public class Get<T> extends AAFClient.Call<T> {
	public Get(AAFClient ac, RosettaDF<T> df) {
		super(ac,df);
	}
	
	public Result<T> read(final String pathInfo) throws Exception {
		return client.hman.best(client.ss, 
			 new Retryable<Result<T>>() {
				@Override
				public Result<T> code(Rcli<?> client) throws APIException, CadiException {
					Future<T> ft = client.read(pathInfo,df); 
					if(ft.get(client.readTimeout)) {
						return Result.ok(ft.code(),ft.value);
					} else {
						return Result.err(ft.code(),ft.body());
					}
				}
			});
	}
}
