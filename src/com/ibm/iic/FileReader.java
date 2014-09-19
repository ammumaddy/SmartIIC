package com.ibm.iic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class FileReader {
	

	public HashMap<String, String> getCloudantMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		final JSONObject jCred = parseVCAPServices("cloudantNoSQLDB");
		String user = (String) jCred.get("username");
		System.out.println("username========= " + user);
		String pass = (String) jCred.get("password");
		System.out.println("password========= " + pass);
		map.put("user", user);
		map.put("pass", pass);
		return map;
	}

	public HashMap<String, String> getTwilioMap() {
		String filename = "twilio.properties";
		Properties prop = new Properties();
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			prop.load(getClass().getResourceAsStream(filename));
			String from = prop.getProperty("from");
			
			final JSONObject jCred = parseVCAPServices("user-provided");
			String sid = (String) jCred.get("accountSID");
			System.out.println("sid========= " + sid);
			String token = (String) jCred.get("authToken");
			System.out.println("token========= " + token);
			
			map.put("from", from);
			map.put("sid", sid);
			map.put("token", token);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * Obtain the JSONObject containing the VCAPServices details for connecting
	 * to the service instance.
	 */
	private JSONObject parseVCAPServices(String serviceName) {

		// Get the contents of the environment variable.
		final String envVCAPServices = System.getenv("VCAP_SERVICES");
		if (envVCAPServices == null) {
			System.err.println("ERROR: The VCAP_SERVICES environment variable has not been set");
			return null;
		}

		// Parse the env string into a JSONObject
		try {
			final JSONObject jVCAP = JSONObject.parse(envVCAPServices);

			// Get the array associated with the service.
			final JSONArray aService = (JSONArray) jVCAP.get(serviceName);

			// Check if any service has been found
			if (aService == null) {
				throw new Exception("ERROR: Connection details could not be found.");
			}

			// Get the credentials of the service

			// We're only handling the case of a single bound service here - to
			// extend this, loop
			// over all aService and handle appropriately
			final JSONObject jWSMQS = (JSONObject) aService.get(0);
			// With the WebSphere MQ Service JSON Object, now extract the
			// credentials object
			final JSONObject jCred = (JSONObject) jWSMQS.get("credentials");
			if (jCred == null) {
				System.err.println("ERROR: JSONArray has no service credentials");
				return null;
			}
			return jCred;
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.println("ERROR: VCAP_SERVICES cannot be parsed");
			return null;
		}
	}
}
