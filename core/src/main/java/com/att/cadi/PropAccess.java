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
package com.att.cadi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import com.att.cadi.config.Config;
import com.att.cadi.config.SecurityInfo;

public class PropAccess implements Access {
	private static final SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	public static Level DEFAULT = Level.AUDIT;
	
	private Symm symm;
	private int level;
	private Properties props;
	private List<String> recursionProtection = null;
	private PrintStream out;
	
	private String name;

	public PropAccess() {
		out=System.out;
		init(null);
	}
	
	/**
	 * This Constructor soley exists to instantiate Servlet Context Based Logging that will call "init" later.
	 * @param sc
	 */
	protected PropAccess(Object o) {
		out=System.out;
		props = new Properties();
	}
	
	public PropAccess(String ... args) {
		this(System.out,args);
	}
	
	public PropAccess(PrintStream ps, String[] args) {
		out=ps==null?System.out:ps;
		Properties nprops=new Properties();
		int eq;
		for(String arg : args) {
			if((eq=arg.indexOf('='))>0) {
				nprops.setProperty(arg.substring(0, eq),arg.substring(eq+1));
			}
		}
		init(nprops);
	}

	public PropAccess(Properties p) {
		this(System.out,p);
	}
	
	public PropAccess(PrintStream ps, Properties p) {
		out=ps==null?System.out:ps;
		init(p);
	}
	
	protected void init(Properties p) {
		// Make sure these two are set before any changes in Logging
		name = "cadi";
		level=DEFAULT.maskOf();
		
		props = new Properties();
		// First, load related System Properties
		for(Entry<Object,Object> es : System.getProperties().entrySet()) {
			String key = es.getKey().toString();
			for(String start : new String[] {"cadi_","aaf_","cm_","csp_"}) {
				if(key.startsWith(start)) {
					props.put(key, es.getValue());
				}
			}			
		}
		// Second, overlay or fill in with Passed in Props
		if(p!=null) {
			props.putAll(p);
		}
		
		// Third, load any Chained Property Files
		load(props.getProperty(Config.CADI_PROP_FILES));
		
		String sLevel = props.getProperty(Config.CADI_LOGLEVEL); 
		if(sLevel!=null) {
			level=Level.valueOf(sLevel).maskOf(); 
		}
		// Setup local Symmetrical key encryption
		if(symm==null) {
			symm = Symm.obtain(this);
		}
		
		name = props.getProperty(Config.CADI_LOGNAME, name);
		
		// Critical - if no Security Protocols set, then set it.  We'll just get messed up if not
		if(props.get(Config.CADI_PROTOCOLS)==null) {
			props.setProperty(Config.CADI_PROTOCOLS, SecurityInfo.HTTPS_PROTOCOLS_DEFAULT);
		}
	}

	private void load(String cadi_prop_files) {
		String prevKeyFile = props.getProperty(Config.CADI_KEYFILE);

		if(cadi_prop_files!=null) {
			int prev = 0, end = cadi_prop_files.length();
			int idx;
			String filename;
			while(prev<end) {
				idx = cadi_prop_files.indexOf(File.pathSeparatorChar,prev);
				if(idx<0) {
					idx = end;
				}
				File file = new File(filename=cadi_prop_files.substring(prev,idx));
				if(file.exists()) {
					printf(Level.INIT,"Loading CADI Properties from %s",file.getAbsolutePath());
					try {
						FileInputStream fis = new FileInputStream(file);
						try {
							props.load(fis);
							// Recursively Load
							String chainProp = props.getProperty(Config.CADI_PROP_FILES);
							if(chainProp!=null) {
								if(recursionProtection==null) {
									recursionProtection = new ArrayList<String>();
									recursionProtection.add(cadi_prop_files);
								}
								if(!recursionProtection.contains(chainProp)) {
									recursionProtection.add(chainProp);
									load(chainProp); // recurse
								}
							}
						} finally {
							fis.close();
						}
					} catch (Exception e) {
						log(e,filename,"cannot be opened");
					}
				} else {
					printf(Level.WARN,"Warning: recursive CADI Property %s does not exist",file.getAbsolutePath());
				}
				prev = idx+1;
			}
		}
		// Reset Symm if Keyfile Changes:
		String newKeyFile = props.getProperty(Config.CADI_KEYFILE);
		if((prevKeyFile==null && newKeyFile!=null) || (newKeyFile!=null && !newKeyFile.equals(prevKeyFile))) {
			symm = Symm.obtain(this);
			prevKeyFile=newKeyFile;
		}
		
		String loglevel = props.getProperty(Config.CADI_LOGLEVEL);
		if(loglevel!=null) {
			try {
				level=Level.valueOf(loglevel).maskOf();
			} catch (IllegalArgumentException e) {
				printf(Level.ERROR,"%s=%s is an Invalid Log Level",Config.CADI_LOGLEVEL,loglevel);
			}
		}
	}
	
	@Override
	public void load(InputStream is) throws IOException {
		props.load(is);
		load(props.getProperty(Config.CADI_PROP_FILES));
	}

	@Override
	public void log(Level level, Object ... elements) {
		if(willLog(level)) {
			StringBuilder sb = buildMsg(level, elements);
			out.println(sb);
			out.flush();
		}
	}
	
	protected StringBuilder buildMsg(Level level, Object[] elements) {
		StringBuilder sb = new StringBuilder(iso8601.format(new Date()));
		sb.append(' ');
		sb.append(level.name());
		sb.append(" [");
		sb.append(name);
		
		int end = elements.length;
		if(end<=0) {
			sb.append("] ");
		} else {
			int idx = 0;
			if(elements[idx] instanceof Integer) {
				sb.append('-');
				sb.append(elements[idx]);
				++idx;
			}
			sb.append("] ");
			String s;
			boolean first = true;
			for(Object o : elements) {
				if(o!=null) {
					s=o.toString();
					if(first) {
						first = false;
					} else {
						int l = s.length();
						if(l>0)	{
							switch(s.charAt(l-1)) {
								case ' ':
									break;
								default:
									sb.append(' ');
							}
						}
					}
					sb.append(s);
				}
			}
		}
		return sb;
	}

	@Override
	public void log(Exception e, Object... elements) {
		log(Level.ERROR,e.getMessage(),elements);
		e.printStackTrace(System.err);
	}

	@Override
	public void printf(Level level, String fmt, Object... elements) {
		if(willLog(level)) {
			log(level,String.format(fmt, elements));
		}
	}

	@Override
	public void setLogLevel(Level level) {
		this.level = level.maskOf();
	}

	@Override
	public boolean willLog(Level level) {
		return level.inMask(this.level);
	}

	@Override
	public ClassLoader classLoader() {
		return ClassLoader.getSystemClassLoader();
	}

	@Override
	public String getProperty(String tag, String def) {
		return props.getProperty(tag,def);
	}

	@Override
	public String decrypt(String encrypted, boolean anytext) throws IOException {
		return (encrypted!=null && (anytext==true || encrypted.startsWith(Symm.ENC)))
			? symm.depass(encrypted)
			: encrypted;
	}
	
	public String encrypt(String unencrypted) throws IOException {
		return Symm.ENC+symm.enpass(unencrypted);
	}

	//////////////////
	// Additional
	//////////////////
	public String getProperty(String tag) {
		return props.getProperty(tag);
	}
	

	public Properties getProperties() {
		return props;
	}

	public void setProperty(String tag, String value) {
		if(value!=null) {
			props.put(tag, value);
			if(Config.CADI_KEYFILE.equals(tag)) {
				// reset decryption too
				symm = Symm.obtain(this);
			}
		}
	}

	public Properties getDME2Properties() {
		return Config.getDME2Props(this);
	}

}
