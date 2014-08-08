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
 public static final String ACCOUNT_SID = "ACfe05e2092bea77df8a431973e0dd98eb"; 
 public static final String AUTH_TOKEN = "8269ac3c0599bb76d8774beaa8ab026a"; 
 
 public static void main(String[]args) throws TwilioRestException { 
	TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN); 
 
	 // Build the parameters 
	 List<NameValuePair> params = new ArrayList<NameValuePair>();  
	 params.add(new BasicNameValuePair("From", "+19376884552"));
	 params.add(new BasicNameValuePair("To", "+8618502158511"));
	 params.add(new BasicNameValuePair("Body", "This is the fist ¶ÌÐÅ£¡"));
	 
	 MessageFactory messageFactory = client.getAccount().getMessageFactory(); 
	 Message message = messageFactory.create(params); 
	 System.out.println(message.getSid()); 
 } 
}
