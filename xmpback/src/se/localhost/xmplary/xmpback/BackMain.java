package se.localhost.xmplary.xmpback;

import se.localhost.xmplary.xmpback.strategies.BackendGatewayReceiverStrategy;
import se.localhost.xmplary.xmpback.strategies.OperatorInputStrategy;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.ChatDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.LoggerDispatcherStrategy;
import se.lolcalhost.xmplary.common.strategies.LoggerReceiverStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy.MUCRoomStyle;

public class BackMain extends XMPMain {
	
	public BackMain(String conf) {
		super(conf);

		dispatchers.add(new MUCDispatchStrategy(this, MUCRoomStyle.INPUT_OUTPUT));
		dispatchers.add(new ChatDispatchStrategy(this));
		dispatchers.add(new LoggerDispatcherStrategy(this));
		receivers.add(new OperatorInputStrategy(this));
		receivers.add(new LoggerReceiverStrategy(this));
		receivers.add(new BackendGatewayReceiverStrategy(this));

		attemptRegistration();

		keepRunning();
	}

	public static void main(String[] args) {
		String conf = "conf.xml";
		if (args.length > 0) {
			conf = args[0];
		}
		new BackMain(conf);
	}

}
