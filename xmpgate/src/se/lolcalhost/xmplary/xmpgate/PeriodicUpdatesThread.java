package se.lolcalhost.xmplary.xmpgate;

import java.util.List;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.xmpgate.commands.SendDataPoints;

public class PeriodicUpdatesThread extends Thread {
	protected static Logger logger = Logger
			.getLogger(PeriodicUpdatesThread.class);

	private static final long UPDATE_PERIOD_MILLIS = 120000;

	private XMPMain main;

	public PeriodicUpdatesThread(XMPMain main) {
		super("PeriodicUpdatesThread");
		this.main = main;
	}

	@Override
	public void run() {
		while (true) {
			try {
				List<XMPNode> reg_gateways = XMPNode.getRegisteredBackends();
				for (XMPNode xmpNode : reg_gateways) {
					List<XMPDataPoint> unsentDataPoints = xmpNode
							.getUnsentDataPoints();
					if (unsentDataPoints.size() > 0) {
						SendDataPoints sdp = new SendDataPoints(main, xmpNode);
						sdp.schedule();
					}
				}
			} catch (Exception e1) {
				logger.error("Error in period updates thread: ", e1);
			}

			try {
				Thread.sleep(UPDATE_PERIOD_MILLIS);
			} catch (InterruptedException e) {
			}
		}
	}
}
