package se.lolcalhost.xmplary.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.util.encoders.Hex;

import se.lolcalhost.xmplary.common.models.XMPNode;
import edu.vt.middleware.crypt.CryptException;
import edu.vt.middleware.crypt.asymmetric.AsymmetricAlgorithm;
import edu.vt.middleware.crypt.asymmetric.RSA;
import edu.vt.middleware.crypt.digest.SHA512;
import edu.vt.middleware.crypt.pbe.PKCS12KeyGenerator;
import edu.vt.middleware.crypt.symmetric.AES;
import edu.vt.middleware.crypt.symmetric.SymmetricAlgorithm;
import edu.vt.middleware.crypt.util.Base64Converter;

/**
 * TODO: validate cert chain with http://java2s.com/Open-Source/Java/Security/Bouncy-Castle/org/bouncycastle/jce/provider/test/CertPathValidatorTest.java.htm
 * and/or http://java2s.com/Open-Source/Java/Security/Bouncy-Castle/org/bouncycastle/jce/provider/test/CertPathBuilderTest.java.htm
 * http://www.nakov.com/blog/2009/12/01/x509-certificate-validation-in-java-build-and-verify-chain-and-verify-clr-with-bouncy-castle/
 * 
 * @author sx00042
 *
 */
public class XMPCrypt {
	protected static Logger logger = Logger.getLogger(XMPCrypt.class);

	
	private static List<X509CertificateObject> trustAnchors;
	private static List<X509CertificateObject> intermediates;

	public static List<X509CertificateObject> getTrustedCerts() {
		if (intermediates == null) {
			intermediates = new ArrayList<X509CertificateObject>();
			File dir = new File(XMPConfig.getTrustedCertsDir());
			File[] listFiles = dir.listFiles();
			for (File file : listFiles) {
				try {
					FileReader fr;
					fr = new FileReader(file);
					PEMReader p = new PEMReader(fr);
					Object o = p.readObject();
					X509CertificateObject cert = null;
					if (o instanceof X509CertificateObject) {
						cert = (X509CertificateObject) o;
						intermediates.add(cert);
					}
					p.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return intermediates;
	}

	private static X509CertificateObject cert = null;

	public static X509CertificateObject getCertificate() {
		if (cert == null) {
			try {

				File f = new File(XMPConfig.getCertfile());
				FileReader fr;
				fr = new FileReader(f);

				PEMReader r = new PEMReader(fr);
				Object o = r.readObject();
				if (o instanceof X509CertificateObject) {
					cert = (X509CertificateObject) o;
				}
				cert.checkValidity();
				r.close();
			} catch (FileNotFoundException e) {
				logger.error("FATAL: couldn't load certificate file", e);
				System.exit(1);
			} catch (NullPointerException e) {
				logger.error("FATAL: couldn't load certificate file", e);
				System.exit(1);
			} catch (IOException e) {
				logger.error("FATAL: couldn't load certificate file", e);
				System.exit(1);
			} catch (CertificateExpiredException e) {
				logger.error("FATAL: couldn't load certificate file", e);
				System.exit(1);
			} catch (CertificateNotYetValidException e) {
				logger.error("FATAL: couldn't load certificate file", e);
				System.exit(1);
			}
		}
		return cert;
	}

	/**
	 * Signing:
	 * http://stackoverflow.com/questions/1580012/using-a-pem-x-509-private
	 * -key-to-sign-a-message-natively
	 */
	private static KeyPair keyPair = null;

	private static KeyPair getKey() {
		if (keyPair == null) {
			try {
				File f = new File(XMPConfig.getKeyfile());
				FileReader r = new FileReader(f);
				PEMReader pr = new PEMReader(r);
				Object o = pr.readObject();
				keyPair = (KeyPair) o;
				pr.close();
			} catch (FileNotFoundException e) {
				logger.error("FATAL: couldn't load private key", e);
				System.exit(1);
			} catch (NullPointerException e) {
				logger.error("FATAL: couldn't load private key", e);
				System.exit(1);
			} catch (IOException e) {
				logger.error("FATAL: couldn't load private key", e);
				System.exit(1);
			}
		}
		return keyPair;
	}

	/**
	 * Sign some data with my private key, so that others can verify it.
	 * 
	 * @param data
	 * @return
	 */
	public static String sign(String data) {
		String sign = null;
		try {
			Signature signature = Signature
					.getInstance("SHA256WithRSAEncryption");

			signature.initSign(getKey().getPrivate());

			signature.update(data.getBytes());
			byte[] signatureBytes = signature.sign();
			sign = new String(Hex.encode(signatureBytes));
		} catch (InvalidKeyException e) {
			logger.error("FATAL: couldn't sign with private key", e);
		} catch (SignatureException e) {
			logger.error("FATAL: couldn't sign with private key", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("FATAL: couldn't sign with private key", e);
		}
		return sign;
	}

	/**
	 * Verify the given data against the given public key and signature.
	 * 
	 * @param data
	 * @param pubkey
	 * @param signature
	 * @return
	 */
	public static boolean verify(String data, PublicKey pubkey, String signature) {
		try {
			Signature verifier = Signature
					.getInstance("SHA256WithRSAEncryption");
			verifier.initVerify(pubkey);

			verifier.update(data.getBytes());
			if (verifier.verify(Hex.decode(signature.getBytes()))) {
				return true;
			} else {
				return false;
			}
		} catch (InvalidKeyException e) {
			logger.error("FATAL: couldn't verify with public key", e);
		} catch (SignatureException e) {
			logger.error("FATAL: couldn't verify with public key", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("FATAL: couldn't verify with public key", e);
		}
		return false;
	}

	public static void init() {
		Security.addProvider(new BouncyCastleProvider());
		getTrustedCerts();
	}
	
	public static SecretKey decryptKey(String contents) throws CryptException {
		final AsymmetricAlgorithm alg = new RSA();
		alg.setKey(getKey().getPrivate());
		byte[] result = null;
		SecretKey key = null;
		alg.initDecrypt();
		result = alg.decrypt(contents, new Base64Converter());
		key = new SecretKeySpec(result, "AES");
		return key;
	}
	
	public static SecretKey generateKey() {
//		final KeyWithIV keyWithIV = PKCS12KeyGenerator.generatePkcs12(
//				  passChars,
//				  256,
//				  new SHA512(),
//				  salt);
//		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//		KeySpec spec = new PBEKeySpec(password, salt, 1024, 256);
//		SecretKey tmp = factory.generateSecret(spec);
//		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
//		return secret;
		
		// this compiles: :p
//		KeyGenerator gen;
//		try {
//			gen = KeyGenerator.getInstance("PBKDF2WithHmaxSHA1");
//			SecretKey key = gen.generateKey();
//			return key;
//		} catch (Exception e) {
//			logger.error("Couldn't generate key", e);
//		}
//		edu.vt.middleware.crypt.symmetric.SecretKeyUtils();
		KeyGenerator gen;
		try {
			gen = KeyGenerator.getInstance("AES");
			gen.init(256);
			SecretKey key = gen.generateKey();
			return key;
		} catch (Exception e) {
			logger.error("Couldn't generate key", e);
		}
		return null;
	}
		
	/**
	 * Decrypt with my private key.
	 * borrowing from http://www.itcsolutions.eu/2011/08/24/how-to-encrypt-decrypt-files-in-java-with-aes-in-cbc-mode-using-bouncy-castle-api-and-netbeans-or-eclipse/
	 * .
	 * @param contents
	 * @return
	 */
	public static String encryptKey(SecretKey newKey, XMPNode target) {
//		InputStream is = new ByteArrayInputStream(contents.getBytes());
//		OutputStream os = new ByteArrayOutputStream();
//		byte[] key = target.getCert().getPublicKey().getEncoded();
//		byte[] actualkey = new byte[32];
//		System.arraycopy(key, 0, actualkey, 0, 32);
//		BouncyCastleProvider_AES_CBC aes = new BouncyCastleProvider_AES_CBC(actualkey, new byte[BouncyCastleProvider_AES_CBC.blockSize]);
//		try {
//			aes.InitCiphers();
//			aes.CBCEncrypt(is, os);
//		} catch (Exception e) {
//			logger.error("Error in encryption. ", e);
//			e.printStackTrace();
//		}
//		String result = os.toString();
//		return result;
		final AsymmetricAlgorithm alg = new RSA();
		alg.setKey(target.getCert().getPublicKey());
		String result = null;
		try {
			alg.initEncrypt();
			result = alg.encrypt(newKey.getEncoded(), new Base64Converter());
		} catch (CryptException e) {
			logger.error("Error in encryption. ", e);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * https://code.google.com/p/vt-middleware/wiki/vtcrypt#Code_Samples
	 * 
	 * @param conts
	 * @param targetedKey
	 * @return
	 */
	public static String encryptMessage(String conts, SecretKey key, byte[] iv) {
		final SymmetricAlgorithm alg = new AES();
//		byte[] generated = pkcs12KeyGenerator.generate(key, 256);
		alg.setKey(key);
		alg.setIV(iv);
		String message = null;
		
		byte[] c2 = conts.getBytes();
		String back = c2.toString();
		
		Base64Converter conv = new Base64Converter();
		
		byte[] c3 = conv.toBytes(conts);
		String back2 = conv.fromBytes(c3);
		
		byte[] c4 = DatatypeConverter.parseBase64Binary(conts);
		String back3 = DatatypeConverter.printBase64Binary(c4);
		
		
		
		try {
			byte[] c5 = conts.getBytes("UTF-8");
			String back4 = c5.toString();
			String back5 = c5.toString();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		Arrays.toString(conts.getBytes())
		try {
			alg.initEncrypt();
//			byte[] bytes = conv.toBytes(conts);
			message = alg.encrypt(conts.getBytes(), new Base64Converter()).toString();
		} catch (CryptException e) {
			logger.error("is problem: ", e);
		}
		return message;
//		pkcs12KeyGenerator.
		
//		alg.setKey(targetedKey);
//		alg.setIV(keyWithIV.getIV());
//		alg.setKey(keyWithIV.getKey());
//		  alg.encrypt(in, out);
	}
	
	/**
	 * 
	 * 
	 * @param conts
	 * @param targetedKey
	 * @return
	 */
	/**
	 * https://code.google.com/p/vt-middleware/wiki/vtcrypt#Code_Samples
	 * 
	 * using http://stackoverflow.com/questions/6684665/java-byte-array-to-string-to-byte-array
	 * in lieu of a good base64 encoder.
	 * 
	 * 
	 * @param conts
	 * @param key
	 * @param iv
	 * @return
	 */
	public static String decryptMessage(String conts, SecretKey key, byte[] iv) {
		final SymmetricAlgorithm alg = new AES();
		alg.setKey(key);
		alg.setIV(iv);
		String message = null;
//		Base64Converter conv = new Base64Converter();
		
		try {
			alg.initDecrypt();
			byte[] bytes = alg.decrypt(conts, new Base64Converter());
//			message = conv.fromBytes(bytes);
			
			message = new String(bytes);
		} catch (CryptException e) {
			logger.error("is problem: ", e);
		}
		return message;
//		pkcs12KeyGenerator.
		
//		alg.setKey(targetedKey);
//		alg.setIV(keyWithIV.getIV());
//		alg.setKey(keyWithIV.getKey());
//		  alg.encrypt(in, out);
	}

	public static byte[] generateIV() {
		final SymmetricAlgorithm alg = new AES();
		byte[] randomIV = alg.getRandomIV();
		return randomIV;
	}

//	/**
//	 * Encrypt to the public key of target node.
//	 * @param contents
//	 * @param target
//	 * @return
//	 */
//	public static String encrypt(String contents, XMPNode target) {
//		InputStream is = new ByteArrayInputStream(contents.getBytes());
//		OutputStream os = new ByteArrayOutputStream();
//		byte[] key = target.getCert().getPublicKey().getEncoded();
//		byte[] actualkey = new byte[32];
//		System.arraycopy(key, 0, actualkey, 0, 32);
//		BouncyCastleProvider_AES_CBC aes = new BouncyCastleProvider_AES_CBC(actualkey, new byte[BouncyCastleProvider_AES_CBC.blockSize]);
//		try {
//			aes.InitCiphers();
//			aes.CBCEncrypt(is, os);
//		} catch (Exception e) {
//			logger.error("Error in encryption. ", e);
//			e.printStackTrace();
//		}
//		String result = os.toString();
//		return result;
//	}
//  // The default block size
//  public static int blockSize = 16;
//
//  Cipher encryptCipher = null;
//  Cipher decryptCipher = null;
//
//  // Buffer used to transport the bytes from one stream to another
//  byte[] buf = new byte[blockSize];       //input buffer
//  byte[] obuf = new byte[512];            //output buffer
//
//  // The key
//  byte[] key = null;
//  // The initialization vector needed by the CBC mode
//  byte[] IV = null;
//  
//	public static String decrypt(String contents) {
//		InputStream is = new ByteArrayInputStream(contents.getBytes());
//		OutputStream os = new ByteArrayOutputStream();
//		byte[] key = getKey().getPublic().getEncoded();
//		byte[] actualkey = new byte[32];
//		System.arraycopy(key, 0, actualkey, 0, 32);
//		BouncyCastleProvider_AES_CBC aes = new BouncyCastleProvider_AES_CBC(actualkey, new byte[BouncyCastleProvider_AES_CBC.blockSize]);
//		try {
//			aes.InitCiphers();
//			aes.CBCDecrypt(is, os);
//		} catch (Exception e) {
//			logger.error("Error in decryption. ", e);
//			e.printStackTrace();
//		}
//		String result = os.toString();
//		return result;
//		
////		String decrypted = null;
////		try {
////			Base64Encoder enc = new Base64Encoder();
////			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
////			cipher.init(Cipher.DECRYPT_MODE, getKey().getPrivate());
////			decrypted = cipher.dofi
////		} catch (NoSuchAlgorithmException e) {
////			logger.error("Couldn't decrypt contents", e);
////		} catch (NoSuchPaddingException e) {
////			logger.error("Couldn't decrypt contents", e);
////		} catch (InvalidKeyException e) {
////			logger.error("Couldn't decrypt contents", e);
////		}//		String decrypted = null;
////		try {
////		       SecretKey keyValue = new SecretKeySpec(key,"AES");
////	       //3. create the IV
////	       AlgorithmParameterSpec IVspec = new IvParameterSpec(IV);
////	       //4. init the cipher
////	       encryptCipher.init(Cipher.ENCRYPT_MODE, keyValue, IVspec);
////	 
////	       //1 create the cipher
////	       decryptCipher =
////	               Cipher.getInstance("AES/CBC/PKCS5Padding");
////	       //2. the key is already created
////	       //3. the IV is already created
////	       //4. init the cipher
////	       decryptCipher.init(Cipher.DECRYPT_MODE, keyValue, IVspec);
////	       
//////			cipher.init(Cipher.DECRYPT_MODE, getKey().getPrivate());
//////			decrypted = cipher.dofi
////		} catch (NoSuchAlgorithmException e) {
////			logger.error("Couldn't decrypt contents", e);
////		} catch (NoSuchPaddingException e) {
////			logger.error("Couldn't decrypt contents", e);
////		} catch (InvalidKeyException e) {
////			logger.error("Couldn't decrypt contents", e);
////		} catch (InvalidAlgorithmParameterException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//	}
}
