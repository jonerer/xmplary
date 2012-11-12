package se.localhost.xmplary;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class WeldingThread extends Thread {

	private LeafMain main;

	public WeldingThread(LeafMain main) {
		this.main = main;
	}

	@Override
	public void run() {
		Random r = new Random();
		while (true) {
			try {
				if (r.nextFloat() < 0.1) {
					XMPMessage msg = new XMPMessage();
					msg.setContents("TEMPERATURE HIGH");
					msg.setType(MessageType.Alarm);
					msg.setTarget(XMPNode.getGateway());
					main.dispatch(msg);
					// main.pushMessage(" -- ALARM: TEMPERATURE HIGH --");
				} else {
					// create a message to send
					XMPMessage msg = new XMPMessage();
					msg.setType(MessageType.DataPoints);
					msg.setTarget(XMPNode.getGateway());
					XMPDb.Messages.create(msg);
					
					// fill it up with a random set of data points.
					int numDatapoints = r.nextInt(10)+1;
					for (int i = 0; i < numDatapoints; i++) {
						XMPDataPoint dp = new XMPDataPoint();
						HashMap<DataPointField, Float> contents = dp.getContents();
						contents.put(DataPointField.Current, r.nextFloat() * 100);
						contents.put(DataPointField.Resistance,
								r.nextFloat() * 200 + 1000);
						contents.put(DataPointField.Temperature,
								r.nextFloat() * 20 + 40);
						dp.setMessage(msg);
						XMPDb.DataPoints.create(dp);
					}

					// msg.setContents("TEMPERATURE HIGH");
					// msg.setType(MessageType.Alarm);
					// msg.setTarget(XMPNode.getGateway());
					main.dispatch(msg);
					msg.setDelivered(true);
					XMPDb.Messages.update(msg);
				}
				// main.pushMessage("Welding data: " + newVal);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {

				Thread.sleep((long) (r.nextFloat() * 6000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
