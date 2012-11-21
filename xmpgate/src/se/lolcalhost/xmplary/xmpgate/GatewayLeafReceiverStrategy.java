package se.lolcalhost.xmplary.xmpgate;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.IncomingDataPoints;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.AbstractMessageReceiverStrategy;
import se.lolcalhost.xmplary.xmpgate.commands.IsRegistered;
import se.lolcalhost.xmplary.xmpgate.commands.MulticastToBackends;
import se.lolcalhost.xmplary.xmpgate.commands.Register;
import se.lolcalhost.xmplary.xmpgate.commands.Unregister;

public class GatewayLeafReceiverStrategy extends AbstractMessageReceiverStrategy {
	protected static Logger logger = Logger.getLogger(GatewayLeafReceiverStrategy.class);

	public GatewayLeafReceiverStrategy(XMPMain main) {
		super(main);
	}
	
	@Override
	protected void registerNodeTypes() {
		nodeTypes.add(NodeType.leaf);
	};

	protected void registerHandlers() {
		handlerClasses.put(MessageType.Alarm, MulticastToBackends.class);
		handlerClasses.put(MessageType.DataPoints, IncomingDataPoints.class);
		
		handlerClasses.put(MessageType.IsRegistered, IsRegistered.class);
		handlerClasses.put(MessageType.Register, Register.class);
		handlerClasses.put(MessageType.Unregister, Unregister.class);
	}

}
