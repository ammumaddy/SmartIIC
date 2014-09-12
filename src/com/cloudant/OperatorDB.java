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

import com.ibm.iic.FileReader;

public class OperatorDB {
	final static String sid = "iicsupport_constant";
	final static String db = "iicsupport";

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
		FileReader fr = new FileReader();
		HashMap<String, String> map = fr.getCloudantMap();
		String user = map.get("user");
		String pass = map.get("pass");
		
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
