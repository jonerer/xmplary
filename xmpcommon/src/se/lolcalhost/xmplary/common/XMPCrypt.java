package se.lolcalhost.xmplary.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.util.encoders.Hex;

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

}
