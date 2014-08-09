// ******************************************************************
//
// Program name: WebMQLightSampleWorker
//
// Description:
//
// A java program that demonstrates the use of the IBM Bluemix MQ Light Service
//
// <copyright
// notice="lm-source-program"
// pids=""
// years="2013"
// crc="659007836" >
// Licensed Materials - Property of IBM
//
//
// (C) Copyright IBM Corp. 2013 All Rights Reserved.
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// </copyright>
// *******************************************************************

package com.ibm.iic;

import java.net.MalformedURLException;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import com.cloudant.OperatorDB;
import com.ibm.iic.MQLightConnectionHelper;
import com.ibm.json.java.JSONObject;
import com.ibm.msg.client.wmq.WMQConstants;
import com.twilio.sdk.TwilioRestException;

/**
 * This file forms part of the MQ Light Sample Messaging Application - Worker
 * offload pattern sample provided to demonstrate the use of the IBM Bluemix MQ
 * Light Service. It provides a simple implementation of a runnable java program
 * to demonstrate the steps required to deploy a simple java backend with
 * messaging resources. The worker connects to the RequestQueue and listens for
 * messages placed on that queue. When a message is received, this program
 * converts the message to upper case, appends a string signifying which process
 * operated on the message, and then returns the processed message to the
 * destination specified in the message. This source file is packaged as part of
 * the MQLightSample/worker.jar and the 'front-end' service which it must be
 * partnered with is packaged in the MQLightSample/web.war file. Both
 * applications should be uploaded to Bluemix and connected to a Messaging
 * Service. See the info-center for further details of how to use the sample.
 */
public class Worker implements Runnable {

	private static Worker instance = null;

	private boolean unlock = true;

	public boolean isUnlock() {
		return unlock;
	}

	public void setUnlock(boolean unlock) {
		this.unlock = unlock;
	}

	private Worker() {
	}

	public static synchronized Worker getInstance() {
		if (instance == null)
			instance = new Worker();
		return instance;
	}

	/**
	 * Main() entry point for java application. Synchronously connects to a
	 * queue and waits for a message, then processes it and returns it to the
	 * reply queue supplied with the message. The worker will run until it has
	 * received ten erroneous messages.
	 * 
	 * @param args
	 * @throws Exception
	 */
	protected void startWorker() throws Exception {
		// Establish a connection to the request queue
		MQLightConnectionHelper connHelper = MQLightConnectionHelper
				.getMQLightConnectionHelper("mqlight");
		Connection conn = connHelper.getJmsConnectionFactory()
				.createConnection(connHelper.getUsername(),
						connHelper.getPassword());
		Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		conn.start();
		Queue queue = sess.createQueue("jms/requestQueue");

		// Create a consumer on the request queue so that we can set a worker as
		// a message listener - this will allow us to process the messages on
		// the queue asynchronously
		MessageConsumer consumer = sess.createConsumer(queue);

		int errorCount = 0;
		System.out.println("begin===============");
		// Keep running until the worker throws ten errors, then drop
		while (errorCount < 10) {
			// Wait to receive a message
			Message message = consumer.receive();
			try {
				System.out.println("Worker invoked");

				// Retrieve the destination to send a reply to.
				Destination replyDestination = message.getJMSReplyTo();

				// Create the JMS resources required to send the reply
				MessageProducer replyProducer = sess
						.createProducer(replyDestination);
				replyProducer.setTimeToLive(30000);

				// Generate the reply message
				// This takes the body of the incoming message, converts it to
				// upper case, then returns it with this instance ID
				String msg = null;
				TextMessage replyMessage = sess.createTextMessage();
				if (message instanceof TextMessage) {
					msg = new String(((TextMessage) message).getText()
							.getBytes("UTF-8"), "UTF-8");
					String convertedMsg = sendMsg(msg);
					replyMessage.setText("The message \"" + convertedMsg + "\""
							+ " have been sent to the applicant via SMS.");
				} else {
					String txt = "ERROR!";
					replyMessage.setText(txt);
				}

				replyMessage.setJMSReplyTo(null);

				// Set the batchID of the reply to match that of the incoming
				// message
				replyMessage.setIntProperty("batchID",
						message.getIntProperty("batchID"));
				replyMessage.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET,
						WMQConstants.CCSID_UTF8);

				// Send the reply
				replyProducer.send(replyMessage);

				// Close the JMS resources
				replyProducer.close();

			} catch (JMSException jmse) {
				// Error encountered
				jmse.printStackTrace();
				errorCount++;
			}
		}
		sess.close();
		conn.close();
		System.out.println("10 errors found - exiting this worker instance");
	}

	private String sendMsg(String message) throws JMSException,
			MalformedURLException {
		System.out.println("send message============" + message);
		String result = "";
		OperatorDB odb = new OperatorDB();
		Map<String, String> map = odb.getRequestMap();
		if (message.equals("approve")) {
			result = "Your request has been approved by IIC, please record this request ID: "
					+ map.get("id");
		} else if (message.equals("reject")) {
			result = "Your request has been rejected.";
		}
		try {
			SmsSender.sendSMS(map.get("mobilephone"), result);
		} catch (TwilioRestException e) {
			e.printStackTrace();
		}
		odb.updateTag(map.get("id"), message);
		odb.updateMainDoc();
		return result;
	}

	/**
	 * Parse the environment variables to get the ID string of this worker
	 * instance
	 */
	private String getID() {
		String envVCAPApplication = System.getenv("VCAP_APPLICATION");
		if (envVCAPApplication == null) {
			System.err
					.println("Worker:getID() --- ERROR the VCAP_APPLICATION environment variable has not been set");
			return null;
		}

		// Parse the env string into a JSONObject
		try {
			JSONObject jVCAP = JSONObject.parse(envVCAPApplication);

			// Get the array associated with the service.
			String instanceID = (String) jVCAP.get("instance_id");
			if (instanceID != null) {
				return instanceID;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("Worker:getID() --- ERROR VCAP_APPLICATION cannot be parsed");
			return null;
		}
	}

	@Override
	public void run() {
		try {
			startWorker();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
