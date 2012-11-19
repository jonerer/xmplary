package se.localhost.xmplary.xmpback;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.ChatDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy.MUCRoomStyle;

public class BackMain extends XMPMain {
	
	public BackMain() {
		init();

		dispatchers.add(new MUCDispatchStrategy(this, MUCRoomStyle.INPUT_OUTPUT));
		dispatchers.add(new ChatDispatchStrategy(this));
		receivers.add(new OperatorInputStrategy(this));
		receivers.add(new BackendGatewayReceiverStrategy(this));

		keepRunning();
	}

	public static void main(String[] args) {
		new BackMain();
	}

}
