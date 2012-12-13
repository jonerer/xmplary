package se.lolcalhost.xmplary.common;

import java.security.Security;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
import org.jivesoftware.smackx.MessageEventManager;

import se.lolcalhost.xmplary.common.commands.RequestRegistrationCommand;
import se.lolcalhost.xmplary.common.commands.UnpackAndReceiveMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.strategies.AbstractMessageDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.IMessageReceiverStrategy;

public class XMPMain {
	protected static Logger logger = Logger.getLogger(XMPMain.class);
	protected Properties p;
	private Connection connection;
	boolean keepRunning = true;

	protected ArrayList<AbstractMessageDispatchStrategy> dispatchers = new ArrayList<AbstractMessageDispatchStrategy>();
	protected ArrayList<IMessageReceiverStrategy> receivers = new ArrayList<IMessageReceiverStrategy>();

	
	
	protected XMPMain(String config) {
		PropertyConfigurator.configure("../xmpcommon/log5j.properties"); // initialize
																			// log4j
		Security.insertProviderAt(new BouncyCastleProvider(), 1);

		XMPConfig.init(config);
		p = XMPConfig.getInstance();
		XMPDb.init();
		XMPCrypt.init();
		XMPMessage.setMain(this);

		connection = new XMPPConnection(p.getProperty("Domain"));
		int tries = 0; 
		boolean connected = false; 
		int maxtries = 5;
		while (tries < maxtries && connected == false) {
			try {
				tries++;
				connection.connect();
				connected = true;
			} catch (XMPPException e3) {
				logger.error("Unable to connect. Try " + tries + ". Max is " + maxtries);
				e3.printStackTrace();
			}
		}
		if (!connected) {
			logger.error("FATAL: Unable to create connection after " + maxtries + " tries.");
			System.exit(1);
		}
		logger.info("Connected! After " + tries + " tries.");

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

		// start the command runner thread:
		XMPCommandRunner cmd = new XMPCommandRunner();
		cmd.start();
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



	public void dispatchRaw(String xmp) {
		// TODO: here is where to sign stuff.
		for (AbstractMessageDispatchStrategy dispatcher : dispatchers) {
			dispatcher.DispatchRawMessage(xmp);
		}
	}

	public void receivePacket(Packet p) {
		if (p instanceof Message) {
			receiveMessage((Message) p);
		}
	}

	public void receiveMessage(Message message) {
		// TODO: here is where to check the integrity of stuff, and stuff's
		// sender
		for (IMessageReceiverStrategy receiver : receivers) {
			receiver.PreparseReceiveMessage(message);
		}

		XMPMessage msg = null;
		UnpackAndReceiveMessage uarm = new UnpackAndReceiveMessage(this, msg, message);
		uarm.schedule();
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	protected void attemptRegistration() {
		// TODO.
	}

	public void runReceiveHandlers(XMPMessage msg) {
		if (msg != null) {
			for (IMessageReceiverStrategy receiver : receivers) {
				receiver.ReceiveMessage(msg);
			}
		}
	}

	/**
	 * Don't use this function directly. instead, create and schedule a MessageDispatchCommand.
	 * @param msg
	 */
	public void sendToDispatchers(XMPMessage msg) {
		for (AbstractMessageDispatchStrategy dispatcher : dispatchers) {
			dispatcher.DispatchMessage(msg);
		}
	}

}
