package se.lolcalhost.xmplary.xmpgate.commands;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class RegisterBackend extends Command {
	public RegisterBackend(XMPMain main, XMPMessage msg) {
		super(main, msg);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws SQLException, JSONException {
		XMPMessage response = msg.createResponse();
		XMPNode from = msg.getFrom();
		from.setRegistered(true); // TODO: some kind of validation here, yeah? :p
		XMPDb.Nodes.update(from);
		
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
		logger.info("New backend registered. Invalidated all SENT_TO_ALL on datapoints.");
		// make sure senddatapoints uses them.
		// make sure xmpnode.unsentDataPoints uses it.
		
		response.setContents(new JSONObject().put("IsRegistered", msg.getFrom().isRegistered()));
		main.dispatch(response);
	}

}
