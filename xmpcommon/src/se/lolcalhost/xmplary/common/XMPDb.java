package se.lolcalhost.xmplary.common;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class XMPDb {
	public static Dao<XMPNode, String> Nodes;
	public static Dao<XMPMessage, String> Messages;
	protected static Logger logger = Logger.getLogger(XMPDb.class);
	private static ConnectionSource connectionSource;

	
	public static void init() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		String databaseUrl = "jdbc:sqlite:database.db";
		try {
			connectionSource = new JdbcConnectionSource(databaseUrl);

			// instantiate the DAO to handle Account with String id
			Nodes = DaoManager.createDao(connectionSource, XMPNode.class);
			Messages = DaoManager.createDao(connectionSource, XMPMessage.class);
	
			// if you need to create the 'accounts' table make this call
			TableUtils.createTableIfNotExists(connectionSource, XMPNode.class);
			TableUtils.createTableIfNotExists(connectionSource, XMPMessage.class);
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
	
}
