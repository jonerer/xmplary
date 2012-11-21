package se.lolcalhost.xmplary.xmpgate.commands;

import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public class IsRegistered extends Command {
	public IsRegistered(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws JSONException {
		XMPMessage response = msg.createResponse();
		response.setContents(new JSONObject().put("IsRegistered", msg.getFrom().isRegistered()));
		response.send();
	}

}
