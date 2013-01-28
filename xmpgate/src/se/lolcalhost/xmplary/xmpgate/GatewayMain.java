package se.lolcalhost.xmplary.xmpgate;

import java.security.KeyPair;
import java.security.PublicKey;
import java.sql.SQLException;

import org.bouncycastle.jce.provider.X509CertificateObject;
import org.jivesoftware.smack.XMPPException;

import se.lolcalhost.xmplary.common.XMPConfig;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.strategies.ChatDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.LoggerDispatcherStrategy;
import se.lolcalhost.xmplary.common.strategies.LoggerReceiverStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy;
import se.lolcalhost.xmplary.common.strategies.MUCDispatchStrategy.MUCRoomStyle;
import se.lolcalhost.xmplary.xmpgate.strategies.GatewayBackendReceiverStrategy;
import se.lolcalhost.xmplary.xmpgate.strategies.GatewayLeafReceiverStrategy;
import se.lolcalhost.xmplary.xmpgate.strategies.SwitchboardReceiverStrategy;

public class GatewayMain extends XMPMain {

	protected GatewayMain(String config) {
		super(config);
	}

	@Override
	public void init() throws SQLException {
		dispatchers.add(new MUCDispatchStrategy(this, MUCRoomStyle.ONLY_OUTPUT));
		dispatchers.add(new ChatDispatchStrategy(this));
		dispatchers.add(new LoggerDispatcherStrategy(this));
		receivers.add(new LoggerReceiverStrategy(this));
		receivers.add(new GatewayBackendReceiverStrategy(this));
		receivers.add(new GatewayLeafReceiverStrategy(this));
		
		receivers.add(new SwitchboardReceiverStrategy(this));

		PeriodicUpdatesThread put = new PeriodicUpdatesThread(this);
		put.start();
	}


	/**
	 * @param args
	 * @throws XMPPException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws XMPPException,
			InterruptedException {
		String conf = "conf.properties";
		if (args.length > 0) {
			conf = args[0];
		}
		new GatewayMain(conf);
	}

}
