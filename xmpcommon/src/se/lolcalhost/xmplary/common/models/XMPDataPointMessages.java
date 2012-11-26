package se.lolcalhost.xmplary.common.models;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.XMPDb;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class XMPDataPointMessages {

	protected static Logger logger = Logger.getLogger(XMPDataPointMessages.class);

	private static final String ID = "id";
	@DatabaseField(columnName = ID, generatedId = true)
	private int id;
	
	public static final String DATAPOINT = "datapoint_id";
	@DatabaseField(foreign=true, columnName=DATAPOINT, canBeNull=false)
	private XMPDataPoint datapoint;
	
	public static final String MESSAGE = "message_id";
	@DatabaseField(foreign=true, columnName=MESSAGE, canBeNull=false)
	private XMPMessage message;
	
	/**
	 * No-arg constructor for the ORM.
	 */
	public XMPDataPointMessages() {}
	
	public XMPDataPointMessages(XMPDataPoint datapoint, XMPMessage message) {
		this.datapoint = datapoint;
		this.message = message;
	}

	public static List<XMPDataPoint> pointsForMessage(XMPMessage m) throws SQLException {
		QueryBuilder<XMPDataPointMessages,String> dpmQb = XMPDb.DataPointMessages.queryBuilder();
		dpmQb.selectColumns(XMPDataPointMessages.DATAPOINT);
		SelectArg messageSelectArg = new SelectArg();
		dpmQb.where().eq(XMPDataPointMessages.MESSAGE, messageSelectArg);
		
		QueryBuilder<XMPDataPoint,String> messageQb = XMPDb.DataPoints.queryBuilder();
		messageQb.where().in(XMPMessage.ID, dpmQb);
		
		PreparedQuery<XMPDataPoint> prepare = messageQb.prepare();
		
		prepare.setArgumentHolderValue(0, m.getId());
		List<XMPDataPoint> query = XMPDb.DataPoints.query(prepare);
		return query;
	}
	
	public static List<XMPMessage> messagesForPoint(XMPDataPoint p) throws SQLException {
		QueryBuilder<XMPDataPointMessages,String> dpmQb = XMPDb.DataPointMessages.queryBuilder();
		dpmQb.selectColumns(XMPDataPointMessages.MESSAGE);
		SelectArg messageSelectArg = new SelectArg();
		dpmQb.where().eq(XMPDataPointMessages.DATAPOINT, messageSelectArg);
		
		QueryBuilder<XMPMessage,String> messageQb = XMPDb.Messages.queryBuilder();
		messageQb.where().in(XMPDataPoint.ID, dpmQb);
		
		PreparedQuery<XMPMessage> prepare = messageQb.prepare();
		
		prepare.setArgumentHolderValue(0, p.getId());
		List<XMPMessage> query = XMPDb.Messages.query(prepare);
		return query;
	}

	public void save() {
		try {
			XMPDb.DataPointMessages.createOrUpdate(this);
		} catch (SQLException e) {
			logger.error("Couldn't save data point message-connection: ", e);
		}
	}

}
