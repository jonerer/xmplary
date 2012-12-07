package se.localhost.xmplary.xmpleaf.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.localhost.xmplary.xmpleaf.WelderConfig;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;

public class SendWelderConfig extends Command {

	public SendWelderConfig(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		XMPMessage mess = msg.createResponse();
		mess.setContents(WelderConfig.getWelderConfig());
		mess.send();
	}

}
