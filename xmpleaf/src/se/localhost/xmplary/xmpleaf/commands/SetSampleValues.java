package se.localhost.xmplary.xmpleaf.commands;

import java.lang.Thread.State;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONException;

import se.localhost.xmplary.xmpleaf.LeafMain;
import se.localhost.xmplary.xmpleaf.WeldingThread;
import se.localhost.xmplary.xmpleaf.WeldingThread.WelderStatus;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;

public class SetSampleValues extends Command {
	private static final double WELDSPEED = 4;
	private static final double FUEL_REFILL_DRAIN = -0.1;
	private static final double FUEL_DRAIN = 0.01;
	private static final int OVERHEAT_THRESHOLD = 100;
	private static final int COOLDOWN_THRESHOLD = 60;
	private static final int FUEL_DRAIN_THRESHOLD = 1;
	private static final int FUEL_FULL_THRESHOLD = 20;
	private static final double ROOM_TEMPERATURE = 24;
	
	private WeldingThread weldingThread;
	private HashMap<DataPointField, Double> data;
	private WelderStatus state;

	public SetSampleValues(LeafMain main, WeldingThread weldingThread) {
		super(main);
		this.weldingThread = weldingThread;
	}

	private boolean isRunning() {
		return state == WelderStatus.RUNNING;
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		data = weldingThread.getData();
		state = weldingThread.getStatus();

		/*
		 * States: RUNNING, REFUELING, COOLINGDOWN, STOPPED
		 * 
		 * the varaibles: Temperature, FuelDrain, FuelRemaining, Weldspeed,
		 * Cheeseburgers, VoodooMagic
		 * 
		 * Temperature should be some kind of differential equation :p With a
		 * room temperature of 24 (also starting pos). probably x' = x' + x'' *
		 * t x = x + x' * t And put something like -+ 0.5 on x'' every now and
		 * then. T is in hours. When temperature reaches a max (80 c), it should
		 * stop the machine.
		 * 
		 * FuelDrain should be related to Weldspeed probably. But keep it
		 * constant for now. It's 0.01.
		 * 
		 * FuelRemaining should be accurate. Fuel should be refilled to max (20)
		 * every ~3 hours, with a variance enough that warnings should occur. If
		 * a warning has been sent, refill after a little while. If out of fuel,
		 * set weldspeed to 0 and let temperature drop rapidly.
		 * 
		 * Weldspeed could be constant at 4 (or 0 when not running)
		 * 
		 * Cheeseburgers per hour should be updated on average every 8'th hour
		 * (when someone new goes on shift). Should be around 0 to 4.
		 * 
		 * VoodooMagic should have a baseline and two sinus waves attached.
		 * Baseline: 4 One with a timespan over 24 hours and amplitude 2 One
		 * with a timespan of 4 hours and amplitude 1
		 * 
		 * 
		 * explanations.put(DataPointField.Temperature, "Temperature (c)");
		 * explanations.put(DataPointField.FuelDrain, "Fuel Drain (litres/hr)");
		 * explanations.put(DataPointField.FuelRemaining,
		 * "Fuel remaining (litres)");
		 * explanations.put(DataPointField.Weldspeed, "Weld speed (mm/sec)");
		 * explanations.put(DataPointField.Cheeseburgers,
		 * "Cheeseburgers consumed (/hour)");
		 * explanations.put(DataPointField.VoodooMagic,
		 * "Voodoo Magic (souls/hour)");
		 */

		/* 1. Check if it's time to make a state transition */
		Random r = new Random();
		switch (state) {
		case STOPPED:
			weldingThread.setStatus(WelderStatus.RUNNING);
			break;
		case COOLINGDOWN:
			if (data.get(DataPointField.Temperature) < COOLDOWN_THRESHOLD) {
				weldingThread.setStatus(WelderStatus.RUNNING);
			}
			break;
		case REFUELING:
			if (data.get(DataPointField.FuelRemaining) > FUEL_FULL_THRESHOLD) {
				weldingThread.setStatus(WelderStatus.RUNNING);
			}
			break;
		case RUNNING:
			if (data.get(DataPointField.Temperature) > OVERHEAT_THRESHOLD) {
				weldingThread.setStatus(WelderStatus.COOLINGDOWN);
			} else if (data.get(DataPointField.FuelRemaining) < FUEL_DRAIN_THRESHOLD) {
				weldingThread.setStatus(WelderStatus.AWAIT_REFUEL);
			}
			break;
		case AWAIT_REFUEL:
			if (r.nextInt(100) < 10) {
				weldingThread.setStatus(WelderStatus.REFUELING);
			}
			break;
		}

		state = weldingThread.getStatus();
		/* 2. Apply current state */
		double fueldrain = data.get(DataPointField.FuelDrain);
		double temp = data.get(DataPointField.Temperature);
		double fuelremain = data.get(DataPointField.FuelRemaining);
		double weldspeed = data.get(DataPointField.Weldspeed);
		double cheeseburgers = data.get(DataPointField.Cheeseburgers);
		double voodoomagic = data.get(DataPointField.VoodooMagic);

		
		switch (state) {
		case RUNNING:
			fueldrain = FUEL_DRAIN;
			temp = temp + 0.1;
			weldspeed = WELDSPEED;
			break;
		case REFUELING:
			fueldrain = FUEL_REFILL_DRAIN;
		case COOLINGDOWN:
		case AWAIT_REFUEL:
		case STOPPED:
			fueldrain = 0;
			weldspeed = 0;
			temp = Math.max(temp - 0.2, ROOM_TEMPERATURE);
			break;
		}
		cheeseburgers = 3;
		voodoomagic = 4;
		fuelremain = fuelremain - fueldrain;
		
		data.put(DataPointField.FuelDrain, fueldrain);
		data.put(DataPointField.Temperature, temp);
		data.put(DataPointField.FuelRemaining, fuelremain);
		data.put(DataPointField.Weldspeed, weldspeed);
		data.put(DataPointField.Cheeseburgers, cheeseburgers);
		data.put(DataPointField.VoodooMagic, voodoomagic);
	}

}
