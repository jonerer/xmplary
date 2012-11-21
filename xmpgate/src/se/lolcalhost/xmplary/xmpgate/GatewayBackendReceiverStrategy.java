package se.lolcalhost.xmplary.xmpgate;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.AbstractMessageReceiverStrategy;
import se.lolcalhost.xmplary.xmpgate.commands.IsRegistered;
import se.lolcalhost.xmplary.xmpgate.commands.RegisterBackend;
import se.lolcalhost.xmplary.xmpgate.commands.RemoveBackend;
import se.lolcalhost.xmplary.xmpgate.commands.SendDataPoints;

public class GatewayBackendReceiverStrategy extends AbstractMessageReceiverStrategy {
	public GatewayBackendReceiverStrategy(XMPMain main) {
		super(main);
	}
	
	@Override
	protected void registerHandlers() {
		handlerClasses.put(MessageType.IsRegistered, IsRegistered.class);
		handlerClasses.put(MessageType.RegisterBackend, RegisterBackend.class);
		handlerClasses.put(MessageType.RemoveBackend, RemoveBackend.class);
		handlerClasses.put(MessageType.RequestDataPoints, SendDataPoints.class);
	}
	
	@Override
	protected void registerNodeTypes() {
		nodeTypes.add(NodeType.backend);
	}
	
}
