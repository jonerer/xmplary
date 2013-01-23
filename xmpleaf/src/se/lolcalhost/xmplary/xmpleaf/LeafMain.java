package se.lolcalhost.xmplary.xmpleaf;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.ChatDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.LoggerDispatcherStrategy;
import se.lolcalhost.xmplary.common.strategies.LoggerReceiverStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy.MUCRoomStyle;
import se.lolcalhost.xmplary.xmpleaf.strategies.LeafGatewayReceiverStrategy;

public class LeafMain extends XMPMain {
	
	protected LeafMain(String config) {
		super(config);
	}

	@Override
	public void init() throws java.sql.SQLException {
		WeldingThread wt = new WeldingThread(this);
		wt.start();

		dispatchers.add(new MUCDispatchStrategy(this, MUCRoomStyle.ONLY_OUTPUT));
		dispatchers.add(new ChatDispatchStrategy(this));
		dispatchers.add(new LoggerDispatcherStrategy(this));
		receivers.add(new LoggerReceiverStrategy(this));
		receivers.add(new LeafGatewayReceiverStrategy(this));
		receivers.add(new BackendReceiverStrategy(this));
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
