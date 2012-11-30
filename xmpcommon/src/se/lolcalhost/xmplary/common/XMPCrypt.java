package se.lolcalhost.xmplary.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.util.encoders.Hex;

import se.lolcalhost.xmplary.common.models.XMPNode;

public class XMPCrypt {
	protected static Logger logger = Logger.getLogger(XMPCrypt.class);

	private static List<X509CertificateObject> trusted;

	public static List<X509CertificateObject> getTrustedCerts() {
		if (trusted == null) {
			trusted = new ArrayList<X509CertificateObject>();
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
						trusted.add(cert);
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
		return trusted;
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
		getTrustedCerts();

		// X509CertificateObject certificate = XMPConfig.getCertificate();
		// KeyPair key = XMPConfig.getKey();
		// PublicKey k1 = certificate.getPublicKey();
		// PublicKey k2 = key.getPublic();
	}
	
	private byte[] decryptWithLWCrypto(byte[] cipher, String password, byte[] salt, final  int iterationCount)
	        throws Exception
	{
	    PKCS12ParametersGenerator pGen = new PKCS12ParametersGenerator(new SHA256Digest());
	    char[] passwordChars = password.toCharArray();
	    final byte[] pkcs12PasswordBytes = PBEParametersGenerator
	            .PKCS12PasswordToBytes(passwordChars);
	    pGen.init(pkcs12PasswordBytes, salt, iterationCount);
	    CBCBlockCipher aesCBC = new CBCBlockCipher(new AESEngine());
	    ParametersWithIV aesCBCParams = (ParametersWithIV) pGen.generateDerivedParameters(256, 128);
	    aesCBC.init(false, aesCBCParams);
	    PaddedBufferedBlockCipher aesCipher = new PaddedBufferedBlockCipher(aesCBC,
	            new PKCS7Padding());
	    byte[] plainTemp = new byte[aesCipher.getOutputSize(cipher.length)];
	    int offset = aesCipher.processBytes(cipher, 0, cipher.length, plainTemp, 0);
	    int last = aesCipher.doFinal(plainTemp, offset);
	    final byte[] plain = new byte[offset + last];
	    System.arraycopy(plainTemp, 0, plain, 0, plain.length);
	    return plain;
	}
	
	public static void getSymmetricKey() {
//		KeyGen256 keyGen256 = new AES.KeyGen256();
//		keyGen256.
//		AESEngine aesEngine = new AESEngine();
	}

	/**
	 * Decrypt with my private key.
	 * borrowing from http://www.itcsolutions.eu/2011/08/24/how-to-encrypt-decrypt-files-in-java-with-aes-in-cbc-mode-using-bouncy-castle-api-and-netbeans-or-eclipse/
	 * .
	 * @param contents
	 * @return
	 */
	
    // The default block size
    public static int blockSize = 16;
 
    Cipher encryptCipher = null;
    Cipher decryptCipher = null;
 
    // Buffer used to transport the bytes from one stream to another
    byte[] buf = new byte[blockSize];       //input buffer
    byte[] obuf = new byte[512];            //output buffer
 
    // The key
    byte[] key = null;
    // The initialization vector needed by the CBC mode
    byte[] IV = null;
    
	public String decrypt(String contents) {
		InputStream is = new ByteArrayInputStream(contents.getBytes());
		OutputStream os = new ByteArrayOutputStream();
		BouncyCastleAPI_AES_CBC aes = new BouncyCastleAPI_AES_CBC(getKey().getPublic().getEncoded());
		aes.InitCiphers();
		try {
			aes.CBCEncrypt(is, os);
		} catch (DataLengthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShortBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidCipherTextException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		String decrypted = null;
//		try {
//			Base64Encoder enc = new Base64Encoder();
//			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//			cipher.init(Cipher.DECRYPT_MODE, getKey().getPrivate());
//			decrypted = cipher.dofi
//		} catch (NoSuchAlgorithmException e) {
//			logger.error("Couldn't decrypt contents", e);
//		} catch (NoSuchPaddingException e) {
//			logger.error("Couldn't decrypt contents", e);
//		} catch (InvalidKeyException e) {
//			logger.error("Couldn't decrypt contents", e);
//		}//		String decrypted = null;
//		try {
//		       SecretKey keyValue = new SecretKeySpec(key,"AES");
//	       //3. create the IV
//	       AlgorithmParameterSpec IVspec = new IvParameterSpec(IV);
//	       //4. init the cipher
//	       encryptCipher.init(Cipher.ENCRYPT_MODE, keyValue, IVspec);
//	 
//	       //1 create the cipher
//	       decryptCipher =
//	               Cipher.getInstance("AES/CBC/PKCS5Padding");
//	       //2. the key is already created
//	       //3. the IV is already created
//	       //4. init the cipher
//	       decryptCipher.init(Cipher.DECRYPT_MODE, keyValue, IVspec);
//	       
////			cipher.init(Cipher.DECRYPT_MODE, getKey().getPrivate());
////			decrypted = cipher.dofi
//		} catch (NoSuchAlgorithmException e) {
//			logger.error("Couldn't decrypt contents", e);
//		} catch (NoSuchPaddingException e) {
//			logger.error("Couldn't decrypt contents", e);
//		} catch (InvalidKeyException e) {
//			logger.error("Couldn't decrypt contents", e);
//		} catch (InvalidAlgorithmParameterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return null;
	}

	/**
	 * Encrypt to the public key of target node.
	 * @param contents
	 * @param target
	 * @return
	 */
	public static String encrypt(String contents, XMPNode target) {
		return null;
	}

}
