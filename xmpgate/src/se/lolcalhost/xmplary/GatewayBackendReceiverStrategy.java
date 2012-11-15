package se.lolcalhost.xmplary;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPConfig;
import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPointMessages;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.MessageReceiverStrategy;

public class GatewayBackendReceiverStrategy extends MessageReceiverStrategy {
	public interface BackendMessageCommandStrategy {
		public void HandleCommand(XMPMessage m) throws JSONException, SQLException, AuthorizationFailureException;
	}

	HashMap<MessageType, BackendMessageCommandStrategy> handlers = new HashMap<MessageType, BackendMessageCommandStrategy>();
	protected static Logger logger = Logger.getLogger(GatewayBackendReceiverStrategy.class);

	public GatewayBackendReceiverStrategy(XMPMain main) {
		super(main);
		registerHandlers();
	}

	@Override
	public void ReceiveMessage(XMPMessage m) {
		if (m.getFrom().getType() == NodeType.backend) {
			if (m.getTarget().equals(XMPNode.getSelf())) {
				// else, pass it on along to the real target.
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
		handlers.put(MessageType.IsRegistered, new BackendMessageCommandStrategy() {
			@Override
			public void HandleCommand(XMPMessage m) throws JSONException {
				XMPMessage response = m.createResponse();
				response.setContents(new JSONObject().put("IsRegistered", m.getFrom().isRegistered()));
				main.dispatch(response);
			}
		});
		handlers.put(MessageType.RegisterBackend, new BackendMessageCommandStrategy() {
			@Override
			public void HandleCommand(XMPMessage m) throws JSONException, SQLException {
				XMPMessage response = m.createResponse();
				XMPNode from = m.getFrom();
				from.setRegistered(true); // TODO: some kind of validation here, yeah? :p
				XMPDb.Nodes.update(from);
				
				response.setContents(new JSONObject().put("IsRegistered", m.getFrom().isRegistered()));
				main.dispatch(response);
			}
		});
		handlers.put(MessageType.RemoveBackend, new BackendMessageCommandStrategy() {
			@Override
			public void HandleCommand(XMPMessage m) throws JSONException, SQLException {
				XMPMessage response = m.createResponse();
				XMPNode from = m.getFrom();
				from.setRegistered(false); // TODO: some kind of validation here, yeah? :p
				XMPDb.Nodes.update(from);
				
				response.setContents(new JSONObject().put("IsRegistered", m.getFrom().isRegistered()));
				main.dispatch(response);
			}
		});
		handlers.put(MessageType.RequestDataPoints, new BackendMessageCommandStrategy() {
			@Override
			public void HandleCommand(XMPMessage m) throws JSONException, SQLException, AuthorizationFailureException {
				requireRegisteredBackend(m);
				// TODO: find out what data points haven't been sent to this backend yet.
				
				// so for each data point, find which ones dont have a message with this target.
				// this should probably be turned into some huge SQL clusterfuck but here we go...
				
				List<XMPDataPoint> unsent = new ArrayList<XMPDataPoint>();
				for (XMPDataPoint point : XMPDb.DataPoints) {
					boolean hasBeenSent = false;
					for (XMPMessage message : XMPDataPointMessages.messagesForPoint(point)) {
						if (message.getTarget().equals(m.getFrom())) {
							hasBeenSent = true;
							break;
						}
					}
					if (!hasBeenSent) {
						unsent.add(point);
					}
				}
				int maxDataPointsPerPacket = XMPConfig.getMaxDataPointsPerPacket();

				for (int i = 0; i * maxDataPointsPerPacket < unsent.size(); i++) {
					XMPMessage response = m.createResponse();
					response.setType(MessageType.DataPoints);
					
					List pts = new ArrayList();
					for (int j = 0; j < maxDataPointsPerPacket; j++) {
						XMPDataPoint point = unsent.get(j + maxDataPointsPerPacket * i);
						pts.add(point);
					}
					response.setContents(pts);
					
					main.dispatch(response);
				}
			}
		});
	}

	protected void requireRegisteredBackend(XMPMessage m) throws AuthorizationFailureException {
		if (m.getFrom().getType() != NodeType.backend || !m.getFrom().isRegistered()) {
			throw new AuthorizationFailureException();
		}
	}

}
