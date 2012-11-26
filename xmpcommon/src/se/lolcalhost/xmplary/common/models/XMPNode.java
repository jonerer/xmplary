package se.lolcalhost.xmplary.common.models;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPConfig;
import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.interfaces.JSONSerializable;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;

import com.j256.ormlite.field.DatabaseField;

public class XMPNode implements JSONSerializable {
	static Logger logger = Logger.getLogger(XMPNode.class);
	
	public enum NodeType {
		gateway,
		leaf,
		backend,
		operator,
		chatroom,
		unknown
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
	
	public static final String PUBLICKEY = "publicKey";
	@DatabaseField(canBeNull = true, columnName = PUBLICKEY)
	private String publicKey;
	
	/**
	 * public key. maybe use a cert library and save certs to disk instead.
	 */
//	@DatabaseField(canBeNull = false)
//	private byte[] publicKey;
	
	/**
	 * the unique ID. maybe use a cert library and save certs to disk instead.
	 */
//	@DatabaseField(canBeNull = false)
//	private byte[] uuid;
	
	/**
	 * has a shared Secret been negotiated? dont save it in the database. make sure to make a new
	 * one for the next session.
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
			return name + "@" + XMPConfig.Domain() + "/Smack";
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
	public static XMPNode getGateway() {
		if (gateway == null) {
			gateway = new XMPNode();
			gateway.setType(NodeType.gateway);
			gateway.setName("gateway");
		}
		return gateway;
	}
	
	private static XMPNode self = null;
	public static XMPNode getSelf() {
		if (self == null) {
			try {
				List<XMPNode> queryForEq = XMPDb.Nodes.queryForEq(NAME, XMPConfig.Name());
				if (queryForEq.isEmpty()) {
					logger.info("Self not found in database. Creating...");
					self = new XMPNode();
					self.setName(XMPConfig.Name());
					self.setType(XMPConfig.Type());
				} else {
					self = queryForEq.get(0);
				}
				XMPDb.Nodes.createIfNotExists(self);
			} catch (SQLException e) {
				handleException(e);
			}
		}
		return self;
	}
	
	private static XMPNode operator = null;
	public static XMPNode getOperator() {
		if (operator == null) {
			try {
				List<XMPNode> queryForEq = XMPDb.Nodes.queryForEq(TYPE, NodeType.operator);
				if (queryForEq.size() != 0) {
					operator = queryForEq.get(0);
				}
			} catch (SQLException e) {
				handleException(e);
			}
		}
		return operator;
	}
	
	private static XMPNode room = null;
	public static XMPNode getRoom() {
		if (room == null) {
			try {
				List<XMPNode> queryForEq = XMPDb.Nodes.queryForEq(NAME, XMPConfig.Room());
				if (queryForEq.isEmpty()) {
					logger.info("Self not found in database. Creating...");
					room = new XMPNode();
					room.setName(XMPConfig.Room());
					room.setType(NodeType.chatroom);
					room.save();
				} else {
					room = queryForEq.get(0);
				}
			} catch (SQLException e) {
				handleException(e);
			}
		}
		return room;
	}

	public void save() {
		try {
			XMPDb.Nodes.createOrUpdate(this);
		} catch (SQLException e) {
			logger.error("Couldn't save node-connection: ", e);
		}
	}

	private static void handleException(SQLException e) {
		logger.error("Database error!", e);
	}

	public NodeType getType() {
		return type;
	}

	public void setType(NodeType type) {
		this.type = type;
	}

	public static XMPNode getByJID(String from) {
		// the JID is on format <blah>@<kek>/<resource>
		// so only grab the first part.
		List<XMPNode> queryForEq = null;
		try {
			queryForEq = XMPDb.Nodes.queryForEq(XMPNode.NAME, from.split("@")[0]);
		} catch (SQLException e) {
			handleException(e);
		}
		try {
			return queryForEq.get(0);
		} catch (IndexOutOfBoundsException ie) {
			return null;
		}
		catch (NullPointerException e) {
			return null;
		}
	}

	public static XMPNode createByJID(String from) {
		XMPNode node = new XMPNode();
		
		String name = from.split("@")[0];
		node.setName(name);
		try {
			node.setType(XMPNode.getTypeFromName(name));
		} catch (IllegalArgumentException e) {
			node.setType(NodeType.unknown);
		}
		try {
			XMPDb.Nodes.create(node);
		} catch (SQLException e) {
			handleException(e);
		}
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
//			NodeType t = NodeType.valueOf(name);
//			NodeType t2 = Enum.valueOf(NodeType.class, name);
//			NodeType t3 = NodeType.valueOf("leaf");
//			boolean w = n.equals("leaf");
//			boolean w2 = n.equals(name);
//
//			//			NodeType t4 = NodeType.valueOf("LEAF");
//			NodeType l = NodeType.valueOf(NodeType.leaf.name());
//			NodeType na = NodeType.valueOf(name);

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
		return o.getClass().equals(XMPNode.class) && ((XMPNode) o).getId() == id;
	}

	public static XMPNode getOrCreateByJID(String string) {
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
		for (XMPDataPoint point : XMPDb.DataPoints.queryForEq(XMPDataPoint.SENT_TO_ALL, false)) {
			boolean hasBeenSent = false;
			for (XMPMessage message : XMPDataPointMessages.messagesForPoint(point)) {
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
		return XMPDb.Nodes.queryBuilder().where().eq(TYPE, NodeType.backend).and().eq(REGISTERED, true).query();
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

	public static List<XMPNode> getLeaves() {
		try {
			return XMPDb.Nodes.queryForEq(TYPE, NodeType.leaf);
		} catch (SQLException e) {
			handleException(e);
		}
		return null;
	}

	public Collection<?> getDatapoints() {
		try {
			return XMPDb.DataPoints.queryForEq(XMPDataPoint.FROM, this);
		} catch (SQLException e) {
			handleException(e);
		}
		return null;
	}

}

