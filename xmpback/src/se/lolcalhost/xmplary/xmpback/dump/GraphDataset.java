package se.lolcalhost.xmplary.xmpback.dump;

import java.util.Collection;
import java.util.Iterator;

import se.lolcalhost.xmplary.common.models.XMPNode;

public abstract class GraphDataset implements Iterable {
	protected Collection<?> items;
	protected XMPNode originNode;

	public GraphDataset(Collection<?> items) {
		this.items = items;
	}
	
	public XMPNode getOriginNode() {
		return originNode;
	}

	public void setOriginNode(XMPNode originNode) {
		this.originNode = originNode;
	}
	
}
