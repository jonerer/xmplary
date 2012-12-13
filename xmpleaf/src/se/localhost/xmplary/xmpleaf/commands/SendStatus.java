package se.localhost.xmplary.xmpleaf.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.Status;
import se.lolcalhost.xmplary.common.Status.WelderStatus;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;

public class SendStatus extends Command {
	private Status status;

	public SendStatus(XMPMain main, WelderStatus stat, String message) {
		super(main, null);
		
		status = new Status(stat, message);
		setPriority(CommandPriority.HIGH);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		XMPMessage msg = new XMPMessage();
		msg.setContents(status);
		msg.setType(MessageType.WelderStatus);
		msg.send(priority);
	}


}
