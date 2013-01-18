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
	private static final String files_dir = "files/";

	public static Properties getInstance() {
		if (p == null) {
			init(files_dir+"conf.xml");
		}
		return p;
	}

	public static String Domain() {
		return p.getProperty("Domain");
	}

	public static String Address() {
		return p.getProperty("Address");
	}
	
	public static boolean isDebug() {
		if (!p.containsKey("debug")) {
			return false;
		}
		return Boolean.valueOf(p.getProperty("debug"));
	}

	public static String RoomDomain() {
		return p.getProperty("RoomDomain");
	}

	public static String Database() {
		return files_dir+p.getProperty("database");
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
		conffile = files_dir + conffile;
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
	
	public static String getCertBaseDir() {
		if (new File("certs").exists()) {
			return "certs/";
		} else {
			return "../certs/";
		}
	}

	public static String getTrustedCertsDir() {
		return getCertBaseDir() + "certs/ca";
	}

	public static String getCertfile() {
		return getCertBaseDir() + p.getProperty("certfile");
	}

	public static String getKeyfile() {
		return getCertBaseDir() + p.getProperty("keyfile");
	}

	public static String getWelderConfigFile() {
		if (p.contains("welderconfig"))
			return p.getProperty(files_dir+"welderconfig");
		else
			return files_dir+"welderconfig.xml";
	}

	public static String getLog4jConfig() {
		if (new File("files/log5j.properties").exists()) {
			return "files/log5j.properties";
		} else {
			return "../xmpcommon/files/log5j.properties";
		}
	}
}
