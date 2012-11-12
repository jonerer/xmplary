package se.localhost.xmplary;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.LeafDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;

public class LeafMain extends XMPMain {
	public LeafMain() {
		init();
		WeldingThread wt = new WeldingThread(this);
		wt.start();

		dispatchers.add(new MUCDispatchStrategy(this));
		dispatchers.add(new LeafDispatchStrategy(this));

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
