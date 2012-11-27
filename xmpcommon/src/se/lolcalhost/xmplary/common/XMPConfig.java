package se.lolcalhost.xmplary.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMReader;

import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;

public class XMPConfig {

	private static Properties p;
	private static File f;
	static Logger logger = Logger.getLogger(XMPConfig.class);
	private static String conffile;

	public static Properties getInstance() {
		if (p == null) {
			init("conf.xml");
		}
		return p;
	}

	public static String Domain() {
		return p.getProperty("Domain");
	}

	public static String RoomDomain() {
		return p.getProperty("RoomDomain");
	}

	public static String Database() {
		return p.getProperty("database");
	}

	public static String Room() {
		return p.getProperty("room");
	}

	public static String Name() {
		return p.getProperty("name");
	}

	public static NodeType Type() {
		return NodeType.valueOf(p.getProperty("type"));
	}

	public static DateFormat jsonDateFormat() {
		return DateFormat.getDateTimeInstance();
	}

	static void init(String conffile) {
		XMPConfig.conffile = conffile;

		f = new File(conffile);
		p = new Properties();
		if (f.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
				p.loadFromXML(fis);
			} catch (InvalidPropertiesFormatException e) {
				logger.error("Config file is malformed.");
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("Could not open config file for reading");
				e.printStackTrace();
			}
		}
		boolean hasName = p.getProperty("name") != null
				&& !p.getProperty("name").equals("player");
		if (!hasName) {
			logger.info("No name in config file. Setting one.");
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(f);
			} catch (FileNotFoundException e) {
				logger.error("Could not open config file for writing.");
				e.printStackTrace();
			}

			SecureRandom sr = new SecureRandom();
			sr.setSeed(System.nanoTime());
			long l = -1;
			while (l < 0)
				l = sr.nextLong();

			// String s = Long.toString(System.nanoTime());
			String name = "leaf-" + l;
			logger.info("New node name: " + name);

			p.setProperty("name", name);
			p.setProperty("Full Name", "XMPlary node " + name);
			p.setProperty("room", "XMPlary");
			try {
				p.storeToXML(fos, "comments?");
			} catch (IOException e) {
				logger.error("Could not save config file");
				e.printStackTrace();
			}
		}

	}

	public static int getMaxDataPointsPerPacket() {
		if (p.contains("MaxDataPointsPerPacket")) {
			return Integer.valueOf(p.getProperty("MaxDataPointsPerPacket"));
		} else {
			return 240;
		}
	}

	public static String getTrustedCertsDir() {
		return "../certs/certs/ca";
	}

	public static String getCertfile() {
		return p.getProperty("certfile");
	}

	public static String getKeyfile() {
		return p.getProperty("keyfile");
	}
}
