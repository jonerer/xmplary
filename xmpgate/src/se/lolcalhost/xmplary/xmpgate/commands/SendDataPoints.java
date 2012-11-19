package se.lolcalhost.xmplary.xmpgate.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.XMPConfig;
import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPointMessages;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;

public class SendDataPoints extends Command {
	private XMPNode target;
	protected static Logger logger = Logger.getLogger(XMPMain.class);

	public SendDataPoints(XMPMain m, XMPNode target) {
		super(m, null);
		this.target = target;
		
	}

	public SendDataPoints(XMPMain m, XMPMessage msg) {
		super(m, msg);
		target = msg.getFrom();
	}

	@Override
	public void execute() throws AuthorizationFailureException, SQLException {
		// dont expect to have msg saved, just a target.
		
		requireRegisteredBackend(target);
		// this should probably be turned into some huge SQL clusterfuck but here we go...
		
		List<XMPDataPoint> unsent = new ArrayList<XMPDataPoint>();
		for (XMPDataPoint point : XMPDb.DataPoints) {
			boolean hasBeenSent = false;
			for (XMPMessage message : XMPDataPointMessages.messagesForPoint(point)) {
				if (message.getTarget().equals(target)) {
					hasBeenSent = true;
					break;
				}
			}
			if (!hasBeenSent) {
				unsent.add(point);
			}
		}
		int maxDataPointsPerPacket = XMPConfig.getMaxDataPointsPerPacket();

		logger.info(String.format("Gateway has %d points that %s lacks. Commencing send, %d points per packet",
				unsent.size(), target.getName(), maxDataPointsPerPacket));
		
		
		for (int i = 0; i * maxDataPointsPerPacket < unsent.size(); i++) {
			XMPMessage response = new XMPMessage(MessageType.DataPoints);
			response.setTarget(target);
			response.setType(MessageType.DataPoints);
			
			List<XMPDataPoint> pts = new ArrayList<XMPDataPoint>();
			for (int j = 0; j < maxDataPointsPerPacket; j++) { 
				try {
					XMPDataPoint point = unsent.get(j + maxDataPointsPerPacket * i);
					XMPDb.Nodes.refresh(point.getFrom());
					pts.add(point);
				} catch (IndexOutOfBoundsException e) {
					break;
				}
			}
			response.setContents(pts);
			
			main.dispatchRaw(String.format("Sent %d datapoints to %s. %d remaining.", maxDataPointsPerPacket, response.getTarget().getName(), unsent.size() - (i*maxDataPointsPerPacket)));
			main.dispatch(response);
		}

	}

	private void requireRegisteredBackend(XMPNode node) throws AuthorizationFailureException {
		if (node.getType() != NodeType.backend || !node.isRegistered()) {
			throw new AuthorizationFailureException();
		}
	}

}
