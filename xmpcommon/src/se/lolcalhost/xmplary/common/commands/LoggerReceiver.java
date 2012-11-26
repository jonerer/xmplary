package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class LoggerReceiver extends Command {
	public LoggerReceiver(XMPMain main, XMPMessage msg) {
		super(main, msg);
		setPriority(CommandPriority.LOW);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		String format = String.format("Received message of type %s with contents %s. Sender is %s. I am %s.", msg.getType().toString(),
				msg.getRawContents(), msg.getFrom().getName(), XMPNode.getSelf().getName());
		logger.info(format);
	}

}
