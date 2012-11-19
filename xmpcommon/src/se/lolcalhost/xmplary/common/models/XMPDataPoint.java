package se.lolcalhost.xmplary.common.models;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPConfig;
import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.interfaces.JSONSerializable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class XMPDataPoint implements JSONSerializable {
	public static enum DataPointField {
		Temperature, Current, Resistance 
		// be sure not to put field names that clash with properties that need to be serialized.
	}
	
	protected static Logger logger = Logger.getLogger(XMPDataPoint.class);

	public static final String ID = "id";
	@DatabaseField(columnName = ID, generatedId = true)
	private int id;

	/**
	 * A JSON field of the contents of the message.
	 */
	public static final String CONTENTS = "contents";
	@DatabaseField(canBeNull = false, columnName = CONTENTS, dataType = DataType.SERIALIZABLE)
	private HashMap<DataPointField, Double> contents = new HashMap<XMPDataPoint.DataPointField, Double>();

	public static final String ORIGINAL_ID = "original_id";
	@DatabaseField(columnName = ORIGINAL_ID)
	private int originalId;

	public static final String FROM = "from";
	@DatabaseField(canBeNull = false, columnName = FROM, foreign = true)
	private XMPNode from;

	public static final String RECEIVED = "received";
	@DatabaseField(columnName = RECEIVED)
	private Date received;

	public static final String TIME = "time";
	@DatabaseField(canBeNull = false, columnName = TIME)
	private Date time;

	public XMPDataPoint() {
		from = XMPNode.getSelf();
		time = new Date();
		received = new Date();
	}

	public XMPNode getFrom() {
		return from;
	}

	public void setFrom(XMPNode from) {
		this.from = from;
	}

	public Map<DataPointField, Double> getContents() {
		return contents;
	}

	public void setContents(HashMap<DataPointField, Double> contents) {
		this.contents = contents;
	}
	
	public void addMessage(XMPMessage message) throws SQLException {
		XMPDataPointMessages dpm = new XMPDataPointMessages(this, message);
		XMPDb.DataPointMessages.create(dpm);
	}

	public int getId() {
		return id;
	}

	public Date getReceived() {
		return received;
	}

	public void setReceived(Date received) {
		this.received = received;
	}

	public void writeObject(JSONObject jo) throws JSONException {
		for (DataPointField field : DataPointField.values()) {
			jo.put(field.name(), this.getContents().get(field));
		}
		jo.put("from", from.getJID());
		jo.put("original", getOriginalId());
		jo.put("time", XMPConfig.jsonDateFormat().format(time));
	}
	
	public void readObject(JSONObject jo) throws JSONException {
		for (DataPointField field : DataPointField.values()) {
			if (jo.has(field.name())) {
				contents.put(field, jo.getDouble(field.name()));
			}
		}
		from = XMPNode.getOrCreateByJID(jo.getString("from"));
		originalId = jo.getInt("original");
		try {
			time = XMPConfig.jsonDateFormat().parse(jo.getString("time"));
		} catch (ParseException e) {
			throw new JSONException(e);
		}
	}

	public int getOriginalId() {
		if (originalId == 0) {
			return id;
		}
		return originalId;
	}

	public void setOriginalId(int originalId) {
		this.originalId = originalId;
	}

	public void save() {
		try {
			XMPDb.DataPoints.createOrUpdate(this);
		} catch (SQLException e) {
			logger.error("Couldn't save xmp data point into DB.", e);
		}
	}

}
