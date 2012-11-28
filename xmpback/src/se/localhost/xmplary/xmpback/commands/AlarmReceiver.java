package se.localhost.xmplary.xmpback.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.commands.Command.CommandPriority;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class AlarmReceiver extends Command {
	public AlarmReceiver(XMPMain main, XMPMessage msg) {
		super(main, msg);
		setPriority(CommandPriority.HIGH);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		String format = String.format("ALARM: %s. Origin is %s.",
				msg.getRawContents(), msg.getOrigin().getName());
		logger.info(format);
		XMPMessage.tellOperator(format);
		main.dispatchRaw(format);
	}

}
