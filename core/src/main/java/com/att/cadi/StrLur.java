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
package com.att.cadi;

import java.util.List;



/**
 * StrLUR: Implements fish with String, skipping the need to be a Principal where it doesn't make sense.
 *
 *
 */
public interface StrLur extends Lur {
	/** 
	 * Fish for Principals in a Pond
	 * 
	 *   or more boringly, is the User identified within a named collection representing permission.
	 * 
	 * @param principalName
	 * @return
	 */
	public boolean fish(String bait, Permission pond);

	/** 
	 * Fish all the Principals out a Pond
	 * 
	 *   For additional humor, pronounce the following with a Southern Drawl, "FishOil"
	 * 
	 *   or more boringly, load the List with Permissions found for Principal
	 * 
	 * @param principalName
	 * @return
	 */
	public void fishAll(String bait, List<Permission> permissions);
}
