package se.lolcalhost.xmplary.xmpgate;

import java.util.List;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;
import se.lolcalhost.xmplary.xmpgate.commands.RunPeriodicUpdate;
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
				RunPeriodicUpdate rpu = new RunPeriodicUpdate(main);
				rpu.schedule();

			try {
				Thread.sleep(UPDATE_PERIOD_MILLIS);
			} catch (InterruptedException e) {
			}
		}
	}
}
