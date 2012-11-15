package se.lolcalhost.xmplary.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.strategies.MessageDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MessageReceiverStrategy;

public class XMPMain {
	protected static Logger logger = Logger.getLogger(XMPMain.class);
	protected Properties p = XMPConfig.getInstance();
	private Connection connection;
	boolean keepRunning = true;

	protected ArrayList<MessageDispatchStrategy> dispatchers = new ArrayList<MessageDispatchStrategy>();
	protected ArrayList<MessageReceiverStrategy> receivers = new ArrayList<MessageReceiverStrategy>();

	protected void init() {
		PropertyConfigurator.configure("../xmpcommon/log4j.properties"); // initialize log4j
		XMPDb.init();
		initConf();

		connection = new XMPPConnection(p.getProperty("Domain"));
		try {
			connection.connect();
		} catch (XMPPException e3) {
			e3.printStackTrace();
		}

		String name = p.getProperty("name");
		String pass = p.getProperty("pass");
		login(name, pass);
		if (!connection.isAuthenticated()) {
			logger.info("Couldn't authenticate with given credentials. Creating account.");
			create(name, pass);
			login(name, pass);
		}
		if (!connection.isAuthenticated()) {
			logger.error("Couldn't authenticate. Shutting down.");
			System.exit(0);
		}

		Presence presence = new Presence(Presence.Type.available);
		presence.setStatus("allmänt cool");
		connection.sendPacket(presence);

		connection.getChatManager().addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean createdLocally) {
				// if (!createdLocally)
				chat.addMessageListener(new MessageListener() {

					@Override
					public void processMessage(Chat arg0, Message arg1) {
						logger.trace("Message of type " + arg1.getType()
								+ " from " + arg1.getFrom());
						if (arg1.getType() == Type.chat) {
							receiveMessage(arg1);
						}
					}
				});
			}
		});

	}

	private void initConf() {
		XMPConfig.getInstance();
	}

	protected void create(String name, String pass) {
		try {
			connection.getAccountManager().createAccount(name, pass);
		} catch (XMPPException e) {
			logger.info("Couldn't create account.");
			e.printStackTrace();
		}
	}

	protected void login(String name, String pass) {
		try {
			connection.login(name, pass);
		} catch (XMPPException e1) {
			logger.info("Login failed.");
			e1.printStackTrace();
		}
	}

	protected void close() {
		XMPDb.close();
	}

	protected void deliverMessages() throws SQLException {
		// read from the queue to see if there is something new to deliver
		// List<Message> query =
		// Messages.queryBuilder().where().eq(Message.DELIVERED, false).query();
	}

	protected void keepRunning() {
		while (keepRunning) {
			try {
				boolean isConnected = connection.isConnected();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void dispatch(XMPMessage xmp) {
		// TODO: here is where to sign stuff.
		try {
			xmp.setDelivered(true); // TODO: maybe do this later, after getting an ACK?
			xmp.setOutgoing(true);
			XMPDb.Messages.createOrUpdate(xmp);
		} catch (SQLException e) {
			logger.error("Tried to save xmp message before sending it. Failed.");
		}
		for (MessageDispatchStrategy dispatcher : dispatchers) {
			dispatcher.DispatchMessage(xmp);
		}
	}
	
	public void dispatchRaw(String xmp) {
		// TODO: here is where to sign stuff.
		for (MessageDispatchStrategy dispatcher : dispatchers) {
			dispatcher.DispatchRawMessage(xmp);
		}
	}
	
	public void receivePacket(Packet p) {
		if (p instanceof Message) {
			receiveMessage((Message)p);
		}
	}

	public void receiveMessage(Message message) {
		// TODO: here is where to check the integrity of stuff, and stuff's sender
		for (MessageReceiverStrategy receiver : receivers) {
			receiver.PreparseReceiveMessage(message);
		}
		
		XMPMessage msg;
		logger.trace(String.format("Attempting to parse message %s ...",
				message.getBody()));
		msg = XMPMessage.unpack(message);
		msg.save();

		if (msg != null) {
			for (MessageReceiverStrategy receiver : receivers) {
				receiver.ReceiveMessage(msg);
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}
