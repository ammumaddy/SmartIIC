package com.cloudant;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

public class OperatorDB {
	final static String sid = "iicsupport_constant";
	final static String user = "cf163141-1e0d-4556-aa47-caa4fe5b4ce7-bluemix";
	final static String pass = "02c6db608127ceb94f3320e9c164fc90e2c20559d79da71236c9c9eedbb7f294";
	final static String db = "iicsupport";
	final static String hosturl = "smartiic.mybluemix.net/StartServlet?message=";
	private CrudWithEktorp cwe = null;
	private CouchDbConnector dbc = null;

	public OperatorDB() {
		try {
			dbc = getCouchDBConnector();
			cwe = new CrudWithEktorp();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public Map<String, String> getRequestMap() throws MalformedURLException {
		System.out.println("begin");
		Map<String, String> map = new HashMap<String, String>();
		String did = getMID();
		if (did.equals("")) {
			System.out.println("false");
		} else {
			CrudRepository cr = new CrudRepository(dbc);
			CrudDocument doc = cwe.getAllDoc(cr, did);
			System.out.println(doc.getName());
			System.out.println(doc.getMobilephone());
			System.out.println(doc.getServer());
			System.out.println(doc.getStorage());
			System.out.println(doc.getUsage());
			System.out.println(doc.getTimedata());
			System.out.println(doc.getTag());
			map.put("id", doc.getId());
			map.put("name", doc.getName());
			map.put("mobilephone", doc.getMobilephone());
			map.put("server", doc.getServer());
			map.put("storage", doc.getStorage());
			map.put("usage", doc.getUsage());
			map.put("timedata", doc.getTimedata());
			map.put("tag", doc.getTag());
		}
		return map;
	}

	public CouchDbConnector getCouchDBConnector() throws MalformedURLException {
		// create the http connection
		HttpClient httpClient;
		httpClient = new StdHttpClient.Builder()
				.url("https://" + user + ".cloudant.com").username(user)
				.password(pass).build();
		// initialize couch instance and connector
		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
		CouchDbConnector dbc = new StdCouchDbConnector(db, dbInstance);
		return dbc;
	}

	public String getMID() throws MalformedURLException {
		MainRepository repo = null;
		repo = new MainRepository(dbc);
		String did = cwe.readMainDoc(repo, sid);
		return did;
	}
	
	public void updateTag(String uid, String tag) throws MalformedURLException {
		CrudRepository repo = null;
		repo = new CrudRepository(dbc);
		cwe.updateDoc(repo, uid, tag);
	}
	
	public void updateMainDoc() throws MalformedURLException {
		MainRepository repo = null;
		repo = new MainRepository(dbc);
		cwe.updateMainDoc(repo);
	}
}
