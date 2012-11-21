package se.lolcalhost.xmplary.xmpgate.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

public class SendAlarmBacklog extends Command {

	private XMPNode from;

	public SendAlarmBacklog(XMPMain main, XMPMessage msg) {
		super(main, msg);
		setPriority(CommandPriority.LOW);
		from = msg.getFrom();
	}

	public SendAlarmBacklog(XMPMain main, XMPNode from) {
		super(main, null);
		setPriority(CommandPriority.LOW);
		this.from = from;
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		for (XMPMessage warning : XMPMessage.getAlarms()) {
			warning.forwardTo(from);
		}
	}

}
