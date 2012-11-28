package se.localhost.xmplary.xmpleaf.commands;

import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONException;

import se.localhost.xmplary.xmpleaf.LeafMain;
import se.localhost.xmplary.xmpleaf.WeldingThread;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;

public class SetSampleValues extends Command {

	private WeldingThread weldingThread;

	public SetSampleValues(LeafMain main, WeldingThread weldingThread) {
		super(main);
		this.weldingThread = weldingThread;
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		// this is executed once a second.
		HashMap<DataPointField, Double> status = weldingThread.getStatus();
		
		 /* the varaibles:
		  * Temperature, FuelDrain, FuelRemaining, Weldspeed, Cheeseburgers, VoodooMagic
		  * 
		  * Temperature should be some kind of differential equation :p
		  *  With a room temperature of 24 (also starting pos).
		  *  probably x' = x' + x'' * t
		  *           x = x + x' * t
		  *  And put something like -+ 0.5 on x'' every now and then. T is in hours.
		  *  When temperature reaches a max (80 c), it should stop the machine.
		  * 
		  * FuelDrain should be related to Weldspeed probably.
		  *  But keep it constant for now. It's 5.
		  * 
		  * FuelRemaining should be accurate.
		  *  Fuel should be refilled to max (20) every ~3 hours, with a variance enough that warnings should occur.
		  *  If a warning has been sent, refill after a little while.
		  *  If out of fuel, set weldspeed to 0 and let temperature drop rapidly.
		  * 
		  * Cheeseburgers per hour should be updated on average every 8'th hour (when someone new
		  * goes on shift). Should be around 0 to 4.
		  * 
		  * VoodooMagic should have a baseline and two sinus waves attached. 
		  * 	Baseline: 4
		  * 	One with a timespan over 24 hours and amplitude 2
		  *  One with a timespan of 4 hours and amplitude 1
		  *  
		  *  
		explanations.put(DataPointField.Temperature, "Temperature (c)");
		explanations.put(DataPointField.FuelDrain, "Fuel Drain (litres/hr)");
		explanations.put(DataPointField.FuelRemaining, "Fuel remaining (litres)");
		explanations.put(DataPointField.Weldspeed, "Weld speed (mm/sec)");
		explanations.put(DataPointField.Cheeseburgers, "Cheeseburgers consumed (/hour)");
		explanations.put(DataPointField.VoodooMagic, "Voodoo Magic (souls/hour)");
		  *  
		  */
	}

}
