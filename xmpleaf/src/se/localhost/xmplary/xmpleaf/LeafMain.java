package se.localhost.xmplary.xmpleaf;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.ChatDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy.MUCRoomStyle;

public class LeafMain extends XMPMain {
	public LeafMain(String config) {
		super(config);

		WeldingThread wt = new WeldingThread(this);
		wt.start();

		dispatchers.add(new MUCDispatchStrategy(this, MUCRoomStyle.ONLY_OUTPUT));
		dispatchers.add(new ChatDispatchStrategy(this));
		
		attemptRegistration();

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
