package se.lolcalhost.xmplary.xmpback.strategies;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.IncomingDataPoints;
import se.lolcalhost.xmplary.common.commands.Register;
import se.lolcalhost.xmplary.common.commands.RespondToRegistrationRequest;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.AbstractMessageReceiverStrategy;
import se.lolcalhost.xmplary.xmpback.commands.AlarmReceiver;
import se.lolcalhost.xmplary.xmpback.commands.WeldStatusReceiver;

public class BackendGatewayReceiverStrategy extends AbstractMessageReceiverStrategy {
	public BackendGatewayReceiverStrategy(XMPMain main) {
		super(main);
	}
	
	@Override
	protected void registerUnsafeHandlers() {
		handlerClassesUnsafe.put(MessageType.Register, Register.class);
		handlerClassesUnsafe.put(MessageType.RegistrationRequest, RespondToRegistrationRequest.class);
	}

	@Override
	protected void registerHandlers() {
		handlerClasses.put(MessageType.DataPoints, IncomingDataPoints.class);
		handlerClasses.put(MessageType.Alarm, AlarmReceiver.class);
		handlerClasses.put(MessageType.WelderStatus, WeldStatusReceiver.class);
	}

	@Override
	protected void registerNodeTypes() {
		nodeTypes.add(NodeType.gateway);
	}
}
