package se.lolcalhost.xmplary.common.models;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.XMPConfig;
import se.lolcalhost.xmplary.common.XMPDb;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class XMPNode {
	static Logger logger = Logger.getLogger(XMPNode.class);
	
	public enum NodeType {
		gateway,
		leaf,
		backend
	}

	private static final String ID = "id";
	@DatabaseField(id = true, canBeNull = false, columnName = ID)
	private int id;


	private static final String NAME = "name";
	@DatabaseField(canBeNull = false, columnName = NAME)
	private String name;
	
	private static final String REGISTERED = "registered";
	@DatabaseField(canBeNull = false, columnName = REGISTERED)
	private boolean registered = false;
	
	private static final String TYPE = "type";
	@DatabaseField(canBeNull = false, columnName = TYPE)
	private NodeType type;
	
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
		return name + "@" + XMPConfig.Domain() + "/Smack";
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
	public static XMPNode self() {
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
		node.setType(XMPNode.getTypeFromName(name));
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

}

