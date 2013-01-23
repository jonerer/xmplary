package se.lolcalhost.xmplary.common;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPointMessages;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class XMPDb {
	public static Dao<XMPNode, String> Nodes;
	public static Dao<XMPMessage, String> Messages;
	public static Dao<XMPDataPoint, String> DataPoints;
	public static Dao<XMPDataPointMessages, String> DataPointMessages;
	protected static Logger logger = Logger.getLogger(XMPDb.class);
	private static ConnectionSource connectionSource;

	
	public static void init() {
		System.out.print("1");
		try {
//			System.setProperty("sqlite.purejava", "true");
//			Class.forName("org.sqlite.JDBC");
			Class.forName("org.hsqldb.jdbcDriver");
			
			// (always running in native mode to ensure determinism)
//			String mode = String.format(" running in %s mode ", SQLiteJDBCLoader.isNativeMode() ? "native" : "pure-java");
//			System.out.println("sqlitejdbc: " + mode);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		System.out.print("2");
//		String databaseUrl = "jdbc:sqlite:" + XMPConfig.Database();
//		String databaseUrl = "jdbc:sqlite::memory:";
		try {
			System.out.print("3");

//			connectionSource = new JdbcConnectionSource(databaseUrl);
			connectionSource = new JdbcConnectionSource("jdbc:hsqldb:file:"+XMPConfig.Database());
			System.out.print("4");

			// instantiate the DAO to handle Account with String id
			Nodes = DaoManager.createDao(connectionSource, XMPNode.class);
			Messages = DaoManager.createDao(connectionSource, XMPMessage.class);
			DataPoints = DaoManager.createDao(connectionSource, XMPDataPoint.class);
			DataPointMessages = DaoManager.createDao(connectionSource, XMPDataPointMessages.class);
	
//			Nodes.isTableExists()
			System.out.print("5");
			if (!Nodes.isTableExists()) {
				TableUtils.createTable(connectionSource, XMPNode.class);
			}
			if (!Messages.isTableExists()) {
				TableUtils.createTable(connectionSource, XMPMessage.class);
			}
			if (!DataPoints.isTableExists()) {
				TableUtils.createTable(connectionSource, XMPDataPoint.class);
			}
			if (!DataPointMessages.isTableExists()) {
				TableUtils.createTable(connectionSource, XMPDataPointMessages.class);
			}
			System.out.print("6");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


	public static void close() {
		if (connectionSource != null) {
			try {
				connectionSource.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void runAsTransaction(Callable<Void> r) throws SQLException {
		TransactionManager.callInTransaction(connectionSource, r);
	}


}
