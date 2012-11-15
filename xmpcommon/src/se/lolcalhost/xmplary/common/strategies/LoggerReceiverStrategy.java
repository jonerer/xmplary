package se.lolcalhost.xmplary.common.strategies;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class LoggerReceiverStrategy extends MessageReceiverStrategy {
	private Logger logger;

	public LoggerReceiverStrategy(XMPMain main) {
		super(main);
		logger = Logger.getLogger(this.getClass());
	}

	@Override
	public void ReceiveMessage(XMPMessage m) {
		//XMPNode s = XMPNode.getByJID(m.getFrom());
		String format = String.format("Received message of type %s with contents %s. Sender is %s. I am %s.", m.getType().toString(),
				m.getRawContents(), m.getFrom().getName(), XMPNode.getSelf().getName());
		logger.info(format);
	}

}
