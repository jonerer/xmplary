package se.lolcalhost.xmplary.xmpgate.strategies;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.AbstractMessageReceiverStrategy;
import se.lolcalhost.xmplary.common.strategies.IMessageReceiverStrategy;
import se.lolcalhost.xmplary.xmpgate.commands.SwitchboardCommand;

public class SwitchboardReceiverStrategy extends
		AbstractMessageReceiverStrategy implements IMessageReceiverStrategy {

	public SwitchboardReceiverStrategy(XMPMain main) {
		super(main);
		setHandleMessagesNotToSelf(true);
	}

	@Override
	protected void registerHandlers() {
		handlerClasses.put(MessageType.RegistrationRequest, SwitchboardCommand.class);
		handlerClasses.put(MessageType.Register, SwitchboardCommand.class);
	}

	@Override
	protected void registerNodeTypes() {
		nodeTypes.add(NodeType.leaf);
		nodeTypes.add(NodeType.backend);
	}

}
