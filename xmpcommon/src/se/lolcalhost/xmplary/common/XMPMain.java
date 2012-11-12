package se.lolcalhost.xmplary.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Presence;
import org.json.JSONException;

import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.strategies.MessageDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MessageReceiverStrategy;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class XMPMain {
	protected static Logger logger = Logger.getLogger(XMPMain.class);
	protected Properties p = XMPConfig.getInstance();
	private Connection connection;
	boolean keepRunning = true;
	
	protected ArrayList<MessageDispatchStrategy> dispatchers = new ArrayList<MessageDispatchStrategy>();
	protected ArrayList<MessageReceiverStrategy> receivers = new ArrayList<MessageReceiverStrategy>();

	protected void init() {
		BasicConfigurator.configure(); // initialize log4j
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
		
		connection.getChatManager().addChatListener(
		    new ChatManagerListener() {
		        @Override
		        public void chatCreated(Chat chat, boolean createdLocally)
		        {
//		            if (!createdLocally)
		                chat.addMessageListener(new MessageListener() {
							
							@Override
							public void processMessage(Chat arg0, Message arg1) {
								logger.trace("Message of type " + arg1.getType() + " from " + arg1.getFrom());
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

	protected void create(String name,
			String pass) {
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
//		List<Message> query = Messages.queryBuilder().where().eq(Message.DELIVERED, false).query();
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
		for (MessageDispatchStrategy dispatcher : dispatchers) {
			dispatcher.DispatchMessage(xmp);
		}
	}
	
	public void receiveMessage(Message message) {
		// TODO: here is where to check the integrity of stuff.
		XMPMessage msg;
		try {
			logger.trace(String.format("Attempting to parse message %s ...", message.getBody()));
			msg = XMPMessage.unpack(message);
			
			logger.trace("Parse successful. Finding origin node.");
			
			XMPNode from = XMPNode.getByJID(message.getFrom());
			if (from == null) {
				logger.info("Unknown sender. Creating new database record.");
				from =  XMPNode.createByJID(message.getFrom());
			} else {
				logger.trace("Sender node identified.");
			}
			msg.setFrom(from);
			
			for (MessageReceiverStrategy receiver : receivers) {
				receiver.ReceiveMessage(msg);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
}
