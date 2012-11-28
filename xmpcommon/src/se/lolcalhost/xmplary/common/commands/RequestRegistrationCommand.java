package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPCrypt;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;

public class RequestRegistrationCommand extends Command {

	public RequestRegistrationCommand(XMPMain main, XMPMessage msg) {
		super(main, msg);
		setPriority(CommandPriority.HIGH);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		XMPMessage resp = new XMPMessage(MessageType.RegistrationRequest);
		resp.setTarget(msg.getOrigin());
		resp.save();
		resp.send();
	}

}
