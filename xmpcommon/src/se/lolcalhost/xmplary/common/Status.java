package se.lolcalhost.xmplary.common;

import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.interfaces.JSONSerializable;

public class Status implements JSONSerializable {
	private String statusMessage = null;
	private WelderStatus status;
	
	public enum WelderStatus {
		RUNNING,
		REFUELING,
		COOLINGDOWN,
		STOPPED, 
		AWAIT_REFUEL
	}
	
	public Status() {}

	public Status(WelderStatus type, String statusMessage) {
		this.status = type;
		this.statusMessage = statusMessage;
	}
	
	@Override
	public void readObject(JSONObject stream) throws JSONException {
		statusMessage = stream.getString("message");
		status = WelderStatus.valueOf(stream.getString("status"));
	}

	@Override
	public void writeObject(JSONObject stream) throws JSONException {
		stream.put("message", statusMessage == null ? "" : statusMessage);
		stream.put("status", status.name());
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public WelderStatus getStatus() {
		return status;
	}

	public void setStatus(WelderStatus status) {
		this.status = status;
	}

}
