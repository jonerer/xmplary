package se.lolcalhost.xmplary.xmpgate.commands;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class MulticastToBackends extends Command {

	public MulticastToBackends(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		for (XMPNode backend : XMPNode.getRegisteredBackends()) {
//			XMPMessage forward = new XMPMessage(msg.getType());
//			forward.setTarget(backend);
//			forward.setOrigin(msg.getOrigin());
//			forward.setOriginalId(msg.getOriginalId());
//			forward.setSignature(msg.getSignature());
//			forward.setContents(msg.getContents());
//			forward.send();
			
			// seems like we should switch this message.
			// we'll serialize the incoming to copy it.
			JSONObject temp = new JSONObject();
			msg.writeObject(temp);
			XMPMessage passon = new XMPMessage(msg.getType());
			passon.readObject(temp);
			passon.setTarget(backend);
			passon.send();
		}
	}
	
}
