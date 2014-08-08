package com.cloudant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.ibm.json.java.JSONObject;

public class HttpOperator {
	public void httpInvoke(JSONObject jo) throws MalformedURLException {
		String url = "http://smartiic.mybluemix.net/FrontendServlet";
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair("message", jo.toString()));
		System.out.println(post(url, "UTF-8", "UTF-8", list));
	}

	/**
	 * 发送 post请求访问本地应用并根据传递参数不同返回不同结果
	 */
	public String post(String url, String respEncoding) {
		return post(url, "UTF-8", respEncoding, new ArrayList<NameValuePair>());
	}

	/**
	 * 发送 post请求访问本地应用并根据传递参数不同返回不同结果
	 */
	public String post(String url, String reqEncoding, String respEncoding,
			List<NameValuePair> param) {
		HttpClient hclient = new DefaultHttpClient();
		String resStr = "";
		HttpPost httppost = new HttpPost(url);
		List<? extends NameValuePair> formparams = param;
		UrlEncodedFormEntity uefEntity;
		try {
			uefEntity = new UrlEncodedFormEntity(formparams, reqEncoding);
			httppost.setEntity(uefEntity);
			HttpResponse response;
			response = hclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				resStr = EntityUtils.toString(entity, respEncoding);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			hclient.getConnectionManager().shutdown();
		}
		return resStr;
	}
}
