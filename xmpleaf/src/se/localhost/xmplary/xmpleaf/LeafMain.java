package se.localhost.xmplary.xmpleaf;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.ChatDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy.MUCRoomStyle;

public class LeafMain extends XMPMain {
	public LeafMain() {
		init();
		WeldingThread wt = new WeldingThread(this);
		wt.start();

		dispatchers.add(new MUCDispatchStrategy(this, MUCRoomStyle.ONLY_OUTPUT));
		dispatchers.add(new ChatDispatchStrategy(this));

		keepRunning();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new LeafMain();
	}

}
