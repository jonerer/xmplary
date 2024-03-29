package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class Unregister extends Command {

	public Unregister(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws SQLException, JSONException {
		XMPMessage response = msg.createResponse();
		XMPNode from = msg.getFrom();
		from.setRegistered(false); // TODO: some kind of validation here, yeah? :p
		XMPDb.Nodes.update(from);
		
		response.setContents(new JSONObject().put("IsRegistered", msg.getFrom().isRegistered()));
		response.send();
	}

}
