package se.localhost.xmplary.xmpback.commands;

import java.sql.SQLException;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;

import se.localhost.xmplary.xmpback.strategies.OperatorInputStrategy.InputCommandStrategy;
import se.lolcalhost.xmplary.common.Alarm;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;

/**
 * A lightweight command, used to get the InputCommandStrategies from OperatorInputStrategy into the same thread.
 * 
 * Yes, the names are confusing. But I prefer to just call them "Organic". It sounds better.
 * 
 * @author sx00042
 *
 */
public class OperatorInputCommand extends Command {
	private InputCommandStrategy strat;
	private Message m;

	public OperatorInputCommand(XMPMain main, Message m, InputCommandStrategy strat) {
		super(main, null);
		this.m = m;
		this.strat = strat;
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		strat.HandleCommand(m);
	}

}
