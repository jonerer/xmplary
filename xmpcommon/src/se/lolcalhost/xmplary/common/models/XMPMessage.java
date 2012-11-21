package se.lolcalhost.xmplary.common.models;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.interfaces.JSONSerializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class XMPMessage implements JSONSerializable {
	public static enum MessageType {
		Alarm,

		IsRegistered, Register, Unregister, RegistrationRequest,

		DebugText, DataPoints, RequestDataPoints
	}
	
	public static MessageType[] MulticastTypes = { MessageType.Alarm };

	protected static Logger logger = Logger.getLogger(XMPMessage.class);

	public static final String ID = "id";
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
	
	public static final String ORIGIN = "origin";
	@DatabaseField(canBeNull = false, columnName = ORIGIN, foreign = true)
	private XMPNode origin;

	public static final String OUTGOING = "outgoing";
	@DatabaseField(canBeNull = false, columnName = OUTGOING)
	private boolean outgoing;

	public static final String DELIVERED = "delivered";
	@DatabaseField(canBeNull = false, columnName = DELIVERED)
	private boolean delivered;
	
	public static final String SIGNATURE = "signature";
	@DatabaseField(canBeNull = true, columnName = SIGNATURE)
	private String signature = "";

	private static XMPMain main;
	
	// protected List<HashMap<DataPointField, Float>> dataPoints = new
	// ArrayList<HashMap<DataPointField,Float>>();
	// TODO: is there a change between sent and acknowledged?

	public XMPMessage() {
		from = XMPNode.getSelf(); // default is to send from self.
		target = XMPNode.getGateway(); // default is to send to gateway.
		origin = XMPNode.getSelf();
	}

	public XMPMessage(MessageType mt) {
		this();
		type = mt;
	}

	public String getRawContents() {
		return contents;
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
	 * 
	 * @param message
	 * @return
	 * @throws JSONException
	 * @throws SQLException
	 */
	public static XMPMessage unpack(Message message) {
		XMPMessage msg = new XMPMessage();
		try {
			msg.setOutgoing(false);
			msg.setDelivered(true);
			msg.setTarget(XMPNode.getSelf());
			
			JSONObject jo;
			jo = new JSONObject(message.getBody());
			msg.readObject(jo);
			
			logger.trace("Parse successful. Finding origin node.");

			XMPNode from = XMPNode.getByJID(message.getFrom());
			if (from == null) {
				logger.info("Unknown sender. Creating new database record.");
				from = XMPNode.createByJID(message.getFrom());
			} else {
				logger.trace("Sender node identified.");
			}

			msg.setFrom(from);
			if (jo.has("contents")) {
				msg.setContents(jo.get("contents"));
			}
			
		} catch (JSONException e) {
			logger.warn("Unable to unpack message into XMPMessage. Body: "
					+ message.getBody());
			msg = null;
		}
		return msg;
	}

	public XMPMessage createResponse() {
		return createResponse(null);
	}

	public Object getContents() throws JSONException {
		if (type == MessageType.DataPoints) {
			List<XMPDataPoint> res = new ArrayList<XMPDataPoint>();
			JSONArray ja = new JSONArray(contents);
			for (int i = 0; i < ja.length(); i++) {
				JSONObject jo = ja.getJSONObject(i);
				XMPDataPoint p = new XMPDataPoint();
				p.readObject(jo);
				p.setReceived(new Date());
				res.add(p);
			}
			return res;
		} else {
			return contents;
		}
	}

	public void setContents(Object contents) {
		try {
			if (contents instanceof JSONObject) {
				this.contents = ((JSONObject) contents).toString();
			} else if (contents instanceof String) {
				this.contents = (String) contents;
			} else if (contents instanceof List<?>) {
				JSONArray ar = new JSONArray();
				for (Object o : (List<?>) contents) {
					JSONObject jo = new JSONObject();
					((JSONSerializable) o).writeObject(jo);
					ar.put(jo);
				}
				this.contents = ar.toString();
			}
		} catch (JSONException e) {
			throw new IllegalArgumentException();
		}
	}

	private XMPMessage createResponse(JSONObject object) {
		XMPMessage m = new XMPMessage(type);
		m.setTarget(from);
		m.setOutgoing(true);
		m.setContents(object);
		return m;
	}

	public String asJSON() {
		JSONObject json = new JSONObject();
		writeObject(json);
		return json.toString();
	}

	public void writeObject(JSONObject stream) {
		try {
			stream.put("contents", contents);

			stream.put("type", type.toString());
			
			stream.put("origin", origin.getName());
			
			stream.put("signature", signature);

		} catch (JSONException e) {
			logger.error("Unable to serialize xmp message: ", e);
		} 
	}

	public void readObject(JSONObject stream) throws JSONException {
		type = MessageType.valueOf(stream.getString("type"));
		String string = stream.getString("origin");
		origin = XMPNode.getByJID(string);
		if (origin == null) {
			origin = XMPNode.createByJID(string);
		}
		signature = stream.getString("signature");
	}
	
	public void sign() {
		signature = "sign_of_" + id + " by " + from.getName();
	}
	
	public JSONObject asJSONObject() {
		JSONObject json = new JSONObject();
		writeObject(json);
		return json;
	}

	public int getId() {
		return id;
	}

	public void save() {
		try {
			XMPDb.Messages.createOrUpdate(this);
		} catch (SQLException e) {
			logger.error("Couldn't save xmp message into DB.", e);
		}
	}
	
	public boolean isMulticast() {
		boolean isMulticast = false;
		for (int i = 0; i < MulticastTypes.length; i++) {
			MessageType typez = MulticastTypes[i];
			if (type == typez) {
				isMulticast = true;
			}
		}
		return isMulticast;
	}

	public XMPNode getOrigin() {
		return origin;
	}

	public void setOrigin(XMPNode origin) {
		this.origin = origin;
	}

	public static List<XMPMessage> getAlarms() {
		try {
			return XMPDb.Messages.queryForEq(TYPE, MessageType.Alarm);
		} catch (SQLException e) {
			logger.error("Unable to get alarm list: " + e);
		}
		return new ArrayList<XMPMessage>();
	}

	public void forwardTo(XMPNode destination) {
		XMPMessage msg = new XMPMessage(type);
		msg.setOrigin(origin);
		msg.setContents(contents);
		msg.setTarget(destination);
		msg.save();
		msg.send();
	}
	
	public void send() {
		main.dispatch(this);
	}

	public static void setMain(XMPMain main) {
		XMPMessage.main = main;
	}

}
