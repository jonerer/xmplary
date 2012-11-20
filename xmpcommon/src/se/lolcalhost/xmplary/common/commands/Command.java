package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPCommandRunner;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public abstract class Command {
	protected XMPMessage msg;
	protected XMPMain main;
	protected static Logger logger = Logger.getLogger(Command.class);

	public Command(XMPMain main, XMPMessage msg) {
		this.main = main;
		this.msg = msg;
	}
	public abstract void execute() throws JSONException, SQLException, AuthorizationFailureException;
	public void schedule() {
		XMPCommandRunner.scheduleCommand(this);
	}
}
