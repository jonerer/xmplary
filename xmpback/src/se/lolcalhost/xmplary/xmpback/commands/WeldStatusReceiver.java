package se.lolcalhost.xmplary.xmpback.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.Status;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public class WeldStatusReceiver extends Command {
	public WeldStatusReceiver(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		Status alm = (Status) msg.getContents();
		String format = String.format("Status: %s. Text: %s. Origin is %s.",
				alm.getStatus().name(), alm.getStatusMessage(), msg.getOrigin().getName());
		logger.info(format);
		XMPMessage.tellOperator(format);
	}

}
