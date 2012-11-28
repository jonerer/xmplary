package se.lolcalhost.xmplary.common.strategies;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;

public class LoggerDispatcherStrategy extends AbstractMessageDispatchStrategy {
	protected static Logger logger = Logger.getLogger(LoggerDispatcherStrategy.class);

	public LoggerDispatcherStrategy(XMPMain main) {
		super(main);
	}

	@Override
	public void DispatchMessage(XMPMessage msg) {
		String format = String
				.format("Sent message of type %s. (o->f->t). (%s->%s->%s) Contents: %s",
						msg.getType().toString(), msg.getOrigin().getName(),
						msg.getFrom().getName(), msg.getTarget().getName(),
						msg.getRawContents());
		logger.info(format);
	}

}
