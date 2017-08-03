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
package com.att.cadi.sso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.att.cadi.Access.Level;
import com.att.cadi.CadiException;
import com.att.cadi.PropAccess;
import com.att.cadi.Symm;
import com.att.cadi.config.Config;
import com.att.cadi.util.MyConsole;
import com.att.cadi.util.SubStandardConsole;
import com.att.cadi.util.TheConsole;


public class AAFSSO {
	public static final MyConsole  cons = TheConsole.implemented()?new TheConsole():new SubStandardConsole();
	
	private Properties diskprops = null; // use for temp storing User/Password on disk
	private File dot_aaf = null, sso=null; // instantiated, if ever, with diskprops
	
	boolean removeSSO=false;
	boolean loginOnly = false;
	private PropAccess access;
	private StringBuilder err;
	private String user,encrypted_pass;
	private boolean use_X509;

	private PrintStream os, stdout=null,stderr=null;

	private Method close;

	public AAFSSO(String[] args) throws IOException, CadiException {
		List<String> larg = new ArrayList<String>(args.length);

		// Cover for bash's need to escape *... (\\*)
		// also, remove SSO if required
		for (int i = 0; i < args.length; ++i) {
			if ("\\*".equals(args[i])) {
				args[i] = "*";
			}
			
			if("-logout".equalsIgnoreCase(args[i])) {
				removeSSO=true;
			} else if("-login".equalsIgnoreCase(args[i])) {
				loginOnly = true;
			} else {
				larg.add(args[i]);
			}
		}
		
		String[] nargs = new String[larg.size()];
		larg.toArray(nargs);

		dot_aaf = new File(System.getProperty("user.home")+"/.aaf");
		if(!dot_aaf.exists()) {
			dot_aaf.mkdirs();
		}
		File f = new File(dot_aaf,"sso.out");
		os = new PrintStream(new FileOutputStream(f,true));
		stdout = System.out;
		stderr = System.err;
		System.setOut(os);
		System.setErr(os);

		access = new PropAccess(os,nargs);
		Config.setDefaultRealm(access);

		user = access.getProperty(Config.AAF_MECHID);
		encrypted_pass = access.getProperty(Config.AAF_MECHPASS);
		
		File dot_aaf_kf = new File(dot_aaf,"keyfile");
		
		sso = new File(dot_aaf,"sso.props");
		if(removeSSO) {
			if(dot_aaf_kf.exists()) {
				dot_aaf_kf.setWritable(true,true);
				dot_aaf_kf.delete();
			}
			if(sso.exists()) {
				sso.delete();
			}
			System.out.println("AAF SSO information removed");
			System.exit(0);
		}
		
		if(!dot_aaf_kf.exists()) {
			FileOutputStream fos = new FileOutputStream(dot_aaf_kf);
			try {
				fos.write(Symm.encrypt.keygen());
				dot_aaf_kf.setExecutable(false,false);
				dot_aaf_kf.setWritable(false,false);
				dot_aaf_kf.setReadable(false,false);
				dot_aaf_kf.setReadable(true, true);
			} finally {
				fos.close();
			}
		}

		String keyfile = access.getProperty(Config.CADI_KEYFILE); // in case it's CertificateMan props
		if(keyfile==null) {
			access.setProperty(Config.CADI_KEYFILE, dot_aaf_kf.getAbsolutePath());
		}
		
		String alias = access.getProperty(Config.CADI_ALIAS);
		if(user==null && alias!=null && access.getProperty(Config.CADI_KEYSTORE_PASSWORD)!=null) {
			user = alias;
			access.setProperty(Config.AAF_MECHID, user);
			use_X509 = true;
		} else {
			use_X509 = false;
			Symm decryptor = Symm.obtain(dot_aaf_kf);
			if (user==null) {
				if(sso.exists() && sso.lastModified()>System.currentTimeMillis()-(8*60*60*1000 /* 8 hours */)) {
					String cm_url = access.getProperty(Config.CM_URL); // SSO might overwrite...
					FileInputStream fos = new FileInputStream(sso);
					try {
						access.load(fos);
						user = access.getProperty(Config.AAF_MECHID);
						encrypted_pass = access.getProperty(Config.AAF_MECHPASS);
						// decrypt with .aaf, and re-encrypt with regular Keyfile
						access.setProperty(Config.AAF_MECHPASS, 
								access.encrypt(decryptor.depass(encrypted_pass)));
						if(cm_url!=null) { //Command line CM_URL Overwrites ssofile.
							access.setProperty(Config.CM_URL, cm_url);
						}
					} finally {
						fos.close();
					}
				} else {
					diskprops = new Properties();
					String realm = Config.getDefaultRealm();
					// Turn on Console Sysout
					System.setOut(stdout);
					user=cons.readLine("aaf_id(%s@%s): ",System.getProperty("user.name"),realm);
					if(user==null) {
						user = System.getProperty("user.name")+'@'+realm;
					} else if(user.length()==0) { // 
						user = System.getProperty("user.name")+'@' + realm;
					} else if(user.indexOf('@')<0 && realm!=null) {
						user = user+'@'+realm;
					}
					access.setProperty(Config.AAF_MECHID,user);
					diskprops.setProperty(Config.AAF_MECHID,user);
					encrypted_pass = new String(cons.readPassword("aaf_password: "));
					System.setOut(os);
					encrypted_pass = Symm.ENC+decryptor.enpass(encrypted_pass);
					access.setProperty(Config.AAF_MECHPASS,encrypted_pass);
					diskprops.setProperty(Config.AAF_MECHPASS,encrypted_pass);
					diskprops.setProperty(Config.CADI_KEYFILE, access.getProperty(Config.CADI_KEYFILE));
				}
			}
		}
		if (user == null) {
			err = new StringBuilder("Add -D" + Config.AAF_MECHID + "=<id> ");
		}
	
		if (encrypted_pass == null && alias==null) {
			if (err == null) {
				err = new StringBuilder();
			} else {
				err.append("and ");
			}
			err.append("-D" + Config.AAF_MECHPASS + "=<passwd> ");
		}
	}
	
	public void setLogDefault() {
		access.setLogLevel(PropAccess.DEFAULT);
		if(stdout!=null) {
			System.setOut(stdout);
		}
	}

	public void setStdErrDefault() {
		access.setLogLevel(PropAccess.DEFAULT);
		if(stderr!=null) {
			System.setErr(stderr);
		}
	}

	public void setLogDefault(Level level) {
		access.setLogLevel(level);
		if(stdout!=null) {
			System.setOut(stdout);
		}
	}
	
	public boolean loginOnly() {
		return loginOnly;
	}

	public void addProp(String key, String value) {
		if(diskprops!=null) {
			diskprops.setProperty(key, value);
		}
	}
	
	public void writeFiles() throws IOException {
		// Store Creds, if they work 
		if(diskprops!=null) {
			if(!dot_aaf.exists()) {
				dot_aaf.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(sso);
			try {
				diskprops.store(fos, "AAF Single Signon");
			} finally {
				fos.close();
				sso.setWritable(false,false);
				sso.setExecutable(false,false);
				sso.setReadable(false,false);
				sso.setReadable(true,true);
			}
		}
		if(sso!=null) {
			sso.setReadable(false,false);
			sso.setWritable(false,false);
			sso.setExecutable(false,false);
			sso.setReadable(true,true);
			sso.setWritable(true,true);
		}
	}

	public PropAccess access() {
		return access;
	}

	public StringBuilder err() {
		return err;
	}
	
	public String user() {
		return user;
	}
	
	public String enc_pass() {
		return encrypted_pass;
	}
	
	public boolean useX509() {
		return use_X509;
	}
	
	public void close() {
		if(close!=null) {
			try {
				close.invoke(null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// nothing to do here.
			}
			close = null;
		}
	}
}
