package se.localhost.xmplary.xmpleaf.commands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;

public class SendWeldingDatapointsCommand extends Command {

	public SendWeldingDatapointsCommand(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		Random r = new Random();
		XMPMessage msg = new XMPMessage();
		msg.setType(MessageType.DataPoints);
		msg.setOutgoing(true);
		msg.save();

		// fill it up with a random set of data points.
		int numDatapoints = r.nextInt(10) + 1;
		List l = new ArrayList();
		for (int i = 0; i < numDatapoints; i++) {
			XMPDataPoint dp = new XMPDataPoint();
			Map<DataPointField, Double> contents = dp.getContents();
			contents.put(DataPointField.Temperature,
					r.nextDouble() * 20 + 40);
			dp.save();
			l.add(dp);
		}

		msg.setContents(l);
		msg.send();		
	}

}
