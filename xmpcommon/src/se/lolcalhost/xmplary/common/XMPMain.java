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

public abstract class XMPMain {
	protected static Logger logger = Logger.getLogger(XMPMain.class);
	protected Properties p;
	private Connection connection;
	boolean keepRunning = true;

	protected ArrayList<AbstractMessageDispatchStrategy> dispatchers = new ArrayList<AbstractMessageDispatchStrategy>();
	protected ArrayList<IMessageReceiverStrategy> receivers = new ArrayList<IMessageReceiverStrategy>();

	public abstract void init() throws SQLException;
	
	protected XMPMain(String config) {
		PropertyConfigurator.configure(XMPConfig.getLog4jConfig()); // initialize
																			// log4j
		Security.insertProviderAt(new BouncyCastleProvider(), 1);

		System.out.print("Initing config...");
		XMPConfig.init(config);
		System.out.println("done.");
		p = XMPConfig.getInstance();
		System.out.print("Initing db... ");
		XMPDb.init();
		System.out.println("done.");
		System.out.print("Initing crypt...");
		XMPCrypt.init();
		System.out.println("done.");
		XMPMessage.setMain(this);

		System.out.print("Attempting connection...");
		connection = new XMPPConnection(XMPConfig.Address());
		int tries = 0; 
		boolean connected = false; 
		int maxtries = 5;
		while (tries < maxtries && connected == false) {
			try {
				tries++;
				connection.connect();
				connected = true;
			} catch (XMPPException e3) {
				logger.error("Unable to connect. Try " + tries + ". Max is " + maxtries + ". Server address is " + XMPConfig.Address());
//				e3.printStackTrace();
			}
		}
		if (!connected) {
			logger.fatal("FATAL: Unable to create connection after " + maxtries + " tries.");
			System.exit(1);
		}
		System.out.println("done.");
		logger.info("Connected! After " + tries + " tries.");

		System.out.print("Authenticating and sending prescence...");
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

		System.out.println("done.");
		System.out.print("Starting command runner...");
		// start the command runner thread:
		XMPCommandRunner cmd = new XMPCommandRunner();
		cmd.start();
		System.out.println("done.");
		System.out.print("Initting userspace code...");
		try {
			this.init();
		} catch (SQLException e) {
			logger.error("Couldn't init userspace code.", e);
		}
		System.out.println("done.");
		System.out.println("XMPLary client running as " + p.getProperty("name") + "! See the log file in files/ for the good stuff.");
		System.out.println("Starting foreverloop.");
		keepRunning();
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



	public void dispatchRaw(String xmp) throws SQLException {
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
			try {
				receiver.PreparseReceiveMessage(message);
			} catch (SQLException e) {
				logger.error("Exception in preparsereceivemessage: ", e);
			}
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
	
	public void runReceiveHandlers(XMPMessage msg) throws SQLException {
		if (msg != null) {
			for (IMessageReceiverStrategy receiver : receivers) {
				receiver.ReceiveMessage(msg);
			}
		}
	}

	/**
	 * Don't use this function directly. instead, create and schedule a MessageDispatchCommand.
	 * @param msg
	 * @throws SQLException 
	 */
	public void sendToDispatchers(XMPMessage msg) throws SQLException {
		for (AbstractMessageDispatchStrategy dispatcher : dispatchers) {
			dispatcher.DispatchMessage(msg);
		}
	}

}
