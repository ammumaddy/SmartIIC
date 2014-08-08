package com.cloudant;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.CouchDbDocument;

// Class to abstract the JSON document.
public class ConstantDocument extends CouchDbDocument {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3593057429690590906L;
	private String main_id;
	private String flag;
	
	@JsonProperty("main_id")
	public void setMainID(String mid) {
		main_id = mid;
	}

	@JsonProperty("main_id")
	public String getMainID() {
		return main_id;
	}
	
	@JsonProperty("flag")
	public String getFlag() {
		return flag;
	}

	@JsonProperty("flag")
	public void setFlag(String f) {
		this.flag = f;
	}
}
