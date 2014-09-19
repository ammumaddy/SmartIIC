package com.ibm.iic;

import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.twilio.sdk.*;
import com.twilio.sdk.resource.factory.*;
import com.twilio.sdk.resource.instance.*;
import com.twilio.sdk.resource.list.*;

public class SmsSender {
	// Find your Account Sid and Token at twilio.com/user/account
	public static String ACCOUNT_SID = null;
	public static String AUTH_TOKEN = null;
	public static String From = null;

	public static void sendSMS(String mobile, String body)
			throws TwilioRestException {

		FileReader fr = new FileReader();
		HashMap<String, String> map = fr.getTwilioMap();
		From = map.get("from");
		ACCOUNT_SID = map.get("sid");
		AUTH_TOKEN = map.get("token");

		TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

		// Build the parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("From", From));
		params.add(new BasicNameValuePair("To", mobile));
		params.add(new BasicNameValuePair("Body", body));

		MessageFactory messageFactory = client.getAccount().getMessageFactory();
		Message message = messageFactory.create(params);
		System.out.println(message.getSid());
	}
}
