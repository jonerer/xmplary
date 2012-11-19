package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public abstract class Command {
	protected XMPMessage msg;
	protected XMPMain main;
	public Command(XMPMain main, XMPMessage msg) {
		this.main = main;
		this.msg = msg;
	}
	public abstract void execute() throws JSONException, SQLException, AuthorizationFailureException;
}
