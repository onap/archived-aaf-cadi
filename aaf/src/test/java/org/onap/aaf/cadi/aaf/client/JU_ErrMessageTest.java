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
package org.onap.aaf.cadi.aaf.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Data.TYPE;
import org.onap.aaf.rosetta.env.RosettaDF;
import org.onap.aaf.rosetta.env.RosettaEnv;

import aaf.v2_0.Error;

public class JU_ErrMessageTest {
	
	@Mock
	private RosettaEnv env;
	
	@Mock(answer=Answers.RETURNS_DEEP_STUBS)
	private RosettaDF<Object> errDF;

	private ErrMessage errMessage;

	private String attErrJson = "key:value";
	
	private Error error;

	private Future<?> future;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(env.newDataFactory(Error.class)).thenReturn(errDF);
		
		future = new Future<Error>() {

			@Override
			public boolean get(int timeout) throws CadiException {
				return false;
			}

			@Override
			public int code() {
				return 0;
			}

			@Override
			public String body() {
				return "Body";
			}

			@Override
			public String header(String tag) {
				return "header";
			}
		};
		
		error = new Error();
		error.setMessageId("Error Message Id");
		error.setText("Error Text");
		errMessage = new ErrMessage(env);
		
		
	}

	@Test
	public void testPrintErrMessage() throws APIException {
		when(errDF.newData().in(TYPE.JSON).load(attErrJson).asObject()).thenReturn(error);
		
		errMessage.printErr(System.out, attErrJson);
	}
	
	@Test
	public void testToMsgJsonErrAttribute() throws APIException {
		when(errDF.newData().in(TYPE.JSON).load(attErrJson).asObject()).thenReturn(error);
		
		StringBuilder sb = new StringBuilder();
		errMessage.toMsg(sb,attErrJson);
		
		assertEquals(sb.toString(),"Error Message Id Error Text");
	}
	
	@Test
	public void testToMsgFuture() {
		StringBuilder sb = errMessage.toMsg(future);
		
		assertEquals(sb.toString(), "0: Body");
	}

	
	@Test
	public void testToMsgFutureWithoutException() throws APIException {
		when(errDF.newData().in(TYPE.JSON).load(future.body()).asObject()).thenReturn(error);
		
		StringBuilder sb = errMessage.toMsg(future);
		
		assertEquals(sb.toString(), "Error Message Id Error Text");
	}
}
