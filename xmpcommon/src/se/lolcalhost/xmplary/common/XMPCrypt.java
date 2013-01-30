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
		alg.setKey(key);
		alg.setIV(iv);
		String message = null;
		try {
			alg.initEncrypt();
			message = alg.encrypt(conts.getBytes(), new Base64Converter()).toString();
		} catch (CryptException e) {
			logger.error("is problem: ", e);
		}
		return message;
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
		
		try {
			alg.initDecrypt();
			byte[] bytes = alg.decrypt(conts, new Base64Converter());
			
			message = new String(bytes);
		} catch (CryptException e) {
			logger.error("is problem: ", e);
		}
		return message;
	}

	public static byte[] generateIV() {
		final SymmetricAlgorithm alg = new AES();
		byte[] randomIV = alg.getRandomIV();
		return randomIV;
	}
}
