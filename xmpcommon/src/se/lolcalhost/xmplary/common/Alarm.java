package se.lolcalhost.xmplary.common;

import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.interfaces.JSONSerializable;

public class Alarm implements JSONSerializable {
	private String errorMessage = null;
	private AlarmTypes type;
	
	public enum AlarmTypes {
		OVERHEAT,
		FUEL_DRAINED,
		CHEESEBURGER_DROPPED,
		QUEUE_BIG,
		CASING_OPENED
	}
	
	public Alarm() {}

	public Alarm(AlarmTypes type, String errorMessage) {
		this.type = type;
		this.errorMessage = errorMessage;
	}
	
	@Override
	public void readObject(JSONObject stream) throws JSONException {
		errorMessage = stream.getString("message");
		type = AlarmTypes.valueOf(stream.getString("type"));
	}

	@Override
	public void writeObject(JSONObject stream) throws JSONException {
		stream.put("message", errorMessage == null ? "" : errorMessage);
		stream.put("type", type.name());
	}

	public AlarmTypes getType() {
		return type;
	}

	public void setType(AlarmTypes type) {
		this.type = type;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
