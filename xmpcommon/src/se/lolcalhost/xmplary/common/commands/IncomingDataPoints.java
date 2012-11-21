package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPointMessages;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public class IncomingDataPoints extends Command {

	public IncomingDataPoints(XMPMain main, XMPMessage msg) {
		super(main, msg);
		setPriority(CommandPriority.LOW);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		final List<XMPDataPoint> points = (List<XMPDataPoint>) msg.getContents();
		
		// transact the datapoints into the DB.
		XMPDb.runAsTransaction(new Callable<Void>() {
			
			@Override
			public Void call() throws Exception {
				for (XMPDataPoint dataPoint : points) {
					dataPoint.save();
				}
				return null;
			}
		});
		
		// transact the message-datapoint-coupling into the db.
		XMPDb.runAsTransaction(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				for (XMPDataPoint dataPoint : points) {
					XMPDataPointMessages dpm = new XMPDataPointMessages(dataPoint, msg);
					dpm.save();
				}
				return null;
			}
		});

//		main.dispatchRaw(String.format("Received %d DataPoints from %s. Thanks for the business, please come again!", points.size(), msg.getFrom().getJID()));
	}

}
