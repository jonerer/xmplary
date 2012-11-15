package se.lolcalhost.xmplary.common.strategies;

import org.jivesoftware.smack.packet.Message;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public abstract class MessageDispatchStrategy {
	protected XMPMain main;
	public MessageDispatchStrategy(XMPMain main) {
		this.main = main;
	}
	
	public abstract void DispatchMessage(XMPMessage mess);

	public void DispatchRawMessage(String msg) {
	}
}
