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

import java.net.ConnectException;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;

import org.onap.aaf.inno.env.APIException;

/**
 * 
 *
 * @param <RT>
 * @param <RET>
 */
public abstract class Retryable<RET> {
	// be able to hold state for consistent Connections.  Not required for all connection types.
	public Rcli<?> lastClient;
	private Locator.Item item;
	
	public Retryable() {
		lastClient = null;
		item = null;
	}

	public Retryable(Retryable<?> ret) {
		lastClient = ret.lastClient;
		item = ret.item;
	}

	public Locator.Item item(Locator.Item item) {
		lastClient = null;
		this.item = item;
		return item;
	}
	public Locator.Item item() {
		return item;
	}
	
	public abstract RET code(Rcli<?> client) throws CadiException, ConnectException, APIException;

	/**
	 * Note, Retryable is tightly coupled to the Client Utilizing.  It will not be the wrong type.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <CLIENT> Rcli<CLIENT> lastClient() {
		return (Rcli<CLIENT>)lastClient;
	}
}
