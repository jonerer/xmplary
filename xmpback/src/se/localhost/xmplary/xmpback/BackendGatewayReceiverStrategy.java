package se.localhost.xmplary.xmpback;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.IncomingDataPoints;
import se.lolcalhost.xmplary.common.commands.LoggerReceiver;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.AbstractMessageReceiverStrategy;

public class BackendGatewayReceiverStrategy extends AbstractMessageReceiverStrategy {
	public BackendGatewayReceiverStrategy(XMPMain main) {
		super(main);
	}

	@Override
	protected void registerHandlers() {
		handlerClasses.put(MessageType.DataPoints, IncomingDataPoints.class);
		handlerClasses.put(MessageType.Alarm, AlarmReceiver.class);

	}

	@Override
	protected void registerNodeTypes() {
		nodeTypes.add(NodeType.gateway);
	}
}
