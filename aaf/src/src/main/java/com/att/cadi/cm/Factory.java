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
package com.att.cadi.cm;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collection;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.att.cadi.Symm;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.Trans;

public class Factory {
	public static final String KEY_ALGO = "RSA";
	private static final String PRIVATE_KEY_HEADER = KEY_ALGO + " PRIVATE KEY";
	public static final String SIG_ALGO = "SHA256withRSA";

	public  static final int KEY_LENGTH = 2048;
	private static final KeyPairGenerator keygen;
	private static final KeyFactory keyFactory;
	private static final CertificateFactory certificateFactory;
	private static final SecureRandom random;
	
	
	private static final Symm base64 = Symm.base64.copy(64);

	static {
			random = new SecureRandom();
			KeyPairGenerator tempKeygen;
			try {
				tempKeygen = KeyPairGenerator.getInstance(KEY_ALGO);//,"BC");
				tempKeygen.initialize(KEY_LENGTH, random);
			} catch (NoSuchAlgorithmException e) {
				tempKeygen = null;
				e.printStackTrace(System.err);
			}
			keygen = tempKeygen;

			KeyFactory tempKeyFactory;
			try {
				tempKeyFactory=KeyFactory.getInstance(KEY_ALGO);//,"BC"
			} catch (NoSuchAlgorithmException e) {
				tempKeyFactory = null;
				e.printStackTrace(System.err);
			};
			keyFactory = tempKeyFactory;
			 
			CertificateFactory tempCertificateFactory;
			try {
				tempCertificateFactory = CertificateFactory.getInstance("X.509");
			} catch (CertificateException e) {
				tempCertificateFactory = null;
				e.printStackTrace(System.err);
			}
			certificateFactory = tempCertificateFactory;

		 
	}


	public static KeyPair generateKeyPair(Trans trans) {
		TimeTaken tt;
		if(trans!=null) {
			tt = trans.start("Generate KeyPair", Env.SUB);
		} else {
			tt = null;
		}
		try {
			return keygen.generateKeyPair();
		} finally {
			if(tt!=null) {
				tt.done();
			}
		}
	}  

	private static final String LINE_END = "-----\n";

	protected static String textBuilder(String kind, byte[] bytes) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("-----BEGIN ");
		sb.append(kind);
		sb.append(LINE_END);

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		base64.encode(bais, baos);
		sb.append(new String(baos.toByteArray()));
		
		if(sb.charAt(sb.length()-1)!='\n') {
			sb.append('\n');
		}
		sb.append("-----END ");
		sb.append(kind);
		sb.append(LINE_END);
		return sb.toString();
	}
	
	public static PrivateKey toPrivateKey(Trans trans, String pk) throws IOException, CertException {
		byte[] bytes = decode(new StringReader(pk));
		return toPrivateKey(trans, bytes);
	}
	
	public static PrivateKey toPrivateKey(Trans trans, byte[] bytes) throws IOException, CertException {
		TimeTaken tt=trans.start("Reconstitute Private Key", Env.SUB);
		try {
			return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
		} catch (InvalidKeySpecException e) {
			throw new CertException("Translating Private Key from PKCS8 KeySpec",e);
		} finally {
			tt.done();
		}
	}
	
	public static PrivateKey toPrivateKey(Trans trans, File file) throws IOException, CertException {
		TimeTaken tt = trans.start("Decode Private Key File", Env.SUB);
		try {
			return toPrivateKey(trans,decode(file));
		}finally {
			tt.done();
		}
	}


	public static String toString(Trans trans, PrivateKey pk) throws IOException {
		trans.debug().log("Private Key to String");
		return textBuilder(PRIVATE_KEY_HEADER,pk.getEncoded());
	}

	public static PublicKey toPublicKey(Trans trans, String pk) throws IOException {
		TimeTaken tt = trans.start("Reconstitute Public Key", Env.SUB);
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(pk.getBytes());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Symm.base64noSplit.decode(bais, baos);

			return keyFactory.generatePublic(new X509EncodedKeySpec(baos.toByteArray()));
		} catch (InvalidKeySpecException e) {
			trans.error().log(e,"Translating Public Key from X509 KeySpec");
			return null;
		} finally {
			tt.done();
		}
	}
	
	public static String toString(Trans trans, PublicKey pk) throws IOException {
		trans.debug().log("Public Key to String");
		return textBuilder("PUBLIC KEY",pk.getEncoded());
	}

	public static Collection<? extends Certificate> toX509Certificate(Trans trans, String x509) throws CertificateException {
		return toX509Certificate(trans, x509.getBytes());
	}
	
	public static Collection<? extends Certificate> toX509Certificate(Trans trans, List<String> x509s) throws CertificateException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			for(String x509 : x509s) {
				baos.write(x509.getBytes());
			}
		} catch (IOException e) {
			throw new CertificateException(e);
		}
		return toX509Certificate(trans, new ByteArrayInputStream(baos.toByteArray()));
	}

	public static Collection<? extends Certificate> toX509Certificate(Trans trans, byte[] x509) throws CertificateException {
		return certificateFactory.generateCertificates(new ByteArrayInputStream(x509));
	}

	public static Collection<? extends Certificate> toX509Certificate(Trans trans, File file) throws CertificateException, FileNotFoundException {
		FileInputStream fis = new FileInputStream(file);
		try {
			return toX509Certificate(trans,fis);
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				throw new CertificateException(e);
			}
		}
	}

	public static Collection<? extends Certificate> toX509Certificate(Trans trans, InputStream is) throws CertificateException {
		TimeTaken tt=trans.start("Reconstitute Certificates", Env.SUB);
		try {
			return certificateFactory.generateCertificates(is);
		} finally {
			tt.done();
		}
	}

	

	public static String toString(Trans trans, Certificate cert) throws IOException, CertException {
		if(trans.debug().isLoggable()) {
			StringBuilder sb = new StringBuilder("Certificate to String");
			if(cert instanceof X509Certificate) {
				sb.append(" - ");
				sb.append(((X509Certificate)cert).getSubjectDN());
			}
			trans.debug().log(sb);
		}
		try {
			if(cert==null) {
				throw new CertException("Certificate not built");
			}
			return textBuilder("CERTIFICATE",cert.getEncoded());
		} catch (CertificateEncodingException e) {
			throw new CertException(e);
		}
	}

	public static Cipher pkCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		return Cipher.getInstance(KEY_ALGO); 
	}

	public static Cipher pkCipher(Key key, boolean encrypt) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance(KEY_ALGO);
		cipher.init(encrypt?Cipher.ENCRYPT_MODE:Cipher.DECRYPT_MODE,key);
		return cipher;
	}

	public static byte[] strip(Reader rdr) throws IOException {
		BufferedReader br = new BufferedReader(rdr);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String line;
		while((line=br.readLine())!=null) {
			if(line.length()>0 &&
			   !line.startsWith("-----") &&
			   line.indexOf(':')<0) {  // Header elements
				baos.write(line.getBytes());
			}
		}
		return baos.toByteArray();
	}
	
	public static class StripperInputStream extends InputStream {
		private Reader created;
		private BufferedReader br;
		private int idx;
		private String line;

		public StripperInputStream(Reader rdr) {
			if(rdr instanceof BufferedReader) {
				br = (BufferedReader)rdr;
			} else {
				br = new BufferedReader(rdr);
			}
			created = null;
		}
		
		public StripperInputStream(File file) throws FileNotFoundException {
			this(new FileReader(file));
			created = br;
		}

		public StripperInputStream(InputStream is) throws FileNotFoundException {
			this(new InputStreamReader(is));
			created = br;
		}

		@Override
		public int read() throws IOException {
			if(line==null || idx>=line.length()) {
				while((line=br.readLine())!=null) {
					if(line.length()>0 &&
					   !line.startsWith("-----") &&
					   line.indexOf(':')<0) {  // Header elements
						break;
					}
				}

				if(line==null) {
					return -1;
				}
				idx = 0;
			}
			return line.charAt(idx++);
		}

		/* (non-Javadoc)
		 * @see java.io.InputStream#close()
		 */
		@Override
		public void close() throws IOException {
			if(created!=null) {
				created.close();
			}
		}
	}

	public static class Base64InputStream extends InputStream {
		private InputStream created;
		private InputStream is;
		private byte trio[];
		private byte duo[];
		private int idx;

		
		public Base64InputStream(File file) throws FileNotFoundException {
			this(new FileInputStream(file));
			created = is;
		}

		public Base64InputStream(InputStream is) throws FileNotFoundException {
			this.is = is;
			trio = new byte[3];
			idx = 4;
		}

		@Override
		public int read() throws IOException {
			if(duo==null || idx>=duo.length) {
				int read = is.read(trio);
				if(read==-1) {
					return -1;
				}
				duo = Symm.base64.decode(trio);
				if(duo==null || duo.length==0) {
					return -1;
				}
				idx=0;
			}
			
			return duo[idx++];
		}

		/* (non-Javadoc)
		 * @see java.io.InputStream#close()
		 */
		@Override
		public void close() throws IOException {
			if(created!=null) {
				created.close();
			}
		}
	}

	public static byte[] decode(byte[] bytes) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Symm.base64.decode(bais, baos);
		return baos.toByteArray();
	}
	
	public static byte[] decode(File f) throws IOException {
		FileReader fr = new FileReader(f);
		try {
			return Factory.decode(fr);
		} finally {
			fr.close();
		}

	}
	public static byte[] decode(Reader rdr) throws IOException {
		return decode(strip(rdr));
	}


	public static byte[] binary(File file) throws IOException {
		DataInputStream dis = new DataInputStream(new FileInputStream(file));
		try {
			byte[] bytes = new byte[(int)file.length()];
			dis.readFully(bytes);
			return bytes;
		} finally {
			dis.close();
		}
	}


	public static byte[] sign(Trans trans, byte[] bytes, PrivateKey pk) throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
		TimeTaken tt = trans.start("Sign Data", Env.SUB);
		try {
			Signature sig = Signature.getInstance(SIG_ALGO);
			sig.initSign(pk, random);
			sig.update(bytes);
			return sig.sign();
		} finally {
			tt.done();
		}
	}

	// TODO IMPLEMENT!
	public static void getSignature(byte[] signed) {
		// TODO Auto-generated method stub
		
	}

}
