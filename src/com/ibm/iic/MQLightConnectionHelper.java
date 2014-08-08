// ******************************************************************
//
// Program name: MQLightConnectionHelper
//
// Description:
//
// A class that interprets the Cloud Foundry VCAP_SERVICES environment
// variable and provides a ConnectionFactory object to the target
// messaging service bound to the invoking application
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

import javax.jms.JMSException;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsConstants;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.common.CommonConstants;

/**
 * A class that interprets the Cloud Foundry VCAP_SERVICES environment variable and provides a
 * ConnectionFactory object to the target messaging service bound to the invoking application
 */
public class MQLightConnectionHelper {

    private static MQLightConnectionHelper mqlCH = new MQLightConnectionHelper();

    private String serviceName = null;
    private MQLightServiceDetails serviceDetails = null;

    /**
     * Private Constructor
     */
    private MQLightConnectionHelper() {}

    /**
     * Get the singleton instance of the MQ Light connection helper
     * 
     * @return the singleton instance
     */
    public static MQLightConnectionHelper getMQLightConnectionHelper() {
        // If no service name is specified, default to mqlight
        return getMQLightConnectionHelper("mqlight");
    }

    /**
     * Get the singleton instance of the MQ Light connection helper
     * 
     * @param service
     *            the service name
     * @return the singleton instance
     */
    public static MQLightConnectionHelper getMQLightConnectionHelper(final String service) {
        // Set the service name and then parse the VCAP_SERVICES for information about the queue
        // manager bound to the service.
        mqlCH.setServiceName(service);
        mqlCH.parseVCAPServices();
        return mqlCH;
    }

    /**
     * Obtain a Jms Connection Factory based on values in the VCAP_SERVICES environment variable.
     * 
     * @return a Jms Connection Factory
     * @throws Exception
     */
    public JmsConnectionFactory getJmsConnectionFactory() throws Exception {
        final JmsConnectionFactory jmsCF = buildJmsConnectionFactory();
        return jmsCF;
    }

    /**
     * Create a JmsConnectionFactory
     * 
     * @return the created connection factory
     */
    private JmsConnectionFactory buildJmsConnectionFactory() throws JMSException {

        displayConnectionDetails();

        final JmsFactoryFactory ff = JmsFactoryFactory.getInstance(JmsConstants.WMQ_PROVIDER);
        final JmsConnectionFactory jmsCF = ff.createConnectionFactory();
        jmsCF.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE, CommonConstants.WMQ_CM_CLIENT);

        jmsCF.setStringProperty(CommonConstants.WMQ_CCDTURL, serviceDetails.getConnectionLookupURI() + "&format=CCDT");
        jmsCF.setIntProperty(CommonConstants.WMQ_CLIENT_RECONNECT_OPTIONS, CommonConstants.WMQ_CLIENT_RECONNECT_Q_MGR);
        return jmsCF;
    }

    /**
     * Obtain the JSONObject containing the VCAPServices details for connecting to the service
     * instance.
     */
    private void parseVCAPServices() {

        // Get the contents of the environment variable.
        final String envVCAPServices = System.getenv("VCAP_SERVICES");
        if (envVCAPServices == null) {
            System.err.println("MQLightConnectionHelper:parseVCAPServices() --- ERROR the VCAP_SERVICES environment variable has not been set");
            return;
        }

        // Parse the env string into a JSONObject
        try {
            final JSONObject jVCAP = JSONObject.parse(envVCAPServices);

            // Get the array associated with the service.
            final JSONArray aService = (JSONArray) jVCAP.get(serviceName);

            // Check if any service has been found
            if (aService == null) {
                throw new Exception(
                    "MQLightConnectionHelper:parseVCAPServices() --- ERROR MQ Connection details could not be found. Check the application is bound to an instance of an MQ Light service.");
            }

            // Get the credentials of the service

            // We're only handling the case of a single bound service here - to extend this, loop
            // over all aService and handle appropriately
            final JSONObject jWSMQS = (JSONObject) aService.get(0);
            // With the WebSphere MQ Service JSON Object, now extract the credentials object
            final JSONObject jCred = (JSONObject) jWSMQS.get("credentials");
            if (jCred == null) {
                System.err.println("MQLightConnectionHelper:parseVCAPServices() --- ERROR JSONArray has no MQ Light service credentials");
                return;
            }

            // Use the setters, to populate the information from the JSONObject into the class
            // variables to be used in connection.
            serviceDetails = new MQLightServiceDetails();

            // Service Id
            serviceDetails.setServiceId((String) jWSMQS.get("name"));

            // Security credentials
            serviceDetails.setUsername((String) jCred.get("username"));
            serviceDetails.setPassword((String) jCred.get("password"));

            // Connection Lookup URI
            serviceDetails.setConnectionLookupURI((String) jCred.get("connectionLookupURI"));

            // Return the JSON credentials object for the bound WebSphere MQ Service
            return;
        }
        catch (final Exception e) {
            e.printStackTrace();
            System.err.println("MQLightConnectionHelper:parseVCAPServices() --- ERROR VCAP_SERVICES cannot be parsed");
            return;
        }
    }

    /**
     * Print out the connection details used to connect to the service
     */
    public void displayConnectionDetails() {
        System.out.println("Creating a connection with the following attributes...");
        System.out.println("ServiceID     --- " + serviceDetails.getServiceId());
        System.out.println("ConnectionURI --- " + serviceDetails.getConnectionLookupURI());
    }

    /**
     * Get the name of the service
     * 
     * @return the name of the service
     */
    public String getServiceName() {
        return serviceName;
    }

    private void setServiceName(final String service) {
        serviceName = service;
    }

    /**
     * Get the of {@link MQLightServiceDetails} associated with this helper
     * 
     * @return service details
     */
    public MQLightServiceDetails getServicesDetails() {
        return serviceDetails;
    }

    /**
     * Get the username associated with this service
     * 
     * @return username
     */
    public String getUsername() {
        return serviceDetails.getUsername();
    }

    /**
     * Get the password associated with this service
     * 
     * @return password
     */
    public String getPassword() {
        return serviceDetails.getPassword();
    }

    /**
     * Get the connection lookup uri associated with this service
     * 
     * @return connectionLookupURI
     */
    public String getConnectionLookupURI() {
        return serviceDetails.getConnectionLookupURI();
    }

    /**
     * Inner class used to store the details required to connect to a service
     */
    public class MQLightServiceDetails {

        private String serviceId = null;
        private String username = null;
        private String password = null;
        private String connectionLookupURI = null;

        /**
         * Get the unique name of the bound service instance
         * 
         * @return serviceId
         */
        public String getServiceId() {
            return serviceId;
        }

        void setServiceId(final String serviceId) {
            this.serviceId = serviceId;
        }

        /**
         * Get the username required for qmgr authentication
         * 
         * @return username
         */
        public String getUsername() {
            return username;
        }

        void setUsername(final String username) {
            this.username = username;
        }

        /**
         * Get the password required for qmgr authentication
         * 
         * @return password
         */
        public String getPassword() {
            return password;
        }

        void setPassword(final String password) {
            this.password = password;
        }

        /**
         * Get the uri of the client connection definition table used to connect to the qmgr
         * 
         * @return connectionLookupURI
         */
        public String getConnectionLookupURI() {
            return connectionLookupURI;
        }

        void setConnectionLookupURI(final String uri) {
            connectionLookupURI = uri;
        }
    }
}
