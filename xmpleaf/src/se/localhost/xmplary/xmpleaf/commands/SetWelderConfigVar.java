package se.localhost.xmplary.xmpleaf.commands;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import se.localhost.xmplary.xmpleaf.WelderConfig;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public class SetWelderConfigVar extends Command {

	public SetWelderConfigVar(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		String c = (String) msg.getContents();
		JSONObject cmd = new JSONObject(c);
		WelderConfig.setWelderConfigVar(cmd.getString("key"), cmd.getString("value"));
		XMPMessage mess = msg.createResponse();
		mess.setContents(WelderConfig.getWelderConfig());
		mess.send();
	}

}
