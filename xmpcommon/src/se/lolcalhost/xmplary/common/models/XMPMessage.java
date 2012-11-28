package se.lolcalhost.xmplary.common.models;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.PublicKey;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPCrypt;
import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.MessageDispatchCommand;
import se.lolcalhost.xmplary.common.interfaces.JSONSerializable;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class XMPMessage implements JSONSerializable {
	public static enum MessageType {
		Alarm,

		IsRegistered, Register, Unregister, RegistrationRequest,

		DebugText, DataPoints, RequestDataPoints,

		Raw
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

	public static final String ORIGINAL_ID = "original_id";
	@DatabaseField(columnName = ORIGINAL_ID)
	private int originalId;

	public static final String RESPONSE_TO_ID = "response_to_id";
	@DatabaseField(canBeNull = true, columnName = RESPONSE_TO_ID)
	private int responseToId;

	public static final String RESPONSE_TO_NODE = "response_to_node_id";
	@DatabaseField(canBeNull = true, columnName = RESPONSE_TO_NODE, foreign = true)
	private XMPNode responseToNode;

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
	private String signature;

	public static final String VERIFIED = "verified";
	@DatabaseField(canBeNull = false, columnName = VERIFIED)
	private boolean verified;

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
			// msg.setTarget(XMPNode.getSelf());

			logger.trace("Finding origin node.");

			XMPNode from = XMPNode.getByJID(message.getFrom());
			if (from == null) {
				logger.info("Unknown sender. Creating new database record.");
				from = XMPNode.createByJID(message.getFrom());
			} else {
				logger.trace("Sender node identified.");
			}			
			msg.setFrom(from);

			if (from.equals(XMPNode.getSelf())) {
				logger.trace("Sender was self. ignoring message.");
				return null;
			}

			logger.trace("Parsing successful.");

			JSONObject jo;
			jo = new JSONObject(message.getBody());
			msg.readObject(jo);


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

	public Object getContents() throws JSONException {
		try {
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
			} else if (type == MessageType.Register) {
				StringReader fr = new StringReader(contents);
				PEMReader r = new PEMReader(fr);
				Object o;
				o = r.readObject();
				X509CertificateObject cert = null;
				if (o instanceof X509CertificateObject) {
					cert = (X509CertificateObject) o;
				}
				r.close();
				return cert;
			} else {
				return contents;
			}
		} catch (IOException e) {
			logger.error("Error in content unpacking: ", e);
		}
		return null;
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
			} else if (contents instanceof X509CertificateObject) {
				X509CertificateObject obj = (X509CertificateObject) contents;
				StringWriter sw = new StringWriter();
				PEMWriter wr = new PEMWriter(sw);
				wr.writeObject(obj);
				wr.close();
				this.contents = sw.toString();
			}
		} catch (JSONException e) {
			throw new IllegalArgumentException();
		} catch (IOException e) {
			throw new IllegalArgumentException();
		}
	}

	public XMPMessage createResponse() {
		XMPMessage m = new XMPMessage(type);
		m.setTarget(from);
		m.setOutgoing(true);
		m.setResponseToId(getOriginalId());
		m.setResponseToNode(getOrigin());
		try {
			m.setContents(getContents());
		} catch (JSONException e) {
			logger.error("Couldn't unpack contents of message to respond to", e);
		}
		return m;
	}

	public String serialized() {
		if (type != MessageType.Raw) {
			JSONObject json = new JSONObject();
			writeObject(json);
			return json.toString();
		} else {
			return contents;
		}
	}

	public void writeObject(JSONObject stream) {
		try {
			stream.put("contents", contents);

			stream.put("target", target.getName());

			stream.put("type", type.toString());

			stream.put("original", getOriginalId());

			stream.put("origin", origin.getName());

			stream.put("signature", getSignature());

			stream.put("response_to_id", responseToId);

			if (responseToId != 0) {
				stream.put("response_to_node", responseToNode.getName());
			}

		} catch (JSONException e) {
			logger.error("Unable to serialize xmp message: ", e);
		}
	}

	public void readObject(JSONObject stream) throws JSONException {
		type = MessageType.valueOf(stream.getString("type"));
		target = XMPNode.getByJID(stream.getString("target"));
		originalId = stream.getInt("original");

		responseToId = stream.getInt("response_to_id");
		if (responseToId != 0) {
			responseToNode = XMPNode.getOrCreateByJID(stream
					.getString("response_to_node"));
		}
		if (stream.has("contents")) {
			contents = stream.getString("contents");
		}

		String string = stream.getString("origin");
		origin = XMPNode.getByJID(string);
		if (origin == null) {
			origin = XMPNode.createByJID(string);
		}
		signature = stream.getString("signature");
	}

	public void sign() {
		setSignature("sign_of_" + id + " by " + from.getName());
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
		MessageDispatchCommand cmd = new MessageDispatchCommand(main, this);
		cmd.schedule();
	}

	public static void setMain(XMPMain main) {
		XMPMessage.main = main;
	}

	public static void tellOperator(String text) {
		XMPNode operator = XMPNode.getOperator();
		if (operator != null) {
			XMPMessage msg = new XMPMessage(MessageType.Raw);
			msg.setTarget(operator);
			msg.setContents(text);
			msg.send();
		}
	}

	public int getOriginalId() {
		if (originalId == 0) {
			return id;
		}
		return originalId;
	}

	public void setOriginalId(int originalId) {
		this.originalId = originalId;
	}

	/**
	 * Who gets to send to whom?
	 * 
	 * Backend can send to operator. Gateway can send to everyone.
	 * 
	 * Everyone can send to chatroom.
	 * 
	 * The rest is sent to gateway.
	 * 
	 * Also, dont expect others to follow this pattern. Signatures must be
	 * traced to origin node.
	 * 
	 * @return
	 */
	public XMPNode getNextRoutingNode() {
		if (XMPNode.getSelf().getType() == NodeType.backend
				&& target.getType() == NodeType.operator) {
			return target;
		} else if (XMPNode.getSelf().equals(XMPNode.getGateway())) {
			return target;
		} else if (target.getType() == NodeType.chatroom) {
			return target;
		} else {
			return XMPNode.getGateway();
		}
	}

	/**
	 * Return the signature. Or, if one doesn't exist and this is from self,
	 * sign it.
	 * 
	 * @return
	 */
	public String getSignature() {
		if (signature == null && from.equals(XMPNode.getSelf())) {
			String conts = contents == null ? "" : contents;
			return XMPCrypt.sign(conts + origin.getName() + getOriginalId());
		}
		return signature;
	}

	/**
	 * Try to verify this message with the stored cert.
	 */
	public boolean verify() {
		if (origin.getCert() == null) {
			verified = false;
			return false;
		} else {
			return verify(origin.getCert().getPublicKey());
		}
	}

	/**
	 * Try to verify this message against a given cert.
	 */
	public boolean verify(PublicKey key) {
		// TODO: should this really have side effects?
		String conts = contents == null ? "" : contents;
		verified = XMPCrypt.verify(
				conts + origin.getName() + getOriginalId(), key, signature);
		return verified;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public int getResponseToId() {
		return responseToId;
	}

	public void setResponseToId(int responseToId) {
		this.responseToId = responseToId;
	}

	public XMPNode getResponseToNode() {
		return responseToNode;
	}

	public void setResponseToNode(XMPNode responseToNode) {
		this.responseToNode = responseToNode;
	}

	public boolean isVerified() {
		return verified;
	}

	public boolean isResponse() {
		return responseToId != 0;
	}

}
