package se.lolcalhost.xmplary.xmpgate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPointMessages;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.common.strategies.MessageReceiverStrategy;
import se.lolcalhost.xmplary.xmpgate.commands.IsRegistered;
import se.lolcalhost.xmplary.xmpgate.commands.RegisterBackend;
import se.lolcalhost.xmplary.xmpgate.commands.RemoveBackend;
import se.lolcalhost.xmplary.xmpgate.commands.SendDataPoints;

public class GatewayBackendReceiverStrategy extends MessageReceiverStrategy {
	public interface BackendMessageCommandStrategy {
		public void HandleCommand(XMPMessage m) throws JSONException, SQLException, AuthorizationFailureException;
	}

	HashMap<MessageType, Class<? extends Command>> hax = new HashMap<XMPMessage.MessageType, Class<? extends Command>>();
	HashMap<MessageType, BackendMessageCommandStrategy> handlers = new HashMap<MessageType, BackendMessageCommandStrategy>();
	protected static Logger logger = Logger.getLogger(GatewayBackendReceiverStrategy.class);

	public GatewayBackendReceiverStrategy(XMPMain main) {
		super(main);
		registerHandlers2();
	}

	@Override
	public void ReceiveMessage(XMPMessage m) {
		if (m.getFrom().getType() == NodeType.backend) {
			if (m.getTarget().equals(XMPNode.getSelf())) {
				// else, pass it on along to the real target.
				try {
//					handlers.get(m.getType()).HandleCommand(m);
					Class<? extends Command> c = hax.get(m.getType());
					Constructor<? extends Command> con = c.getConstructor(XMPMain.class, XMPMessage.class);
					Command command = con.newInstance(main, m);
					command.execute();
				} catch (JSONException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				} catch (SQLException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				} catch (AuthorizationFailureException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				} catch (SecurityException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				} catch (NoSuchMethodException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				} catch (IllegalArgumentException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				} catch (InstantiationException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				} catch (IllegalAccessException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				} catch (InvocationTargetException e) {
					logger.error("Error in gateway backend reciever handler: ", e);
				}
			}
		}
	}
	
	private void registerHandlers2() {
		hax.put(MessageType.IsRegistered, IsRegistered.class);
		hax.put(MessageType.RegisterBackend, RegisterBackend.class);
		hax.put(MessageType.RemoveBackend, RemoveBackend.class);
		hax.put(MessageType.RequestDataPoints, SendDataPoints.class);
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

				logger.info(String.format("Gateway has %d points that %s lacks. Commencing send, %d points per packet",
						unsent.size(), m.getFrom().getName(), maxDataPointsPerPacket));
				
				
				for (int i = 0; i * maxDataPointsPerPacket < unsent.size(); i++) {
					XMPMessage response = m.createResponse();
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
		});
	}

	protected void requireRegisteredBackend(XMPMessage m) throws AuthorizationFailureException {
		if (m.getFrom().getType() != NodeType.backend || !m.getFrom().isRegistered()) {
			throw new AuthorizationFailureException();
		}
	}

}
