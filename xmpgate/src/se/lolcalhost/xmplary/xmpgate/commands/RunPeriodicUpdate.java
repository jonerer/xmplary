package se.lolcalhost.xmplary.xmpgate.commands;

import java.sql.SQLException;
import java.util.List;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class RunPeriodicUpdate extends Command {

	public RunPeriodicUpdate(XMPMain main) {
		super(main);
		setPriority(CommandPriority.LOW);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		List<XMPNode> reg_gateways = XMPNode.getRegisteredBackends();
		for (XMPNode xmpNode : reg_gateways) {
			List<XMPDataPoint> unsentDataPoints = xmpNode
					.getUnsentDataPoints();
			if (unsentDataPoints.size() > 0) {
				SendDataPoints sdp = new SendDataPoints(main, xmpNode);
				sdp.schedule();
			}
		}
	}

}
