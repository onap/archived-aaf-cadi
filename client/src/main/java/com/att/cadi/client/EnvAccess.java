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
package com.att.cadi.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import com.att.cadi.Access;
import com.att.cadi.Symm;
import com.att.inno.env.Decryptor;
import com.att.inno.env.Env;
import com.att.inno.env.impl.BasicEnv;

public class EnvAccess implements Access {
	private Env env;

	/**
	 * String Property tag for files/resources that may contain properties.  Can be null.
	 * Resources of ClassLoader will be checked first, if exist. Can be null.
	 * @param env
	 * @param tag
	 * @param cl
	 * @throws IOException
	 */
	public EnvAccess(BasicEnv env, ClassLoader cl) throws IOException {
		this.env = env;
		final Symm s = Symm.obtain(this);
		env.set(new Decryptor() {
				private Symm symm = s;
				@Override
				public String decrypt(String encrypted) {
					try {
						return (encrypted!=null && (encrypted.startsWith(Symm.ENC)))
								? symm.depass(encrypted)
								: encrypted;
					} catch (IOException e) {
						return "";
					}
				}
			}
		);
	}

	
	/**
	 * Construct with the Classloader of Env and CADI_PROP_FILES, if possible
	 * 
	 * @param env
	 * @throws IOException
	 */
	public EnvAccess(BasicEnv env) throws IOException {
		this(env, env.getClass().getClassLoader());
	}
	
	@Override
	public void log(Level level, Object... elements) {
		switch(level) {
			case AUDIT:
				env.audit().log(elements);
				break;
			case DEBUG:
				env.debug().log(elements);
				break;
			case ERROR:
				env.error().log(elements);
				break;
			case INFO:
				env.info().log(elements);
				break;
			case INIT:
				env.init().log(elements);
				break;
			case WARN:
				env.warn().log(elements);
				break;
			default:
				break;
		}
		
	}

	@Override
	public void log(Exception e, Object... elements) {
		env.error().log(e,elements);
	}

	@Override
	public void printf(Level level, String fmt, Object... elements) {
		if(willLog(level)) {
			log(level,String.format(fmt, elements));
		}
	}


	@Override
	public boolean willLog(Level level) {
		switch(level) {
			case AUDIT:
				return env.audit().isLoggable();
			case DEBUG:
				return env.debug().isLoggable();
			case ERROR:
				return env.error().isLoggable();
			case INFO:
				return env.info().isLoggable();
			case INIT:
				return env.init().isLoggable();
			case WARN:
				return env.warn().isLoggable();
			default:
				return false;
		}
	}


	@Override
	public void setLogLevel(Level level) {
		// unused
	}

	@Override
	public ClassLoader classLoader() {
		return env.getClass().getClassLoader();
	}

	@Override
	public String getProperty(String string, String def) {
		return env.getProperty(string, def);
	}
	
	@Override
	public void load(InputStream is) throws IOException {
		Properties props = new Properties();
		props.load(is);
		for(Entry<Object, Object> es :props.entrySet()) {
			env.setProperty(es.getKey().toString(), es.getValue().toString());
		}
	}

	@Override
	public String decrypt(String encrypted, boolean anytext) throws IOException {
		return env.decryptor().decrypt(encrypted);
	}

}
