package se.lolcalhost.xmplary.common.interfaces;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONSerializable {
	public void readObject(JSONObject stream) throws JSONException, SQLException;
	public void writeObject(JSONObject stream) throws JSONException;
}
