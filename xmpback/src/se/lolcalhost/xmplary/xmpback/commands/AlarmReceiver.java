package se.lolcalhost.xmplary.xmpback.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.Alarm;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public class AlarmReceiver extends Command {
	public AlarmReceiver(XMPMain main, XMPMessage msg) {
		super(main, msg);
		setPriority(CommandPriority.HIGH);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		Alarm alm = (Alarm) msg.getContents();
		String format = String.format("ALARM: %s. Text: %s. Origin is %s.",
				alm.getType().name(), alm.getErrorMessage(), msg.getOrigin().getName());
		logger.info(format);
		XMPMessage.tellOperator(format);
		main.dispatchRaw(format);
	}

}
