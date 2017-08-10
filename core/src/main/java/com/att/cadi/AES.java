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
package com.att.cadi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.att.cadi.util.Chmod;

public class AES {
	public static final String AES = AES.class.getSimpleName();
	public static final int AES_KEY_SIZE = 128; // 256 isn't supported on all JDKs.
	
	private Cipher aesCipher;
	private SecretKeySpec aeskeySpec;

	public AES() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
		aesCipher = Cipher.getInstance(AES);
	    aeskeySpec = new SecretKeySpec(newKey().getEncoded(), AES);
	}
	
	public static SecretKey newKey() throws NoSuchAlgorithmException {
		KeyGenerator kgen = KeyGenerator.getInstance(AES);
	    kgen.init(AES_KEY_SIZE);
	    return kgen.generateKey();
	}

	public AES(File keyfile) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
		aesCipher = Cipher.getInstance(AES);
		byte[] aesKey = new byte[AES_KEY_SIZE/8];
		FileInputStream fis = new FileInputStream(keyfile);
		try {
			fis.read(aesKey);
		} finally {
			fis.close();
		}
		aeskeySpec = new SecretKeySpec(aesKey,AES);
	}

	public AES(byte[] aeskey, int offset, int len) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
		aesCipher = Cipher.getInstance(AES);
		aeskeySpec = new SecretKeySpec(aeskey,offset,len,AES);
	}
	
	public byte[] encrypt(byte[] in) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		aesCipher.init(Cipher.ENCRYPT_MODE,aeskeySpec);
		return aesCipher.doFinal(in);
	}
	
	public byte[] decrypt(byte[] in) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		aesCipher.init(Cipher.DECRYPT_MODE,aeskeySpec); 
		return aesCipher.doFinal(in);
	}
	
	public void save(File keyfile) throws IOException {
		FileOutputStream fis = new FileOutputStream(keyfile);
		try {
			fis.write(aeskeySpec.getEncoded());
		} finally {
			fis.close();
		}
		Chmod.to400.chmod(keyfile);
	}

	public CipherOutputStream outputStream(OutputStream os, boolean encrypt) {
		try {
			if(encrypt) {
				aesCipher.init(Cipher.ENCRYPT_MODE,aeskeySpec);
			} else {
				aesCipher.init(Cipher.DECRYPT_MODE,aeskeySpec);
			}
		} catch (InvalidKeyException e) {
			// KeySpec created earlier... no chance being wrong.
		} 
		return new CipherOutputStream(os,aesCipher);
	}
	
	public CipherInputStream inputStream(InputStream is, boolean encrypt) {
		try {
			if(encrypt) {
				aesCipher.init(Cipher.ENCRYPT_MODE,aeskeySpec);
			} else {
				aesCipher.init(Cipher.DECRYPT_MODE,aeskeySpec);
			}
		} catch (InvalidKeyException e) {
			// KeySpec created earlier... no chance being wrong.
		} 
		
		return new CipherInputStream(is,aesCipher);
	}
}
