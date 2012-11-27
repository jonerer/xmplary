package se.lolcalhost.xmplary.common.strategies;

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
	protected void registerUnsafeHandlers() {
		for (MessageType type : XMPMessage.MessageType.values()) {
			handlerClassesUnsafe.put(type, LoggerReceiver.class);
		}
	}

	@Override
	protected void registerHandlers() {}
	
	@Override
	protected void registerNodeTypes() {
		for (NodeType type : XMPNode.NodeType.values()) {
			nodeTypes.add(type);
		}
	}

}
