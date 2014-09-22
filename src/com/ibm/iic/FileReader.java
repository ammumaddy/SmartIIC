package com.ibm.iic;

import java.util.HashMap;
import java.util.Iterator;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;
import com.twilio.sdk.resource.list.IncomingPhoneNumberList;

public class FileReader {

	public HashMap<String, String> getCloudantMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		final JSONObject jCred = parseVCAPServices("cloudantNoSQLDB");
		String user = (String) jCred.get("username");
		String pass = (String) jCred.get("password");
		map.put("user", user);
		map.put("pass", pass);
		return map;
	}

	public HashMap<String, String> getTwilioMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		try {

			final JSONObject jCred = parseVCAPServices("user-provided");
			String sid = (String) jCred.get("accountSID");
			String token = (String) jCred.get("authToken");
			String from = getNumber(sid, token);

			map.put("from", from);
			map.put("sid", sid);
			map.put("token", token);
		} catch (TwilioRestException e) {
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
			System.err
					.println("ERROR: The VCAP_SERVICES environment variable has not been set");
			return null;
		}

		// Parse the env string into a JSONObject
		try {
			final JSONObject jVCAP = JSONObject.parse(envVCAPServices);

			// Get the array associated with the service.
			final JSONArray aService = (JSONArray) jVCAP.get(serviceName);

			// Check if any service has been found
			if (aService == null) {
				throw new Exception(
						"ERROR: Connection details could not be found.");
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
				System.err
						.println("ERROR: JSONArray has no service credentials");
				return null;
			}
			return jCred;
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.println("ERROR: VCAP_SERVICES cannot be parsed");
			return null;
		}
	}

	public String getNumber(String sid, String token)
			throws TwilioRestException {
		String number = "";
		TwilioRestClient client = new TwilioRestClient(sid, token);

		// Build a filter for the AvailablePhoneNumberList
		HashMap<String, String> params = new HashMap<String, String>();

		IncomingPhoneNumberList innumbers = client.getAccount()
				.getIncomingPhoneNumbers(params);
		for (Iterator<IncomingPhoneNumber> it = innumbers.iterator(); it
				.hasNext();) {
			IncomingPhoneNumber ip = it.next();
			if (ip.getPhoneNumber() != null && !ip.getPhoneNumber().equals("")) {
				number = ip.getPhoneNumber();
				break;
			}
		}
		return number;
	}
	
}
