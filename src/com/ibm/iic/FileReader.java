package com.ibm.iic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class FileReader {

	public HashMap<String, String> getCloudantMap() {
		String filename = "cloudant.properties";
		Properties prop = new Properties();
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			prop.load(getClass().getResourceAsStream(filename));
			String user = prop.getProperty("user");
			String pass = prop.getProperty("pass");
			map.put("user", user);
			map.put("pass", pass);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public HashMap<String, String> getTwilioMap() {
		String filename = "twilio.properties";
		Properties prop = new Properties();
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			prop.load(getClass().getResourceAsStream(filename));
			String from = prop.getProperty("from");
			String sid = prop.getProperty("sid");
			String token = prop.getProperty("token");
			map.put("from", from);
			map.put("sid", sid);
			map.put("token", token);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
}
