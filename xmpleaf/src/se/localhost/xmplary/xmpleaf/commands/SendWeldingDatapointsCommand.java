package se.localhost.xmplary.xmpleaf.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import org.json.JSONException;

import se.localhost.xmplary.xmpleaf.LeafMain;
import se.localhost.xmplary.xmpleaf.WeldingThread;
import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;

public class SendWeldingDatapointsCommand extends Command {

	private WeldingThread weldingThread;

	public SendWeldingDatapointsCommand(LeafMain main, WeldingThread weldingThread) {
		super(main);
		this.weldingThread = weldingThread;
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		XMPDb.runAsTransaction(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				XMPMessage msg = new XMPMessage();
				msg.setType(MessageType.DataPoints);
				msg.setOutgoing(true);
				msg.save();

				// fill it up with a random set of data points.
				int numDatapoints = 1;
				List l = new ArrayList();
				for (int i = 0; i < numDatapoints; i++) {
					XMPDataPoint dp = new XMPDataPoint();
					dp.setContents(weldingThread.getData());
					dp.save();
					l.add(dp);
				}

				msg.setContents(l);
				msg.send();
				return null;
			}
		});	
	}

}
