package se.lolcalhost.xmplary.common.models;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;

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
	
	protected static Logger logger = Logger.getLogger(XMPMessage.class);
	
	private static final String ID = "id";
	@DatabaseField(columnName = ID, generatedId = true)
	private int id;

	/**
	 * A JSON field of the contents of the message.
	 */
	public static final String CONTENTS = "contents";
	@DatabaseField(columnName = CONTENTS)
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

//	protected List<HashMap<DataPointField, Float>> dataPoints = new ArrayList<HashMap<DataPointField,Float>>();
	// TODO: is there a change between sent and acknowledged?
	
	public XMPMessage() {
		from = XMPNode.getSelf(); // default is to send from self.
	}

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

	/**
	 * Unpacks a message from from an XMPP message into an XMP message.
	 * Warning: this method has side effects; e.g. if the message is a DataPoint-message, it will create
	 * a bunch of XMPDataPoints in the database. 
	 * @param message
	 * @return
	 * @throws JSONException
	 */
	public static XMPMessage unpack(Message message) throws JSONException {
		XMPMessage msg = new XMPMessage();
		msg.delivered = true;
		
		JSONObject contents2 = new JSONObject(message.getBody());
		msg.setContents(contents2.getString("contents"));
		msg.setType(MessageType.valueOf(contents2.getString("type")));
		
		return msg;
	}
	
	public String asJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("contents", contents);

			json.put("type", type.toString());
			
			if (type == MessageType.DataPoints) {
				// add all related datapoints (as JSONObjects)
				JSONArray ja = new JSONArray();
				List<XMPDataPoint> queryForEq = XMPDb.DataPoints.queryForEq("message_id", this);
				for (XMPDataPoint xmpDataPoint : queryForEq) {
					JSONObject jo = new JSONObject();
					for (DataPointField field : DataPointField.values()) {
						jo.put(field.name(), xmpDataPoint.getContents().get(field));
					}
					ja.put(jo);
				}
				json.put("contents", ja.toString());
			}
		
		} catch (JSONException e) {
			logger.error("Unable to serialize xmp message: ", e);
		} catch (SQLException e) {
			logger.error("Unable to serialize xmp message: ", e);
		}
		return json.toString();
	}
}
