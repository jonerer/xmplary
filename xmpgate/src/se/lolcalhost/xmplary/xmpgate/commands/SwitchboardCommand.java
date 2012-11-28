package se.lolcalhost.xmplary.xmpgate.commands;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;

public class SwitchboardCommand extends Command {

	public SwitchboardCommand(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		XMPNode targ = msg.getTarget();
		XMPNode orig = msg.getOrigin();
		if ((targ.getType() == NodeType.backend ||
				targ.getType() == NodeType.leaf) &&
				(orig.getType() == NodeType.backend ||
				orig.getType() == NodeType.leaf)) {
			if (targ.getType() != msg.getOrigin().getType()) {
				// seems like we should switch this message.
				// we'll serialize the incoming to copy it.
				JSONObject temp = new JSONObject();
				msg.writeObject(temp);
				XMPMessage passon = new XMPMessage(msg.getType());
				passon.readObject(temp);
				passon.send();
			}
		}
	}

}
