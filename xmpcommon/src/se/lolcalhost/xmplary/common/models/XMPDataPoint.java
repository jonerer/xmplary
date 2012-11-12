package se.lolcalhost.xmplary.common.models;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class XMPDataPoint {
	public static enum DataPointField {
		Temperature,
		Current,
		Resistance
	}
	
	private static final String ID = "id";
	@DatabaseField(columnName = ID, generatedId = true)
	private int id;

	/**
	 * A JSON field of the contents of the message.
	 */
	public static final String CONTENTS = "contents";
	@DatabaseField(canBeNull = false, columnName = CONTENTS, dataType = DataType.SERIALIZABLE)
	private HashMap<DataPointField, Float> contents = new HashMap<XMPDataPoint.DataPointField, Float>();
	
	public static final String FROM = "from";
	@DatabaseField(canBeNull = false, columnName = FROM, foreign = true)
	private XMPNode from;
	
	public static final String RECEIVED = "received";
	@DatabaseField(columnName = RECEIVED)
	private Date received;
	
	public static final String TIME = "time";
	@DatabaseField(canBeNull = false, columnName = TIME)
	private Date time;
	
	public static final String MESSAGE = "message_id";
	@DatabaseField(foreign = true)
	private XMPMessage message;
	
	public XMPDataPoint() {
		from = XMPNode.getSelf();
		time = new Date();
		received = new Date();
	}

//	public String getRawContents() {
//		return contents;
//	}
//
//	public void setContents(String contents) {
//		this.contents = contents;
//	}
//	
//	public void setContents(JSONObject contents) {
//		this.contents = contents.toString();
//	}
//	
//	public JSONObject getContents() {
//		try {
//			return new JSONObject(contents);
//		} catch (JSONException e) {
//			return new JSONObject();
//		}
//	}

	public XMPNode getFrom() {
		return from;
	}

	public void setFrom(XMPNode from) {
		this.from = from;
	}

	public HashMap<DataPointField, Float> getContents() {
		return contents;
	}

	public void setContents(HashMap<DataPointField, Float> contents) {
		this.contents = contents;
	}

	public XMPMessage getMessage() {
		return message;
	}

	public void setMessage(XMPMessage message) {
		this.message = message;
	}

}
