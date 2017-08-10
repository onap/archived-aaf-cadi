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
package com.att.cadi.cm;

import java.io.PrintStream;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

import com.att.inno.env.Trans;

public class PlaceArtifactOnStream implements PlaceArtifact {
	private PrintStream out;

	public PlaceArtifactOnStream(PrintStream printStream) {
		out = printStream;
	}

	@Override
	public boolean place(Trans trans, CertInfo capi, Artifact a) {
		if(capi.getNotes()!=null && capi.getNotes().length()>0) {
			trans.info().printf("Warning:    %s\n",capi.getNotes());
		}
		out.printf("Challenge:  %s\n",capi.getChallenge());
		out.printf("PrivateKey:\n%s\n",capi.getPrivatekey());
		out.println("Certificate Chain:");
		for(String c : capi.getCerts()) {
			out.println(c);
		}
		return true;
	}
}
