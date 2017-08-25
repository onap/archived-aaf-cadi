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
package org.onap.aaf.cadi.filter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.Get;

public class CadiAccess implements Access {
	// constants for a couple of very commonly used strings.
	protected static final String FROM = "from";
	protected static final String FOR = "for";

	// Properties derived from <pass> sources (could be property files, Valve Configurations, Filter
	// configs, etc. 
	protected Properties props;

	// Will we write Logs?
	protected Level willWrite = Level.INFO;
	
	protected ServletContext context;
	protected Get getter = Get.NULL; // replace with Derived Class getter
	private Symm symm;

	public CadiAccess(Map<String, Object> map) {
		if(map!=null && !map.isEmpty()) {
			props = new Properties();
			for(Entry<String, Object> es : map.entrySet()) {
				Object v = es.getValue();
				if(v!=null) {
					props.put(es.getKey(), v.toString());
				}
			}
			Object keyfile = props.get(Config.CADI_KEYFILE);
			if(keyfile!=null) {
				try {
					FileInputStream fis = new FileInputStream(keyfile.toString());
					symm = Symm.obtain(fis);
				} catch (Exception e) {
				}
			}

		}
	}
	
	public Level willWrite() {
		return willWrite;
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.Access#willLog(com.att.cadi.Access.Level)
	 */
	@Override
	public boolean willLog(Level level) {
		return willWrite.compareTo(level)<=0;
	}

	/**
	 * Add the "Level" to the Buildline for Logging types that don't specify, or straight Streams, etc.  Then buildline
	 * 
	 * Build a line of code onto a StringBuilder based on Objects.  Analyze whether 
	 * spaces need including.
	 *
	 * @param level
	 * @param sb
	 * @param elements
	 * @return
	 */
	public final static StringBuilder buildLine(Level level, StringBuilder sb, Object[] elements) {
		sb.append(level.name());
		return buildLine(sb,elements);
	}
	
	/*
	 * Build a line of code onto a StringBuilder based on Objects.  Analyze whether 
	 * spaces need including.
	 * 
	 * @param sb
	 * @param elements
	 * @return
	 */
	public final static StringBuilder buildLine(StringBuilder sb, Object[] elements) {
		sb.append(' ');
		String str;
		boolean notFirst = false;
		for(Object o : elements) {
			if(o!=null) {
				str = o.toString();
				
				if(str.length()>0) {
					if(notFirst && shouldAddSpace(str,true) && shouldAddSpace(sb,false)) {
						sb.append(' ');
					} else {
						notFirst=true;
					}
					sb.append(str);
				}
			}
		}
		return sb;
	}
	
	private static boolean shouldAddSpace(CharSequence c,boolean start) {
		if(c.length()>0)
			switch(c.charAt(start?0:c.length()-1)) {
				case ' ':
				case '\t':
				case '\n':
				case '\'':
				case '"':
				case '|':
					return false;
			}
		return true;
	}

	/**
	 * Standard mechanism for logging, given being within a Servlet Context
	 * 
	 * Here, we treat
	 * 
	 * if context exists, log to it, otherwise log to Std Out (The latter is usually for startup 
	 * scenarios)
	 *
	 */
	public void log(Level level, Object... elements) {
		if(willWrite.compareTo(level)<=0) {
			StringBuilder sb = buildLine(level, new StringBuilder(),elements);
			if(context==null) {
				System.out.println(sb.toString());
			} else {
				context.log(sb.toString());
			}
		}
	}

	/**
	 * Standard mechanism for logging an Exception, given being within a Servlet Context, etc
	 * 
	 * if context exists, log to it, otherwise log to Std Out (The latter is usually for startup 
	 * scenarios)
	 *
	 */
	public void log(Exception e, Object... elements) {
		if(willWrite.compareTo(Level.ERROR)<=0) {
			StringBuilder sb = buildLine(Level.ERROR, new StringBuilder(),elements);
		
			if(context==null) {
				sb.append(e.toString());
				System.out.println(sb.toString());
			} else {
				context.log(sb.toString(),e);
			}
		}
	}
	
	public void setLogLevel(Level level) {
		willWrite = level;
	}
	
	/**
	 * Pass back the classloader of the Servlet Context, if it exists. Otherwise, get the classloader
	 * of this object.
	 */
	public ClassLoader classLoader() { // Use the Classloader that Context was created with
		return (context==null?this:context).getClass().getClassLoader();
	}

	/**
	 * Get the Property from Context
	 */
	public String getProperty(String string, String def) {
		String rv = null;

        if ( props != null )
            rv = props.getProperty( string, def );
        
		if(rv==null) {
            rv = context.getInitParameter(string);
		}
		return rv==null?def:rv;

	}

	public void load(InputStream is) throws IOException {
		if(this.props==null) {
			this.props = new Properties();
		}
		this.props.load(is);
		symm = Symm.obtain(this);
	}

	public String decrypt(String encrypted, boolean anytext) throws IOException {
		if(symm==null) {
			String keyfile = getter.get(Config.CADI_KEYFILE, null, true);
			if(keyfile!=null) {
				FileInputStream fis = new FileInputStream(keyfile);
				symm=Symm.obtain(fis);
				fis.close();
			}
		}
		return (symm!=null && encrypted!=null && (anytext || encrypted.startsWith(Symm.ENC)))
			? symm.depass(encrypted)
			: encrypted;
	}

	@Override
	public void printf(Level level, String fmt, Object[] elements) {
		// TODO Auto-generated method stub
		
	}

}
