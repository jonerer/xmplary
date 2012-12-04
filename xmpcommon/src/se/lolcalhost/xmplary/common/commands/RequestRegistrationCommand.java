package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPCrypt;
import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class RequestRegistrationCommand extends Command {

	private XMPNode target;

	public RequestRegistrationCommand(XMPMain main, XMPMessage msg) {
		super(main, msg);
		target = msg.getOrigin();
		setPriority(CommandPriority.SEMIURGENT);
	}
	
	public RequestRegistrationCommand(XMPMain main, XMPNode target) {
		super(main, null);
		this.target = target;
		setPriority(CommandPriority.SEMIURGENT);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		XMPDb.Nodes.refresh(target);
		if (!target.isRegistered()) {
			XMPMessage resp = new XMPMessage(MessageType.RegistrationRequest);
			resp.setTarget(target);
			resp.save();
			resp.send(priority);
		}
	}

}
