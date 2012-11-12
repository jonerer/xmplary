package se.lolcalhost.xmplary;

import org.jivesoftware.smack.XMPPException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.LoggerReceiverStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;

public class Main extends XMPMain {
	public Main() {
		init();
		
		dispatchers.add(new MUCDispatchStrategy(this));
		receivers.add(new LoggerReceiverStrategy(this));
		
		keepRunning();
	}

	/**
	 * @param args
	 * @throws XMPPException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws XMPPException,
			InterruptedException {
		new Main();
	}

}
