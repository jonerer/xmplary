package se.localhost.xmplary.xmpback.dump;

import java.util.Collection;
import java.util.Iterator;

import se.lolcalhost.xmplary.common.models.XMPDataPoint;

public class DatapointDataset extends GraphDataset {

	public DatapointDataset(Collection<?> items) {
		super(items);
	}

	@Override
	public Iterator iterator() {
		Collection<XMPDataPoint> col = (Collection<XMPDataPoint>) items;
		return col.iterator();
	}

}
