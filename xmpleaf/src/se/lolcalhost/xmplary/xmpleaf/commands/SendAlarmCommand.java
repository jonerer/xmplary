package se.lolcalhost.xmplary.xmpleaf.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.Alarm;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;

public class SendAlarmCommand extends Command {

	private Alarm alm;

	public SendAlarmCommand(XMPMain main, Alarm alm) {
		super(main, null);
		this.alm = alm;
		setPriority(CommandPriority.HIGH);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		XMPMessage msg = new XMPMessage();
		msg.setContents(alm);
		msg.setType(MessageType.Alarm);
		msg.send(priority);
	}

}
