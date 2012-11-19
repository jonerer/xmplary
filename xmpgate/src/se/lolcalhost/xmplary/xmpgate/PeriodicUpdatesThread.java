package se.lolcalhost.xmplary.xmpgate;

public class PeriodicUpdatesThread extends Thread {
	private static final long UPDATE_PERIOD_MILLIS = 120000;

	@Override
	public void run() {
		while(true) {
//			List<XMPNode> 
		try {
			Thread.sleep(UPDATE_PERIOD_MILLIS);
		}
		catch (InterruptedException e) {
		}
		}
	}
}
