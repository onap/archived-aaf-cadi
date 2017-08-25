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
package org.onap.aaf.cadi.wsse.test;

import java.io.FileInputStream;

import javax.xml.stream.events.XMLEvent;

import org.junit.Test;
import org.onap.aaf.cadi.wsse.XEvent;
import org.onap.aaf.cadi.wsse.XReader;

public class JU_XReader {
	@Test
	public void test() throws Exception {
		FileInputStream fis = new FileInputStream("test/CBUSevent.xml");
		try {
			XReader xr = new XReader(fis);
			while(xr.hasNext()) {
				XEvent xe = xr.nextEvent();
				switch(xe.getEventType()) {
					case XMLEvent.START_DOCUMENT:
						System.out.println("Start Document");
						break;
					case XMLEvent.START_ELEMENT:
						System.out.println("Start Event: " + xe.asStartElement().getName());
						break;
					case XMLEvent.END_ELEMENT:
						System.out.println("End Event: " + xe.asEndElement().getName());
						break;
					case XMLEvent.CHARACTERS:
						System.out.println("Characters: " + xe.asCharacters().getData());
						break;
					case XMLEvent.COMMENT:
						System.out.println("Comment: " + ((XEvent.Comment)xe).value);
						break;
				}
			}
		} finally {
			fis.close();
		}
		
	}

}
