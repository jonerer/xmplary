package se.lolcalhost.xmplary.common.strategies;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Message;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;

public abstract class AbstractMessageReceiverStrategy implements
		IMessageReceiverStrategy {
	protected XMPMain main;
	protected static Logger logger = Logger
			.getLogger(AbstractMessageReceiverStrategy.class);
	protected HashMap<MessageType, Class<? extends Command>> handlerClasses = new HashMap<XMPMessage.MessageType, Class<? extends Command>>();
	protected Set<NodeType> nodeTypes = new HashSet<NodeType>();
	protected HashMap<MessageType, Class<? extends Command>> handlerClassesUnsafe = new HashMap<XMPMessage.MessageType, Class<? extends Command>>();

	public AbstractMessageReceiverStrategy(XMPMain main) {
		this.main = main;
		registerHandlers();
		registerNodeTypes();
		registerUnsafeHandlers();
	}

	public void PreparseReceiveMessage(Message m) {
	}

	protected abstract void registerHandlers();

	protected abstract void registerNodeTypes();

	/**
	 * Not mandatory.
	 */
	protected void registerUnsafeHandlers() {
	}

	public void ReceiveMessage(XMPMessage m) {
		if (nodeTypes.contains(m.getFrom().getType())) {
			if (m.getTarget().equals(XMPNode.getSelf())) {
				// else, pass it on along to the real target.
				try {
					Class<? extends Command> c;
					c = handlerClassesUnsafe.get(m.getType());

					if (m.isVerified() && handlerClasses.containsKey(m.getType())) {
						c = handlerClasses.get(m.getType());
					}
					if (c != null) {
						Constructor<? extends Command> con = c.getConstructor(
								XMPMain.class, XMPMessage.class);
						Command command = con.newInstance(main, m);
						command.schedule();
					}
				} catch (SecurityException e) {
					logger.error("Error in gateway backend reciever handler: ",
							e);
				} catch (NoSuchMethodException e) {
					logger.error("Error in gateway backend reciever handler: ",
							e);
				} catch (IllegalArgumentException e) {
					logger.error("Error in gateway backend reciever handler: ",
							e);
				} catch (InstantiationException e) {
					logger.error("Error in gateway backend reciever handler: ",
							e);
				} catch (IllegalAccessException e) {
					logger.error("Error in gateway backend reciever handler: ",
							e);
				} catch (InvocationTargetException e) {
					logger.error("Error in gateway backend reciever handler: ",
							e);
				}
			}
		}
	}

}
