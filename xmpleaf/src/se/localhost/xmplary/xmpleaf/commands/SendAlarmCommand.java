package se.localhost.xmplary.xmpleaf.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;

public class SendAlarmCommand extends Command {

	public SendAlarmCommand(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		XMPMessage msg = new XMPMessage();
		msg.setContents("Temperature High");
		msg.setType(MessageType.Alarm);
		msg.send();
	}

}
