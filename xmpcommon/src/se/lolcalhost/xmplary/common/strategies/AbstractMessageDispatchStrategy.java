package se.lolcalhost.xmplary.common.strategies;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public abstract class AbstractMessageDispatchStrategy {
	protected XMPMain main;
	public AbstractMessageDispatchStrategy(XMPMain main) {
		this.main = main;
	}
	
	public abstract void DispatchMessage(XMPMessage mess);

	public void DispatchRawMessage(String msg) {
	}
}
