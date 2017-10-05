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
package org.onap.aaf.cadi.lur.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.CredVal.Type;
import org.onap.aaf.cadi.config.UsersDump;
import org.onap.aaf.cadi.lur.LocalLur;
import org.onap.aaf.cadi.lur.LocalPermission;

public class JU_LocalLur {

	@Test
	public void test() throws IOException {
		Symm symmetric = Symm.baseCrypt().obtain();
		LocalLur up;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(Symm.ENC.getBytes());
		symmetric.enpass("<pass>", baos);
		PropAccess ta = new PropAccess();
		Lur ml = up = new LocalLur(ta,"myname:groupA,groupB","admin:myname,yourname;suser:hisname,hername,m1234%"+baos.toString());

		
//		Permission admin = new LocalPermission("admin");
//		Permission suser = new LocalPermission("suser");
//		
//		// Check User fish
//		assertTrue(ml.fish(new JUPrincipal("myname"),admin));
//		assertTrue(ml.fish(new JUPrincipal("hisname"),admin));
//		assertFalse(ml.fish(new JUPrincipal("noname"),admin));
//		assertTrue(ml.fish(new JUPrincipal("itsname"),suser));
//		assertTrue(ml.fish(new JUPrincipal("hername"),suser));
//		assertFalse(ml.fish(new JUPrincipal("myname"),suser));
//		
//		// Check validate password
//		assertTrue(up.validate("m1234",Type.PASSWORD, "<pass>".getBytes()));
//		assertFalse(up.validate("m1234",Type.PASSWORD, "badPass".getBytes()));
//		
		// Check fishAll
		Set<String> set = new TreeSet<String>();
		List<Permission> perms = new ArrayList<Permission>();
		ml.fishAll(new JUPrincipal("myname"), perms);
		for(Permission p : perms) {
			set.add(p.getKey());
		}
//		assertEquals("[admin, groupA, groupB]",set.toString());
		UsersDump.write(System.out, up);
		System.out.flush();
		
	}
	
	// Simplistic Principal for testing purposes
	private static class JUPrincipal implements Principal {
		private String name;
		public JUPrincipal(String name) {
			this.name = name;
		}
//		@Override
		public String getName() {
			return name;
		}
	}



	
	
}
