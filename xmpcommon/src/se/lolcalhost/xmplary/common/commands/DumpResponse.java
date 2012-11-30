package se.lolcalhost.xmplary.common.commands;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.sql.SQLException;

import org.json.JSONException;

import se.lolcalhost.xmplary.common.XMPMain;
import se.lolcalhost.xmplary.common.exceptions.AuthorizationFailureException;
import se.lolcalhost.xmplary.common.models.XMPMessage;
import se.lolcalhost.xmplary.common.models.XMPMessage.MessageType;
import se.lolcalhost.xmplary.common.models.XMPNode;
import se.lolcalhost.xmplary.common.models.XMPNode.NodeType;

public class DumpResponse extends Command {

	public DumpResponse(XMPMain main, XMPMessage msg) {
		super(main, msg);
	}

	@Override
	public void execute() throws JSONException, SQLException,
			AuthorizationFailureException {
		if (msg.getTarget().equals(XMPNode.getSelf()) && msg.getOrigin().getType() == NodeType.backend) {
			ThreadMXBean bean = ManagementFactory.getThreadMXBean();
			ThreadInfo[] infos = bean.dumpAllThreads(true, true);
			StringBuffer threads = new StringBuffer();
	
			for (ThreadInfo info : infos) {
			  StackTraceElement[] elems = info.getStackTrace();
			  // Print out elements, etc.
			  String thread = info.toString();
			  threads.append(thread);
			}
			String dump = threads.toString();
			
			XMPMessage res = msg.createResponse(MessageType.DumpResponse);
			res.setContents(dump);
			res.send();
		}
	}

}
