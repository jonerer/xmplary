package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public class ReValidateCommand extends Command {

	public ReValidateCommand(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		final List<XMPMessage> query = XMPDb.Messages.queryBuilder().where().eq(XMPMessage.ORIGIN, msg.getOrigin()).and().eq(XMPMessage.VERIFIED, false).query();
		XMPDb.runAsTransaction(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				for (XMPMessage xmpMessage : query) {
					if (xmpMessage.verify()) {
						main.runReceiveHandlers(msg);
						msg.save();
					}
				}
				return null;
			}
		});
		
	}

}
