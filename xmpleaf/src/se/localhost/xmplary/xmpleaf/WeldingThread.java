package se.localhost.xmplary.xmpleaf;

import java.util.HashMap;
import java.util.Random;

import org.joda.time.DateTime;

import se.localhost.xmplary.xmpleaf.commands.SendAlarmCommand;
import se.localhost.xmplary.xmpleaf.commands.SendStatus;
import se.localhost.xmplary.xmpleaf.commands.SendWeldingDatapointsCommand;
import se.localhost.xmplary.xmpleaf.commands.UpdateWelderValues;
import se.lolcalhost.xmplary.common.XMPCommandRunner;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;

public class WeldingThread extends Thread {

	private LeafMain main;
	private HashMap<XMPDataPoint.DataPointField, Double> data = new HashMap<XMPDataPoint.DataPointField, Double>();
	private DateTime lastStatusChange = new DateTime();
	private WelderStatus status = WelderStatus.STOPPED;
	
	public enum WelderStatus {
		RUNNING,
		REFUELING,
		COOLINGDOWN,
		STOPPED, 
		AWAIT_REFUEL
	}
	
	public WeldingThread(LeafMain main) {
		super("WeldingThread");
		this.main = main;
	}
	
	protected void initWelder() {
		data.put(DataPointField.Cheeseburgers, 2d);
		data.put(DataPointField.FuelDrain, 0d);
		data.put(DataPointField.FuelRemaining, 10d);
		data.put(DataPointField.VoodooMagic, 2d);
		data.put(DataPointField.Weldspeed, 2d);
		data.put(DataPointField.Temperature, 2d);
	}

	@Override
	public void run() {
		Random r = new Random();
		initWelder();
		while (true) {
			UpdateWelderValues ssv = new UpdateWelderValues(main, this);
			ssv.schedule();
			
			SendWeldingDatapointsCommand cmd = new SendWeldingDatapointsCommand(main, this);
			cmd.schedule();
			
			
			if (r.nextFloat() < 0.1) {
				// send a "test alarm"
				SendAlarmCommand sac = new SendAlarmCommand(main, null);
				sac.schedule();
			}
			try {
				Thread.sleep((long) (r.nextFloat() * 100000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int queuesize = XMPCommandRunner.getQueueSize();
			if (queuesize > 100) {
				// send an alarm about the queue
			}
			
		}
	}

	public HashMap<XMPDataPoint.DataPointField, Double> getData() {
		return data;
	}

	public DateTime getLastStatusChange() {
		return lastStatusChange;
	}

	public WelderStatus getStatus() {
		return status;
	}

	public void setStatus(WelderStatus status) {
		lastStatusChange = new DateTime();
		this.status = status;
	}

}
