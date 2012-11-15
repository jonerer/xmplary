package se.localhost.xmplary;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.ChatDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;

public class BackMain extends XMPMain {
	
	public BackMain() {
		init();

		dispatchers.add(new MUCDispatchStrategy(this));
		dispatchers.add(new ChatDispatchStrategy(this));
		receivers.add(new OperatorInputStrategy(this));
		receivers.add(new BackendGatewayReceiverStrategy(this));

		keepRunning();
	}

	public static void main(String[] args) {
		new BackMain();
	}

}
