package se.localhost.xmplary;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPointMessages;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.MessageReceiverStrategy;

public class BackendGatewayReceiverStrategy extends MessageReceiverStrategy {
	protected static Logger logger = Logger.getLogger(BackendGatewayReceiverStrategy.class);
	public BackendGatewayReceiverStrategy(XMPMain main) {
		super(main);
		registerHandlers();
	}

	public interface BackendMessageCommandStrategy {
		public void HandleCommand(XMPMessage m) throws JSONException, SQLException, AuthorizationFailureException;
	}

	HashMap<MessageType, BackendMessageCommandStrategy> handlers = new HashMap<MessageType, BackendMessageCommandStrategy>();

	@Override
	public void ReceiveMessage(XMPMessage m) {
		if (m.getFrom().getType() == NodeType.gateway) {
			if (m.getTarget().equals(XMPNode.getSelf())) {
				// else, disregard
				try {
					handlers.get(m.getType()).HandleCommand(m);
				} catch (JSONException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				} catch (SQLException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				} catch (AuthorizationFailureException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				}
			}
		}
	}
	
	/**
	 * 
	 */
	private void registerHandlers() {
		handlers.put(MessageType.DataPoints, new BackendMessageCommandStrategy() {
			@Override
			public void HandleCommand(XMPMessage m) throws JSONException {
				List<XMPDataPoint> points = (List<XMPDataPoint>) m.getContents();
				for (XMPDataPoint dataPoint : points) {
					dataPoint.save();
					XMPDataPointMessages dpm = new XMPDataPointMessages(dataPoint, m);
					dpm.save();
				}
				main.dispatchRaw(String.format("Received %d DataPoints from %s. Thanks for the business, please come again!", points.size(), m.getFrom().getJID()));
			}
		});
	}

	protected void requireRegisteredLeaf(XMPMessage m) throws AuthorizationFailureException {
		if (m.getFrom().getType() != NodeType.leaf || !m.getFrom().isRegistered()) {
			throw new AuthorizationFailureException();
		}
	}

}
