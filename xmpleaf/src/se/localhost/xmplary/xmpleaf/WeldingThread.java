package se.localhost.xmplary.xmpleaf;

import java.util.HashMap;
import java.util.Random;

import se.localhost.xmplary.xmpleaf.commands.SendAlarmCommand;
import se.localhost.xmplary.xmpleaf.commands.SendStatus;
import se.localhost.xmplary.xmpleaf.commands.SendWeldingDatapointsCommand;
import se.localhost.xmplary.xmpleaf.commands.SetSampleValues;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;

public class WeldingThread extends Thread {

	private LeafMain main;
	private HashMap<XMPDataPoint.DataPointField, Double> status = new HashMap<XMPDataPoint.DataPointField, Double>();
	public enum WelderStatus {
		RUNNING,
		REFUELING,
		COOLINGDOWN,
		STOPPED
	}
	
	public WeldingThread(LeafMain main) {
		super("WeldingThread");
		this.main = main;
	}

	@Override
	public void run() {
		Random r = new Random();
		while (true) {
			SetSampleValues ssv = new SetSampleValues(main, this);
			ssv.schedule();
			
			SendStatus ss = new SendStatus(main, this);
			if (r.nextFloat() < 0.3) {
				SendAlarmCommand sac = new SendAlarmCommand(main, null);
				sac.schedule();
				// main.pushMessage(" -- ALARM: TEMPERATURE HIGH --");
			} else {
				// create a message to send
				SendWeldingDatapointsCommand cmd = new SendWeldingDatapointsCommand(main, null);
				cmd.schedule();
			}
			try {
				Thread.sleep((long) (r.nextFloat() * 1000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public HashMap<XMPDataPoint.DataPointField, Double> getStatus() {
		return status;
	}

}
