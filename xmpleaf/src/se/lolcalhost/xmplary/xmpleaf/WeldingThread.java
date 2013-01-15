package se.lolcalhost.xmplary.xmpleaf;

import java.util.HashMap;
import java.util.Random;

import org.joda.time.DateTime;

import se.lolcalhost.xmplary.common.Alarm;
import se.lolcalhost.xmplary.common.Alarm.AlarmTypes;
import se.lolcalhost.xmplary.common.Status.WelderStatus;
import se.lolcalhost.xmplary.common.XMPCommandRunner;
import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;
import se.lolcalhost.xmplary.xmpleaf.commands.SendAlarmCommand;
import se.lolcalhost.xmplary.xmpleaf.commands.SendStatus;
import se.lolcalhost.xmplary.xmpleaf.commands.SendWeldingDatapointsCommand;
import se.lolcalhost.xmplary.xmpleaf.commands.UpdateWelderValues;

public class WeldingThread extends Thread {

	private LeafMain main;
	private HashMap<XMPDataPoint.DataPointField, Double> data = new HashMap<XMPDataPoint.DataPointField, Double>();
	private DateTime lastStatusChange = new DateTime();
	private WelderStatus status = WelderStatus.STOPPED;
	private double VoodooSequence;
	
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
		data.put(DataPointField.Temperature, UpdateWelderValues.ROOM_TEMPERATURE);
	}

	@Override
	public void run() {
		Random r = new Random();
		VoodooSequence = r.nextDouble();
		initWelder();
		while (true) {
			UpdateWelderValues ssv = new UpdateWelderValues(main, this);
			ssv.schedule();
			
			SendWeldingDatapointsCommand cmd = new SendWeldingDatapointsCommand(main, this);
			cmd.schedule();
			
			
			if (r.nextFloat() < 0.05) {
				// send a "test alarm"
				Alarm a = new Alarm();
				a.setType(AlarmTypes.CHEESEBURGER_DROPPED);
				a.setErrorMessage("Someone dropped a cheeseburger on the circut board.");
				SendAlarmCommand sac = new SendAlarmCommand(main, a);
				sac.schedule();
			}
			try {
				Thread.sleep((long) (r.nextFloat() * 10000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int queuesize = XMPCommandRunner.getQueueSize();
			if (queuesize > 100) {
				// maybe send alarm here? or will that clog up the queue even more?
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

	public double getVoodooSequence() {
		return VoodooSequence;
	}

	public void setVoodooSequence(double voodooSequence) {
		VoodooSequence = voodooSequence;
	}

}
