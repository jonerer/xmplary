package se.lolcalhost.xmplary.common.models;

import java.util.Date;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class XMPDataPoint {
	public enum DataPointField {
		Temperature,
		Current,
		Resistance
	}
	private static final String ID = "id";
	@DatabaseField(id = true, canBeNull = false, columnName = ID)
	private int id;

	/**
	 * A JSON field of the contents of the message.
	 */
	public static final String CONTENTS = "contents";
	@DatabaseField(canBeNull = false, columnName = CONTENTS, dataType = DataType.STRING)
	private String contents;
	
	public static final String FROM = "from";
	@DatabaseField(canBeNull = false, columnName = FROM, foreign = true)
	private XMPNode from;
	
	public static final String RECEIVED = "received";
	@DatabaseField(canBeNull = false, columnName = RECEIVED)
	private Date received;
	
	public static final String TIME = "time";
	@DatabaseField(canBeNull = false, columnName = TIME)
	private Date time;
	
	
	public XMPDataPoint() {}

	public String getRawContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
	
	public void setContents(JSONObject contents) {
		this.contents = contents.toString();
	}
	
	public JSONObject getContents() {
		try {
			return new JSONObject(contents);
		} catch (JSONException e) {
			return new JSONObject();
		}
	}

	public XMPNode getFrom() {
		return from;
	}

	public void setFrom(XMPNode from) {
		this.from = from;
	}

}
