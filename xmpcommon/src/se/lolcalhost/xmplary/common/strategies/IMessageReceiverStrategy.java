package se.lolcalhost.xmplary.common.strategies;

import org.jivesoftware.smack.packet.Message;

import se.lolcalhost.xmplary.common.models.XMPMessage;

public interface IMessageReceiverStrategy {
	public void PreparseReceiveMessage(Message m);
	public void ReceiveMessage(XMPMessage m);
	
}
