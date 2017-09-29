/*******************************************************************************
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
package org.onap.aaf.cadi.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.onap.aaf.cadi.UserChain;

public class JU_UserChainManip {

	@Test
	public void build(){
		UserChain.Protocol baseAuth=UserChain.Protocol.BasicAuth;
		assertEquals(UserChainManip.build(new StringBuilder(""), "app", "id", baseAuth, true).toString(), "app:id:BasicAuth:AS");
	}
	
	@Test
	public void idToNS(){
		assertEquals(UserChainManip.idToNS(null), "");
	}
	
	@Test
	public void idToNS1(){
		assertEquals(UserChainManip.idToNS("t@st"), "st");
	}

}
