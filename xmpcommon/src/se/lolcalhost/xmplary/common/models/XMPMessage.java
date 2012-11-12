package se.lolcalhost.xmplary.common.models;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class XMPMessage {
	public static enum MessageType {
		Hello,
		Alarm,
		DataDump,
		RegisterBackend,
		RemoveBackend,
		DebugText,
		DataPoints
	}
	
	private static final String ID = "id";
	@DatabaseField(id = true, canBeNull = false, columnName = ID)
	private int id;

	/**
	 * A JSON field of the contents of the message.
	 */
	public static final String CONTENTS = "contents";
	@DatabaseField(canBeNull = false, columnName = CONTENTS)
	private String contents;
	
	public static final String TYPE = "type";
	@DatabaseField(canBeNull = false, columnName = TYPE)
	private MessageType type;
	
	public static final String TARGET = "target";
	@DatabaseField(canBeNull = true, columnName = TARGET, foreign = true)
	private XMPNode target;
	
	public static final String FROM = "from";
	@DatabaseField(canBeNull = false, columnName = FROM, foreign = true)
	private XMPNode from;

	public static final String OUTGOING = "outgoing";
	@DatabaseField(canBeNull = false, columnName = OUTGOING)
	private boolean outgoing;
	
	public static final String DELIVERED = "delivered";
	@DatabaseField(canBeNull = false, columnName = DELIVERED)
	private boolean delivered;

	// TODO: is there a change between sent and acknowledged?
	
	public XMPMessage() {}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
	
	public void setContents(JSONObject contents) {
		this.contents = contents.toString();
	}
	
	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public XMPNode getTarget() {
		return target;
	}

	public void setTarget(XMPNode target) {
		this.target = target;
	}

	public XMPNode getFrom() {
		return from;
	}

	public void setFrom(XMPNode from) {
		this.from = from;
	}

	public boolean isOutgoing() {
		return outgoing;
	}

	public void setOutgoing(boolean outgoing) {
		this.outgoing = outgoing;
	}

	public boolean isDelivered() {
		return delivered;
	}

	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}

	public static XMPMessage parseFrom(Message message) throws JSONException {
		XMPMessage msg = new XMPMessage();
		msg.delivered = true;
		
		JSONObject contents2 = new JSONObject(message.getBody());
		msg.setContents(contents2.getString("contents"));
		msg.setType(MessageType.valueOf(contents2.getString("type")));
		return msg;
	}
	
	public String asJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("contents", contents);
		json.put("type", type.toString());
		return json.toString();
	}
}
