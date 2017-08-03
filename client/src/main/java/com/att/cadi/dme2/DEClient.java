/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
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
package com.att.cadi.dme2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.api.DME2Manager;
import com.att.aft.dme2.handler.DME2RestfulHandler;
import com.att.aft.dme2.handler.DME2RestfulHandler.ResponseInfo;
import com.att.cadi.CadiException;
import com.att.cadi.SecuritySetter;
import com.att.cadi.client.EClient;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.inno.env.APIException;
import com.att.inno.env.Data;
import com.att.rosetta.env.RosettaDF;

public class DEClient implements EClient<DME2Client> {
	private DME2Client client;
	private DME2RestfulHandler replyHandler;
	private EClient.Transfer payload;
	private boolean isProxy;
	private SecuritySetter<DME2Client> ss;
	
	public DEClient(DME2Manager manager, SecuritySetter<DME2Client> ss, URI uri, long timeout) throws DME2Exception, CadiException {
		client = new DME2Client(manager,uri,timeout);
		client.setAllowAllHttpReturnCodes(true);
		this.ss = ss;
		ss.setSecurity(client);
		replyHandler = new DME2RestfulHandler(Rcli.BLANK);
		client.setReplyHandler(replyHandler);
	}

	@Override
	public void setMethod(String meth) {
		client.setMethod(meth);
	}

	/**
	 * DME2 can't handle having QueryParams on the URL line, but it is the most natural way, so...
	 * 
	 * Also, DME2 can't handle "/proxy" as part of Context in the main URI line, so we add it when we see authz-gw to "isProxy"
	 */
	public void setPathInfo(String pathinfo) {
		int qp = pathinfo.indexOf('?');
		if(qp<0) {
			client.setContext(isProxy?("/proxy"+pathinfo):pathinfo);
		} else {
			client.setContext(isProxy?("/proxy"+pathinfo.substring(0,qp)):pathinfo.substring(0,qp));
			client.setQueryParams(pathinfo.substring(qp+1));
		}
	}

	@Override
	public void setPayload(EClient.Transfer transfer) {
		payload = transfer;
	}

	@Override
	public void addHeader(String tag, String value) {
		client.addHeader(tag, value);
	}


	@Override
	public void setQueryParams(String q) {
		client.setQueryParams(q);
	}

	@Override
	public void setFragment(String f) {
		// DME2 does not implement this
	}

	@Override
	public void send() throws APIException {
		try {
			if(payload!=null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				payload.transfer(baos);
				client.setPayload(new String(baos.toByteArray()));
			} else {
				client.setPayload("");
			}
			client.send();
		} catch (DME2Exception e) {
			throw new APIException(e);
		} catch (IOException e) {
			throw new APIException(e);
		}
	}

	
	public class DFuture<T> extends Future<T> {
		protected final DME2RestfulHandler reply;
		protected ResponseInfo info;
		
		public DFuture(DME2RestfulHandler reply) {
			this.reply = reply;
		}
		
		protected boolean evalInfo() throws APIException{
			//return info.getCode()==200;
			return true;
		};
		
		public final boolean get(int timeout) throws CadiException {
			try {
				info = reply.getResponse(timeout);
				ss.setLastResponse(info.getCode());
				return evalInfo();
			} catch (Exception e) {
				throw new CadiException(e);
			}
		}

		@Override
		public int code() {
			return info.getCode();
		}

		@Override
		public String body() {
			return info.getBody();
		}

		@Override
		public String header(String tag) {
			return info.header(tag);
		}

	}

	@Override
	public <T> Future<T> futureCreate(Class<T> t) {
		return new DFuture<T>(replyHandler) {
			public boolean evalInfo() throws APIException {
				
				return info.getCode()==201;
			}
		};
	}
	

	@Override
	public Future<String> futureReadString() {
		return new DFuture<String>(replyHandler) {
			public boolean evalInfo() throws APIException {
				if(info.getCode()==200) {
					value = info.getBody();
					return true;
				}
				return false;
			}
		};
	}
	
	@Override
	public<T> Future<T> futureRead(final RosettaDF<T> df, final Data.TYPE type) {
		return new DFuture<T>(replyHandler) {
			public boolean evalInfo() throws APIException {
				if(info.getCode()==200) {
					value = df.newData().in(type).load(info.getBody()).asObject();
					return true;
				}
				return false;
			}
		};
	}

	@Override
	public <T> Future<T> future(final T t) {
		return new DFuture<T>(replyHandler) {
			public boolean evalInfo() {
				if(info.getCode()==200) {
					value = t;
					return true;
				}
				return false;
			}
		};
	}

	@Override
	public Future<Void> future(HttpServletResponse resp,int expected) throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setProxy(boolean isProxy) {
		this.isProxy=isProxy;
	}

	
}
