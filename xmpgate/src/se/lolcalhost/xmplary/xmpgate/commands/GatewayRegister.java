package se.lolcalhost.xmplary.xmpgate.commands;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Register;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;

public class GatewayRegister extends Register {
	public GatewayRegister(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws SQLException, JSONException {
		super.execute();
		if (from.getType() == NodeType.backend) {
			XMPDb.runAsTransaction(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					for (XMPDataPoint datapoint : XMPDb.DataPoints.queryForAll()) {
						datapoint.setSentToAll(false);
						XMPDb.DataPoints.update(datapoint);
					}
					return null;
				}
			});
			logger.info("New backend registered. Invalidated all SENT_TO_ALL on datapoints. Also scheduling a datapoint-send.");
			SendDataPoints sdp = new SendDataPoints(main, from);
			sdp.schedule();
			SendAlarmBacklog ala = new SendAlarmBacklog(main, from);
			ala.schedule();
		} else {
			logger.info("New " + from.getType().name() + " registered.");
		}
	}

}
