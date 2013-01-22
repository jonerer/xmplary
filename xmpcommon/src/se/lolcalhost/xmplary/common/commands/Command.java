package se.lolcalhost.xmplary.common.commands;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPCommandRunner;
import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;

public abstract class Command implements Comparable<Command> {
	protected XMPMessage msg;
	protected XMPMain main;
	protected static Logger logger = Logger.getLogger(Command.class);
	protected CommandPriority priority;
	public enum CommandPriority {
		LOW,
		NORMAL,
		HIGH,
		INCOMING,
		SEMIURGENT,
		URGENT,
	}
	
	public Command(XMPMain main) {
		this(main, null);
	}

	public Command(XMPMain main, XMPMessage msg) {
		this.main = main;
		this.msg = msg;
		priority = CommandPriority.NORMAL;
	}
	
	public abstract void execute() throws JSONException, SQLException, AuthorizationFailureException;
	public void schedule() {
		XMPCommandRunner.scheduleCommand(this);
	}
	
	@Override
	public int compareTo(Command arg0) {
        if(this.priority.ordinal()<arg0.getPriority().ordinal()){
            return 1;
         }
         else if(this.priority.ordinal() == arg0.getPriority().ordinal()){
            return 0;
          }
         else{
            return -1;
          }
	}
	
	@Override
	public String toString() {
		return this.getClass().getName();
	};

	public CommandPriority getPriority() {
		return priority;
	}

	public void setPriority(CommandPriority priority) {
		this.priority = priority;
	}
	
	protected void requireRegisteredLeaf(XMPNode m) throws AuthorizationFailureException {
		if (m.getType() != NodeType.leaf || !m.isRegistered()) {
			throw new AuthorizationFailureException();
		}
	}
	
	protected void requireRegisteredBackend(XMPNode node)
			throws AuthorizationFailureException {
		if (node.getType() != NodeType.backend || !node.isRegistered()) {
			throw new AuthorizationFailureException();
		}
	}

	public boolean hasLogMessage() {
		return false;
	}

	public String logMessage() {
		return null;
	}
}
