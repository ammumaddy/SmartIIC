With a little bit of code, You will be able to create a simple approval tool for IBM Innovation Center, this tool provide our partners an easy way to submit their request to us in their mobile devices and we can also use it manage all the request and send response to applicant in time.
You will create an Android app for submit request IIC¡¯s facilities and support, the request data will be stored in a Cloudant DB, you can also develop a web-based application to get the request from DB, then put the request data to the MQ, in the meantime, the Java application also have a message engine for sending SMS to the applicant, then the application get data from reply queue. To do it yourself, follow these steps.

1.Find an Android smart phone or a tablet

2.Enable the "Unknown Source" in your device for allowing Android to install the unknown apps.

3.Install "IICRequestForm.apk" in the zip folder and run this app.

4.Input the information, make sure add your country code such as "+1" before your mobile phone number.

5.Back to your laptop, open a browser link to: smartiic.mybluemix.net

6.Review the information you have requested, submit your opinion. 

7.You can receive a result via SMS.

Note: As the default Twilio account is trial, you can't send SMS to the unverified numbers. So if you want to receive SMS, you should verify the new phone number. Or you can view my article to follow my whole tutorial, you can send me a mail for request the tutorial: huleihl@cn.ibm.com