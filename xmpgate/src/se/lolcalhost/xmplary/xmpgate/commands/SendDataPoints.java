package se.lolcalhost.xmplary.xmpgate.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
		final List<XMPNode> backends = XMPNode.getRegisteredBackends();
		// this should probably be turned into some huge SQL clusterfuck but
		// here we go...

		List<XMPDataPoint> unsent = target.getUnsentDataPoints();
		int maxDataPointsPerPacket = XMPConfig.getMaxDataPointsPerPacket();

		logger.info(String
				.format("Gateway has %d points that %s lacks. Commencing send, %d points per packet",
						unsent.size(), target.getName(), maxDataPointsPerPacket));

		int sent = 0;
		for (int i = 0; i * maxDataPointsPerPacket < unsent.size(); i++) {
			final XMPMessage response = new XMPMessage(MessageType.DataPoints);
			response.setTarget(target);
			response.setType(MessageType.DataPoints);

			final List<XMPDataPoint> pts = new ArrayList<XMPDataPoint>();
			for (int j = 0; j < maxDataPointsPerPacket; j++) {
				try {
					XMPDataPoint point = unsent.get(j + maxDataPointsPerPacket
							* i);
					XMPDb.Nodes.refresh(point.getFrom());
					pts.add(point);
					sent++;
				} catch (IndexOutOfBoundsException e) {
					break;
				}
			}
			response.setContents(pts);

			main.dispatchRaw(String.format(
					"Sent %d datapoints to %s. %d remaining.",
					pts.size(), response.getTarget().getName(),
					unsent.size() - sent));
			main.dispatch(response);

			// 1. save in the database that it has been sent to this backend.
			XMPDb.runAsTransaction(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					for (XMPDataPoint xmpDataPoint : pts) {
						XMPDataPointMessages dpm = new XMPDataPointMessages(
								xmpDataPoint, response);
						dpm.save();
					}
					return null;
				}
			});

			// 2: check if it has been sent to all. if so, denormalize and set
			// sent_to_all.
			XMPDb.runAsTransaction(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					for (XMPDataPoint xmpDataPoint : pts) {
						boolean sentToAll = true;
						List<XMPMessage> messagesForPoint = XMPDataPointMessages
								.messagesForPoint(xmpDataPoint);

						for (XMPNode backend : backends) {
							boolean hasBeenSent = false;
							for (XMPMessage xmpMessage : messagesForPoint) {
								if (xmpMessage.getTarget().equals(backend)) {
									hasBeenSent = true;
									break;
								}
							}
							if (!hasBeenSent) {
								sentToAll = false;
								break;
							}
						}
						if (sentToAll) {
							xmpDataPoint.setSentToAll(true);
							xmpDataPoint.save();
						}
					}
					return null;
				}
			});
		}
	}
}
