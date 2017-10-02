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
package org.onap.aaf.cadi.dme2;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.servlet.annotation.HttpMethodConstraint;

import org.junit.Before;
import org.junit.Test;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.api.DME2Exception;

public class JU_DME2ClientSSTest {
	

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws IOException, DME2Exception {
		DME2ClientSS client = new DME2ClientSS(null, "user", "pass");
		
		assertNotNull(client);

		assertEquals(client.getID(), "user");
		assertEquals(client.setLastResponse(0), 0);
	}

}
