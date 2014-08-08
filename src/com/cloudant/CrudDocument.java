package com.cloudant;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.CouchDbDocument;

// Class to abstract the JSON document.
public class CrudDocument extends CouchDbDocument {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3593057429690590906L;
	private String name;
	private String mobilephone;
	private String server;
	private String storage;
	private String usage;
	private String timedata;
	private String tag;
	
	@JsonProperty("name")
	public void setName(String n) {
		name = n;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}
	
	@JsonProperty("mobilephone")
	public String getMobilephone() {
		return mobilephone;
	}

	@JsonProperty("mobilephone")
	public void setMobilephone(String mobilephone) {
		this.mobilephone = mobilephone;
	}

	@JsonProperty("server")
	public String getServer() {
		return server;
	}

	@JsonProperty("server")
	public void setServer(String server) {
		this.server = server;
	}

	@JsonProperty("storage")
	public String getStorage() {
		return storage;
	}

	@JsonProperty("storage")
	public void setStorage(String storage) {
		this.storage = storage;
	}

	@JsonProperty("usage")
	public String getUsage() {
		return usage;
	}

	@JsonProperty("usage")
	public void setUsage(String usage) {
		this.usage = usage;
	}

	@JsonProperty("timedata")
	public String getTimedata() {
		return timedata;
	}

	@JsonProperty("timedata")
	public void setTimedata(String timedata) {
		this.timedata = timedata;
	}

	@JsonProperty("tag")
	public String getTag() {
		return tag;
	}

	@JsonProperty("tag")
	public void setTag(String tag) {
		this.tag = tag;
	}

}
