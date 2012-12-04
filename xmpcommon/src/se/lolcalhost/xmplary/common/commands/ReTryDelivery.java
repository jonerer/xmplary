package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPDb;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;

/**
 * Find messages targeted to the same node as the one given in the constructor.
 * Those which were previously undeliverable (due to lacking registration) should be re-evaluated.
 * 
 * @author sx00042
 *
 */
public class ReTryDelivery extends Command {

	private XMPNode target;

	public ReTryDelivery(XMPMain main, XMPNode target) {
		super(main, null);
		this.target = target;
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		for (XMPMessage message : XMPDb.Messages.queryBuilder().where().eq(XMPMessage.TARGET, target).and()
				.eq(XMPMessage.DELIVERED, false).query()) {
			MessageDispatchCommand mdc = new MessageDispatchCommand(main, message);
			mdc.schedule();
		}
	}

}
