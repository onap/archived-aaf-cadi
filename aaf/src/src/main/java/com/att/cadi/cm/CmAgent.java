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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.att.cadi.Access;
import com.att.cadi.Symm;
import com.att.cadi.aaf.client.ErrMessage;
import com.att.cadi.aaf.v2_0.AAFCon;
import com.att.cadi.aaf.v2_0.AAFConHttp;
import com.att.cadi.client.EnvAccess;
import com.att.cadi.client.Future;
import com.att.cadi.config.Config;
import com.att.cadi.http.HBasicAuthSS;
import com.att.inno.env.Data.TYPE;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.Trans;
import com.att.inno.env.util.Chrono;
import com.att.inno.env.util.Split;
import com.att.rosetta.env.RosettaDF;
import com.att.rosetta.env.RosettaEnv;

import certman.v1_0.Artifacts;
import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;
import certman.v1_0.CertificateRequest;

public class CmAgent {
	private static final String PRINT = "print";
	private static final String FILE = "file";
	private static final String PKCS12 = "pkcs12";
	private static final String JKS = "jks";
	private static final String SCRIPT="script";
	
	private static final String CM_VER = "1.0";
	public static final int PASS_SIZE = 24;
	private static int TIMEOUT;
	
	private static MyConsole cons;
	
	private static RosettaDF<CertificateRequest> reqDF;
	private static RosettaDF<CertInfo> certDF;
	private static RosettaDF<Artifacts> artifactsDF;
	private static ErrMessage errMsg;
	private static Map<String,PlaceArtifact> placeArtifact;
	private static RosettaEnv env;

	public static void main(String[] args) {
		int exitCode = 0;
		env = new RosettaEnv(Config.CADI_PROP_FILES,args);
		Deque<String> cmds = new ArrayDeque<String>();
		for(String p : args) {
			if(p.indexOf('=')<0) {
				cmds.add(p);
			}
		}
		
		if(cmds.size()==0) {
			System.out.println("Usage: java -jar <cadi-aaf-*-full.jar> cmd [<tag=value>]*");
			System.out.println("   create   <mechID> [<machine>]");
			System.out.println("   read     <mechID> [<machine>]");
			System.out.println("   update   <mechID> [<machine>]");
			System.out.println("   delete   <mechID> [<machine>]");
			System.out.println("   copy     <mechID> <machine> <newmachine>[,<newmachine>]*");
			System.out.println("   place    <mechID> [<machine>]");
			System.out.println("   showpass <mechID> [<machine>]");
			System.out.println("   check    <mechID> [<machine>]");
			System.exit(1);
		}
		
		TIMEOUT = Integer.parseInt(env.getProperty(Config.AAF_CONN_TIMEOUT, "5000"));
		cons = TheConsole.implemented()?new TheConsole():new SubStandardConsole();

		try {
			reqDF = env.newDataFactory(CertificateRequest.class);
			artifactsDF = env.newDataFactory(Artifacts.class);
			certDF = env.newDataFactory(CertInfo.class);
			errMsg = new ErrMessage(env);

			placeArtifact = new HashMap<String,PlaceArtifact>();
			placeArtifact.put(JKS, new PlaceArtifactInKeystore(JKS));
			placeArtifact.put(PKCS12, new PlaceArtifactInKeystore(PKCS12));
			placeArtifact.put(FILE, new PlaceArtifactInFiles());
			placeArtifact.put(PRINT, new PlaceArtifactOnStream(System.out));
			placeArtifact.put(SCRIPT, new PlaceArtifactScripts());
			
			Access access = new EnvAccess(env);
			Trans trans = env.newTrans();
			try {
				getProperty(env,false, Config.CM_URL,Config.CM_URL+": ");
				String str=env.getProperty(Config.CADI_ALIAS);
				if(str==null) { // ask for MechID pass
					getProperty(env,false,Config.AAF_MECHID,"Your Identity: ");
					getProperty(env,true,Config.AAF_MECHPASS,"Password: ");
				}
				AAFCon<?> aafcon = new AAFConHttp(access,Config.CM_URL);
						
				String cmd = cmds.removeFirst();
				if("place".equals(cmd)) {
					placeCerts(trans,aafcon,cmds);
				} else if("create".equals(cmd)) {
					createArtifact(trans, aafcon,cmds);
				} else if("read".equals(cmd)) {
					readArtifact(trans, aafcon, cmds);
				} else if("copy".equals(cmd)) {
					copyArtifact(trans, aafcon, cmds);
				} else if("update".equals(cmd)) {
					updateArtifact(trans, aafcon, cmds);
				} else if("delete".equals(cmd)) {
					deleteArtifact(trans, aafcon, cmds);
				} else if("showpass".equals(cmd)) {
					showPass(trans,aafcon,cmds);
				} else if("check".equals(cmd)) {
					try {
						exitCode = check(trans,aafcon,cmds);
					} catch (Exception e) {
						exitCode = 1;
						throw e;
					}
				} else {
					cons.printf("Unknown command \"%s\"\n", cmd);
				}
			} finally {
				StringBuilder sb = new StringBuilder();
                trans.auditTrail(4, sb, Trans.REMOTE);
                if(sb.length()>0) {
                	trans.info().log("Trans Info\n",sb);
                }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(exitCode!=0) {
			System.exit(exitCode);
		}
	}

	private static String getProperty(Env env, boolean secure, String tag, String prompt, Object ... def) {
		String value;
		if((value=env.getProperty(tag))==null) {
			if(secure) {
				value = new String(cons.readPassword(prompt, def));
			} else {
				value = cons.readLine(prompt,def).trim();
			}
			if(value!=null) {
				if(value.length()>0) {
					env.setProperty(tag,value);
				} else if(def.length==1) {
					value=def[0].toString();
					env.setProperty(tag,value);
				}
			}
		}
		return value;
	}

	private interface MyConsole {
		public String readLine(String fmt, Object ... args);
		public char[] readPassword(String fmt, Object ... args);
		public void printf(String fmt, Object ...args);
	}

	private static class TheConsole implements MyConsole {
		@Override
		public String readLine(String fmt, Object... args) {
			String rv = System.console().readLine(fmt, args);
			if(args.length>0 && args[0]!=null && rv.length()==0) {
				rv = args[0].toString();
			}
			return rv;
		}

		@Override
		public char[] readPassword(String fmt, Object... args) {
			return System.console().readPassword(fmt, args);
		}
		
		public static boolean implemented() {
			return System.console()!=null;
		}

		@Override
		public void printf(String fmt, Object... args) {
			System.console().printf(fmt, args);
		}
	}
	
	// Substandard, because System.in doesn't do Passwords..
	private static class SubStandardConsole implements MyConsole {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		@Override
		public String readLine(String fmt, Object... args) {
			String rv;
			try {
				System.out.printf(fmt,args);
				rv = br.readLine();
				if(args.length==1 && rv.length()==0) {
					rv = args[0].toString();
				}
			} catch (IOException e) {
				System.err.println("uh oh...");
				rv = "";
			}
			return rv;
		}

		@Override
		public char[] readPassword(String fmt, Object... args) {
			try {
				System.out.printf(fmt,args);
				return br.readLine().toCharArray();
			} catch (IOException e) {
				System.err.println("uh oh...");
				return new char[0];
			}
		}

		@Override
		public void printf(String fmt, Object... args) {
			System.out.printf(fmt, args);
		}
	}

//	private static class AutoData implements MyConsole {
////		private Env env;
//		private Map<String,String> data;
//
//		@Override
//		public String readLine(String fmt, Object... args) {
//			String rv=data.get(fmt);
//			return rv==null?"":rv;
//		}
//
//		@Override
//		public char[] readPassword(String fmt, Object... args) {
//			String rv=data.get(fmt);
//			return rv==null?new char[0]:rv.toCharArray();
//		}
//		
//		@Override
//		public void printf(String fmt, Object... args) {
//			System.out.printf(fmt, args);
//		}
//
//	}
//	
	private static String mechID(Deque<String> cmds) {
		if(cmds.size()<1) {
			String alias = env.getProperty(Config.CADI_ALIAS);
			return alias!=null?alias:cons.readLine("MechID: ");
		}
		return cmds.removeFirst();	
	}

	private static String machine(Deque<String> cmds) throws UnknownHostException {
		if(cmds.size()>0) {
			return cmds.removeFirst();
		} else {
			String mach = env.getProperty(Config.HOSTNAME);
			return mach!=null?mach:InetAddress.getLocalHost().getHostName();
		}
	}

	private static String[] machines(Deque<String> cmds)  {
		String machines;
		if(cmds.size()>0) {
			machines = cmds.removeFirst();
		} else {
			machines = cons.readLine("Machines (sep by ','): ");
		}
		return Split.split(',', machines);
	}

	private static void createArtifact(Trans trans, AAFCon<?> aafcon, Deque<String> cmds) throws Exception {
		String mechID = mechID(cmds);
		String machine = machine(cmds);

		Artifacts artifacts = new Artifacts();
		Artifact arti = new Artifact();
		artifacts.getArtifact().add(arti);
		arti.setMechid(mechID!=null?mechID:cons.readLine("MechID: "));
		arti.setMachine(machine!=null?machine:cons.readLine("Machine (%s): ",InetAddress.getLocalHost().getHostName()));
		arti.setCa(cons.readLine("CA: (%s): ","aaf"));
		
		String resp = cons.readLine("Types [file,jks,pkcs12] (%s): ", "jks");
		for(String s : Split.splitTrim(',', resp)) {
			arti.getType().add(s);
		}
		// Always do Script
		if(!resp.contains(SCRIPT)) {
			arti.getType().add(SCRIPT);
		}

		// Note: Sponsor is set on Creation by CM
		String configRootName = AAFCon.reverseDomain(arti.getMechid());
		arti.setAppName(cons.readLine("AppName (%s): ",configRootName));
		arti.setDir(cons.readLine("Directory (%s): ", System.getProperty("user.dir")));
		arti.setOsUser(cons.readLine("OS User (%s): ", System.getProperty("user.name")));
		arti.setRenewDays(Integer.parseInt(cons.readLine("Renewal Days (%s):", "30")));
		arti.setNotification(toNotification(cons.readLine("Notification (mailto owner):", "")));
		
		TimeTaken tt = trans.start("Create Artifact", Env.REMOTE);
		try {
			Future<Artifacts> future = aafcon.client(CM_VER).create("/cert/artifacts", artifactsDF, artifacts);
			if(future.get(TIMEOUT)) {
				trans.info().printf("Call to AAF Certman successful %s, %s",arti.getMechid(), arti.getMachine());
			} else {
				trans.error().printf("Call to AAF Certman failed, %s",
					errMsg.toMsg(future));
			}
		} finally {
			tt.done();
		}
	}

	private static String toNotification(String notification) {
		if(notification==null) {
			notification="";
		} else if(notification.length()>0) {
			if(notification.indexOf(':')<0) {
				notification = "mailto:" + notification;
			}
		}
		return notification;
	}
	

	private static void readArtifact(Trans trans, AAFCon<?> aafcon, Deque<String> cmds) throws Exception {
		String mechID = mechID(cmds);
		String machine = machine(cmds);

		TimeTaken tt = trans.start("Read Artifact", Env.SUB);
		try {
			Future<Artifacts> future = aafcon.client(CM_VER)
					.read("/cert/artifacts/"+mechID+'/'+machine, artifactsDF);
	
			if(future.get(TIMEOUT)) {
				boolean printed = false;
				for(Artifact a : future.value.getArtifact()) {
					cons.printf("MechID:          %s\n",a.getMechid()); 
					cons.printf("  Sponsor:       %s\n",a.getSponsor()); 
					cons.printf("Machine:         %s\n",a.getMachine()); 
					cons.printf("CA:              %s\n",a.getCa()); 
					StringBuilder sb = new StringBuilder();
					boolean first = true;
					for(String t : a.getType()) {
						if(first) {first=false;}
						else{sb.append(',');}
						sb.append(t);
					}
					cons.printf("Types:           %s\n",sb);
					cons.printf("AppName:         %s\n",a.getAppName()); 
					cons.printf("Directory:       %s\n",a.getDir());
					cons.printf("O/S User:        %s\n",a.getOsUser());
					cons.printf("Renew Days:      %d\n",a.getRenewDays());
					cons.printf("Notification     %s\n",a.getNotification());
					printed = true;
				}
				if(!printed) {
					cons.printf("Artifact for %s %s does not exist", mechID, machine);
				}
			} else {
				trans.error().log(errMsg.toMsg(future));
			}
		} finally {
			tt.done();
		}
	}
	
	private static void copyArtifact(Trans trans, AAFCon<?> aafcon, Deque<String> cmds) throws Exception {
		String mechID = mechID(cmds);
		String machine = machine(cmds);
		String[] newmachs = machines(cmds);
		if(newmachs==null || newmachs == null) {
			trans.error().log("No machines listed to copy to");
		} else {
			TimeTaken tt = trans.start("Copy Artifact", Env.REMOTE);
			try {
				Future<Artifacts> future = aafcon.client(CM_VER)
						.read("/cert/artifacts/"+mechID+'/'+machine, artifactsDF);
			
				if(future.get(TIMEOUT)) {
					boolean printed = false;
					for(Artifact a : future.value.getArtifact()) {
						for(String m : newmachs) {
							a.setMachine(m);
							Future<Artifacts> fup = aafcon.client(CM_VER).update("/cert/artifacts", artifactsDF, future.value);
							if(fup.get(TIMEOUT)) {
								trans.info().printf("Copy of %s %s successful to %s",mechID,machine,m);
							} else {
								trans.error().printf("Call to AAF Certman failed, %s",
									errMsg.toMsg(fup));
							}
	
							printed = true;
						}
					}
					if(!printed) {
						cons.printf("Artifact for %s %s does not exist", mechID, machine);
					}
				} else {
					trans.error().log(errMsg.toMsg(future));
				}
			} finally {
				tt.done();
			}
		}
	}

	private static void updateArtifact(Trans trans, AAFCon<?> aafcon, Deque<String> cmds) throws Exception {
		String mechID = mechID(cmds);
		String machine = machine(cmds);

		TimeTaken tt = trans.start("Update Artifact", Env.REMOTE);
		try {
			Future<Artifacts> fread = aafcon.client(CM_VER)
					.read("/cert/artifacts/"+mechID+'/'+machine, artifactsDF);
	
			if(fread.get(TIMEOUT)) {
				Artifacts artifacts = new Artifacts();
				for(Artifact a : fread.value.getArtifact()) {
					Artifact arti = new Artifact();
					artifacts.getArtifact().add(arti);
					
					cons.printf("For %s on %s\n", a.getMechid(),a.getMachine());
					arti.setMechid(a.getMechid());
					arti.setMachine(a.getMachine());
					arti.setCa(cons.readLine("CA: (%s): ",a.getCa()));
					StringBuilder sb = new StringBuilder();
					boolean first = true;
					for(String t : a.getType()) {
						if(first) {first=false;}
						else{sb.append(',');}
						sb.append(t);
					}
	
					String resp = cons.readLine("Types [file,jks,pkcs12] (%s): ", sb);
					for(String s : Split.splitTrim(',', resp)) {
						arti.getType().add(s);
					}
					// Always do Script
					if(!resp.contains(SCRIPT)) {
						arti.getType().add(SCRIPT);
					}

					// Note: Sponsor is set on Creation by CM
					arti.setAppName(cons.readLine("AppName (%s): ",a.getAppName()));
					arti.setDir(cons.readLine("Directory (%s): ", a.getDir()));
					arti.setOsUser(cons.readLine("OS User (%s): ", a.getOsUser()));
					arti.setRenewDays(Integer.parseInt(cons.readLine("Renew Days (%s):", a.getRenewDays())));
					arti.setNotification(toNotification(cons.readLine("Notification (%s):", a.getNotification())));
	
				}
				if(artifacts.getArtifact().size()==0) {
					cons.printf("Artifact for %s %s does not exist", mechID, machine);
				} else {
					Future<Artifacts> fup = aafcon.client(CM_VER).update("/cert/artifacts", artifactsDF, artifacts);
					if(fup.get(TIMEOUT)) {
						trans.info().printf("Call to AAF Certman successful %s, %s",mechID,machine);
					} else {
						trans.error().printf("Call to AAF Certman failed, %s",
							errMsg.toMsg(fup));
					}
				}
			} else {
				trans.error().printf("Call to AAF Certman failed, %s %s, %s",
						errMsg.toMsg(fread),mechID,machine);
			}
		} finally {
			tt.done();
		}
	}
	
	private static void deleteArtifact(Trans trans, AAFCon<?> aafcon, Deque<String> cmds) throws Exception {
		String mechid = mechID(cmds);
		String machine = mechID(cmds);
		
		TimeTaken tt = trans.start("Delete Artifact", Env.REMOTE);
		try {
			Future<Void> future = aafcon.client(CM_VER)
					.delete("/cert/artifacts/"+mechid+"/"+machine,"application/json" );
	
			if(future.get(TIMEOUT)) {
				trans.info().printf("Call to AAF Certman successful %s, %s",mechid,machine);
			} else {
				trans.error().printf("Call to AAF Certman failed, %s %s, %s",
					errMsg.toMsg(future),mechid,machine);
			}
		} finally {
			tt.done();
		}
	}

	

	private static boolean placeCerts(Trans trans, AAFCon<?> aafcon, Deque<String> cmds) throws Exception {
		boolean rv = false;
		String mechID = mechID(cmds);
		String machine = machine(cmds);
		
		TimeTaken tt = trans.start("Place Artifact", Env.REMOTE);
		try {
			Future<Artifacts> acf = aafcon.client(CM_VER)
					.read("/cert/artifacts/"+mechID+'/'+machine, artifactsDF);
			if(acf.get(TIMEOUT)) {
				// Have to wait for JDK 1.7 source...
				//switch(artifact.getType()) {
				if(acf.value.getArtifact()==null || acf.value.getArtifact().isEmpty()) {
					cons.printf("There are no artifacts for %s %s", mechID, machine);
				} else {
					for(Artifact a : acf.value.getArtifact()) {
						CertificateRequest cr = new CertificateRequest();
						cr.setMechid(a.getMechid());
						cr.setSponsor(a.getSponsor());
						cr.getFqdns().add(a.getMachine());
						Future<String> f = aafcon.client(CM_VER)
								.setQueryParams("withTrust")
								.updateRespondString("/cert/" + a.getCa(),reqDF, cr);
						if(f.get(TIMEOUT)) {
							CertInfo capi = certDF.newData().in(TYPE.JSON).load(f.body()).asObject();
							for(String type : a.getType()) {
								PlaceArtifact pa = placeArtifact.get(type);
								if(pa!=null) {
									if(rv = pa.place(trans, capi, a)) {
										notifyPlaced(a,rv);
									}
								}
							}
							// Cover for the above multiple pass possibilities with some static Data, then clear per Artifact
							ArtifactDir.clear();
						} else {
							trans.error().log(errMsg.toMsg(f));
						}
					}
				}
			} else {
				trans.error().log(errMsg.toMsg(acf));
			}
		} finally {
			tt.done();
		}
		return rv;
	}
	
	private static void notifyPlaced(Artifact a, boolean rv) {
		
		
	}

	private static void showPass(Trans trans, AAFCon<?> aafcon, Deque<String> cmds) throws Exception {
		String mechID = mechID(cmds);
		String machine = machine(cmds);

		TimeTaken tt = trans.start("Show Password", Env.REMOTE);
		try {
			Future<Artifacts> acf = aafcon.client(CM_VER)
					.read("/cert/artifacts/"+mechID+'/'+machine, artifactsDF);
			if(acf.get(TIMEOUT)) {
				// Have to wait for JDK 1.7 source...
				//switch(artifact.getType()) {
				if(acf.value.getArtifact()==null || acf.value.getArtifact().isEmpty()) {
					cons.printf("No Artifacts found for %s on %s", mechID, machine);
				} else {
					String id = aafcon.defID();
					boolean allowed;
					for(Artifact a : acf.value.getArtifact()) {
						allowed = id!=null && (id.equals(a.getSponsor()) ||
								(id.equals(a.getMechid()) 
										&& aafcon.securityInfo().defSS.getClass().isAssignableFrom(HBasicAuthSS.class)));
						if(!allowed) {
							Future<String> pf = aafcon.client(CM_VER).read("/cert/may/" + 
									a.getAppName() + ".certman|"+a.getCa()+"|showpass","*/*");
							if(pf.get(TIMEOUT)) {
								allowed = true;
							} else {
								trans.error().log(errMsg.toMsg(pf));
							}
						}
						if(allowed) {
							File dir = new File(a.getDir());
							Properties props = new Properties();
							FileInputStream fis = new FileInputStream(new File(dir,a.getAppName()+".props"));
							try {
								props.load(fis);
								fis.close();
								fis = new FileInputStream(new File(dir,a.getAppName()+".chal"));
								props.load(fis);
							} finally {
								fis.close();
							}
							
							File f = new File(dir,a.getAppName()+".keyfile");
							if(f.exists()) {
								Symm symm = Symm.obtain(f);
								
								for(Iterator<Entry<Object,Object>> iter = props.entrySet().iterator(); iter.hasNext();) {
									Entry<Object,Object> en = iter.next();
									if(en.getValue().toString().startsWith("enc:???")) {
										System.out.printf("%s=%s\n", en.getKey(), symm.depass(en.getValue().toString()));
									}
								}
							} else {
								trans.error().printf("%s.keyfile must exist to read passwords for %s on %s",
										f.getCanonicalPath(),a.getMechid(), a.getMachine());
							}
						}
					}
				}
			} else {
				trans.error().log(errMsg.toMsg(acf));
			}
		} finally {
			tt.done();
		}

	}
	

	/**
	 * Check returns Error Codes, so that Scripts can know what to do
	 * 
	 *   0 - Check Complete, nothing to do
	 *   1 - General Error
	 *   2 - Error for specific Artifact - read check.msg
	 *   10 - Certificate Updated - check.msg is email content
	 *   
	 * @param trans
	 * @param aafcon
	 * @param cmds
	 * @return
	 * @throws Exception
	 */
	private static int check(Trans trans, AAFCon<?> aafcon, Deque<String> cmds) throws Exception {
		int exitCode=1;
		String mechID = mechID(cmds);
		String machine = machine(cmds);
		
		TimeTaken tt = trans.start("Check Certificate", Env.REMOTE);
		try {
		
			Future<Artifacts> acf = aafcon.client(CM_VER)
					.read("/cert/artifacts/"+mechID+'/'+machine, artifactsDF);
			if(acf.get(TIMEOUT)) {
				// Have to wait for JDK 1.7 source...
				//switch(artifact.getType()) {
				if(acf.value.getArtifact()==null || acf.value.getArtifact().isEmpty()) {
					cons.printf("No Artifacts found for %s on %s", mechID, machine);
				} else {
					String id = aafcon.defID();
					GregorianCalendar now = new GregorianCalendar();
					for(Artifact a : acf.value.getArtifact()) {
						if(id.equals(a.getMechid())) {
							File dir = new File(a.getDir());
							Properties props = new Properties();
							FileInputStream fis = new FileInputStream(new File(dir,a.getAppName()+".props"));
							try {
								props.load(fis);
							} finally {
								fis.close();
							}
							
							String prop;						
							File f;
	
							if((prop=props.getProperty(Config.CADI_KEYFILE))==null ||
								!(f=new File(prop)).exists()) {
									trans.error().printf("Keyfile must exist to check Certificates for %s on %s",
										a.getMechid(), a.getMachine());
							} else {
								String ksf = props.getProperty(Config.CADI_KEYSTORE);
								String ksps = props.getProperty(Config.CADI_KEYSTORE_PASSWORD);
								if(ksf==null || ksps == null) {
									trans.error().printf("Properties %s and %s must exist to check Certificates for %s on %s",
											Config.CADI_KEYSTORE, Config.CADI_KEYSTORE_PASSWORD,a.getMechid(), a.getMachine());
								} else {
									KeyStore ks = KeyStore.getInstance("JKS");
									Symm symm = Symm.obtain(f);
									
									fis = new FileInputStream(ksf);
									try {
										ks.load(fis,symm.depass(ksps).toCharArray());
									} finally {
										fis.close();
									}
									X509Certificate cert = (X509Certificate)ks.getCertificate(mechID);
									String msg = null;

									if(cert==null) {
										msg = String.format("X509Certificate does not exist for %s on %s in %s",
												a.getMechid(), a.getMachine(), ksf);
										trans.error().log(msg);
										exitCode = 2;
									} else {
										GregorianCalendar renew = new GregorianCalendar();
										renew.setTime(cert.getNotAfter());
										renew.add(GregorianCalendar.DAY_OF_MONTH,-1*a.getRenewDays());
										if(renew.after(now)) {
											msg = String.format("As of %s, X509Certificate for %s on %s, expiration %s is still within %d renewal days.\n", 
													Chrono.dateOnlyStamp(), a.getMechid(), a.getMachine(), cert.getNotAfter(),a.getRenewDays());
											trans.info().log(msg);
											exitCode = 0; // OK
										} else {
											trans.info().printf("X509Certificate for %s on %s expiration, %s, needs Renewal.\n", 
													a.getMechid(), a.getMachine(),cert.getNotAfter());
											cmds.offerLast(mechID);
											cmds.offerLast(machine);
											if(placeCerts(trans,aafcon,cmds)) {
												msg = String.format("X509Certificate for %s on %s has been renewed. Ensure services using are refreshed.\n", 
														a.getMechid(), a.getMachine());
												exitCode = 10; // Refreshed
											} else {
												msg = String.format("X509Certificate for %s on %s attempted renewal, but failed. Immediate Investigation is required!\n", 
														a.getMechid(), a.getMachine());
												exitCode = 1; // Error Renewing
											}
										}
									}
									if(msg!=null) {
										FileOutputStream fos = new FileOutputStream(a.getDir()+'/'+a.getAppName()+".msg");
										try {
											fos.write(msg.getBytes());
										} finally {
											fos.close();
										}
									}
								}
								
							}
						}
					}
				}
			} else {
				trans.error().log(errMsg.toMsg(acf));
				exitCode=1;
			}
		} finally {
			tt.done();
		}
		return exitCode;
	}

}
			
		


