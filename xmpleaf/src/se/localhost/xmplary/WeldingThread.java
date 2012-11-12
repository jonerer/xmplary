package se.localhost.xmplary;

import java.util.Random;

import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;

public class WeldingThread extends Thread {
	
	private LeafMain main;

	public WeldingThread(LeafMain main) {
		this.main = main;
	}

	@Override
	public void run() {
		Random r = new Random();
		while(true) {
			try {
				if (r.nextFloat() < 0.9) {
					XMPMessage msg = new XMPMessage();
					msg.setContents("TEMPERATURE HIGH");
					msg.setType(MessageType.Alarm);
					msg.setTarget(XMPNode.getGateway());
					main.dispatch(msg);
//					main.pushMessage(" -- ALARM: TEMPERATURE HIGH --");
				} else {
					float newVal = r.nextFloat() * 100;
//					msg.setContents("TEMPERATURE HIGH");
//					msg.setType(MessageType.Alarm);
//					msg.setTarget(XMPNode.getGateway());
//					main.dispatch(msg);
					
//					main.pushMessage("Welding data: " + newVal);
				}
				Thread.sleep((long) (r.nextFloat() * 6000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
