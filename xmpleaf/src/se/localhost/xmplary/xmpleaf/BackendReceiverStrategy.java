package se.localhost.xmplary.xmpleaf;

import se.lolcalhost.xmplary.common.commands.DumpResponse;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.AbstractMessageReceiverStrategy;
import se.lolcalhost.xmplary.common.strategies.IMessageReceiverStrategy;

public class BackendReceiverStrategy extends AbstractMessageReceiverStrategy
		implements IMessageReceiverStrategy {

	public BackendReceiverStrategy(LeafMain leafMain) {
		super(leafMain);
	}

	@Override
	protected void registerHandlers() {
		handlerClasses.put(MessageType.DumpRequest, DumpResponse.class);

	}

	@Override
	protected void registerNodeTypes() {
		nodeTypes.add(NodeType.gateway);
		nodeTypes.add(NodeType.backend);
	}

}
