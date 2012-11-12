package se.lolcalhost.xmplary.common.strategies;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public abstract class MessageDispatchStrategy {
	protected XMPMain main;
	public MessageDispatchStrategy(XMPMain main) {
		this.main = main;
	}
	
	public abstract void DispatchMessage(XMPMessage mess);
}
