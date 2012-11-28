package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPCrypt;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;

public class RespondToRegistrationRequest extends Command {

	public RespondToRegistrationRequest(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		XMPMessage xmpMessage = new XMPMessage(MessageType.Register);
		xmpMessage.setContents(XMPCrypt.getCertificate());
		xmpMessage.setTarget(msg.getOrigin());
		xmpMessage.send();
	}

}
