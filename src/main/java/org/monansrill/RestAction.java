package org.monansrill;

import com.fasterxml.jackson.databind.JsonNode;

public class RestAction {

	private String method;
	private String id;
	private JsonNode msg;
	public RestAction(String method, String id, JsonNode msg) {
		this.method = method;
		this.id = id;
		this.msg = msg;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public JsonNode getMsg() {
		return msg;
	}
	public void setMsg(JsonNode msg) {
		this.msg = msg;
	}
	

	
}
