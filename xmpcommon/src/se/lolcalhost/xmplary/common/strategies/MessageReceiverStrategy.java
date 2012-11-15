package se.lolcalhost.xmplary.common.strategies;

import org.jivesoftware.smack.packet.Message;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public abstract class MessageReceiverStrategy {
	protected XMPMain main;
	public MessageReceiverStrategy(XMPMain main) {
		this.main = main;
	}
	
	public void PreparseReceiveMessage(Message m) {}
	public abstract void ReceiveMessage(XMPMessage m);
}
