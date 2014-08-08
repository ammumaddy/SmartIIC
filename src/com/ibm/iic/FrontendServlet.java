// ******************************************************************
//
// Program name: MQLightSampleWeb
//
// Description:
//
// A http servlet that demonstrates use of the IBM Bluemix MQ Light Service.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.msg.client.wmq.WMQConstants;

/**
 * This file forms part of the MQ Light Sample Messaging Application - Worker offload pattern sample
 * provided to demonstrate the use of the IBM Bluemix MQ Light Service. It provides a simple
 * implementation of a HttpServlet to demonstrate the steps required to deploy a Web Application
 * with messaging resources. In response to a HTTP POST sent from the accompanying web page this
 * Servlet sends a request message to the 'back-end' service. The Servlet waits for a reply message
 * posted to the reply queue and then sends its contents as a JSON fragment in the HTTP response
 * back to the web page. The web page uses a small javascript function to parse the returned JSON
 * and update the result shown on the web page. This source file is packaged as part of the
 * MQLightSample/web.war and the 'back-end' service which it must be partnered with is packaged in
 * the MQLightSample/worker.jar file. Both applications should be uploaded to Bluemix and connected
 * to a Messaging Service. See the info-center for further details of how to use the sample.
 */
/**
 * Servlet implementation class FrontEndServlet
 */
@WebServlet("/FrontEndServlet")
public class FrontendServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final Queue myRequestQueue;
	private final Queue myReplyQueue;
	InitialContext ctx;
	private MQLightConnectionHelper connHelper = MQLightConnectionHelper
			.getMQLightConnectionHelper("mqlight");

	/**
	 * Default Constructor. Establishes a connection to the service, and sets up
	 * the request and the reply queues
	 * 
	 * @throws Exception
	 * @see HttpServlet#HttpServlet()
	 */
	public FrontendServlet() throws Exception {
		super();

		Connection connection = connHelper.getJmsConnectionFactory()
				.createConnection(connHelper.getUsername(),
						connHelper.getPassword());
		Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);
		connection.start();
		// Make sure queues are defined on the queue manager
		myRequestQueue = session.createQueue("jms/requestQueue");
		myReplyQueue = session.createQueue("jms/replyQueue");

		session.close();
		connection.close();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * Receive the incoming HTTP POST request from the doPost method, and check
	 * the reply queue for a response from the back-end worker processes. The
	 * reply is re-formatted into JSON and returned as the HTTP response. We use
	 * a selector supplied by the POST request to identify messages intended for
	 * this call of getReply()
	 */
	private void getReply(HttpServletRequest request,
			HttpServletResponse response, int messagesSent, String selector)
			throws IOException {
		// Initialise the error buffer used to collect error information to be
		// returned in the HTTP response
		StringBuffer errBuf = new StringBuffer();

		// Process the JMS reply messages
		ArrayList<String> replyMsgTxt = new ArrayList<String>();
		int messagesReceived = 0;
		if (messagesSent != 0) {
			try {
				// Create the JMS resources required to receive the reply
				// messages
				Connection connection = connHelper.getJmsConnectionFactory()
						.createConnection(connHelper.getUsername(),
								connHelper.getPassword());

				// Start the connection so messages can be received
				connection.start();

				Session session = connection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
				MessageConsumer consumer = session.createConsumer(myReplyQueue,
						selector);

				// Wait for the reply
				TextMessage replyMsg;
				while ((messagesReceived < messagesSent)
						&& ((replyMsg = (TextMessage) consumer.receive(2000)) != null)) {
					replyMsgTxt.add(new String(replyMsg.getText().getBytes(
							"UTF-8"), "UTF-8"));
					messagesReceived++;
				}

				// Clean-up the JMS resources
				consumer.close();
				session.close();
				connection.close();

			} catch (Exception e) {
				e.printStackTrace();
				errBuf.append(e.getMessage() + "::");
			}

			// Process the message received and convert it to JSON before
			// sending
			// back to the web page
			if (errBuf.length() != 0) {
				generateReply(response, "ERROR!", errBuf.toString());
			} else {
				if (messagesReceived == 0) {
					generateReply(response, "NO RESPONSE!", replyMsgTxt);
				} else {
					generateReply(response, "SUCCESS!", replyMsgTxt);
				}
			}
		}

	}

	/**
	 * Main entry point Receive the incoming HTTP POST request from the sample
	 * web page, send a request message to the back-end Enterprise Application.
	 * Implementation for HttpServlet#doPost(HttpServletRequest request,
	 * HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// Initialise the error buffer used to collect error information to be
		// returned in the HTTP response
		StringBuffer errBuf = new StringBuffer();

		// Counter to track number of messages sent
		int messagesSent = 0;

		// Generate a pseudo-random number to uniquely identify messages from
		// this
		// batch of messages
		Random rand = new Random();
		int ID = rand.nextInt();
		String msg = request.getParameter("message");
		System.out.println("msg===============" + msg);
		if (request.getQueryString().startsWith("message=")) {
			// Extract the 'message' parameter from the request message, this
			// contains the message to divide and send to the back end
			// applications
			// for processing
			String encodedRequest = request.getQueryString().substring(8);
			String message = URLDecoder.decode(encodedRequest, "UTF-8");
			// Split the 'message' into individual words to allow for parallel
			// processing at the back end
			System.out.println("message==" + message);
			String[] messages = message.split(" ");

			// Process the JMS request messages
			try {

				// Create the JMS resources required to send the request and
				// receive
				// the reply messages
				Connection connection = connHelper.getJmsConnectionFactory()
						.createConnection(connHelper.getUsername(),
								connHelper.getPassword());
				Session session = connection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
				MessageProducer producer = session
						.createProducer(myRequestQueue);
				producer.setTimeToLive(30000);

				// Start the connection so messages can be sent
				connection.start();
				for (String word : messages) {
					// Build the request message specifying the:
					// - word to process
					// - the reply destination to use
					TextMessage requestMsg = session.createTextMessage(word);
					requestMsg.setJMSReplyTo(myReplyQueue);
					requestMsg.setIntProperty("batchID", ID);
					requestMsg.setIntProperty(
							WMQConstants.JMS_IBM_CHARACTER_SET,
							WMQConstants.CCSID_UTF8);

					// Send the request message
					producer.send(requestMsg);
				}
				messagesSent = messages.length;

				// Clean-up the JMS resources
				producer.close();
				session.close();
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
				errBuf.append(e.getMessage() + "::");
			}
		} else {
			errBuf.append("Unknown message received :: "
					+ request.getQueryString());
		}

		// Check for error messages
		if (errBuf.length() != 0) {
			generateReply(response, "ERROR!", errBuf.toString());
		} else {
			getReply(request, response, messagesSent, "batchID = " + ID);
		}
	}

	/**
	 * Generate a JSON fragment in the HTTP response which includes the success
	 * status and the contents of the converted sentence message. A typical JSON
	 * fragment would be: { "state": "SUCCESS!", "message":
	 * "SOMETHING Processed by worker ID:ea02d386edf109611a3343d09c57431a" }
	 */
	private void generateReply(HttpServletResponse response, String state,
			String message) {

		try {
			PrintWriter out = response.getWriter();
			out.println("{ \"state\": \"" + state + "\",\"message\": \""
					+ message + "\"}");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Generate a JSON fragment in the HTTP response which includes the success
	 * status and the contents of the converted sentence message A typical JSON
	 * fragment would be: { "state": "SUCCESS!", "message100": { "message" :
	 * "HELLO" , "process" :
	 * "Processed by worker ID:ea02d386edf109611a3343d09c57431a" },
	 * "message101": { "message" : "WORLD" , "process" :
	 * "Processed by worker ID:4d5337ba89ee1085accd56e71001456b" }, }
	 */
	private void generateReply(HttpServletResponse response, String state,
			ArrayList<String> messages) {
		try {
			PrintWriter out = response.getWriter();
			out.println("{ \"state\": \"" + state + "\"");
			int i = 100;
			for (String msg : messages) {
				String[] splitMessages = msg.split("\t", 2);
				if (splitMessages.length == 2) {
					out.println(",\"message" + i + "\": { \"message\": \""
							+ URLEncoder.encode(splitMessages[0], "UTF-8")
							+ "\", " + "\"process\": \"" + splitMessages[1]
							+ "\"}	");
				} else {
					out.println(",\"message" + i + "\": { \"message\": \""
							+ splitMessages[0] + "\", " + "\"process\": \""
							+ "undefined" + "\"}");
				}
				i++;
			}
			out.println("}");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
