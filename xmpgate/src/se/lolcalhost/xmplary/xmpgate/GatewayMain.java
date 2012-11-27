package se.lolcalhost.xmplary.xmpgate;

import java.security.KeyPair;
import java.security.PublicKey;

import org.bouncycastle.jce.provider.X509CertificateObject;
import org.jivesoftware.smack.XMPPException;

import se.lolcalhost.xmplary.common.XMPConfig;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.ChatDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.LoggerReceiverStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy.MUCRoomStyle;

public class GatewayMain extends XMPMain {
	public GatewayMain(String conf) {
		super(conf);
		
		dispatchers.add(new MUCDispatchStrategy(this, MUCRoomStyle.ONLY_OUTPUT));
		dispatchers.add(new ChatDispatchStrategy(this));
		receivers.add(new LoggerReceiverStrategy(this));
		receivers.add(new GatewayBackendReceiverStrategy(this));
		receivers.add(new GatewayLeafReceiverStrategy(this));

		PeriodicUpdatesThread put = new PeriodicUpdatesThread(this);
		put.start();
		
		keepRunning();
	}

	/**
	 * @param args
	 * @throws XMPPException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws XMPPException,
			InterruptedException {
		String conf = "conf.xml";
		if (args.length > 0) {
			conf = args[0];
		}
		new GatewayMain(conf);
	}

}
