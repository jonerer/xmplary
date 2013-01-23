package se.lolcalhost.xmplary.common.models;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPConfig;
import se.lolcalhost.xmplary.common.XMPCrypt;
import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.interfaces.JSONSerializable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class XMPNode implements JSONSerializable {
	static Logger logger = Logger.getLogger(XMPNode.class);

	public enum NodeType {
		gateway, leaf, backend, operator, chatroom, unknown
	}

	private static final String ID = "id";
	@DatabaseField(columnName = ID, generatedId = true)
	private int id;

	public static final String NAME = "name";
	@DatabaseField(canBeNull = false, columnName = NAME)
	private String name;

	public static final String REGISTERED = "registered";
	@DatabaseField(canBeNull = false, columnName = REGISTERED)
	private boolean registered = false;

	public static final String TYPE = "type";
	@DatabaseField(canBeNull = false, columnName = TYPE)
	private NodeType type;

	public static final String CERT = "cert";
	@DatabaseField(canBeNull = true, columnName = CERT, dataType = DataType.LONG_STRING)
	private String cert;

	/**
	 * public key. maybe use a cert library and save certs to disk instead.
	 */
	// @DatabaseField(canBeNull = false)
	// private byte[] publicKey;

	/**
	 * the unique ID. maybe use a cert library and save certs to disk instead.
	 */
	// @DatabaseField(canBeNull = false)
	// private byte[] uuid;

	/**
	 * has a shared Secret been negotiated? dont save it in the database. make
	 * sure to make a new one for the next session.
	 */
	public String sharedSecret;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJID() {
		if (type != NodeType.chatroom) {
			return name + "@" + XMPConfig.Address() + "/Smack"; // apparently it's not .Domain() here. dunno why. differs on openfire.
		} else {
			return name + "@" + XMPConfig.RoomDomain() + "";

		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private static XMPNode gateway = null;

	public static XMPNode getGateway() throws SQLException {
		if (gateway == null) {
			if (XMPConfig.Type() == NodeType.gateway) {
				gateway = getSelf();
				return getSelf();
			}
			List<XMPNode> queryForEq = XMPDb.Nodes.queryForEq(TYPE,
					NodeType.gateway);
			if (queryForEq.isEmpty()) {
				logger.info("Self not found in database. Creating...");
				gateway = new XMPNode();
				gateway.setType(NodeType.gateway);
				gateway.setName("gateway");
			} else {
				gateway = queryForEq.get(0);
			}
			XMPDb.Nodes.createIfNotExists(gateway);
		}
		return gateway;
	}

	private static XMPNode self = null;

	public static XMPNode getSelf() throws SQLException {
		if (self == null) {
			List<XMPNode> queryForEq = XMPDb.Nodes.queryForEq(NAME,
					XMPConfig.Name());
			if (queryForEq.isEmpty()) {
				logger.info("Self not found in database. Creating...");
				self = new XMPNode();
				self.setCert(XMPCrypt.getCertificate());
				self.setName(XMPConfig.Name());
				self.setType(XMPConfig.Type());
				self.setRegistered(true);
			} else {
				self = queryForEq.get(0);
			}
			XMPDb.Nodes.createIfNotExists(self);
		}
		return self;
	}

	private static XMPNode operator = null;

	public static XMPNode getOperator() throws SQLException {
		if (operator == null) {
			List<XMPNode> queryForEq = XMPDb.Nodes.queryForEq(TYPE,
					NodeType.operator);
			if (queryForEq.size() != 0) {
				operator = queryForEq.get(0);
			}
		}
		return operator;
	}

	private static XMPNode room = null;

	public static XMPNode getRoom() throws SQLException {
		if (room == null) {
			List<XMPNode> queryForEq = XMPDb.Nodes.queryForEq(NAME,
					XMPConfig.Room());
			if (queryForEq.isEmpty()) {
				logger.info("Self not found in database. Creating...");
				room = new XMPNode();
				room.setName(XMPConfig.Room());
				room.setType(NodeType.chatroom);
				room.save();
			} else {
				room = queryForEq.get(0);
			}
		}
		return room;
	}

	public void save() throws SQLException {
		XMPDb.Nodes.createOrUpdate(this);
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public static XMPNode getByJID(String from) throws SQLException {
		// the JID is on format <blah>@<kek>/<resource>
		// so only grab the first part.
		List<XMPNode> queryForEq = null;
		queryForEq = XMPDb.Nodes.queryForEq(XMPNode.NAME,
				from.split("@")[0]);
		try {
			return queryForEq.get(0);
		} catch (IndexOutOfBoundsException ie) {
			return null;
		} catch (NullPointerException e) {
			return null;
		}
	}

	public static XMPNode createByJID(String from) throws SQLException {
		XMPNode node = new XMPNode();

		String name = from.split("@")[0];
		node.setName(name);
		try {
			node.setType(XMPNode.getTypeFromName(name));
		} catch (IllegalArgumentException e) {
			node.setType(NodeType.unknown);
		}
		XMPDb.Nodes.create(node);
		return node;
	}

	private static NodeType getTypeFromName(String name) {
		String typename = null;
		if (name.contains("-")) {
			typename = name.split("-")[0];
		} else {
			typename = name;
		}
		String s = NodeType.leaf.toString();
		String n = NodeType.leaf.name();
		NodeType[] b = NodeType.values();
		try {
			// NodeType t = NodeType.valueOf(name);
			// NodeType t2 = Enum.valueOf(NodeType.class, name);
			// NodeType t3 = NodeType.valueOf("leaf");
			// boolean w = n.equals("leaf");
			// boolean w2 = n.equals(name);
			//
			// // NodeType t4 = NodeType.valueOf("LEAF");
			// NodeType l = NodeType.valueOf(NodeType.leaf.name());
			// NodeType na = NodeType.valueOf(name);

		} catch (Exception e) {
			logger.info(e);
		}
		return NodeType.valueOf(typename);
	}

	public boolean isRegistered() {
		return registered;
	}

	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

	public boolean equals(Object o) {
		return o.getClass().equals(XMPNode.class)
				&& ((XMPNode) o).getId() == id;
	}

	public static XMPNode getOrCreateByJID(String string) throws SQLException {
		XMPNode node = getByJID(string);
		if (node == null) {
			node = createByJID(string);
		}
		return node;
	}

	public List<XMPDataPoint> getUnsentDataPoints() throws SQLException {
		if (type != NodeType.backend) {
			throw new IllegalArgumentException();
		}
		List<XMPDataPoint> unsent = new ArrayList<XMPDataPoint>();
		for (XMPDataPoint point : XMPDb.DataPoints.queryForEq(
				XMPDataPoint.SENT_TO_ALL, false)) {
			boolean hasBeenSent = false;
			for (XMPMessage message : XMPDataPointMessages
					.messagesForPoint(point)) {
				if (message.getTarget().equals(this)) {
					hasBeenSent = true;
					break;
				}
			}
			if (!hasBeenSent) {
				unsent.add(point);
			}
		}
		return unsent;
	}

	public static List<XMPNode> getRegisteredBackends() throws SQLException {
		return XMPDb.Nodes.queryBuilder().where().eq(TYPE, NodeType.backend)
				.and().eq(REGISTERED, true).query();
	}

	@Override
	public void readObject(JSONObject stream) throws JSONException {
		name = stream.getString("name");
		type = getTypeFromName(stream.getString("name"));
		registered = false;
	}

	@Override
	public void writeObject(JSONObject stream) throws JSONException {
		stream.put("name", name);
	}

	public static List<XMPNode> getLeaves() throws SQLException {
		return XMPDb.Nodes.queryForEq(TYPE, NodeType.leaf);
	}

	public Collection<?> getDatapoints() throws SQLException {
		return XMPDb.DataPoints.queryForEq(XMPDataPoint.FROM, this);
	}

	public X509CertificateObject getCert() throws IOException {
		if (this.cert == null) {
			return null;
		}
		StringReader fr = new StringReader(this.cert);
		X509CertificateObject cert = null;
		PEMReader r = new PEMReader(fr);
		Object o;
		o = r.readObject();

		if (o instanceof X509CertificateObject) {
			cert = (X509CertificateObject) o;
		}
		r.close();
		return cert;
	}

	public void setCert(X509CertificateObject cert) {
		StringWriter sw = new StringWriter();
		PEMWriter wr = new PEMWriter(sw);
		try {
			wr.writeObject(cert);
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.cert = sw.toString();
	}

}
