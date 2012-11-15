package se.lolcalhost.xmplary.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;

public class XMPConfig {

	private static Properties p;
	private static File f;
	static Logger logger = Logger.getLogger(XMPConfig.class);
	
	public static Properties getInstance() {
		if (p == null) {
			init();
		}
		return p;
	}
	
	public static String Domain() {
		return p.getProperty("Domain");
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

	private static void init() {
		f = new File("conf.xml");
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
		boolean hasName = p.getProperty("name") != null && !p.getProperty("name").equals("player");
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
			
//			String s = Long.toString(System.nanoTime());
			String name = "leaf-" + l ;
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
			return 40;
		}
	}
}
