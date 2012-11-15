package se.lolcalhost.xmplary;

import org.jivesoftware.smack.XMPPException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.ChatDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.LoggerReceiverStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;

public class GatewayMain extends XMPMain {
	public GatewayMain() {
		init();
		
		dispatchers.add(new MUCDispatchStrategy(this));
		dispatchers.add(new ChatDispatchStrategy(this));
		receivers.add(new LoggerReceiverStrategy(this));
		receivers.add(new GatewayBackendReceiverStrategy(this));
		receivers.add(new GatewayLeafReceiverStrategy(this));
		
		keepRunning();
	}

	/**
	 * @param args
	 * @throws XMPPException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws XMPPException,
			InterruptedException {
		new GatewayMain();
	}

}
