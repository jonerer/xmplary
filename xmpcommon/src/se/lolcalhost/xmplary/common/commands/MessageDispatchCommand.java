package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;

/**
 * Dispatch a message. This function is available via the XMPMessage.send() comfortability function.
 * So you probably wont have to call this one directly.
 * 
 * @param xmp
 */
public class MessageDispatchCommand extends Command {

	public MessageDispatchCommand(XMPMain main, XMPMessage msg) {
		super(main, msg);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		// TODO: should stuff be signed here?
		msg.setDelivered(false);
		msg.setOutgoing(true);

		msg.save(); // save before signing so i'm sure it has an ID.
		if (msg.shouldSign()) {
			msg.sign();
		}
		if (msg.shouldEncrypt()) {
			if (msg.getTarget().isRegistered() == false) {
				RequestRegistrationCommand cmd = new RequestRegistrationCommand(main, msg.getTarget());
				cmd.schedule();
				this.schedule();
				msg.save();
				return;
			}
			msg.encrypt();
		}
		msg.setDelivered(true); // TODO: maybe do this later, after getting
		// an ACK?
		msg.save();
		main.sendToDispatchers(msg);
	}

}
