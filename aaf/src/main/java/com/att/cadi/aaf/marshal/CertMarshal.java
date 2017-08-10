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
package com.att.cadi.aaf.marshal;

import javax.xml.datatype.XMLGregorianCalendar;

import aaf.v2_0.Certs.Cert;

import com.att.rosetta.marshal.FieldDateTime;
import com.att.rosetta.marshal.FieldHexBinary;
import com.att.rosetta.marshal.FieldString;
import com.att.rosetta.marshal.ObjMarshal;

public class CertMarshal extends ObjMarshal<Cert> {
	public CertMarshal() {
		add(new FieldHexBinary<Cert>("fingerprint") {
			@Override
			protected byte[] data(Cert t) {
				return t.getFingerprint();
			}
		});

		add(new FieldString<Cert>("id") {
			@Override
			protected String data(Cert t) {
				return t.getId();
			}
		});

		add(new FieldString<Cert>("x500") {
			@Override
			protected String data(Cert t) {
				return t.getX500();
			}
		});
		
		add(new FieldDateTime<Cert>("expires") {
			@Override
			protected XMLGregorianCalendar data(Cert t) {
				return t.getExpires();
			}
		});


	}
}
