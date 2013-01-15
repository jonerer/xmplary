package se.lolcalhost.xmplary.xmpleaf;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.XMPConfig;

public class WelderConfig {
	private static Properties p;
	static Logger logger = Logger.getLogger(XMPConfig.class);
	public static String getWelderConfig() {
		if (p == null) {
			initWelderConfig();
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		try {
			p.storeToXML(bos, "");
		} catch (IOException e) {
			logger.error("Error in getting welder config.", e);
		}
		return new String(bos.toByteArray());
	}
	
	private static void initWelderConfig() {
		String weldconf = XMPConfig.getWelderConfigFile();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(weldconf);
			p = new Properties();
			p.loadFromXML(fis);
			fis.close();
			logger.info("Loaded welder config file.");
		} catch (InvalidPropertiesFormatException e) {
			logger.info("Created new welder config file.");
			p = new Properties();
		} catch (IOException e) {
			logger.info("Created new welder config file.");
			p = new Properties();
		}

	}

	public static void setWelderConfigVar(String name, String value) {
		if (p == null) {
			initWelderConfig();
		}
		p.setProperty(name, value);
		save();
	}

	private static void save() {
		String weldconf = XMPConfig.getWelderConfigFile();
		try {
			FileOutputStream fos = new FileOutputStream(weldconf);
			p.storeToXML(fos, "");
			fos.close();
		} catch (FileNotFoundException e) {
			logger.error("Error in saving welder config",e);
		} catch (IOException e) {
			logger.error("Error in saving welder config",e);
		}
	}
}
