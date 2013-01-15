package se.lolcalhost.xmplary.xmpback.dump;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;

public class DatapointGraph extends Graph {
	public List<DatapointDataset> datasets = new ArrayList<DatapointDataset>();
	private DataPointField field;

	public DatapointGraph(String title, DataPointField field) {
		super(GraphType.TIMESERIES, title);
		this.field = field;
	}

	@Override
	String getXLabel() {
		return field.name();
	}
	
	public void addDataset(DatapointDataset set) {
		datasets.add(set);
	}
	
	@Override
	protected TimeSeriesCollection populate() {
		TimeSeriesCollection set = new TimeSeriesCollection();
		for (DatapointDataset dset : datasets) {
			TimeSeries s1 = new TimeSeries(dset.getOriginNode().getName());
			for (Object object : dset) {
				XMPDataPoint dp = (XMPDataPoint) object;
				s1.addOrUpdate(new FixedMillisecond(dp.getTime()), dp.getContents().get(field));
			}
			set.addSeries(s1);
		}
		return set;
	}
}
