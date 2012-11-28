package se.localhost.xmplary.xmpleaf.strategies;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.LoggerReceiver;
import se.lolcalhost.xmplary.common.commands.Register;
import se.lolcalhost.xmplary.common.commands.RespondToRegistrationRequest;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.AbstractMessageReceiverStrategy;

public class LeafGatewayReceiverStrategy extends AbstractMessageReceiverStrategy {
	protected static Logger logger = Logger.getLogger(LeafGatewayReceiverStrategy.class);

	public LeafGatewayReceiverStrategy(XMPMain main) {
		super(main);
	}
	
	@Override
	protected void registerNodeTypes() {
		nodeTypes.add(NodeType.gateway);
	};
	
	@Override
	protected void registerUnsafeHandlers() {
		handlerClassesUnsafe.put(MessageType.Register, Register.class);
		handlerClassesUnsafe.put(MessageType.RegistrationRequest, RespondToRegistrationRequest.class);
	}

	@Override
	protected void registerHandlers() {
	}

}
