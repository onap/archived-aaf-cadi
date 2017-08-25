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
package org.onap.aaf.cadi.cm;

import java.io.File;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.util.Chmod;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

import org.onap.aaf.inno.env.Trans;

public class PlaceArtifactInFiles extends ArtifactDir {
	@Override
	public boolean _place(Trans trans, CertInfo certInfo, Artifact arti) throws CadiException {
		try {
			// Setup Public Cert
			File f = new File(dir,arti.getAppName()+".crt");
			write(f,Chmod.to644,certInfo.getCerts().get(0),C_R);
			
			// Setup Private Key
			f = new File(dir,arti.getAppName()+".key");
			write(f,Chmod.to400,certInfo.getPrivatekey(),C_R);
			
		} catch (Exception e) {
			throw new CadiException(e);
		}
		return true;
	}
}


