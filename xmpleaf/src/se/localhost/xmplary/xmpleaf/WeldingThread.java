package se.localhost.xmplary.xmpleaf;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;

public class WeldingThread extends Thread {

	private LeafMain main;

	public WeldingThread(LeafMain main) {
		super("WeldingThread");
		this.main = main;
	}

	@Override
	public void run() {
		Random r = new Random();
		while (true) {
			if (r.nextFloat() < 0.5) {
				XMPMessage msg = new XMPMessage();
				msg.setContents("Temperature High");
				msg.setType(MessageType.Alarm);
				main.dispatch(msg);
				// main.pushMessage(" -- ALARM: TEMPERATURE HIGH --");
			} else {
				// create a message to send
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
					contents.put(DataPointField.Current, r.nextDouble() * 100);
					contents.put(DataPointField.Resistance,
							r.nextDouble() * 200 + 1000);
					contents.put(DataPointField.Temperature,
							r.nextDouble() * 20 + 40);
					dp.save();
					l.add(dp);
				}

				msg.setContents(l);
				main.dispatch(msg);
			}
			// main.pushMessage("Welding data: " + newVal);
			try {

				Thread.sleep((long) (r.nextFloat() * 6000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
