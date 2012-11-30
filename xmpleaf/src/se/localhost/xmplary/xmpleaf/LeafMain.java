package se.localhost.xmplary.xmpleaf;

import se.localhost.xmplary.xmpleaf.strategies.LeafGatewayReceiverStrategy;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.ChatDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.LoggerDispatcherStrategy;
import se.lolcalhost.xmplary.common.strategies.LoggerReceiverStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy.MUCRoomStyle;

public class LeafMain extends XMPMain {
	public LeafMain(String config) {
		super(config);

		WeldingThread wt = new WeldingThread(this);
		wt.start();

		dispatchers.add(new MUCDispatchStrategy(this, MUCRoomStyle.ONLY_OUTPUT));
		dispatchers.add(new ChatDispatchStrategy(this));
		dispatchers.add(new LoggerDispatcherStrategy(this));
		receivers.add(new LoggerReceiverStrategy(this));
		receivers.add(new LeafGatewayReceiverStrategy(this));
		receivers.add(new BackendReceiverStrategy(this));
		
		keepRunning();
	}
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String conf = "conf.xml";
		if (args.length > 0) {
			conf = args[0];
		}
		new LeafMain(conf);
	}

}
