package se.localhost.xmplary.xmpleaf.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.localhost.xmplary.xmpleaf.LeafMain;
import se.localhost.xmplary.xmpleaf.WeldingThread;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.commands.Command;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;

public class SendStatus extends Command {

	private WeldingThread weldingThread;

	public SendStatus(LeafMain main, WeldingThread weldingThread) {
		super(main);
		this.weldingThread = weldingThread;
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		
	}

}
