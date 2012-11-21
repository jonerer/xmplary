package se.lolcalhost.xmplary.common.strategies;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.LoggerReceiver;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;

public class LoggerReceiverStrategy extends AbstractMessageReceiverStrategy {
	public LoggerReceiverStrategy(XMPMain main) {
		super(main);
	}

	@Override
	protected void registerHandlers() {
		for (MessageType type : XMPMessage.MessageType.values()) {
			handlerClasses.put(type, LoggerReceiver.class);
		}
		
	}
	
	@Override
	protected void registerNodeTypes() {
		for (NodeType type : XMPNode.NodeType.values()) {
			nodeTypes.add(type);
		}
	}

}
